package games.core.application.constant;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import games.core.application.handler.ChannelContextInfo;
import games.core.proto.ProtoSerializer;
import libs.util.LogFactory;
import message.XProtos;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by tuanhoang on 4/13/17.
 */
public abstract class ICommand {
    public abstract void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException, NoSuchFieldException, IllegalAccessException, IOException;

    protected void pushToClient(ChannelContextInfo ctxInfo, int actionCode, ProtoSerializer protoSerializer){
        pushToClient(ctxInfo, actionCode, protoSerializer.getProtoMessage());
    }

    protected void pushToClient(ChannelContextInfo ctxInfo, int actionCode, Message.Builder builder){
        pushToClient(ctxInfo, actionCode, builder.build());
    }

    public void pushToClient(ChannelContextInfo ctxInfo, int actionCode, Message message){
        ctxInfo.push(getCommandCode(), actionCode, message);
    }

    protected void pushToClient(ChannelContextInfo ctxInfo, int actionCode){
        ctxInfo.push(getCommandCode(), actionCode);
    }

    public int getCommandCode(){
        return 0;
    }

    public Logger getLogger(){
        return LogFactory.getLogger(this.getClass().getSimpleName());
    }
}
