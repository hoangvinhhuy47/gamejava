package games.features.inbox;

import com.google.protobuf.InvalidProtocolBufferException;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.constant.ServerCommandConstant;
import games.features.constant.RequestBeanFeatureConst;
import games.features.constant.ResponseBeanFeatureConst;
import games.features.database.document.Inbox;
import message.FeatureProtos.*;
import message.XProtos;

import java.util.List;

/**
 * Created by tuanhoang on 8/21/17.
 */
public class InboxCommand extends ICommand implements ResponseBeanFeatureConst, RequestBeanFeatureConst {
    private static InboxCommand instance;
    public static InboxCommand getInstance() {
        if (instance == null) instance = new InboxCommand();
        return instance;
    }

    private InboxService inboxService;
    InboxCommand() {
        inboxService = InboxService.getInstance();
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        int userId = ctx.getUserId();
        switch (message.getBeanType()) {
            case REQUEST_LIST_INBOX_MESSAGE_BEAN:
                {
                List<Inbox> listInbox = inboxService.getListInbox(userId);
                ListInboxMessageResponseBean.Builder listMessageResponse = ListInboxMessageResponseBean.newBuilder();
                for (Inbox inbox : listInbox)
                {
                    InboxMessageBean inboxMessageBean = parseInboxToProto(inbox);
                    listMessageResponse.addListInboxMessage(inboxMessageBean);
                }
                XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder().
                        setCommand(ServerCommandConstant.INBOX_COMMAND).
                        setBeanType(LIST_INBOX_MESSAGE_RESPONSE_BEAN).
                        setData(listMessageResponse.build().toByteString());
                ctx.writeAndFlush(responseMessage.build());
            }
                break;
            case REQUEST_OPEN_INBOX_MESSAGE_BEAN: {
                OpenInboxMessageRequestBean openInboxMessageRequestBean = OpenInboxMessageRequestBean.parseFrom(message.getData());
                long inboxId = openInboxMessageRequestBean.getInboxId();
                int errorCode = inboxService.openInbox(userId, inboxId);

                OpenInboxMessageResponseBean.Builder responseBean = OpenInboxMessageResponseBean.newBuilder();
                responseBean.setErrorCode(errorCode);
                responseBean.setInboxId(inboxId);

                XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder().
                        setCommand(ServerCommandConstant.INBOX_COMMAND).
                        setBeanType(OPEN_INBOX_MESSAGE_RESPONSE_BEAN).
                        setData(responseBean.build().toByteString());
                ctx.writeAndFlush(responseMessage.build());
            }
                break;
            case REQUEST_REMOVE_INBOX_MESSAGE_BEAN:{
                RemoveInboxMessageRequestBean requestBean = RemoveInboxMessageRequestBean.parseFrom(message.getData());

                long inboxID = requestBean.getInboxId();
                int errorCode = inboxService.removeInbox(userId, inboxID);

                RemoveInboxMessageResponseBean.Builder responseBean = RemoveInboxMessageResponseBean.newBuilder();
                responseBean.setErrorCode(errorCode);
                responseBean.setInboxId(inboxID);

                XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder().
                        setCommand(ServerCommandConstant.INBOX_COMMAND).
                        setBeanType(REMOVE_INBOX_MESSAGE_RESPONSE_BEAN).
                        setData(responseBean.build().toByteString());
                ctx.writeAndFlush(responseMessage.build());
            }
                break;
            case REQUEST_SEND_INBOX_MESSAGE_BEAN:
            {
                SendInboxMessageRequestBean requestBean = SendInboxMessageRequestBean.parseFrom(message.getData());
                int error_code = this.inboxService.sendMessage(userId, requestBean.getToId(), requestBean.getTitle(), requestBean.getContent());

                SendInboxMessageResponseBean.Builder responseBean = SendInboxMessageResponseBean.newBuilder();
                responseBean.setErrorCode(error_code);

                XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder().
                        setCommand(ServerCommandConstant.INBOX_COMMAND).
                        setBeanType(SEND_INBOX_MESSAGE_RESPONSE_BEAN).
                        setData(responseBean.build().toByteString());
                ctx.writeAndFlush(responseMessage.build());
            }
                break;
        }
    }

    InboxMessageBean parseInboxToProto(Inbox inbox) {
        InboxMessageBean.Builder inboxMessageBean = InboxMessageBean.newBuilder();
        inboxMessageBean.setInboxId(inbox.getId());
        inboxMessageBean.setFrom(inbox.getFromNickName());
        inboxMessageBean.setTo(inbox.getToNickName());
        inboxMessageBean.setTitle(inbox.getTitle());
        inboxMessageBean.setContent(inbox.getContent());
        inboxMessageBean.setTimestamp(inbox.getTimestamp());
        inboxMessageBean.setIsRead(inbox.isRead());
        return inboxMessageBean.build();
    }
}
