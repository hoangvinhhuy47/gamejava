package games.shootingfish.manager;

import games.core.application.ServerConfig;
import games.core.application.channel.TaskScheduler;
import games.core.database.RedisConst;
import games.core.database.RedisManager;
import games.core.database.document.DRoomInfo;
import games.shootingfish.FishShooting;
import games.shootingfish.GameLobby;
import games.shootingfish.GameRoom;
import games.shootingfish.data.DataManager;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.datatype.TimingSpawnFish;
import libs.util.LogFactory;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FundManager implements RedisConst {

    public static FundManager getInstance() {
        if (instance == null) instance = new FundManager();
        return instance;
    }

    private static final Jedis jedis = RedisManager.getInstance().getResource(0);
    private static Logger logger = LogFactory.getLogger(FundManager.class.getSimpleName());
    public static FundManager instance;

    private Set<RoomType> roomTypes;

    private boolean percent = false;

    //Cho nay chinh ti le jackpot, ti le thang thua --- THINH
    private int rateToFundDealer = 70;
    private int rateToJackpot = 5;

    public FundManager() {
        roomTypes = DataManager.getInstance().getDataRoomType();
        for (RoomType roomType : roomTypes) {
            if (jedis.hget(RD_CONFIG_FUND_ROOM_KEY, RD_BASE_FUND_KEY + "_" + roomType.getRoomName()) == null) {
                setBaseFund(roomType.getBaseFund(), roomType);
                logger.info("Not found Basefund in redis. Init this " + roomType.getRoomName() + " Ok!");
            }

            DRoomInfo roomInfo = new DRoomInfo(roomType.getRoomId());

            if (jedis.hget(RD_CONFIG_FUND_ROOM_KEY, RD_FUND_KEY + "_" + roomType.getRoomName()) == null) {
                setFund(roomInfo.getFund(), roomType);
                logger.info("Not found fund in redis. Init this " + roomType.getRoomName() + " Ok!");
            }

            if (jedis.hget(RD_CONFIG_FUND_DEALER_ROOM_KEY, RD_FUND_DEALER_KEY + "_" + roomType.getRoomName()) == null) {
                setFundDealer(roomInfo.getFundDealer(), roomType);
                logger.info("Not found fund dealer in redis. Init this " + roomType.getRoomName() + " Ok!");
            }

            if (jedis.hget(RD_CONFIG_JACK_POT_ROOM_KEY, RD_JACK_POT_KEY + "_" + roomType.getRoomName()) == null) {
                long jackpot = roomInfo.getPot();
                if (jackpot < TimingSpawnFish.MIN_JACKPOT) {
                    jackpot = TimingSpawnFish.MIN_JACKPOT;
                }
                setJackPot(jackpot, roomType);
                logger.info("Not found jackpot in redis. Init this " + roomType.getRoomName() + " Ok!");
            }
        }

        rateToFundDealer = ServerConfig.getInstance().getDealerRate();
        rateToJackpot = ServerConfig.getInstance().getJackpotRate();
        TaskScheduler.getInstance().scheduleAtFixedRate(new FundManager.MongoFundSaveRunnable(), 5000, 5000, TimeUnit.MILLISECONDS);
    }

    private class MongoFundSaveRunnable implements Runnable {

        @Override
        public void run() {
            saveToMongo();
        }
    }

    public synchronized void saveToMongo() {
        for (RoomType roomType : roomTypes) {
            long fundAmount = getFund(roomType);
            long jackpot = getJackPot(roomType);
            long fundDealer = getFundDealer(roomType);
            //no them 1 thang nay
            GameRoom gameRoom = GameLobby.getInstance().getListGameRoom().get(roomType.getRoomId());
            gameRoom.getRoomFund().setFundAnd(fundAmount, fundDealer, jackpot);
        }
    }

    public void addJackPot(Long jackPot, RoomType roomType) {
        long newJackPot = getJackPot(roomType) + jackPot;
        setJackPot(newJackPot, roomType);
    }

    public void subJackPot(Long jackPot, RoomType roomType) {
        long newJackPot = getJackPot(roomType) - jackPot;
        setJackPot(newJackPot, roomType);
    }

    public synchronized void setJackPot(Long jackPot, RoomType roomType) {
        String keyJackPot = RD_JACK_POT_KEY + "_" + roomType.getRoomName();
        jedis.hset(RD_CONFIG_JACK_POT_ROOM_KEY, keyJackPot, jackPot.toString());
    }

    public synchronized long getJackPot(RoomType roomType) {
        String keyFund = RD_JACK_POT_KEY + "_" + roomType.getRoomName();
        String value = jedis.hget(RD_CONFIG_JACK_POT_ROOM_KEY, keyFund);
        //logger.error("getJackPot - key = " + keyFund + " - value = " + value);
        return Long.valueOf(value);
    }

    //add tu tool
    public synchronized void addMoneyFund(long money, RoomType roomType) {
        //quỹ mới
        long newFund = getFund(roomType) + money;
        setFund(newFund, roomType);
        checkBaseFund(roomType);
    }

    public synchronized void addMoney(long money, RoomType roomType) {
        long moneyToJackPot = (long) (rateToJackpot * money / 100);

        //dam bao jackpot phai > min va < max
        long curJackpot = getJackPot(roomType);
        long newJackpot = curJackpot + moneyToJackPot;
        if (newJackpot < TimingSpawnFish.MIN_JACKPOT) {
            moneyToJackPot = TimingSpawnFish.MIN_JACKPOT - curJackpot;//jackpot phai lon hon min
        } else {
            if (newJackpot > TimingSpawnFish.MAX_JACKPOT) {
                moneyToJackPot = TimingSpawnFish.MAX_JACKPOT - curJackpot;
            }
        }

        //lay 70% ra cho nha cai
        long moneyToDealer = (long) (rateToFundDealer * money / 100);
        long newFundDealer = getFundDealer(roomType) + moneyToDealer;
        setFundDealer(newFundDealer, roomType);

        long moneyToFund = money - moneyToJackPot - moneyToDealer;

        //quỹ mới
        long newFund = getFund(roomType) + moneyToFund;
        setFund(newFund, roomType);
        checkBaseFund(roomType);

        //add jackpot
        addJackPot(moneyToJackPot, roomType);
    }

    public synchronized void subMoney(long money, RoomType roomType) {

        //quỹ mới
        long newFund = getFund(roomType) - money;
        setFund(newFund, roomType);

        checkBaseFund(roomType);
    }

    public synchronized long recieveJackPot(RoomType roomType, int gunMultiple) {
        long jackPot = getJackPot(roomType);
        int rateRecieveJackpot = TimingSpawnFish.MAX_MUL_JACKPOT;//1 : 1000

        long moneyReward = (long) (rateRecieveJackpot * gunMultiple);
        if (jackPot < moneyReward) {
            moneyReward = jackPot;
        }
        long jackPotCurrent = jackPot - moneyReward;

        //kiem tra neu jackpotCurrent ma  < min => set no ve min va tru vao quy
        if (jackPotCurrent < TimingSpawnFish.MIN_JACKPOT) {
            long jackpotGetFromFund = TimingSpawnFish.MIN_JACKPOT - jackPotCurrent;

            //tru vao fund
            subMoney(jackpotGetFromFund, roomType);

            //tinh lai jackpot current
            jackPotCurrent += jackpotGetFromFund;
        }

        setJackPot(jackPotCurrent, roomType);

        return moneyReward;
    }

    public void setBaseFund(Long baseFund, RoomType roomType) {
        String keyBaseFund = RD_BASE_FUND_KEY + "_" + roomType.getRoomName();
        jedis.hset(RD_CONFIG_FUND_ROOM_KEY, keyBaseFund, baseFund.toString());
    }

    //tien quy cua nha cai
    public void setFundDealer(Long fundDealer, RoomType roomType) {
        String keyFund = RD_FUND_DEALER_KEY + "_" + roomType.getRoomName();
        jedis.hset(RD_CONFIG_FUND_DEALER_ROOM_KEY, keyFund, fundDealer.toString());
    }

    //tien quy cua nha cai
    public long getFundDealer(RoomType roomType) {
        String keyFund = RD_FUND_DEALER_KEY + "_" + roomType.getRoomName();
        return Long.valueOf(jedis.hget(RD_CONFIG_FUND_DEALER_ROOM_KEY, keyFund));
    }

    public void setFund(Long fund, RoomType roomType) {
        String keyFund = RD_FUND_KEY + "_" + roomType.getRoomName();
        jedis.hset(RD_CONFIG_FUND_ROOM_KEY, keyFund, fund.toString());
    }

    public long getFund(RoomType roomType)
    {
        String keyFund = RD_FUND_KEY + "_" + roomType.getRoomName();
        return Long.valueOf(jedis.hget(RD_CONFIG_FUND_ROOM_KEY, keyFund));
    }

    public long getBaseFund(RoomType roomType) {
        String keyFund = RD_BASE_FUND_KEY + "_" + roomType.getRoomName();
        return Long.valueOf(jedis.hget(RD_CONFIG_FUND_ROOM_KEY, keyFund));
    }

    public void checkBaseFund(RoomType roomType) {
//        if (roomType.getRoomId() != RoomType.TRIAL)
//        {
//            boolean checkPercent = roomType.getCheckPercent(roomType.getRoomId());
//
//            float baseFund = getBaseFund(roomType);
//            long fundAmount = getFund(roomType);
//            float rate = (fundAmount / baseFund);
//
//            if(!checkPercent)
//            {
//                roomType.setRateKillFish(rate);
//            }
//            else{
//                if(rate >= 1.5)
//                {
//                    roomType.setRateKillFish(rate);
//                    checkPercent = false;
//                }
//                else{
//                    roomType.setRateKillFish(0);
//                }
//            }
//
//            if(rate < 0.55){
//                checkPercent = true;
//                roomType.setRateKillFish(0);
//            }
//
//            roomType.setCheckPercent(roomType.getRoomId(), checkPercent);
        long baseFund = getBaseFund(roomType);
        long currentFund = getFund(roomType);
        if (baseFund >= currentFund && !roomType.isRefund())
        {
            roomType.setRefund(true);
            if (currentFund <= 100000) {
                roomType.setRateKillFish(0.0f);
            }
            else
            {
                roomType.setRateKillFish(0.5f);
            }

            return;
        }
        if (roomType.isRefund() && getBaseFund(roomType) < getFund(roomType))
        {
            roomType.setRefund(false);
            roomType.setRateKillFish(roomType.getBackupRateKillFish());
        }
    }


    public int getRateToFundDealer() {
        return rateToFundDealer;
    }

    public int getRateToJackpot() {
        return rateToJackpot;
    }

    public static void main(String[] args) {
        FundManager.getInstance();
    }
}
//TONG QUAN VE TINH TIEN JACKPOT