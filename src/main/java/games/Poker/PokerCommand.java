package games.Poker;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import games.APISendMoney;
import games.Poker.CARDS.CARDNUMBER;
import games.Poker.CARDS.CARDTYPE;
import games.Poker.CARDS.PokerCard;
import games.Poker.ENUM.*;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.game.GameErrorCode;
import games.core.user.User;
import message.FishShootingProtos;
import message.PokerProto;
import message.XProtos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class PokerCommand extends ICommand implements PokerConstanst{
    @Descreption_Anno(Name = "METHOD ACTIVE TAIXIUCOMMAND AT HANDLERCHANNELCONTEXT - PROCESS")
    public void Start() {
        PokerGameInstance.Start();
    }
    @Descreption_Anno(Name = "FIELDS")
    private PokerGame PokerGameInstance;
    private PokerGame getPokerGameInstance() {
        return PokerGameInstance;
    }

    @Descreption_Anno(Name = "SINGLETONE")
    public static PokerCommand instance = null;
    public synchronized static PokerCommand getInstance() {
        if (instance == null) {
            instance = new PokerCommand(PKGame.getInstance());
        }
        return instance;
    }
    @Descreption_Anno(Name = "CONSTRUCTOR")
    PokerCommand (PokerGame pokerGame) {
        this.PokerGameInstance = pokerGame;
    }
    @Descreption_Anno(Name = "MAIN POKER PROCESS")
    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        if (ctx.checkAuth()){
            int beanType = message.getBeanType();
            switch (beanType){
                case PokerConstanst.GAME_INFO:
                    this.HandleRoomGameInfo(ctx, message);
                    break;
                case PokerConstanst.JOIN_LOBBY:
                    this.HandleJoinLobby(ctx,message);
                    break;
                case PokerConstanst.LEAVE_LOBBY:
                    this.HandleLeaveLobby(ctx, message);
                    break;

                case PokerConstanst.JOIN_ROOM:
                    this.HandleJoinRoom(ctx,message);
                    break;
                case PokerConstanst.LEAVE_ROOM:
                    this.HandleLeaveRoom(ctx,message);
                    break;

                case PokerConstanst.UPDATE_MONEY:
                    this.Handle_UpdateMoney(ctx,message);
                    break;
                case PokerConstanst.POKER_FAKEPLAYER:
                    this.Handle_FakePlayerJoin(ctx, message);
                    break;
                case PokerConstanst.POKER_STOPTURN:
                    this.Handle_StopTurn(ctx,message);
                    break;

            }
        }
    }
    private void HandleRoomGameInfo(ChannelContextInfo ctx, XProtos.XMessage message) { // Gửi toàn bộ các rooms hiện có cho client
        ByteString byteString = getPokerGameInstance().parseMessageToByteString(); // gọi getPokerGameInstance lần đầu
        PokerGameInstance.SendMessage(ctx, byteString, PokerConstanst.GAME_INFO);
    }
    public void HandleJoinLobby (ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        PokerGameInstance.AddPlayer(ctx.getUser());
    }
    public void HandleLeaveLobby (ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        PokerGameInstance.removePlayer(ctx.getUser());
        APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                ctx.getUser().getUserName(),
                Long.toString(ctx.getUser().getMoneyGold()),0,""));
    }
    public void HandleJoinRoom (ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        PokerProto.Poker_Request_JoinRoom request = PokerProto.Poker_Request_JoinRoom.parseFrom(message.getData());
        PokerRoom pokerRoom = PokerGameInstance.getRoomById(request.getRoomid());
        // Lấy room ra khỏi list ở PokerRoom dựa vào roomid ng chơi tại PokerRoom (lấy 1 / 10 room)
        if ((new User(ctx.getUserId(), 0).getMoneyGold() < POKER_MIN_JOINNING_MONEY)){
            PokerProto.Poker_Response_RoomInfo.Builder response_roominfo = pokerRoom.parseProtoBuilder();
            response_roominfo.setRoomId(-1); // ko đủ tiền
            PokerGameInstance.SendMessage(ctx, response_roominfo.build().toByteString(), PokerConstanst.JOIN_ROOM);
        }
        else {
            if (pokerRoom.players.size() > 8){
                PokerProto.Poker_Response_RoomInfo.Builder response_roominfo = pokerRoom.parseProtoBuilder();
                response_roominfo.setRoomId(0); // dư ng chơi
                PokerGameInstance.SendMessage(ctx, response_roominfo.build().toByteString(), PokerConstanst.JOIN_ROOM);
            }
            else {
                PokerGameInstance.joinRoom(ctx.getUser(), pokerRoom); // add người chơi vào list player của PokerRoom + set room cho player (toàn bộ người chơi trong 1 room lấy bằng id ở trên)
                if (pokerRoom.players.size() == 1){
                    SetInfoForNewGame_1(pokerRoom,true,false);
                }
                if (pokerRoom.players.size() == 2){
                    if (pokerRoom.Poker_phase == PHASE.WAITING){ // chỉ waiting khi từ 0  và có 1 ng join vào. còn lại giữa các ván chơi sẽ gán PHASE.WAITING_CHANGING_SCENE
                        SetInfoForNewGame_2_FIRST(pokerRoom);
                    }
                }
                if (pokerRoom.players.size() > 2 && pokerRoom.players.size() < 9) {
                    if (pokerRoom.Poker_phase == PHASE.WAITING){
                    }
                    else {
                    }
                }
                PokerGameInstance.SendMessage(ctx, pokerRoom.parseProtoBuilder().build().toByteString(), PokerConstanst.JOIN_ROOM);
//            PokerGameInstance.SendToAll(pokerRoom.parseProtoBuilder().build().toByteString(), PokerConstanst.GAME_INFO_1ROOM);
                // KO BAO GIỜ NÊN DÙNG METHOD NÀY - TÀO LAO - VÌ SEND TOÀN BỘ PLAYER TRONG GAME POKER THÔNG TIN CỦA 1 PHÒNG => CLIENT SẼ PARSE THÔNG TIN CỦA 1 PHÒNG ĐÓ
                pokerRoom.SendToAll_OneRoom(pokerRoom.parseProtoBuilder().build().toByteString(), PokerConstanst.GAME_INFO_1ROOM);
                PokerGameInstance.SendToAll(getPokerGameInstance().parseMessageToByteString(), PokerConstanst.GAME_INFO);
            }
        }
    }
    public void HandleLeaveRoom (ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                ctx.getUser().getUserName(),
                Long.toString(ctx.getUser().getMoneyGold()),0,""));
        PokerProto.Poker_Request_LeaveRoom request = PokerProto.Poker_Request_LeaveRoom.parseFrom(message.getData());
        PokerRoom pokerRoom = PokerGameInstance.getRoomById(request.getRoomid());

        SetInfoforLeaver(pokerRoom, ctx);

        PokerProto.Poker_Request_LeaveRoom.Builder response = PokerProto.Poker_Request_LeaveRoom.newBuilder();
        response.setErrorCode(0);
        try {
            PokerGameInstance.leaveRoom(ctx.getUser());
//            PokerGameInstance.removePlayer(ctx.getUser());
        } catch (NotFoundPlayerInGame e) {
            response.setErrorCode(GameErrorCode.PLAYER_NOT_FOUND_IN_GAME);
        } catch (Exception e) {
            response.setErrorCode(GameErrorCode.ERROR_NOT_DEFINE);
            getLogger().error(String.format("User[%d] leave slot room fail", ctx.getUserId()), e);
        }

        PokerGameInstance.SendMessage(ctx, response.build().toByteString(), PokerConstanst.LEAVE_ROOM); // để chuyển scene
        PokerGameInstance.SendToAll(getPokerGameInstance().parseMessageToByteString(), PokerConstanst.GAME_INFO);
    }
    public void Handle_UpdateMoney (ChannelContextInfo ctx, XProtos.XMessage message){
        PokerProto.Poker_Response_UpdateMoney.Builder builder = PokerProto.Poker_Response_UpdateMoney.newBuilder();
        User user = new User(ctx.getUserId(), 0);
        long newMoney = user.getMoneyGold();
        builder.setMoney(newMoney);
        PokerGameInstance.SendMessage(ctx, builder.build().toByteString(), PokerConstanst.UPDATE_MONEY);
    }

    public void Handle_FakePlayerJoin (ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        PokerProto.Poker_Request_JoinFakePlayer request = PokerProto.Poker_Request_JoinFakePlayer.parseFrom(message.getData());
        PokerRoom pokerRoom = PokerGameInstance.getRoomById(request.getRoomID());
        if (pokerRoom.players.size() > 8){
            PokerProto.Poker_Response_RoomInfo.Builder response_roominfo = pokerRoom.parseProtoBuilder();
            response_roominfo.setRoomId(0); // trả về 0 khi phòng full ng
            PokerGameInstance.SendMessage(ctx, response_roominfo.build().toByteString(), PokerConstanst.JOIN_ROOM);
        }
        else {
//            CreateFakeUser(request.getId(), request.getUsername(), pokerRoom);

            // PokerGameInstance.SendMessage(ctx, getPokerGameInstance().parseMessageToByteString(), PokerConstanst.GAME_INFO); // gửi cho ng chơi (khi test)
            PokerGameInstance.SendMessage(ctx, pokerRoom.parseProtoBuilder().build().toByteString(), PokerConstanst.GAME_INFO_1ROOM); // gửi cho ng chơi (khi test)
            PokerGameInstance.SendToAll(pokerRoom.parseProtoBuilder().build().toByteString(), PokerConstanst.GAME_INFO_1ROOM); // trong man hinh create room player chua duoc add vao
        }
//        ByteString byteString = getPokerGameInstance().parseMessageToByteString();
//        PokerGameInstance.SendMessage(ctx, byteString, PokerConstanst.GAME_INFO); // Gửi mỗi khi có ng join room
    }
    public void Handle_StopTurn (ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        PokerProto.Poker_TurnOver request = PokerProto.Poker_TurnOver.parseFrom(message.getData());
        PokerRoom pokerRoom = PokerGameInstance.getRoomById(request.getRoomid());
        if (pokerRoom.Poker_phase == PHASE.WAITING){ // 0

        }
        if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){ // 1 - ko có PLAYEROPTION.CHECK + PLAYEROPTION.BET
            if (pokerRoom.Poker_JoinedGameID.size() == 2){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD_2(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE){
                    RAISE_2(pokerRoom, ctx, request);
                }
            }
            else if (pokerRoom.Poker_JoinedGameID.size() > 2 && pokerRoom.Poker_JoinedGameID.size() < 10){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE){
                    RAISE(pokerRoom, ctx, request);
                }
            }
        }
        else if (pokerRoom.Poker_phase == PHASE.FLOP){ // 2
            if (pokerRoom.Poker_JoinedGameID.size() == 2){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD_2(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CHECK){
                    CHECK_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.CALL) {
                    CALL_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.BET){
                    BET_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE) {
                    RAISE_2(pokerRoom, ctx, request);
                }
            }
            else if (pokerRoom.Poker_JoinedGameID.size() > 2 && pokerRoom.Poker_JoinedGameID.size() < 10){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CHECK){
                    CHECK(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.BET){
                    BET(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE){
                    RAISE(pokerRoom, ctx, request);
                }
            }
        }
        else if (pokerRoom.Poker_phase == PHASE.TURN){ // 3
            if (pokerRoom.Poker_JoinedGameID.size() == 2){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD_2(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CHECK){
                    CHECK_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.BET){
                    BET_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE) {
                    RAISE_2(pokerRoom, ctx, request);
                }
            }
            else if (pokerRoom.Poker_JoinedGameID.size() > 2 && pokerRoom.Poker_JoinedGameID.size() < 10){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CHECK){
                    CHECK(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.BET){
                    BET(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE){
                    RAISE(pokerRoom, ctx, request);
                }
            }
        }
        else if (pokerRoom.Poker_phase == PHASE.RIVER){ // 4
            if (pokerRoom.Poker_JoinedGameID.size() == 2){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD_2(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CHECK){
                    CHECK_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.BET){
                    BET_2(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE) {
                    RAISE_2(pokerRoom, ctx, request);
                }
            }
            else if (pokerRoom.Poker_JoinedGameID.size() > 2 && pokerRoom.Poker_JoinedGameID.size() < 10){
                if (request.getAction() == PLAYEROPTION.FOLD){
                    FOLD(pokerRoom, ctx);
                }
                if (request.getAction() == PLAYEROPTION.CHECK){
                    CHECK(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.CALL){
                    CALL(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.BET){
                    BET(pokerRoom, ctx, request);
                }
                if (request.getAction() == PLAYEROPTION.RAISE){
                    RAISE(pokerRoom, ctx, request);
                }
            }
        }
        else if (pokerRoom.Poker_phase == PHASE.SHOWDOWN){

        }
    }
    @Descreption_Anno(Name = "STUFF METHODS")
//    public void CreateFakeUser (int id, String name, PokerRoom pokerRoom) throws IOException {
//        User fakeplayer = User.createFakeUser(0);
//        fakeplayer.setId(id);
//        fakeplayer.setUserName(name);
//        Money money = new Money(CreateRandomNumber_Money(), MoneyType.GOLD);
//        MoneyService.getInstance().addMoney(money, fakeplayer, true);
//        PokerGameInstance.AddPlayer(fakeplayer);
//        PokerGameInstance.joinRoom(fakeplayer, pokerRoom);
//    }
    public long CreateRandomNumber_Money () {
        Random rand = new Random();
        long rand_Long = rand.nextInt(10000) + 10000;
        return rand_Long;
    }
    public ArrayList<Integer> CreateAvailableAction (boolean FOLD, boolean CHECK, boolean BET, boolean CALL, boolean RAISE, boolean ALL_IN) {
        // Fold - check/call - bet/raise
        // check = call(0$)
        ArrayList<Integer> listAvailableAction = new ArrayList<>();
        if (FOLD){
            listAvailableAction.add(PLAYEROPTION.FOLD);
        }
        if (CHECK){
            listAvailableAction.add(PLAYEROPTION.CHECK);
        }
        if (BET){
            listAvailableAction.add(PLAYEROPTION.BET);
        }
        if (CALL){
            listAvailableAction.add(PLAYEROPTION.CALL);
        }
        if (RAISE){
            listAvailableAction.add(PLAYEROPTION.RAISE);
        }
        if (ALL_IN){
            listAvailableAction.add(PLAYEROPTION.ALL_IN);
        }
        return listAvailableAction;
    }
    public void SetPlayerTo_FOLD_STATE(PokerRoom pokerRoom, int indexPlayerInJoinedGameId) {

    }
    public void SetInfoForNewGame_2_FIRST(PokerRoom pokerRoom){ // ---------2---------- //
        pokerRoom.Poker_JoinedGameID = new ArrayList(pokerRoom.players.keySet()); // set ng tham gia = id của 2 player trong room hiện tại //
        pokerRoom.Poker_phase = PHASE.PRE_FLOP;//
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER; // 10000 tương đương 10s ở local //
        // set dealer dựa vào số ng chơi + set pokerRoom.Poker_IdOfNextDealer
        // set betamount + trừ tiền người chơi
        // set turn + Poker_PlayerAvailableAction + lastAction
        for (int i = 0; i < 2; i++){
            if (i == 0){
                pokerRoom.Poker_DealerTurn.add(DEALERTURN.DEALER_SMALLBLIND);
                pokerRoom.Poker_Turn.add(true);
                pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(true,false,false,true,true,false)));
                pokerRoom.Poker_LastAction.add(PLAYEROPTION.NOTHING);
                pokerRoom.Poker_BetAmount.add(POKER_SMALL_BLIND); // 500L
                pokerRoom.Poker_BetAmountTotal.add(POKER_SMALL_BLIND);
                PokerGameInstance.SubMoney(POKER_SMALL_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(i), 0),MoneyType.GOLD);
            }
            else {
                pokerRoom.Poker_DealerTurn.add(DEALERTURN.BIGBLIND);
                pokerRoom.Poker_Turn.add(false);
                pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(false,false,false,false,false,false)));
                pokerRoom.Poker_LastAction.add(PLAYEROPTION.BET);
                pokerRoom.Poker_BetAmount.add(POKER_BIG_BLIND); // 1000L
                pokerRoom.Poker_BetAmountTotal.add(POKER_BIG_BLIND);
                PokerGameInstance.SubMoney(POKER_BIG_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(i), 0),MoneyType.GOLD);
            }
        }
        // set ng kế thừa dealer
        pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(1);
        // set newminbet
        pokerRoom.Poker_NewMinBet = POKER_BIG_BLIND;
        // giữ nguyên pokerroom.Poker_AllCard52
        // giữ nguyên pokerroom.Poker_CommunityCards
        // set CardHolder
        for (int j = 0; j < 2; j++){ // 2 ng / room
            ArrayList<PokerCard> poker_cardholder_1player = new ArrayList<>();
            for (int i = 0; i < 2; i++){ // 2 lá bài / 1 ng
                // tạo random 1 - số bài hiện tại trong deck
                Random rand = new Random();
                int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                pokerRoom.Poker_AllCard52.remove(rand_index);

                poker_cardholder_1player.add(pokerCard);
            }
            pokerRoom.Poker_playerCardHolder.add(poker_cardholder_1player);
        }
        pokerRoom.Poker_CardStrenght.clear();
    } // --------------- 1

    public void SetInfoForNewGame_1_9_SECOND(PokerRoom pokerRoom) { // ---------2---------- //
        pokerRoom.Poker_JoinedGameID = new ArrayList(pokerRoom.players.keySet());
        if (pokerRoom.Poker_JoinedGameID.size() == 0 || pokerRoom.Poker_JoinedGameID.size() == 1){ // set lại thành phase.waiting
            SetInfoForNewGame_1(pokerRoom,true, false);
        }
        else if (pokerRoom.Poker_JoinedGameID.size() > 1 && pokerRoom.Poker_JoinedGameID.size() < 10){
            SetInfoForNewGame_2_9(pokerRoom);
        }

    } // ---------------- 2
    public void SetInfoForNewGame_1(PokerRoom pokerRoom, boolean oneplayer, boolean waitnewgame_changing_games) { // ---------1---------- //
        if (waitnewgame_changing_games){
            pokerRoom.Poker_CountDown = POKER_COUNTDOWN_GAMEWAITING;
            pokerRoom.Poker_phase = PHASE.WAITING_CHANGING_SCENE;
            // pokerRoom.Poker_IdOfNextDealer 0 đổi
        }
        else {
            pokerRoom.Poker_phase = PHASE.WAITING;
        }
        if (oneplayer){
            pokerRoom.Poker_CountDown = 0; // chỉ hiện bản waiting ng chơi khác
//            pokerRoom.Poker_IdOfNextDealer = 0; // ván đầu set giá trị 0
        }
        pokerRoom.Poker_NewMinBet = 0;
        pokerRoom.Poker_LastAction.clear();
        pokerRoom.Poker_Pot = 0;
        pokerRoom.Poker_JoinedGameID.clear();
        pokerRoom.Poker_Turn.clear();
        pokerRoom.Poker_DealerTurn.clear();
        pokerRoom.Poker_BetAmount.clear();
        pokerRoom.Poker_BetAmountTotal.clear();
        pokerRoom.Poker_playerCardHolder.clear();
        pokerRoom.Poker_PlayerAvailableAction.clear();
        pokerRoom.CreateDeck52Cards(); // pokerRoom.Poker_AllCard52 => 52 cards
        pokerRoom.Poker_CommunityCards.clear();
        pokerRoom.Poker_CardStrenght.clear();
        pokerRoom.Poker_JoinedGameID_FOLDER.clear();
        pokerRoom.Poker_JoinedGameID_OUTOFMONEY.clear();

        ArrayList<Integer> EstimatedPlayerID = new ArrayList(pokerRoom.players.keySet());
        for (int i = 0; i < EstimatedPlayerID.size(); i++){
            User user = new User(EstimatedPlayerID.get(i), 0);
            Long money = user.getMoneyGold();
            if (money < POKER_MIN_JOINNING_MONEY){
                PokerGameInstance.leaveRoom(user);
                PokerGameInstance.SendToAll(getPokerGameInstance().parseMessageToByteString(), PokerConstanst.GAME_INFO);
            }
        }
    } // ----------------2.1
    public void SetInfoForNewGame_2_9(PokerRoom pokerRoom){
        if (pokerRoom.Poker_JoinedGameID.size() == 2){
            pokerRoom.Poker_phase = PHASE.PRE_FLOP;//
            pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER; // 10000 tương đương 10s ở local //

            // Index của ng kế thừa dealer
            int IndexOfNextDealer = pokerRoom.Poker_JoinedGameID.indexOf(pokerRoom.Poker_IdOfNextDealer);
            // set dealer dựa vào số ng chơi + set pokerRoom.Poker_IdOfNextDealer
            // set betamount + trừ tiền người chơi
            // set turn + Poker_PlayerAvailableAction + lastAction
            for (int i = 0; i < 2; i++){
                if (i == IndexOfNextDealer){
                    pokerRoom.Poker_DealerTurn.add(DEALERTURN.DEALER_SMALLBLIND);
                    pokerRoom.Poker_Turn.add(true);
                    pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(true,false,false,true,true,false)));
                    pokerRoom.Poker_LastAction.add(PLAYEROPTION.NOTHING);
                    pokerRoom.Poker_BetAmount.add(POKER_SMALL_BLIND); // 500L
                    pokerRoom.Poker_BetAmountTotal.add(POKER_SMALL_BLIND); // 500L
                    PokerGameInstance.SubMoney(POKER_SMALL_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(i), 0),MoneyType.GOLD);
                }
                else {
                    pokerRoom.Poker_DealerTurn.add(DEALERTURN.BIGBLIND);
                    pokerRoom.Poker_Turn.add(false);
                    pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(false,false,false,false,false,false)));
                    pokerRoom.Poker_LastAction.add(PLAYEROPTION.BET);
                    pokerRoom.Poker_BetAmount.add(POKER_BIG_BLIND); // 1000L
                    pokerRoom.Poker_BetAmountTotal.add(POKER_BIG_BLIND); // 1000L
                    PokerGameInstance.SubMoney(POKER_BIG_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(i), 0),MoneyType.GOLD);
                }
            }
            // set ng kế thừa dealer
            for (int i = 0; i < 2; i++){
                if (i != IndexOfNextDealer){
                    pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(i);
                }
            }
            // set newminbet
            pokerRoom.Poker_NewMinBet = POKER_BIG_BLIND;
            // giữ nguyên pokerroom.Poker_AllCard52
            // giữ nguyên pokerroom.Poker_CommunityCards
            // set CardHolder
            for (int j = 0; j < 2; j++){ // 2 ng / room
                ArrayList<PokerCard> poker_cardholder_1player = new ArrayList<>();
                for (int i = 0; i < 2; i++){ // 2 lá bài / 1 ng
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);

                    poker_cardholder_1player.add(pokerCard);
                }
                pokerRoom.Poker_playerCardHolder.add(poker_cardholder_1player);
            }
            pokerRoom.Poker_CardStrenght.clear();
        }
        else if (pokerRoom.Poker_JoinedGameID.size() > 2 && pokerRoom.Poker_JoinedGameID.size() < 9){
            pokerRoom.Poker_phase = PHASE.PRE_FLOP;
            pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
            // set MinBetValue
            pokerRoom.Poker_NewMinBet = POKER_BIG_BLIND;
            int indexOf_Last_Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.indexOf(pokerRoom.Poker_IdOfNextDealer);
            // set dealer dựa vào số ng chơi + set pokerRoom.Poker_IdOfNextDealer
            // set betamount + trừ tiền người chơi
            // set turn + Poker_PlayerAvailableAction + lastAction
            if (indexOf_Last_Poker_IdOfNextDealer == (pokerRoom.Poker_JoinedGameID.size() - 1)){
                for (int j = 0; j < pokerRoom.Poker_JoinedGameID.size(); j++) {
                    if (j == 0){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.SMALLBLIND);
                        pokerRoom.Poker_BetAmount.add(POKER_SMALL_BLIND);
                        pokerRoom.Poker_BetAmountTotal.add(POKER_SMALL_BLIND);
                        PokerGameInstance.SubMoney(POKER_SMALL_BLIND, new User(pokerRoom.Poker_JoinedGameID.get(j), 0),MoneyType.GOLD);
                    }
                    else if (j == 1){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.BIGBLIND);
                        pokerRoom.Poker_BetAmount.add(POKER_BIG_BLIND);
                        pokerRoom.Poker_BetAmountTotal.add(POKER_BIG_BLIND);
                        PokerGameInstance.SubMoney(POKER_BIG_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(j), 0),MoneyType.GOLD);
                    }
                    else if (j == indexOf_Last_Poker_IdOfNextDealer){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.DEALER);
                        pokerRoom.Poker_BetAmount.add(0L);
                        pokerRoom.Poker_BetAmountTotal.add(0L);
                    }
                    else {
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.NOTHING);
                        pokerRoom.Poker_BetAmount.add(0L);
                        pokerRoom.Poker_BetAmountTotal.add(0L);
                    }
                    pokerRoom.Poker_Turn.add(false);
                    pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(false,false,false,false,false,false)));
                    pokerRoom.Poker_LastAction.add(PLAYEROPTION.NOTHING);
                }
                pokerRoom.Poker_Turn.set(2,true);
                pokerRoom.Poker_PlayerAvailableAction.set(2,(CreateAvailableAction(true,false,false,true,true,false)));
                pokerRoom.Poker_LastAction.set(1,PLAYEROPTION.BET);
            }
            else if (indexOf_Last_Poker_IdOfNextDealer == (pokerRoom.Poker_JoinedGameID.size() - 2)){
                for (int k = 0; k < pokerRoom.Poker_JoinedGameID.size(); k++){
                    if (k == 0){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.BIGBLIND);
                        pokerRoom.Poker_BetAmount.add(POKER_BIG_BLIND);
                        pokerRoom.Poker_BetAmountTotal.add(POKER_BIG_BLIND);
                        PokerGameInstance.SubMoney(POKER_BIG_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(k), 0),MoneyType.GOLD);
                    }
                    else if (k == indexOf_Last_Poker_IdOfNextDealer){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.DEALER);
                        pokerRoom.Poker_BetAmount.add(0L);
                        pokerRoom.Poker_BetAmountTotal.add(0L);
                    }
                    else if (k == indexOf_Last_Poker_IdOfNextDealer + 1){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.SMALLBLIND);
                        pokerRoom.Poker_BetAmount.add(POKER_SMALL_BLIND);
                        pokerRoom.Poker_BetAmountTotal.add(POKER_SMALL_BLIND);
                        PokerGameInstance.SubMoney(POKER_SMALL_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(k), 0),MoneyType.GOLD);
                    }
                    else {
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.NOTHING);
                        pokerRoom.Poker_BetAmount.add(0L);
                        pokerRoom.Poker_BetAmountTotal.add(0L);
                    }
                    pokerRoom.Poker_Turn.add(false);
                    pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(false,false,false,false,false,false)));
                    pokerRoom.Poker_LastAction.add(PLAYEROPTION.NOTHING);
                }
                pokerRoom.Poker_Turn.set(1,true);
                pokerRoom.Poker_PlayerAvailableAction.set(1,(CreateAvailableAction(true,false,false,true,true,false)));
                pokerRoom.Poker_LastAction.set(0,PLAYEROPTION.BET);
            }
            else {
                for (int l = 0; l < pokerRoom.Poker_JoinedGameID.size(); l++){
                    if (l == indexOf_Last_Poker_IdOfNextDealer){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.DEALER);
                        pokerRoom.Poker_BetAmount.add(0L);
                        pokerRoom.Poker_BetAmountTotal.add(0L);
                    }
                    else if (l == indexOf_Last_Poker_IdOfNextDealer + 1){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.SMALLBLIND);
                        pokerRoom.Poker_BetAmount.add(POKER_SMALL_BLIND);
                        pokerRoom.Poker_BetAmountTotal.add(POKER_SMALL_BLIND);
                        PokerGameInstance.SubMoney(POKER_SMALL_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(l), 0),MoneyType.GOLD);
                    }
                    else if (l == indexOf_Last_Poker_IdOfNextDealer + 2){
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.BIGBLIND);
                        pokerRoom.Poker_BetAmount.add(POKER_BIG_BLIND);
                        pokerRoom.Poker_BetAmountTotal.add(POKER_BIG_BLIND);
                        PokerGameInstance.SubMoney(POKER_BIG_BLIND,new User(pokerRoom.Poker_JoinedGameID.get(l), 0),MoneyType.GOLD);
                    }
                    else {
                        pokerRoom.Poker_DealerTurn.add(DEALERTURN.NOTHING);
                        pokerRoom.Poker_BetAmount.add(0L);
                        pokerRoom.Poker_BetAmountTotal.add(0L);
                    }
                    pokerRoom.Poker_Turn.add(false);
                    pokerRoom.Poker_PlayerAvailableAction.add((CreateAvailableAction(false,false,false,false,false,false)));
                    pokerRoom.Poker_LastAction.add(PLAYEROPTION.NOTHING);
                }
                pokerRoom.Poker_LastAction.set(indexOf_Last_Poker_IdOfNextDealer + 2,PLAYEROPTION.BET);
                if ((indexOf_Last_Poker_IdOfNextDealer + 3) == pokerRoom.Poker_JoinedGameID.size()){
                    pokerRoom.Poker_Turn.set(0,true);
                    pokerRoom.Poker_PlayerAvailableAction.set(0,(CreateAvailableAction(true,false,false,true,true,false)));
                }
                else {
                    pokerRoom.Poker_Turn.set(indexOf_Last_Poker_IdOfNextDealer + 3,true);
                    pokerRoom.Poker_PlayerAvailableAction.set(indexOf_Last_Poker_IdOfNextDealer + 3,CreateAvailableAction(true,false,false,true,true,false));
                }
            }
            pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.SMALLBLIND));
            // set CardHolder
            for (int j = 0; j < pokerRoom.Poker_JoinedGameID.size(); j++){ // 2 ng / room
                ArrayList<PokerCard> poker_cardholder_1player = new ArrayList<>();
                for (int i = 0; i < 2; i++){ // 2 lá bài / 1 ng
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);

                    poker_cardholder_1player.add(pokerCard);
                }
                pokerRoom.Poker_playerCardHolder.add(poker_cardholder_1player);
            }
        }
    } // -------------- 2.2

    public void SetInfoforLeaver(PokerRoom pokerRoom, ChannelContextInfo ctx) throws IOException {
        // phải xác định xem người leave có trong game hay ko - nếu ko thì ko cần làm gì
        if (pokerRoom.Poker_JoinedGameID.contains(ctx.getUserId())){
            if (pokerRoom.Poker_JoinedGameID.size() == 2){
                int indexPlayerInJoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId());
                if (ctx.getUserId() == pokerRoom.Poker_IdOfNextDealer){
                    if (indexPlayerInJoinedGameId == 0){
                        pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(1);
                    }
                    else {
                        pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(0);
                    }
                }
                FOLD_2(pokerRoom, ctx);
//            SetInfoForNewGame_1(pokerRoom, true, false);
            }
            if (pokerRoom.Poker_JoinedGameID.size() > 2 && pokerRoom.Poker_JoinedGameID.size() < 10){
                if (pokerRoom.Poker_JoinedGameID.contains(ctx.getUserId())){

                    int indexPlayerInJoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId());
                    // Poker_Phase giữ nguyên
                    // Poker_MinBet có thể không đổi
                    // chuyển idNextDealerTurn
                    if (ctx.getUserId() == pokerRoom.Poker_IdOfNextDealer){
                        if (indexPlayerInJoinedGameId == (pokerRoom.Poker_JoinedGameID.size() - 1)){
                            pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(0);
                        }
                        else {
                            pokerRoom.Poker_IdOfNextDealer = pokerRoom.Poker_JoinedGameID.get(indexPlayerInJoinedGameId + 1);
                        }
                    }
                    // remove khỏi turn - có thể để đầu để fold trước
                    if (!pokerRoom.Poker_Turn.get(indexPlayerInJoinedGameId)){ // ko đang trong lượt
                        pokerRoom.Poker_Turn.remove(indexPlayerInJoinedGameId);
                    }
                    else { // đang đến lượt đi - FOLD 1 lần - nếu chuyển phase và lại đến lượt thì FOLD lần nữa
                        FOLD(pokerRoom, ctx);
                        if (pokerRoom.Poker_Turn.get(indexPlayerInJoinedGameId)){
                            FOLD(pokerRoom, ctx);
                            pokerRoom.Poker_Turn.remove(indexPlayerInJoinedGameId);
                        }
                        pokerRoom.Poker_Turn.remove(indexPlayerInJoinedGameId);
                    }
                    // remove khỏi availableAction
                    pokerRoom.Poker_PlayerAvailableAction.remove(indexPlayerInJoinedGameId);
                    // remove khỏi JoinedGameID
                    pokerRoom.Poker_JoinedGameID.remove(indexPlayerInJoinedGameId);
                    // remove khỏi JoinedGameID_FOLDER
                    if (pokerRoom.Poker_JoinedGameID_FOLDER.contains(ctx.getUserId())){
                        pokerRoom.Poker_JoinedGameID_FOLDER.remove(pokerRoom.Poker_JoinedGameID_FOLDER.indexOf(ctx.getUserId()));
                    }
                    // remove khỏi dealerturn
                    switch (pokerRoom.Poker_DealerTurn.get(indexPlayerInJoinedGameId)){
                        case DEALERTURN.SMALLBLIND: // set lại vị trí smallblind vì set turn đầu phase dựa vào smallblind
                            if (indexPlayerInJoinedGameId == (pokerRoom.Poker_DealerTurn.size() - 1)){
                                pokerRoom.Poker_DealerTurn.set(0, DEALERTURN.SMALLBLIND);
                            }
                            else {
                                pokerRoom.Poker_DealerTurn.set(indexPlayerInJoinedGameId + 1, DEALERTURN.SMALLBLIND);
                            }
                            pokerRoom.Poker_DealerTurn.remove(indexPlayerInJoinedGameId);
                            break;
                        default:
                            pokerRoom.Poker_DealerTurn.remove(indexPlayerInJoinedGameId);
                            break;
                    }
                    // remove khỏi lastAction
                    pokerRoom.Poker_LastAction.remove(indexPlayerInJoinedGameId);
                    // + thêm Pot
                    pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId);
                    // remove BetAmount
                    pokerRoom.Poker_BetAmount.remove(indexPlayerInJoinedGameId);
                    // remove CardHolder
                    pokerRoom.Poker_playerCardHolder.remove(indexPlayerInJoinedGameId);
                }
                ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
                pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM);
            }
        }

    }
    @Descreption_Anno(Name = "ACTION COMMAND - LEVEL I")
    public void FOLD_2(PokerRoom pokerRoom, ChannelContextInfo ctx) throws IOException {
        int indexPlayerInJoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId());
        // set lại Pot theo betamount phase đầu tiên - trước khi loại bỏ người chơi
        for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
            pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(i);
        }
        // set countdown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_GAMEWAITING;
        // add vào list FOLDER
        pokerRoom.Poker_JoinedGameID_FOLDER.add(ctx.getUserId());
        // cộng tiền cho ng chơi - trừ tiền lúc đặt cượt rồi
        int indexOfWinner = 0;
        for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
            if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // loại bỏ player FOLD ra
                indexOfWinner = i;
            }
        }
        int IdOfWinner = pokerRoom.Poker_JoinedGameID.get(indexOfWinner);
        PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(IdOfWinner, 0), MoneyType.GOLD);
        SetInfoForNewGame_1(pokerRoom,false,true);
        pokerRoom.SendToAll_OneRoom(pokerRoom.parseProtoBuilder().build().toByteString(), PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room khi chờ tạo ván mới

        // Preflop Phase sau POKER_COUNTDOWN_GAMEWAITING
        new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        SetInfoForNewGame_1(pokerRoom,true,false);
                        SetInfoForNewGame_1_9_SECOND(pokerRoom);
                        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                    }
                },
                POKER_COUNTDOWN_GAMEWAITING
        );
    }
    public void CALL_2 (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request) throws IOException {
        // giữ nguyên
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        int indexOfPlayer = pokerRoom.Poker_JoinedGameID.indexOf(request.getPlayerid()); // index của player trong joined gameid
        // trừ tiền
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexOfPlayer);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set LastAction = call
        pokerRoom.Poker_LastAction.set(indexOfPlayer, PLAYEROPTION.CALL);
        // check xem có nên chuyển phase - Poker_phase - Poker_CommunityCards - Poker_turn - Poker_AvailableAction
        boolean CheckToNextPhase_1 = true; // mặc định là luôn chuyển phase - 50% - $ bằng nhau
        for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
            if (pokerRoom.Poker_BetAmount.get(i) - pokerRoom.Poker_NewMinBet != 0){
                if (!pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                    CheckToNextPhase_1 = false; // nếu có sự chênh lệch là tiếp tục ko chuyển phase
                    break;
                }
            }
        }
        boolean CheckToNextPhase_2 = true; // mặc định là luôn chuyển phase - 50% - all player tới lượt
        CheckToNextPhase_2 = !pokerRoom.Poker_LastAction.contains(PLAYEROPTION.NOTHING);
        if (CheckToNextPhase_1 && CheckToNextPhase_2){ // chuyển phase
            // set phase
            switch (pokerRoom.Poker_phase){
                case PHASE.PRE_FLOP:
                    pokerRoom.Poker_phase = PHASE.FLOP;
                    break;
                case PHASE.FLOP:
                    pokerRoom.Poker_phase = PHASE.TURN;
                    break;
                case PHASE.TURN:
                    pokerRoom.Poker_phase = PHASE.RIVER;
                    break;
                case PHASE.RIVER:
                    pokerRoom.Poker_phase = PHASE.SHOWDOWN;
                    break;
            }
            // set pot theo tổng số dư Bet Amount - sau đó mới clear betamount
            for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
                pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(i);
            }
            // clear betamount
            for (int m = 0; m < 2; m++){
                pokerRoom.Poker_BetAmount.set(m, 0L);
            }
            // reset NewMinBet
            pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount); // 0
            // reset LastAction cho phase mới
            for (int l = 0; l < 2; l++){
                if (!pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(l))){
                    pokerRoom.Poker_LastAction.set(l, PLAYEROPTION.NOTHING);
                }
            }
            // set community cards
            int NumberOfCommunityCardAdded = 0;
            switch (pokerRoom.Poker_phase){
                case PHASE.FLOP:
                    NumberOfCommunityCardAdded = 3;
                    break;
                case PHASE.TURN:
                case PHASE.RIVER:
                    NumberOfCommunityCardAdded = 1;
                    break;
                default:
                    break;
            }
            if (pokerRoom.Poker_phase == PHASE.FLOP || pokerRoom.Poker_phase == PHASE.TURN || pokerRoom.Poker_phase == PHASE.RIVER){
                for (int j = 0; j < NumberOfCommunityCardAdded; j++){ // 3 lá bài community đầu / room
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);
                    pokerRoom.Poker_CommunityCards.add(pokerCard);
                }
            }
            // CHECK KẾT THÚC GAME LUÔN KHI CÓ 1 NG OUT OF MONEY HOẶC ĐẾN PHASE CUỐI
            if (pokerRoom.Poker_phase == PHASE.SHOWDOWN || (pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size()) == 1){
                if ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size()) == 1){ // nếu chưa đến SHOWDOWN => add đủ community
                    int NeededCommunityCards = POKER_MAX_COMMUNITY_CARDS - pokerRoom.Poker_CommunityCards.size(); // số community cards cần thêm
                    for (int j = 0; j < NeededCommunityCards; j++){
                        // tạo random 1 - số bài hiện tại trong deck
                        Random rand = new Random();
                        int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                        // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                        PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                        pokerRoom.Poker_AllCard52.remove(rand_index);
                        pokerRoom.Poker_CommunityCards.add(pokerCard);
                    }
                }
                // set lại CountDown
                pokerRoom.Poker_CountDown = POKER_COUNTDOWN_SHOWDOWN;
                // set Turn - phase cuối ko có lượt ai cả
                for (int k = 0; k < 2; k++){
                    pokerRoom.Poker_Turn.set(k, false);
                }
                // set Poker_AvailableAction - lên phase cuối: ko có available Action
                for (int k = 0; k < 2; k++){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
                // Check độ mạnh của bài các ng chơi còn lại - check hết ko trừ FOLDER
                pokerRoom.Poker_CardStrenght = CHECK_PLAYER_CARDSTRENGHT(pokerRoom.Poker_playerCardHolder, pokerRoom.Poker_CommunityCards);
//                // trường hợp bài 2 ng mạnh bằng nhau
//                if (pokerRoom.Poker_CardStrenght.get(0) == pokerRoom.Poker_CardStrenght.get(1)){
//                    for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){ // trả lại toàn bộ tiền đã cược cho 2 ng chơi
//                        PokerGameInstance.AddMoney(pokerRoom.Poker_BetAmountTotal.get(i), new User(pokerRoom.Poker_JoinedGameID.get(i)),MoneyType.GOLD);
//                    }
//                }
//                else {
                int indexOfWinner = pokerRoom.Poker_CardStrenght.indexOf(Collections.max(pokerRoom.Poker_CardStrenght));
                int idOfWinner = pokerRoom.Poker_JoinedGameID.get(indexOfWinner);
                if (pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(idOfWinner)){ // trường hợp player nằm trong list hết tiền
                    long WonMoney = pokerRoom.Poker_Pot; // trừ dần
                    for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                        long moneyBet = pokerRoom.Poker_BetAmountTotal.get(i);
                        if ((moneyBet - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner)) > 0){ // nếu số tiền đặt cượt > số tiền cược ng chơi đặt thì trả lại // = 0 là ng win rôi
                            long MoneyNeedPayBack = moneyBet - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner);
                            // trừ tiền dư
                            WonMoney -= MoneyNeedPayBack;
                            // trả lại ng đặt
                            PokerGameInstance.AddMoney(MoneyNeedPayBack, new User(pokerRoom.Poker_JoinedGameID.get(i), 0),MoneyType.GOLD);
                        }
                    }
                    PokerGameInstance.AddMoney(WonMoney, new User(idOfWinner, 0),MoneyType.GOLD);
                }
                else {
                    PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(idOfWinner, 0),MoneyType.GOLD);
                }
//                }

                new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                SetInfoForNewGame_1(pokerRoom,true,false);
                                SetInfoForNewGame_1_9_SECOND(pokerRoom);
                                ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                                pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                            }
                        },
                        POKER_COUNTDOWN_SHOWDOWN
                );
            }
            else {
                // set Turn
                int indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.DEALER_SMALLBLIND);
                if (indexOfPlayerSmallBlind == -1){
                    indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.SMALLBLIND);
                }
                for (int k = 0; k < 2; k++){
                    if (k == indexOfPlayerSmallBlind){
                        pokerRoom.Poker_Turn.set(k, true);
                    }
                    else {
                        pokerRoom.Poker_Turn.set(k, false);
                    }
                }
                // set Poker_AvailableAction
                int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
                for (int k = 0; k < 2; k++){
                    if (k == indexOfTurn){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,true,false,false,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                    }
                }
            }
        }
        else { // ko thay đổi phase + community cards // trường họp 2 ng chơi - ko có trường hợp này xảy ra
            // set lại Turn
            pokerRoom.Poker_Turn.set(indexOfPlayer, false);
            if (indexOfPlayer == (pokerRoom.Poker_Turn.size() - 1)){
                pokerRoom.Poker_Turn.set(0, true);
            }
            else {
                pokerRoom.Poker_Turn.set(indexOfPlayer + 1, true);
            }
            // set lại AvailableAction
            int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
            for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                if (k == indexOfTurn){
                    if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else {
                        if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.BET)) {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                        }
                        else {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,true,true,false,false));
                        }
                    }
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
            }
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    public void CHECK_2 (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request) throws IOException {
        // giữ nguyên
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        // - Poker_newMinBet
        int indexOfPlayer = pokerRoom.Poker_JoinedGameID.indexOf(request.getPlayerid()); // index của player trong joined gameid
        // trừ tiền
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexOfPlayer);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set lại LastAction == check
        pokerRoom.Poker_LastAction.set(indexOfPlayer, PLAYEROPTION.CHECK);
        // check xem có nên chuyển phase - Poker_phase - Poker_CommunityCards - Poker_turn - Poker_AvailableAction
        boolean CheckToNextPhase_1 = true; // mặc định là luôn chuyển phase - 50% - $ bằng nhau
        for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
            if (pokerRoom.Poker_BetAmount.get(i) - pokerRoom.Poker_NewMinBet != 0){
                if (!pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                    CheckToNextPhase_1 = false; // nếu có sự chênh lệch là tiếp tục ko chuyển phase
                    break;
                }
            }
        }
        boolean CheckToNextPhase_2 = true; // mặc định là luôn chuyển phase - 50% - all player tới lượt
        CheckToNextPhase_2 = !pokerRoom.Poker_LastAction.contains(PLAYEROPTION.NOTHING);

        if (CheckToNextPhase_1 && CheckToNextPhase_2){ // chuyển phase
            // set phase
            switch (pokerRoom.Poker_phase){
                case PHASE.FLOP:
                    pokerRoom.Poker_phase = PHASE.TURN;
                    break;
                case PHASE.TURN:
                    pokerRoom.Poker_phase = PHASE.RIVER;
                    break;
                case PHASE.RIVER:
                    pokerRoom.Poker_phase = PHASE.SHOWDOWN;
                    break;
            }
            // set thêm Pot từ Pot cũ + thêm tổng số dư bet của lượt này + sau đó xóa betamount
            for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
                pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(i);
            }
            // clear betamount
            for (int m = 0; m < 2; m++){
                pokerRoom.Poker_BetAmount.set(m, 0L);
            }
            // reset NewMinBet
            pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount); // 0
            // reset LastAction cho phase mới
            for (int l = 0; l < 2; l++){
                pokerRoom.Poker_LastAction.set(l, PLAYEROPTION.NOTHING);
            }
            int NumberOfCommunityCardAdded = 0;
            switch (pokerRoom.Poker_phase){
                case PHASE.TURN:
                case PHASE.RIVER:
                    NumberOfCommunityCardAdded = 1;
                    break;
                default:
                    break;
            }
            if (pokerRoom.Poker_phase == PHASE.FLOP || pokerRoom.Poker_phase == PHASE.TURN || pokerRoom.Poker_phase == PHASE.RIVER){
                for (int j = 0; j < NumberOfCommunityCardAdded; j++){ // 3 lá bài community đầu / room
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);
                    pokerRoom.Poker_CommunityCards.add(pokerCard);
                }
            }
            // CHECK KẾT THÚC GAME LUÔN KHI CÓ 1 NG OUT OF MONEY HOẶC ĐẾN PHASE CUỐI
            if (pokerRoom.Poker_phase == PHASE.SHOWDOWN || (pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size()) == 1){
                if ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size()) == 1){ // nếu chưa đến SHOWDOWN => add đủ community
                    int NeededCommunityCards = POKER_MAX_COMMUNITY_CARDS - pokerRoom.Poker_CommunityCards.size(); // số community cards cần thêm
                    for (int j = 0; j < NeededCommunityCards; j++){
                        // tạo random 1 - số bài hiện tại trong deck
                        Random rand = new Random();
                        int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                        // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                        PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                        pokerRoom.Poker_AllCard52.remove(rand_index);
                        pokerRoom.Poker_CommunityCards.add(pokerCard);
                    }
                }
                // set lại CountDown
                pokerRoom.Poker_CountDown = POKER_COUNTDOWN_SHOWDOWN;
                // set Turn - phase cuối ko có lượt ai cả
                for (int k = 0; k < 2; k++){
                    pokerRoom.Poker_Turn.set(k, false);
                }
                // set Poker_AvailableAction - lên phase cuối: ko có available Action
                for (int k = 0; k < 2; k++){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
                // Check độ mạnh của bài các ng chơi còn lại - check hết ko trừ FOLDER
                pokerRoom.Poker_CardStrenght = CHECK_PLAYER_CARDSTRENGHT(pokerRoom.Poker_playerCardHolder, pokerRoom.Poker_CommunityCards);
//                // trường hợp bài 2 ng mạnh bằng nhau
//                if (pokerRoom.Poker_CardStrenght.get(0) == pokerRoom.Poker_CardStrenght.get(1)){
//                    for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){ // trả lại toàn bộ tiền đã cược cho 2 ng chơi
//                        PokerGameInstance.AddMoney(pokerRoom.Poker_BetAmountTotal.get(i), new User(pokerRoom.Poker_JoinedGameID.get(i)),MoneyType.GOLD);
//                    }
//                }
//                else {
                int indexOfWinner = pokerRoom.Poker_CardStrenght.indexOf(Collections.max(pokerRoom.Poker_CardStrenght));
                int idOfWinner = pokerRoom.Poker_JoinedGameID.get(indexOfWinner);
                if (pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(idOfWinner)){ // trường hợp player nằm trong list hết tiền
                    long WonMoney = pokerRoom.Poker_Pot; // trừ dần
                    for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                        long moneyBet = pokerRoom.Poker_BetAmountTotal.get(i);
                        if ((moneyBet - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner)) > 0){ // nếu số tiền đặt cượt > số tiền cược ng chơi đặt thì trả lại // = 0 là ng win rôi
                            long MoneyNeedPayBack = moneyBet - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner);
                            // trừ tiền dư
                            WonMoney -= MoneyNeedPayBack;
                            // trả lại ng đặt
                            PokerGameInstance.AddMoney(MoneyNeedPayBack, new User(pokerRoom.Poker_JoinedGameID.get(i), 0),MoneyType.GOLD);
                        }
                    }
                    PokerGameInstance.AddMoney(WonMoney, new User(idOfWinner, 0),MoneyType.GOLD);
                }
                else {
                    PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(idOfWinner, 0),MoneyType.GOLD);
                }
//                }

                new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                SetInfoForNewGame_1(pokerRoom,true,false);
                                SetInfoForNewGame_1_9_SECOND(pokerRoom);
                                ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                                pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                            }
                        },
                        POKER_COUNTDOWN_SHOWDOWN
                );
            }
            else {
                // set Turn
                int indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.DEALER_SMALLBLIND);
                if (indexOfPlayerSmallBlind == -1){
                    indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.SMALLBLIND);
                }
                for (int k = 0; k < 2; k++){
                    if (k == indexOfPlayerSmallBlind){
                        pokerRoom.Poker_Turn.set(k, true);
                    }
                    else {
                        pokerRoom.Poker_Turn.set(k, false);
                    }
                }
                // set Poker_AvailableAction
                int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
                for (int k = 0; k < 2; k++){
                    if (k == indexOfTurn){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,true,false,false,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                    }
                }
            }
        }
        else { // ko thay đổi phase + community cards
            // set lại Turn
            pokerRoom.Poker_Turn.set(indexOfPlayer, false);
            if (indexOfPlayer == (pokerRoom.Poker_Turn.size() - 1)){
                pokerRoom.Poker_Turn.set(0, true);
            }
            else {
                pokerRoom.Poker_Turn.set(indexOfPlayer + 1, true);
            }
            // set lại AvailableAction
            int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
            for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                if (k == indexOfTurn){
                    if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else {
                        if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.BET)) {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                        }
                        else {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,true,true,false,false));
                        }
                    }
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
            }
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    public void RAISE_2 (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request){
        // giữ nguyên:
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_phase
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        // - Poker_CommunityCards
        // - Poker_Pot
        int indexOfPlayer = pokerRoom.Poker_JoinedGameID.indexOf(request.getPlayerid()); // index của player trong joined gameid
        // trừ tiền tương ứng
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexOfPlayer);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        // set lại newMinBet
        pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount);
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set lại Turn
        pokerRoom.Poker_Turn.set(indexOfPlayer, false);
        if (indexOfPlayer == (pokerRoom.Poker_Turn.size() - 1)){
            pokerRoom.Poker_Turn.set(0, true);
        }
        else {
            pokerRoom.Poker_Turn.set(indexOfPlayer + 1, true);
        }
        // set lại LastAction == raise
        pokerRoom.Poker_LastAction.set(indexOfPlayer, PLAYEROPTION.RAISE);
        // set lại AvailableAction

        int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
        for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
            if (k == indexOfTurn){
                if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                }
                else {
                    if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.CHECK)){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,false,false,true,false));
                    }
                }
            }
            else {
                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
            }
        }

        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    public void BET_2 (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request){
        // giữ nguyên:
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_phase
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        // - Poker_CommunityCards
        // - Poker_Pot
        int indexOfPlayer = pokerRoom.Poker_JoinedGameID.indexOf(request.getPlayerid()); // index của player trong joined gameid
        // trừ tiền tương ứng
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexOfPlayer);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        // set lại newMinBet
        pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount);
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set lại LastAction == bet
        pokerRoom.Poker_LastAction.set(indexOfPlayer, PLAYEROPTION.BET);
        // set lại Turn
        pokerRoom.Poker_Turn.set(indexOfPlayer, false);
        if (indexOfPlayer == (pokerRoom.Poker_Turn.size() - 1)){
            pokerRoom.Poker_Turn.set(0, true);
        }
        else {
            pokerRoom.Poker_Turn.set(indexOfPlayer + 1, true);
        }
        // set lại AvailableAction
        int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
        for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
            if (k == indexOfTurn){
                if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.CHECK)){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,false,false,true,false));
                }
            }
            else {
                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
            }
        }

        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }

    /* -------------------------------------<$$$>------------------------------------- */
    @Descreption_Anno(Name = "ACTION COMMAND - LEVEL II")
    public void FOLD (PokerRoom pokerRoom, ChannelContextInfo ctx) throws IOException {
        // ko trừ tiền
        // ko set betamount
        // 0 thay đổi newminbet
        // 0 thay đổi dealerturn
        int indexPlayerInJoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId());
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set LastAction = FOLD + add vào list FOLDER
        pokerRoom.Poker_LastAction.set(indexPlayerInJoinedGameId, PLAYEROPTION.FOLD);
        pokerRoom.Poker_JoinedGameID_FOLDER.add(ctx.getUserId());
        // check chuyển phase
        boolean CheckToNextPhase_1 = true; // mặc định là luôn chuyển phase - 50% - $ bằng nhau
        for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
            if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // phải bỏ ng fold ra => chỉ tính ng ko FOLD
                if (!pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // phari bỏ ng đã đặt hết tiền
                    if (pokerRoom.Poker_BetAmount.get(i) - pokerRoom.Poker_NewMinBet != 0){
                        CheckToNextPhase_1 = false; // nếu có sự chênh lệch là tiếp tục ko chuyển phase
                        break;
                    }
                }
            }
        }
        boolean CheckToNextPhase_2 = true; // mặc định là luôn chuyển phase - 50% - all player tới lượt
        CheckToNextPhase_2 = !pokerRoom.Poker_LastAction.contains(PLAYEROPTION.NOTHING);
        if (CheckToNextPhase_1 && CheckToNextPhase_2) { // chuyển phase
            // set phase
            switch (pokerRoom.Poker_phase){
                case PHASE.PRE_FLOP:
                    pokerRoom.Poker_phase = PHASE.FLOP;
                    break;
                case PHASE.FLOP:
                    pokerRoom.Poker_phase = PHASE.TURN;
                    break;
                case PHASE.TURN:
                    pokerRoom.Poker_phase = PHASE.RIVER;
                    break;
                case PHASE.RIVER:
                    pokerRoom.Poker_phase = PHASE.SHOWDOWN;
                    break;
            }
            // set pot theo tổng số dư Bet Amount - sau đó mới clear betamount
            for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
                pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(i);
            }
            // clear betamount
            for (int m = 0; m < pokerRoom.Poker_BetAmount.size(); m++){
                pokerRoom.Poker_BetAmount.set(m, 0L);
            }
            // reset NewMinBet
            pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount); // 0
            // reset LastAction cho phase mới - phải loai bỏ người FOLD vẫn là FOLD - loại bỏ ng OUTOFMONEY vẫn là OUTOFMONEY
            for (int l = 0; l < pokerRoom.Poker_LastAction.size(); l++){
                if (pokerRoom.Poker_LastAction.get(l) != PLAYEROPTION.FOLD){
                    if (pokerRoom.Poker_LastAction.get(l) != PLAYEROPTION.OUT_OF_MONEY){
                        pokerRoom.Poker_LastAction.set(l, PLAYEROPTION.NOTHING);
                    }
                }
            }
            // set community cards
            int NumberOfCommunityCardAdded = 0;
            switch (pokerRoom.Poker_phase){
                case PHASE.FLOP:
                    NumberOfCommunityCardAdded = 3;
                    break;
                case PHASE.TURN:
                case PHASE.RIVER:
                    NumberOfCommunityCardAdded = 1;
                    break;
                default:
                    NumberOfCommunityCardAdded = 0;
                    break;
            }
            if (pokerRoom.Poker_phase == PHASE.FLOP || pokerRoom.Poker_phase == PHASE.TURN || pokerRoom.Poker_phase == PHASE.RIVER){
                for (int j = 0; j < NumberOfCommunityCardAdded; j++){ // 3 lá bài community đầu / room
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);
                    pokerRoom.Poker_CommunityCards.add(pokerCard);
                }
            }
            // set Turn + availableAction
            /* remove tất cả FOLDER khỏi Poker_JoinedGameID - trừ SmallBlind -  bằng cách tạo 1 array add các player ko phải FOLDER + smallblind (chưa xem xét xem smallblind có FOLDER ko) - lấy id của ng giữ turn tiếp - check xem smallblind có FOLD chưa */
            // trường hợp phase cuối hoặc còn duy nhất 1 ng chơi sau khi loại folder và các ng chơi hết tiền
            if (pokerRoom.Poker_phase == PHASE.SHOWDOWN
                    || ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1)
                            && pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() > 0){ // ko có lượt ai cả + ko có available action + check độ mạnh (cardstrenght)
                if ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1){
                    int NeededCommunityCards = POKER_MAX_COMMUNITY_CARDS - pokerRoom.Poker_CommunityCards.size(); // số community cards cần thêm
                    for (int j = 0; j < NeededCommunityCards; j++){
                        // tạo random 1 - số bài hiện tại trong deck
                        Random rand = new Random();
                        int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                        // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                        PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                        pokerRoom.Poker_AllCard52.remove(rand_index);
                        pokerRoom.Poker_CommunityCards.add(pokerCard);
                    }
                }
                // set lại CountDown
                pokerRoom.Poker_CountDown = POKER_COUNTDOWN_SHOWDOWN;
                // set Turn - phase cuối ko có lượt ai cả
                for (int k = 0; k < pokerRoom.Poker_Turn.size(); k++){
                    pokerRoom.Poker_Turn.set(k, false);
                }
                // set Poker_AvailableAction - lên phase cuối: ko có available Action
                for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
                // Check độ mạnh của bài các ng chơi còn lại - check hết ko trừ FOLDER
                pokerRoom.Poker_CardStrenght = CHECK_PLAYER_CARDSTRENGHT(pokerRoom.Poker_playerCardHolder, pokerRoom.Poker_CommunityCards); // Sau đó cộng tiền cho ng chơi thắng cuộc = toàn bộ Pot - Tạo list bao gồm các ng ko phải FOLDER + list card strengt tương đương - xác định id ng win rồi + tiền
                ArrayList<Integer> Poker_PlayerJoinedGameId_notFOLDER = new ArrayList<>();
                ArrayList<Long> Poker_CardStrenght_notFOLDER = new ArrayList<>();
                long maxBetAmount_notFOLDER = 0; // tiền betamounttotal cao nhất trong những ng ko fold
                for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){ // loại bỏ folder ra
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        Poker_PlayerJoinedGameId_notFOLDER.add(pokerRoom.Poker_JoinedGameID.get(i));
                        Poker_CardStrenght_notFOLDER.add(pokerRoom.Poker_CardStrenght.get(i));
                        if (pokerRoom.Poker_BetAmountTotal.get(i) > maxBetAmount_notFOLDER){
                            maxBetAmount_notFOLDER = pokerRoom.Poker_BetAmountTotal.get(i);
                        }
                    }
                }
//                int numberOf_EvenCardStrenghtPlayer = Collections.frequency(Poker_CardStrenght_notFOLDER, Collections.max(Poker_CardStrenght_notFOLDER)); // số ng huề
//                if (numberOf_EvenCardStrenghtPlayer != 1){
//                    ArrayList<Integer> Poker_PlayerJoinedGameId_notFOLDER_Even = new ArrayList<>();
//                    ArrayList<Long> Poker_CardStrenght_notFOLDER_Even = new ArrayList<>();
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER.size(); i++){ // loại bỏ ng thua ra, lấy ng thắng trùng bài
//                        if (Poker_CardStrenght_notFOLDER.get(i) == Collections.max(Poker_CardStrenght_notFOLDER)){
//                            Poker_PlayerJoinedGameId_notFOLDER_Even.add(Poker_PlayerJoinedGameId_notFOLDER.get(i));
//                            Poker_CardStrenght_notFOLDER_Even.add(Poker_CardStrenght_notFOLDER.get(i));
//                        }
//                    }
//
//                    long remainMoneyInPot = pokerRoom.Poker_Pot; // số tiền còn lại sau khi trả tiền betamounttotal cho ng thắng - còn ng thua + ng fold
//                    long maxBetAmount_Winner_Even = 0; // số tiền cược max trong các ng thắng - để sau này so với tiền cược max của toàn bộ player_notfolder
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER_Even.size(); i++){ // trả tiền đã đặt cho các ng chơi thắng trùng bài
//                        int id = Poker_PlayerJoinedGameId_notFOLDER_Even.get(i);
//                        int index_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id);
//                        long betamounttotal = pokerRoom.Poker_BetAmountTotal.get(index_JoinedGameId);
//                        PokerGameInstance.AddMoney(betamounttotal, new User(id),MoneyType.GOLD);
//                        remainMoneyInPot -= betamounttotal;
//                        if (betamounttotal > maxBetAmount_Winner_Even){
//                            maxBetAmount_Winner_Even = betamounttotal;
//                        }
//                    }
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER_Even.size(); i++){ // chia tiền còn lại trong Pot theo tỷ lệ
//                        int id = Poker_PlayerJoinedGameId_notFOLDER_Even.get(i);
//                        int index_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id);
//                        long betamounttotal = pokerRoom.Poker_BetAmountTotal.get(index_JoinedGameId);
//
//                        float ratio = (float) betamounttotal / (pokerRoom.Poker_Pot - remainMoneyInPot); // tỷ lệ betamount so với tổng lượt bet amount trong số ng huề
//                        long receivedMoney = Math.round(remainMoneyInPot * ratio);
//                        PokerGameInstance.AddMoney(receivedMoney, new User(id),MoneyType.GOLD);
//                    }
//                }
//                else {
                int indexOfWinner = Poker_CardStrenght_notFOLDER.indexOf(Collections.max(Poker_CardStrenght_notFOLDER));
                int idOfWinner = Poker_PlayerJoinedGameId_notFOLDER.get(indexOfWinner);

                int indexOfWinner_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(idOfWinner);
                if (pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(idOfWinner)){ // trường hợp player nằm trong list hết tiền
                    long WonMoney = pokerRoom.Poker_Pot; // trừ dần
                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER.size(); i++){
                        int idPlayerNotFOLDER = Poker_PlayerJoinedGameId_notFOLDER.get(i);
                        int indexOfidPlayerNotFOLDER_JoinGameId = pokerRoom.Poker_JoinedGameID.indexOf(idPlayerNotFOLDER); //
                        long moneyBetOfPlayerNotFOLDER = pokerRoom.Poker_BetAmountTotal.get(indexOfidPlayerNotFOLDER_JoinGameId);
                        if ((moneyBetOfPlayerNotFOLDER - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner_JoinedGameId)) > 0){ // nếu số tiền đặt cượt > số tiền cược ng chơi đặt thì trả lại // = 0 là ng win rôi
                            long MoneyNeedPayBack = moneyBetOfPlayerNotFOLDER - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner_JoinedGameId);
                            // trừ tiền dư
                            WonMoney -= MoneyNeedPayBack;
                            // trả lại ng đặt
                            PokerGameInstance.AddMoney(MoneyNeedPayBack, new User(idPlayerNotFOLDER, 0),MoneyType.GOLD);
                        }
                    }
                    PokerGameInstance.AddMoney(WonMoney, new User(idOfWinner, 0),MoneyType.GOLD);
                }
                else {
                    PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(idOfWinner, 0),MoneyType.GOLD);
                }
//                }


                new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                SetInfoForNewGame_1(pokerRoom,true,false);
                                SetInfoForNewGame_1_9_SECOND(pokerRoom);
                                ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                                pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                            }
                        },
                        POKER_COUNTDOWN_SHOWDOWN
                );
            }
            else {
                // CHECK KẾT THÚC GAME LUÔN - KO CÒN AI CHƠI KO CẦN SHƠ 5 COMMUNITY
                ArrayList<Integer> NOTFOLD_PLAYER = new ArrayList<>();
                for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        NOTFOLD_PLAYER.add(pokerRoom.Poker_JoinedGameID.get(i));
                    }
                }
                if ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1){ // nếu còn duy nhất 1 ng chưa fold thì win luôn khỏi chơi tiếp
                    int idOfWinner = NOTFOLD_PLAYER.get(0);
                    // cộng tiền cho ng chơi - trừ tiền lúc đặt cượt rồi
                    PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(idOfWinner, 0), MoneyType.GOLD);
                    // Waiting Phase
                    SetInfoForNewGame_1(pokerRoom,false,true);
                    ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin chờ ván + send
                    pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room khi chờ tạo ván mới
                    // Preflop Phase sau POKER_COUNTDOWN_GAMEWAITING
                    new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    SetInfoForNewGame_1(pokerRoom,true,false);
                                    SetInfoForNewGame_1_9_SECOND(pokerRoom);
                                    ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                                    pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                                }
                            },
                            POKER_COUNTDOWN_GAMEWAITING
                    );
                }
                else {
                    // set turn + availableAction cho các phase còn lại
                    // set turn cho các phase còn lại
                    int indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.DEALER_SMALLBLIND);
                    if (indexOfPlayerSmallBlind == -1){
                        indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.SMALLBLIND);
                    }
                    int IdOfSmallBlind = pokerRoom.Poker_JoinedGameID.get(indexOfPlayerSmallBlind);
                    for (int i = 0; i < pokerRoom.Poker_Turn.size(); i++){
                        pokerRoom.Poker_Turn.set(i, false);
                    }
                    ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
                    for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                        if (i == indexOfPlayerSmallBlind){ // nếu là smallblind thì add
                            Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                        }
                        else { // nếu ko phải thì chỉ add các player ko phải FOLDER + ko phải OUTOFMONEY
                            if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                                Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                            }
                        }
                    } /* lấy id của ng giữ turn tiếp - check xem smallblind có FOLD hoặc OUTOFMONEY chưa */
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(IdOfSmallBlind) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(IdOfSmallBlind)){ // trường hợp SmallBlind chưa fold +  => ng kế thừa
                        pokerRoom.Poker_Turn.set(indexOfPlayerSmallBlind, true);
                    }
                    else { // trường hợp SmallBlind đã fold hoặc đã OUTOFMONEY => ng kế thừa + 1 trong Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY // trường hợp hết array => trở về 0
                        int IdOfSmallBlind_nextto = 0;
                        if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(IdOfSmallBlind) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
                            IdOfSmallBlind_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
                        }
                        else {
                            IdOfSmallBlind_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(IdOfSmallBlind) + 1);
                        }
                        int IndexOfSmallBlind_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfSmallBlind_nextto);
                        pokerRoom.Poker_Turn.set(IndexOfSmallBlind_nextto, true);
                    }
                    // set Poker_AvailableAction cho các phase còn lại
                    int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
                    for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                        if (k == indexOfTurn){
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,true,false,false,false));
                        }
                        else {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                        }
                    }
                }
            }
            // loại bỏ ng chơi fold ra khỏi các list cho lượt sau
            SetPlayerTo_FOLD_STATE(pokerRoom, indexPlayerInJoinedGameId);
        }
        else { // ko thay đổi phase + community cards
            // set lại Turn
            /* remove tất cả FOLDER khỏi Poker_JoinedGameID - bằng cách tạo 1 array add các player ko phải FOLDER + Player đang giữ lượt vừa FOLD */
            pokerRoom.Poker_Turn.set(indexPlayerInJoinedGameId, false);
            ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
            for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                if(i == indexPlayerInJoinedGameId){ // nếu ko phải thì chỉ add các player ko phải FOLDER
                    Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                }
                else {
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                    }
                }
            }
            int IdOfPlayerCurrentTurn_nextto = 0; // lấy id của ng giữ turn tiếp
            if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
                IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
            }
            else {
                IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1));
            }
            int IndexOfPlayerCurrent_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfPlayerCurrentTurn_nextto);
            pokerRoom.Poker_Turn.set(IndexOfPlayerCurrent_nextto, true);
            // set lại AvailableAction
            int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
            for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                if (k == indexOfTurn){
                    if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){ // preflop chỉ có raise và bet
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else { // các phase còn lại có check,call,bet,raise
                        if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.BET)) {
                            if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.CHECK)){
                                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                            }
                            else {
                                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,false,false,true,false));
                            }
                        }
                        else {
                            if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.CHECK)){
                                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,true,true,false,false));
                            }
                            else {
                                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,true,false,false,false));
                            }
                        }
                    }
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
            }
            // loại bỏ ng chơi fold ra khỏi các list
            SetPlayerTo_FOLD_STATE(pokerRoom, indexPlayerInJoinedGameId);
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin đã bớt ng chơi
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room sau khi bỏ ng chơi
    }
    public void CHECK (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request) throws IOException {
        // giữ nguyên
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        // - Poker_newMinBet
        int indexPlayerInJoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId()); // index của player trong joined gameid
        // trừ tiền
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexPlayerInJoinedGameId), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexPlayerInJoinedGameId), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexPlayerInJoinedGameId, currentMoney + pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId));
            pokerRoom.Poker_BetAmountTotal.set(indexPlayerInJoinedGameId, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexPlayerInJoinedGameId));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexPlayerInJoinedGameId, moneyGap + pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId));
            pokerRoom.Poker_BetAmountTotal.set(indexPlayerInJoinedGameId, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexPlayerInJoinedGameId));
        }
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set lại LastAction == check
        pokerRoom.Poker_LastAction.set(indexPlayerInJoinedGameId, PLAYEROPTION.CHECK);
        // check xem có nên chuyển phase - Poker_phase - Poker_CommunityCards - Poker_turn - Poker_AvailableAction
        boolean CheckToNextPhase_1 = true; // mặc định là luôn chuyển phase - 50% - $ bằng nhau
        for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
            if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // phải bỏ ng fold ra => chỉ tính ng ko FOLD
                if (!pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // phari bỏ ng đã đặt hết tiền
                    if (pokerRoom.Poker_BetAmount.get(i) - pokerRoom.Poker_NewMinBet != 0){
                        CheckToNextPhase_1 = false; // nếu có sự chênh lệch là tiếp tục ko chuyển phase
                        break;
                    }
                }
            }
        }
        boolean CheckToNextPhase_2 = true; // mặc định là luôn chuyển phase - 50% - all player tới lượt
        CheckToNextPhase_2 = !pokerRoom.Poker_LastAction.contains(PLAYEROPTION.NOTHING);

        if (CheckToNextPhase_1 && CheckToNextPhase_2) {
            // chuyển phase
            // set phase
            switch (pokerRoom.Poker_phase){
                case PHASE.FLOP:
                    pokerRoom.Poker_phase = PHASE.TURN;
                    break;
                case PHASE.TURN:
                    pokerRoom.Poker_phase = PHASE.RIVER;
                    break;
                case PHASE.RIVER:
                    pokerRoom.Poker_phase = PHASE.SHOWDOWN;
                    break;
            }
            // set thêm Pot từ Pot cũ + thêm tổng số dư bet của lượt này + sau đó xóa betamount
            for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
                pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(i);
            }
            // clear betamount
            for (int m = 0; m < pokerRoom.Poker_BetAmount.size(); m++){
                pokerRoom.Poker_BetAmount.set(m, 0L);
            }
            // reset NewMinBet
            pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount); // 0
            // set lại LastAction == OUT_OF_MONEY cho ng hết tiền - trong phase để lại check,call,bet,raise để set turn cho ng khác
            for (int i = 0; i < pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size(); i++){
                int IdOf_OUTOFMONEY = pokerRoom.Poker_JoinedGameID_OUTOFMONEY.get(i);
                int IndexOf_IdOf_OUTOFMONEY_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(IdOf_OUTOFMONEY);
                pokerRoom.Poker_LastAction.set(IndexOf_IdOf_OUTOFMONEY_JoinedGameId, PLAYEROPTION.OUT_OF_MONEY);
            }
            // reset LastAction cho phase mới - phải loai bỏ người FOLD vẫn là FOLD - loại bỏ ng OUTOFMONEY vẫn là OUTOFMONEY
            for (int l = 0; l < pokerRoom.Poker_LastAction.size(); l++){
                if ((pokerRoom.Poker_LastAction.get(l) != PLAYEROPTION.FOLD) && (pokerRoom.Poker_LastAction.get(l) != PLAYEROPTION.OUT_OF_MONEY)){
                    pokerRoom.Poker_LastAction.set(l, PLAYEROPTION.NOTHING);
                }
            }
            // set community cards
            int NumberOfCommunityCardAdded = 0;
            switch (pokerRoom.Poker_phase){
                case PHASE.TURN:
                case PHASE.RIVER:
                    NumberOfCommunityCardAdded = 1;
                    break;
                default:
                    break;
            }
            if (pokerRoom.Poker_phase == PHASE.TURN || pokerRoom.Poker_phase == PHASE.RIVER){
                for (int j = 0; j < NumberOfCommunityCardAdded; j++){ // 3 lá bài community đầu / room
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);
                    pokerRoom.Poker_CommunityCards.add(pokerCard);
                }
            }
            if (pokerRoom.Poker_phase == PHASE.SHOWDOWN
                    || ((pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() > 0)
                    && (pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1)){
                if ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1){
                    int NeededCommunityCards = POKER_MAX_COMMUNITY_CARDS - pokerRoom.Poker_CommunityCards.size(); // số community cards cần thêm
                    for (int j = 0; j < NeededCommunityCards; j++){
                        // tạo random 1 - số bài hiện tại trong deck
                        Random rand = new Random();
                        int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                        // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                        PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                        pokerRoom.Poker_AllCard52.remove(rand_index);
                        pokerRoom.Poker_CommunityCards.add(pokerCard);
                    }
                }
                // set lại CountDown
                pokerRoom.Poker_CountDown = POKER_COUNTDOWN_SHOWDOWN;
                // set Turn - phase cuối ko có lượt ai cả
                for (int k = 0; k < pokerRoom.Poker_Turn.size(); k++){
                    pokerRoom.Poker_Turn.set(k, false);
                }
                // set Poker_AvailableAction - lên phase cuối: ko có available Action
                for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
                // Check độ mạnh của bài các ng chơi còn lại - check hết ko trừ FOLDER
                pokerRoom.Poker_CardStrenght = CHECK_PLAYER_CARDSTRENGHT(pokerRoom.Poker_playerCardHolder, pokerRoom.Poker_CommunityCards); // Sau đó cộng tiền cho ng chơi thắng cuộc = toàn bộ Pot - Tạo list bao gồm các ng ko phải FOLDER + list card strengt tương đương - xác định id ng win rồi + tiền
                ArrayList<Integer> Poker_PlayerJoinedGameId_notFOLDER = new ArrayList<>();
                ArrayList<Long> Poker_CardStrenght_notFOLDER = new ArrayList<>();
                for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        Poker_PlayerJoinedGameId_notFOLDER.add(pokerRoom.Poker_JoinedGameID.get(i));
                        Poker_CardStrenght_notFOLDER.add(pokerRoom.Poker_CardStrenght.get(i));
                    }
                }
//                int numberOf_EvenCardStrenghtPlayer = Collections.frequency(Poker_CardStrenght_notFOLDER, Collections.max(Poker_CardStrenght_notFOLDER)); // số ng huề
//                if (numberOf_EvenCardStrenghtPlayer != 1){
//                    ArrayList<Integer> Poker_PlayerJoinedGameId_notFOLDER_Even = new ArrayList<>();
//                    ArrayList<Long> Poker_CardStrenght_notFOLDER_Even = new ArrayList<>();
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER.size(); i++){ // loại bỏ ng thua ra, lấy ng thắng trùng bài
//                        if (Poker_CardStrenght_notFOLDER.get(i) == Collections.max(Poker_CardStrenght_notFOLDER)){
//                            Poker_PlayerJoinedGameId_notFOLDER_Even.add(Poker_PlayerJoinedGameId_notFOLDER.get(i));
//                            Poker_CardStrenght_notFOLDER_Even.add(Poker_CardStrenght_notFOLDER.get(i));
//                        }
//                    }
//
//                    long remainMoneyInPot = pokerRoom.Poker_Pot; // số tiền còn lại sau khi trả tiền betamounttotal cho ng thắng - còn ng thua + ng fold
//                    long maxBetAmount_Winner_Even = 0; // số tiền cược max trong các ng thắng - để sau này so với tiền cược max của toàn bộ player_notfolder
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER_Even.size(); i++){ // trả tiền đã đặt cho các ng chơi thắng trùng bài
//                        int id = Poker_PlayerJoinedGameId_notFOLDER_Even.get(i);
//                        int index_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id);
//                        long betamounttotal = pokerRoom.Poker_BetAmountTotal.get(index_JoinedGameId);
//                        PokerGameInstance.AddMoney(betamounttotal, new User(id),MoneyType.GOLD);
//                        remainMoneyInPot -= betamounttotal;
//                        if (betamounttotal > maxBetAmount_Winner_Even){
//                            maxBetAmount_Winner_Even = betamounttotal;
//                        }
//                    }
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER_Even.size(); i++){ // chia tiền còn lại trong Pot theo tỷ lệ
//                        int id = Poker_PlayerJoinedGameId_notFOLDER_Even.get(i);
//                        int index_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id);
//                        long betamounttotal = pokerRoom.Poker_BetAmountTotal.get(index_JoinedGameId);
//
//                        float ratio = (float) betamounttotal / (pokerRoom.Poker_Pot - remainMoneyInPot); // tỷ lệ betamount so với tổng lượt bet amount trong số ng huề
//                        long receivedMoney = Math.round(remainMoneyInPot * ratio);
//                        PokerGameInstance.AddMoney(receivedMoney, new User(id),MoneyType.GOLD);
//                    }
//                }
//                else {
                int indexOfWinner = Poker_CardStrenght_notFOLDER.indexOf(Collections.max(Poker_CardStrenght_notFOLDER));
                int idOfWinner = Poker_PlayerJoinedGameId_notFOLDER.get(indexOfWinner);

                int indexOfWinner_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(idOfWinner);
                if (pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(idOfWinner)){ // trường hợp player nằm trong list hết tiền
                    long WonMoney = pokerRoom.Poker_Pot; // trừ dần
                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER.size(); i++){
                        int idPlayerNotFOLDER = Poker_PlayerJoinedGameId_notFOLDER.get(i);
                        int indexOfidPlayerNotFOLDER_JoinGameId = pokerRoom.Poker_JoinedGameID.indexOf(idPlayerNotFOLDER); //
                        long moneyBetOfPlayerNotFOLDER = pokerRoom.Poker_BetAmountTotal.get(indexOfidPlayerNotFOLDER_JoinGameId);
                        if ((moneyBetOfPlayerNotFOLDER - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner_JoinedGameId)) > 0){ // nếu số tiền đặt cượt > số tiền cược ng chơi đặt thì trả lại // = 0 là ng win rôi
                            long MoneyNeedPayBack = moneyBetOfPlayerNotFOLDER - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner_JoinedGameId);
                            // trừ tiền dư
                            WonMoney -= MoneyNeedPayBack;
                            // trả lại ng đặt
                            PokerGameInstance.AddMoney(MoneyNeedPayBack, new User(idPlayerNotFOLDER, 0),MoneyType.GOLD);
                        }
                    }
                    PokerGameInstance.AddMoney(WonMoney, new User(idOfWinner, 0),MoneyType.GOLD);
                }
                else {
                    PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(idOfWinner, 0),MoneyType.GOLD);
                }
//                }

                new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                SetInfoForNewGame_1(pokerRoom,true,false);
                                SetInfoForNewGame_1_9_SECOND(pokerRoom);
                                ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                                pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                            }
                        },
                        POKER_COUNTDOWN_SHOWDOWN
                );
            }
            else {
                // set Turn
            /* remove tất cả FOLDER khỏi Poker_JoinedGameID - trừ SmallBlind
            bằng cách tạo 1 array add các player ko phải FOLDER + smallblind (chưa xem xét xem smallblind có FOLDER ko) */
                int indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.DEALER_SMALLBLIND);
                if (indexOfPlayerSmallBlind == -1){
                    indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.SMALLBLIND);
                }
                int IdOfSmallBlind = pokerRoom.Poker_JoinedGameID.get(indexOfPlayerSmallBlind);
                for (int i = 0; i < pokerRoom.Poker_Turn.size(); i++){
                    pokerRoom.Poker_Turn.set(i, false);
                }
                ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
                for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                    if (i == indexOfPlayerSmallBlind){ // nếu là smallblind thì add
                        Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                    }
                    else { // nếu ko phải thì chỉ add các player ko phải FOLDER + ko phải OUTOFMONEY
                        if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                            Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                        }
                    }
                } /* lấy id của ng giữ turn tiếp - check xem smallblind có FOLD hoặc OUTOFMONEY chưa */
                if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(IdOfSmallBlind) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(IdOfSmallBlind)){ // trường hợp SmallBlind chưa fold +  => ng kế thừa
                    pokerRoom.Poker_Turn.set(indexOfPlayerSmallBlind, true);
                }
                else { // trường hợp SmallBlind đã fold hoặc đã OUTOFMONEY => ng kế thừa + 1 trong Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY // trường hợp hết array => trở về 0
                    int IdOfSmallBlind_nextto = 0;
                    if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(IdOfSmallBlind) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
                        IdOfSmallBlind_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
                    }
                    else {
                        IdOfSmallBlind_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(IdOfSmallBlind) + 1);
                    }
                    int IndexOfSmallBlind_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfSmallBlind_nextto);
                    pokerRoom.Poker_Turn.set(IndexOfSmallBlind_nextto, true);
                }
                // set Poker_AvailableAction
                int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
                for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                    if (k == indexOfTurn){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,true,false,false,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                    }
                }
            }
        }
        else {
            // ko thay đổi phase + community cards
            // set lại Turn
            /* remove tất cả FOLDER khỏi Poker_JoinedGameID
            bằng cách tạo 1 array add các player ko phải FOLDER + Player đang giữ lượt vừa FOLD */
            pokerRoom.Poker_Turn.set(indexPlayerInJoinedGameId, false);
            ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
            for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                if(i == indexPlayerInJoinedGameId){ // nếu ko phải thì chỉ add các player ko phải FOLDER
                    Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                }
                else {
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                    }
                }
            }
            int IdOfPlayerCurrentTurn_nextto = 0; // lấy id của ng giữ turn tiếp
            if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
                IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
            }
            else {
                IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1));
            }
            int IndexOfPlayerCurrent_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfPlayerCurrentTurn_nextto);
            pokerRoom.Poker_Turn.set(IndexOfPlayerCurrent_nextto, true);
            // set lại AvailableAction
            int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
            for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                if (k == indexOfTurn){
                    if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.BET)) {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,true,true,false,false));
                    }
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
            }
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    public void CALL (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request) throws IOException {
        /* Giữ nguyên
         - Poker_IdOfNextDealer
         - Poker_JoinedGameID
         - Poker_DealerTurn
         - Poker_playerCardHolder
         - Poker_AllCard52
         - Poker_newMinBet */
        int indexPlayerInJoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId()); // index của player trong joined gameid
        // trừ tiền
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexPlayerInJoinedGameId), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexPlayerInJoinedGameId), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexPlayerInJoinedGameId, currentMoney + pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId));
            pokerRoom.Poker_BetAmountTotal.set(indexPlayerInJoinedGameId, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexPlayerInJoinedGameId));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexPlayerInJoinedGameId, moneyGap + pokerRoom.Poker_BetAmount.get(indexPlayerInJoinedGameId));
            pokerRoom.Poker_BetAmountTotal.set(indexPlayerInJoinedGameId, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexPlayerInJoinedGameId));
        }
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set LastAction = call
        pokerRoom.Poker_LastAction.set(indexPlayerInJoinedGameId, PLAYEROPTION.CALL);
        // check xem có nên chuyển phase - Poker_phase - Poker_CommunityCards - Poker_turn - Poker_AvailableAction
        boolean CheckToNextPhase_1 = true; // mặc định là luôn chuyển phase - 50% - $ bằng nhau
        for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
            if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // phải bỏ ng fold ra => chỉ tính ng ko FOLD
                if (!pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){ // phari bỏ ng đã đặt hết tiền
                    if (pokerRoom.Poker_BetAmount.get(i) - pokerRoom.Poker_NewMinBet != 0){
                        CheckToNextPhase_1 = false; // nếu có sự chênh lệch là tiếp tục ko chuyển phase
                        break;
                    }
                }
            }
        }
        boolean CheckToNextPhase_2 = true; // mặc định là luôn chuyển phase - 50% - all player tới lượt
        CheckToNextPhase_2 = !pokerRoom.Poker_LastAction.contains(PLAYEROPTION.NOTHING);

        if (CheckToNextPhase_1 && CheckToNextPhase_2) {
            // chuyển phase
            // set phase
            switch (pokerRoom.Poker_phase){
                case PHASE.PRE_FLOP:
                    pokerRoom.Poker_phase = PHASE.FLOP;
                    break;
                case PHASE.FLOP:
                    pokerRoom.Poker_phase = PHASE.TURN;
                    break;
                case PHASE.TURN:
                    pokerRoom.Poker_phase = PHASE.RIVER;
                    break;
                case PHASE.RIVER:
                    pokerRoom.Poker_phase = PHASE.SHOWDOWN;
                    break;
            }
            // set pot theo tổng số dư Bet Amount - sau đó mới clear betamount
            for (int i = 0; i < pokerRoom.Poker_BetAmount.size(); i++){
                pokerRoom.Poker_Pot += pokerRoom.Poker_BetAmount.get(i);
            }
            // clear betamount
            for (int m = 0; m < pokerRoom.Poker_BetAmount.size(); m++){
                pokerRoom.Poker_BetAmount.set(m, 0L);
            }
            // reset NewMinBet
            pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount); // 0
            // set lại LastAction == OUT_OF_MONEY cho ng hết tiền - trong phase để lại check,call,bet,raise để set turn cho ng khác
            for (int i = 0; i < pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size(); i++){
                int IdOf_OUTOFMONEY = pokerRoom.Poker_JoinedGameID_OUTOFMONEY.get(i);
                int IndexOf_IdOf_OUTOFMONEY_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(IdOf_OUTOFMONEY);
                pokerRoom.Poker_LastAction.set(IndexOf_IdOf_OUTOFMONEY_JoinedGameId, PLAYEROPTION.OUT_OF_MONEY);
            }
            // reset LastAction cho phase mới - phải loai bỏ người FOLD vẫn là FOLD - loại bỏ ng OUTOFMONEY vẫn là OUTOFMONEY
            for (int l = 0; l < pokerRoom.Poker_LastAction.size(); l++){
                if ((pokerRoom.Poker_LastAction.get(l) != PLAYEROPTION.FOLD) && (pokerRoom.Poker_LastAction.get(l) != PLAYEROPTION.OUT_OF_MONEY)){
                    pokerRoom.Poker_LastAction.set(l, PLAYEROPTION.NOTHING);
                }
            }
            // set community cards
            int NumberOfCommunityCardAdded = 0;
            switch (pokerRoom.Poker_phase){
                case PHASE.FLOP:
                    NumberOfCommunityCardAdded = 3;
                    break;
                case PHASE.TURN:
                case PHASE.RIVER:
                    NumberOfCommunityCardAdded = 1;
                    break;
                default:
                    break;
            }
            if (pokerRoom.Poker_phase == PHASE.FLOP || pokerRoom.Poker_phase == PHASE.TURN || pokerRoom.Poker_phase == PHASE.RIVER){
                for (int j = 0; j < NumberOfCommunityCardAdded; j++){ // 3 lá bài community đầu / room
                    // tạo random 1 - số bài hiện tại trong deck
                    Random rand = new Random();
                    int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                    // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                    PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                    pokerRoom.Poker_AllCard52.remove(rand_index);
                    pokerRoom.Poker_CommunityCards.add(pokerCard);
                }
            }
            // set Turn + availableAction
            /* remove tất cả FOLDER khỏi Poker_JoinedGameID - trừ SmallBlind -  bằng cách tạo 1 array add các player ko phải FOLDER + smallblind (chưa xem xét xem smallblind có FOLDER ko) - lấy id của ng giữ turn tiếp - check xem smallblind có FOLD chưa */
            if (pokerRoom.Poker_phase == PHASE.SHOWDOWN
                    || ((pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() > 0)
                    && (pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1)){ // ko có lượt ai cả + ko có available action + check độ mạnh (cardstrenght)
                if ((pokerRoom.Poker_JoinedGameID.size() - pokerRoom.Poker_JoinedGameID_OUTOFMONEY.size() - pokerRoom.Poker_JoinedGameID_FOLDER.size()) == 1){
                    int NeededCommunityCards = POKER_MAX_COMMUNITY_CARDS - pokerRoom.Poker_CommunityCards.size(); // số community cards cần thêm
                    for (int j = 0; j < NeededCommunityCards; j++){
                        // tạo random 1 - số bài hiện tại trong deck
                        Random rand = new Random();
                        int rand_index = rand.nextInt(pokerRoom.Poker_AllCard52.size());
                        // tạo pokercard từ 1 lá bài random index trong phần bài còn lại + xóa lá bài trong bài còn lại
                        PokerCard pokerCard = pokerRoom.Poker_AllCard52.get(rand_index);
                        pokerRoom.Poker_AllCard52.remove(rand_index);
                        pokerRoom.Poker_CommunityCards.add(pokerCard);
                    }
                }
                // set lại CountDown
                pokerRoom.Poker_CountDown = POKER_COUNTDOWN_SHOWDOWN;
                // set Turn - phase cuối ko có lượt ai cả
                for (int k = 0; k < pokerRoom.Poker_Turn.size(); k++){
                    pokerRoom.Poker_Turn.set(k, false);
                }
                // set Poker_AvailableAction - lên phase cuối: ko có available Action
                for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
                // Check độ mạnh của bài các ng chơi còn lại - check hết ko trừ FOLDER
                pokerRoom.Poker_CardStrenght = CHECK_PLAYER_CARDSTRENGHT(pokerRoom.Poker_playerCardHolder, pokerRoom.Poker_CommunityCards);
                ArrayList<Integer> Poker_PlayerJoinedGameId_notFOLDER = new ArrayList<>(); // Sau đó cộng tiền cho ng chơi thắng cuộc = toàn bộ Pot - Tạo list bao gồm các ng ko phải FOLDER + list card strengt tương đương - xác định id ng win rồi + tiền
                ArrayList<Long> Poker_CardStrenght_notFOLDER = new ArrayList<>();
                for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        Poker_PlayerJoinedGameId_notFOLDER.add(pokerRoom.Poker_JoinedGameID.get(i));
                        Poker_CardStrenght_notFOLDER.add(pokerRoom.Poker_CardStrenght.get(i));
                    }
                }
//                int numberOf_EvenCardStrenghtPlayer = Collections.frequency(Poker_CardStrenght_notFOLDER, Collections.max(Poker_CardStrenght_notFOLDER)); // số ng huề
//                if (numberOf_EvenCardStrenghtPlayer != 1){
//                    ArrayList<Integer> Poker_PlayerJoinedGameId_notFOLDER_Even = new ArrayList<>();
//                    ArrayList<Long> Poker_CardStrenght_notFOLDER_Even = new ArrayList<>();
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER.size(); i++){ // loại bỏ ng thua ra, lấy ng thắng trùng bài
//                        if (Poker_CardStrenght_notFOLDER.get(i) == Collections.max(Poker_CardStrenght_notFOLDER)){
//                            Poker_PlayerJoinedGameId_notFOLDER_Even.add(Poker_PlayerJoinedGameId_notFOLDER.get(i));
//                            Poker_CardStrenght_notFOLDER_Even.add(Poker_CardStrenght_notFOLDER.get(i));
//                        }
//                    }
//
//                    long remainMoneyInPot = pokerRoom.Poker_Pot; // số tiền còn lại sau khi trả tiền betamounttotal cho ng thắng - còn ng thua + ng fold
//                    long maxBetAmount_Winner_Even = 0; // số tiền cược max trong các ng thắng - để sau này so với tiền cược max của toàn bộ player_notfolder
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER_Even.size(); i++){ // trả tiền đã đặt cho các ng chơi thắng trùng bài
//                        int id = Poker_PlayerJoinedGameId_notFOLDER_Even.get(i);
//                        int index_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id);
//                        long betamounttotal = pokerRoom.Poker_BetAmountTotal.get(index_JoinedGameId);
//                        PokerGameInstance.AddMoney(betamounttotal, new User(id),MoneyType.GOLD);
//                        remainMoneyInPot -= betamounttotal;
//                        if (betamounttotal > maxBetAmount_Winner_Even){
//                            maxBetAmount_Winner_Even = betamounttotal;
//                        }
//                    }
//                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER_Even.size(); i++){ // chia tiền còn lại trong Pot theo tỷ lệ
//                        int id = Poker_PlayerJoinedGameId_notFOLDER_Even.get(i);
//                        int index_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id);
//                        long betamounttotal = pokerRoom.Poker_BetAmountTotal.get(index_JoinedGameId);
//
//                        float ratio = (float) betamounttotal / (pokerRoom.Poker_Pot - remainMoneyInPot); // tỷ lệ betamount so với tổng lượt bet amount trong số ng huề
//                        long receivedMoney = Math.round(remainMoneyInPot * ratio);
//                        PokerGameInstance.AddMoney(receivedMoney, new User(id),MoneyType.GOLD);
//                    }
//                }
//                else {
                int indexOfWinner = Poker_CardStrenght_notFOLDER.indexOf(Collections.max(Poker_CardStrenght_notFOLDER));
                int id_OfWinner = Poker_PlayerJoinedGameId_notFOLDER.get(indexOfWinner);

                int indexOfWinner_JoinedGameId = pokerRoom.Poker_JoinedGameID.indexOf(id_OfWinner);
                if (pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(id_OfWinner)){ // trường hợp player nằm trong list hết tiền
                    long WonMoney = pokerRoom.Poker_Pot; // trừ dần
                    for (int i = 0; i < Poker_PlayerJoinedGameId_notFOLDER.size(); i++){
                        int idPlayerNotFOLDER = Poker_PlayerJoinedGameId_notFOLDER.get(i);
                        int indexOfidPlayerNotFOLDER_JoinGameId = pokerRoom.Poker_JoinedGameID.indexOf(idPlayerNotFOLDER); //
                        long moneyBetOfPlayerNotFOLDER = pokerRoom.Poker_BetAmountTotal.get(indexOfidPlayerNotFOLDER_JoinGameId);
                        if ((moneyBetOfPlayerNotFOLDER - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner_JoinedGameId)) > 0){ // nếu số tiền đặt cượt > số tiền cược ng chơi đặt thì trả lại // = 0 là ng win rôi
                            long MoneyNeedPayBack = moneyBetOfPlayerNotFOLDER - pokerRoom.Poker_BetAmountTotal.get(indexOfWinner_JoinedGameId);
                            // trừ tiền dư
                            WonMoney -= MoneyNeedPayBack;
                            // trả lại ng đặt
                            PokerGameInstance.AddMoney(MoneyNeedPayBack, new User(idPlayerNotFOLDER, 0),MoneyType.GOLD);
                        }
                    }
                    PokerGameInstance.AddMoney(WonMoney, new User(id_OfWinner, 0),MoneyType.GOLD);
                }
                else {
                    PokerGameInstance.AddMoney(pokerRoom.Poker_Pot, new User(id_OfWinner, 0),MoneyType.GOLD);
                }
//                }

                new java.util.Timer().schedule( // Send lại thông tin ván mới sau 15s
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                SetInfoForNewGame_1(pokerRoom,true,false);
                                SetInfoForNewGame_1_9_SECOND(pokerRoom);
                                ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString(); // build lai thông tin ván mới + send
                                pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
                            }
                        },
                        POKER_COUNTDOWN_SHOWDOWN
                );
            }
            else {
                // set turn + availableAction cho các phase còn lại
                // set turn cho các phase còn lại
                int indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.DEALER_SMALLBLIND);
                if (indexOfPlayerSmallBlind == -1){
                    indexOfPlayerSmallBlind = pokerRoom.Poker_DealerTurn.indexOf(DEALERTURN.SMALLBLIND);
                }
                int IdOfSmallBlind = pokerRoom.Poker_JoinedGameID.get(indexOfPlayerSmallBlind);
                for (int i = 0; i < pokerRoom.Poker_Turn.size(); i++){
                    pokerRoom.Poker_Turn.set(i, false);
                }
                ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
                for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                    if (i == indexOfPlayerSmallBlind){ // nếu là smallblind thì add
                        Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                    }
                    else { // nếu ko phải thì chỉ add các player ko phải FOLDER + ko phải OUTOFMONEY
                        if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                            Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                        }
                    }
                } /* lấy id của ng giữ turn tiếp - check xem smallblind có FOLD hoặc OUTOFMONEY chưa */
                if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(IdOfSmallBlind) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(IdOfSmallBlind)){ // trường hợp SmallBlind chưa fold +  => ng kế thừa
                    pokerRoom.Poker_Turn.set(indexOfPlayerSmallBlind, true);
                }
                else { // trường hợp SmallBlind đã fold hoặc đã OUTOFMONEY => ng kế thừa + 1 trong Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY // trường hợp hết array => trở về 0
                    int IdOfSmallBlind_nextto = 0;
                    if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(IdOfSmallBlind) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
                        IdOfSmallBlind_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
                    }
                    else {
                        IdOfSmallBlind_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(IdOfSmallBlind) + 1);
                    }
                    int IndexOfSmallBlind_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfSmallBlind_nextto);
                    pokerRoom.Poker_Turn.set(IndexOfSmallBlind_nextto, true);
                }
                // set Poker_AvailableAction cho các phase còn lại
                int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
                for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                    if (k == indexOfTurn){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,true,false,false,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                    }
                }
            }
        }
        else{
            // ko thay đổi phase + community cards // trường họp 2 ng chơi - ko có trường hợp này xảy ra
            // set lại Turn
            /* remove tất cả FOLDER khỏi Poker_JoinedGameID - bằng cách tạo 1 array add các player ko phải FOLDER + Player đang giữ lượt vừa FOLD */
            pokerRoom.Poker_Turn.set(indexPlayerInJoinedGameId, false);
            ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
            for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
                if(i == indexPlayerInJoinedGameId){ // nếu ko phải thì chỉ add các player ko phải FOLDER
                    Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                }
                else {
                    if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                        Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                    }
                }
            }
            int IdOfPlayerCurrentTurn_nextto = 0; // lấy id của ng giữ turn tiếp
            if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
                IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
            }
            else {
                IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1));
            }
            int IndexOfPlayerCurrent_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfPlayerCurrentTurn_nextto);
            pokerRoom.Poker_Turn.set(IndexOfPlayerCurrent_nextto, true);
            // set lại AvailableAction
            int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
            for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
                if (k == indexOfTurn){
                    if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else {
                        if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.BET)) {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                        }
                        else {
                            pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,true,true,false,false));
                        }
                    }
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
                }
            }
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    public void BET (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request){
        // giữ nguyên:
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_phase
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        // - Poker_CommunityCards
        // - Poker_Pot
        int indexOfPlayer = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId()); // index của player trong joined gameid
        // trừ tiền tương ứng
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexOfPlayer);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        // set lại newMinBet
        pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount);
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set lại LastAction == bet
        pokerRoom.Poker_LastAction.set(indexOfPlayer, PLAYEROPTION.BET);
        // set lại Turn
        /* remove tất cả FOLDER khỏi Poker_JoinedGameID
        bằng cách tạo 1 array add các player ko phải FOLDER + Player đang giữ lượt vừa FOLD */
        pokerRoom.Poker_Turn.set(indexOfPlayer, false);
        ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
        for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
            if(i == indexOfPlayer){ // nếu ko phải thì chỉ add các player ko phải FOLDER
                Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
            }
            else {
                if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                    Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                }
            }
        }
        int IdOfPlayerCurrentTurn_nextto = 0; // lấy id của ng giữ turn tiếp
        if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
            IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
        }
        else {
            IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1));
        }
        int IndexOfPlayerCurrent_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfPlayerCurrentTurn_nextto);
        pokerRoom.Poker_Turn.set(IndexOfPlayerCurrent_nextto, true);
        // set lại AvailableAction
        int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
        for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
            if (k == indexOfTurn){
                if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.CHECK)){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                }
                else {
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,false,false,true,false));
                }
            }
            else {
                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
            }
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    public void RAISE (PokerRoom pokerRoom, ChannelContextInfo ctx, PokerProto.Poker_TurnOver request){
        // giữ nguyên:
        // - Poker_IdOfNextDealer
        // - Poker_JoinedGameID
        // - Poker_DealerTurn
        // - Poker_phase
        // - Poker_playerCardHolder
        // - Poker_AllCard52
        // - Poker_CommunityCards
        // - Poker_Pot
        int indexOfPlayer = pokerRoom.Poker_JoinedGameID.indexOf(ctx.getUserId()); // index của player trong joined gameid
        // trừ tiền tương ứng
        long currentMoney = (new User(ctx.getUserId(), 0)).getMoneyGold();
        long moneyGap = pokerRoom.Poker_NewMinBet + request.getCurrentMoneyPhase() - pokerRoom.Poker_BetAmount.get(indexOfPlayer);
        if (moneyGap >= currentMoney){ // chỉ trừ tiền tối đa bằng ng chơi
            PokerGameInstance.SubMoney(currentMoney,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
            pokerRoom.Poker_JoinedGameID_OUTOFMONEY.add(ctx.getUserId()); // add vào list riêng
        }
        else {
            PokerGameInstance.SubMoney(moneyGap,new User(pokerRoom.Poker_JoinedGameID.get(indexOfPlayer), 0),MoneyType.GOLD);
        }
        // set lại BetAmount - request.getCurrentMoneyPhase() == 0
        if (moneyGap >= currentMoney){ // số tiền bet = tổng số tiền còn lại ng chơi có
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, currentMoney + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        else {
            pokerRoom.Poker_BetAmount.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmount.get(indexOfPlayer));
            pokerRoom.Poker_BetAmountTotal.set(indexOfPlayer, moneyGap + pokerRoom.Poker_BetAmountTotal.get(indexOfPlayer));
        }
        // set lại newMinBet
        pokerRoom.Poker_NewMinBet = Collections.max(pokerRoom.Poker_BetAmount);
        // set lại CountDown
        pokerRoom.Poker_CountDown = POKER_COUNTDOWN_PLAYER;
        // set lại LastAction == raise
        pokerRoom.Poker_LastAction.set(indexOfPlayer, PLAYEROPTION.RAISE);
        // set lại Turn
        /* remove tất cả FOLDER khỏi Poker_JoinedGameID
         bằng cách tạo 1 array add các player ko phải FOLDER + Player đang giữ lượt vừa FOLD */
        pokerRoom.Poker_Turn.set(indexOfPlayer, false);
        ArrayList<Integer> Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY = new ArrayList<>();
        for (int i = 0; i < pokerRoom.Poker_JoinedGameID.size(); i++){
            if(i == indexOfPlayer){ // nếu ko phải thì chỉ add các player ko phải FOLDER
                Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
            }
            else {
                if (!pokerRoom.Poker_JoinedGameID_FOLDER.contains(pokerRoom.Poker_JoinedGameID.get(i)) && !pokerRoom.Poker_JoinedGameID_OUTOFMONEY.contains(pokerRoom.Poker_JoinedGameID.get(i))){
                    Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.add(pokerRoom.Poker_JoinedGameID.get(i));
                }
            }
        }
        int IdOfPlayerCurrentTurn_nextto = 0; // lấy id của ng giữ turn tiếp
        if ((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1) == Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.size()){
            IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get(0);
        }
        else {
            IdOfPlayerCurrentTurn_nextto = Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.get((Poker_JoinedGameID_AfterRemove_FOLDER_OUTOFMONEY.indexOf(ctx.getUserId()) + 1));
        }
        int IndexOfPlayerCurrent_nextto = pokerRoom.Poker_JoinedGameID.indexOf(IdOfPlayerCurrentTurn_nextto);
        pokerRoom.Poker_Turn.set(IndexOfPlayerCurrent_nextto, true);
        // set lại AvailableAction
        int indexOfTurn = pokerRoom.Poker_Turn.indexOf(true);
        for (int k = 0; k < pokerRoom.Poker_PlayerAvailableAction.size(); k++){
            if (k == indexOfTurn){
                if (pokerRoom.Poker_phase == PHASE.PRE_FLOP){
                    pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                }
                else {
                    if (pokerRoom.Poker_LastAction.contains(PLAYEROPTION.CHECK)){
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,false,false,true,true,false));
                    }
                    else {
                        pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(true,true,false,false,true,false));
                    }
                }
            }
            else {
                pokerRoom.Poker_PlayerAvailableAction.set(k, CreateAvailableAction(false,false,false,false,false,false));
            }
        }
        ByteString byteString = pokerRoom.parseProtoBuilder().build().toByteString();
        pokerRoom.SendToAll_OneRoom(byteString, PokerConstanst.GAME_INFO_1ROOM); // gửi lại toàn bộ tt game info cho player trong 1 room
    }
    @Descreption_Anno(Name = "CARD STRENGTH")
    public ArrayList<Long> CHECK_PLAYER_CARDSTRENGHT (ArrayList<ArrayList<PokerCard>> cardholder_All, ArrayList<PokerCard> community){
        ArrayList<Long> Final_CardStrength_All = new ArrayList<>();
        for (int i = 0; i < cardholder_All.size(); i++){
            // tạo array và add các giá trị sức mạnh của bài vào
            ArrayList<Long> list_CardStrength_1P = new ArrayList<>();
            list_CardStrength_1P.add(Check_CardStrength_STRAIGHTFLUSH(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_FOUROFAKIND(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_FULLHOUSE(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_FLUSH(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_STRAIGHT(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_THREEOFAKIND(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_TWOPAIR(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_PAIR(cardholder_All.get(i), community));
            list_CardStrength_1P.add(Check_CardStrength_HIGHCARD(cardholder_All.get(i), community));
            // lấy giá trị sức mạnh cao nhất gán cho vị trí của player đó
            Final_CardStrength_All.add(Collections.max(list_CardStrength_1P));
        }
        return Final_CardStrength_All;
    }

    public long Check_CardStrength_STRAIGHTFLUSH (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community){ // cardholder_1P - tương đương 1 ng
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        ArrayList<Long> type_int = Converter_Type_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght = new ArrayList<>();
        all_bonusStrenght.add(0L);
        // loại bỏ element i và j mỗi lượt thử => 7 - 5
        for (int i = 0; i < 6; i++){
            for (int j = i + 1; j < 7; j++){
                // Tạo list mới = list trên để remove i mỗi vòng lặp
                ArrayList<Long> number_int_ex = new ArrayList<>();
                number_int_ex.addAll(number_int);
                ArrayList<Long> type_int_ex = new ArrayList<>();
                type_int_ex.addAll(type_int);
                number_int_ex.remove(j);
                number_int_ex.remove(i);
                type_int_ex.remove(j);
                type_int_ex.remove(i);
                // lấy giá trị min
                long min_number = Collections.min(number_int_ex);
                long min_type = Collections.min(type_int_ex);
                // check số lần trùng lặp type trong 5 lá bài
                int duplicate_type = 0;
                for (int k = 0; k < 5; k++){
                    if (type_int_ex.get(k) - min_type == 0){
                        duplicate_type++;
                    }
                }
                if (min_number == 1){
                    if (number_int_ex.contains(min_number + 1) && number_int_ex.contains(min_number + 2) && number_int_ex.contains(min_number + 3) && number_int_ex.contains(min_number + 4)
                            && duplicate_type == 5){ // 5 con giống nước - 1-2-3-4-5 - nhỏ nhất
                        check = true;
                        all_bonusStrenght.add(1L);
                    }
                    else if (number_int_ex.contains(10) && number_int_ex.contains(11) && number_int_ex.contains(12) && number_int_ex.contains(13)
                            && duplicate_type == 5){ // 5 con giống nước - 10-J-Q-K-1 - lớn nhất
                        check = true;
                        all_bonusStrenght.add(14L);
                    }
                }
                else {
                    if (number_int_ex.contains(min_number + 1) && number_int_ex.contains(min_number + 2) && number_int_ex.contains(min_number + 3) && number_int_ex.contains(min_number + 4)
                            && duplicate_type == 5){ // 5 con giống nước - sảnh
                        check = true;
                        all_bonusStrenght.add(min_number);
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = Collections.max(all_bonusStrenght) * 100000000;
        if (check){
            return POKER_STRAIGHTFLUSH_9 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_FOUROFAKIND (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght_1 = new ArrayList<>(); // check độ mạnh của tứ quý
        all_bonusStrenght_1.add(0L);
        ArrayList<Long> all_bonusStrenght_2 = new ArrayList<>(); // check độ mạnh của lá bài lẻ cao nhất
        all_bonusStrenght_2.add(0L);
        // loại bỏ element i, j và k mỗi lượt thử => 7 - 4
        for (int i = 0; i < 5; i++){
            for (int j = i + 1; j < 6; j++){
                for (int k = j + 1; k < 7; k++){
                    // Tạo list mới = list trên để remove i mỗi vòng lặp
                    ArrayList<Long> number_int_ex = new ArrayList<>();
                    number_int_ex.addAll(number_int);
                    number_int_ex.remove(k);
                    number_int_ex.remove(j);
                    number_int_ex.remove(i);
                    // lấy giá trị min
                    long min_number = Collections.min(number_int_ex);
                    // check số lần trùng lặp number trong 4 lá bài
                    int duplicate_number = 0;
                    for (int l = 0; l < 4; l++){
                        if (number_int_ex.get(l) - min_number == 0){
                            duplicate_number++;
                        }
                    }
                    if (duplicate_number == 4){ // có tứ quý
                        check = true;
                        // lá ACES tính là cao nhất (1 => 14)
                        if (min_number == 1){
                            all_bonusStrenght_1.add(14L);
                        }
                        all_bonusStrenght_1.add(min_number);
                        // tạo array mới add 3 giá trị đã bị loại trừ (i,j,k) của number_int (để loại bỏ tứ quý)
                        // lấy giá trị cao nhất còn lại trong các lá bài đó
                        ArrayList<Long> get_DumbCardLeft = new ArrayList<>();
                        get_DumbCardLeft.add(number_int.get(k));
                        get_DumbCardLeft.add(number_int.get(j));
                        get_DumbCardLeft.add(number_int.get(i));
                        // lá ACES tính là cao nhất (1 => 14)
                        if (get_DumbCardLeft.contains(1L)){
                            all_bonusStrenght_2.add(14L);
                        }
                        else {
                            all_bonusStrenght_2.add(Collections.max(get_DumbCardLeft));
                        }
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = Collections.max(all_bonusStrenght_1) * 100000000 + Collections.max(all_bonusStrenght_2) * 1000000;
        if (check){
            return POKER_FOUROFAKIND_8 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_FULLHOUSE (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght_1 = new ArrayList<>();
        all_bonusStrenght_1.add(0L);
        ArrayList<Long> all_bonusStrenght_2 = new ArrayList<>();
        all_bonusStrenght_2.add(0L);
        // loại bỏ element i và j mỗi lượt thử => 7 - 5
        for (int i = 0; i < 6; i++) {
            for (int j = i + 1; j < 7; j++) {
                // Tạo list mới = list trên để remove i mỗi vòng lặp
                ArrayList<Long> number_int_ex = new ArrayList<>();
                number_int_ex.addAll(number_int);
                number_int_ex.remove(j);
                number_int_ex.remove(i);
                // lấy giá trị min
                long max_number = Collections.min(number_int_ex);
                long min_number = Collections.max(number_int_ex);
                // check số lần trùng lặp type trong 5 lá bài
                int duplicate_number_max = 0;
                for (int k = 0; k < 5; k++){
                    if (number_int_ex.get(k) - max_number == 0){
                        duplicate_number_max++;
                    }
                }
                int duplicate_number_min = 0;
                for (int k = 0; k < 5; k++){
                    if (number_int_ex.get(k) - min_number == 0){
                        duplicate_number_min++;
                    }
                }
                if (duplicate_number_max == 3 && duplicate_number_min == 2){
                    check = true;
                    // ACES chuyển thành 14
                    if (max_number == 1){
                        all_bonusStrenght_1.add(14L);
                    }
                    else {
                        all_bonusStrenght_1.add(max_number);
                    }
                    all_bonusStrenght_2.add(min_number);
                }
                else if (duplicate_number_max == 2 && duplicate_number_min == 3){
                    check = true;
                    all_bonusStrenght_1.add(min_number);
                    // ACES chuyển thành 14
                    if (max_number == 1){
                        all_bonusStrenght_2.add(14L);
                    }
                    else {
                        all_bonusStrenght_2.add(max_number);
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = 0;
        for (int i = 0; i < all_bonusStrenght_1.size(); i++){
            if ((all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i) * 1000000) > bonusStrenght){
                bonusStrenght = all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i) * 1000000;
            }
        }
        if (check){
            return POKER_FULLHOUSE_7 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_FLUSH (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        ArrayList<Long> type_int = Converter_Type_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght = new ArrayList<>();
        all_bonusStrenght.add(0L);
        // loại bỏ element i và j mỗi lượt thử => 7 - 5
        for (int i = 0; i < 6; i++) {
            for (int j = i + 1; j < 7; j++) {
                // Tạo list mới = list trên để remove i mỗi vòng lặp
                ArrayList<Long> number_int_ex = new ArrayList<>();
                number_int_ex.addAll(number_int);
                ArrayList<Long> type_int_ex = new ArrayList<>();
                type_int_ex.addAll(type_int);
                number_int_ex.remove(j);
                number_int_ex.remove(i);
                type_int_ex.remove(j);
                type_int_ex.remove(i);
                // lấy giá trị min
                long min_type = Collections.min(type_int_ex);
                // check số lần trùng lặp type trong 5 lá bài
                int duplicate_type = 0;
                for (int k = 0; k < 5; k++){
                    if (type_int_ex.get(k) - min_type == 0){
                        duplicate_type++;
                    }
                }
                if (duplicate_type == 5){
                    check = true;
                    for (int k = 0; k < number_int_ex.size(); k++){
                        if (number_int_ex.get(k) == 1){
                            number_int_ex.set(k, 14L);
                        }
                    }
                    Collections.sort(number_int_ex);
                    all_bonusStrenght.add(number_int_ex.get(4) * 100000000L + number_int_ex.get(3) * 1000000L + number_int_ex.get(2) * 10000L + number_int_ex.get(1) * 100L + number_int_ex.get(0));
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = Collections.max(all_bonusStrenght);
        if (check){
            return POKER_FLUSH_6 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_STRAIGHT (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        ArrayList<Long> type_int = Converter_Type_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght = new ArrayList<>();
        all_bonusStrenght.add(0L);
        // loại bỏ element i và j mỗi lượt thử => 7 - 5
        for (int i = 0; i < 6; i++) {
            for (int j = i + 1; j < 7; j++) {
                // Tạo list mới = list trên để remove i,j mỗi vòng lặp
                ArrayList<Long> number_int_ex = new ArrayList<>();
                number_int_ex.addAll(number_int);
                ArrayList<Long> type_int_ex = new ArrayList<>();
                type_int_ex.addAll(type_int);
                number_int_ex.remove(j);
                number_int_ex.remove(i);
                type_int_ex.remove(j);
                type_int_ex.remove(i);
                // lấy giá trị min
                long min_number = Collections.min(number_int_ex);
                long min_type = Collections.min(type_int_ex);
                if (min_number == 1){
                    if (number_int_ex.contains(min_number + 1) && number_int_ex.contains(min_number + 2) && number_int_ex.contains(min_number + 3) && number_int_ex.contains(min_number + 4)){ // 5 con giống nước - 1-2-3-4-5 - nhỏ nhất
                        check = true;
                        all_bonusStrenght.add(1L);
                    }
                    else if (number_int_ex.contains(10) && number_int_ex.contains(11) && number_int_ex.contains(12) && number_int_ex.contains(13)){ // 5 con giống nước - 10-J-Q-K-1 - lớn nhất
                        check = true;
                        all_bonusStrenght.add(14L);
                    }
                }
                else {
                    if (number_int_ex.contains(min_number + 1) && number_int_ex.contains(min_number + 2) && number_int_ex.contains(min_number + 3) && number_int_ex.contains(min_number + 4)){ // 5 con giống nước - sảnh
                        check = true;
                        all_bonusStrenght.add(min_number);
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = Collections.max(all_bonusStrenght) * 100000000;
        if (check){
            return POKER_STRAIGHT_5 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_THREEOFAKIND (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght_1 = new ArrayList<>(); // check độ mạnh của 3 con
        all_bonusStrenght_1.add(0L);
        ArrayList<Long> all_bonusStrenght_2 = new ArrayList<>(); // check độ mạnh của 2 con lẻ lớn nhất
        all_bonusStrenght_2.add(0L);
        // loại bỏ element i, j và k mỗi lượt thử => 7 - 4
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 5; j++) {
                for (int k = j + 1; k < 6; k++) {
                    for (int l = k + 1; l < 7; l++){
                        ArrayList<Long> number_int_ex = new ArrayList<>();
                        number_int_ex.addAll(number_int);
                        number_int_ex.remove(l);
                        number_int_ex.remove(k);
                        number_int_ex.remove(j);
                        number_int_ex.remove(i);
                        // lấy giá trị min
                        long min_number = Collections.min(number_int_ex);
                        // check số lần trùng lặp number trong 3 lá bài
                        int duplicate_number = 0;
                        for (int m = 0; m < 3; m++){
                            if (number_int_ex.get(m) - min_number == 0){
                                duplicate_number++;
                            }
                        }
                        if (duplicate_number == 3){
                            check = true;
                            // lá ACES tính là cao nhất (1 => 14)
                            if (min_number == 1){
                                all_bonusStrenght_1.add(14L);
                            }
                            else {
                                all_bonusStrenght_1.add(min_number);
                            }
                            // tạo array mới add 4 giá trị đã bị loại trừ (i,j,k,l) của number_int (để loại bỏ 3 con)
                            // lấy giá trị cao nhất + cao nhì còn lại trong các lá bài đó
                            ArrayList<Long> get_DumbCardLeft = new ArrayList<>();
                            get_DumbCardLeft.add(number_int.get(l));
                            get_DumbCardLeft.add(number_int.get(k));
                            get_DumbCardLeft.add(number_int.get(j));
                            get_DumbCardLeft.add(number_int.get(i));
                            // lá ACES tính là cao nhất (1 => 14)
                            for (int m = 0; m < get_DumbCardLeft.size(); m++){
                                if (get_DumbCardLeft.get(m) == 1){
                                    get_DumbCardLeft.set(m, 14L);
                                }
                            }
                            Collections.sort(get_DumbCardLeft);
                            all_bonusStrenght_2.add(get_DumbCardLeft.get(3) * 1000000 + get_DumbCardLeft.get(2) * 10000);
                        }
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = 0;
        for (int i = 0; i < all_bonusStrenght_1.size(); i++){
            if ((all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i)) > bonusStrenght){
                bonusStrenght = all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i);
            }
        }
        if (check){
            return POKER_THREEOFAKIND_4 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_TWOPAIR (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght_1 = new ArrayList<>(); // check độ mạnh của đôi mạnh
        all_bonusStrenght_1.add(0L);
        ArrayList<Long> all_bonusStrenght_2 = new ArrayList<>(); // check độ mạnh của đôi yếu hơn
        all_bonusStrenght_2.add(0L);
        ArrayList<Long> all_bonusStrenght_3 = new ArrayList<>(); // check độ mạnh của lá bài lẻ cao nhất
        all_bonusStrenght_3.add(0L);
        // loại bỏ element i, j và k mỗi lượt thử => 7 - 4
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 6; j++) {
                for (int k = j + 1; k < 7; k++) {
                    // Tạo list mới = list trên để remove i mỗi vòng lặp
                    ArrayList<Long> number_int_ex = new ArrayList<>();
                    number_int_ex.addAll(number_int);
                    number_int_ex.remove(k);
                    number_int_ex.remove(j);
                    number_int_ex.remove(i);
                    // chuyển ACES thành 14
                    for (int l = 0; l < number_int_ex.size(); l++){
                        if (number_int_ex.get(l) == 1){
                            number_int_ex.set(l, 14L);
                        }
                    }
                    // lấy giá trị min + max
                    long min_number = Collections.min(number_int_ex);
                    long max_number = Collections.max(number_int_ex);
                    // check số lần trùng lặp number trong 4 lá bài
                    int duplicate_number_1 = 0;
                    for (int l = 0; l < 4; l++){
                        if (number_int_ex.get(l) - max_number == 0){
                            duplicate_number_1++;
                        }
                    }
                    // check số lần trùng lặp number trong 4 lá bài
                    int duplicate_number_2 = 0;
                    for (int l = 0; l < 4; l++){
                        if (number_int_ex.get(l) - min_number == 0){
                            duplicate_number_2++;
                        }
                    }
                    if (duplicate_number_1 == 2 && duplicate_number_2 == 2){ // có 2 đôi
                        check = true;
                        all_bonusStrenght_1.add(max_number);
                        all_bonusStrenght_2.add(min_number);
                        // tạo array mới add 3 giá trị đã bị loại trừ (i,j,k) của number_int (để loại bỏ 2 đôi)
                        // lấy giá trị cao nhất còn lại trong các lá bài đó
                        ArrayList<Long> get_DumbCardLeft = new ArrayList<>();
                        get_DumbCardLeft.add(number_int.get(k));
                        get_DumbCardLeft.add(number_int.get(j));
                        get_DumbCardLeft.add(number_int.get(i));
                        // lá ACES tính là cao nhất (1 => 14)
                        if (get_DumbCardLeft.contains(1L)){
                            all_bonusStrenght_3.add(14L);
                        }
                        else {
                            all_bonusStrenght_3.add(Collections.max(get_DumbCardLeft));
                        }
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = 0;
        for (int i = 0; i < all_bonusStrenght_1.size(); i++){
            if ((all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i) * 1000000 + all_bonusStrenght_3.get(i) * 10000) > bonusStrenght){
                bonusStrenght = all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i) * 1000000 + all_bonusStrenght_3.get(i) * 10000;
            }
        }
        if (check){
            return POKER_TWOPAIR_3 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_PAIR (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        boolean check = false;
        ArrayList<Long> all_bonusStrenght_1 = new ArrayList<>(); // check độ mạnh của tứ quý
        all_bonusStrenght_1.add(0L);
        ArrayList<Long> all_bonusStrenght_2 = new ArrayList<>(); // check độ mạnh của 3 lá bài lẻ cao nhất
        all_bonusStrenght_2.add(0L);
        // loại bỏ element i, j và k, l, m mỗi lượt thử => 7 - 2s
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                for (int k = j + 1; k < 5; k++) {
                    for (int l = k + 1; l < 6; l++) {
                        for (int m = l + 1; m < 7; m++) {
                            // Tạo list mới = list trên để remove i mỗi vòng lặp
                            ArrayList<Long> number_int_ex = new ArrayList<>();
                            number_int_ex.addAll(number_int);
                            number_int_ex.remove(m);
                            number_int_ex.remove(l);
                            number_int_ex.remove(k);
                            number_int_ex.remove(j);
                            number_int_ex.remove(i);
                            // chuyển ACES thành 14
                            for (int n = 0; n < number_int_ex.size(); n++){
                                if (number_int_ex.get(n) == 1){
                                    number_int_ex.set(n, 14L);
                                }
                            }
                            // lấy giá trị min
                            long min_number = Collections.min(number_int_ex);
                            // check số lần trùng lặp number trong 2 lá bài
                            int duplicate_number = 0;
                            for (int o = 0; o < 2; o++){
                                if (number_int_ex.get(o) - min_number == 0){
                                    duplicate_number++;
                                }
                            }
                            if (duplicate_number == 2){
                                check = true;
                                all_bonusStrenght_1.add(min_number);
                            }
                            // tạo array mới add 5 giá trị đã bị loại trừ (i,j,k,l,m) của number_int (để loại bỏ 1 đôi)
                            // lấy giá trị cao nhất còn lại trong các lá bài đó
                            ArrayList<Long> get_DumbCardLeft = new ArrayList<>();
                            get_DumbCardLeft.add(number_int.get(m));
                            get_DumbCardLeft.add(number_int.get(l));
                            get_DumbCardLeft.add(number_int.get(k));
                            get_DumbCardLeft.add(number_int.get(j));
                            get_DumbCardLeft.add(number_int.get(i));
                            Collections.sort(get_DumbCardLeft);
                            all_bonusStrenght_2.add(get_DumbCardLeft.get(4) * 1000000 + get_DumbCardLeft.get(3) * 10000 + get_DumbCardLeft.get(2) * 100);
                        }
                    }
                }
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = 0;
        for (int i = 0; i < all_bonusStrenght_1.size(); i++){
            if ((all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i)) > bonusStrenght){
                bonusStrenght = all_bonusStrenght_1.get(i) * 100000000 + all_bonusStrenght_2.get(i);
            }
        }
        if (check){
            return POKER_PAIR_2 + bonusStrenght;
        }
        else {
            return POKER_NOTHING;
        }
    }
    public long Check_CardStrength_HIGHCARD (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community) {
        ArrayList<Long> number_int = Converter_Number_IntegerArrayList(cardholder_1P, community); // size = 7
        // biến check xem có đạt đủ điều kiện 1 bộ này 0
        ArrayList<Long> all_bonusStrenght_1 = new ArrayList<>(); // check độ mạnh của 5 lá bài lẻ
        all_bonusStrenght_1.add(0L);
        for (int i = 0; i < 6; i++) {
            for (int j = i + 1; j < 7; j++) {
                // Tạo list mới = list trên để remove i, j mỗi vòng lặp
                ArrayList<Long> number_int_ex = new ArrayList<>();
                number_int_ex.addAll(number_int);
                number_int_ex.remove(j);
                number_int_ex.remove(i);
                // chuyển ACES thành 14
                for (int k = 0; k < number_int_ex.size(); k++){
                    if (number_int_ex.get(k) == 1){
                        number_int_ex.set(k, 14L);
                    }
                }
                Collections.sort(number_int_ex);
                all_bonusStrenght_1.add(number_int_ex.get(4) * 100000000 + number_int_ex.get(3) * 1000000 + number_int_ex.get(2) * 10000 + number_int_ex.get(1) * 100 + number_int_ex.get(0));
            }
        }
        // biến độ mạnh tăng thêm của bộ này
        long bonusStrenght = Collections.max(all_bonusStrenght_1);
        return POKER_HIGHCARD_1 + bonusStrenght;
    }
    public static void main(String[] args) {
//        PokerCommand pokerCommand = PokerCommand.getInstance();
//
//        ArrayList<ArrayList<PokerCard>> All_cardholder = new ArrayList<>();
//        ArrayList<PokerCard> cardholder = new ArrayList<>();
//        cardholder.add(new PokerCard (CARDNUMBER.ACES, CARDTYPE.SPADE));
//        cardholder.add(new PokerCard (CARDNUMBER.ACES, CARDTYPE.HEART));
//        All_cardholder.add(cardholder);
//
//        ArrayList<PokerCard> community = new ArrayList<>();
//        community.add(new PokerCard (CARDNUMBER.TEN, CARDTYPE.HEART));
//        community.add(new PokerCard (CARDNUMBER.FOUR, CARDTYPE.HEART));
//        community.add(new PokerCard (CARDNUMBER.FIVE, CARDTYPE.SPADE));
//        community.add(new PokerCard (CARDNUMBER.SIX, CARDTYPE.SPADE));
//        community.add(new PokerCard (CARDNUMBER.SEVEN, CARDTYPE.HEART));
//
//        pokerCommand.CHECK_PLAYER_CARDSTRENGHT(All_cardholder, community);
    }
    public ArrayList<Long> Converter_Number_IntegerArrayList (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community){
        // bài của 1P + community cards
        ArrayList<String> number_string = new ArrayList<>();
        for (int i = 0; i < community.size(); i++){
            number_string.add(community.get(i).cardnumber);
        }
        for (int i = 0; i < cardholder_1P.size(); i++) { // cardholder_1P.size() == 2
            number_string.add(cardholder_1P.get(i).cardnumber);
        }
        ArrayList<Long> number_int = new ArrayList<>();
        for (int i = 0; i < number_string.size(); i++){
            if (number_string.get(i) == CARDNUMBER.ACES){
                number_int.add(1L);
            }
            else if (number_string.get(i) == CARDNUMBER.TWO){
                number_int.add(2L);
            }
            else if (number_string.get(i) == CARDNUMBER.THREE){
                number_int.add(3L);
            }
            else if (number_string.get(i) == CARDNUMBER.FOUR){
                number_int.add(4L);
            }
            else if (number_string.get(i) == CARDNUMBER.FIVE){
                number_int.add(5L);
            }
            else if (number_string.get(i) == CARDNUMBER.SIX){
                number_int.add(6L);
            }
            else if (number_string.get(i) == CARDNUMBER.SEVEN){
                number_int.add(7L);
            }
            else if (number_string.get(i) == CARDNUMBER.EIGHT){
                number_int.add(8L);
            }
            else if (number_string.get(i) == CARDNUMBER.NINE){
                number_int.add(9L);
            }
            else if (number_string.get(i) == CARDNUMBER.TEN){
                number_int.add(10L);
            }
            else if (number_string.get(i) == CARDNUMBER.JACK){
                number_int.add(11L);
            }
            else if (number_string.get(i) == CARDNUMBER.QUEEN){
                number_int.add(12L);
            }
            else if (number_string.get(i) == CARDNUMBER.KING){
                number_int.add(13L);
            }
        }
        return number_int;
    }
    public ArrayList<Long> Converter_Type_IntegerArrayList (ArrayList<PokerCard> cardholder_1P, ArrayList<PokerCard> community){
        // bài của 1P + community cards
        ArrayList<String> type_string = new ArrayList<>();
        for (int i = 0; i < community.size(); i++){
            type_string.add(community.get(i).cardtype);
        }
        for (int i = 0; i < cardholder_1P.size(); i++) { // cardholder_1P.size() == 2
            type_string.add(cardholder_1P.get(i).cardtype);
        }
        ArrayList<Long> type_int = new ArrayList<>();
        for (int i = 0; i < type_string.size(); i++) {
            if (type_string.get(i) == CARDTYPE.HEART) {
                type_int.add(4L);
            }
            else if (type_string.get(i) == CARDTYPE.DIAMOND) {
                type_int.add(3L);
            }
            else if (type_string.get(i) == CARDTYPE.CLUB) {
                type_int.add(2L);
            }
            else if (type_string.get(i) == CARDTYPE.SPADE) {
                type_int.add(1L);
            }
        }
        return type_int;
    }

}
