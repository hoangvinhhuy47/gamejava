package games.slot;

import com.google.protobuf.Message;
import games.core.application.ServerConfig;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.TransactionData;
import games.core.common.money.TransactionStatus;
import games.core.datatype.HashPlayer;
import games.core.exception.game.*;
import games.core.proto.ProtoSerializer;
import games.core.user.User;
import games.slot.data.*;
import games.slot.exception.PlayerLineNotValid;
import games.slot.pool.SlotPlayerPool;
import games.slot.room.RoomFund;
import games.slot.room.RoomPot;
import games.slot.room.SlotRoom;
import libs.util.CRandom;
import libs.util.data.HashByInt;
import message.SlotGameProto;

import java.util.ArrayList;
import java.util.List;

public abstract class SlotGame implements ProtoSerializer<SlotGameProto.SlotGameInfo>
{
    private static final CRandom cRandom = new CRandom();
    private static SlotConfig slotConfig = SlotConfig.getInstance();
    private static ServerConfig serverConfig = ServerConfig.getInstance();
    public static HashByInt<MainSlotLine> lines = slotConfig.getSlotOceanLines();
    private static HashByInt<MainSlotReward> rewards = slotConfig.getSlotOceanRewards();
    private static HashByInt<SlotReel> reels = slotConfig.getSlotOceanReels();
    public static SlotGameConfig<MainSlotLine, MainSlotReward> config = new SlotGameConfig<>(lines, rewards, reels);

    private static int winJackpotRate = serverConfig.getSlotWinJackpot();
    private static int winRate = serverConfig.getSlotWinRate() * 100;
    private static int loseAllRate = serverConfig.getSlotLoseAllRate() * 100;


    protected SlotPlayerPool playerPool = new SlotPlayerPool();

    /**
     * Danh sách những user đang chơi game này
     */
    protected HashPlayer<SlotPlayer> players = new HashPlayer<>();

    protected HashByInt<SlotRoom> rooms = new HashByInt<>();

    public SlotGame() {
        initRooms();
    }

    protected abstract void initRooms();

    protected void addRoom(int id){
        this.rooms.put(id, new SlotRoom(id));
    }

    protected ArrayList<MainSlotLine> generateDefaultLines(){
        return new ArrayList<>(getLines().values());
    }

    protected HashByInt<MainSlotLine> getLines(){
        return config.getLines();
    }

    protected HashByInt<MainSlotReward> getRewards(){
        return config.getRewards();
    }

    protected HashByInt<SlotReel> getReels(){
        return config.getReels();
    }

    private MainResultBoard generateJackpotResultBoard(SlotPlayer player){
        return new MainResultBoard(
                player, config,
                slotConfig.getRandomMainJackpotRawBoardByLines(player.getLines())
        );
    }

    public MainResultBoard generateResultBoard(SlotPlayer player){
        int numLine = player.getNumLine();
        int ranInt = cRandom.randInt(1, 10000);
        MainResultBoard resultBoard = new MainResultBoard(player, config);
        int maxTries = 200;
        int counter = 0;
        int randJackpot = cRandom.randInt(10, 20);
        int realWinRate = numLine * winRate / 20;
        if (ranInt <= winJackpotRate && player.getNumLine() >= randJackpot){
            resultBoard = generateJackpotResultBoard(player);
        } else if (ranInt < realWinRate){
            while (
                    resultBoard.getMoneyCanWin() < player.getTotalStake().getAmount()
                            && resultBoard.getFreeSpinResult().getFreeSpinAmount() < 2
                            && counter < maxTries || resultBoard.getJackpotMoneyWin() > 0
            ){
                resultBoard = new MainResultBoard(player, config);
                counter++;
            }
        } else {
            if (player.getNumLine() > 5){
                if (ranInt < loseAllRate){
                    while (
                            resultBoard.getMoneyCanWin() != 0
                                    || resultBoard.getFreeSpinResult().getFreeSpinAmount() > 0
                                    && counter < maxTries
                    ){
                        resultBoard = new MainResultBoard(player, config);
                        counter++;
                    }
                } else {
                    while (resultBoard.getMoneyCanWin() <= 0
                            || resultBoard.getMoneyCanWin() >= player.getTotalStake().getAmount()
                            || resultBoard.getFreeSpinResult().getFreeSpinAmount() > 0
                            && counter < maxTries
                    ){
                        resultBoard = new MainResultBoard(player, config);
                        counter++;
                    }
                }
            } else {
                while (
                        resultBoard.getMoneyCanWin() > player.getTotalStake().getAmount()
                                && counter < maxTries
                ){
                    resultBoard = new MainResultBoard(player, config);
                    counter++;
                }
            }

        }

        // Tiền giới hạn player được phép lãi
        long limitFundAmount = player.getRoom().getLimitFundAmount();

        // Số tiền lãi/lỗ của player. (<0 => lỗ)
        long playerDeltaAmount = resultBoard.getMoneyCanWin();// - player.getTotalStake().getAmount();

        // Nếu player lãi mà số tiền nó lãi lại vượt quá giới hạn quỹ (currentFund-minJackpot)
        // Thì quay cho đến khi nào nó không lãi hoặc số tiền nó lãi không vượt quá giới hạn quỹ thì thôi
        // Nếu số lượt bonus > 9
        // Nếu đang còn lượt quay free thì không cho trúng freeSpin nữa

        counter = 0;
        while (
                (playerDeltaAmount > 0 && playerDeltaAmount > limitFundAmount)
                        || resultBoard.getTotalBonusReward() > 9
                || (player.isFreeSpin() && resultBoard.getFreeSpinResult().getFreeSpinAmount() > 0)
                        && counter < maxTries
        ){
            counter++;
            resultBoard = new MainResultBoard(player, config);
            playerDeltaAmount = resultBoard.getMoneyCanWin(); //- player.getTotalStake().getAmount();
        }
//
//        while (resultBoard.getJackpotMoneyWin() == 0){
//            resultBoard = new MainResultBoard(player, config);
//        }
//        if (resultBoard.getJackpotMoneyWin() > 0){
//            System.out.println("Win jackpot on board");
//        }

        return resultBoard;
    }

    public MainResultBoard spin(SlotPlayer player) throws NotFoundPlayerInGame, PlayerNotEnoughMoney
    {
        if (isPlayerExist(player))
        {
            player.executeBonusWinResult(); // Phòng trường hợp lượt quay trước chưa execute mở bonus

            if (player.isFreeSpin()){
                // Không trừ tiền mà trừ lượt quay free
                player.subFreeSpin(1);
            } else {
                // Lấy tổng Gold mà player mất cho lượt quay này
                Money goldPlayerStake = player.getTotalStake();

                // Trừ tiền user
                TransactionData data = MoneyService.getInstance().subMoneyAndSaveMongo(goldPlayerStake, player.getUser(), false, true);
                if (data.getStatus() != TransactionStatus.SUCCESS){
                    throw new PlayerNotEnoughMoney(player.getUser(), goldPlayerStake.getAmount());
                }
            }

            MainResultBoard resultBoard = this.generateResultBoard(player);

            Money playerWinMoney = resultBoard.getMoney();
            RoomFund roomFund = player.getRoom().getRoomFund();
            if (playerWinMoney.getAmount() > 0)
            {
                player.getUser().SetMoney(playerWinMoney.getAmount());
                MoneyService.getInstance().addMoneyToSave(playerWinMoney, player.getUser(), true, "Slot_Game");

                // Giải phóng jackpot
                player.releaseJackpotAmount();
            }

            if (resultBoard.getBonusWinResult().isWin() && !resultBoard.getBonusWinResult().isWin5Item()){
                player.setBonusWinResult(resultBoard.getBonusWinResult());
            }

            if (player.isFreeSpin()){
                // Nếu nó là freespin thì quỹ chỉ có thể lỗ hoặc hoà
                if (playerWinMoney.getAmount() > 0){ // Tức lỗ, =0 hoà thì không cần làm gì
                    roomFund.sub(playerWinMoney);
                }
            } else {

                // Cộng lượt freeSpin cho nó
                if (resultBoard.getFreeSpinResult().isWin()){
                    player.plusFreeSpin(resultBoard.getFreeSpinResult().getFreeSpinAmount());
                }
                RoomPot roomPot = player.getRoom().getRoomPot();
                // Cộng tiền vào hũ
                // Nếu hũ chưa max mới cộng
                Money moneyToPot = player.getAddToPotStakeMoney();
                if (!roomPot.isReachMax()) {
                    roomPot.plus(moneyToPot);
                }

                // Cộng tiền vào quỹ chủ game
                Money moneyToFundDealer = player.getAddToFundDealerStakeMoney();
                roomFund.plusDealer(moneyToFundDealer.getAmount());

                // Tiền lãi/lỗ của player. ví dụ: đặt 100 ăn được 60 => lỗ 40 (Không tính jackpot vì jackpot đã được tính riêng)
                long playerDeltaMoney = resultBoard.getNormalMoneyWin() - player.getTotalStake().getAmount();

                // Tiền lãi/lỗ của quỹ. Ví dụ player: đặt 100 ăn được 96 => quỹ lời 4 => nhưng
                //  do đưa vào hũ 5 rồi nên thực ra quỹ sẽ lỗ 1
                long fundDeltaMoney = -playerDeltaMoney;

                // trừ số tiền đã đưa vào hũ
                if (!roomPot.isReachMax()){
                    fundDeltaMoney -= moneyToPot.getAmount();
                }

                // Trừ số tiền đã đưa vào quỹ dealer
                fundDeltaMoney -= moneyToFundDealer.getAmount();

                if (fundDeltaMoney > 0){ // Nếu quỹ lãi
                    roomFund.plus(fundDeltaMoney);
                } else if (fundDeltaMoney < 0){ // Nếu quỹ lỗ
                    roomFund.sub(-fundDeltaMoney);
                }
            }

            return resultBoard;
        }
        return null;
    }

    public void updatePlayerLines(SlotPlayer player, List<Integer> lines) throws NotFoundPlayerInGame, PlayerLineNotValid {
        checkPlayerExist(player);
        player.updateLines(lines, config.getLines());
    }

    private void checkPlayerExist(SlotPlayer player) throws NotFoundPlayerInGame {
        if (!isPlayerExist(player)){
            throw new NotFoundPlayerInGame(player.getUser());
        }
    }

    /**
     * Thêm 1 user vào game
     * - neu no co roi thi remove thang cu ra(thang cu bi ket ban)
     * @param user instance of User class
     */
    public void addPlayer(User user) {
        if (isPlayerExist(user)){
            this.players.remove(user.getId());
        }
        SlotPlayer newPlayer = this.createPlayer(user);
        this.players.putIfAbsent(newPlayer.getId(), newPlayer);
    }

    private SlotPlayer createPlayer(User user) {
        SlotPlayer playerFromPool = playerPool.shiftSync();
        playerFromPool.setUser(user);
        return playerFromPool;
    }

    /**
     * Kiểm tra xem user đó đã tồn tại trong game này hay chưa
     * @param player instance của BasePlayer
     * @return true nếu đã tồn tại
     */
    public boolean isPlayerExist(SlotPlayer player){
        return players.containsKey(player.getId());
    }

    public boolean isPlayerExist(User user){
        return players.containsKey(user.getId());
    }

    public void joinRoom(User user, SlotRoom room)
    {
        SlotPlayer player = getPlayer(user);
        room.addPlayer(player);
        player.setRoom(room);
    }

    public void leaveRoom(User user)
            throws NotFoundPlayerInGame,
            NotFoundPlayerInRoom,
            CannotRemovePlayerInRoom {
        SlotPlayer player = getPlayer(user);
        SlotRoom room = player.getRoom();
        room.removePlayer(player);
        player.onLeaveRoom();
    }

    public void removePlayer(User user) throws NotFoundPlayerInGame {
        playerPool.putSync(getPlayer(user));
        this.players.remove(user.getId());
    }

    public SlotRoom getRoomById(int id) throws RoomNotExist {
        SlotRoom room = this.rooms.get(id);
        if (room == null){
            throw new RoomNotExist(id);
        }
        return room;
    }

    public SlotGameConfig<MainSlotLine, MainSlotReward> getConfig() {
        return config;
    }

    public SlotPlayer getPlayer(User user) throws NotFoundPlayerInGame {
        if (!players.containsKey(user.getId())) {
            throw new NotFoundPlayerInGame(user);
        }
        return this.players.get(user.getId());
    }

    public HashByInt<SlotRoom> getRooms(){
        return this.rooms;
    }

    @Override
    public Message.Builder parseProtoBuilder() {
        SlotGameProto.SlotGameInfo.Builder builder = SlotGameProto.SlotGameInfo.newBuilder();
        getRooms().values().forEach(slotRoom -> builder.addRoom(slotRoom.parseProtoBuilder()));
        return builder;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 20000; i++) {
            if (i < 10000){
                User user = User.createFakeUser(i);
                SlotRoom room = (SlotRoom)SlotOceanGame.getInstance().getRooms().values().toArray()[0];
                SlotOceanGame.getInstance().addPlayer(user);
                SlotOceanGame.getInstance().joinRoom(user, room);
            } else {
                User user = User.createFakeUser(i-10000);
                SlotOceanGame.getInstance().leaveRoom(user);
                SlotOceanGame.getInstance().removePlayer(user);
            }

        }
    }
}
