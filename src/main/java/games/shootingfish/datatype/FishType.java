package games.shootingfish.datatype;

import games.shootingfish.data.DataRow;
import games.shootingfish.data.IDataRow;

/**
 * Created by WINDNCC on 7/6/2017.
 */
public class FishType implements IDataRow {
    private int typeId;
    private float rate;
    private int multiplied;
    private float backupRate;
    public FishType(){

    }
    public FishType(String[] strings){
        this.loadDataRow(strings);
    }
    public FishType(int typeId, int rate, int multiplied){
        this.typeId = typeId;
        this.rate = rate;
        this.backupRate = rate;
        this.multiplied = multiplied;
    }

    @Override
    public void loadDataRow(String[] strings) {
        this.typeId = Integer.valueOf(strings[0]);
        this.rate = Float.valueOf(strings[1]);
        this.multiplied = Integer.valueOf(strings[2]);
    }

    public int getTypeId() {
        return typeId;
    }

    public float getRate() {
        return rate;
    }

    public int getMultiplied() {
        return multiplied;
    }

    public void setRate(float newRate) {
        this.rate = newRate;
    }
    public void resetRate(RoomType roomType){
        roomType.setRateKillFish(roomType.getBackupRateKillFish());
    }
}
