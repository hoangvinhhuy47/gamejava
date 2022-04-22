package games.api;

import games.core.application.GameServer;
import games.core.application.channel.TaskScheduler;
import games.core.database.MongoManager;
import games.core.database.document.DFundHistory;
import games.core.database.model.MFundHistory;
import games.core.exception.currency.CannotLessThanZero;
import games.shootingfish.GameLobby;
import games.shootingfish.GameRoom;
import games.shootingfish.GameService;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.manager.FundManager;
import games.tx.TaiXiuConst;
import games.tx.TXGame;
import games.tx.room.TXRoom;
import games.system.SystemService;
import libs.util.LogFactory;
import org.apache.log4j.Logger;
import org.bson.Document;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SystemsSection implements IApiSection {
    private static SystemsSection instance;
    private GameService gameService;

    public static SystemsSection getInstance() {
        if (instance == null) instance = new SystemsSection();
        return instance;
    }

    SystemsSection() {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        systemService = SystemService.getInstance();
        gameService = GameService.getInstance();
    }
    private static Logger logger;

    SystemService systemService;

    @Override
    public ApiReturnObject process(String action, Map<String, Object> data) {
        ApiReturnObject apiReturnObject = new ApiReturnObject();
        switch (action){
            case UPDATE_MAINTAIN_MODE:
                try{
                    boolean status = Boolean.valueOf((String) data.get("status"));
                    updateMaintainMode(status);
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case GET_CURRENT_MAINTAIN_STATUS:
                try{
                    apiReturnObject.setData(getMaintainStatus());
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case UPDATE_IAP_STATUS:
                try{
                    boolean status = Boolean.valueOf((String) data.get("status"));
                    updateIAPStatus(status);
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case GET_IAP_STATUS:
                try{
                    apiReturnObject.setData(getIAPStatus());
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;

            case UPDATE_WIN_RATE:
                try{
                    apiReturnObject.setData(updateWinRate(
                            Double.valueOf((String) data.get("fish_normal")),
                            Double.valueOf((String) data.get("fish_vip")),
                            Double.valueOf((String) data.get("fish_super_vip")),
                            Double.valueOf((String) data.get("slot_ocean_normal")),
                            Double.valueOf((String) data.get("slot_ocean_rich")),
                            Double.valueOf((String) data.get("slot_ocean_vip")),
                            Double.valueOf((String) data.get("slot_tk_normal")),
                            Double.valueOf((String) data.get("slot_tk_rich")),
                            Double.valueOf((String) data.get("slot_tk_vip"))
                    ));
                } catch (Exception e)
                {
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case GET_WIN_RATE:
                try{
                    apiReturnObject.setData(getWinRate());
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;

            case UPDATE_FUND:
                try
                {
                    boolean isDealer = Boolean.valueOf(String.valueOf(data.get("is_dealer")));
                    apiReturnObject.setData(
                            updateFund(
                                Integer.valueOf(String.valueOf(data.get("action_type"))),
                                Integer.valueOf(String.valueOf(data.get("game_id"))),
                                Integer.valueOf(String.valueOf(data.get("room_id"))),
                                Long.valueOf(String.valueOf(data.get("value"))),
                                String.valueOf(data.get("admin")),
                                isDealer
                            )
                    );
                    apiReturnObject.setData(true);
                } catch (CannotLessThanZero cannotLessThanZero){
                    apiReturnObject.setCode(cannotLessThanZero.getErrorCode());
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case GET_FUND:
                try{
                    int gameId = Integer.valueOf(String.valueOf(data.get("game_id")));
                    apiReturnObject.setData(getGameRooms(gameId));
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case CLEAR_DATA:
                try{
                    apiReturnObject.setData(clearData());
                } catch (Exception e){
                    e.printStackTrace();
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            default:
                apiReturnObject.setCode(ApiResponseConst.NOT_FOUND);
                break;
        }
        return apiReturnObject;
    }

    private void updateMaintainMode(boolean status){
        logger.info("Update main tain mode - " + status);
        systemService.setServerMaintance(status);
    }

    private boolean getMaintainStatus(){
        return systemService.isServerMaintance();
    }

    private void updateIAPStatus(boolean status){
        systemService.setEnableIAP(status);
    }

    private boolean getIAPStatus(){
        return systemService.isEnableIAP();
    }

    private boolean updateWinRate(
            double fishNormal, double fishVip, double fishSuperVip,
            double slotNormal, double slotRich, double slotVip,
            double slotTKNormal, double slotTKRich, double slotTKVip
    ){
        gameService.setRateKillFishByRoomType(RoomType.NORMAL, (float)fishNormal);
        gameService.setRateKillFishByRoomType(RoomType.VIP, (float)fishVip);
        gameService.setRateKillFishByRoomType(RoomType.SUPER_VIP, (float)fishSuperVip);
        return true;
    }

    private boolean updateFund(int actionType, int gameId, int roomId,
            long value, String admin, boolean isDealer)
    {
        long beforeValue;
        switch (gameId){
            case GameLobby.GAME_ID:
                GameRoom gameRoom = GameLobby.getInstance().getRoom(roomId);
                beforeValue = FundManager.getInstance().getFund(gameRoom.getRoomType());

                if (actionType == DFundHistory.TYPE_IN)
                {
//                    FundManager.getInstance().addMoney(value, gameRoom.getRoomType());
                    FundManager.getInstance().addMoneyFund(value, gameRoom.getRoomType());
                    FundManager.getInstance().saveToMongo();
                    MFundHistory.getInstance().logAddFund(gameId, roomId, beforeValue, value, admin);
                }
                else
                {
                    if (isDealer)
                    {
                        if (gameRoom.getRoomFund().getDealerAmount() - value < 0)
                        {
                            throw new CannotLessThanZero();
                        }
                        beforeValue = FundManager.getInstance().getFundDealer(gameRoom.getRoomType());
                        gameRoom.getRoomFund().subDealer(value);

                    }
                    else
                    {
                        if (gameRoom.getRoomFund().getAmount() - value < 0){
                            throw new CannotLessThanZero();
                        }
                        FundManager.getInstance().subMoney(value, gameRoom.getRoomType());
                        FundManager.getInstance().saveToMongo();
                    }

                    MFundHistory.getInstance().logSubFund(gameId, roomId, beforeValue, value, admin, isDealer);
                }
                break;
            case TaiXiuConst.TX_ID:
                TXRoom TXRoom = TXGame.getInstance().getRoomById(roomId);
                beforeValue = TXRoom.getRoomFund().getAmount();
                if (actionType == DFundHistory.TYPE_IN){
                    TXRoom.getRoomFund().plus(value);
                    MFundHistory.getInstance().logAddFund(gameId, roomId, beforeValue, value, admin);
                } else {
                    if (isDealer){
                        beforeValue = TXRoom.getRoomFund().getDealerAmount();
                        if (TXRoom.getRoomFund().getDealerAmount() - value < 0){
                            throw new CannotLessThanZero();
                        }
                        TXRoom.getRoomFund().subDealer(value);
                    } else {
                        if (TXRoom.getRoomFund().getAmount() - value < 0){
                            throw new CannotLessThanZero();
                        }
                        TXRoom.getRoomFund().sub(value);
                    }
                    MFundHistory.getInstance().logSubFund(gameId, roomId, beforeValue, value, admin, isDealer);
                }
                break;
        }
        return true;
    }

    private List<Document> getGameRooms(int gameId){
        List<Document> result = new ArrayList<>();

        switch (gameId){
            case GameLobby.GAME_ID:
                GameLobby.getInstance().getListGameRoom().values().forEach(gameRoom -> {
                    result.add(gameRoom.getRoomInfoDocument());
                });
                break;
            case TaiXiuConst.TX_ID:
                TXGame.getInstance().getRooms().values().forEach(room -> {
                    result.add(room.getRoomInfoDocument());
                });
                break;
            case TaiXiuConst.TX_NORMAL_ROOM_ID:
                TXGame.getInstance().getRooms().values().forEach(room -> {
                    result.add(room.getRoomInfoDocument());
                });
                break;
        }
        return result;
    }

    private Document getWinRate(){
        logger.info("Get win rate");
        Document result = new Document();
        ArrayList<Document> fishingGame = new ArrayList<>();

        result.append("fish", fishingGame);

        for (GameRoom gameRoom : GameLobby.getInstance().getListGameRoom().values()) {
            Document roomData = new Document("type", gameRoom.getRoomType().getRoomName()).append("rate", gameRoom.getRoomType().getRateKillFish());
            fishingGame.add(roomData);
        }
        return result;
    }

    private boolean clearData() throws MessagingException {
        try {
            // Tắt server trước khi drop database
            // Vì nếu để server hoạt động thì data đang lưu trên ram sẽ write xuống db

            // Tắt tất cả các task đang run
            TaskScheduler.getInstance().shutDown();
            // Tắt gameServer
            GameServer.getInstance().stopGameServer();

            // Drop database
            MongoManager.getInstance().getDatabase().drop();

            return true;
        } catch (Exception e){
            return false;
        } finally {
            // Tắt api server
            GameServer.getInstance().stopAPIServer();
        }

    }
}
