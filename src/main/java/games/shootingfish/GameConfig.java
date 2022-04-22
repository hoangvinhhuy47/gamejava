package games.shootingfish;

import games.shootingfish.data.ConfigFilePath;
import games.shootingfish.data.DataManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameConfig implements ConfigFilePath{
    private static GameConfig instance;
    public FishSpawnConfig fishSpawnConfig;
    private GameConfig(){
        fishSpawnConfig = new FishSpawnConfig();
    }
    public static GameConfig getInstance() {
        if (instance == null) instance = new GameConfig();
        return instance;
    }
    public class FishSpawnConfig{
        int timeBetween2Timer;
        int minPathInOneTimer;
        int maxPathInOneTimer;
        float rateAloneGroup;
        int rateShadow = 0;//ty le ra vong xoay
        float rateBotKillFish = 1.0f;
        float rateKillFishBy2Player = 0;
        int countFishBy3Player = 0;
        int countSoForBulletFree = 0;//bao nhieu cai so thi duoc bullet free
        int maxMulJackpot = 0;//so nhan toi da cua jackpot
        float rateFundForJackpot = 0;
        FishSpawnConfig(){
            loadDataFromFileToObject(FILE_RATE_SPAWN_FISH, this::load);
        }
        private void load(String[] strings){
            timeBetween2Timer = Integer.valueOf(strings[0]);
            minPathInOneTimer = Integer.valueOf(strings[1]);
            maxPathInOneTimer = Integer.valueOf(strings[2]);
            rateAloneGroup    = Float.valueOf(strings[3]);
            rateShadow    = Integer.valueOf(strings[4]);
            rateBotKillFish    = Float.valueOf(strings[5]);
            rateKillFishBy2Player    = Float.valueOf(strings[6]);
            countFishBy3Player    = Integer.valueOf(strings[7]);
            countSoForBulletFree    = Integer.valueOf(strings[8]);
            maxMulJackpot    = Integer.valueOf(strings[9]);
            rateFundForJackpot    = Float.valueOf(strings[10]);
        }

        public float getRateAloneGroup() {
            return rateAloneGroup;
        }

        public int getRateShadow() {
            return  rateShadow;
        }

        public int getMaxPathInOneTimer() {
            return maxPathInOneTimer;
        }

        public int getMinPathInOneTimer() {
            return minPathInOneTimer;
        }

        public int getTimeBetween2Timer() {
            return timeBetween2Timer;
        }

        public float getRateBotKillFish() {
            return rateBotKillFish;
        }

        public float getRateKillFishBy2Player() {
            return rateKillFishBy2Player;
        }

        public int getCountFishBy3Player() {
            return countFishBy3Player;
        }

        public int getCountSoForBulletFree() {
            return countSoForBulletFree;
        }

        public int getMaxMulJackpot() {
            return maxMulJackpot;
        }

        public float getRateFundForJackpot() {
            return rateFundForJackpot;
        }
    }

    public static void main(String[] args) {
        GameConfig gameConfig = GameConfig.getInstance();
        System.out.println(gameConfig.fishSpawnConfig.maxPathInOneTimer);
    }
    private interface HandleData {
        public void handle(String[] strings);
    }
    //Method loadData
    private void loadDataFromFileToObject(String filePath, HandleData iHandleData){
        try {
            List<String> listStr = FileUtils.readLines(new File(filePath), "utf-8");
            for (int i = 1; i< listStr.size(); i++){
                String string = listStr.get(i);
                String[] str = string.split("\t");
                iHandleData.handle(str);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
