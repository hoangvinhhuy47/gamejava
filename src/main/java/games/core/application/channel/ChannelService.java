package games.core.application.channel;

import games.GameManager;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.CommonCommand;
import games.core.constant.ResponseBeanConstant;
import games.core.constant.ResponseErrorCodeConst;
import games.core.user.User;
import games.core.user.UserService;
import games.shootingfish.GameCommand;
import games.shootingfish.manager.FishingPlayerManager;
import games.system.database.document.ReportCCU;
import games.system.database.model.MReportCCU;
import io.netty.channel.ChannelHandlerContext;
import libs.util.Helper;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cuong Nguyen Cao on 8/8/17.
 */
public class ChannelService {
    private static ChannelService instance;
    private Map<ChannelHandlerContext, ChannelContextInfo> listActiveChannel;
    private Map<Integer, ChannelContextInfo> listAuthChannel;
    private static Logger logger = LogFactory.getLogger(ChannelService.class.getSimpleName());

    private static final int			PING_TIME_OUT	= 30;										// 40 second
    private static final int			HARD_TIME_OUT	= 180;									// 3 minutes

    public static ChannelService getInstance() {
        if (instance == null) {
            instance = new ChannelService();
        }
        return instance;
    }
    private TaskScheduler taskScheduler;

    private CommonCommand commonCommand;
    private FishingPlayerManager fishingPlayerManager;
    private MReportCCU mReportCCU;
    private ReportCCU reportCCU;
    private ChannelService() {
        init();
    }

    private int ccu = 0;
    private int ccu_in_game = 0;

    public void init() {
        taskScheduler = TaskScheduler.getInstance();
        mReportCCU = MReportCCU.getInstance();
        fishingPlayerManager = FishingPlayerManager.getInstance();
        commonCommand = CommonCommand.getInstance();

        int expected_maximal_number_of_data = 1000;
        int capacity = (int) ((expected_maximal_number_of_data) / 0.75 + 1); // reference here https://sites.google.com/site/markussprunck/blog-1/howtoinitializeajavahashmapwithreasonablevalues

        listActiveChannel = new ConcurrentHashMap<>(capacity);
        listAuthChannel = new ConcurrentHashMap<>(capacity);
        ZonedDateTime next1Min = ZonedDateTime.now().plusMinutes(1).withSecond(0);
        ZonedDateTime current = ZonedDateTime.now();

        int dayCurrent = Helper.getDayCurrent();
        initReportCCUByDay(dayCurrent);

        taskScheduler.scheduleAtFixedRate(new MonitorUserOnline(), (int) (next1Min.toInstant().getEpochSecond() - current.toInstant().getEpochSecond()), 15, TimeUnit.SECONDS);
    }


    public boolean authChannel(int userId, ChannelContextInfo channel) {
//        System.out.println(userId + " wanna auth ?");
        if (channel != null){
            if (listAuthChannel.containsKey(userId)) {
                //user này đang chơi => force quit nó
                ChannelContextInfo oldChannel = listAuthChannel.get(userId);
                if (oldChannel != null && oldChannel.getChannel().isOpen()) {
                    oldChannel.disconnect(ResponseErrorCodeConst.REC_DISCONNECT_BECAUSE_OTHER_PLAYER_LOGIN_YOUR_ACCOUNT);
                    logger.info("Duplicate login - " + userId + " - force quit");
                    listAuthChannel.remove(userId);
                    oldChannel.setForceQuit(true);
                    oldChannel.close();
                }
            }

            if (channel.isAuth()) return true;
            User user = UserService.getInstance().getUser(userId);
            if (user != null){
                channel.setUserId(userId);
                channel.setUser(user);
                channel.setAuth(true);
                listAuthChannel.put(userId, channel);
//            System.out.println(userId + " auth success!");
                return true;
            }
            return false;
        }
        return false;
    }

    public int getCCU() {
        return ccu;
    }

    public int getCCU_IN_GAME() {
        return ccu_in_game;
    }

    //Ngày mới thì khởi tạo lại
    private void initReportCCUByDay(int dayCurrent) {
        try {
            reportCCU = mReportCCU.getReportCCUByDay(dayCurrent);
        } catch (Exception e) {
            reportCCU = new ReportCCU();
            reportCCU.setDay(dayCurrent);
            logger.error("Get error report ccu by day - " + dayCurrent, e);
        }
    }

    public class MonitorUserOnline implements Runnable {
        public void run() {
            Collection<ChannelContextInfo> listChannel = listAuthChannel.values();
            ccu = listAuthChannel.size();
            ccu_in_game = fishingPlayerManager.getListPlayer().size();

            commonCommand.sendCCUResponse(ccu);
//            logger.info("Number of CCU connect to game: " + ccu);
//            logger.info("Number of CCU playing fishing: " + ccu_in_game);


            long now = ZonedDateTime.now().toEpochSecond();
            if (reportCCU != null) {
                int dayCurrent = Helper.getDayCurrent();
                if (reportCCU.getDay() != dayCurrent) {
                    //ngày mới nên khởi tạo lại
                    initReportCCUByDay(dayCurrent);
                }

                reportCCU.updateByCCU(ccu);
                reportCCU.updateByCCUInGame(ccu_in_game);
                try {
                    mReportCCU.saveToMongo(reportCCU);
                } catch (Exception e) {
                    logger.error("Save report ccu error");
                }
            }

            for (ChannelContextInfo ctxInfo : listChannel) {
                long lastTime = ctxInfo.getLastTimeInteractive();
                long timeDiff = now - lastTime;
                if (lastTime <= 0) {
                    logger.info("Close channle because lastime <= 0");
                    ctxInfo.close();
                } else{
                    if (timeDiff >= HARD_TIME_OUT) {
                        logger.info("Close channel because over time HARD_TIME_OUT - time disactive = " +timeDiff + " - user id = " + ctxInfo.getUserId());
                        // quá thời gian không tương tác cho phép
                        if (ctxInfo.getChannel().isOpen()) {
                            //channel còn đang mở. => đóng
                            ctxInfo.close();
                        } else {

                            //Nếu channel đã đóng => leave table
                            GameManager.getInstance().disconnectCtx(ctxInfo);
                            if (ctxInfo.getCtx() != null) {
                                removeChannel(ctxInfo.getCtx());
                            }

                        }
                    } else if (timeDiff >= PING_TIME_OUT){
                        //server chủ động PING xuống để check client
//                        logger.info("Ping to check client" + " - user id = " + ctxInfo.getUserId());
                        ctxInfo.pingToClient(ResponseBeanConstant.RESPONSE_PING_CHECK_CLIENT);
                    }
                }
            }
        }
    }

    public List<ChannelContextInfo> getAllChannle() {
        return new ArrayList<>(listAuthChannel.values());
    }

    public Map<ChannelHandlerContext, ChannelContextInfo> getListActiveChannel() {
        return listActiveChannel;
    }

    public ChannelContextInfo getChannel(ChannelHandlerContext ctx){
        return listActiveChannel.get(ctx);
    }

    public void removeChannel(ChannelHandlerContext ctx){
        ChannelContextInfo ctxInfo = listActiveChannel.get(ctx);
        if (!ctxInfo.isForceQuit()) {
            if (ctxInfo.isAuth()) listAuthChannel.remove(ctxInfo.getUserId());
        }

        if (listActiveChannel.containsKey(ctx)) listActiveChannel.remove(ctx);
    }
}
