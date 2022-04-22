package games.Poker;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.mongodb.client.MongoCollection;
import games.Poker.CARDS.CARDNUMBER;
import games.Poker.CARDS.CARDTYPE;
import games.Poker.CARDS.PokerCard;
import games.Poker.ENUM.DEALERTURN;
import games.Poker.ENUM.PHASE;
import games.Poker.ENUM.PokerConstanst;
import games.core.application.channel.TaskScheduler;
import games.core.application.handler.ChannelContextInfo;
import games.core.database.MongoManager;
import games.core.database.document.DRoomInfo;
import games.core.datatype.HashPlayer;
import games.core.exception.game.CannotRemovePlayerInRoom;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.exception.game.NotFoundPlayerInRoom;
import games.core.exception.game.RoomNotExist;
import games.core.proto.ProtoSerializer;
import games.core.user.User;
import libs.util.LogFactory;
import libs.util.data.HashByInt;
import message.PokerProto;
import message.XProtos;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import scala.collection.generic.BitOperations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
import static games.core.constant.ServerCommandConstant.POKER_COMMAND;

public class PokerRoom implements ProtoSerializer<PokerProto.Poker_Response_RoomInfo> {
@Descreption_Anno(Name = "SINGLETONE")
    public static PokerRoom instance;
    public static PokerRoom getInstance() {
        if (instance == null) {
            instance = new PokerRoom();
        }
        return instance;
    }
@Descreption_Anno(Name = "LOGGER")
    protected Logger logger;
    public Logger getLogger() {
        if (logger == null){
            logger = LogFactory.getLogger(getClass().getSimpleName());
        }
        return logger;
    }
@Descreption_Anno(Name = "FIELDS")
    private PokerGame PokerGameInstance;
    private PokerGame getPokerGameInstance() {
        return PokerGameInstance;
    }
    private DRoomInfo roomInfo; // Lấy toàn bộ thông tin của Room trên MongoDB
    public DRoomInfo getRoomInfo() {
        return roomInfo;
    }

    private int roomid; // roomid của phòng

    public long Poker_NewMinBet; // Mỗi khi có ng bet/raise/all-in. nếu all-in thì bỏ ra khỏi danh sách so sánh Poker_NewMinBet khi có ng bet, raise, all-in
    public ArrayList<Integer> Poker_LastAction;

    public Integer Poker_IdOfNextDealer; // set lại mỗi khi có ng leave room hoặc tạo ván mới

    public int Poker_phase;
    public long Poker_Pot;
    public ArrayList<Integer> Poker_JoinedGameID;
    public ArrayList<Integer> Poker_JoinedGameID_FOLDER;
    public ArrayList<Boolean> Poker_Turn;
    public int Poker_CountDown; // khi phase = PHASE.WAITING countdown = countdown cho toàn bàn
    public ArrayList<Integer> Poker_DealerTurn;
    public ArrayList<Long> Poker_BetAmount;
    public ArrayList<Long> Poker_BetAmountTotal;
    public ArrayList<Integer> Poker_JoinedGameID_OUTOFMONEY;
    public ArrayList<ArrayList<PokerCard>> Poker_playerCardHolder;
    ArrayList<PokerCard> Poker_playerCardHolder_player1 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player2 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player3 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player4 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player5 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player6 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player7 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player8 = new ArrayList<PokerCard>() {{add(new PokerCard()); add(new PokerCard());}};
    ArrayList<PokerCard> Poker_playerCardHolder_player9 = new ArrayList<PokerCard>(){{add(new PokerCard()); add(new PokerCard());}};
    public ArrayList<ArrayList<Integer>> Poker_PlayerAvailableAction; // 0 add
    ArrayList<Integer> Poker_PlayerAvailableAction_player1 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player2 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player3 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player4 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player5 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player6 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player7 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player8 = new ArrayList<>();
    ArrayList<Integer> Poker_PlayerAvailableAction_player9 = new ArrayList<>();
    public ArrayList<PokerCard> Poker_AllCard52;
    public ArrayList<PokerCard> Poker_CommunityCards;
    public PokerCard Poker_CommunityCard_1 = new PokerCard();
    public PokerCard Poker_CommunityCard_2 = new PokerCard();
    public PokerCard Poker_CommunityCard_3 = new PokerCard();
    public PokerCard Poker_CommunityCard_4 = new PokerCard();
    public PokerCard Poker_CommunityCard_5 = new PokerCard();
    public ArrayList<Long> Poker_CardStrenght = new ArrayList<>();
    protected LinkedHashMap<Integer,PokerPlayer> players = new LinkedHashMap<>(); // toàn bộ player - ConcurrentHashMap<playerID, PokerPlayer



    public void setPoker_NewMinBet(long poker_NewMinBet) {
        Poker_NewMinBet = poker_NewMinBet;
    }
    public long getPoker_NewMinBet() {
        return Poker_NewMinBet;
    }
    public void setPoker_LastAction(ArrayList<Integer> poker_LastAction) {
        Poker_LastAction = poker_LastAction;
    }
    public ArrayList<Integer> getPoker_LastAction() {
        return Poker_LastAction;
    }
    public void setPoker_JoinedGameID(ArrayList<Integer> poker_JoinedGameID) {
        Poker_JoinedGameID = poker_JoinedGameID;
    }
    public int getRoomid() {
        return roomid;
    }
    public void setPoker_IdOfNextDealer(Integer poker_IdOfNextDealer) {
        Poker_IdOfNextDealer = poker_IdOfNextDealer;
    }
    public Integer getPoker_IdOfNextDealer() {
        return Poker_IdOfNextDealer;
    }
    public ArrayList<Integer> getPoker_JoinedGameID() {
        return Poker_JoinedGameID;
    }
    public void setPoker_Turn(ArrayList<Boolean> poker_Turn) {
        Poker_Turn = poker_Turn;
    }
    public ArrayList<Boolean> getPoker_Turn() {
        return Poker_Turn;
    }
    public int getPoker_CountDown() {
        return Poker_CountDown;
    }
    public void setPoker_CountDown(int poker_CountDown) {
        Poker_CountDown = poker_CountDown;
    }
    public void setPoker_DealerTurn(ArrayList<Integer> poker_DealerTurn) {
        Poker_DealerTurn = poker_DealerTurn;
    }
    public ArrayList<Integer> getPoker_DealerTurn() {
        return Poker_DealerTurn;
    }
    public void setPoker_BetAmount(ArrayList<Long> poker_BetAmount) {
        Poker_BetAmount = poker_BetAmount;
    }
    public ArrayList<Long> getPoker_BetAmount() {
        return Poker_BetAmount;
    }
    public void setPoker_playerCardHolder(ArrayList<ArrayList<PokerCard>> poker_playerCardHolder) {
        Poker_playerCardHolder = poker_playerCardHolder;
    }
    public ArrayList<ArrayList<PokerCard>> getPoker_playerCardHolder() {
        return Poker_playerCardHolder;
    }
    public void setPoker_PlayerAvailableAction(ArrayList<ArrayList<Integer>> poker_PlayerAvailableAction) {
        Poker_PlayerAvailableAction = poker_PlayerAvailableAction;
    }
    public ArrayList<ArrayList<Integer>> getPoker_PlayerAvailableAction() {
        return Poker_PlayerAvailableAction;
    }
    public int getPoker_phase(){
        return Poker_phase;
    }
    public void setPoker_phase(int Poker_phase) {
        this.Poker_phase = Poker_phase;
    }
    public void setPoker_Pot(long poker_Pot) {
        Poker_Pot = poker_Pot;
    }
    public long getPoker_Pot() {
        return Poker_Pot;
    }
    public void setPoker_AllCard52(ArrayList<PokerCard> poker_AllCard52) {
        Poker_AllCard52 = poker_AllCard52;
    }
    public ArrayList<PokerCard> getPoker_AllCard52() {
        return Poker_AllCard52;
    }
    public void setPoker_CommunityCards(ArrayList<PokerCard> poker_CommunityCards) {
        Poker_CommunityCards = poker_CommunityCards;
    }
    public ArrayList<PokerCard> getPoker_CommunityCards() {
        return Poker_CommunityCards;
    }
    public LinkedHashMap<Integer,PokerPlayer> getPlayers() {
        return players;
    }
    PokerProto.Poker_Response_RoomIdFund.Builder default_response_roomid; // Build Proto trả roomid và fund bên ngoài
    protected PokerPlayerPool playerPool = new PokerPlayerPool();

@Descreption_Anno(Name = "CONSTRUCTOR + SAVE TO MONGO")
    public PokerRoom(){
    }
    public PokerRoom (int id){
        try {
            roomInfo = new DRoomInfo(id); // Tạo constructor mới - Trong constructor đã tự update về
        }
        catch (Exception e){
            e.printStackTrace();
            this.getLogger().warn("Poker Room chưa được tạo trên MongoDB : ");
        }
        this.roomid = id; // Gán id Game Poker cho biến roomid (50000)  - lưu server
        SaveRoomInfoToMongo();
        // default_response_roomid = PokerProto.Poker_Response_RoomIdFund.newBuilder();
        // default_response_roomid.setRoomid(id); // Trả biến roomid về cho client - lưu client
        // Tạo 1 Thread khi constructor đuọc gọi - trễ 2s, 5s gọi 1 lần
        TaskScheduler.getInstance().scheduleAtFixedRate(new Thread_Twosec_Fund(), 2000, 5000, TimeUnit.MILLISECONDS);
    }
    private void sendToAllPlayer(Message message) {
        getPlayers().values().forEach(pokerPlayer -> PKCommand.getInstance().pushToClient(
                pokerPlayer.getCtxInfo(), PokerConstanst.POKER_ROOM_FUND, message));
    }
    class Thread_Twosec_Fund implements Runnable {
        @Override
        public void run() {
//            sendToAllPlayer(default_response_roomid.build());
        }
    }
@Descreption_Anno(Name = "METHOD MẶC ĐỊNH KẾ THỪA INTERFACE")
    @Override
    public PokerProto.Poker_Response_RoomInfo.Builder parseProtoBuilder() { // Poker_Response_RoomInfo
    PokerProto.Poker_Response_RoomInfo.Builder response = PokerProto.Poker_Response_RoomInfo.newBuilder();
    response.setRoomId(this.roomid); // Roomid / 1 room
    response.setRoomCurrentPhase(this.Poker_phase);

    ArrayList<Integer> useridlist = new ArrayList<>();
    ArrayList<String> userName = new ArrayList<>();
    ArrayList<Long> usermoney = new ArrayList<>();

    for (PokerPlayer pokerPlayer: this.players.values()){ // trả về list theo thứ tự list players
        User user = new User(pokerPlayer.getId(), 0); // gọi để tự động update
        useridlist.add(pokerPlayer.getId());
        usermoney.add(user.getMoneyGold());
        userName.add(user.getUserName());
    }
    response.addAllUserid(useridlist); // all id / 1 room
    response.addAllUsermoney(usermoney); // all money / 1 room
    response.addAllUsername(userName); // all user / 1 room

    response.setRoomIDOfNextDealer(this.Poker_IdOfNextDealer);
    response.setRoomNewMinBet(this.Poker_NewMinBet);
    response.setRoomPot(this.Poker_Pot);
    response.addAllRoomJoinedGameID(this.Poker_JoinedGameID);
    response.addAllRoomJoinedGameIDFOLDER(this.Poker_JoinedGameID_FOLDER);
    response.addAllRoomJoinedGameIDOUTOFMONEY(this.Poker_JoinedGameID_OUTOFMONEY);
    response.addAllRoomTurn(this.Poker_Turn);
    response.addAllRoomLastAction(this.Poker_LastAction);
    response.addAllRoomDealerTurn(this.Poker_DealerTurn);
    response.addAllRoomBetAmount(this.Poker_BetAmount);
    response.addAllRoomBetAmountTotal(this.Poker_BetAmountTotal);
    response.addAllRoomCardStrenght(this.Poker_CardStrenght);

    for (int j = 0; j < this.Poker_playerCardHolder.size(); j++) { //
        PokerProto.Poker_Player.Builder pokerplayer_AllCardHolder = PokerProto.Poker_Player.newBuilder();
        for (int i = 0; i < 2; i++){ // 2 lá bài / 1 ng
            PokerProto.Poker_Cards.Builder pokercard_PlayerCardHolder = PokerProto.Poker_Cards.newBuilder();
            pokercard_PlayerCardHolder.setCardnumber(this.Poker_playerCardHolder.get(j).get(i).getCardnumber());
            pokercard_PlayerCardHolder.setCardtype(this.Poker_playerCardHolder.get(j).get(i).getCardtype());
            pokerplayer_AllCardHolder.addPlayerCardHolder(pokercard_PlayerCardHolder);
        }
        response.addRoomPlayerCardsHolder(pokerplayer_AllCardHolder);
    }

    for (int i = 0; i < this.Poker_PlayerAvailableAction.size(); i++){ //
        PokerProto.Poker_Player.Builder listInt32_PlayerAvailableAction = PokerProto.Poker_Player.newBuilder();
        listInt32_PlayerAvailableAction.addAllPlayerAvailableAction(this.Poker_PlayerAvailableAction.get(i));
        response.addRoomPlayerAvailableAction(listInt32_PlayerAvailableAction);
    }

    for (int i = 0; i < this.Poker_AllCard52.size(); i++){
        PokerProto.Poker_Cards.Builder pokercard_roomAll52Cards = PokerProto.Poker_Cards.newBuilder();
        pokercard_roomAll52Cards.setCardnumber(this.Poker_AllCard52.get(i).getCardnumber());
        pokercard_roomAll52Cards.setCardtype(this.Poker_AllCard52.get(i).getCardtype());
        response.addRoomAll52Cards(pokercard_roomAll52Cards);
    }

    for (int i = 0; i < this.Poker_CommunityCards.size(); i++){
        PokerProto.Poker_Cards.Builder pokercard_roomCommunityCards = PokerProto.Poker_Cards.newBuilder();
        pokercard_roomCommunityCards.setCardnumber(this.Poker_CommunityCards.get(i).getCardnumber());
        pokercard_roomCommunityCards.setCardtype(this.Poker_CommunityCards.get(i).getCardtype());
        response.addRoomCommunityCards(pokercard_roomCommunityCards);
    }
    return response;
    }
@Descreption_Anno(Name = "SEND TIME COUNTDOWN TO ALL")

    public PokerProto.Poker_Response_CountDown.Builder BuildInfoForOneRoom () {
        PokerProto.Poker_Response_CountDown.Builder response = PokerProto.Poker_Response_CountDown.newBuilder();
        response.setRoomId(this.roomid);
        response.setRoomCountdown((this.Poker_CountDown));
        return response;
    }

@Descreption_Anno(Name = "ADD PLAYER TO ROOM")
    public void AddPlayer(PokerPlayer player) {
        players.remove(player.getId());
        players.putIfAbsent(player.getId(), player);
    }
    public void removePlayer(User user) throws NotFoundPlayerInGame { //
        playerPool.putSync(getPlayer(user));
        this.players.remove(user.getId());
    }
    public void removePlayer(PokerPlayer player) throws NotFoundPlayerInRoom, CannotRemovePlayerInRoom {
        if (!players.containsKey(player.getId())){
            throw new NotFoundPlayerInRoom(player);
        }
        if (!players.remove(player.getId(), player)){
            throw new CannotRemovePlayerInRoom(player);
        }
    }
@Descreption_Anno(Name = "GET PLAYER FROM ROOM")
    public PokerPlayer getPlayer(User user) throws NotFoundPlayerInGame { // Trả về player đang trong list dựa vào id
        if (!players.containsKey(user.getId())) {
            throw new NotFoundPlayerInGame(user);
        }
        return this.players.get(user.getId());
    }
@Descreption_Anno(Name = "STUFF METHODS")
    public String CreateRandomCARNUMBER(){
        ArrayList<String> listCARNUMBER = new ArrayList<>();
        listCARNUMBER.add(CARDNUMBER.ACES);
        listCARNUMBER.add(CARDNUMBER.TWO);
        listCARNUMBER.add(CARDNUMBER.THREE);
        listCARNUMBER.add(CARDNUMBER.FOUR);
        listCARNUMBER.add(CARDNUMBER.FIVE);
        listCARNUMBER.add(CARDNUMBER.SIX);
        listCARNUMBER.add(CARDNUMBER.SEVEN);
        listCARNUMBER.add(CARDNUMBER.EIGHT);
        listCARNUMBER.add(CARDNUMBER.NINE);
        listCARNUMBER.add(CARDNUMBER.TEN);
        listCARNUMBER.add(CARDNUMBER.JACK);
        listCARNUMBER.add(CARDNUMBER.QUEEN);
        listCARNUMBER.add(CARDNUMBER.KING);

        Random rand = new Random();
        Integer rand_int = rand.nextInt(13); // 0-12
        return listCARNUMBER.get(rand_int);
    }
    public String CreateRandomCARDTYPE(){
        ArrayList<String> listCARDTYPE = new ArrayList<>();
        listCARDTYPE.add(CARDTYPE.HEART);
        listCARDTYPE.add(CARDTYPE.DIAMOND);
        listCARDTYPE.add(CARDTYPE.CLUB);
        listCARDTYPE.add(CARDTYPE.SPADE);

        Random rand = new Random();
        Integer rand_int = rand.nextInt(4); // 0-3
        return listCARDTYPE.get(rand_int);
    }
@Descreption_Anno(Name = "CREATE 52 CARDS FIRST TIME")
    public void CreateDeck52Cards() {
        this.Poker_AllCard52.clear();

        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.ACES, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TWO, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.THREE, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FOUR, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FIVE, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SIX, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SEVEN, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.EIGHT, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.NINE, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TEN, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.JACK, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.QUEEN, CARDTYPE.HEART));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.KING, CARDTYPE.HEART));

        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.ACES, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TWO, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.THREE, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FOUR, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FIVE, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SIX, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SEVEN, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.EIGHT, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.NINE, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TEN, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.JACK, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.QUEEN, CARDTYPE.DIAMOND));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.KING, CARDTYPE.DIAMOND));

        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.ACES, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TWO, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.THREE, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FOUR, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FIVE, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SIX, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SEVEN, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.EIGHT, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.NINE, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TEN, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.JACK, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.QUEEN, CARDTYPE.CLUB));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.KING, CARDTYPE.CLUB));

        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.ACES, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TWO, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.THREE, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FOUR, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.FIVE, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SIX, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.SEVEN, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.EIGHT, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.NINE, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.TEN, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.JACK, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.QUEEN, CARDTYPE.SPADE));
        this.Poker_AllCard52.add(new PokerCard(CARDNUMBER.KING, CARDTYPE.SPADE));

    }
    public void SaveRoomInfoToMongo () {
        MongoCollection mgcollection = MongoManager.getInstance().getDatabase().getCollection("roomInfo");

        this.Poker_LastAction = new ArrayList<>();
        Document doc_level_1_Poker_LastAction = new Document();
        for (int i = 0; i < this.Poker_LastAction.size(); i++){
            doc_level_1_Poker_LastAction.append("PLAYER " + i, this.Poker_LastAction.get(i));
        }
        mgcollection.updateOne(eq("_id", this.roomid),combine(set("Poker_LastAction",doc_level_1_Poker_LastAction)));

        Poker_IdOfNextDealer = 0;

        this.Poker_JoinedGameID = new ArrayList<>();
        Document doc_level_1_Poker_JoinedGameID = new Document();
        for (int i = 0; i < this.Poker_JoinedGameID.size(); i++){
            doc_level_1_Poker_JoinedGameID.append("PLAYER " + i, this.Poker_JoinedGameID.get(i));
        }
        mgcollection.updateOne(eq("_id", this.roomid),combine(set("Poker_JoinedGameID",doc_level_1_Poker_JoinedGameID)));

        this.Poker_JoinedGameID_FOLDER = new ArrayList<>();
        this.Poker_JoinedGameID_OUTOFMONEY = new ArrayList<>();
        this.Poker_BetAmountTotal = new ArrayList<>();

        this.Poker_DealerTurn = new ArrayList<>();
        Document doc_level_1_Poker_DealerTurn = new Document();
        for (int i = 0; i < this.Poker_DealerTurn.size(); i++){
            doc_level_1_Poker_DealerTurn.append("PLAYER " + i, this.Poker_DealerTurn.get(i));
        }
        mgcollection.updateOne(eq("_id", this.roomid),combine(set("Poker_DealerTurn",doc_level_1_Poker_DealerTurn)));

        this.Poker_BetAmount = new ArrayList<>();
        Document doc_level_1_Poker_BetMoney = new Document();
        for (int i = 0; i < this.Poker_BetAmount.size(); i++){
            doc_level_1_Poker_BetMoney.append("PLAYER_BET_" + i, this.Poker_BetAmount.get(i));
        }
        mgcollection.updateOne(eq("_id", this.roomid),combine(set("Poker_BetMoney",doc_level_1_Poker_BetMoney)));

        this.Poker_playerCardHolder = new ArrayList<>();
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player1);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player2);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player3);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player4);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player5);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player6);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player7);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player8);
//        this.Poker_playerCardHolder.add(this.Poker_playerCardHolder_player9);
        Document doc_level_0_Poker_playerCardHolder = new Document();

        Document doc_level_1_Poker_playerCardHolder_player1 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player1.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player1.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player1.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player1.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 1",doc_level_1_Poker_playerCardHolder_player1);

        Document doc_level_1_Poker_playerCardHolder_player2 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player2.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player2.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player2.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player2.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 2",doc_level_1_Poker_playerCardHolder_player2);

        Document doc_level_1_Poker_playerCardHolder_player3 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player3.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player3.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player3.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player3.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 3",doc_level_1_Poker_playerCardHolder_player3);

        Document doc_level_1_Poker_playerCardHolder_player4 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player4.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player4.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player4.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player4.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 4",doc_level_1_Poker_playerCardHolder_player4);

        Document doc_level_1_Poker_playerCardHolder_player5 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player5.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player5.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player5.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player5.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 5",doc_level_1_Poker_playerCardHolder_player5);

        Document doc_level_1_Poker_playerCardHolder_player6 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player6.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player6.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player6.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player6.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 6",doc_level_1_Poker_playerCardHolder_player6);

        Document doc_level_1_Poker_playerCardHolder_player7 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player7.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player7.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player7.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player7.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 7",doc_level_1_Poker_playerCardHolder_player7);

        Document doc_level_1_Poker_playerCardHolder_player8 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player8.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player8.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player8.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player8.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 8",doc_level_1_Poker_playerCardHolder_player8);

        Document doc_level_1_Poker_playerCardHolder_player9 = new Document();
        for (int i = 0; i < Poker_playerCardHolder_player9.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_playerCardHolder_player9.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_playerCardHolder_player9.get(i).getCardtype());
            doc_level_1_Poker_playerCardHolder_player9.append("Card_" + (i + 1), doc_level_2);
        }
        doc_level_0_Poker_playerCardHolder.append("PLAYER 9",doc_level_1_Poker_playerCardHolder_player9);
        mgcollection.updateOne(eq("_id", this.roomid), combine(set("Poker_playerCardHolder",doc_level_0_Poker_playerCardHolder)));

        this.Poker_PlayerAvailableAction = new ArrayList<>();
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player1);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player2);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player3);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player4);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player5);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player6);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player7);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player8);
//        this.Poker_PlayerAvailableAction.add(this.Poker_PlayerAvailableAction_player9);

        Document doc_level_0_Poker_PlayerAvailableAction = new Document();

        Document doc_level_1_Poker_PlayerAvailableAction_player1 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player1.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player1.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player1.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 1",doc_level_1_Poker_PlayerAvailableAction_player1);

        Document doc_level_1_Poker_PlayerAvailableAction_player2 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player2.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player2.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player2.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 2",doc_level_1_Poker_PlayerAvailableAction_player2);

        Document doc_level_1_Poker_PlayerAvailableAction_player3 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player3.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player3.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player3.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 3",doc_level_1_Poker_PlayerAvailableAction_player3);

        Document doc_level_1_Poker_PlayerAvailableAction_player4 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player4.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player4.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player4.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 4",doc_level_1_Poker_PlayerAvailableAction_player4);

        Document doc_level_1_Poker_PlayerAvailableAction_player5 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player5.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player5.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player5.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 5",doc_level_1_Poker_PlayerAvailableAction_player5);

        Document doc_level_1_Poker_PlayerAvailableAction_player6 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player6.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player6.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player6.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 6",doc_level_1_Poker_PlayerAvailableAction_player6);

        Document doc_level_1_Poker_PlayerAvailableAction_player7 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player7.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player7.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player7.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 7",doc_level_1_Poker_PlayerAvailableAction_player7);

        Document doc_level_1_Poker_PlayerAvailableAction_player8 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player8.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player8.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player8.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 8",doc_level_1_Poker_PlayerAvailableAction_player8);

        Document doc_level_1_Poker_PlayerAvailableAction_player9 = new Document();
        for (int i = 0; i < Poker_PlayerAvailableAction_player9.size(); i++){
            doc_level_1_Poker_PlayerAvailableAction_player9.append("Action_" + (i + 1), this.Poker_PlayerAvailableAction_player9.get(i));
        }
        doc_level_0_Poker_PlayerAvailableAction.append("PLAYER 9",doc_level_1_Poker_PlayerAvailableAction_player9);

        mgcollection.updateOne(eq("_id", this.roomid), combine(set("Poker_PlayerAvailableAction",doc_level_0_Poker_PlayerAvailableAction)));

        this.Poker_Turn = new ArrayList<>();
        Document doc_level_1_Poker_Turn = new Document();
        for (int i = 0; i < this.Poker_Turn.size(); i++){
            doc_level_1_Poker_Turn.append("PLAYER " + i, this.Poker_Turn.get(i));
        }
        mgcollection.updateOne(eq("_id", this.roomid), combine(set("Poker_Turn",doc_level_1_Poker_Turn)));

        this.Poker_CountDown = 0;
        mgcollection.updateOne(eq("_id",this.roomid),combine(set("Poker_CountDown",this.Poker_CountDown)));

        this.Poker_phase = PHASE.WAITING;
        mgcollection.updateOne(eq("_id",this.roomid),combine(set("Poker_phase",this.Poker_phase)));

        this.Poker_Pot = 0;
        mgcollection.updateOne(eq("_id",this.roomid),combine(set("Poker_pot",this.Poker_Pot)));

        this.Poker_CommunityCards = new ArrayList<>();
        this.Poker_CommunityCards.add(Poker_CommunityCard_1);
        this.Poker_CommunityCards.add(Poker_CommunityCard_2);
        this.Poker_CommunityCards.add(Poker_CommunityCard_3);
        this.Poker_CommunityCards.add(Poker_CommunityCard_4);
        this.Poker_CommunityCards.add(Poker_CommunityCard_5);
        Document doc_level_1_Poker_communityCards = new Document();
        for (int i = 0; i < this.Poker_CommunityCards.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_CommunityCards.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_CommunityCards.get(i).getCardtype());
            doc_level_1_Poker_communityCards.append("Card_" + (i + 1), doc_level_2);
        }
        Bson filter1 = eq("_id", this.roomid);
        Bson bson1 = set("Poker_communityCards", doc_level_1_Poker_communityCards);
        Bson query1 = combine(bson1);
        mgcollection.updateOne(filter1, query1);

        this.Poker_AllCard52 = new ArrayList<>();;
        CreateDeck52Cards();
        Document doc_level_1_Poker_AllCard52 = new Document();
        for (int i = 0; i < this.Poker_AllCard52.size(); i++){
            Document doc_level_2 = new Document();
            doc_level_2.append("NUMBER", this.Poker_AllCard52.get(i).getCardnumber());
            doc_level_2.append("TYPE", this.Poker_AllCard52.get(i).getCardtype());
            doc_level_1_Poker_AllCard52.append("Card_" + (i + 1), doc_level_2);
        }
        Bson filter2 = eq("_id", this.roomid);
        Bson bson2 = set("Poker_AllCard52", doc_level_1_Poker_AllCard52);
        Bson query2 = combine(bson2);
        mgcollection.updateOne(filter2, query2);
    }
    @Descreption_Anno(Name = "SEND DATA TO PLAYERS - SOCKET")
    public void SendToAll_OneRoom(ByteString responseBeanByteString, int responseCode) {
        for (PokerPlayer player : players.values()) {
            if (player != null) { // Tránh trường hợp người ảo
                if(player.getCtxInfo() == null){
                }
                else {
                    SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
                }
            }
        }
    }
    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode) {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(POKER_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);
        ctx.writeAndFlush(responseMessage.build());
    }
}
