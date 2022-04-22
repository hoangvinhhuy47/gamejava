package games.tx.room;

import com.google.protobuf.Message;
import games.core.database.document.DRoomInfo;
import games.core.proto.ProtoSerializer;
import message.JackpotGameProto;

public class RoomStake implements ProtoSerializer<JackpotGameProto.RoomStakeInfo> {
    private DRoomInfo roomInfo;
    private int minStep;
    private int maxStep;

    /**
     * Số tiền cược mặc định khi vào phòng. (Nếu không được gán thì lấy = min)
     */
    private int defaultStep;

    /**
     * Số tiền cược mỗi lần tăng hoặc giảm mức cược
     */
    private long stepMoney;

    public RoomStake(DRoomInfo roomInfo){
        this.roomInfo = roomInfo;
        this.minStep = roomInfo.getSlotMinStep();
        this.maxStep = roomInfo.getSlotMaxStep();
        this.stepMoney = roomInfo.getSlotStepMoney();
        this.defaultStep = minStep;
    }

    public long getMoneyByStep(int step){
        return step * stepMoney;
    }

    public int getDefaultStep() {
        return defaultStep;
    }

    public long getDefaultMoney(){
        return defaultStep * stepMoney;
    }

    public int getMinStep() {
        return minStep;
    }

    public int getMaxStep() {
        return maxStep;
    }

    public long getStepMoney() {
        return stepMoney;
    }

    @Override
    public Message.Builder parseProtoBuilder() {
        JackpotGameProto.RoomStakeInfo.Builder builder = JackpotGameProto.RoomStakeInfo.newBuilder();
        builder.setDefaultStep(getDefaultStep())
                .setMaxStep(getMaxStep())
                .setMinStep(getMinStep())
                .setStepMoney(getStepMoney());
        return builder;
    }
}
