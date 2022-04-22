package games.tx;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.*;
import games.core.datatype.HashPlayer;
import games.core.exception.game.*;
import games.core.proto.ProtoSerializer;
import games.core.user.User;
import games.tx.pool.TXPlayerPool;

import games.tx.room.TXRoom;
import libs.util.data.HashByInt;
import message.TaiXiuGameProto;
import message.XProtos;

import java.util.*;

import static games.core.constant.ServerCommandConstant.TAI_XIU_COMMAND;


public abstract class TaiXiuGame implements ProtoSerializer<TaiXiuGameProto.TXGameInfo>, Runnable {


    protected TXPlayerPool playerPool = new TXPlayerPool();

    protected HashPlayer<TXPlayer> players = new HashPlayer<>();

    protected HashByInt<TXRoom> rooms = new HashByInt<>();

    public long totalBetAmountTai = 0;
    public long virtualTotalBetAmountTai = 0;
    public int totalPlayerBetTai = 0;
    public int virtualTotalPlayerBetTai = 0;

    private long virtualMoney;
    private int virtualPeople;

    public long totalBetAmountXiu = 0;
    public long virtualTotalBetAmountXiu = 0;
    public int totalPlayerBetXiu = 0;
    public int virtualTotalPlayerBetXiu = 0;

    public boolean isTai;
    public boolean isXiu;
    public boolean didTaiBet;
    public boolean didXiuBet;
    public TaiXiuGameProto.BetHistory.Builder bigHistory = TaiXiuGameProto.BetHistory.newBuilder();
    public TaiXiuGameProto.BetHistory.Builder smallHistory = TaiXiuGameProto.BetHistory.newBuilder();

    public long bigBetAmout;
    public long smallBetAmout;



    public long playerMoneyLossFund = 0;

    //add new
    private int maxSession = 100000000;
    //

    private int TimeCountdownSendToAllPlayer = TaiXiuConst.ORIGIN_TIME;
    private int TimeCountdownToStartNewGame = TaiXiuConst.ORIGIN_TIME_START_NEW_GAME;
    private int delayTime;
    private int botCreationTime = 2;
    private int currentTime = 0;

    MoneyService moneyService;


    public long lastTimeUpdate = 0;
    //
    public int lastTimeUpdate1 = 0;
    // Khoa add

    private List<String> ServerNOTIFICATION = new ArrayList<String>();
    private double totalTimePlaying = 0.0f;
    private float stateTimePlaying = 0.0f;
    private int oneSecCount = 0;
    private int twoSecCount = 0;
    private int threeSecCount = 0;
    private int fourSecCount = 0;
    private int fiveSecCount = 0;
    boolean checkiftype = false;

//    List<Integer> SUPERFinalResult = new ArrayList<Integer>();
    public List<Integer> FinalResult = new ArrayList<Integer>();

//    List<Integer> SUPERDice1Result = new ArrayList<Integer>();
//    List<Integer> SUPERDice2Result = new ArrayList<Integer>();
//    List<Integer> SUPERDice3Result = new ArrayList<Integer>();
    public List<Integer> Dice1Result = new ArrayList<Integer>();
    public List<Integer> Dice2Result = new ArrayList<Integer>();
    public List<Integer> Dice3Result = new ArrayList<Integer>();

//    List<Boolean> SUPERSumTaiXIu = new ArrayList<Boolean>();
//    public List<Boolean> SumTaiXIu = new ArrayList<Boolean>(); // Tai = true , Xiu = false

    public List<Integer> FinalResultGrapth3 = new ArrayList<Integer>();
    public List<Integer> FinalResultGrapth4 = new ArrayList<Integer>();
    //-----------------------------------------------


    public static int session = 0;

    private boolean isReset = false;

    private Set<Integer> listRand = new HashSet<>();

    private int min = 40;
    private int max = TaiXiuConst.ORIGIN_TIME - min;
    /**
     * Tổng tiền player đang đặt
     */
    private Money totalStake = new Money(0, MoneyType.GOLD);

    public TaiXiuGame() {
        initRooms();
        moneyService = MoneyService.getInstance();
    }

    protected abstract void initRooms();

    public void Start()
    {
        //lastTimeUpdate = System.currentTimeMillis();
        //
        //TaskScheduler.getInstance().scheduleAtFixedRate(new Main(), 0, 1000, TimeUnit.MILLISECONDS);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run()
            {
                Update();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 1000);
    }



//    private class Main implements Runnable
//    {
//        @Override
//        public void run() {
//            Long now = System.currentTimeMillis();
//            double deltaTime = (now - lastTimeUpdate) / 1000.0f;
//            lastTimeUpdate = now;
//            try {
//                //Update(deltaTime);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }


    public void Update()
    {
        TimeCountdownSendToAllPlayer --;
        if (TimeCountdownSendToAllPlayer > 0)
        {
            // Trong khoảng thời gian được phép đặt cược
            if (TimeCountdownSendToAllPlayer > 3)
            {
                currentTime ++;
                // Tạo bot khi có ng đặt cược
                if (currentTime == botCreationTime)
                {
                    Random random = new Random();
                    int numberPeopleDifferent = random.nextInt(2);
                    CreateBot(numberPeopleDifferent);

                    // Thời gian tạo bot
                    Random rand = new Random();
                    botCreationTime = 1 + rand.nextInt(3); //random 1 - 4
                    currentTime = 0;
                }

                // Tạo bot theo time
                else
                {
                    Random rand = new Random();
                    int count = min + rand.nextInt(max);
                    listRand.add(count);
                    CheckCreateBot(listRand);
                }
            }
        }

        else  if (TimeCountdownSendToAllPlayer == 0)
        {
            delayTime = TaiXiuConst.ORIGIN_DELAY_TIME;
        }
        else
        {
            delayTime++;
            if (delayTime == TaiXiuConst.CLIP_DURATION)
            {
                TaiXiuCommand.instance.handleDiceResults();
            }

            else if (delayTime > TaiXiuConst.CLIP_DURATION)
            {
                TimeCountdownToStartNewGame --;

                if (TimeCountdownToStartNewGame == 1)
                {
                    session++;
                    TaiXiuCommand.instance.CreateMongoSession(session);
                }
            }
        }

        SendRoomInfoToClient();


        // Cần phải gọi sau SendRoomInfoToClient
        if (TimeCountdownToStartNewGame == 0 && TimeCountdownSendToAllPlayer < 0)
        {
            ResetParameters();
        }
    }

    private void CheckCreateBot(Set<Integer> listTime)
    {
        for (int time : listTime)
        {
            if (TimeCountdownSendToAllPlayer == time)
            {
                CreateBot();
                min /= 2;
            }
        }
    }

    private void CreateBot()
    {
        Random rand = new Random();
        int virtualPeopleBetXiu = rand.nextInt(6);

        if (virtualPeopleBetXiu != 0)
        {
            long randomMoney = 3 + rand.nextInt(100);
            virtualMoney = randomMoney * 10000;

            virtualTotalBetAmountXiu += virtualMoney;
            virtualTotalPlayerBetXiu += virtualPeopleBetXiu;
        }

        int virtualPeopleBetTai = rand.nextInt(6);
        if (virtualPeopleBetTai != 0)
        {
            long randomMoney2 = 3 + rand.nextInt(100);
            virtualMoney = randomMoney2 * 10000;

            virtualTotalBetAmountTai += virtualMoney;
            virtualTotalPlayerBetTai += virtualPeopleBetTai;
        }
    }

    private void CreateBot(int numberPeopleDifferent)
    {
        if (totalBetAmountTai > totalBetAmountXiu)
        {
            if (virtualTotalPlayerBetXiu < totalPlayerBetTai + numberPeopleDifferent)
            {
                Random rand = new Random();
                int scale = 20 + rand.nextInt(10); // 20% - 30%
                virtualPeople = 1 + rand.nextInt(2);

                long offset = (totalBetAmountTai * scale) / 100;

                int numberRand = rand.nextInt(2);
                if (numberRand == 1)
                {
                    virtualMoney = totalBetAmountTai + offset;
                }
                else
                {
                    virtualMoney = totalBetAmountTai - offset;
                }

                if (virtualMoney < 3000)
                {
                    long randomMoney = 3 + rand.nextInt(9);
                    virtualMoney = randomMoney * 1000;
                }

                virtualTotalBetAmountXiu += virtualMoney;
                virtualTotalPlayerBetXiu += virtualPeople;
            }
        }
        else if (totalBetAmountXiu > totalBetAmountTai)
        {
            if (virtualTotalPlayerBetTai < totalPlayerBetXiu + numberPeopleDifferent)
            {
                Random rand = new Random();
                int scale = 20 + rand.nextInt(10); // 20% - 30%

                virtualPeople = 1 + rand.nextInt(2);

                long offset = (totalBetAmountXiu * scale) / 100;

                int numberRand = rand.nextInt(2);
                if (numberRand == 1)
                {
                    virtualMoney = totalBetAmountXiu + offset;
                }
                else
                {
                    virtualMoney = totalBetAmountXiu - offset;
                }

                if (virtualMoney < 3000)
                {
                    long randomMoney = 3 + rand.nextInt(9);
                    virtualMoney = randomMoney * 1000;
                }

                virtualTotalBetAmountTai += virtualMoney;
                virtualTotalPlayerBetTai += virtualPeople;
            }
        }
    }

    private void SendRoomInfoToClient() {
        TaiXiuGameProto.TXRoomInfo.Builder builder = TaiXiuGameProto.TXRoomInfo.newBuilder();
        builder.setTotalBetAmountTai(virtualTotalBetAmountTai);
        builder.setTotalBetAmountXiu(virtualTotalBetAmountXiu);
        builder.setTotalPlayerBetTai(virtualTotalPlayerBetTai);
        builder.setTotalPlayerBetXiu(virtualTotalPlayerBetXiu);
        builder.setCountDown((int) TimeCountdownSendToAllPlayer);
        builder.setDelayTime(delayTime);
        builder.setTimeStartNewGame((int) TimeCountdownToStartNewGame);
        builder.setSession(session);

        ByteString newMessage = builder.build().toByteString();
        SendToAll(newMessage, TXActionConst.UPDATE);
    }


    public void AddValuetoFinalResult(int finalresult, int Dice1, int Dice2, int Dice3)
    {
        if (FinalResult.size() > 17)
        {
            FinalResult.remove(0);
        }
        FinalResult.add(finalresult);
        // -----------------------------------

        if (Dice1Result.size() > 17)
        {
            Dice1Result.remove(0);
        }
        Dice1Result.add(Dice1);
        // ------------------------------------

        if (Dice2Result.size() > 17) {
            Dice2Result.remove(0);
        }
        Dice2Result.add(Dice2);
        //------------------------------------

        if (Dice3Result.size() > 17) {
            Dice3Result.remove(0);
        }
        Dice3Result.add(Dice3);
        // -------------------------------------
        if (FinalResultGrapth3.size() > 70) {
            FinalResultGrapth3.remove(0);
        }
        FinalResultGrapth3.add(finalresult);
        //-------------------------------------
        if (FinalResultGrapth4.size() > 130) {
            FinalResultGrapth4.remove(0);
        }

        FinalResultGrapth4.add(finalresult);
        //-------------------------------------

    }

    private void ResetParameters()
    {
        totalBetAmountTai = totalBetAmountXiu = totalPlayerBetTai = totalPlayerBetXiu = 0;

        TimeCountdownSendToAllPlayer = TaiXiuConst.ORIGIN_TIME;
        delayTime = TaiXiuConst.ORIGIN_DELAY_TIME;
        TimeCountdownToStartNewGame = TaiXiuConst.ORIGIN_TIME_START_NEW_GAME;

        didTaiBet = false;
        didXiuBet = false;

        virtualTotalPlayerBetXiu = 0;
        virtualTotalPlayerBetTai = 0;
        virtualTotalBetAmountXiu = 0;
        virtualTotalBetAmountTai = 0;

        TaiXiuCommand.instance.isCheck = false;

        min = 30;
        max = TaiXiuConst.ORIGIN_TIME - min;

        listRand.clear();
        listRand = new HashSet<>();
    }

    protected void addRoom(int id) {
        this.rooms.put(id, new TXRoom(id));
    }

    private void checkPlayerExist(TXPlayer player) throws NotFoundPlayerInGame {
        if (!isPlayerExist(player)) {
            throw new NotFoundPlayerInGame(player.getUser());
        }
    }

    /**
     * Thêm 1 user vào game
     * - neu no co roi thi remove thang cu ra(thang cu bi ket ban)
     *
     * @param user instance of User class
     */
    public void addPlayer(User user) {
        if (isPlayerExist(user)) {
            this.players.remove(user.getId());
        }
        TXPlayer newPlayer = this.createPlayer(user);
        this.players.putIfAbsent(newPlayer.getId(), newPlayer);
    }

    private TXPlayer createPlayer(User user) {
        TXPlayer playerFromPool = playerPool.shiftSync();
        playerFromPool.setUser(user);
        return playerFromPool;
    }

    /**
     * Kiểm tra xem user đó đã tồn tại trong game này hay chưa
     *
     * @param player instance của BasePlayer
     * @return true nếu đã tồn tại
     */
    public boolean isPlayerExist(TXPlayer player) {
        return players.containsKey(player.getId());
    }

    public boolean isPlayerExist(User user) {
        return players.containsKey(user.getId());
    }


    public void joinRoom(User user, TXRoom room) {
        TXPlayer player = getPlayer(user);
        room.addPlayer(player);
        player.setRoom(room);
    }

    public void SubMoney(long betAmount, User user, MoneyType moneyType) {
        Money money = new Money(betAmount, moneyType);
        MoneyService.getInstance().subMoneyAndSaveMongo(money, user, true, true);
    }

    public void AddMoney(long winnings, User user, MoneyType moneyType)
    {
        Money money = new Money(winnings, moneyType);
        user.SetMoney(money.getAmount());
        MoneyService.getInstance().addMoneyToSave(money, user, true, "Tai_Xiu");
    }

    //add new
    public void AddMoneyWin(long moneyWin, User user, MoneyType moneyType) throws NoSuchFieldException, IllegalAccessException {
        Money money = new Money(moneyWin, moneyType);
        user.addMoneyWin(money);
        user.saveUserMoney();
    }

    //
    public void SendToAll(ByteString responseBeanByteString, int responseCode) {
        for (TXPlayer player : players.values()) {
            if (player != null) {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }

    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode) {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(TAI_XIU_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);

        ctx.writeAndFlush(responseMessage.build());
    }

    public void leaveRoom(User user)
            throws NotFoundPlayerInGame,
            NotFoundPlayerInRoom,
            CannotRemovePlayerInRoom {
        TXPlayer player = getPlayer(user);
        TXRoom room = player.getRoom();
        room.removePlayer(player);
    }

    public void removePlayer(User user) throws NotFoundPlayerInGame {
        playerPool.putSync(getPlayer(user));
        this.players.remove(user.getId());
    }

    public TXRoom getRoomById(int id) throws RoomNotExist {
        TXRoom room = this.rooms.get(id);
        if (room == null) {
            throw new RoomNotExist(id);
        }
        return room;
    }

    public TXPlayer getPlayer(User user) throws NotFoundPlayerInGame {
        if (!players.containsKey(user.getId())) {
            throw new NotFoundPlayerInGame(user);
        }
        return this.players.get(user.getId());
    }

    public HashByInt<TXRoom> getRooms() {
        return this.rooms;
    }

    @Override
    public Message.Builder parseProtoBuilder() {
        TaiXiuGameProto.TXGameInfo.Builder builder = TaiXiuGameProto.TXGameInfo.newBuilder();
        getRooms().values().forEach(txRoom -> builder.addRoom(txRoom.parseProtoBuilder()));

        return builder;
    }

    public ByteString parseMessageToByteString() {
        TaiXiuGameProto.TXGameInfo.Builder builder = TaiXiuGameProto.TXGameInfo.newBuilder();
        getRooms().values().forEach(txRoom -> builder.addRoom(txRoom.parseProtoBuilder()));

        return builder.build().toByteString();
    }


    public static void main(String[] args) {
        for (int i = 0; i < 20000; i++) {
            if (i < 10000) {
                User user = User.createFakeUser(i);
                TXRoom room = (TXRoom) TXGame.getInstance().getRooms().values().toArray()[0];
                TXGame.getInstance().addPlayer(user);
                TXGame.getInstance().joinRoom(user, room);
            } else {
                User user = User.createFakeUser(i - 10000);
                TXGame.getInstance().leaveRoom(user);
                TXGame.getInstance().removePlayer(user);
            }

        }
    }
}
