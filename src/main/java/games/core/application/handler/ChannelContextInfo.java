package games.core.application.handler;

import com.google.protobuf.Message;
import games.core.application.handler.websocket.ProtobufHandler;
import games.core.constant.Platform;
import games.core.constant.ResponseBeanConstant;
import games.core.constant.ResponseErrorCodeConst;
import games.core.user.User;
import games.tx.TaiXiuGame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import libs.util.LogFactory;
import message.XProtos;
import message.XProtos.DisconectBean;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;

import static games.core.constant.ServerCommandConstant.USER_COMMAND;
/**
 * Created by Cuong Nguyen Cao on 8/8/17.
 */
public class ChannelContextInfo
{
    //Logger log;
    private static Logger logger = LogFactory.getLogger(ChannelContextInfo.class.getSimpleName());
    protected io.netty.channel.ChannelHandlerContext ctx;
    protected boolean isWebContext;

    private int userId;
    private User user;
    private boolean isAuth;


    //last time channle send request to server
    private int lastTimeInteractive;

    //force quit when duplicate account. when channle offline, if force quit = true => don't remove on channle service
    private boolean forceQuit;

    private String deviceId = "";

    private String macAddress = "";

    private String imie = "";

    private String ipAddress = "";

    private Platform platform = Platform.UNKNOWN;

    private int version = 0;

    private String source = "";
    /**
     *
     */
    public ChannelContextInfo(io.netty.channel.ChannelHandlerContext context, boolean isWebContext) {
        //MetaHelper.inject(this);
        this.ctx = context;
        this.userId = 0;
        this.isAuth = false;
        this.forceQuit = false;
        this.deviceId = "";
        this.macAddress = "";
        this.ipAddress = "";
        this.imie = "";
        platform = Platform.UNKNOWN;
        version = 0;
        lastTimeInteractive = (int)ZonedDateTime.now().toEpochSecond();
        this.source = "";

        this.isWebContext = isWebContext;
    }

    public ChannelContextInfo() {
        //MetaHelper.inject(this);
    }

    public void init(String deviceId, String macAddress, String imie, String ipAddress, Platform platform, int version, String source) {
        this.deviceId = deviceId;
        this.macAddress = macAddress;
        this.imie = imie;
        this.ipAddress = ipAddress;
        this.platform = platform;
        this.version = version;
        this.source = source;
    }

    public boolean isForceQuit() {
        return forceQuit;
    }

    public void setForceQuit(boolean forceQuit) {
        this.forceQuit = forceQuit;
    }

    public int getLastTimeInteractive() {
        return lastTimeInteractive;
    }

    public void setLastTimeInteractive(int lastTimeInteractive) {
        this.lastTimeInteractive = lastTimeInteractive;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public User getUser(){
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public boolean isAuth(){
        return isAuth;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getImie() {
        return imie;
    }

    public void setImie(String imie) {
        this.imie = imie;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getSource() {
        return source;
    }

    public boolean checkAuth() {
        return checkAuth(true);
    }

    public boolean checkAuth(boolean disconnect){
        if (disconnect && (!isAuth() || userId == 0)){
            disconnect(ResponseErrorCodeConst.REC_USER_NOT_AUTH);
        }
        return isAuth() && userId != 0;
    }

    public void disconnect(int errorCode){
        logger.info("Disconnect - User[" + userId + "] - errorCode[" + errorCode + "]");
        DisconectBean.Builder responseBean = DisconectBean.newBuilder();
        XProtos.XMessage.Builder xMessageResponse = XProtos.XMessage.newBuilder();
        responseBean.setErrorCode(errorCode);
        xMessageResponse.setCommand(USER_COMMAND).setBeanType(ResponseBeanConstant.DISCONNECT_RESPONSE_BEAN);
        xMessageResponse.setData(responseBean.build().toByteString());
        ctx.writeAndFlush(xMessageResponse.build());
    }

    public void pingToClient(int beanType) {
        XProtos.XMessage.Builder xmessageResponse= XProtos.XMessage.newBuilder();
        xmessageResponse.setCommand(USER_COMMAND).setBeanType(beanType);
        ctx.writeAndFlush(xmessageResponse.build());
    }

    public void close() {
        this.ctx.close();
    }

    public Channel getChannel() {
        return this.ctx.channel();
    }

    public ChannelHandlerContext getCtx() {
        return this.ctx;
    }

    public ChannelFuture writeAndFlush(Object msg)
    {
        if (isWebContext)
        {
            byte[] data = null;
            if (msg instanceof XProtos.XMessage.Builder) {
                data = ((XProtos.XMessage.Builder)msg).build().toByteArray();
            } else if (msg instanceof XProtos.XMessage) {
                data = ((XProtos.XMessage)msg).toByteArray();
            } else {
                throw new UnsupportedOperationException(msg.toString());
            }
            ByteBuf buff = Unpooled.copiedBuffer(data);
            ByteBuf outBuff = Unpooled.buffer();
            try {
                ProtobufHandler.encode(buff, outBuff);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //logger.debug("--- Sent data " + outBuff.readableBytes());

            BinaryWebSocketFrame frame = new BinaryWebSocketFrame(outBuff);
            return this.ctx.writeAndFlush(frame);
        }
        else
        {
            return this.ctx.writeAndFlush(msg);
        }
    }

    public ChannelFuture push(int commandId, int actionId){
        XProtos.XMessage.Builder protoResponse = XProtos.XMessage.newBuilder();
        protoResponse.setCommand(commandId).setBeanType(actionId);
        return writeAndFlush(protoResponse.build());
    }

    public ChannelFuture push(int commandId, int actionId, Message message){
        XProtos.XMessage.Builder protoResponse = XProtos.XMessage.newBuilder();
        protoResponse.setCommand(commandId).setBeanType(actionId);
        protoResponse.setData(message.toByteString());
        return writeAndFlush(protoResponse.build());
    }

    public ChannelFuture push(int commandId, int actionId, Message.Builder builder){
        return push(commandId, actionId, builder.build());
    }
}
