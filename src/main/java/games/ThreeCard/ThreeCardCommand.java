package games.ThreeCard;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import games.APISendMoney;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.MoneyType;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.game.GameErrorCode;
import games.core.user.User;
import games.tx.TXErrorCode;
import message.ThirteenKillerProto;
import message.ThreeCardProto;
import message.XProtos;

import java.io.IOException;
import java.util.*;

public class ThreeCardCommand extends ICommand {
    private static ThreeCardCommand instance;
    private ThreeCardGame threeCardGame;
    private int roomID = TCActionConst.ROOM;

    public ThreeCardCommand(ThreeCardGame tcGame) {
        threeCardGame = tcGame;
    }

    public synchronized static ThreeCardCommand GetInstance() {
        if (instance == null)
            instance = new ThreeCardCommand(ThreeCardGame.GetInstance());


        return instance;
    }


    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException, NoSuchFieldException {
        if (ctx.checkAuth()) {
            int request = message.getBeanType();

            switch (request) {
                case TCActionConst.GAME_INFO:
                    UpdateSceneLobby(ctx);
                    break;

                case TCActionConst.TRY_JOIN_ROOM:
                    HandleTryJoinRoom(ctx, message);
                    break;

                case TCActionConst.JOIN_ROOM:
                    HandleJoinRoom(ctx, message);
                    break;

                case TCActionConst.JOIN_ROOM_RANDOM:
                    HandleJoinRoomRandom(ctx);
                    break;

                case TCActionConst.LEAVE_GAME:
                    HandleLeaveGame(ctx, message);
                    break;

                case TCActionConst.CREATE_ROOM:
                    HandleCreateRoom(ctx, message);
                    break;

                case TCActionConst.COUNT_DOWN:
                    HandleCountDown(ctx, message, TCActionConst.COUNT_DOWN);
                    break;

                case TCActionConst.BET:
                    HandleBet(ctx, message);
                    break;

                case TCActionConst.CANCEL_BET:
                    HandleCancelBet(ctx, message);
                    break;

                case TCActionConst.READY:
                    HandleReStartGame(ctx, message);
                    break;

                case TCActionConst.DEAL_CARDS:
                    HandleDealCards(message);
                    break;

                case TCActionConst.SEND_CARDS_FOR_OTHER_CLIENT:
                    HandleSendCardsForOtherClient(message);
                    break;

                case TCActionConst.TIME_TO_SEE_CARDS:
                    HandleCountDown(ctx, message, TCActionConst.TIME_TO_SEE_CARDS);
                    break;

                case TCActionConst.COMPARE_RESULTS_AND_PAYMENT:
                    HandleCompareResultsAndPayment(message);
                    break;
            }
        }
    }

    private void HandleCompareResultsAndPayment(XProtos.XMessage message) throws IOException
    {
        ThreeCardProto.TCUserBetInfo userBetInfo = ThreeCardProto.TCUserBetInfo.parseFrom(message.getData());

        ThreeCardProto.TCUserInfo userMaster = userBetInfo.getMaster();

        List<ThreeCardProto.TCUserInfo> users = userBetInfo.getUsersList();

        TCRoom room = threeCardGame.getRoomByID(userBetInfo.getRoomId());

        int masterID = userMaster.getUserId();
        TCPlayer master  = room.GetPlayer(masterID);
        master.winMoney = 0;

        for (ThreeCardProto.TCUserInfo user : users)
        {
            int userID = user.getUserId();
            TCPlayer player = room.GetPlayer(userID);

            int scoreCardOfMaster = userMaster.getScoreCard();

            int scorePlayer = user.getScoreCard();

            long betAmount = user.getBetAmount();

            if (scoreCardOfMaster > scorePlayer)
            {
                room.AddMoney(betAmount, master.getUser(), MoneyType.GOLD);
                master.winMoney += betAmount;

                player.winMoney = - betAmount;

            }
            else if (scoreCardOfMaster < scorePlayer)
            {
                room.AddMoney(betAmount * 2, player.getUser(), MoneyType.GOLD);
                player.winMoney = betAmount;

                room.SubMoney(betAmount, master.getUser(), MoneyType.GOLD);
                master.winMoney -= betAmount;
            }

            else
            {
                player.winMoney = 0;
            }
        }

        room.Payment(masterID);
    }

    private void HandleSendCardsForOtherClient(XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThreeCardProto.TCCards data = ThreeCardProto.TCCards.parseFrom(message.getData());

        ThreeCardProto.TCCards.Builder cards = ThreeCardProto.TCCards.newBuilder();
        cards.setUserId(data.getUserId());
        cards.addAllListCard(data.getListCardList());

        TCRoom room = threeCardGame.getRoomByID(data.getRoomId());
        room.SendToTheRest(data.getUserId(), cards.build().toByteString(), TCActionConst.SEND_CARDS_FOR_OTHER_CLIENT);

    }

    private void HandleReStartGame(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        ThreeCardProto.TCRoomInfo roomInfo = ThreeCardProto.TCRoomInfo.parseFrom(message.getData());
        TCRoom room = threeCardGame.getRoomByID(roomInfo.getRoomId());
        TCPlayer player = room.GetPlayer(ctx.getUserId());
        player.isReady = true;
        LinkedHashMap<Integer, TCPlayer> users = room.GetUsersInCurrentRoom();

        for (TCPlayer user : users.values()) {
            if (user.getUser() != null && !user.isReady && !user.isMasterClient)
                return;
        }

        ThreeCardProto.TCRoomInfo.Builder resToClient = ThreeCardProto.TCRoomInfo.newBuilder();
        resToClient.setErrorCode(0);
        room.SendToMasterClient(resToClient.build().toByteString(), TCActionConst.READY);
    }

    private void HandleCancelBet(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        ThreeCardProto.TCUserInfo request = ThreeCardProto.TCUserInfo.parseFrom(message.getData());
        ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();

        TCRoom room = threeCardGame.getRoomByID(request.getRoomID());

        int userId = request.getUserId();
        userInfo.setUserId(userId);

        TCPlayer player = room.GetPlayer(ctx.getUserId());
        long totalBet = player.totalBet;
        long newMoney = ctx.getUser().getMoneyGold() + totalBet;
        userInfo.setMoney(newMoney);

        room.SendToAllInRoom(userInfo.build().toByteString(), TCActionConst.CANCEL_BET);
        room.AddMoney(totalBet, ctx.getUser(), MoneyType.GOLD);

        player.totalBet = 0;
    }

    private void HandleBet(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        ThreeCardProto.TCUserInfo request = ThreeCardProto.TCUserInfo.parseFrom(message.getData());
        ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();

        int userId = request.getUserId();
        userInfo.setUserId(userId);

        long betAmount = request.getBetAmount();
        long newMoney = ctx.getUser().getMoneyGold() - betAmount;
        userInfo.setMoney(newMoney);

        TCRoom room = threeCardGame.getRoomByID(request.getRoomID());
        userInfo.setBetAmount(betAmount);

        room.SendToAllInRoom(userInfo.build().toByteString(), TCActionConst.BET);
        room.SubMoney(betAmount, ctx.getUser(), MoneyType.GOLD);

        TCPlayer player = room.GetPlayer(ctx.getUserId());
        player.totalBet += betAmount;
    }

    private void HandleJoinRoomRandom(ChannelContextInfo ctx)
    {
        long gold = ctx.getUser().getMoneyGold();

        TCRoom room = threeCardGame.GetRoomRandom(gold);

        if (room != null)
        {
            ThreeCardProto.TCRoomInfo.Builder builder = room.parseProtoBuilder();

            room.playersJoinedRoom++;

            TCPlayer player = threeCardGame.GetPlayerLobbỵ̣(ctx.getUserId());
            if (player == null)
                player = threeCardGame.CreatePlayer(ctx.getUser());
            threeCardGame.AddPlayer(player);

            threeCardGame.JoinRoom(player, room);

            builder.setErrorCode(TCActionConst.SUCCESS);

            ByteString roomInfo = builder.build().toByteString();
            threeCardGame.SendMessage(ctx, roomInfo, TCActionConst.TRY_JOIN_ROOM);
        }
        else
        {
            ThirteenKillerProto.TKRoomInfo.Builder builder = ThirteenKillerProto.TKRoomInfo.newBuilder();
            builder.setErrorCode(TCActionConst.NOT_FOUND_ROOM);
            ByteString roomInfo = builder.build().toByteString();
            threeCardGame.SendMessage(ctx, roomInfo, TCActionConst.TRY_JOIN_ROOM);
        }
    }

    private void HandleCountDown(ChannelContextInfo ctx, XProtos.XMessage message, int responseCode) throws InvalidProtocolBufferException
    {
        ThreeCardProto.TCCountdown data = ThreeCardProto.TCCountdown.parseFrom(message.getData());

        TCRoom room = threeCardGame.getRoomByID(data.getRoomId());

        room.isActive = responseCode == TCActionConst.TIME_TO_SEE_CARDS;

        room.isCountdown = true;

        CountDown(ctx.getUserId(), room, responseCode);

        UpdateSceneLobby();
    }

    private void CountDown(int userID, TCRoom room, int responseCode)
    {
        TCPlayer master = room.GetPlayer(userID);
        master.timeToBet  = TCActionConst.TIME_COUNT_DOWN;

        room.timer.cancel();

        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                master.timeToBet -= 1;
                int time = master.timeToBet;
                if (time >= 0)
                {
                    if (responseCode == TCActionConst.COUNT_DOWN)
                        room.isActive = false;

                    else if (responseCode == TCActionConst.TIME_TO_SEE_CARDS)
                        room.isActive = true;

                    SendTimeToClients(room, time, responseCode);
                }
                else room.timer.cancel();

            }
        };

        room.timer = new Timer();

        room.timer.schedule(timerTask, 0, 1000);
    }


    private void HandleDealCards(XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThirteenKillerProto.TKRoomInfo roomInfo = ThirteenKillerProto.TKRoomInfo.parseFrom(message.getData());

        int roomId = roomInfo.getRoomId();

        TCRoom room = threeCardGame.getRoomByID(roomId);

        room.isActive = true;

        room.DealCards();

        UpdateSceneLobby();
    }

    private void SendTimeToClients(TCRoom room, float countdown, int responseCode)
    {
        ThreeCardProto.TCCountdown.Builder countDown = ThreeCardProto.TCCountdown.newBuilder();

        countDown.setTime(countdown);

        room.SendToAllInRoom(countDown.build().toByteString(), responseCode);
    }

    private void HandleCreateRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        ThreeCardProto.TCRoomInfo room = ThreeCardProto.TCRoomInfo.parseFrom(message.getData());
        roomID++;
        long betLevel = room.getBetLevel();
        String tableOwner = room.getTableOwner();
        int maxPlayerOfRoom = room.getMaxPlayersOfRoom();
        int playerJoined = room.getPlayersJoinedRoom();

        threeCardGame.AddRoom(ctx.getUser(), roomID, betLevel, tableOwner, maxPlayerOfRoom, playerJoined);
    }

    private void UpdateSceneLobby(ChannelContextInfo ctx)
    {
        Random rand = new Random();
        int numberRand = rand.nextInt(20);

        TCPlayer player = threeCardGame.GetPlayerLobbỵ̣(ctx.getUserId());
        if (player == null)
        {
            player = threeCardGame.CreatePlayer(ctx.getUser());
            threeCardGame.AddPlayerOnLobby(player);
        }

        int idAvatar = Integer.parseInt(ctx.getUser().getAvatar());
        player.idAvatar = idAvatar;

        ThreeCardProto.TCGameInfo.Builder builder = ThreeCardProto.TCGameInfo.newBuilder();
        threeCardGame.GetRoom().values().forEach(tcRoom -> builder.addRooms(tcRoom.parseProtoBuilder()));

        builder.addAllIDOfDeletedRooms(threeCardGame.idOfDeletedRooms);

        builder.setMoney(ctx.getUser().getMoneyGold());

        builder.setAvatar(player.idAvatar);

        ByteString gameInfo = builder.build().toByteString();
        threeCardGame.SendMessage(ctx, gameInfo, TCActionConst.GAME_INFO);
    }

    private void UpdateSceneLobby()
    {
        ThreeCardProto.TCGameInfo.Builder builder = ThreeCardProto.TCGameInfo.newBuilder();
        threeCardGame.GetRoom().values().forEach(tcRoom -> builder.addRooms(tcRoom.parseProtoBuilder()));

        builder.addAllIDOfDeletedRooms(threeCardGame.idOfDeletedRooms);

        ByteString gameInfo = builder.build().toByteString();
        threeCardGame.SendToAllLobby(gameInfo, TCActionConst.LOBBY_INFO);
    }

    private void HandleTryJoinRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThreeCardProto.TCRoomInfo tcRoomInfo = ThreeCardProto.TCRoomInfo.parseFrom(message.getData());
        TCRoom room;

        if (tcRoomInfo.getRoomId() == 0)// phòng mới tạo
            room = threeCardGame.getRoomByID(roomID);
        else
            room = threeCardGame.getRoomByID(tcRoomInfo.getRoomId());

        ThreeCardProto.TCRoomInfo.Builder builder = room.parseProtoBuilder();

        long gold = ctx.getUser().getMoneyGold();

        // gold của user phải lớn hơn hoặc bằng tiền cược của bàn
        // nếu bé hơn thì báo lỗi k đủ tiền
        if (gold < room.betLevel)
        {
            builder.setErrorCode(TCActionConst.NOT_ENOUGH_GOLD);
            ByteString roomInfo = builder.build().toByteString();
            threeCardGame.SendMessage(ctx, roomInfo, TCActionConst.TRY_JOIN_ROOM);
            return;
        }

        else if (room.isActive)
        {
            builder.setErrorCode(TCActionConst.ROOM_PLAYING);
            ByteString roomInfo = builder.build().toByteString();
            threeCardGame.SendMessage(ctx, roomInfo, TCActionConst.TRY_JOIN_ROOM);
        }

        else if (room.maxPlayer == room.playersJoinedRoom)
        {
            builder.setErrorCode(TCActionConst.FULL_ROOM);
            ByteString roomInfo = builder.build().toByteString();
            threeCardGame.SendMessage(ctx, roomInfo, TCActionConst.TRY_JOIN_ROOM);
        }

        else
        {
            room.playersJoinedRoom++;

            TCPlayer player = threeCardGame.GetPlayerLobbỵ̣(ctx.getUserId());
            if (player == null)
                player = threeCardGame.CreatePlayer(ctx.getUser());

            threeCardGame.AddPlayer(player);
            threeCardGame.JoinRoom(player, room);

            builder.setErrorCode(TCActionConst.SUCCESS);

            ByteString roomInfo = builder.build().toByteString();
            threeCardGame.SendMessage(ctx, roomInfo, TCActionConst.TRY_JOIN_ROOM);
        }


    }

    private void HandleJoinRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThreeCardProto.TCRoomInfo data = ThreeCardProto.TCRoomInfo.parseFrom(message.getData());
        TCRoom room = threeCardGame.getRoomByID(data.getRoomId());
        SendUsersJoinRoomToClient(room, data.getRoomId(), data.getBetLevel(), data.getMaxPlayersOfRoom());
        threeCardGame.RemovePlayerOnLobby(ctx.getUser());
        UpdateSceneLobby();
    }

    private void SendUsersJoinRoomToClient(TCRoom room, int roomId, long betLevel, int maxPlayerInARoom)
    {
        ThreeCardProto.TCRoomInfo.Builder roomInfo = ThreeCardProto.TCRoomInfo.newBuilder();
        roomInfo.setRoomId(roomId);
        roomInfo.setBetLevel(betLevel);
        roomInfo.setIsRoomActive(room.isCountdown);

        LinkedHashMap<Integer, TCPlayer> listUser = room.GetUsersInCurrentRoom();
        TCPlayer[] users = listUser.values().toArray(new TCPlayer[0]);

        boolean hadMasterClient = false;

        for (int i = 0; i < maxPlayerInARoom; i++)
        {
            if (users.length > i && users[i].getUser() != null)
            {
                User user = users[i].getUser();
                long money = user.getMoneyGold();
                String name = user.getUserName();
                int id = users[i].getId();
                int idAvatar = Integer.parseInt(user.getAvatar());

                ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();
                userInfo.setMoney(money);
                userInfo.setNickName(name);
                userInfo.setPosUser(i + 1);
                userInfo.setUserId(id);
                userInfo.setIdAvatar(idAvatar);

                if (!hadMasterClient)
                {
                    if (users[i].isMasterClient) userInfo.setIsMasterClient(true);

                    hadMasterClient = true;
                }

                roomInfo.addUsers(i, userInfo);
            }
            else if (users.length < maxPlayerInARoom)
            {
                ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();
                roomInfo.addUsers(i, userInfo);
            }
        }

        room.SendToAllInRoom(roomInfo.build().toByteString(), TCActionConst.UPDATE_USERS_JOIN_ROOM);
    }

    private void HandleLeaveGame(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException
    {
        APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                        ctx.getUser().getUserName(),
                        Long.toString(ctx.getUser().getMoneyGold()),0,""));
        ThreeCardProto.TCRoomInfo tcRoomInfo = ThreeCardProto.TCRoomInfo.parseFrom(message.getData());
        TCRoom room = threeCardGame.getRoomByID(tcRoomInfo.getRoomId());
        TCPlayer player = room.GetPlayer(ctx.getUserId());

        ThreeCardProto.TCRoomInfo.Builder builder = ThreeCardProto.TCRoomInfo.newBuilder();
        try
        {
            threeCardGame.LeaveRoom(room, player, player.getId());
            threeCardGame.RemovePlayer(player);
            threeCardGame.RemoveRoom(tcRoomInfo.getRoomId());

            room.playersJoinedRoom--;

            // Khi còn 1 user trong room thì gán isCountdown = false
            // Để user có thể set btn Ready dưới cliet
            if (room.playersJoinedRoom == 1)
            {
                room.isCountdown = false;
                room.timer.cancel();
            }

            builder.setErrorCode(0);
        }
        catch (NotFoundPlayerInGame notFoundPlayerInGame)
        {
            builder.setErrorCode(GameErrorCode.PLAYER_NOT_FOUND_IN_GAME);
        }
        catch (Exception e)
        {
            builder.setErrorCode(TXErrorCode.ERROR_NOT_DEFINE);
        }

        ByteString errorCode = builder.build().toByteString();
        threeCardGame.SendMessage(ctx, errorCode, TCActionConst.LEAVE_GAME);

        SendUserLeaveRoomToClient(room, room.maxPlayer);
        UpdateSceneLobby();
    }

    private void SendUserLeaveRoomToClient(TCRoom room, int maxPlayer)
    {
        ThreeCardProto.TCRoomInfo.Builder roomInfo = ThreeCardProto.TCRoomInfo.newBuilder();
        LinkedHashMap<Integer, TCPlayer> listUser = room.GetUsersInCurrentRoom();
        TCPlayer[] users = listUser.values().toArray(new TCPlayer[0]);

        boolean hadMasterClient = false;

        for (int i = 0; i < maxPlayer; i++)
        {
            if (users.length > i && users[i].getUser() != null)
            {
                User user = users[i].getUser();
                long money = user.getMoneyGold();
                String name = user.getUserName();
                int id = users[i].getId();

                ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();
                userInfo.setMoney(money);
                userInfo.setNickName(name);
                userInfo.setPosUser(i + 1);
                userInfo.setUserId(id);

                if (!hadMasterClient)
                {
                    TCPlayer player = room.GetPlayer(users[i].getId());
                    if (player.isMasterClient)
                        userInfo.setIsMasterClient(true);
                    hadMasterClient = true;
                }

                roomInfo.addUsers(i, userInfo);
            }
            else
            {
                ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();
                roomInfo.addUsers(i, userInfo);
            }
        }
        room.SendToAllInRoom(roomInfo.build().toByteString(), TCActionConst.UPDATE_USERS_LEAVE_ROOM);
    }

}
