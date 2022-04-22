package games.core.common.data;

import games.core.constant.Version;
import games.core.user.object.DailyRewardItem;
import games.core.user.object.InfoUserItem;
import games.minigame.wheel.FortuneWheelItem;
import games.payment.constant.PaymentActionType;
import games.payment.object.PaymentItem;
import games.shootingfish.datatype.HashByInt;
import libs.util.CRandom;
import libs.util.LogFactory;
import message.XProtos;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by tuanhoang on 8/17/17.
 */
public class DataCommonManager implements DataCommonFilePath {
    Logger logger;
    private static DataCommonManager instance;
    public static DataCommonManager getInstance() {
        if (instance == null){
            instance = new DataCommonManager();
        }
        return instance;
    }

    private DataCommonManager(){
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        loadDataFortuneWheel();
        logger.info("Wheel item size = " + wheelItemArrayList.size());

        loadDataForDailyItems();
        logger.info("Daily reward item size = " + dailyRewardItems.size());

        loadDataForInfoUserItems();
        logger.info("Info users item size = " + dailyRewardItems.size());

        loadDataForVersion();
        logger.info("Version item size = " + versions.size());

        loadDataForPaymentItems();
        logger.info("Payment item size = " + paymentItems.size());
    }

    public static void main(String[] args){
        DataCommonManager.getInstance();
    }

    HashByInt<DailyRewardItem> dailyRewardItems = new HashByInt<>();
    HashByInt<ArrayList<FortuneWheelItem>> wheelItemArrayList = new HashByInt<ArrayList<FortuneWheelItem>>();
    ArrayList<InfoUserItem> infoUserItems = new ArrayList<>();
    ArrayList<Version> versions = new ArrayList<>();
    ArrayList<PaymentItem> paymentItems = new ArrayList<>();
    List<XProtos.PaymentItemResponseBean> paymentItemResponseBeans = new ArrayList<>();

    private void loadDataForVersion() {
        loadDataFromFileToObject(FILE_VERSION_TYPE, new HandleData()
        {
            @Override
            public void handle(String[] strings)
            {
                Version version = new Version(strings);
                versions.add(version);
            }
        });
    }

    private void loadDataForInfoUserItems() {
        loadDataFromFileToObject(FILE_INFO_USER_TYPE, new HandleData() {
            @Override
            public void handle(String[] strings) {
                InfoUserItem infoUserItem = new InfoUserItem(strings);
                infoUserItems.add(infoUserItem);
            }
        });
    }


    private void loadDataForDailyItems()
    {
        loadDataFromFileToObject(FILE_DAILY_REWARD_TYPE, new HandleData()
        {
            @Override
            public void handle(String[] strings) {
                DailyRewardItem dailyRewardItem = new DailyRewardItem(strings);
                dailyRewardItems.put(dailyRewardItem.getDay(), dailyRewardItem);
            }
        });
    }

    private void loadDataForPaymentItems() {
        loadDataFromFileToObject(FILE_PAYMENT_TYPE, strings ->
        {
            PaymentItem paymentItem = new PaymentItem(strings);
            paymentItems.add(paymentItem);
            paymentItemResponseBeans.add(paymentItem.getProtoMessage());
        });
    }

    private void loadDataFortuneWheel() {
        loadDataFromFileToObject(FILE_VQMM_TYPE, new HandleData() {
            @Override
            public void handle(String[] strings) {
                FortuneWheelItem wheelItem = new FortuneWheelItem(strings);
                int wheelId = wheelItem.getWheelId();
                ArrayList<FortuneWheelItem> listWheel = wheelItemArrayList.get(wheelId);
                if (listWheel == null) {
                    listWheel = new ArrayList<FortuneWheelItem>();
                    listWheel.add(wheelItem);
                    wheelItemArrayList.put(wheelId, listWheel);
                } else {
                    listWheel.add(wheelItem);
                    wheelItemArrayList.replace(wheelId, listWheel);
                }
            }
        });
    }

    private interface HandleData {
        public void handle(String[] strings);
    }
    //Method loadData
    private void loadDataFromFileToObject(String filePath, HandleData iHandleData){
        try {
            List<String> listStr = FileUtils.readLines(new File(filePath), "utf-8");
            for (int i = 1; i< listStr.size(); i++)
            {
                String string = listStr.get(i);
                String[] str = string.split(",");
                iHandleData.handle(str);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Collection<DailyRewardItem> getDailyRewardItems() {
        return dailyRewardItems.values();
    }

    public DailyRewardItem getDailyRewardItemByDay(int day) {
        return dailyRewardItems.get(day);
    }

    public HashByInt<ArrayList<FortuneWheelItem>> getWheelItemArrayList() {
        return wheelItemArrayList;
    }

    public InfoUserItem getRandomeInfoUser() {
        int countInfoUser = infoUserItems.size();
        int idInfo = new CRandom().randInt(0, countInfoUser - 1);
        return infoUserItems.get(idInfo);
    }

    public ArrayList<Version> getVersions() {
        return versions;
    }


    public PaymentItem getPaymentItem(int id) {
        for (PaymentItem paymentItem : paymentItems) {
            if (paymentItem.getId() == id) {
                return paymentItem;
            }
        }
        return null;
    }

    //khi can lay thong tin the cao hoac cashout
    public PaymentItem getPaymentItem(PaymentActionType type, int value) {
        for(int i = 0; i < paymentItems.size(); i++) {
            PaymentItem paymentItem = paymentItems.get(i);
            if(paymentItem.getActionType() == type && paymentItem.getValue() == value) {
                return paymentItem;
            }
        }
        return null;
    }

    public int getPercentPaymentItemTransfer(){
        //PaymentItem paymentItem = getPaymentItem(10);
        PaymentItem paymentItem = getPaymentItem(PaymentActionType.TRANSFER, 100);
        return paymentItem.getValue() - paymentItem.getRealValue();
    }

    public List<XProtos.PaymentItemResponseBean> getPaymentItemResponseBeans() {
        return paymentItemResponseBeans;
    }
}
