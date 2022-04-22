package games.shootingfish.datatype;

import libs.util.CRandom;
import games.shootingfish.data.IDataRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WINDNCC on 7/6/2017.
 */
public class FishGroup implements IDataRow {
    private int groupId;
    private List<Integer> listFishTypeId;
    private int fishNumber;
    private static CRandom cRandom = new CRandom();
    public FishGroup(){

    }
    public FishGroup(String[] strings){
        this.loadDataRow(strings);
    }

    public int getRandomFishType(){
        int rndIndex = cRandom.randInt(0, listFishTypeId.size()-1);
        return listFishTypeId.get(rndIndex);
    }

    /*
    * Doi voi Boss thi phai dua vao Room Type
    * */
    public int getFishBossType(RoomType roomType) {
        if (listFishTypeId.size() < 3) {
            return 19;//default
        }
        switch (roomType.getRoomId()){
            case RoomType.NORMAL:
                return listFishTypeId.get(0);
            case RoomType.VIP:
                return  listFishTypeId.get(1);
            case RoomType.SUPER_VIP:
                return listFishTypeId.get(2);
        }
        return 19;//default
    }

    //Load data from file line
    @Override
    public void loadDataRow(String[] strings) {
        this.groupId = Integer.valueOf(strings[0]);
        listFishTypeId = new ArrayList<>();
        String[] fishIds = strings[1].split(";");
        for (String fishId : fishIds) {
            listFishTypeId.add(Integer.valueOf(fishId));
        }
        this.fishNumber = Integer.valueOf(strings[2]);
    }

    public List<Integer> getListFishTypeId() {
        return listFishTypeId;
    }

    public int getFishNumber() {
        return fishNumber;
    }

    public int getGroupId() {
        return groupId;
    }

}
