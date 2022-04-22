package games.shootingfish.object;

import games.core.common.money.MoneyType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 10/3/17.
 */
public class FishingPlayerLogGame {
    protected int userId = 0;
    protected int tableId = 0;
    protected String nameSession = "";
    protected long moneyStart = 0;
    protected long moneyEnd = 0;
    protected long timeStart = 0;
    protected long timeEnd = 0;
    protected List<String> listBullets = new ArrayList<>();
    protected List<String> listKillFish = new ArrayList<>();
    protected long sumMoneyFireBullet = 0;
    protected long sumMoneyKillFish = 0;
    protected MoneyType moneyType;

    public void resetLog() {
        tableId = 0;
        userId = 0;
        nameSession = "";
        moneyStart = 0;
        moneyEnd = 0;
        timeStart = 0;
        timeEnd = 0;
        sumMoneyFireBullet = 0;
        sumMoneyKillFish = 0;
        listBullets = new ArrayList<>();
        listKillFish = new ArrayList<>();
        moneyType = MoneyType.NONE;
    }

    public void startSession(int userId, int tableId, String nameSession, long moneyStart, long timeStart, MoneyType moneyType) {
        this.userId = userId;
        this.tableId = tableId;
        this.nameSession = nameSession;
        this.moneyStart = moneyStart;
        this.timeStart = timeStart;
        this.moneyType = moneyType;
    }

    public void fireBullet(int bulletId, long money) {
        listBullets.add(bulletId + "-" + money);
        sumMoneyFireBullet += money;
    }

    public void killFish(int fishTypeId, int bulletId, long moneyReward) {
        listKillFish.add(fishTypeId + "-" + bulletId + "-" + moneyReward);
        sumMoneyKillFish += moneyReward;
    }

    public void endSession(long moneyEnd, long timeEnd) {
        this.moneyEnd = moneyEnd;
        this.timeEnd = timeEnd;
    }

}
