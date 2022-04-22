package games.system;

import games.core.application.ServerConfig;
import games.core.application.channel.ChannelService;
import games.core.application.handler.ChannelContextInfo;
import games.core.constant.ResponseErrorCodeConst;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by tuanhoang on 9/7/17.
 */
public class SystemService {
    /**
     * singleton
     */
    private static SystemService instance;

    /**
     * for log
     */
    private static Logger logger;

    public static SystemService getInstance() {
        if (instance == null) instance = new SystemService();
        return instance;
    }

    SystemService() {
        isServerMaintance = Boolean.parseBoolean(ServerConfig.getInstance().getIsMaintance());
        isEnableIAP = Boolean.parseBoolean(ServerConfig.getInstance().getIsEnableIAP());
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    private boolean isServerMaintance = false;
    private boolean isEnableIAP = false;

    public boolean isEnableIAP() {
        return isEnableIAP;
    }

    public void setEnableIAP(boolean enableIAP) {
        isEnableIAP = enableIAP;
    }

    public boolean isServerMaintance() {
        return isServerMaintance;
    }

    public void setServerMaintance(boolean serverMaintance) {
        logger.info("Set server main tain - " + isServerMaintance + " - now = " + serverMaintance);
        isServerMaintance = serverMaintance;
        if (isServerMaintance) {
            forceDisconnectAllUser();
        }
    }

    public void forceDisconnectAllUser() {
        List<ChannelContextInfo> listActiveChanle = ChannelService.getInstance().getAllChannle();
        logger.info("forceDisconnectAllUser size = " + listActiveChanle.size());
        for (ChannelContextInfo ctx : listActiveChanle) {
            ctx.disconnect(ResponseErrorCodeConst.REC_SERVER_MAINTENANCE);
        }
    }
}
