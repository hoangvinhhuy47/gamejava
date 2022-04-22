package games.features.friends;

import com.google.protobuf.ByteString;
import games.core.application.handler.ChannelContextInfo;
import games.core.constant.ServerCommandConstant;
import games.core.user.User;
import games.core.user.UserService;
import games.features.constant.ResponseBeanFeatureConst;
import games.features.constant.ResponseFeatureErrorCode;
import games.features.database.document.Friends;
import games.features.database.model.MFriends;
import games.features.inbox.InboxService;
import libs.util.CRandom;
import libs.util.LogFactory;
import message.FeatureProtos;
import message.XProtos;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tuanhoang on 9/15/17.
 */
public class FriendsService implements ResponseFeatureErrorCode, ResponseBeanFeatureConst{
    private static FriendsService instance;
    public static FriendsService getInstance() {
        if (instance == null) instance = new FriendsService();
        return instance;
    }
    UserService userService;
    InboxService inboxService;
    MFriends mFriends;
    private Logger logger;
    private int LIMIT_FRIEND  = 10;
    FriendsService() {
        userService = UserService.getInstance();
        mFriends = MFriends.getInstance();
        inboxService = InboxService.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    public void getListUserOnline(ChannelContextInfo ctx) {
        FeatureProtos.ListFriendBean.Builder listFriendBean = FeatureProtos.ListFriendBean.newBuilder();

        try {
            listFriendBean.setErrorCode(REC_SUCCESS);
            Collection<User> listUser = userService.getListActiveUsers();
            Collection<User> listUserRand = new ArrayList<>();
            if (listUser.size() < LIMIT_FRIEND) {
                listUserRand = listUser;
            } else {
                CRandom cRandom = new CRandom();
                for (int i = 0; i < LIMIT_FRIEND; i++) {
                    User user = null;
                    do {
                        int randIndex = cRandom.randInt(0, listUser.size() -1);
                        user = (User) listUser.toArray()[randIndex];
                    } while (listUserRand.contains(user));
                    if (user != null) {
                        listUserRand.add(user);
                    }
                }
            }
            for (User user : listUserRand) {
                listFriendBean.addListFriends(user.parseToFriendBean(false));
            }

        } catch (Exception e) {
            listFriendBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("getListUserOnline", e);
        }
        sendMessage(ctx, listFriendBean.build().toByteString(), LIST_USER_ONLINE_RESPONSE_BEAN);
    }

    public List<Integer> listFriend(int userId) {
        try {
            Friends friends = mFriends.getFriendById(userId);
            List<Integer> listFriendIds = friends.getListFriendIds();
            return listFriendIds;
        } catch (Exception e) {
            logger.error("get list friend error", e);
        }
        return null;
    }
    public void getListFriends(ChannelContextInfo ctx) {
        FeatureProtos.ListFriendBean.Builder listFriendBean = FeatureProtos.ListFriendBean.newBuilder();
        try {
            int userId = ctx.getUserId();
            Friends friends = mFriends.getFriendById(userId);
            List<Integer> listFriendIds = friends.getListFriendIds();
            for (Integer friendId : listFriendIds) {
                FeatureProtos.FriendInfoBean friendInfoBean = userService.getFriendInfoById(friendId, false);
                listFriendBean.addListFriends(friendInfoBean);
            }

            List<Integer> listfriendFBIds = friends.getListFriendFacebook();
            for (Integer friendId : listfriendFBIds) {
                FeatureProtos.FriendInfoBean friendInfoBean = userService.getFriendInfoById(friendId, true);
                listFriendBean.addListFriends(friendInfoBean);
            }
            listFriendBean.setErrorCode(REC_SUCCESS);
        } catch (Exception e) {
            listFriendBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("GetListFriends", e);
        }
        sendMessage(ctx, listFriendBean.build().toByteString(), LIST_FRIEND_RESPONSE_BEAN);
    }

    public void getListAskFriends(ChannelContextInfo ctx) {
        FeatureProtos.ListFriendBean.Builder listFriendBean = FeatureProtos.ListFriendBean.newBuilder();
        try {
            int userId = ctx.getUserId();
            Friends friends = mFriends.getFriendById(userId);
            List<Integer> listFriendIds = friends.getListFriendRequest();
            for (Integer friendId : listFriendIds) {
                FeatureProtos.FriendInfoBean friendInfoBean = userService.getFriendInfoById(friendId, false);
                listFriendBean.addListFriends(friendInfoBean);
            }

            listFriendBean.setErrorCode(REC_SUCCESS);
        } catch (Exception e) {
            listFriendBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("getListAskFriends", e);
        }
        sendMessage(ctx, listFriendBean.build().toByteString(), LIST_ASK_FRIEND_RESPONSE_BEAN);
    }

    void sendAcceptFriendForOthers(int userId, int friendId) {
        User user = userService.getUser(userId);

        //add friend cho no ne
        user.addFriend(friendId);

        String content = "Chúc mừng bạn và " + user.getNickName() + " đã trở thành bạn của nhau.";
        inboxService.sendMessageFromSystem(friendId, "Kết bạn thành công", content);

        User others = userService.getUser(friendId);
        if (others != null) {
            if (!others.isBot()) {
                FeatureProtos.OtherAcceptFriendBean.Builder otherAddFriend = FeatureProtos.OtherAcceptFriendBean.newBuilder();
                otherAddFriend.setNickName(user.getNickName());
                otherAddFriend.setFriendId(userId);

                XProtos.XMessage.Builder xmessage = XProtos.XMessage.newBuilder();
                xmessage.setCommand(ServerCommandConstant.FRIENDS_COMMAND);
                xmessage.setBeanType(OTHER_ACCEPT_FRIEND_RESPONSE_BEAN);
                xmessage.setData(otherAddFriend.build().toByteString());
                others.getCtx().writeAndFlush(xmessage.build());

                //add friend cho ban no
                others.addFriend(userId);
            }

        }
    }

    void sendAddFriendForOther(int userId, int friendId) {
        User user = userService.getUser(userId);
        User others = userService.getUser(friendId);
        if (others != null) {
            FeatureProtos.OtherRequestAddFriendBean.Builder otherAddFriend = FeatureProtos.OtherRequestAddFriendBean.newBuilder();
            otherAddFriend.setNickName(user.getNickName());
            otherAddFriend.setFriendId(userId);

            XProtos.XMessage.Builder xmessage = XProtos.XMessage.newBuilder();
            xmessage.setCommand(ServerCommandConstant.FRIENDS_COMMAND);
            xmessage.setBeanType(OTHER_ADD_FRIEND_RESPONSE_BEAN);
            xmessage.setData(otherAddFriend.build().toByteString());

            if (!others.isBot()) {
                others.getCtx().writeAndFlush(xmessage.build());
            }
        }
    }

    public void requestAddFriend(ChannelContextInfo ctx, int friendId) {
        FeatureProtos.AddFriendResponseBean.Builder responseBean = FeatureProtos.AddFriendResponseBean.newBuilder();
        try {
            int userId = ctx.getUserId();
            Friends friends = mFriends.getFriendById(userId);
            if (userId == friendId) {
                responseBean.setErrorCode(REC_FRIEND_CANT_ADD_FRIEND_WITH_MINE);
            } else {
                if (!friends.checkIsFriend(friendId)) {
                    User user = userService.getUser(friendId);
                    if (user != null && user.isBot()) {
                        //gửi kết bạn với bot thì gửi cho vui thôi. Chả có ý nghĩa gì cả.
                        responseBean.setErrorCode(REC_SUCCESS);
                        responseBean.setFriendId(friendId);
                    } else {
                        Friends othersFriends = mFriends.getFriendById(friendId);
                        if (othersFriends != null) {
                            if (friends.checkExistInListRequest(userId)){
                                //cả 2 dùng request nhau, cho thành bạn luôn.
                                othersFriends.addFriendsSuccess(userId);
                                friends.addFriendsSuccess(friendId);
                                responseBean.setErrorCode(REC_SUCCESS);

                                friends.saveDB();
                                othersFriends.saveDB();
                                //gửi cho thằng bạn nó biết là mình đã là friend
                                sendAcceptFriendForOthers(userId, friendId);


                            } else {
                                //nếu đã reuqest rồi hoặc đã bị reject thì không được add nữa
                                if (othersFriends.checkExistInReject(userId) || othersFriends.checkExistInListRequest(userId)) {
                                    responseBean.setErrorCode(REC_FRIEND_CANT_ADD_BECAUSE_ALREADY_IN_REQUEST_OR_REJECT);
                                } else {
                                    othersFriends.requestAddFriends(userId);
                                    othersFriends.saveDB();
                                    responseBean.setErrorCode(REC_SUCCESS);
                                    responseBean.setFriendId(friendId);

                                    //báo cho thằng đó biết
                                    sendAddFriendForOther(userId, friendId);
                                }
                            }
                        } else {
                            responseBean.setErrorCode(REC_FRIEND_NOT_EXIST);
                        }
                    }


                } else {
                    responseBean.setErrorCode(REC_FRIEND_CANT_ADD_BECAUSE_ALREADY_FRIEND);
                }
            }

        } catch (Exception e) {
            responseBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("requestAddFriend", e);
        }
        sendMessage(ctx, responseBean.build().toByteString(), ADD_FRIEND_RESPONSE_BEAN);
    }

    public void requestAcceptFriend(ChannelContextInfo ctx, int friendId) {
        FeatureProtos.AcceptFriendResponseBean.Builder responseBean = FeatureProtos.AcceptFriendResponseBean.newBuilder();
        try {
            int userId = ctx.getUserId();
            if (userId == friendId) {
                responseBean.setErrorCode(REC_FRIEND_CANT_ADD_FRIEND_WITH_MINE);
            } else {
                Friends friends = mFriends.getFriendById(userId);
                Friends othersFriend = mFriends.getFriendById(friendId);
                if (othersFriend!= null) {
                    if (friends.checkIsFriend(friendId)) {
                        responseBean.setErrorCode(REC_FRIEND_CANT_ACCEPT_BECAUSE_ALREADY_FRIEND);
                    } else {
                        if (friends.checkExistInListRequest(friendId)) {
                            friends.addFriendsSuccess(friendId);
                            othersFriend.addFriendsSuccess(userId);
                            othersFriend.saveDB();
                            friends.saveDB();

                            responseBean.setErrorCode(REC_SUCCESS);
                            responseBean.setFriendId(friendId);
                            //gửi cho thằng bạn nó biết là mình đã là friend
                            sendAcceptFriendForOthers(userId, friendId);
                        } else {
                            responseBean.setErrorCode(REC_FRIEND_CANT_ACCEPT_BECAUSE_NOT_EXIST_IN_REQUEST);
                        }
                    }
                } else {
                    responseBean.setErrorCode(REC_FRIEND_NOT_EXIST);
                }
            }

        } catch (Exception e) {
            responseBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("requestAcceptFriend", e);
        }
        sendMessage(ctx, responseBean.build().toByteString(), ACCEPT_FRIEND_RESPONSE_BEAN);
    }

    public void requestDeclineFriend(ChannelContextInfo ctx, int friendId) {
        FeatureProtos.DeclineFriendResponseBean.Builder responseBean = FeatureProtos.DeclineFriendResponseBean.newBuilder();
        try {
            int userId = ctx.getUserId();
            if (userId == friendId) {
                responseBean.setErrorCode(REC_FRIEND_CANT_ADD_FRIEND_WITH_MINE);
            } else {
                Friends friends = mFriends.getFriendById(userId);
                Friends othersFriend = mFriends.getFriendById(friendId);
                if (othersFriend!= null) {
                    if (friends.checkIsFriend(friendId)) {
                        responseBean.setErrorCode(REC_FRIEND_CANT_DECLINE_BECAUSE_ALREADY_FRIEND);
                    } else {
                        if (friends.checkExistInListRequest(friendId)) {
                            friends.declineFriend(friendId);
                            friends.saveDB();

                            responseBean.setErrorCode(REC_SUCCESS);
                            responseBean.setFriendId(friendId);
                        } else {
                            responseBean.setErrorCode(REC_FRIEND_CANT_DECLINE_BECAUSE_NOT_EXIST_IN_REQUEST);
                        }
                    }
                } else {
                    responseBean.setErrorCode(REC_FRIEND_NOT_EXIST);
                }
            }

        } catch (Exception e) {
            responseBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("requestDeclineFriend", e);
        }
        sendMessage(ctx, responseBean.build().toByteString(), DECLINE_FRIEND_RESPONSE_BEAN);
    }

    public void requestRemoveFriend(ChannelContextInfo ctx, int friendId) {
        FeatureProtos.DeclineFriendResponseBean.Builder responseBean = FeatureProtos.DeclineFriendResponseBean.newBuilder();
        try {
            int userId = ctx.getUserId();

            Friends friends = mFriends.getFriendById(userId);
            Friends othersFriend = mFriends.getFriendById(friendId);
            if (othersFriend!= null) {
                if (friends.checkIsFriend(friendId)) {
                    responseBean.setErrorCode(REC_SUCCESS);
                    responseBean.setFriendId(friendId);

                    friends.removeFriend(friendId);
                    othersFriend.removeFriend(userId);

                    friends.saveDB();
                    othersFriend.saveDB();

                    //remove friend for user
                    User user = userService.getUser(userId);
                    if (user != null) {
                        user.removeFriend(friendId);
                    }

                    //remove friend for other
                    User other = userService.getUser(friendId);
                    if (other != null) {
                        other.removeFriend(userId);
                    }
                } else {
                    responseBean.setErrorCode(REC_FRIEND_CANT_REMOVE_BECAUSE_YOU_ARE_NOT_FRIEND);
                }
            } else {
                responseBean.setErrorCode(REC_FRIEND_NOT_EXIST);
            }
        } catch (Exception e) {
            responseBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("requestRemoveFriend", e);
        }
        sendMessage(ctx, responseBean.build().toByteString(), REMOVE_FRIEND_RESPONSE_BEAN);
    }

    public void findFriend(ChannelContextInfo ctx, int friendId) {
        FeatureProtos.ListFriendBean.Builder listFriendBean = FeatureProtos.ListFriendBean.newBuilder();
        try {
            FeatureProtos.FriendInfoBean friendInfoBean = userService.getFriendInfoById(friendId, false);
            listFriendBean.setErrorCode(REC_SUCCESS);
            listFriendBean.addListFriends(friendInfoBean);
        } catch (Exception e) {
            listFriendBean.setErrorCode(REC_NOT_DEFINE);
            logger.error("findFriend", e);
        }
        sendMessage(ctx, listFriendBean.build().toByteString(), FIND_FRIEND_RESPONSE_BEAN);
    }

    private void sendMessage(ChannelContextInfo ctx, ByteString data, int beanCode) {
        XProtos.XMessage.Builder xMessage = XProtos.XMessage.newBuilder();
        xMessage.setCommand(ServerCommandConstant.FRIENDS_COMMAND).setBeanType(beanCode)
                .setData(data);
        ctx.writeAndFlush(xMessage.build());
    }
}
