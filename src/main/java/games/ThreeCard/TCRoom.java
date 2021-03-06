package games.ThreeCard;

import com.google.protobuf.ByteString;
import games.ThirteenKiller.Cards;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.proto.ProtoSerializer;
import games.core.user.User;
import message.ThreeCardProto;
import message.XProtos;

import java.io.IOException;
import java.util.*;

import static games.core.constant.ServerCommandConstant.THREE_CARD_COMMAND;

public class TCRoom implements ProtoSerializer<ThreeCardProto.TCRoomInfo>
{
    protected LinkedHashMap<Integer, TCPlayer> players = new LinkedHashMap<>();
    private ArrayList<Integer> deckOfCard = new ArrayList<>();

    private int roomID;
    public long betLevel;
    private String tableOwner;
    public int maxPlayer;
    public int playersJoinedRoom;

    private boolean isNewGame;

    public List<Integer> listOfPreCards = new ArrayList<>();

    public List<Long> theFined = new ArrayList<>();
    public List<TCPlayer> playersIsHIT = new ArrayList<>();

    public Timer timer = new Timer();

    public Timer timerDeal = new Timer();

    public int countPass;

    public int countGoFirst;

    public int countLoser;

    protected LinkedHashMap<String, TCPlayer> playersComeFirst = new LinkedHashMap<>();

    public String winOne = "WinOne";
    public String winTwo = "WinTwo";
    public String lostThree = "LostThree";
    public String lostFour = "LostFour";

    TCPlayer masterClient;
    TCPlayer playerWinOne;

    Cards Cards = new Cards();

    int time;

    int indexOfDeck;

    int indexOfPlayer;

    int countPlayersHaveCard;

    public boolean isActive;

    public  boolean isCountdown;

    public TCRoom(TCPlayer masterClient, int roomId, long money, String tableOwner, int maxPlayerOfRoom, int numberPlayersJoined)
    {
        this.masterClient = masterClient;
        AddPlayerToRoom(masterClient);
        roomID = roomId;
        betLevel = money;
        this.tableOwner = tableOwner;
        maxPlayer = maxPlayerOfRoom;
        playersJoinedRoom = numberPlayersJoined;

        int id;
        for (int i = 3; i < 16; i++) {
            id = i * 10;
            for (int j = 0; j < 4; j++)
                deckOfCard.add(id + j);
        }
        isNewGame = true;
    }

    private void ShuffleTheDeck() {
        Random rand = new Random();

        int index = 0;

        for (int i = 0; i < 52; i++)
        {
            index = rand.nextInt(52);

            int valueA = deckOfCard.get(index);

            int valueB = deckOfCard.get(i);

            deckOfCard.set(i, valueA);

            deckOfCard.set(index, valueB);
        }
    }

    public void DealCards()
    {

        // X??o b??i
        ShuffleTheDeck();

        time = 0;

        indexOfDeck = 0;

        indexOfPlayer = 0;

        int numberRealPlayerInRoom = GetNumberRealPlayerCurrentInRoom();

        TCPlayer[] arrPlayer = players.values().toArray(new TCPlayer[0]);

        timerDeal.cancel();

        TimerTask timerTaskDeal = new TimerTask()
        {
            @Override
            public void run()
            {
                isActive = true;

                // N???u s??? ng?????i c?? b??i c??n b?? h??n s??? ng c?? trong ph??ng th?? ti???p t???c chia b??i
                if (countPlayersHaveCard < numberRealPlayerInRoom)
                {
                    SendCardToClient(arrPlayer, indexOfDeck, time);
                    indexOfDeck++;
                    countPlayersHaveCard++;
                    indexOfPlayer++;
                }
                // N???u s??? ng?????i c?? b??i == s??? ng c?? trong ph??ng th?? chia ti???p c??y b??i th??? 2
                // T????ng t??? s??? chia c??y b??i th??? 3
                else if (countPlayersHaveCard == numberRealPlayerInRoom)
                {
                    countPlayersHaveCard = 0;
                    indexOfPlayer = 0;
                    time++;
                }

                // Sau 3 l???n chia b??a th?? m???i ng?????i ch??i s??? c?? 3 l?? b??i v?? k???t th??c chia b??i
                if (time == 3)
                {
                    ThreeCardProto.TCCards.Builder card = ThreeCardProto.TCCards.newBuilder();
                    SendToAllInRoom(card.build().toByteString(), TCActionConst.TURN_ON_MODE_SEE_CARD);

                    timerDeal.cancel();
                }
            }
        };

        timerDeal = new Timer();
        timerDeal.schedule(timerTaskDeal, 0, 500);
    }

    private void SendCardToClient(TCPlayer[] arrPlayerint, int index, int  ordinalCard)
    {
        if (indexOfPlayer < arrPlayerint.length)
        {
            TCPlayer player = arrPlayerint[indexOfPlayer];

            while (player.getUser() == null)
            {
                indexOfPlayer = indexOfPlayer < arrPlayerint.length - 1 ? indexOfPlayer + 1 : 0;
                player = arrPlayerint[indexOfPlayer];
            }

            ThreeCardProto.TCCards.Builder cards = ThreeCardProto.TCCards.newBuilder();

            int idCard = deckOfCard.get(index);

            cards.setIdCard(idCard);

            cards.setOrdinalCard(ordinalCard);

            cards.setUserId(player.getId());

            SendToAllInRoom(cards.build().toByteString(), TCActionConst.DEAL_CARDS);
        }
    }

    @Override
    public ThreeCardProto.TCRoomInfo.Builder parseProtoBuilder()
    {
        ThreeCardProto.TCRoomInfo.Builder builder = ThreeCardProto.TCRoomInfo.newBuilder();
        builder.setRoomId(roomID);
        builder.setBetLevel(betLevel);
        builder.setTableOwner(tableOwner);
        builder.setMaxPlayersOfRoom(maxPlayer);
        builder.setPlayersJoinedRoom(playersJoinedRoom);
        builder.setIsRoomActive(isActive);

        return builder;
    }

    public void AddPlayerToRoom(TCPlayer player)
    {
        if (!players.containsKey(player.getId()))
            players.put(player.getId(), player);
        else
        {
            players.replace(player.getId(), player);
        }

        isNewGame = true;
    }

    private int GetNumberRealPlayerCurrentInRoom()
    {
        int countRealPLayer = 0;
        for (TCPlayer player : players.values())
        {
            if (player.getUser() != null)
                countRealPLayer++;
        }

        return countRealPLayer;
    }

    public LinkedHashMap<Integer, TCPlayer> GetUsersInCurrentRoom()
    {
        return players;
    }

    // ???????c g???i khi c?? 1 user leave room
    public void RemovePlayerFromRoom(TCPlayer player)
    {
        // Ch??? thay player sang d???ng r???ng ????? k b??? ???nh h?????ng v??? tr?? c???a nh???ng player d?????i client
        int id = player.getId();
        if (players.containsKey(player.getId()))
            players.replace(id, new TCPlayer(id));
    }

    public void SendToAllInRoom(ByteString responseBeanByteString, int responseCode)
    {
        for (TCPlayer player : players.values())
        {
            if (player.getUser() != null)
            {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }

    public void SendToTheRest(int userId, ByteString responseBeanByteString, int responseCode) {
        for (TCPlayer player : players.values())
        {
            if (player.getUser() != null && player.getId() != userId) {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }

    public void SendToMasterClient(ByteString responseBeanByteString, int responseCode)
    {
        for (TCPlayer player : players.values())
        {
            if (player.getUser() != null && player.isMasterClient)
            {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
                return;
            }
        }
    }

    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode)
    {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(THREE_CARD_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);

        ctx.writeAndFlush(responseMessage.build());
    }

    public void SetUserLose(int userId) {
        // ??i???m s??? ng?????i ???? t???i
        countLoser++;

        LinkedHashMap<Integer, TCPlayer> users = GetUsersInCurrentRoom();
        TCPlayer player = users.get(userId);

        if (users.size() == 2) {
            player.isLoser = true;
            playersComeFirst.put(lostFour, player);

            TCPlayer playerWinOne = GetPlayerOfNextTurn(player.getId());
            playersComeFirst.put(winOne, playerWinOne);
        }

        // TH: 3 users or 4 users
        else {
            player.isLoser = true;

            /**
             * TH 1: n???u c?? 1 user ????nh l???ng th?? x??t l?? thua bet v?? n???u tr?????c ???? ???? c?? 2 users
             * t???i r???i th?? ch??? c??n 1 l???i user trong b??n n??n x??t user ???? thua ba
             * N??i c??ch kh??c l?? ???? c?? 2 users t???i r, hi???n t???i ch??? c??n 2 users n??n n???u c?? nguoi ????nh l???ng
             * th?? 1 ng?????i d??nh l???ng thua bet, ng?????i c??n l???i thua ba
             */
            if (!playersComeFirst.containsKey(lostFour)) {
                playersComeFirst.put(lostFour, player);
                if (countGoFirst == 2) {
                    TCPlayer playerLostThree = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(lostThree, playerLostThree);
                }
            }

            /**
             * TH2: n???u ???? c?? 1 user ????nh l???ng r th?? th???ng user ????nh l???ng n??y s??? x??t l?? thua 3
             * V?? n???u trong b??n ch??? c?? 3 users ch??i th?? th???ng c??n l???i s??? ???????c Nh???t
             */
            else if (!playersComeFirst.containsKey(lostThree)) {
                playersComeFirst.put(lostThree, player);
                if (users.size() == 3) {
                    TCPlayer playerWinOne = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(winOne, playerWinOne);
                }
            }

            /**
             *  TH3: n???u ???? c?? 2 th???ng ????nh l???ng tr?????c ???? r???i v?? th???ng n??y l???i ????nh l???ng s??? ???????c x??t t???i nh??
             * Th???ng c??n l???i t???i Nh???t
             */
            else if (!playersComeFirst.containsKey(winTwo)) {
                playersComeFirst.put(winTwo, player);

                TCPlayer playerWinOne = GetPlayerOfNextTurn(player.getId());
                playersComeFirst.put(winOne, playerWinOne);
            }

        }
    }


    public TCPlayer GetPlayerOfNextTurn(int userId)
    {
        TCPlayer[] listPlayer = null;
        listPlayer = players.values().toArray(new TCPlayer[0]);

        for (int i = 0; i < listPlayer.length; i++)
        {
            if (listPlayer[i].getUser() != null && listPlayer[i].getId() == userId)
            {
                // tr??? v??? l?????t ????nh cho th???ng ti???p theo
                int index = i + 1 < listPlayer.length ? i + 1 : 0;

                User newUser = listPlayer[index].getUser();

                while (newUser == null || newUser.getMoneyGold() < betLevel * playersJoinedRoom)
                {
                    index++;
                    if (index >= listPlayer.length) index = 0;
                    newUser = listPlayer[index].getUser();
                }
                return listPlayer[index];
            }
        }

        return null;
    }

    public TCPlayer GetPlayer(int userID)
    {
        if (players.containsKey(userID))
            return players.get(userID);

        return null;
    }

    public int GetMasterPlayerID()
    {
        for (TCPlayer player : players.values())
        {
            if (player.isMasterClient)
                return player.getId();
        }

        return masterClient.getId();
    }

    public void Payment(int masterID)
    {
        // g???i s??? ti???n m???i xu???ng cho c??c client trong ph??ng
        LinkedHashMap<Integer, TCPlayer> listUser = GetUsersInCurrentRoom();
        TCPlayer[] users = listUser.values().toArray(new TCPlayer[0]);
        SendNewMoneyToAllClient(users, masterID);
    }

    public void SendNewMoneyToAllClient(TCPlayer[] users, int masterID)
    {
        ThreeCardProto.TCRoomInfo.Builder roomInfo = ThreeCardProto.TCRoomInfo.newBuilder();
        roomInfo.setMasterID(masterID);

        for (int i = 0; i < users.length; i++)
        {
            ThreeCardProto.TCUserInfo.Builder userInfo = ThreeCardProto.TCUserInfo.newBuilder();
            if (users.length > i && users[i].getUser() != null)
            {
                long newGold = users[i].getUser().getMoneyGold();
                String userName = users[i].getUser().getUserName();
                int id = users[i].getId();
                userInfo.setNickName(userName);
                userInfo.setUserId(id);
                userInfo.setMoney(newGold);
                userInfo.setWinGold(users[i].winMoney);
                roomInfo.addUsers(userInfo);
             }
            else
                roomInfo.addUsers(i, userInfo);
        }

        SendToAllInRoom(roomInfo.build().toByteString(), TCActionConst.PAYMENT);
    }

    public void AddMoney(long winnings, User user, MoneyType moneyType) throws IOException {
        Money money = new Money(winnings, moneyType);
        user.SetMoney(winnings);
        MoneyService.getInstance().addMoneyToSave(money, user, true, "ThreeCard");
    }

    public void SubMoney(long betAmount, User user, MoneyType moneyType) {
        Money money = new Money(betAmount, moneyType);
        MoneyService.getInstance().subMoneyAndSaveMongo(money, user, true, true);
    }

    public void ResetAllParameter() {
        // l???y player t???i nh???t ????? sau khi chia b??i xong s??? set cho player n??y ??i tr?????c
        playerWinOne = playersComeFirst.get(winOne);
        playersComeFirst.clear();

        // Bi???n l??u l???i nh???ng l?? b??i c???a player tr?????c. Xo?? h???t n?? ????? ????nh v??n m???i
        listOfPreCards.removeAll(listOfPreCards);

        // ????y l?? bi???n ?????m nh???ng player ???? b??? or m???t l?????t. Reset v??? 0 ????? t??nh l???i t??? ?????u
        countPass = 0;

        countLoser = 0;

        // ????y l?? bi???n ?????m nh???ng player ???? t???i. Reset v??? 0 ????? t??nh l???i t??? ?????u
        countGoFirst = 0;

        Cards.isThreePairs = false;
        Cards.isFourPairs = false;
        Cards.isFourOfaKind = false;

        for (TCPlayer player : players.values())
            player.isFinishing = false;
    }
}
