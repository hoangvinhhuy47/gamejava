package games.shootingfish.datatype;

import games.shootingfish.data.IDataRow;

public class NewFishType implements IDataRow {
    private int typeId;
    private float rate;

    private int minPrice = 0;
    private int maxPrice = 0;
    private int poolNumber = 0;
    private int rateAlone = 0;
    private boolean canHasShadow = false;
    private boolean isFish1 = false;
    private boolean isFish2 = false;
    private boolean isFish3  = false;

    public NewFishType(){

    }
    public NewFishType(String[] strings){
        this.loadDataRow(strings);
    }

    @Override
    public void loadDataRow(String[] strings) {
        this.typeId = Integer.valueOf(strings[0]);
        //this.rate = Float.valueOf(strings[2]);
        this.minPrice = Integer.valueOf(strings[3]);
        this.maxPrice = Integer.valueOf(strings[4]);
        this.poolNumber = Integer.valueOf(strings[5]);
        this.rateAlone = Integer.valueOf(strings[6]);
        this.canHasShadow = Integer.valueOf(strings[8]) == 1;
        this.isFish1 = Integer.valueOf(strings[9]) == 1;
        this.isFish2 = Integer.valueOf(strings[10]) == 1;
        this.isFish3 = Integer.valueOf(strings[11]) == 1;
    }



    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public int getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getPoolNumber() {
        return poolNumber;
    }

    public void setPoolNumber(int poolNumber) {
        this.poolNumber = poolNumber;
    }

    public int getRateAlone() {
        return rateAlone;
    }

    public void setRateAlone(int rateAlone) {
        this.rateAlone = rateAlone;
    }

    public boolean isCanHasShadow() {
        return canHasShadow;
    }

    public void setCanHasShadow(boolean canHasShadow) {
        this.canHasShadow = canHasShadow;
    }

    public boolean isFish1() {
        return isFish1;
    }

    public void setFish1(boolean fish1) {
        isFish1 = fish1;
    }

    public boolean isFish2() {
        return isFish2;
    }

    public void setFish2(boolean fish2) {
        isFish2 = fish2;
    }

    public boolean isFish3() {
        return isFish3;
    }

    public void setFish3(boolean fish3) {
        isFish3 = fish3;
    }
}
