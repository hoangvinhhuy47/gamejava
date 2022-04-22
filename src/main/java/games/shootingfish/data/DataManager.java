package games.shootingfish.data;

import games.shootingfish.datatype.*;
import games.shootingfish.object.Gun;
import libs.util.CRandom;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by WINDNCC on 7/6/2017.
 */
public class DataManager implements DataFilePath{
    /**
     * Hold all room type in RoomType.txt
     */
    private Set<RoomType> dataRoomType = new HashSet<>(10);

    /**
     * Hold all fish type in FishType.txt
     */
    private HashByInt<FishType> dataFishType = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt
     */
    private HashByInt<NewFishType> dataNewFishType = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt => isFish1
     */
    private HashByInt<NewFishType> dataNewFishType1 = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt => isFish2
     */
    private HashByInt<NewFishType> dataNewFishType2 = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt => isFish3
     */
    private HashByInt<NewFishType> dataNewFishType3 = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt => isFish1 and price <= 10
     */
    private HashByInt<NewFishType> dataNewFishTypeSmall1 = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt => isFish2 and price <= 10
     */
    private HashByInt<NewFishType> dataNewFishTypeSmall2 = new HashByInt<>(100);

    /**
     * Hold all newfish type in NewFishType.txt => isFish3 and price <= 10
     */
    private HashByInt<NewFishType> dataNewFishTypeSmall3 = new HashByInt<>(100);

    /**
     * Hold all fish group in FishGroup.txt
     */
    private HashByInt<FishGroup> dataFishGroup = new HashByInt<>(100);

//    private List<FishProperties> listFishPropertiesNormal = new ArrayList<>(100);

    /**
     * Hold all gun in GunType.txt
     */
    private HashByInt<Gun> dataGun = new HashByInt<>(20);

    private HashByInt<FishPathProperties> dataFishPathNormal = new HashByInt<>(100);
    private ArrayList<TimerPaths> dataPathDesignBoss1 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss1 = new HashByInt<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss1_2 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss2 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss2 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss3 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss3 = new HashByInt<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss3_2 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss4 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss4 = new HashByInt<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss4_2 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss5 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss5 = new HashByInt<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss5_2 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss11 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss11 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss12 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss12 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss13 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss13 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss14 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss14 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss15 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss15 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss17 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss17 = new HashByInt<>(100);

    private ArrayList<TimerPaths> dataPathDesignBoss18 = new ArrayList<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss18 = new HashByInt<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss18_1 = new HashByInt<>(100);
    private HashByInt<FishPathProperties> dataFishPathBoss18_2 = new HashByInt<>(100);

    private static DataManager instance;

    private CRandom cRandom;
    private static final int MAX_RATE_FISH_ALONE = 302;

    private int MAX_RATE_FISH_ALONE_1 = 0;//total rateAlone fish game 1
    private int MAX_RATE_FISH_ALONE_2 = 0;// totoal rateAlone fish game 2
    private int MAX_RATE_FISH_ALONE_3 = 0;//total rateAlone fish game3

    private int MAX_RATE_FISH_ALONE_SMALL_1 = 0;//total rateAlone fish game 1 and price <= 10
    private int MAX_RATE_FISH_ALONE_SMALL_2 = 0;// totoal rateAlone fish game 2 and price <= 10
    private int MAX_RATE_FISH_ALONE_SMALL_3 = 0;//total rateAlone fish game3 and price <= 10

    public static DataManager getInstance() {
        if (instance == null){
            instance = new DataManager();
        }
        return instance;
    }
    private DataManager(){
        loadDataRoomType();
        loadDataFishType();
        loadDataNewFishType();
        loadDataFishGroup();
//        loadDataFishProperties(DataFilePath.FILE_FISH_SPAWN_NORMAL, listFishPropertiesNormal);
        loadDataGunType();
        /*
          Design of normal state will be random so don't need to load PathDesign
         */
        loadDataFishPath(FILE_FISH_PATH_NORMAL, dataFishPathNormal);
        /*
            Path design make decision when path render fish
         */
        loadDataFishPath(FILE_FISH_PATH_BOSS_1, dataFishPathBoss1);
        loadPathDesign(FILE_PATH_DESIGN_BOSS_1, dataPathDesignBoss1);

        loadDataFishPath(FILE_FISH_PATH_BOSS_2, dataFishPathBoss2);
        loadPathDesign(FILE_PATH_DESIGN_BOSS_2, dataPathDesignBoss2);

        loadDataFishPath(FILE_FISH_PATH_BOSS_3, dataFishPathBoss3);
        loadPathDesign(FILE_PATH_DESIGN_BOSS_3, dataPathDesignBoss3);

        loadDataFishPath(FILE_FISH_PATH_BOSS_4, dataFishPathBoss4);
        loadPathDesign(FILE_PATH_DESIGN_BOSS_4, dataPathDesignBoss4);

        loadDataFishPath(FILE_FISH_PATH_BOSS_5, dataFishPathBoss5);
        loadPathDesign(FILE_PATH_DESIGN_BOSS_5, dataPathDesignBoss5);

        loadDataFishPath(FILE_FISH_PATH_BOSS_11, dataFishPathBoss11);
        loadPathDesign(FILE_PATH_DESIGN_BOSS_11, dataPathDesignBoss11);

        loadPathDesign(FILE_PATH_DESIGN_BOSS_12, dataPathDesignBoss12);
        loadDataFishPath(FILE_FISH_PATH_BOSS_12, dataFishPathBoss12);

        loadPathDesign(FILE_PATH_DESIGN_BOSS_13, dataPathDesignBoss13);
        loadDataFishPath(FILE_FISH_PATH_BOSS_13, dataFishPathBoss13);

        loadPathDesign(FILE_PATH_DESIGN_BOSS_14, dataPathDesignBoss14);
        loadDataFishPath(FILE_FISH_PATH_BOSS_14, dataFishPathBoss14);

        loadPathDesign(FILE_PATH_DESIGN_BOSS_15, dataPathDesignBoss15);
        loadDataFishPath(FILE_FISH_PATH_BOSS_15, dataFishPathBoss15);

        loadPathDesign(FILE_PATH_DESIGN_BOSS_17, dataPathDesignBoss17);
        loadDataFishPath(FILE_FISH_PATH_BOSS_17, dataFishPathBoss17);

        loadPathDesign(FILE_PATH_DESIGN_BOSS_18, dataPathDesignBoss18);
        loadDataFishPath(FILE_FISH_PATH_BOSS_18, dataFishPathBoss18);


        loadDataFishPath(FILE_FISH_PATH_BOSS_1_2, dataFishPathBoss1_2);
        loadDataFishPath(FILE_FISH_PATH_BOSS_3_2, dataFishPathBoss3_2);
        loadDataFishPath(FILE_FISH_PATH_BOSS_4_2, dataFishPathBoss4_2);
        loadDataFishPath(FILE_FISH_PATH_BOSS_5_2, dataFishPathBoss5_2);

        loadDataFishPath(FILE_FISH_PATH_BOSS_18_1, dataFishPathBoss18_1);
        loadDataFishPath(FILE_FISH_PATH_BOSS_18_2, dataFishPathBoss18_2);

        cRandom = new CRandom();
    }
    public static void main(String[] args) {
//        Long s = System.nanoTime();
        //System.out.println(DataManager.getInstance().dataNewFishType.toString());
        NewFishType newFish = DataManager.getInstance().getRandomeNewFishAlone(RoomType.NORMAL, false);

//        Long e = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            newFish = DataManager.getInstance().getRandomeNewFishAlone(RoomType.VIP, false);
            System.out.println("New Fish type = " + newFish.getTypeId());
        }

        for (int i = 0; i < 1000; i++) {
            newFish = DataManager.getInstance().getRandomeNewFishAlone(RoomType.SUPER_VIP, false);
            System.out.println("New Fish type = " + newFish.getTypeId());
        }
    }

    private void loadDataFishPath(String filePath, HashMap<Integer, FishPathProperties> fishPathPropertiesHashMap)
    {
        loadDataFromFileToObject(filePath, new HandleData()
        {
            @Override
            public void handle(String[] strings) {
                FishPathProperties pathProperties = new FishPathProperties(strings);
                fishPathPropertiesHashMap.put(pathProperties.getPathId(), pathProperties);
            }
        });
    }

    private void loadDataRoomType(){
        loadDataFromFileToObject(FILE_ROOM_TYPE, new HandleData() {
            @Override
            public void handle(String[] strings)
            {
                RoomType roomType = new RoomType(strings);
                dataRoomType.add(roomType);
            }
        });
    }



    private void loadDataFishType(){
//        System.out.println(DataFilePath.FILE_FISH_TYPE);
        loadDataFromFileToObject(FILE_FISH_TYPE, new HandleData() {
            @Override
            public void handle(String[] strings) {
                FishType fishType = new FishType(strings);
                dataFishType.put(fishType.getTypeId(), fishType);
            }
        });
    }

    private void loadDataNewFishType()
    {
//        System.out.println(DataFilePath.FILE_FISH_TYPE);
        loadDataFromFileToObject(FILE_NEW_FISH_TYPE, new HandleData()
        {
            @Override
            public void handle(String[] strings)
            {
                NewFishType newFishType = new NewFishType(strings);
                dataNewFishType.put(newFishType.getTypeId(), newFishType);

                if (newFishType.isFish1())
                {
                    dataNewFishType1.put(newFishType.getTypeId(), newFishType);
                    MAX_RATE_FISH_ALONE_1 += newFishType.getRateAlone();

                    if (newFishType.getMinPrice() <= 10)
                    {
                        dataNewFishTypeSmall1.put(newFishType.getTypeId(), newFishType);
                        MAX_RATE_FISH_ALONE_SMALL_1 += newFishType.getRateAlone();
                    }
                }
                if (newFishType.isFish2())
                {
                    dataNewFishType2.put(newFishType.getTypeId(), newFishType);
                    MAX_RATE_FISH_ALONE_2 += newFishType.getRateAlone();

                    if (newFishType.getMinPrice() <= 10)
                    {
                        dataNewFishTypeSmall2.put(newFishType.getTypeId(), newFishType);
                        MAX_RATE_FISH_ALONE_SMALL_2 += newFishType.getRateAlone();
                    }
                }
                if (newFishType.isFish3())
                {
                    dataNewFishType3.put(newFishType.getTypeId(), newFishType);
                    MAX_RATE_FISH_ALONE_3 += newFishType.getRateAlone();

                    if (newFishType.getMinPrice() <= 10)
                    {
                        dataNewFishTypeSmall3.put(newFishType.getTypeId(), newFishType);
                        MAX_RATE_FISH_ALONE_SMALL_3 += newFishType.getRateAlone();
                    }
                }
            }
        });
    }

    private void loadDataFishGroup()
    {
        loadDataFromFileToObject(FILE_FISH_GROUP, new HandleData() {
            @Override
            public void handle(String[] strings) {
                FishGroup fishGroup = new FishGroup(strings);
                dataFishGroup.put(fishGroup.getGroupId(), fishGroup);
            }
        });
    }
    private void loadDataGunType()
    {
        loadDataFromFileToObject(DataFilePath.FILE_GUN_TYPE, new HandleData() {
            @Override
            public void handle(String[] strings) {
                Gun gun = new Gun(strings);
                dataGun.put(gun.getId(), gun);
            }
        });
    }
//    private void loadDataFishProperties(String filePath, List<FishProperties> handleList){
//        loadDataFromFileToObject(filePath, new HandleData() {
//            @Override
//            public void handle(String[] strings) {
//                FishProperties fishProperties = new FishProperties(strings);
//                handleList.add(fishProperties);
//            }
//        });
//    }
    private void loadPathDesign(String filePath, ArrayList<TimerPaths> timerPathsData){
            loadDataFromFileToObject(filePath, new HandleData() {
                @Override
                public void handle(String[] strings) {
                    if (strings.length>1){
                        TimerPaths timerPaths = new TimerPaths(strings);
                        timerPathsData.add(timerPaths);
                    }
                }
            });
    }
    private interface HandleData
    {
        public void handle(String[] strings);
    }
    //Method loadData
    private void loadDataFromFileToObject(String filePath, HandleData iHandleData)
    {
        try {
            List<String> listStr = FileUtils.readLines(new File(filePath), "utf-8");
            for (int i = 1; i< listStr.size(); i++)
            {
                String string = listStr.get(i);
                String[] str = string.split("\t");
                iHandleData.handle(str);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public NewFishType getNewFishTypeById(int fishTypeId) {
        return dataNewFishType.getOrDefault(fishTypeId, null);
    }

    public NewFishType getRandomeNewFishAlone(int roomType, boolean isSmall)
    {
        switch (roomType)
        {
            case RoomType.NORMAL:
                if (isSmall)
                {
                    return getRandomeNewFishAlone(dataNewFishTypeSmall1, MAX_RATE_FISH_ALONE_SMALL_1);
                } else
                {
                    return getRandomeNewFishAlone(dataNewFishType1, MAX_RATE_FISH_ALONE_1);
                }

            case RoomType.VIP:
                if (isSmall) {
                    return getRandomeNewFishAlone(dataNewFishTypeSmall2, MAX_RATE_FISH_ALONE_SMALL_2);
                } else {
                    return getRandomeNewFishAlone(dataNewFishType2, MAX_RATE_FISH_ALONE_2);
                }

            case RoomType.SUPER_VIP:
                if (isSmall) {
                    return getRandomeNewFishAlone(dataNewFishTypeSmall3, MAX_RATE_FISH_ALONE_SMALL_3);
                } else {
                    return getRandomeNewFishAlone(dataNewFishType3, MAX_RATE_FISH_ALONE_3);
                }
        }
        return null;
    }

    private NewFishType getRandomeNewFishAlone(HashByInt<NewFishType> poolFishType, int maxRateAlone)
    {
        //System.out.println("Rand = " + maxRateAlone);
        int rand = cRandom.randInt(0, maxRateAlone - 1);

        for (NewFishType newFishType : poolFishType.values())
        {
            if (newFishType != null) {
                rand -= newFishType.getRateAlone();
                if (rand < 0)
                {
                    //System.out.println("Fish type = " + newFishType.getTypeId() + " - " + newFishType.getRateAlone());
                    return newFishType;
                }
            }
        }
        return null;
    }

    public HashMap<Integer, FishType> getDataFishType() {
        return dataFishType;
    }

    public HashMap<Integer, FishGroup> getDataFishGroup() {
        return dataFishGroup;
    }

//    public List<FishProperties> getListFishPropertiesNormal() {
//        return listFishPropertiesNormal;
//    }

    public HashMap<Integer, Gun> getDataGun() {
        return dataGun;
    }

    public HashMap<Integer, FishPathProperties> getDataFishPathNormal() {
        return dataFishPathNormal;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss1(){return dataPathDesignBoss1;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss1() {
        return dataFishPathBoss1;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss2() {
        return dataPathDesignBoss2;
    }

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss2() {
        return dataFishPathBoss2;
    }


    public HashMap<Integer, FishPathProperties> getDataFishPathBoss3() {
        return dataFishPathBoss3;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss3() {
        return dataPathDesignBoss3;
    }


    public HashMap<Integer, FishPathProperties> getDataFishPathBoss4() {
        return dataFishPathBoss4;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss4() {
        return dataPathDesignBoss4;
    }


    public HashMap<Integer, FishPathProperties> getDataFishPathBoss5() {
        return dataFishPathBoss5;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss5() {
        return dataPathDesignBoss5;
    }

    public HashByInt<FishPathProperties> getDataFishPathBoss1_2() {
        return dataFishPathBoss1_2;
    }

    public HashByInt<FishPathProperties> getDataFishPathBoss3_2() {
        return dataFishPathBoss3_2;
    }

    public HashByInt<FishPathProperties> getDataFishPathBoss4_2() {
        return dataFishPathBoss4_2;
    }

    public HashByInt<FishPathProperties> getDataFishPathBoss5_2() {
        return dataFishPathBoss5_2;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss11(){return dataPathDesignBoss11;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss11() {
        return dataFishPathBoss11;
    }

    public ArrayList<TimerPaths> getDataPathDesignBoss12(){return dataPathDesignBoss12;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss12() {return dataFishPathBoss12; }

    public ArrayList<TimerPaths> getDataPathDesignBoss13(){return dataPathDesignBoss13;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss13() {return dataFishPathBoss13; }

    public ArrayList<TimerPaths> getDataPathDesignBoss14(){return dataPathDesignBoss14;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss14() {return dataFishPathBoss14; }

    public ArrayList<TimerPaths> getDataPathDesignBoss15(){return dataPathDesignBoss15;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss15() {return dataFishPathBoss15; }

    public ArrayList<TimerPaths> getDataPathDesignBoss17(){return dataPathDesignBoss17;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss17() {return dataFishPathBoss17; }

    public ArrayList<TimerPaths> getDataPathDesignBoss18(){return dataPathDesignBoss18;}

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss18() {return dataFishPathBoss18; }

    public HashMap<Integer, FishPathProperties> getDataFishPathBoss18_1() {return dataFishPathBoss18_1; }
    public HashMap<Integer, FishPathProperties> getDataFishPathBoss18_2() {return dataFishPathBoss18_2; }

    public Set<RoomType> getDataRoomType() {
        return dataRoomType;
    }
}
