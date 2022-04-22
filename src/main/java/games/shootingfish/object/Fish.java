package games.shootingfish.object;

import games.core.common.data.DataCommonManager;
import games.shootingfish.data.DataManager;
import games.shootingfish.data.IDataRow;
import games.shootingfish.datatype.NewFishType;
import games.shootingfish.datatype.TimingSpawnFish;
import libs.util.CRandom;
import message.FishShootingProtos;
import message.FishShootingProtos.FishInfoResponseBean;
import message.IResponseBean;

import java.util.Random;

/**
 * Created by WINDNCC on 7/5/2017.
 */
public class Fish extends GameObject implements IDataRow, IResponseBean<FishInfoResponseBean.Builder>{
    private int typeId;
    private int groupId;
    private int indexInGroup;
    private int pathId;
    private boolean isAlive = false;
    private float rate = 0;//ty le chet con ca nay
    private int price = 0;//so tien nhan duoc khi ban chet ca nay
    private int typeShadow = 0;


    //thoi gian spawm ra fish
    private int timer = 0;
    //return true: add shadow thanh cong
    //return false: add shadow loi
    public boolean ParseData(int typeId, int groupId, int pathId, int indexInGroup, int typeShadow) {
        this.typeId = typeId;
        this.groupId = groupId;
        this.pathId = pathId;
        this.indexInGroup = indexInGroup;

        NewFishType newFishType = DataManager.getInstance().getNewFishTypeById(typeId);
        if (newFishType != null) {
            CRandom cRandom = new CRandom();
            if(typeId == 20 || typeId == 21 || typeId == 118 || typeId == 215) {
                //2 con tien ca
                int min = newFishType.getMinPrice() / 10;
                int max = newFishType.getMaxPrice() / 10;
                price = cRandom.randInt(min, max) * 10;
                //System.out.println("Price tien ca -  " + typeId + " - " + price);
            } else {
                price = cRandom.randInt(newFishType.getMinPrice(), newFishType.getMaxPrice());
            }


            if (newFishType.isCanHasShadow()) {
                this.typeShadow = typeShadow;
            } else {
                this.typeShadow = 0;
            }
            //System.out.println("Fish - " + this.typeId + " - " + " - price = " + price + " min = " + newFishType.getMinPrice() + " - max = " + newFishType.getMaxPrice() + " - hasShadow = " + typeShadow);
        } else {
            this.typeShadow = 0;
            price = 0;
            //System.out.println("Fish - " + typeId + " - " + " - price = 0 - error");
        }
        if (price == 0) {
            rate = 0;//khong bao gio chet
        } else {
            rate = 1.0f / price;
        }
        if (!newFishType.isCanHasShadow()) {
            return  false;
        } else {
            return true;
        }
    }

    //update cho fish has shadow
    public void updateRate(int newPrice)
    {
        if (newPrice == 0)
        {
            rate = 0;//khong bao gio chet
        } else {
            rate = 1.0f / newPrice;
        }
        //System.out.println("Fish " + typeId + " - has shadow = " + typeShadow + " - new price = " + newPrice + " - rate = " + rate);
    }

    public Fish(int id){
        this.id = id;
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

    public int getPrice()
    {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getTypeShadow() {
        return typeShadow;
    }

    public void setTypeShadow(int typeShadow) {
        this.typeShadow = typeShadow;
    }


    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getIndexInGroup() {
        return indexInGroup;
    }

    public void setIndexInGroup(int indexInGroup) {
        this.indexInGroup = indexInGroup;
    }

    public int getPathId() {
        return pathId;
    }

    public void setPathId(int pathId) {
        this.pathId = pathId;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    @Override
    public String toString() {
        return String.format("( FISH [id: %s, path: %s type: %s, group: %s, IndexGroup: %s) \n", this.id, this.pathId, this.typeId, this.groupId, this.indexInGroup);
    }

    @Override
    public FishInfoResponseBean.Builder parseResponseBeanBuilder() {
        FishInfoResponseBean.Builder builder = FishInfoResponseBean.newBuilder();
            builder.setFishId(getId())
                    .setGroupId(getGroupId())
                    .setIndexInGroup(getIndexInGroup())
                    .setPathId(getPathId())
                    .setTypeId(getTypeId())
                    .setPrice(price)
                    .setTypeShadow(typeShadow);
        return builder;
    }

    public FishShootingProtos.FishHasBeenHoleResponseBean.Builder parseFishHasBeenHoleBulder() {
        FishShootingProtos.FishHasBeenHoleResponseBean.Builder builder = FishShootingProtos.FishHasBeenHoleResponseBean.newBuilder();
        builder.setFishId(getId());
        return builder;
    }

    @Override
    public void loadDataRow(String[] strings) {
        this.typeId = Integer.valueOf(strings[0]);
        this.pathId = Integer.valueOf(strings[1]);
        this.groupId = Integer.valueOf(strings[2]);
    }
}
