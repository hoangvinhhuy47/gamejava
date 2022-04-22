package games.slot.room;

import com.google.protobuf.Message;
import games.core.application.channel.TaskScheduler;
import games.core.database.document.DRoomInfo;
import games.core.database.model.MFundHistory;
import games.core.datatype.HashPlayer;
import games.core.exception.game.CannotRemovePlayerInRoom;
import games.core.exception.game.NotFoundPlayerInRoom;
import games.core.proto.ProtoSerializer;
import games.slot.*;
import games.slot.data.ResultBoard;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.util.LogFactory;
import message.JackpotGameProto;
import message.SlotGameProto;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

public class SlotRoom implements ProtoSerializer<SlotGameProto.SlotRoomInfo> {
    protected int id;
    /**
     * Room config lưu trong collection roomInfo
     */
    protected DRoomInfo roomInfo;

    protected Logger logger;

    /**
     * Hash chứa player ở trong room này
     */
    protected HashPlayer<SlotPlayer> players = new HashPlayer<>();

    /**
     * Hũ của phòng
     */
    protected RoomPot roomPot;

    /**
     * Mức cược của phòng
     */
    protected RoomStake roomStake;

    /**
     * Quỹ của room lấy từ roomInfo
     */
    protected RoomFund roomFund;

    private JackpotGameProto.RoomPotResponse.Builder defaultRoomPotResponseBuilder;

    public SlotRoom(int id) {
        try {
            roomInfo = new DRoomInfo(id);
        } catch (NotFoundDocumentInDatabase notFoundDocumentInDatabase) {
            getLogger().info("Room[" + id + "] not have config in database!");
        }
        this.id = id;
        this.roomPot = new RoomPot(roomInfo);
        this.roomStake = new RoomStake(roomInfo);
        this.roomFund = new RoomFund(roomInfo);
        defaultRoomPotResponseBuilder = JackpotGameProto.RoomPotResponse.newBuilder();
        defaultRoomPotResponseBuilder.setRoomId(id);
        TaskScheduler.getInstance().scheduleAtFixedRate(new FiveSecUpdateRunnable(), 2000, 2000, TimeUnit.MILLISECONDS);
    }

    class FiveSecUpdateRunnable implements Runnable {
        @Override
        public void run() {
            defaultRoomPotResponseBuilder.setPot(getRoomPot().getAmount());
            sendToAllPlayer(defaultRoomPotResponseBuilder.build());
        }
    }

    private void sendToAllPlayer(Message message) {
        if (id >= SlotConst.SLOT_THREE_KINGDOMS_NORMAL_ROOM_ID) {
            getPlayers().values().forEach(slotPlayer -> SlotThreeKingdomsCommand.getInstance().pushToClient(
                    slotPlayer.getCtxInfo(),
                    SlotActionConst.ROOM_POT_INFO,
                    message
            ));
        } else {
            getPlayers().values().forEach(slotPlayer -> SlotOceanCommand.getInstance().pushToClient(
                    slotPlayer.getCtxInfo(),
                    SlotActionConst.ROOM_POT_INFO,
                    message
            ));
        }

    }

    public void addPlayer(SlotPlayer player) {
        players.remove(player.getId());
        players.putIfAbsent(player.getId(), player);
        onAddPlayer(player);
    }

    public void removePlayer(SlotPlayer player) throws NotFoundPlayerInRoom, CannotRemovePlayerInRoom {
        if (!players.containsKey(player.getId())) {
            throw new NotFoundPlayerInRoom(player);
        }
        if (!players.remove(player.getId(), player)) {
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

    public HashPlayer<SlotPlayer> getPlayers() {
        return players;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogFactory.getLogger(getClass().getSimpleName());
        }
        return logger;
    }

    protected void onAddPlayer(SlotPlayer player) {
    }

    protected void onRemovePlayer(SlotPlayer player) {
    }

    public RoomPot getRoomPot() {
        return roomPot;
    }

    public RoomStake getRoomStake() {
        return roomStake;
    }

    public RoomFund getRoomFund() {
        return roomFund;
    }

    @Override
    public SlotGameProto.SlotRoomInfo.Builder parseProtoBuilder()
    {
        SlotGameProto.SlotRoomInfo.Builder builder = SlotGameProto.SlotRoomInfo.newBuilder();
        builder.setRoomId(this.id);
        builder.setRoomStakeInfo(getRoomStake().getProtoMessage());
        builder.setPotAmount(this.getRoomPot().getAmount());
        return builder;
    }

    public String getRoomName() {
        if (getId() == SlotConst.SLOT_OCEAN_NORMAL_ROOM_ID ||
                getId() == SlotConst.SLOT_THREE_KINGDOMS_NORMAL_ROOM_ID) {
            return "NORMAL";
        } else if (getId() == SlotConst.SLOT_OCEAN_RICH_ROOM_ID ||
                getId() == SlotConst.SLOT_THREE_KINGDOMS_RICH_ROOM_ID) {
            return "RICH";
        } else if (getId() == SlotConst.SLOT_OCEAN_VIP_ROOM_ID ||
                getId() == SlotConst.SLOT_THREE_KINGDOMS_VIP_ROOM_ID) {
            return "VIP";
        } else {
            return "UNKNOWN WRONG ID";
        }
    }

    public Document getRoomInfoDocument() {
        Document room = new Document();
        room.put(DRoomInfo.FIELD_ROOM_INFO_POT, this.getRoomPot().getAmount());
        room.put(DRoomInfo.FIELD_ROOM_INFO_NAME, this.getRoomName());
        room.put(DRoomInfo.FIELD_ROOM_INFO_FUND, this.getRoomFund().getAmount());
        room.put(DRoomInfo.FIELD_ROOM_INFO_FUND_DEALER, this.getRoomFund().getDealerAmount());
        room.put(DRoomInfo.FIELD_ROOM_ID, this.getId());
        return room;
    }

    public void setSlotWinRate(double winRate) {
        this.getRoomInfo().setSlotWinRate(winRate);
        this.getRoomInfo().saveDB();
    }

    /**
     * Số tiền tối đa mà player được phép ăn
     */
    public long getLimitFundAmount() {
        return this.getRoomFund().getAmount();// - this.getRoomInfo().getMinJackpot();
    }

    public static void main(String[] args) {
        SlotRoom room = new SlotRoom(20001);
        System.out.println(room.getRoomPot().getAmount());
        room.getRoomPot().plus(500000);
        room.getRoomPot().jackpot();
        System.out.println(room.getRoomPot().getAmount());
    }
}
