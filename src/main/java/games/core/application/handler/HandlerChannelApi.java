package games.core.application.handler;

import games.api.*;
import games.core.application.ServerConfig;
import games.core.common.money.MoneyService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import libs.util.JsonUtil;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ChannelHandler.Sharable
public class HandlerChannelApi extends SimpleChannelInboundHandler{

    private static UserSection userSection = UserSection.getInstance();
    private static HandlerChannelApi instance = new HandlerChannelApi();

    private static String apiPrefix = ServerConfig.getInstance().getApiPrefixUrl();

    private static Logger logger = LogFactory.getLogger(HandlerChannelApi.class.getSimpleName());
    public static HandlerChannelApi getInstance() {
        if (instance == null) instance = new HandlerChannelApi();
        return instance;
    }

    private HandlerChannelApi() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
    {
        HttpResponseStatus status = HttpResponseStatus.OK;
        ApiReturnObject returnObject = new ApiReturnObject();
        try {
            //Nếu không phải HTTP request
            if (!(msg instanceof HttpRequest))
            {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                status = HttpResponseStatus.NOT_FOUND;
                throw new Exception("NOT HTTP REQUEST");
            }
            FullHttpRequest request = (FullHttpRequest) msg;
            //Nếu không bắt đầu bằng "/api
            if(!request.uri().startsWith(apiPrefix))
            {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                status = HttpResponseStatus.NOT_FOUND;
                throw new Exception("NOT START WITH /api");
            }

            //=====Neu thoa dieu kien=======
            Map<String, Object> dataRequest = new HashMap<>();
            HttpMethod method = request.method();
//        System.out.println(method);
            if (method == HttpMethod.GET)
            {
                QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
                Map<String, List<String>> dataFromRequest = decoder.parameters();
                for (String key : dataFromRequest.keySet()){
                    dataRequest.put(key, dataFromRequest.get(key).get(0));
                }
            }
            else if (method == HttpMethod.POST)
            {
                ByteBuf data = request.content();
                if (data.isReadable())
                {
                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
                    decoder.offer(request);
                    List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

                    for (InterfaceHttpData parm : parmList) {
                        Attribute datap = (Attribute) parm;
                        dataRequest.put(datap.getName(), datap.getValue());
                    }
                }
            }


            //Lay url request
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            String[] segments = decoder.path().split("/");
            if (segments.length < 2) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                return;
            }
            String target = segments[segments.length - 2];
            String action = segments[segments.length - 1];
            switch (action) {
                case "user":
                    returnObject = userSection.process(action, dataRequest);
                    break;
                case "text":
                    returnObject = TextRunSection.getInstance().process(action, dataRequest);
                    break;
                case "message":
                    returnObject = MessageSection.getInstance().process(action, dataRequest);
                    break;
                case "cash-out":
                    returnObject = CashoutSection.getInstance().process(action, dataRequest);
                    break;
                case "systems":
                    returnObject = SystemsSection.getInstance().process(action, dataRequest);
                    break;
                case "withdrawal":
                    returnObject = MoneyService.getInstance().Withdrawal(dataRequest);
                    break;
                default:
                    returnObject.setData("NOT FOUND ACTION!");
            }

        } catch (Exception e){
            logger.error(e);
            returnObject.setData(e.getMessage());
        } finally {
            writeResponse(ctx, returnObject, status);
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, ApiReturnObject result, HttpResponseStatus status) {
        if (result != null)
            writeResponse(ctx, JsonUtil.generateJson(result), status);
        else
            writeResponse(ctx, "", HttpResponseStatus.NOT_FOUND);
    }

    private void writeResponse(ChannelHandlerContext ctx, String result, HttpResponseStatus status) {
        //boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
        response.headers().add("Access-Control-Allow-Origin", "*");
        response.headers().add("Access-Control-Allow-Methods", "GET,POST");
        response.headers().add("Content-Type", "application/json");
        //response.headers().add("Access-Control-Allow-Headers", "*");
        ctx.write(response);
        //if (!keepAlive)
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

}
