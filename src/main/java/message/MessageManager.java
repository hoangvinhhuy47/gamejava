package message;

import games.core.application.handler.ChannelContextInfo;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {

    private static MessageManager instance;

    public static MessageManager getInstance() {
        if (instance == null) instance = new MessageManager();
        return instance;
    }

    /**
     * @param ctx ChannelContextInfo wanna send to client
     * @param message XMessage wanna send to client
     * @return true if ctx != null
     */

    public boolean send(ChannelContextInfo ctx, XProtos.XMessage message){
        if (ctx != null){
            ctx.writeAndFlush(message);
            return true;
        }
        return false;
    }

    /**
     * @param ctxs list ctx wanna send
     * @param message XMessage wanna send
     * @return true if all ctx of ctxs != null
     */
    public boolean send(ChannelContextInfo[] ctxs, XProtos.XMessage message){
        boolean flag = true;
        for (ChannelContextInfo ctx : ctxs){
            if (!send(ctx, message)) flag = false;
        }
        return flag;
    }

    /**
     * @param hashChannelMessage HashMap<ChannelContextInfo, XMessage>
     * @return true if all key of HashMap != null
     */
    public boolean send(HashMap<ChannelContextInfo, XProtos.XMessage> hashChannelMessage){
        boolean flag = true;
        for (Map.Entry<ChannelContextInfo, XProtos.XMessage> entry : hashChannelMessage.entrySet()){
            if (!send(entry.getKey(), entry.getValue())) flag = false;
        }
        return flag;
    }

}
