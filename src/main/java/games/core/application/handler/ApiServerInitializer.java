package games.core.application.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;


public class ApiServerInitializer extends ChannelInitializer<SocketChannel> {
    private HandlerChannelApi handler;

    public ApiServerInitializer(HandlerChannelApi handler) {
        this.handler = handler;
    }


    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandler(60, 30, 0));
        HttpRequestDecoder requestDecoder = new HttpRequestDecoder();

        p.addLast(new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(new HttpResponseEncoder());
        p.addLast(new HttpObjectAggregator(100 * 1024)// 1048576
        {
            @Override
            protected void handleOversizedMessage(final ChannelHandlerContext ctx, HttpMessage oversized) throws Exception {
                //TODO oversize http request
            }
        });
        p.addLast(handler);

    }
}
