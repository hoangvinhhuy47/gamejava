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
        // Xào bài
        ShuffleTheDeck();

        int min = 0;
        int  max = 13;
        int origin = max;

        int idCardMin = 0;

        TKPlayer playerWillGoFirst = null;

        for (TKPlayer player: players.values())
        {
            ThirteenKillerProto.TKCards.Builder cards = ThirteenKillerProto.TKCards.newBuilder();

            List<Integer> idCards = deckOfCard.subList(min, max); // từ min đến max - 1(vd: 0-12)

            if (idCardMin == 0) idCardMin = idCards.get(0);

            for (int i = 0; i < idCards.size(); i++)
            {
                ThirteenKillerProto.TKCardInfo.Builder cardInfo = ThirteenKillerProto.TKCardInfo.newBuilder();

                int idCard = idCards.get(i);

                if (isNewGame && idCardMin >= idCard)
                {
                    // Lấy id của user có lá bài nhỏ nhất,
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

        // Nếu user nào tới nhất sẽ được quyền đi trước
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

        // Ngược lại user có lá bài nhỏ nhất sẽ đi trước
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

    // Được gọi khi có 1 user leave room
    public void RemovePlayerFromRoom(TKPlayer player)
    {
        // Chỉ thay player sang dạng rỗng để k bị ảnh hưởng vị trí của những player dưới client
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
        // Điếm số người đã tới
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
             * TH 1: nếu có 1 user đánh lủng thì xét là thua bet và nếu trước đó đã có 2 users
             * tới rồi thì chỉ còn 1 lại user trong bàn nên xét user đó thua ba
             * Nói cách khác là đã có 2 users tới r, hiện tại chỉ còn 2 users nên nếu có nguoi đánh lủng
             * thì 1 người dánh lủng thua bet, người còn lại thua ba
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
             * TH2: nếu đã có 1 user đánh lủng r thì thằng user đánh lủng này sẽ xét là thua 3
             * Và nếu trong bàn chỉ có 3 users chơi thì thằng còn lại sẽ được Nhất
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
             *  TH3: nếu đã có 2 thằng đánh lủng trước đó rồi và thằng này lại đánh lủng sẽ được xét tới nhì
             * Thằng còn lại tới Nhất
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
        // Điếm số người đã tới
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
           * TH1: nếu thằng này tới Nhất
           * nhưng trước có đó có 2 thằng đánh lủng
           * thì thằng này sẽ được xét tới Nhất thằng còn lại tới Nhì
           *
           * TH2: nếu trong bàn chỉ có 3 thằng và có 1 thằng đánh lủng
           * thì thằng này tới Nhất, thằng còn lại tới Nhì
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
             * TH3: nếu chỉ có 3 users chơi
             * thì thằng này sẽ được xét là thua 3
             * thằng còn lại thua bét
             *
             * TH4: nhưng nếu có 4 users chơi
             * thì nó được xét tới Nhì
             * Và nếu có 1 user trước đánh lủng thì thằng kế tiếp sẽ thua 3
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
                // trả về lượt đánh cho thằng tiếp theo
                int index = i + 1 < listPlayer.length ? i + 1 : 0;

                User newUser = listPlayer[index].getUser();

                // Nếu user thuộc vào những diện này thì sẽ tìm user khác
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
        // Trường hợp có 4 user
        if (playersComeFirst.size() == 4)
        {
            // Tới nhất dc cộng nguyên tiền
            TKPlayer playerWinOne = playersComeFirst.get(winOne);
            playerWinOne.winMoney = betLevel;
            AddMoney(betLevel, playerWinOne.getUser(), MoneyType.GOLD);


            // Tới nhì dc cộng nữa tiền
            TKPlayer playerWinTwo = playersComeFirst.get(winTwo);
            playerWinTwo.winMoney = betLevel / 2;
            AddMoney(betLevel / 2, playerWinTwo.getUser(), MoneyType.GOLD);


            // Tới ba thua nữa tiền
            TKPlayer playerLostThree = playersComeFirst.get(lostThree);
            playerLostThree.winMoney = -(betLevel / 2);
            SubMoney(betLevel / 2, playerLostThree.getUser(), MoneyType.GOLD);


            // Tới bét thua nguyên tiền
            TKPlayer playerLostFour = playersComeFirst.get(lostFour);
            playerLostFour.winMoney = -betLevel;
            playerLostFour.isLostFour = true;
            SubMoney(betLevel, playerLostFour.getUser(), MoneyType.GOLD);
        }

        // Trường hợp 3 user
        else if (playersComeFirst.size() == 3)
        {
            // Tới nhất dc cộng nguyên tiền của thua bét và nữa tiền của tới ba
            TKPlayer playerWinOne = playersComeFirst.get(winOne);
            long winGold = betLevel + (betLevel / 2);
            playerWinOne.winMoney = winGold;
            AddMoney(winGold, playerWinOne.getUser(), MoneyType.GOLD);

            // Tới ba thua nữa tiền
            TKPlayer playerLostThree = playersComeFirst.get(lostThree);
            playerLostThree.winMoney = -(betLevel / 2);
            SubMoney(betLevel / 2, playerLostThree.getUser(), MoneyType.GOLD);


            // Tới bét thua nguyên tiền
            TKPlayer playerLostFour = playersComeFirst.get(lostFour);
            playerLostFour.winMoney = -betLevel;
            playerLostFour.isLostFour = true;
            SubMoney(betLevel, playerLostFour.getUser(), MoneyType.GOLD);
        }

        // Trường hợp 2 user
        else
        {
            // Tới nhất dc cộng nguyên tiền
            TKPlayer playerWinOne = playersComeFirst.get(winOne);
            playerWinOne.winMoney = betLevel;
            AddMoney(betLevel, playerWinOne.getUser(), MoneyType.GOLD);


            // Tới nhì thua nguyên tiền
            TKPlayer playerLostFour = playersComeFirst.get(lostFour);
            playerLostFour.winMoney = -betLevel;
            playerLostFour.isLostFour = true;
            SubMoney(betLevel, playerLostFour.getUser(), MoneyType.GOLD);
        }

        // gửi số tiền mới xuống cho các client trong phòng
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
        // lấy player tới nhất để sau khi chia bài xong sẽ set cho player này đi trước
        playerWinOne = playersComeFirst.get(winOne);
        playersComeFirst.clear();

        // Biến lưu lại những lá bài của player trước. Xoá hết nó để đánh ván mới
        listOfPreCards.removeAll(listOfPreCards);

        // Đây là biến đếm những player đã bỏ or mất lượt. Reset về 0 để tính lại từ đầu
        countPass = 0;

        countLoser = 0;

        countPenalty = 0;

        // Đây là biến đếm những player đã tới. Reset về 0 để tính lại từ đầu
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
