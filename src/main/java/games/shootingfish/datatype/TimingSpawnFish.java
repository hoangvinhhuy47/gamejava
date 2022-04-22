package games.shootingfish.datatype;
import games.shootingfish.FishShooting;
import games.shootingfish.GameConfig;
import games.shootingfish.data.DataManager;
import games.shootingfish.object.Fish;
import games.shootingfish.pool.PoolFish;
import libs.util.CRandom;
import message.FishShootingProtos;
import message.FishShootingProtos.TimingSpawnFishResponseBean;
import message.IResponseBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by WINDNCC on 7/5/2017.
 */
public class TimingSpawnFish extends ConcurrentHashMap<Integer, HashFish> implements IResponseBean<TimingSpawnFishResponseBean.Builder> {

    //GET CONFIG FROM FILE
    private static GameConfig.FishSpawnConfig config = GameConfig.getInstance().fishSpawnConfig;
    private static final int TIME_BETWEEN_2_TIMER = config.getTimeBetween2Timer();
    private static final int MIN_PATH_IN_ONE_TIMER = config.getMinPathInOneTimer();
    private static final int MAX_PATH_IN_ONE_TIMER = config.getMaxPathInOneTimer();
    private static final float RATE_ALONE_GROUP  = config.getRateAloneGroup();

    public static final float RATE_BOT_KILL_FISH = config.getRateBotKillFish();
    public static final int RATE_SHADOW = config.getRateShadow();

    //cu moi 2 player tang len thi kill fish giam bao nhieu lan
    public static final float RATE_KILL_FISH_2PLAYER = config.getRateKillFishBy2Player();

    //cu moi 3 nguoi choi tang len thi ty le ca tang len bao nhieu con
    public static final int COUNT_FISH_3PLAYER = config.getCountFishBy3Player();

    public static final int COUNT_SO_FREE_BULLET = config.getCountSoForBulletFree();

    public  static final int MAX_MUL_JACKPOT = config.getMaxMulJackpot();

    public static final float RATE_FUND_FOR_JACKPOT = config.getRateFundForJackpot();

    //min cua jackpot
    public static final long MIN_JACKPOT = 500000;

    public static final long MAX_JACKPOT = 900000;
    //#


    private static final int TIME_BETWEEN_2_PATH = 10;

    private static final int MAX_COUNT_GROUP_SHADOW = 10;
    private static final int TIME_BETWEEN_2_SHADOW = 40;
    //30s khong duoc ra 2 con ca lon cung luc
    private static final int TIME_BETWEEN_2_BIG_FISH = 35;
    private static final int TIME_BETWEEN_2_BOSS_FISH = 120;
    private HashMap<Integer, List<Integer>> listFishAtTime = new HashMap<>();
    private HashMap<Integer, List<Integer>> listPathAtTime = new HashMap<>();
    private HashMap<Integer, List<Integer>> listShadowAtTime = new HashMap<>();

    //nhung con ca co cung shadow tai 1 thoi diem
    //khi no 1 con thi no het nhung con con lai
    private HashMap<Integer, List<Fish>> listFishGroupAtTime = new HashMap<>();

    //con nai
    private List<Fish> listFishBoss = new ArrayList<>();

    private int[] listPathBoss = new int[]{25,3};

    private int[] listPathJackpot = new int[]{5, 26, 30, 18};

    private PoolFish poolFish;
    private static DataManager dataManager = DataManager.getInstance();
    private static CRandom cRandom = new CRandom();
//    private ArrayList<FishProperties> listFishNormalState;
    private static int numOfPath = 40;//dataManager.getDataFishPathNormal().size();
    private static int numOfGroup = 26;//dataManager.getDataFishGroup().size();
    public TimingSpawnFish(){

    }
    private FishShooting fishShooting;
    public TimingSpawnFish(int initCapacity, FishShooting fishShooting){
        super(initCapacity);
        this.fishShooting = fishShooting;
        this.poolFish = new PoolFish();
//        listFishNormalState = new ArrayList<>(DataManager.getInstance().getListFishPropertiesNormal());
    }

    public static void main(String[] args) throws InterruptedException {
//        GameState gameState = new GameState();
//        TimingSpawnFish timingSpawnFish = new TimingSpawnFish(100);
//        for (int i=1 ; i<=10; i++){
//            timingSpawnFish.getByState(gameState.nextState());
//            System.out.println("pool size after shift: " + timingSpawnFish.poolFish.inSize());
//            System.out.println("total fish has been shift: " + timingSpawnFish.totalSize());
//        }
//        int uniqueId = (int) (System.currentTimeMillis() & 0xfffffff);
//        System.out.println(uniqueId);

        //Test random GROUP
        TimingSpawnFish timingSpawnFish = new TimingSpawnFish();
        int count = 0;
        for (int i = 1; i<100; i++){
            FishGroup fishGroup = timingSpawnFish.getRandomFishGroup();
            if (fishGroup.getGroupId() == 1) count++;
            System.out.print(fishGroup.getGroupId() + " ");
        }
        System.out.println();
        System.out.println(count);
    }

    /**
     * remove fish from this
     * @param timer timer holder fish
     * @param fishId fish id
     */
    public void removeFish(int timer, int fishId){
        if (this.containsKey(timer))
            if (this.get(timer).containsKey(fishId))
                this.remove(fishId);
    }

    public boolean removeFish(int timer, Fish fish){
        if (this.containsKey(timer))
            if (this.get(timer).containsKey(fish.getId()))
                if (this.get(timer).remove(fish.getId(), fish)){
                    poolFish.putFish(fish);
                    return true;
                }
        return false;
    }

    /**
     *  get random listPath(numberPathGet) in DataFishPathNormal read from FishPathNormal.txt
     * @param numberPathGet number of Path want to get
     * @return ArrayList<FishPathProperties>
     */
    private ArrayList<FishPathProperties> getRandomPath(int numberPathGet, int atTime){
        ArrayList<FishPathProperties> fishPathPropertiesArrayList = new ArrayList<>();
        for (int i = 1; i<=numberPathGet; i++){
            /*
            Random 1 of paths in DataFishPathNormal
             */
            FishPathProperties randomPath = checkCanAddPath(atTime);
            if (fishPathPropertiesArrayList.contains(randomPath)) {
                i--;
            } else {
                fishPathPropertiesArrayList.add(randomPath);
            }
        }
        return fishPathPropertiesArrayList;
    }


    private FishPathProperties checkCanAddPath(int time) {
        int retry = 0;
        boolean canAddFish = true;
        FishPathProperties path = dataManager.getDataFishPathNormal().get(cRandom.randInt(1,numOfPath));
        int pathId = 0;
        do {
            canAddFish = true;
            pathId = path.getPathId();
            if (listPathAtTime.containsKey(pathId)) {
                List<Integer> timeAddPath = listPathAtTime.get(pathId);

                for (int i = 0; i < timeAddPath.size(); i++) {
                    int timeCur = timeAddPath.get(i);

                    if ( Math.abs(time - timeCur) < TIME_BETWEEN_2_PATH) {
                        canAddFish = false;
                    }
                }
            } else {
                canAddFish = true;
            }

            if (!canAddFish) {
                path = dataManager.getDataFishPathNormal().get(cRandom.randInt(1,numOfPath));
                retry ++;
            }
        } while (!canAddFish && retry < 10);


        if (listPathAtTime.containsKey(pathId)) {
            List<Integer> timeAddPath = listPathAtTime.get(pathId);
            timeAddPath.add(time);
        } else {
            List<Integer> timeAddPath = new ArrayList<>();
            timeAddPath.add(time);
            listPathAtTime.put(pathId, timeAddPath);
        }
        return path;
    }

    private int checkCanAddTypeShadow(int typeShadow, int time)
    {
        boolean canAddFish = true;
        canAddFish = true;
        if (listShadowAtTime.containsKey(typeShadow))
        {
            List<Integer> timeAddPath = listShadowAtTime.get(typeShadow);

            for (int i = 0; i < timeAddPath.size(); i++) {
                int timeCur = timeAddPath.get(i);

                if ( Math.abs(time - timeCur) < TIME_BETWEEN_2_SHADOW) {
                    canAddFish = false;
                }
            }
        } else {
            canAddFish = true;
        }
        if (!canAddFish) {
            return 0;
        }

        if (listShadowAtTime.containsKey(typeShadow)) {
            List<Integer> timeAddPath = listShadowAtTime.get(typeShadow);
            timeAddPath.add(time);
        } else {
            List<Integer> timeAddPath = new ArrayList<>();
            timeAddPath.add(time);
            listShadowAtTime.put(typeShadow, timeAddPath);
        }
        return typeShadow;
    }

    private FishPathProperties getBossPath() {
        int rand = cRandom.randInt(0, 100);
        int pathId = 0;
        if (rand < 50) {
            pathId = 101;
        } else {
            pathId = 102;
        }
        FishPathProperties pathBoss = dataManager.getDataFishPathNormal().get(pathId);
        return pathBoss;
    }

    /**
     * Random one of list Group in DataGroup read from FishGroup.txt
     * @return FishGroup
     */
    private FishGroup getRandomFishGroup(){
        int randAlone = cRandom.randInt(1,100);
        if (randAlone <= RATE_ALONE_GROUP * 100) return dataManager.getDataFishGroup().get(1);
        int randId = cRandom.randInt(2,numOfGroup);
        return dataManager.getDataFishGroup().get(randId);
    }

    /**
     * Get fish group boss with group id = 0, in DataGroup read from FishGroup.txt
     * @return FishGroup
     */
    private FishGroup getBossGroup() {
        return dataManager.getDataFishGroup().get(0);
    }

    /**
     * random num of fish decision by rand num of path and rand group (use for normal state)
     * @return HashFish
     */
    private HashFish _getRandomHashFish(int atTime) throws InterruptedException {
        HashFish hashFish = new HashFish();
        int numPathGet = cRandom.randInt(MIN_PATH_IN_ONE_TIMER, MAX_PATH_IN_ONE_TIMER);
//        if (fishShooting != null && fishShooting.getGameTable() != null) {
//            numPathGet += fishShooting.getGameTable().getCountFishInscreaseByPlayerInRoom();
//        }

        if (atTime == 0) {
            numPathGet *= 2;//nhan 2 lan ca khi lan dau vao game
        }
        int countShadow = 0;
        int typeShadow = 0;
        boolean hasGroupShadow = false;
        int rand = cRandom.randInt(0, 100);
        if (rand < TimingSpawnFish.RATE_SHADOW) {
            hasGroupShadow = true;
            countShadow = cRandom.randInt(1, 2);
            if (fishShooting.getRoomType().getRoomId() == RoomType.VIP ||
                    fishShooting.getRoomType().getRoomId() == RoomType.SUPER_VIP) {
                typeShadow = 4;
            } else {
                typeShadow = cRandom.randInt(1, 2);
                //cai cu la 1-3 ***THINH
            }

            typeShadow = checkCanAddTypeShadow(typeShadow, atTime);
        }

        List<Fish> listFishHasShadow = new ArrayList<>();
        int totalPriceListFishShadow = 0;
        for (FishPathProperties fishPathProperties : this.getRandomPath(numPathGet, atTime)){
            if (fishPathProperties!=null){
                FishGroup fishGroup = getRandomFishGroup();
                int fishTypeId = 1;
                if (fishGroup.getGroupId() == 1) {
                    //alone
                    NewFishType newFishType = dataManager.getRandomeNewFishAlone(fishShooting.getRoomType().getRoomId(), false);
                    if (newFishType != null) {
                        fishTypeId = newFishType.getTypeId();
                    }

                } else {
                    fishTypeId = fishGroup.getRandomFishType();
                }
                switch (fishShooting.getRoomType().getRoomId()) {
                    case RoomType.NORMAL:
                        if ((fishTypeId >= 11 && fishTypeId <= 23) || fishTypeId == 6) {
                            fishTypeId = checkCanAddBigFish(fishGroup, fishTypeId, atTime);
                        }
                        break;
                    case RoomType.SUPER_VIP:
                        if (fishTypeId == 204 || fishTypeId >= 209 || fishTypeId == 120)
                        {
                            fishTypeId = checkCanAddBigFish(fishGroup, fishTypeId, atTime);
                        }
                        break;
                    case RoomType.VIP:
                        if (fishTypeId == 6 || fishTypeId == 13 || fishTypeId == 14 || fishTypeId >= 111 ){
                            fishTypeId = checkCanAddBigFish(fishGroup, fishTypeId, atTime);
                        }
                        break;
                }

                if (countShadow <= 0) {
                    typeShadow = 0;
                }

                Fish fishShadow = ParseListFish(hashFish, fishPathProperties, fishGroup, fishTypeId, typeShadow, atTime);
                if (fishShadow != null && hasGroupShadow)
                {
                    //add fish thanh cong
                    if (fishShadow.getTypeShadow() > 0) {
                        countShadow--;//con ca that su co shadow ne
                        listFishHasShadow.add(fishShadow);
                        totalPriceListFishShadow += fishShadow.getPrice();
                    } else {
                        if (listFishHasShadow.size() < MAX_COUNT_GROUP_SHADOW) {
                            listFishHasShadow.add(fishShadow);
                            totalPriceListFishShadow += fishShadow.getPrice();
                        }
                    }
                }
            }
        }
        if (listFishHasShadow.size() > 0 && !listFishGroupAtTime.containsKey(atTime))
        {
            listFishGroupAtTime.put(atTime, listFishHasShadow);

            for (int i = 0; i < listFishHasShadow.size(); i++)
            {
                Fish fish = listFishHasShadow.get(i);

                if (fish != null && fish.getTypeShadow() > 0)
                {
                    fish.updateRate(totalPriceListFishShadow + 5);
                }
            }
        }
        return hashFish;
        ///*****THINH - cho nay khac cho cu
    }

    private int checkCanAddBigFish(FishGroup fishGroup, int fishTypeId, int time) {
        int retry = 0;
        int fishIdReal = fishTypeId;
        boolean canAddFish = true;
        do {
            fishIdReal = fishTypeId;
            //gom 3 tien ca nho lai lam 1
            if(fishTypeId == 13 || fishTypeId == 15 || fishTypeId == 14 || fishTypeId == 115) {
                fishTypeId = 13;
            }
            if (fishTypeId == 16 || fishTypeId == 17 || fishTypeId == 18 || fishTypeId == 19) {
                fishTypeId = 16;
            }

            canAddFish = true;
            if (listFishAtTime.containsKey(fishTypeId)) {
                List<Integer> timeAddFish = listFishAtTime.get(fishTypeId);
                int countLimit = 1;
                int timeLimit = TIME_BETWEEN_2_BIG_FISH;
                int idTypeBigFish = 19;
                switch (fishShooting.getRoomType().getRoomId()) {
                    case RoomType.NORMAL:
                        if (fishTypeId == 6) {
                            countLimit = 4;
                        }
                        if (fishTypeId == 11) {
                            countLimit = 2;
                        }

                        if (fishTypeId == 13 || fishTypeId == 15 || fishTypeId == 14) {
                            countLimit = 2;
                            timeLimit = 35;
                        }

                        if (fishTypeId >= 16 || fishTypeId == 12) {
                            timeLimit = TIME_BETWEEN_2_BOSS_FISH;
                        }
                        if(fishTypeId == 16) {
                            timeLimit = 80;
                        }
                        if (fishTypeId == 21) {
                            timeLimit = 300;//ko cho ca den ra lan 2
                        }

                        if (fishTypeId == 23) {
                            timeLimit = 120;
                            countLimit = 1;
                        }
                        idTypeBigFish = 19;
                        break;
                    case RoomType.SUPER_VIP:
                        if (fishTypeId == 204) {
                            countLimit = 4;
                        }

                        if (fishTypeId >= 209 && fishTypeId <= 211){
                            countLimit = 2;
                        }
                        if (fishTypeId == 213) {
                            timeLimit = 150;
                            countLimit = 1;
                        }

                        if (fishTypeId >= 212) {
                            countLimit = 1;
                            timeLimit = TIME_BETWEEN_2_BOSS_FISH;
                        }
                        if(fishTypeId == 214) {
                            timeLimit = 150;
                            countLimit = 1;
                        }
                        if(fishTypeId == 120) {
                            timeLimit = 120;
                            countLimit = 1;
                        }

                        idTypeBigFish = 215;
                        break;
                    case RoomType.VIP:
                        if (fishTypeId == 111){
                            countLimit = 2;
                        }
                        if (fishTypeId == 6) {
                            countLimit = 4;
                        }

                        if (fishTypeId == 13 || fishTypeId == 115 || fishTypeId == 14) {
                            countLimit = 2;
                            timeLimit = 35;
                        }
                        if (fishTypeId == 112) {
                            timeLimit = 150;
                            countLimit = 1;
                        }

                        if (fishTypeId >= 116) {
                            countLimit = 1;
                            timeLimit = TIME_BETWEEN_2_BOSS_FISH;
                        }
                        if(fishTypeId == 120) {
                            timeLimit = 120;
                            countLimit = 1;
                        }

                        //jus for test
//                        if(fishTypeId == 117) {
//                            timeLimit = 50;
//                            countLimit = 3;
//                        }

                        idTypeBigFish = 116;
                        break;
                }

                if (time < 5 && fishTypeId >= idTypeBigFish) {
                    canAddFish = false;//trong 5 giay dau khong ra ca lon
                } else {
                    for (int i = 0; i < timeAddFish.size(); i++) {
                        int timeCur = timeAddFish.get(i);

                        if (Math.abs(time - timeCur) < timeLimit) {
                            countLimit --;
                            if (countLimit <= 0 || time == timeCur) {
                                canAddFish = false;
                            }
                        }
                    }
                }

            } else {
                canAddFish = true;
            }

            if (!canAddFish) {
                if (fishGroup.getGroupId() == 1) {
                    //alone
                    boolean isSmall = false;
                    if (retry >= 12) {
                        isSmall = true;
                    }
                    NewFishType newFishType = dataManager.getRandomeNewFishAlone(fishShooting.getRoomType().getRoomId(), isSmall);
                    if (newFishType != null) {
                        fishTypeId = newFishType.getTypeId();
                    }

                } else {
                    fishTypeId = fishGroup.getRandomFishType();
                }

                if ((fishTypeId < 11 || (fishTypeId >= 101 && fishTypeId < 111)
                        || (fishTypeId >= 198 && fishTypeId < 208) && fishTypeId != 6))
                {
                    return fishTypeId;
                }
                retry ++;
            }
        } while (!canAddFish && retry < 15);

        if (listFishAtTime.containsKey(fishTypeId))
        {
            List<Integer> timeAddFish = listFishAtTime.get(fishTypeId);
            timeAddFish.add(time);
            //System.out.printf("time add fish - " + fishTypeId + " - " + time);
        } else {
            List<Integer> timeAddFish = new ArrayList<>();
            timeAddFish.add(time);
            listFishAtTime.put(fishTypeId, timeAddFish);
            //System.out.printf("time add fish - " + fishTypeId + " - " + time);
        }
        return fishIdReal;
    }

    /**
     * convert config from file to HashFish
     * @param listPaths list path want to get (ex: 5,7,8) read from pathDesign
     * @param fishPathState HashMap PathProperties read from FishPath
     * @return HashFish
     */
    private HashFish _getHashFish(List<Integer> listPaths, HashMap<Integer, FishPathProperties> fishPathState) throws InterruptedException {
        HashFish hashFish = new HashFish();
        if (fishPathState != null && !fishPathState.isEmpty()){
            for (Integer pathId : listPaths){
                FishPathProperties fishPathProperties = fishPathState.get(pathId);
                if (fishPathProperties != null){
                    FishGroup fishGroup = dataManager.getDataFishGroup().get(fishPathProperties.getFishGroupId());
                    if (fishGroup!=null){
                        for (int i=0; i< fishGroup.getFishNumber(); i++){
                            //Lấy cá từ pool ra
                            Fish fishFromPool = this.poolFish.shiftFish();

//                            fishFromPool.setGroupId(fishGroup.getGroupId());
//                            fishFromPool.setPathId(fishPathProperties.getPathId());
//                            fishFromPool.setIndexInGroup(i);

                            //fish group khong co shadow
                            fishFromPool.ParseData(fishPathProperties.getFishTypeId(), fishGroup.getGroupId(), fishPathProperties.getPathId(), i, 0);
                            fishFromPool.setAlive(true);
                            fishFromPool.setTimer(0);
//                            fishFromPool.setTypeId(fishPathProperties.getFishTypeId());
                            hashFish.put(fishFromPool);
                        }
                    }
                }
            }
        }
        // for (Fish fish : hashFish.values()) {
        //     System.out.println("Fish in hash - " + fish.getId() + " - " + fish.getTypeId() + " - " + fish.getIndexInGroup());
        // }
        return hashFish;
    }

    //lay danh sach ca group tai 1 thoi diem
    public List<Fish> getListGroupAtTime(int timer) {
        if (listFishGroupAtTime.containsKey(timer)) {
            return listFishGroupAtTime.get(timer);
        }
        return null;
    }

    /**
     * get HashFish and timer for normal state
     */
    private void getNormalSate(int timeDuring, RoomType roomType) throws InterruptedException {
        int numberOfTimer = (timeDuring-10)/TIME_BETWEEN_2_TIMER;//20 -> 2

        for (int i = 0; i<= numberOfTimer; i++){
            int randTime = cRandom.randInt( i*3, ((i+1)*3 - 1) );
            if(i == 0) {
                randTime = 1;//add tu dau luon
            }

            this.put(randTime, _getRandomHashFish(randTime));
        }

        //addBossInNormalState(timeDuring, roomType);
        System.out.printf("List path at time = " + listPathAtTime.size());
        System.out.printf("List fish group at time = " + listFishGroupAtTime.size());
        System.out.printf("List fish group at time = " + listFishBoss.size());
        System.out.printf("List fish group at time = " + listShadowAtTime.size());
    }

    private void addBossInNormalState(int timeDuring, RoomType roomType) throws InterruptedException {
        HashFish hashFish = new HashFish();
        int minTime = timeDuring / 4;
        int maxTime = minTime + timeDuring / 8;
        Integer randTime = cRandom.randInt(minTime, maxTime);
        if (this.contains(randTime)) {
            hashFish = this.get(randTime);
        } else {
            this.put(randTime, hashFish);
        }
        System.out.println("Time during = " + timeDuring + " - rand time = " + randTime);
        FishPathProperties fishPathProperties = getBossPath();
        if (fishPathProperties!=null){
            FishGroup fishGroup = getBossGroup();
            int fishTypeId = fishGroup.getFishBossType(roomType);
            ParseListFish(hashFish, fishPathProperties, fishGroup, fishTypeId, 0, 0);
        }
    }

    /*
    * Dua list fish vao Hash dua vao group va fishId
    * */
    private Fish ParseListFish(HashFish hashFish, FishPathProperties fishPathProperties, FishGroup fishGroup, int fishTypeId, int typeShadow, int atTime) throws InterruptedException {
        boolean hasShadow = false;
        Fish fish = null;
        for (int i = 0; i< fishGroup.getFishNumber(); i++) {
            Fish fishFromPool = this.poolFish.shiftFish();
//            fishFromPool.setGroupId(fishGroup.getGroupId());
//            fishFromPool.setPathId(fishPathProperties.getPathId());
//            fishFromPool.setIndexInGroup(i);
//            fishFromPool.setAlive(true);
//            fishFromPool.setTypeId(fishTypeId);
            fishFromPool.setAlive(true);
            fishFromPool.setTimer(atTime);

            int pathId = fishPathProperties.getPathId();
            if (fishTypeId == 22 || fishTypeId == 214)
            {
                //randome lai danh sach duong di cho con boss
                int index = cRandom.randInt(0, listPathBoss.length -1);
                pathId = listPathBoss[index];

                //System.out.println("New path id of boss - " + index  + " - " + pathId);
            }

            if (fishTypeId == 23 || fishTypeId == 120) {
                //randome lai danh sach duong di cho con boss
                int index = cRandom.randInt(0, listPathJackpot.length -1);
                pathId = listPathJackpot[index];
                //System.out.println("New path id of boss - " + index  + " - " + pathId);
            }
            hasShadow = fishFromPool.ParseData(fishTypeId, fishGroup.getGroupId(), pathId, i, typeShadow);

            //con boss
            if (fishFromPool.getTypeId() == 22 || fishFromPool.getTypeId() == 214) {
                listFishBoss.add(fishFromPool);
            }
            if (hasShadow) {
                fish = fishFromPool;
            }
            hashFish.put(fishFromPool);
        }
        return fish;
    }

    /**
     * get HashFish and timer for boss state
     */
    private void getBossState(GameState.StateType stateType){
        HashMap<Integer, FishPathProperties> fishPathBoss;
        ArrayList<TimerPaths> pathDesignBoss;
        switch (stateType){
            case BOSS2:
//                System.out.println("lay boss 2");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss2();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss2();
                break;
            case BOSS3:
//                System.out.println("lay boss 2");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss3();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss3();
                break;
            case BOSS4:
//                System.out.println("lay boss 2");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss4();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss4();
                break;
            case BOSS5:
//                System.out.println("lay boss 2");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss5();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss5();
                break;
            case BOSS1_2:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss1();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss1_2();
                break;
            case BOSS3_2:
//                System.out.println("lay boss 2");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss3();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss3_2();
                break;
            case BOSS4_2:
//                System.out.println("lay boss 2");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss4();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss4_2();
                break;
            case BOSS5_2:
//               System.out.println("lay boss 2");
               pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss5();
               fishPathBoss = DataManager.getInstance().getDataFishPathBoss5_2();
              break;
            case BOSS11:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss11();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss11();
                break;
            case BOSS12:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss12();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss12();
                break;
            case BOSS13:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss13();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss13();
                break;
            case BOSS14:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss14();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss14();
                break;
            case BOSS15:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss15();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss15();
                break;
            case BOSS17:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss17();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss17();
                break;
            case BOSS18:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss18();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss18();
                break;
            case BOSS18_1:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss18();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss18_1();
                break;
            case BOSS18_2:
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss18();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss18_2();
                break;
            default:
//                System.out.println("lay boss 1");
                pathDesignBoss = DataManager.getInstance().getDataPathDesignBoss1();
                fishPathBoss = DataManager.getInstance().getDataFishPathBoss1();
                break;
        }
        try {
            if (pathDesignBoss!= null && !pathDesignBoss.isEmpty()){
                for (TimerPaths timerPaths : pathDesignBoss){
                    this.put(timerPaths.getTimer(), _getHashFish(timerPaths.getListPath(), fishPathBoss));
                }
            }
            System.out.println("Count fish = " + this.size());
        } catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public TimingSpawnFish getByState(GameState state, RoomType roomType) throws InterruptedException {
        _pushFishBackToPool();
        
        listFishAtTime = new HashMap<>();//reset
        listPathAtTime = new HashMap<>();
        listFishGroupAtTime = new HashMap<>();
        listFishBoss = new ArrayList<>();
        listShadowAtTime = new HashMap<>();
        
        switch (state.getStateType()){
            case NORMAL:
                getNormalSate(state.getTimeDuring(), roomType);
                break;
            default:
                getBossState(state.getStateType());
                //getNormalSate(state.getTimeDuring(), roomType);
                break;
        }
        return this;
    }

    private void _pushFishBackToPool(){
        poolFish.collectBackToPool();
        this.clear();
    }

    @Override
    public TimingSpawnFishResponseBean.Builder parseResponseBeanBuilder() {
        TimingSpawnFishResponseBean.Builder builder = TimingSpawnFishResponseBean.newBuilder();
        if (!this.isEmpty()){
            for (Map.Entry<Integer, HashFish> hashFishEntry : this.entrySet()){
                if (hashFishEntry!=null){
                    //Tạo ra 1 HashTimingSpawnFish (Tương ứng với 1 cặp Key->Value)
                    FishShootingProtos.HashTimingSpawnFishResponseBean.Builder hashTimingSpawnFishRes = FishShootingProtos.HashTimingSpawnFishResponseBean.newBuilder();
                    hashTimingSpawnFishRes.setTimeSpawn(hashFishEntry.getKey());
                    //Duyệt hash chứa danh sách cá với key là fishId
                    for (Map.Entry<Integer, Fish> fishEntry : hashFishEntry.getValue().entrySet()){
                        //Gán thông tin trả về (Tương ứng với thông tin của 1 object Fish)
                        if (fishEntry.getValue() != null){
                            hashTimingSpawnFishRes.addHashFishSpawn(fishEntry.getValue().parseResponseBeanBuilder().setTimer(hashFishEntry.getKey()));
                        }
                    }
                    builder.addHasTimingPawnFish(hashTimingSpawnFishRes);
                }
            }
        }
        return builder;
    }
    public int totalSize(){
        int s = 0;
        for (HashFish hashFish : this.values()){
            s+= hashFish.size();
        }
        return s;
    }

    public List<Fish> getListFishBoss() {
        return listFishBoss;
    }
}
