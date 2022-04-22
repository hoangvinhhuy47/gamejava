package games.features.friends;

import com.google.protobuf.InvalidProtocolBufferException;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.features.constant.RequestBeanFeatureConst;
import libs.util.LogFactory;
import message.FeatureProtos;
import message.XProtos;
import org.apache.log4j.Logger;

/**
 * Created by tuanhoang on 9/15/17.
 */
public class FriendsCommand extends ICommand implements RequestBeanFeatureConst{
    private static FriendsCommand instance;
    public static FriendsCommand getInstance() {
        if (instance == null) instance = new FriendsCommand();
        return instance;
    }
    private Logger logger;
    private FriendsService friendsService;
    FriendsCommand() {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        friendsService = FriendsService.getInstance();
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        if (!ctx.checkAuth()) {
            return;
        }
        switch (message.getBeanType()) {
            case LIST_USER_ONLINE_REQUEST_BEAN:
                friendsService.getListUserOnline(ctx);
                break;
            case ADD_FRIEND_REQUEST_BEAN:
                FeatureProtos.AddFriendRequestBean addFriendRequestBean = FeatureProtos.AddFriendRequestBean.parseFrom(message.getData());
                friendsService.requestAddFriend(ctx, addFriendRequestBean.getFriendId());
                break;
            case ACCEPT_FRIEND_REQUEST_BEAN:
                FeatureProtos.AcceptFriendRequestBean acceptFriendRequestBean = FeatureProtos.AcceptFriendRequestBean.parseFrom(message.getData());
                friendsService.requestAcceptFriend(ctx, acceptFriendRequestBean.getFriendId());
                break;
            case DECLINE_FRIEND_REQUEST_BEAN:
                FeatureProtos.DeclineFriendRequestBean declineFriendRequestBean = FeatureProtos.DeclineFriendRequestBean.parseFrom(message.getData());
                friendsService.requestDeclineFriend(ctx, declineFriendRequestBean.getFriendId());
                break;
            case REMOVE_FRIEND_REQUEST_BEAN:
                FeatureProtos.RemoveFriendRequestBean removeFriendRequestBean = FeatureProtos.RemoveFriendRequestBean.parseFrom(message.getData());
                friendsService.requestRemoveFriend(ctx, removeFriendRequestBean.getFriendId());
                break;
            case LIST_FRIEND_REQUEST_BEAN:
                friendsService.getListFriends(ctx);
                break;
            case LIST_ASK_FRIEND_REQUEST_BEAN:
                friendsService.getListAskFriends(ctx);
                break;
            case FIND_FRIEND_REQUEST_BEAN:
                FeatureProtos.FindFriendRequestBean requestBean = FeatureProtos.FindFriendRequestBean.parseFrom(message.getData());
                friendsService.findFriend(ctx,requestBean.getFriendId());
                break;
        }
    }
}
