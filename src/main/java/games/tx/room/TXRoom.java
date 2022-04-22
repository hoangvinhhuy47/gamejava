package games.tx.room;

import com.google.protobuf.Message;
import games.core.application.channel.TaskScheduler;
import games.core.database.document.DRoomInfo;
import games.core.datatype.HashPlayer;
import games.core.exception.game.CannotRemovePlayerInRoom;
import games.core.exception.game.NotFoundPlayerInRoom;
import games.core.proto.ProtoSerializer;
import games.tx.*;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.util.LogFactory;
import message.JackpotGameProto;
import message.TaiXiuGameProto;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

public class TXRoom implements ProtoSerializer<TaiXiuGameProto.TXRoomInfo> {
    protected int id;
    /**
     * Room config lưu trong collection roomInfo
     */
    protected DRoomInfo roomInfo;

    protected Logger logger;

    /**
     * Hash chứa player ở trong room này
     */
    protected HashPlayer<TXPlayer> players = new HashPlayer<>();

    /**
     * Quỹ của room lấy từ roomInfo
     */
    protected RoomFund roomFund;

    private JackpotGameProto.RoomPotResponse.Builder defaultRoomPotResponseBuilder;

    public TXRoom(int id){
        try {
            roomInfo = new DRoomInfo(id);
        } catch (NotFoundDocumentInDatabase notFoundDocumentInDatabase) {
            getLogger().info("Room["+id+"] not have config in database!");
        }
        this.id = id;
        this.roomFund = new RoomFund(roomInfo);
        defaultRoomPotResponseBuilder = JackpotGameProto.RoomPotResponse.newBuilder();
        defaultRoomPotResponseBuilder.setRoomId(id);
        TaskScheduler.getInstance().scheduleAtFixedRate(new FiveSecUpdateRunnable(), 2000, 2000, TimeUnit.MILLISECONDS);
    }
    static TXRoom instance;
    public static TXRoom getInstance(){
        return instance;
    }

    class FiveSecUpdateRunnable implements Runnable {
        @Override
        public void run() {
            sendToAllPlayer(defaultRoomPotResponseBuilder.build());
        }
    }

    private void sendToAllPlayer(Message message)
    {
        getPlayers().values().forEach(txPlayer -> TXCommand.getInstance().pushToClient(
                txPlayer.getCtxInfo(),
                TXActionConst.ROOM_POT_INFO,
                message
        ));
    }

    public void addPlayer(TXPlayer player) {
        players.remove(player.getId());
        players.putIfAbsent(player.getId(), player);
        onAddPlayer(player);
    }

    public void removePlayer(TXPlayer player) throws NotFoundPlayerInRoom, CannotRemovePlayerInRoom {
        if (!players.containsKey(player.getId())){
            throw new NotFoundPlayerInRoom(player);
        }
        if (!players.remove(player.getId(), player)){
            throw new CannotRemovePlayerInRoom(player);
        }
        onRemovePlayer(player);
    }


    public int getId() {
        return id;
    }

    public DRoomInfo getRoomInfo() {
        return roomInfo;
    }

    public HashPlayer<TXPlayer> getPlayers() {
        return players;
    }

    public Logger getLogger() {
        if (logger == null){
            logger = LogFactory.getLogger(getClass().getSimpleName());
        }
        return logger;
    }

    protected void onAddPlayer(TXPlayer player){}
    protected void onRemovePlayer(TXPlayer player){}

    public RoomFund getRoomFund() {
        return roomFund;
    }

    @Override
    public TaiXiuGameProto.TXRoomInfo.Builder parseProtoBuilder() {
        TaiXiuGameProto.TXRoomInfo.Builder builder = TaiXiuGameProto.TXRoomInfo.newBuilder();

        builder.setRoomId(this.id);

        return builder;
    }

    public String getRoomName(){
            return "TAIXIU";
    }

    public Document getRoomInfoDocument(){
        Document room = new Document();
        room.put(DRoomInfo.FIELD_ROOM_INFO_NAME, this.getRoomName());
        room.put(DRoomInfo.FIELD_ROOM_INFO_FUND, this.getRoomFund().getAmount());
        room.put(DRoomInfo.FIELD_ROOM_INFO_FUND_DEALER, this.getRoomFund().getDealerAmount());
        room.put(DRoomInfo.FIELD_ROOM_ID, this.getId());
        return room;
    }

    public void setSlotWinRate(double winRate){
        this.getRoomInfo().setSlotWinRate(winRate);
        this.getRoomInfo().saveDB();
    }

    /**
     * Số tiền tối đa mà player được phép ăn
     */
    public long getLimitFundAmount(){
        return this.getRoomFund().getAmount();// - this.getRoomInfo().getMinJackpot();
    }

    public static void main(String[] args) {
        TXRoom room = new TXRoom(30001);
    }
}
