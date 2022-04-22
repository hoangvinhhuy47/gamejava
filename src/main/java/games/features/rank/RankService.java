package games.features.rank;

import games.core.application.channel.TaskScheduler;
import games.core.application.handler.ChannelContextInfo;
import games.core.constant.ServerCommandConstant;
import games.core.database.MongoConst;
import games.core.database.document.UserInfo;
import games.core.database.document.UserMoney;
import games.core.database.model.MUserMoney;
import games.features.constant.ResponseBeanFeatureConst;
import games.tx.TXActionConst;
import libs.util.LogFactory;
import message.FeatureProtos;
import message.FeatureProtos.RankUserInfo;
import message.XProtos;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static games.core.constant.ServerCommandConstant.TAI_XIU_COMMAND;

/**
 * Created by tuanhoang on 8/21/17.
 */
public class RankService implements ResponseBeanFeatureConst{
    private static RankService _instance = new RankService();
    MUserMoney mUserMoney;
    Logger logger;
    public static RankService getInstance() {
        if (_instance == null) {
            _instance = new RankService();
        }
        return _instance;
    }

    RankService() {
        mUserMoney = MUserMoney.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        init();
    }

    void init() {
        TaskScheduler.getInstance().scheduleAtFixedRate(new MonitorUpdateRanking(), 0, TIME_UPDATE_RANKING, TimeUnit.MINUTES);
    }

    private static final int TIME_UPDATE_RANKING = 1;//minutes
    private static final int LIMIT_RANK = 20;

    private int currentGoldRank = 0;
    private int currentWinGoldRank = 0;

    //List<UserMoney> listTopGold = new ArrayList<>();
    //List<UserMoney> listTopWinGold = new ArrayList<>();

    List<RankUserInfo> listRankGold = new ArrayList<>();
    List<RankUserInfo> listRankWinGold = new ArrayList<>();

    //region init
    void initRanking() {
        initTopGold();
        initTopWinGold();
    }

    void initTopGold() {
        try {
            List<UserMoney> listTopGold = mUserMoney.getRankByKey(LIMIT_RANK, MongoConst.FIELD_USER_GOLD);
            listRankGold = getListRankByListUserMoney(listTopGold);
        } catch (IllegalAccessException e) {
            logger.error("Init top gold error", e);
        }
    }

    void initTopWinGold() {
        try {
            List<UserMoney> listTopWinGold = mUserMoney.getRankByKey(LIMIT_RANK, MongoConst.FIELD_WIN_GOLD);
            listRankWinGold = getListRankByListUserMoney(listTopWinGold);
        } catch (IllegalAccessException e) {
            logger.error("Init top gold error", e);
        }
    }

    List<RankUserInfo> getListRankByListUserMoney(List<UserMoney> listUserMoney) {
        List<RankUserInfo> listRank = new ArrayList<>();
        int rank = 0;
        for (UserMoney userMoney : listUserMoney) {
            rank ++;
            RankUserInfo rankUserInfo = getRankInfoByUserMoney(rank, userMoney);
            listRank.add(rankUserInfo);
        }
        return listRank;
    }

    RankUserInfo getRankInfoByUserMoney(int rank, UserMoney userMoney)
    {
        UserInfo userInfo = new UserInfo(userMoney.getId());
        try {
            userInfo.updateDB();
            if (userInfo != null) {
                RankUserInfo.Builder rankInfoBuilder = RankUserInfo.newBuilder();
                rankInfoBuilder.setRank(rank);
                rankInfoBuilder.setNickName(userInfo.getUserName());
                rankInfoBuilder.setUserId(userInfo.getId());
                rankInfoBuilder.setAvatar("");
                rankInfoBuilder.setGold(userMoney.getMoneyGold());
                rankInfoBuilder.setTotalWinGold(userMoney.getWinGold());
                return rankInfoBuilder.build();
            }
        } catch (Exception e) {
            logger.error("Parse rank info error", e);
            e.printStackTrace();
        }
        return null;
    }

    class MonitorUpdateRanking implements Runnable {
        @Override
        public void run() {
//            initRanking();
        }
    }
    //endregion

    //region request
    public void getTopGold(ChannelContextInfo ctx) {
        FeatureProtos.RankingResponse.Builder responseRank = FeatureProtos.RankingResponse.newBuilder();
        responseRank.addAllListUser(listRankGold);
        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.RANK_COMMAND);
        response.setBeanType(RESPONSE_RANKING_TOP_TOTAL_MONEY_BEAN);
        response.setData(responseRank.build().toByteString());
        ctx.writeAndFlush(response.build());
    }

    public void getTopWinGold(ChannelContextInfo ctx) {
        FeatureProtos.RankingResponse.Builder responseRank = FeatureProtos.RankingResponse.newBuilder();
        responseRank.addAllListUser(listRankWinGold);
        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(ServerCommandConstant.RANK_COMMAND);
        response.setBeanType(RESPONSE_RANKING_TOP_WIN_MONEY_BEAN);
        response.setData(responseRank.build().toByteString());
        ctx.writeAndFlush(response.build());
    }

    //add new
    public void getRankWinGold(ChannelContextInfo ctx, int command)
    {
        FeatureProtos.RankingResponse.Builder responseRank = FeatureProtos.RankingResponse.newBuilder();
        responseRank.addAllListUser(listRankWinGold);
        XProtos.XMessage.Builder response = XProtos.XMessage.newBuilder();
        response.setCommand(TAI_XIU_COMMAND);
        response.setBeanType(command);
        response.setData(responseRank.build().toByteString());
        ctx.writeAndFlush(response.build());
    }
    //endregion
}
