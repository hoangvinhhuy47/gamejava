package games.core.common;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.constant.RequestBeanConstant;
import games.core.constant.ResponseErrorCodeConst;
import games.core.user.User;
import games.core.user.UserService;
import libs.util.LogFactory;
import message.CommonProtos;
import message.XProtos;
import org.apache.log4j.Logger;

import java.util.Map;

import static games.core.constant.ResponseBeanConstant.CCU_RESPONSE_BEAN;
import static games.core.constant.ResponseBeanConstant.CHAT_RESPONSE_BEAN;
import static games.core.constant.ServerCommandConstant.COMMON_COMMAND;

public class CommonCommand extends ICommand implements RequestBeanConstant, ResponseErrorCodeConst {
    private static CommonCommand instance;
    private Logger logger;
    public static CommonCommand getInstance() {
        if (instance == null) {
            instance = new CommonCommand();
        }
        return instance;
    }

    CommonCommand() {
        userService = UserService.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }
    private static UserService userService;

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        int type = message.getBeanType();
        if (ctx.checkAuth()){
            switch (type){
                case CHAT_REQUEST_BEAN:
                    processChat(ctx, message);
                    break;
            }
        }

    }

    private void processChat(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException{
        CommonProtos.ChatRequestBean chatRequestBean = CommonProtos.ChatRequestBean.parseFrom(message.getData());
        CommonProtos.ChatResponseBean.Builder chatResponseBean = CommonProtos.ChatResponseBean.newBuilder();
        try{
            User user = UserService.getInstance().getUser(ctx.getUserId());
            if (user != null) {
                chatResponseBean.setUserId(ctx.getUserId())
                        .setMessage(chatRequestBean.getMessage())
                        .setNickname(user.getNickName());
                chatResponseBean.setErrorCode(REC_SUCCESS);
                ByteString byteString = chatResponseBean.build().toByteString();
                sendMessageToAll(byteString, CHAT_RESPONSE_BEAN);
            }

        } catch (Exception e){
            chatResponseBean.setErrorCode(REC_NOT_DEFINE);
            sendMessage(ctx, chatResponseBean.build().toByteString(), CHAT_RESPONSE_BEAN);
        }
    }

    public void sendCCUResponse(int ccu) {
        CommonProtos.CCUResponse.Builder ccuResponse = CommonProtos.CCUResponse.newBuilder();
        ccuResponse.setCcu(userService.getListActiveUsers().size());
        sendMessageToAll(ccuResponse.build().toByteString(), CCU_RESPONSE_BEAN);
    }

    private void sendMessageToAll(ByteString responseBeanByteString, short responseCode) {
        try {
            XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
            responseMessage.setCommand(COMMON_COMMAND)
                    .setBeanType(responseCode)
                    .setData(responseBeanByteString);
            XProtos.XMessage xMessage = responseMessage.build();
            Map<Integer, User> listAuthUser = userService.getListAthUser();
            for (User user : listAuthUser.values()){
                if (!user.isBot()) {
                    user.getCtx().writeAndFlush(xMessage);
                }
            }
        } catch (Exception e) {
            logger.error("Send message to all error - " + responseCode, e);
        }

    }

    private void sendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, short responseCode){
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(COMMON_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);
        ctx.writeAndFlush(responseMessage.build());
    }
}
