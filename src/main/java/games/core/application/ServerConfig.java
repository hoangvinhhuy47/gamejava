package games.core.application;

import libs.util.LoadConfig;

public class ServerConfig {
    private static ServerConfig instance;

    public static ServerConfig getInstance() {
        if (instance == null) instance = new ServerConfig();
        return instance;
    }
    private LoadConfig loadConfig;
    private ServerConfig(){
       loadConfig = new LoadConfig("server_config.json");
    }
    //Game server
    public String getGameServerHost(){
        return loadConfig.getValue("server>host");
    }
    public int getGameServerPort(){
        return Integer.valueOf(loadConfig.getValue("server>port"));
    }
    public String getGameServerType(){
        return loadConfig.getValue("server>type");
    }

    public String getWebServerHost(){
        return loadConfig.getValue("websocket>host");
    }
    public int getWebServerPort(){
        return Integer.valueOf(loadConfig.getValue("websocket>port"));
    }

    public boolean getWebServerSecure() {
        return Boolean.valueOf(loadConfig.getValue("websocket>secure"));
    }


    //MONGO

    public String getMongoHost(){
        return loadConfig.getValue("mongo>host");
    }

    public int getMongoPort(){
        return Integer.valueOf(loadConfig.getValue("mongo>port"));
    }


    public String getMongoUser(){
        return loadConfig.getValue("mongo>user");
    }

    public String getMongoPass(){
        return loadConfig.getValue("mongo>pass");
    }

    public String getMongoDBName(){
        return loadConfig.getValue("mongo>db");
    }

    public String getMongoDBToolsName(){
        return loadConfig.getValue("mongo>db_tools");
    }

    public String getApiHost(){ return loadConfig.getValue("api>address");}
    public int getApiPort(){return Integer.valueOf(loadConfig.getValue("api>port"));}
    public String getApiPrefixUrl(){return loadConfig.getValue("api>prefix");}
    //REDIS

    public String getRedisHost(){
        return loadConfig.getValue("redis>host");
    }

    public int getRedisPort(){
        return Integer.valueOf(loadConfig.getValue("redis>port"));
    }

    //Kafka
    public String getKafkaHost(){
        return loadConfig.getValue("kafka>host");
    }

    public String getKafkaPort(){
        return loadConfig.getValue("kafka>port");
    }

    public String getKafkaGroup() {return loadConfig.getValue("kafka>group");}

    //setting
    public String getIsMaintance() {
        return loadConfig.getValue("system>isInMaintain");
    }

    public String getIsEnableIAP() {
        return loadConfig.getValue("system>isEnableIAP");
    }

    public String getMoneyPromoteCode() {
        return loadConfig.getValue("system>moneyRewardPromote");
    }

    public String getMaxCardOnceIP() {
        return loadConfig.getValue("system>maxCardOnceIP");
    }

    public String getLinkFanpage() {
        return loadConfig.getValue("system>linkFanpage");
    }

    public int getMoneyRewardWhenRegister() {
        return Integer.valueOf(loadConfig.getValue("system>moneyRewardRegister"));
    }

    //so tien gioi han rut 1 ngay
    public int getMoneyCashoutLimitPerDay() {
        return Integer.valueOf(loadConfig.getValue("system>moneyLimitCashoutPerDay"));
    }

    public String getMaxCardPerDayOnceIP() {
        return loadConfig.getValue("system>maxCardPerDayOnceIP");
    }

    public String getMoneyRewardVideoAds() {
        return loadConfig.getValue("system>moneyRewardVideoAds");
    }

    public String getMaxCountVideoAds() {
        return loadConfig.getValue("system>maxCountVideoOnceDay");
    }

    public String getTicketWheelPromoteCode() {
        return loadConfig.getValue("system>ticketWheelRewardPromote");
    }

    public String getMaxFriendPromoteCode() {
        return loadConfig.getValue("system>maxFriendPromote");
    }

    public String getThe247UserName(){
        return loadConfig.getValue("the247>userName");
    }

    public String getThe247Password(){
        return loadConfig.getValue("the247>password");
    }

    public int getSlotWinJackpot(){
        return (int)Math.round(loadConfig.getValue("game_slot>win_jackpot", Double.class));
    }

    public int getSlotWinRate(){
        return (int)Math.round(loadConfig.getValue("game_slot>win_rate", Double.class));
    }

    public int getSlotLoseAllRate(){
        return (int)Math.round(loadConfig.getValue("game_slot>lose_all_rate", Double.class));
    }

    public  int getJackpotRate() {
        return (int)Math.round(loadConfig.getValue("fund>jackpot", Double.class));
    }

    public  int getFundRate() {
        return (int)Math.round(loadConfig.getValue("fund>fund", Double.class));
    }

    public  int getDealerRate() {
        return (int)Math.round(loadConfig.getValue("fund>dealer", Double.class));
    }
}
