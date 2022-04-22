package games.api;

import java.util.Map;

public interface IApiSection {
    ApiReturnObject process(String action, Map<String, Object> data);
    String GET_CCU = "ccu";

    String CREATE_TEXT_RUN = "create";
    String RELOAD_TEXT_RUN = "reload";
    //USER
    String SUB_GOLD = "sub-gold";
    String SUB_SILVER = "sub-silver";
    String ADD_GOLD = "add-gold";
    String ADD_SILVER = "add-silver";
    String SUB_WHEEL = "sub-wheel";
    String ADD_WHEEL = "add-wheel";
    String SUB_TRIAL = "sub-trial";
    String ADD_TRIAL = "add-trial";

    //MESSAGE
    String SEND_ONE = "send-one";
    String SEND_ALL = "send-all";

    //SYSTEM
    String UPDATE_MAINTAIN_MODE = "update-maintain-mode";
    String GET_CURRENT_MAINTAIN_STATUS ="get-maintain-status";
    String UPDATE_IAP_STATUS = "update-iap-status";
    String GET_IAP_STATUS = "get-iap-status";

    String UPDATE_WIN_RATE = "update-win-rate";
    String GET_WIN_RATE = "get-win-rate";

    String UPDATE_FUND = "update-fund";
    String GET_FUND = "get-fund";
    String CLEAR_DATA = "clear-data";

    //CASHOUT
    String ACCEPT_RECEIPT = "accept";


    // bans
    String BANS = "bans";
    String UN_BANS = "un-bans";
}
