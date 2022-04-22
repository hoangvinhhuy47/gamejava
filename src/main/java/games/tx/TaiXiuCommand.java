package games.tx;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import games.APISendMoney;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.MoneyType;
import games.core.constant.ServerCommandConstant;
import games.core.database.MongoManager;
import games.core.exception.game.NotFoundPlayerInGame;
import games.features.rank.RankService;
import games.shootingfish.datatype.RoomType;
import games.shootingfish.manager.FundManager;
import games.tx.room.TXRoom;
import message.TaiXiuGameProto;
import message.XProtos;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import static com.mongodb.client.model.Filters.eq;


public class TaiXiuCommand extends ICommand {
    public static TaiXiuCommand taiXiuCommand;

    public static TaiXiuCommand instance = null;

    private TaiXiuGame gameInstance;
    public static int dice1;
    public static int dice2;
    public static int dice3;
    public static int total;


    int numberGenerator = 1;

    private int betHistoryLimit = 30;

    private int countCollection;


    public boolean isCheck;

    long fund_dealer = 0;

    private String txBetHistory1 =  "txBetHistory1";


    public synchronized static TaiXiuCommand getInstance() {
        if (instance == null) {
            instance = new TaiXiuCommand(TXGame.getInstance());
        }
        return instance;
    }

    public void Start() {
        gameInstance.Start();
        TaiXiuGame.session = GetSessionFromMongo();
    }

    private static FundManager fundManager = FundManager.getInstance();
    private RoomType roomType;

    TaiXiuCommand(TaiXiuGame gameInstance) {
        this.gameInstance = gameInstance;
        taiXiuCommand = this;
    }


    @Override
    public int getCommandCode() {
        return ServerCommandConstant.TAI_XIU_COMMAND;
    }

    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException, NoSuchFieldException, IllegalAccessException
    {
        if (ctx.checkAuth())
        {
            int type = message.getBeanType();
            switch (type) {
                case TXActionConst.GAME_INFO:
                    this.handleGameInfo(ctx, message);
                    break;

                case TXActionConst.JOIN_ROOM:
                    this.handleJoinRoom(ctx, message);
                    break;

                case TXActionConst.LEAVE_ROOM:
                    this.handleLeaveGame(ctx, message);
                    break;

                case TXActionConst.CANCEL_BET:
                    handleCancelBet(ctx, message);
                    break;

                case TXActionConst.BET:
                    handleBet(ctx, message);
                    break;

                case TXActionConst.PAYMENT:
                    handlePayment(ctx, message);
                    break;

                //Add new
                case TXActionConst.TOP_WIN:
                    handleTopWin(ctx, message);
                    break;

                case TXActionConst.BET_HISTORY:
                    handleBetHistory(ctx, message);
                    break;

                case TXActionConst.GET_DICE_RESULT:
                    GetDiceResultWhenStart(ctx, message);
                    break;

                case TXActionConst.DICE_REULTS:
                    GetDiceResult(ctx, message);
                    break;
                case TXActionConst.SERVER_RESULT:
                    handleShowGraphWhewStart(ctx, message);
                    break;
            }
        }
    }

    private void GetDiceResultWhenStart(ChannelContextInfo ctx, XProtos.XMessage message) {
        TaiXiuGameProto.TXGetDiceResult.Builder builder = TaiXiuGameProto.TXGetDiceResult.newBuilder();

        builder.setDice1(dice1);
        builder.setDice2(dice2);
        builder.setDice3(dice3);

        TaiXiuGame.session = GetSessionFromMongo();
        builder.setTotal(TaiXiuGame.session);


        ByteString newMessage = builder.build().toByteString();
        gameInstance.SendMessage(ctx, newMessage, TXActionConst.GET_DICE_RESULT);
    }

    private void GetDiceResult(ChannelContextInfo ctx, XProtos.XMessage message) {
        TaiXiuGameProto.DiceHistory.Builder builder = TaiXiuGameProto.DiceHistory.newBuilder();


        builder.setDice1(dice1);
        builder.setDice2(dice2);
        builder.setDice3(dice3);

        ByteString newMessage = builder.build().toByteString();
        gameInstance.SendMessage(ctx, newMessage, TXActionConst.DICE_REULTS);

    }

    // Khoa add
    private void AddValueToList_For_ClientGraph(int total_graph, int dice1_graph, int dice2_graph, int dice3_graph)
    {
        // Add result at server
        gameInstance.AddValuetoFinalResult(total_graph, dice1_graph, dice2_graph, dice3_graph);

        //// Add result at Mongodb
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyy - HH:mm");
//        LocalDateTime now = LocalDateTime.now();
//        MongoDatabase mongo = MongoManager.getInstance().getDatabase();
//        MongoCollection collection = mongo.getCollection("txResult");
//        if (collection == null) {
//            mongo.createCollection("txResult");
//        }
//
//        collection.insertOne(new Document()
//                .append("final", total_graph)
//                .append("dice1", dice1_graph)
//                .append("dice2", dice2_graph)
//                .append("dice3", dice3_graph)
//                .append("RealTime", dtf.format(now))
//                .append("_id", String.format("#%d", (collection.count() + numberGenerator))));
    }

    public void CreateMongoSession(int session)
    {
        MongoDatabase mongo = MongoManager.getInstance().getDatabase();
        MongoCollection<Document> collection = mongo.getCollection("txSession");

        if (collection == null)
            mongo.createCollection("txSession");

        Document doc = collection.find().first();
        if (doc != null)
        {
            Bson filter = new Document("_id", 1);
            Bson newValue = new Document("session", session);
            Bson updateOperationDoc = new Document("$set", newValue);
            collection.updateOne(filter, updateOperationDoc);
        }
        else
        {
            collection.insertOne(new Document()
                    .append("_id", 1)
                    .append("session", session));
        }
    }


    public int GetSessionFromMongo()
    {
        int session = 0;

        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection("txSession");

        if (collection != null) {
            Document doc = collection.find().sort(Sorts.descending("session")).first();
            if (doc != null)
                session = (int) doc.get("session");
        }

        return session;
    }

    private void handleBetHistory(ChannelContextInfo ctx, XProtos.XMessage message)
    {
        long betAmountBig = ctx.getUser().taiXiuGame.bigHistory.getBet();
        long betAmountSmall = ctx.getUser().taiXiuGame.smallHistory.getBet();

        if (betAmountBig > 0)
            CreateMongoBetHistory(ctx.getUser().taiXiuGame.bigHistory, ctx);

        if (betAmountSmall > 0)
            CreateMongoBetHistory(ctx.getUser().taiXiuGame.smallHistory, ctx);

        TaiXiuGameProto.ListBetHistory.Builder listHistory = GetBetHistoryFromMongoDB(ctx.getUserId());
        ByteString newMessage = listHistory.build().toByteString();
        gameInstance.SendMessage(ctx, newMessage, TXActionConst.BET_HISTORY);

        ctx.getUser().taiXiuGame.bigHistory = TaiXiuGameProto.BetHistory.newBuilder();
        ctx.getUser().taiXiuGame.smallHistory = TaiXiuGameProto.BetHistory.newBuilder();
    }

    private void CreateMongoBetHistory(TaiXiuGameProto.BetHistory.Builder newBetHistory, ChannelContextInfo ctx)
    {
        int GetCountIDTXBetHistory_justincase = GetMaxIdBetHistory(ctx.getUserId()) + 1;

        MongoDatabase mongo = MongoManager.getInstance().getDatabase();
        MongoCollection<Document> collection = mongo.getCollection(txBetHistory1);
        if (collection == null)
        {
            mongo.createCollection(txBetHistory1);
        }
        collection.insertOne(new Document()
                .append("id", GetCountIDTXBetHistory_justincase)
                .append("UserID", ctx.getUserId())
                .append("Session", newBetHistory.getSession())
                .append("Time", newBetHistory.getTime())
                .append("Bet", newBetHistory.getCuaDat())
                .append("Result", newBetHistory.getResultDice())
                .append("BetAmount", newBetHistory.getBet())
                .append("Refund", newBetHistory.getRefund())
                .append("Receive", newBetHistory.getReceive()));

        ArrayList<Document> documents = collection.find(eq("UserID", ctx.getUserId())).into(new ArrayList<>());
        while (documents.size() > betHistoryLimit)
        {
            int minId = GetMinIdBetHistory(ctx.getUserId());
            DeleteMongoSession(minId);
            documents =  collection.find(eq("UserID", ctx.getUserId())).into(new ArrayList<>());
        }
    }

    private int GetMaxIdBetHistory(int userID)
    {
        int max_id = 0;

        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection(txBetHistory1);
        ArrayList<Document> documents = collection.find(eq("UserID", userID)).into(new ArrayList<Document>());

        if (documents != null) {
            for (Document document : documents)
            {
                int id = (int) document.get("id");
                if (max_id < id)
                {
                    max_id = id;
                }
            }
        }
        return max_id;
    }

    private int GetMinIdBetHistory(int userID)
    {
        int min_id = 0;

        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection(txBetHistory1);
        ArrayList<Document> documents = collection.find(eq("UserID", userID)).into(new ArrayList<Document>());

        if (documents != null)
        {
            min_id = (int)documents.get(0).get("id");
            for (Document document : documents)
            {
                int id = (int) document.get("id");
                if (min_id > id)
                {
                    min_id = id;
                }
            }
        }
        return min_id;
    }

    private TaiXiuGameProto.ListBetHistory.Builder GetBetHistoryFromMongoDB(int userID)
    {
        TaiXiuGameProto.ListBetHistory.Builder listHistory = TaiXiuGameProto.ListBetHistory.newBuilder();

        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection(txBetHistory1);
        ArrayList<Document> documents = collection.find(eq("UserID", userID)).into(new ArrayList<Document>());

        if (documents != null)
        {
            for (Document document : documents)
            {
                TaiXiuGameProto.BetHistory.Builder newBetHistory = TaiXiuGameProto.BetHistory.newBuilder();

                newBetHistory.setSession((Integer) document.get("Session"));
                newBetHistory.setTime((String) document.get("Time"));
                newBetHistory.setCuaDat((String) document.get("Bet"));
                newBetHistory.setResultDice((String) document.get("Result"));
                newBetHistory.setBet((long) document.get("BetAmount"));
                newBetHistory.setRefund((long) document.get("Refund"));
                newBetHistory.setReceive((long) document.get("Receive"));

                listHistory.addListBetHistory(newBetHistory);
            }
        }

        return listHistory;
    }

    public void DeleteMongoSession(int id)
    {
        MongoClient mongo = MongoManager.getInstance().getClient();
        MongoCollection<Document> collection = mongo.getDatabase("fish").getCollection(txBetHistory1);
        collection.deleteOne(eq("id", id));
    }

    //add new
    private void handleTopWin(ChannelContextInfo ctx, XProtos.XMessage message) {
        RankService.getInstance().getRankWinGold(ctx, TXActionConst.TOP_WIN);
    }

    //
    private void handleCancelBet(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        TaiXiuGameProto.TXBetInfo betInfo = TaiXiuGameProto.TXBetInfo.parseFrom(message.getData());
        TaiXiuGameProto.TXBetInfo.Builder builder = TaiXiuGameProto.TXBetInfo.newBuilder();

        long totalBigBetAmount = betInfo.getTotalBigBetAmount();
        long totalSmallBetAmount = betInfo.getTotalSmallBetAmount();

        // Trừ tổng số tiền user đã cược ra khỏi totalBetAmountTai/totalBetAmountXiu
        // Và totalPlayerBetTai/totalPlayerBetXiu
        if (gameInstance.totalPlayerBetTai > 0) {
            gameInstance.totalPlayerBetTai--;
            gameInstance.virtualTotalPlayerBetTai--;

            gameInstance.totalBetAmountTai -= totalBigBetAmount;
            gameInstance.virtualTotalBetAmountTai -= totalBigBetAmount;
        }
        if (gameInstance.totalPlayerBetXiu > 0) {
            gameInstance.totalPlayerBetXiu--;
            gameInstance.virtualTotalPlayerBetXiu--;

            gameInstance.totalBetAmountXiu -= totalSmallBetAmount;
            gameInstance.virtualTotalBetAmountXiu -= totalSmallBetAmount;
        }

        // Cho phép user được đặt cược
        ctx.getUser().taiXiuGame.didXiuBet = false;
        ctx.getUser().taiXiuGame.didTaiBet = false;


        // Huỷ lịch sử đăt cược
        ctx.getUser().taiXiuGame.smallHistory = TaiXiuGameProto.BetHistory.newBuilder();
        ctx.getUser().taiXiuGame.bigHistory = TaiXiuGameProto.BetHistory.newBuilder();


        //Huỷ bỏ tổng số tiền mà user đã đặt cược
        ctx.getUser().taiXiuGame.bigBetAmout = 0;
        ctx.getUser().taiXiuGame.smallBetAmout = 0;


        //Trả lại tiền cược cho user


//        User user = new User(ctx.getUserId());
//
//        user.SetMoney(totalBigBetAmount);

        gameInstance.AddMoney(totalBigBetAmount, ctx.getUser(), MoneyType.GOLD);
        gameInstance.AddMoney(totalSmallBetAmount, ctx.getUser(), MoneyType.GOLD);

        //Get số tiền mới của user
        long userMoney = ctx.getUser().getMoneyGold();

        //SetUserMoney để gửi xuống số tiền mới cho client
        builder.setUserMoney(userMoney);

        ByteString newMessage = builder.build().toByteString();
        gameInstance.SendMessage(ctx, newMessage, TXActionConst.CANCEL_BET);
    }


    public void handleDiceResults()
    {
        TaiXiuGameProto.DiceHistory.Builder builder = TaiXiuGameProto.DiceHistory.newBuilder();
        RandomDice();
        total = dice1 + dice2 + dice3;

        if (gameInstance.totalPlayerBetTai + gameInstance.totalPlayerBetXiu < 2)
        {
            // Số tiền tối đa mà user có thể ăn được là = quỹ đã thua + 10% của quỹ đã thua;
            long offset = (gameInstance.playerMoneyLossFund * 10) / 100; // 10% quỹ
            if (gameInstance.totalBetAmountTai > gameInstance.playerMoneyLossFund + offset ||
                    gameInstance.totalBetAmountXiu > gameInstance.playerMoneyLossFund + offset) {
                // Vào hàm này thì user chắc chắn thua
                DiceCalculations();
                // Khi user thua thì số tiền thua sẽ được ghi vào quỹ
                gameInstance.playerMoneyLossFund += gameInstance.totalBetAmountTai + gameInstance.totalBetAmountXiu;
            }
        }
        else
        {
            DiceCalculations();
        }

        builder.setDice1(dice1);
        builder.setDice2(dice2);
        builder.setDice3(dice3);

        int totalDices = dice1 + dice2 + dice3;

        gameInstance.isTai = totalDices > 10;
        gameInstance.isXiu = totalDices <= 10;

        ByteString newMessage = builder.build().toByteString();
        gameInstance.SendToAll(newMessage, TXActionConst.DICE_REULTS);
        // Khoa add
        if (dice1 != 0) {
            AddValueToList_For_ClientGraph(dice1 + dice2 + dice3, dice1, dice2, dice3);
        }

    }
    //-----------------------------------------------------------

    private void DiceCalculations()
    {
        if (gameInstance.totalBetAmountTai > gameInstance.totalBetAmountXiu)
        {
            //Nếu đặt cửa tài nhiều hơn cửa xỉu, thì cần phải ra cửa xỉu
            //Nghĩa là total cần bé hơn 10
            while (total > 10)
            {
                RandomDice();
                total = dice1 + dice2 + dice3;
            }
        }

        else if (gameInstance.totalBetAmountTai < gameInstance.totalBetAmountXiu)
        {
            //Nếu đặt cửa xỉu nhiều hơn cửa tài, thì cần phải ra cửa tài
            //Nghĩa là total cần lớn hơn 10
            while (total <= 10)
            {
                RandomDice();
                total = dice1 + dice2 + dice3;
            }
        }
    }

    private void handlePayment(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException, NoSuchFieldException, IllegalAccessException
    {
        TaiXiuGameProto.TXBetInfo request = TaiXiuGameProto.TXBetInfo.parseFrom(message.getData());
        TaiXiuGameProto.TXBetInfo.Builder builder = TaiXiuGameProto.TXBetInfo.newBuilder();
        TaiXiuGameProto.DiceHistory.Builder diceBuild = TaiXiuGameProto.DiceHistory.newBuilder();

        diceBuild.setDice1(dice1);
        diceBuild.setDice2(dice2);
        diceBuild.setDice3(dice3);

        long totalBigBetAmount = request.getTotalBigBetAmount();
        long totalSmallBetAmount = request.getTotalSmallBetAmount();
        boolean isTai = request.getIsTai();
        boolean isXiu = request.getIsXiu();

        int totalDices = dice1 + dice2 + dice3;
        // Nếu user đặt tài và kq ra tài thì cộng tiền cho user
        if (isTai && totalDices > 10) {
            long VAT = totalBigBetAmount * 5 / 100;
            long moneyWinAfterTax = (totalBigBetAmount * 2) - VAT;
            gameInstance.AddMoney(moneyWinAfterTax, ctx.getUser(), MoneyType.GOLD);
            gameInstance.AddMoneyWin(totalBigBetAmount - VAT, ctx.getUser(), MoneyType.GOLD);

            //Reset quỹ tiền thua của user về 0...xem Line 284-287 để hiểu thêm
            gameInstance.playerMoneyLossFund = 0;

            ctx.getUser().taiXiuGame.bigHistory.setReceive(moneyWinAfterTax);
            //Set tiền thắng cược
            //long moneyWin = totalBigBetAmount * 2;
            builder.setTotalBigBetAmount(totalBigBetAmount - VAT);
            SaveFundDealerIntoMongon(VAT);
        }

        // Nếu user đặt xỉu và kq ra xỉu thì cũng cộng tiền cho user
        if (isXiu && totalDices <= 10) {
            long VAT = totalSmallBetAmount * 5 / 100;

            long moneyWinAfterTax = (totalSmallBetAmount * 2) - VAT;
            gameInstance.AddMoney(moneyWinAfterTax, ctx.getUser(), MoneyType.GOLD);
            gameInstance.AddMoneyWin(totalSmallBetAmount - VAT, ctx.getUser(), MoneyType.GOLD);

            //Reset quỹ tiền thua của user về 0...xem Line 284-287 để hiểu thêm
            gameInstance.playerMoneyLossFund = 0;

            ctx.getUser().taiXiuGame.smallHistory.setReceive(moneyWinAfterTax);
            builder.setTotalSmallBetAmount(totalSmallBetAmount - VAT);
            SaveFundDealerIntoMongon(VAT);
        }

        String result = totalDices>10? "OVER":"UNDER";
        ctx.getUser().taiXiuGame.bigHistory.setResultDice(result);
        ctx.getUser().taiXiuGame.smallHistory.setResultDice(result);


        long userMoney = ctx.getUser().getMoneyGold();

        //Set tiền còn lại của user
        builder.setUserMoney(userMoney);

        ByteString newMassage = builder.build().toByteString();
        ByteString history = diceBuild.build().toByteString();

        gameInstance.SendMessage(ctx, newMassage, TXActionConst.PAYMENT);
        gameInstance.SendMessage(ctx, history, TXActionConst.SERVER_RESULT_ONE);

        // Cho user đặt cược lại
        ctx.getUser().taiXiuGame.didXiuBet = false;
        ctx.getUser().taiXiuGame.didTaiBet = false;

        ctx.getUser().taiXiuGame.bigBetAmout = 0;
        ctx.getUser().taiXiuGame.smallBetAmout = 0;
    }


    private void SaveFundDealerIntoMongon(long VAT) {
        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection("roomInfo");
        Document document = collection.find(eq("_id", 30001)).first();
        if (document != null) {
            fund_dealer = (long) document.get("fund_dealer");

            Bson filter = new Document("_id", 30001);
            Bson newValue = new Document("fund_dealer", fund_dealer + VAT);
            Bson updateOperationDoc = new Document("$set", newValue);
            collection.updateOne(filter, updateOperationDoc);
        }
    }

    private void handleBet(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException, NoSuchFieldException, IllegalAccessException {
        TaiXiuGameProto.TXBetInfo request = TaiXiuGameProto.TXBetInfo.parseFrom(message.getData());
        TaiXiuGameProto.TXBetInfo.Builder builder = TaiXiuGameProto.TXBetInfo.newBuilder();

        long betAmount = request.getBetAmount();
        boolean isTai = request.getIsTai();
        boolean isXiu = request.getIsXiu();

        long userMoney = ctx.getUser().getMoneyGold();
        userMoney -= betAmount;

        if (userMoney < 0) {
            builder.setErrorCode(1);
        } else {
            if (isTai)
            {
                gameInstance.totalBetAmountTai += betAmount;
                gameInstance.virtualTotalBetAmountTai += betAmount;
                if (!ctx.getUser().taiXiuGame.didTaiBet)
                {
                    ctx.getUser().taiXiuGame.didTaiBet = true;
                    gameInstance.totalPlayerBetTai++;
                    gameInstance.virtualTotalPlayerBetTai++;

                    ctx.getUser().taiXiuGame.bigHistory.setSession(TaiXiuGame.session);

                     String newTimer = new String();
                    newTimer = GetDateTime();

                    ctx.getUser().taiXiuGame.bigHistory.setTime(newTimer);
                    ctx.getUser().taiXiuGame.bigHistory.setCuaDat("OVER");
                }
                ctx.getUser().taiXiuGame.bigBetAmout += betAmount;
                ctx.getUser().taiXiuGame.bigHistory.setBet(ctx.getUser().taiXiuGame.bigBetAmout);
            }
            else if (isXiu)
            {
                gameInstance.totalBetAmountXiu += betAmount;
                gameInstance.virtualTotalBetAmountXiu += betAmount;

                if (!ctx.getUser().taiXiuGame.didXiuBet) {
                    ctx.getUser().taiXiuGame.didXiuBet = true;
                    gameInstance.totalPlayerBetXiu++;
                    gameInstance.virtualTotalPlayerBetXiu++;

                    ctx.getUser().taiXiuGame.smallHistory.setSession(TaiXiuGame.session);

                    ctx.getUser().taiXiuGame.smallHistory.setTime(GetDateTime());
                    ctx.getUser().taiXiuGame.smallHistory.setCuaDat("UNDER");
                }
                ctx.getUser().taiXiuGame.smallBetAmout += betAmount;
                ctx.getUser().taiXiuGame.smallHistory.setBet(ctx.getUser().taiXiuGame.smallBetAmout);
            }


            // Trừ tiền player
            gameInstance.SubMoney(betAmount, ctx.getUser(), MoneyType.GOLD);
            long newMoney = ctx.getUser().getMoneyGold();

            //Set tiền để gửi xuống client
            builder.setUserMoney(newMoney);

            //Set tổng tiền cược của user
            builder.setTotalBigBetAmount(request.getTotalBigBetAmount());
            builder.setTotalSmallBetAmount(request.getTotalSmallBetAmount());
        }

        gameInstance.SendMessage(ctx, builder.build().toByteString(), TXActionConst.BET);
    }

    private void handleGameInfo(ChannelContextInfo ctx, XProtos.XMessage message) {
        ByteString byteString = getGameInstance().parseMessageToByteString();
        gameInstance.SendMessage(ctx, byteString, TXActionConst.GAME_INFO);

    }

    private void handleJoinRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        TaiXiuGameProto.TXJoinRequest request = TaiXiuGameProto.TXJoinRequest.parseFrom(message.getData());
        TXRoom room = gameInstance.getRoomById(request.getRoomId());
        TaiXiuGameProto.TXRoomInfo.Builder roomInfo = room.parseProtoBuilder();

        gameInstance.addPlayer(ctx.getUser());
        gameInstance.joinRoom(ctx.getUser(), room);

        ByteString newMessage = roomInfo.build().toByteString();
        gameInstance.SendMessage(ctx, newMessage, TXActionConst.JOIN_ROOM);


    }

    private void handleShowGraphWhewStart(ChannelContextInfo ctx, XProtos.XMessage message)
    {

        TaiXiuGameProto.TXResult_Server_All.Builder builder = TaiXiuGameProto.TXResult_Server_All.newBuilder();
        builder.addAllFinalResult(gameInstance.FinalResult)
                .addAllDice1(gameInstance.Dice1Result)
                .addAllDice2(gameInstance.Dice2Result)
                .addAllDice3(gameInstance.Dice3Result)
                .addAllFinalResultGraph3(gameInstance.FinalResultGrapth3)
                .addAllFinalResultGraph4(gameInstance.FinalResultGrapth4);
        ByteString ByteStringResultToAll = builder.build().toByteString();
        gameInstance.SendMessage(ctx, ByteStringResultToAll, TXActionConst.SERVER_RESULT);

    }


    private void handleLeaveGame(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException {
        TaiXiuGameProto.TXLeaveGameResponse.Builder builder = TaiXiuGameProto.TXLeaveGameResponse.newBuilder();
        builder.setErrorCode(0);
        APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                ctx.getUser().getUserName(),
                Long.toString(ctx.getUser().getMoneyGold()),0,""));
        try {
            gameInstance.leaveRoom(ctx.getUser());
            gameInstance.removePlayer(ctx.getUser());

        } catch (NotFoundPlayerInGame notFoundPlayerInGame) {
            builder.setErrorCode(TXErrorCode.PLAYER_NOT_FOUND_IN_GAME);
        } catch (Exception e) {
            builder.setErrorCode(TXErrorCode.ERROR_NOT_DEFINE);
            getLogger().error(String.format(
                    "User[%d] leave slot room fail",
                    ctx.getUserId()), e
            );
        }
        ByteString newMessage = builder.build().toByteString();
        gameInstance.SendMessage(ctx, newMessage, TXActionConst.LEAVE_ROOM);
    }

    private TaiXiuGame getGameInstance() {
        return gameInstance;
    }

    private void RandomDice()
    {
        Random rand = new Random();
        dice1 = 1 + rand.nextInt(6);
        dice2 = 1 + rand.nextInt(6);
        dice3 = 1 + rand.nextInt(6);
    }

    private String GetDateTime()
    {
        //Asia/Ho_Chi_Minh
        TimeZone zone = TimeZone.getTimeZone("GMT+7");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyy - HH:mm");
        simpleDateFormat.setTimeZone(zone);

        Calendar c = Calendar.getInstance(zone);
        return simpleDateFormat.format(c.getTime());
    }


}
