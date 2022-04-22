package games.Poker;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import games.Poker.ENUM.PokerConstanst;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.datatype.HashPlayer;
import games.core.exception.game.CannotRemovePlayerInRoom;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.exception.game.NotFoundPlayerInRoom;
import games.core.exception.game.RoomNotExist;
import games.core.proto.ProtoSerializer;
import games.core.user.User;
import libs.util.data.HashByInt;
import message.PokerProto;
import message.XProtos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static games.core.constant.ServerCommandConstant.POKER_COMMAND;

public abstract class PokerGame implements ProtoSerializer<PokerProto.Poker_Response_GameInfo>, Runnable{

@Descreption_Anno(Name = "CONSTRUCTOR")
    public PokerGame(){
        initRooms();
        moneyService = MoneyService.getInstance();
    }
@Descreption_Anno(Name = "ABSTRACT CLASS FOR DERIVED CLASS")
    protected abstract void initRooms();

@Descreption_Anno(Name = "SEND DATA TO PLAYERS - SOCKET")
    public void SendToAll(ByteString responseBeanByteString, int responseCode) {
        for (PokerPlayer player : players.values()) {
            if (player != null) { // Tránh trường hợp người ảo
                if(player.getCtxInfo() == null){
                }
                else {
                    SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
                }
            }
        }
    }
    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode) {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(POKER_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);
        ctx.writeAndFlush(responseMessage.build());
    }
@Descreption_Anno(Name = "FIELDS")
    MoneyService moneyService;
    protected PokerPlayerPool playerPool = new PokerPlayerPool();

    protected HashPlayer<PokerPlayer> players = new HashPlayer<>(); //
    public boolean isPlayerExist(User user) {
        return players.containsKey(user.getId());
    }
    private PokerPlayer createPlayer(User user) {
        PokerPlayer playerFromPool = playerPool.shiftSync();
        playerFromPool.setUser(user);
        return playerFromPool;
    }
    public void AddPlayer(User user) { //
        if (isPlayerExist(user)) {
            this.players.remove(user.getId());
        }
        PokerPlayer newPlayer = this.createPlayer(user);
        this.players.putIfAbsent(newPlayer.getId(), newPlayer);
    }
    public void removePlayer(User user) throws NotFoundPlayerInGame {
        playerPool.putSync(getPlayer(user));
        this.players.remove(user.getId());
    }
    public PokerPlayer getPlayer(User user) throws NotFoundPlayerInGame {
        if (!players.containsKey(user.getId())) {
            throw new NotFoundPlayerInGame(user);
        }
        return this.players.get(user.getId());
    }

    protected HashByInt<PokerRoom> rooms = new HashByInt<>();
    public HashByInt<PokerRoom> getRooms() {
        return this.rooms;
    }
    public PokerRoom getRoomById(int id) throws RoomNotExist {
        PokerRoom room = this.rooms.get(id);
        if (room == null) {
            throw new RoomNotExist(id);
        }
        return room;
    }

    protected void addRoom(int id) {
        this.rooms.put(id, new PokerRoom(id));
    }
    @Descreption_Anno(Name = "ROOM & PLAYER")
    public void joinRoom(User user, PokerRoom room) {
        PokerPlayer player = getPlayer(user);
        room.AddPlayer(player);
        player.setRoom(room);
    }
    public void leaveRoom(User user) throws NotFoundPlayerInGame, NotFoundPlayerInRoom, CannotRemovePlayerInRoom {
        PokerPlayer player = getPlayer(user);
        PokerRoom room = player.getRoom();
        room.removePlayer(player);
    }
    @Descreption_Anno(Name = "BUILD GAME_INFO PROTO RESPONSE")
    @Override
    public Message.Builder parseProtoBuilder() { // Poker_Response_GameInfo
        PokerProto.Poker_Response_GameInfo.Builder response = PokerProto.Poker_Response_GameInfo.newBuilder();
        getRooms().values().forEach(pokerRoom -> response.addRoom(pokerRoom.parseProtoBuilder()));
        return response;
    }
    public ByteString parseMessageToByteString() { // Nội dung Proto GAME_INFO gửi cho client // Poker_Response_GameInfo
        PokerProto.Poker_Response_GameInfo.Builder response = PokerProto.Poker_Response_GameInfo.newBuilder();
        getRooms().values().forEach(pokerRoom -> response.addRoom(pokerRoom.parseProtoBuilder()));
        return response.build().toByteString();
    }
    public PokerProto.Poker_Response_AllCountDown_10Room.Builder BuildInfoForAllRoom () {
        PokerProto.Poker_Response_AllCountDown_10Room.Builder respose_allroom = PokerProto.Poker_Response_AllCountDown_10Room.newBuilder();
        getRooms().values().forEach(
                pokerRoomObj -> {respose_allroom.addRoom(pokerRoomObj.BuildInfoForOneRoom());
        });
        return respose_allroom;
    }
    public void Start() { // MAIN CONTINUOUSTY SENDING TO CLIENTS
        ArrayList<PokerRoom> Rooms = new ArrayList<>(getRooms().values());

        Timer timer = new Timer(); // MAIN TIMER
        TimerTask timerTask_1 = new TimerTask () {
            @Override
            public void run()
            {
//                // Tạo delta time cho java
//                Long now = System.currentTimeMillis();
//                double deltaTime = (now - lastTimeUpdate) / 1000.0f;
//                lastTimeUpdate = now;
                for (int i = 0; i < Rooms.size(); i++){
                    if (Rooms.get(i).getPoker_CountDown() > -1000){
                        Rooms.get(i).setPoker_CountDown(Rooms.get(i).getPoker_CountDown() - 100);
//                        System.out.println(Rooms.get(i).getPoker_CountDown());
                    }
                }
                Update_OneSec();
            }
        };
        timer.schedule(timerTask_1, 0, 100); // gọi các timertask mỗi 100 mili giây 1 lần
    }
    public void Update_OneSec(){
        SendToAll(BuildInfoForAllRoom().build().toByteString(), PokerConstanst.GAME_PLAYERCOUNTDOWN); // chua sử dụng
        PokerRoom pokerRoom = getRoomById(50001);
    }
    @Descreption_Anno(Name = "STUFF METHODS + ADD/SUB MONEY")
    public void AddMoney(long winnings, User user, MoneyType moneyType) throws IOException
    {
        Money money = new Money(winnings, moneyType);

        user.SetMoney(winnings);

        MoneyService.getInstance().addMoneyToSave(money, user, true, "Poker");
    }
    public void SubMoney(long betAmount, User user, MoneyType moneyType) {
        Money money = new Money(betAmount, moneyType);
        MoneyService.getInstance().subMoneyAndSaveMongo(money, user, true, true);
    }
}
