package games.shootingfish.datatype;

import games.core.common.money.MoneyType;
import games.shootingfish.data.DataRow;

public class RoomType extends DataRow{

    public static final int TRIAL = 0;
    public static final int NORMAL = 1;
    public static final int VIP = 2;
    public static final int SUPER_VIP = 3;
    public static final int NONE = -1;

    public boolean checkPercent1 = false;
    public boolean checkPercent2 = false;
    public boolean checkPercent3 = false;

    private int roomId;
    private String roomName;
    private long baseFund;
    private int baseGunMoney;
    private float rateKillFish;
    private float backupRateKillFish;
    private boolean isRefund;
    private long maxGoldInstantJoinTable;
    private MoneyType moneyType;
    public RoomType(String[] strings){
        load(strings);
    }
    RoomType(int typeId, String roomName, long baseFund, int baseGunMoney, float rateKillFish){
        this.isRefund = false;
        this.roomId = typeId;
        this.roomName = roomName;
        this.baseFund = baseFund;
        this.baseGunMoney = baseGunMoney;
        this.rateKillFish = rateKillFish;
        this.backupRateKillFish = rateKillFish;

        switch (this.roomId) {
            case RoomType.TRIAL:
                this.moneyType = MoneyType.TRIAL;
                break;
            default:
                this.moneyType = MoneyType.GOLD;
        }
    }

    public boolean getCheckPercent(int roomID)
    {
        switch (roomID){
            case 1: return this.checkPercent1;
            case 2: return this.checkPercent2;
            case 3: return this.checkPercent3;
        }

        return false;
    }

    public void setCheckPercent(int roomID, boolean value)
    {
        switch (roomID){
            case 1:  this.checkPercent1 = value;
            case 2:  this.checkPercent2 = value;
            case 3:  this.checkPercent3 = value;
        }
    }

    public MoneyType getMoneyType() {
        return this.moneyType;
    }

    public float getBackupRateKillFish() {
        return backupRateKillFish;
    }

    public void setRefund(boolean refund) {
        isRefund = refund;
    }

    public boolean isRefund() {
        return isRefund;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public long getBaseFund() {
        return baseFund;
    }

    public void setBaseFund(long baseFund) {
        this.baseFund = baseFund;
    }

    public int getBaseGunMoney() {
        return 1;//baseGunMoney;
    }

    public void setBaseGunMoney(int baseGunMoney) {
        this.baseGunMoney = baseGunMoney;
    }

    public float getRateKillFish() {
        return rateKillFish;
    }

    public void setRateKillFish(float rateKillFish) {
        this.rateKillFish = rateKillFish;
    }

    public long getMaxGoldInstantJoinTable() {
        return maxGoldInstantJoinTable;
    }
    @Override
    public void load(String[] strings) {
        this.roomId = Integer.valueOf(strings[0]);
        this.roomName = strings[1];
        this.baseFund = Long.valueOf(strings[2]);
        this.baseGunMoney = Integer.valueOf(strings[3]);
        this.rateKillFish = Float.valueOf(strings[4]);
        this.isRefund = false;
        this.backupRateKillFish = this.rateKillFish;
        this.maxGoldInstantJoinTable = Long.valueOf(strings[5]);

        switch (this.roomId) {
            case RoomType.TRIAL:
                this.moneyType = MoneyType.TRIAL;
                break;
            default:
                this.moneyType = MoneyType.GOLD;
        }
    }
}
