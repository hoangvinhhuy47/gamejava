package games.ThirteenKiller;

import com.google.protobuf.ByteString;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.proto.ProtoSerializer;
import games.core.user.User;
import message.ThirteenKillerProto;
import message.XProtos;

import java.io.IOException;
import java.util.*;

import static games.core.constant.ServerCommandConstant.THIRTEENKILLER_COMMAND;

public class TKRoom implements ProtoSerializer<ThirteenKillerProto.TKRoomInfo>
{
    protected LinkedHashMap<Integer, TKPlayer> players = new LinkedHashMap<Integer, TKPlayer>();
    private ArrayList<Integer> deckOfCard = new ArrayList<>();

    private int roomID;
    public long betLevel;
    private String tableOwner;
    public int maxPlayer;
    public int playersJoinedRoom;

    private boolean isNewGame;

    public List<Integer> listOfPreCards = new ArrayList<>();

    public List<Long> theFined = new ArrayList<>();
    public List<TKPlayer> playersIsHIT = new ArrayList<>();

    public Timer timer = new Timer();
    public   TimerTask timerTask;

    public int countPass;

    public int countGoFirst;

    public int countPenalty;

    public int countLoser;

    protected LinkedHashMap<String, TKPlayer> playersComeFirst = new LinkedHashMap<String, TKPlayer>();

    public String winOne = "WinOne";
    public String winTwo = "WinTwo";
    public String lostThree = "LostThree";
    public String lostFour = "LostFour";

    TKPlayer masterClient;
    TKPlayer playerWinOne;

    Cards Cards = new Cards();

    public TKRoom(TKPlayer masterClient, int roomId, long money, String tableOwner, int maxPlayerOfRoom, int numberPlayersJoined)
    {
        this.masterClient = masterClient;
        AddPlayerToRoom(masterClient);
        roomID = roomId;
        betLevel = money;
        this.tableOwner = tableOwner;
        maxPlayer = maxPlayerOfRoom;
        playersJoinedRoom = numberPlayersJoined;

        int id;
        for (int i = 3; i < 16; i++)
        {
           id = i * 10;
            for (int j = 0; j < 4; j++)
                deckOfCard.add(id + j);
        }
        isNewGame = true;
    }

    private void ShuffleTheDeck()
    {
        Random rand = new Random();

        int index = 0;

        for (int i = 0; i < 52 ; i++)
        {
            index = rand.nextInt(52);

            int valueA = deckOfCard.get(index);

            int valueB = deckOfCard.get(i);

            deckOfCard.set(i, valueA);

            deckOfCard.set(index, valueB);
        }
    }

    private void ShuffleTheDeck2()
    {
        Random rand = new Random();

        int index = 0;

        for (int i = 0; i < 52 ; i++)
        {
            int valueA = deckOfCard.get(index);

            int valueB = deckOfCard.get(i);

            deckOfCard.set(i, valueA);

            deckOfCard.set(index, valueB);

            if (i >= 4)
            {
                index = rand.nextInt(52);
                while (index <= 4)
                    index = rand.nextInt(52);
            }
            else
                index++;
        }
    }

    public void DealCards(TKPlayer playerGoFirst, TKRoom room)
    {
        // X??o b??i
        ShuffleTheDeck();

        int min = 0;
        int  max = 13;
        int origin = max;

        int idCardMin = 0;

        TKPlayer playerWillGoFirst = null;

        for (TKPlayer player: players.values())
        {
            ThirteenKillerProto.TKCards.Builder cards = ThirteenKillerProto.TKCards.newBuilder();

            List<Integer> idCards = deckOfCard.subList(min, max); // t??? min ?????n max - 1(vd: 0-12)

            if (idCardMin == 0) idCardMin = idCards.get(0);

            for (int i = 0; i < idCards.size(); i++)
            {
                ThirteenKillerProto.TKCardInfo.Builder cardInfo = ThirteenKillerProto.TKCardInfo.newBuilder();

                int idCard = idCards.get(i);

                if (isNewGame && idCardMin >= idCard)
                {
                    // L???y id c???a user c?? l?? b??i nh??? nh???t,
                    idCardMin = idCard;
                    playerWillGoFirst = player;
                }

                cardInfo.setCardId(idCard);

                cards.addListCard(cardInfo);
            }

            SendMessage(player.getCtxInfo(), cards.build().toByteString(), TKActionConst.DEAL_CARDS);

            min = max;
            max += origin;
        }

        ThirteenKillerProto.TKUserGoFirst.Builder userGoFirst = ThirteenKillerProto.TKUserGoFirst.newBuilder();

        // N???u user n??o t???i nh???t s??? ???????c quy???n ??i tr?????c
        if (playerGoFirst != null)
        {
            playerWillGoFirst = playerGoFirst;
            userGoFirst.setIsNewGame(false);
        }

        else if(isNewGame)
        {
            userGoFirst.setIsNewGame(true);
            userGoFirst.setCardId(idCardMin);
        }

        // Ng?????c l???i user c?? l?? b??i nh??? nh???t s??? ??i tr?????c
        userGoFirst.setUserId(playerWillGoFirst.getId());

        room.SendToAllInRoom(userGoFirst.build().toByteString(), TKActionConst.SMALL_CARD_GO_FIRST);

        isNewGame = false;
    }


    @Override
    public ThirteenKillerProto.TKRoomInfo.Builder parseProtoBuilder()
    {
        ThirteenKillerProto.TKRoomInfo.Builder builder = ThirteenKillerProto.TKRoomInfo.newBuilder();
        builder.setRoomId(roomID);
        builder.setBetLevel(betLevel);
        builder.setTableOwner(tableOwner);
        builder.setMaxPlayersOfRoom(maxPlayer);
        builder.setPlayersJoinedRoom(playersJoinedRoom);

        return builder;
    }

    public void AddPlayerToRoom(TKPlayer player)
    {
        if (!players.containsKey(player.getId()))
            players.put(player.getId(), player);
        else
        {
            TKPlayer newPlayer = players.get(player.getId());

            if (newPlayer.isMasterClient)
                player.isMasterClient = true;

            players.replace(player.getId(), player);
        }

        isNewGame = true;
    }

    public LinkedHashMap<Integer, TKPlayer> GetUsersInCurrentRoom()
    {
        LinkedHashMap<Integer, TKPlayer> listUser = new LinkedHashMap<Integer, TKPlayer>();
        for (TKPlayer player: players.values())
        {
            if (player.getUser() != null)
                listUser.put(player.getId(), player);
        }
        return listUser;
    }

    // ???????c g???i khi c?? 1 user leave room
    public void RemovePlayerFromRoom(TKPlayer player)
    {
        // Ch??? thay player sang d???ng r???ng ????? k b??? ???nh h?????ng v??? tr?? c???a nh???ng player d?????i client
        if (players.containsKey(player.getId()))
            players.replace(player.getId(), new TKPlayer());
    }

    public void SendToAllInRoom(ByteString responseBeanByteString, int responseCode)
    {
        for (TKPlayer player : players.values())
        {
            if (player.getUser() != null)
            {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }

    public void SendToTheRest(int userId, ByteString responseBeanByteString, int responseCode)
    {
        for (TKPlayer player : players.values())
        {
            if (player.getUser() != null && player.getId() != userId)
            {
                SendMessage(player.getCtxInfo(), responseBeanByteString, responseCode);
            }
        }
    }

    public void SendMessage(ChannelContextInfo ctx, ByteString responseBeanByteString, int responseCode)
    {
        XProtos.XMessage.Builder responseMessage = XProtos.XMessage.newBuilder();
        responseMessage.setCommand(THIRTEENKILLER_COMMAND)
                .setBeanType(responseCode)
                .setData(responseBeanByteString);

        ctx.writeAndFlush(responseMessage.build());
    }

    public void SetUserLose(int userId)
    {
        // ??i???m s??? ng?????i ???? t???i
        countPenalty++;

        LinkedHashMap<Integer, TKPlayer> users = GetUsersInCurrentRoom();
        TKPlayer player = users.get(userId);

        if (users.size() == 2)
        {
            player.isLoser = true;
            playersComeFirst.put(lostFour, player);

            TKPlayer playerWinOne = GetPlayerOfNextTurn(player.getId());
            playersComeFirst.put(winOne, playerWinOne);
        }

        // TH: 3 users or 4 users
        else
        {
            player.isLoser = true;

            /**
             * TH 1: n???u c?? 1 user ????nh l???ng th?? x??t l?? thua bet v?? n???u tr?????c ???? ???? c?? 2 users
             * t???i r???i th?? ch??? c??n 1 l???i user trong b??n n??n x??t user ???? thua ba
             * N??i c??ch kh??c l?? ???? c?? 2 users t???i r, hi???n t???i ch??? c??n 2 users n??n n???u c?? nguoi ????nh l???ng
             * th?? 1 ng?????i d??nh l???ng thua bet, ng?????i c??n l???i thua ba
             */
            if (!playersComeFirst.containsKey(lostFour))
            {
                playersComeFirst.put(lostFour, player);
                if (countGoFirst == 2)
                {
                    TKPlayer playerLostThree = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(lostThree, playerLostThree);
                }
            }

            /**
             * TH2: n???u ???? c?? 1 user ????nh l???ng r th?? th???ng user ????nh l???ng n??y s??? x??t l?? thua 3
             * V?? n???u trong b??n ch??? c?? 3 users ch??i th?? th???ng c??n l???i s??? ???????c Nh???t
             */
            else if (!playersComeFirst.containsKey(lostThree))
            {
                playersComeFirst.put(lostThree, player);
                if (users.size() == 3)
                {
                    TKPlayer playerWinOne = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(winOne, playerWinOne);
                }
            }

            /**
             *  TH3: n???u ???? c?? 2 th???ng ????nh l???ng tr?????c ???? r???i v?? th???ng n??y l???i ????nh l???ng s??? ???????c x??t t???i nh??
             * Th???ng c??n l???i t???i Nh???t
             */
            else if (!playersComeFirst.containsKey(winTwo))
            {
                playersComeFirst.put(winTwo, player);

                TKPlayer playerWinOne = GetPlayerOfNextTurn(player.getId());
                playersComeFirst.put(winOne, playerWinOne);
            }

        }
    }

    public void SetUserGoFirst(int userId) throws IOException
    {
        // ??i???m s??? ng?????i ???? t???i
        countGoFirst++;

        LinkedHashMap<Integer, TKPlayer> users = GetUsersInCurrentRoom();
        TKPlayer player = users.get(userId);

        if (players.size() == 2)
        {
            player.isFinishing = true;
            playersComeFirst.put(winOne, player);

            TKPlayer playerLostFour = GetPlayerOfNextTurn(player.getId());
            playersComeFirst.put(lostFour, playerLostFour);
        }

        else
        {
            player.isFinishing = true;

            /**
           * TH1: n???u th???ng n??y t???i Nh???t
           * nh??ng tr?????c c?? ???? c?? 2 th???ng ????nh l???ng
           * th?? th???ng n??y s??? ???????c x??t t???i Nh???t th???ng c??n l???i t???i Nh??
           *
           * TH2: n???u trong b??n ch??? c?? 3 th???ng v?? c?? 1 th???ng ????nh l???ng
           * th?? th???ng n??y t???i Nh???t, th???ng c??n l???i t???i Nh??
           */
            if (countGoFirst == 1)
            {
                playersComeFirst.put(winOne, player);

                if (countPenalty == 2)
                {
                    TKPlayer playerWinTwo = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(winTwo, playerWinTwo);
                }

                else if (users.size() == 3 && countPenalty == 1)
                {
                    TKPlayer playerWinTwo = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(winTwo, playerWinTwo);
                }
            }

            /**
             * TH3: n???u ch??? c?? 3 users ch??i
             * th?? th???ng n??y s??? ???????c x??t l?? thua 3
             * th???ng c??n l???i thua b??t
             *
             * TH4: nh??ng n???u c?? 4 users ch??i
             * th?? n?? ???????c x??t t???i Nh??
             * V?? n???u c?? 1 user tr?????c ????nh l???ng th?? th???ng k??? ti???p s??? thua 3
             */
            else if (countGoFirst == 2)
            {
                if (users.size() == 3)
                {
                    playersComeFirst.put(lostThree, player);

                    TKPlayer playerLostFour = GetPlayerOfNextTurn(player.getId());
                    playersComeFirst.put(lostFour, playerLostFour);
                }
                else if (users.size() == 4)
                {
                    playersComeFirst.put(winTwo, player);
                    if (countPenalty == 1)
                    {
                        TKPlayer playerLostThree = GetPlayerOfNextTurn(player.getId());
                        playersComeFirst.put(lostThree, playerLostThree);
                    }
                }
            }

            else if (countGoFirst == 3)
            {
                playersComeFirst.put(lostThree, player);

                TKPlayer playerLostFour = GetPlayerOfNextTurn(player.getId());
                playersComeFirst.put(lostFour, playerLostFour);

                /**
                 * Note
                 */
            }

            //ResetTurn();
        }
    }

    private void ResetTurn()
    {
        LinkedHashMap<Integer, TKPlayer> players = GetUsersInCurrentRoom();
        for (TKPlayer player : players.values()) player.isPass = false;

        countPass = 0;

        ThirteenKillerProto.TKUserPass.Builder userPass = ThirteenKillerProto.TKUserPass.newBuilder();
        userPass.setIsPass(false);

        SendToAllInRoom(userPass.build().toByteString(), TKActionConst.OPEN_TURN);
    }

    public int GetCountGoFirst()
    {
        return countGoFirst + countPenalty;
    }

    public TKPlayer GetUserGoFirst()
    {
        if (!isNewGame && playerWinOne != null)
        {
             return playerWinOne;
        }
        return null;
    }

    public TKPlayer GetPlayerOfNextTurn(int userId)
    {
        TKPlayer[] listPlayer = null;
        listPlayer = players.values().toArray(new TKPlayer[0]);

        for (int i = 0; i < listPlayer.length; i++)
        {
            if (listPlayer[i].getUser() != null && listPlayer[i].getId() == userId)
            {
                // tr??? v??? l?????t ????nh cho th???ng ti???p theo
                int index = i + 1 < listPlayer.length ? i + 1 : 0;

                User newUser = listPlayer[index].getUser();

                // N???u user thu???c v??o nh???ng di???n n??y th?? s??? t??m user kh??c
                while (newUser == null ||
                       listPlayer[index].isFinishing ||
                       listPlayer[index].isPass ||
                       listPlayer[index].isLoser)
                {
                    index++;
                    if (index >= listPlayer.length) index = 0;
                    newUser = listPlayer[index].getUser();
                }

                return  listPlayer[index];
            }
        }

        return null;
    }

    public TKPlayer GetPlayer(int userID)
    {
        if (players.containsKey(userID))
            return players.get(userID);

        return null;
    }

    public TKPlayer GetMasterPlayer()
    {
        for (TKPlayer player: players.values())
        {
            if (player.isMasterClient)
                return player;
        }

        TKPlayer[] users = players.values().toArray(new TKPlayer[0]);
        for (int i = 0; i < users.length; i++)
        {
            if (users[i].getUser() != null)
                return users[i];
        }

        return null;
    }

    public void HandleWinOrLose() throws IOException
    {
        // Tr?????ng h???p c?? 4 user
        if (playersComeFirst.size() == 4)
        {
            // T???i nh???t dc c???ng nguy??n ti???n
            TKPlayer playerWinOne = playersComeFirst.get(winOne);
            playerWinOne.winMoney = betLevel;
            AddMoney(betLevel, playerWinOne.getUser(), MoneyType.GOLD);


            // T???i nh?? dc c???ng n???a ti???n
            TKPlayer playerWinTwo = playersComeFirst.get(winTwo);
            playerWinTwo.winMoney = betLevel / 2;
            AddMoney(betLevel / 2, playerWinTwo.getUser(), MoneyType.GOLD);


            // T???i ba thua n???a ti???n
            TKPlayer playerLostThree = playersComeFirst.get(lostThree);
            playerLostThree.winMoney = -(betLevel / 2);
            SubMoney(betLevel / 2, playerLostThree.getUser(), MoneyType.GOLD);


            // T???i b??t thua nguy??n ti???n
            TKPlayer playerLostFour = playersComeFirst.get(lostFour);
            playerLostFour.winMoney = -betLevel;
            playerLostFour.isLostFour = true;
            SubMoney(betLevel, playerLostFour.getUser(), MoneyType.GOLD);
        }

        // Tr?????ng h???p 3 user
        else if (playersComeFirst.size() == 3)
        {
            // T???i nh???t dc c???ng nguy??n ti???n c???a thua b??t v?? n???a ti???n c???a t???i ba
            TKPlayer playerWinOne = playersComeFirst.get(winOne);
            long winGold = betLevel + (betLevel / 2);
            playerWinOne.winMoney = winGold;
            AddMoney(winGold, playerWinOne.getUser(), MoneyType.GOLD);

            // T???i ba thua n???a ti???n
            TKPlayer playerLostThree = playersComeFirst.get(lostThree);
            playerLostThree.winMoney = -(betLevel / 2);
            SubMoney(betLevel / 2, playerLostThree.getUser(), MoneyType.GOLD);


            // T???i b??t thua nguy??n ti???n
            TKPlayer playerLostFour = playersComeFirst.get(lostFour);
            playerLostFour.winMoney = -betLevel;
            playerLostFour.isLostFour = true;
            SubMoney(betLevel, playerLostFour.getUser(), MoneyType.GOLD);
        }

        // Tr?????ng h???p 2 user
        else
        {
            // T???i nh???t dc c???ng nguy??n ti???n
            TKPlayer playerWinOne = playersComeFirst.get(winOne);
            playerWinOne.winMoney = betLevel;
            AddMoney(betLevel, playerWinOne.getUser(), MoneyType.GOLD);


            // T???i nh?? thua nguy??n ti???n
            TKPlayer playerLostFour = playersComeFirst.get(lostFour);
            playerLostFour.winMoney = -betLevel;
            playerLostFour.isLostFour = true;
            SubMoney(betLevel, playerLostFour.getUser(), MoneyType.GOLD);
        }

        // g???i s??? ti???n m???i xu???ng cho c??c client trong ph??ng
        LinkedHashMap<Integer, TKPlayer> listUser = GetUsersInCurrentRoom();
        TKPlayer[] users = listUser.values().toArray(new TKPlayer[0]);
        SendNewMoneyToAllClient(users, TKActionConst.WIN_MONEY);
    }

    public void SendNewMoneyToAllClient(TKPlayer[] players, int code)
    {
        ThirteenKillerProto.TKRoomInfo.Builder roomInfo = ThirteenKillerProto.TKRoomInfo.newBuilder();

        for ( int i = 0; i < 4; i++ )
        {
            ThirteenKillerProto.TKUserInfo.Builder userInfo = ThirteenKillerProto.TKUserInfo.newBuilder();
            if (players.length > i && players[i].getUser() != null)
            {
                long newGold = players[i].getUser().getMoneyGold();
                String userName = players[i].getUser().getUserName();
                int id = players[i].getId();
                userInfo.setNickName(userName);
                userInfo.setUserId(id);
                userInfo.setMoney(newGold);
                userInfo.setWinGold(players[i].winMoney);
                if (players[i].isLostFour)
                    userInfo.setIsLostFour(true);
                roomInfo.addUsers(userInfo);
            }
            else
                roomInfo.addUsers(i, userInfo);
        }

        SendToAllInRoom(roomInfo.build().toByteString(), code);
    }

    public void AddMoney(long winnings, User user, MoneyType moneyType) throws IOException
    {
        Money money = new Money(winnings, moneyType);
        user.SetMoney(winnings);
        MoneyService.getInstance().addMoneyToSave(money, user, true, "ThrirtteenKiller");
    }

    public void SubMoney(long betAmount, User user, MoneyType moneyType)
    {
        Money money = new Money(betAmount, moneyType);
        MoneyService.getInstance().subMoneyAndSaveMongo(money, user, true, true);
    }

    public void ResetAllParameter()
    {
        // l???y player t???i nh???t ????? sau khi chia b??i xong s??? set cho player n??y ??i tr?????c
        playerWinOne = playersComeFirst.get(winOne);
        playersComeFirst.clear();

        // Bi???n l??u l???i nh???ng l?? b??i c???a player tr?????c. Xo?? h???t n?? ????? ????nh v??n m???i
        listOfPreCards.removeAll(listOfPreCards);

        // ????y l?? bi???n ?????m nh???ng player ???? b??? or m???t l?????t. Reset v??? 0 ????? t??nh l???i t??? ?????u
        countPass = 0;

        countLoser = 0;

        countPenalty = 0;

        // ????y l?? bi???n ?????m nh???ng player ???? t???i. Reset v??? 0 ????? t??nh l???i t??? ?????u
        countGoFirst = 0;

        Cards.isThreePairs = false;
        Cards.isFourPairs = false;
        Cards.isFourOfaKind = false;

        for (TKPlayer player: players.values())
        {
            player.isFinishing = false;
            player.isPass = false;
            player.isLoser = false;
        }
    }
}
