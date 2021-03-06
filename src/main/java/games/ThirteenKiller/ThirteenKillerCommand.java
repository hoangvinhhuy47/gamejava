package games.ThirteenKiller;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import games.APISendMoney;
import games.core.application.constant.ICommand;
import games.core.application.handler.ChannelContextInfo;
import games.core.common.money.MoneyType;
import games.core.exception.game.NotFoundPlayerInGame;
import games.core.game.GameErrorCode;
import games.tx.TXErrorCode;
import message.ThirteenKillerProto;
import message.XProtos;

import java.io.IOException;
import java.util.*;

public class ThirteenKillerCommand extends ICommand
{
    private static ThirteenKillerCommand instance;
    private ThirteenKillerGame thirteenKillerGame;
    private int roomID = TKActionConst.ROOM;

    Timer countdown = new Timer();


    public ThirteenKillerCommand(ThirteenKillerGame tkGame) {
        thirteenKillerGame = tkGame;
    }

    public synchronized static ThirteenKillerCommand GetInstance() {
        if (instance == null)
            instance = new ThirteenKillerCommand(ThirteenKillerGame.GetInstance());

        return instance;
    }


    @Override
    public void process(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException, NoSuchFieldException
    {
        if (ctx.checkAuth())
        {
            int request = message.getBeanType();

            switch (request)
            {
                case TKActionConst.GAME_INFO:
                    UpdateSceneLobby(ctx);
                    break;

                case TKActionConst.TRY_JOIN_ROOM:
                    HandleTryJoinRoom(ctx, message);
                    break;

                case TKActionConst.JOIN_ROOM:
                    HandleJoinRoom(ctx, message);
                    break;

                case TKActionConst.LEAVE_GAME:
                    HandleLeaveGame(ctx, message);
                    break;

                case TKActionConst.CREATE_ROOM:
                    HandleCreateRoom(ctx, message);
                    break;

                case TKActionConst.COUNT_DOWN:
                    HandleCountDown(ctx, message);
                    break;

                case TKActionConst.DEAL_CARDS:
                    HandleDealCards(message);
                    break;

                case TKActionConst.THROW_CARDS:
                    HandleThrowCards(ctx, message);
                    break;

                case TKActionConst.GO_FIRST:
                    HandleGoFirst(ctx, message);
                    break;

                case TKActionConst.TIME_TO_THROW_CARDS:
                    HandleBloodBar(message);
                    break;

                case TKActionConst.NEW_TURN:
                    HandleNewTurn(message);
                    break;

                case TKActionConst.FLIP_CARDS:
                    HandleFlipCard(message);
                    break;

                case TKActionConst.JOIN_ROOM_RANDOM:
                     HandleJoinRoomRandom(ctx);
                     break;
            }
        }
    }

    private void HandleJoinRoomRandom(ChannelContextInfo ctx)
    {
        long gold = ctx.getUser().getMoneyGold();

        TKRoom room = thirteenKillerGame.GetRoomRandom(gold);


        if (room != null)
        {
            ThirteenKillerProto.TKRoomInfo.Builder builder = room.parseProtoBuilder();

            room.playersJoinedRoom++;

            thirteenKillerGame.AddPlayer(ctx.getUser());
            thirteenKillerGame.JoinRoom(ctx.getUser(), room);

            builder.setErrorCode(TKActionConst.SUCCESS);

            ByteString roomInfo = builder.build().toByteString();
            thirteenKillerGame.SendMessage(ctx, roomInfo, TKActionConst.TRY_JOIN_ROOM);
        }

        else
        {
            ThirteenKillerProto.TKRoomInfo.Builder builder = ThirteenKillerProto.TKRoomInfo.newBuilder();
            builder.setErrorCode(TKActionConst.NOT_FOUND_ROOM);
            ByteString roomInfo = builder.build().toByteString();
            thirteenKillerGame.SendMessage(ctx, roomInfo, TKActionConst.TRY_JOIN_ROOM);
        }
    }

    private void HandleFlipCard(XProtos.XMessage message) throws IOException
    {
        // L???y data t??? client
        ThirteenKillerProto.TKCards data = ThirteenKillerProto.TKCards.parseFrom(message.getData());
        List<ThirteenKillerProto.TKCardInfo> cardInfo = data.getListCardList();

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());
        List<Integer> cardListOfFines = FindThreePairsOrFourPairs(cardInfo, room);

        ThirteenKillerProto.TKCards.Builder build = ThirteenKillerProto.TKCards.newBuilder();
        int userID = data.getUserId();
        build.setUserId(userID);
        build.addAllListCard(cardInfo);
        if (cardListOfFines.size() > 0)
            build.addAllCardListOfFines(cardListOfFines);

        //g???i nh???ng l?? b??i cho nh???ng user kh??c xem
        room.SendToTheRest(userID, build.build().toByteString(), TKActionConst.FLIP_CARDS);

        // Reset l???i t???t c??? c??c tham s??? ????? b???t ?????u v??n m???i
        ResetAllParameter(room);
    }

    private List<Integer> FindThreePairsOrFourPairs(List<ThirteenKillerProto.TKCardInfo> cardInfo, TKRoom room) throws IOException
    {
        long totalFined = 0;
        List<Integer> cardListOfFines = new ArrayList<>();
        List<Integer> idCards = new ArrayList<>();

        // Check heo
        for (ThirteenKillerProto.TKCardInfo card : cardInfo)
        {
            int id = card.getCardId();
            idCards.add(id);
            if (id == 150 || id == 151)         // id = 150 v?? 151 l?? heo ??en n??n ph???t n???a ti???n
            {
                totalFined += (room.betLevel / 2);
                cardListOfFines.add(id);
            }
            else if (id == 152 || id == 153)    // id = 152 v?? 153 l?? heo ????? n??n ph???t ????? ti???n
            {
                totalFined += room.betLevel;
                cardListOfFines.add(id);
            }
        }

        // Check 3 th??ng
        if (idCards.size() >= 6)
        {
            /** C??ng th???c ch??? l???y 6 c??y ra ????? x??t l???n l?????t cho ?????n khi h???t b??i
              * V?? d??? c?? 7 l?? b??i(t???c l?? c?? 7 ph???n t??? b???t ?????u t??? 0 - 6) th?? ta l???y 6 c??y ra dc 2 l???n (l1: 0-5, l2: 1-6)
              * V?? d??? c?? 9 l?? (t???c l?? c?? 9 ph???n t??? t??? 0 - 8) th?? ta l???y 6 c??y ra dc 4 l???n (l1: 0-5, l2: 1-6, l3: 2-7, l4:3-8)
            **/
            List<Integer> aPart = null;
            for (int i = 0; i <= idCards.size() - 6; i++)
            {
                aPart = idCards.subList(i, i + 6);
                boolean isThreePairs = CheckThreePairOrFourPair(aPart);
                if (isThreePairs)
                {
                    totalFined += (room.betLevel + room.betLevel / 2);
                    cardListOfFines.addAll(aPart);
                }
            }
        }

        if (idCards.size() >= 4)
        {
            List<Integer> aPart = null;
            for (int i = 0; i <= idCards.size() - 4; i++)
            {
                aPart = idCards.subList(i, i + 4);
                boolean isFourOFaKind = CheckFourOfaKind(aPart);
                if (isFourOFaKind)
                {
                    totalFined += (room.betLevel * 2);
                    cardListOfFines.addAll(aPart);
                }
            }
        }

        // C???ng ti???n cho ng?????i t???i ba v?? tr??? ti???n ng?????i thua b??t b??? ch???t heo ho???c h??ng
        if (totalFined > 0)
        {
            TKPlayer playerLostThree = null;
            TKPlayer playerLostFour = null;
            if (room.playersComeFirst.size() == 2)
            {
                playerLostThree = room.playersComeFirst.get(room.winOne);
                playerLostFour = room.playersComeFirst.get(room.lostFour);
            }
            else
            {
                playerLostThree = room.playersComeFirst.get(room.lostThree);
                playerLostFour = room.playersComeFirst.get(room.lostFour);
            }


            playerLostThree.winMoney = totalFined;
            playerLostFour.winMoney = -totalFined;

            room.AddMoney(totalFined, playerLostThree.getUser(), MoneyType.GOLD);
            room.SubMoney(totalFined, playerLostFour.getUser(), MoneyType.GOLD);

            List<TKPlayer> users = new ArrayList<>();
            users.add(playerLostThree);
            users.add(playerLostFour);


            TKPlayer[] players = users.toArray(new TKPlayer[0]);

            // g???i s??? ti???n m???i xu???ng cho c??c client trong ph??ng
            room.SendNewMoneyToAllClient(players, TKActionConst.THEFINED);
        }

        return cardListOfFines;
    }

    private void HandleNewTurn(XProtos.XMessage message) throws IOException
    {
        ThirteenKillerProto.TKUserPass data = ThirteenKillerProto.TKUserPass.parseFrom(message.getData());

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());

        // user ???? b??? l?????t
        TKPlayer player = room.GetPlayer(data.getUserId());
        player.isPass = data.getIsPass();
        if (player.isPass)
            room.countPass++;

        // user s??? ??i l?????t ti???p theo
        TKPlayer playerOfNextTurn = room.GetPlayerOfNextTurn(data.getUserId());

        BloodBarsTaperedOff(playerOfNextTurn.getId(), room);


        LinkedHashMap<Integer, TKPlayer> users = room.GetUsersInCurrentRoom();
        int size = users.size();
        // N???u c?? 4 user m?? 3 user ???? pass th?? m??? l?????t ????nh l???i cho t???t c??? ng?????i ch??i
        if (room.countPass + room.countLoser + room.countGoFirst >= size - 1)
        {
            // x??? l?? ti???n ph???t heo, 3 ????i th??ng ho???c t??? qu??,... n???u c??
            if (room.Cards.isFourOfaKind || room.Cards.isThreePairs || room.Cards.isFourPairs)
                HandleFined(room);

            // reset l???i l?????t cho t???t c??? ng?????i ch??i
            ResetTurn(room, users);
        }

    }

    private void ResetTurn(TKRoom room, LinkedHashMap<Integer, TKPlayer> users)
    {
        room.theFined.clear();
        room.playersIsHIT.clear();
        room.listOfPreCards.clear();

        for (TKPlayer user : users.values()) user.isPass = false;

        room.countPass = 0;

        room.Cards.isThreePairs = false;
        room.Cards.isFourPairs = false;
        room.Cards.isFourOfaKind = false;

        ThirteenKillerProto.TKUserPass.Builder userPass = ThirteenKillerProto.TKUserPass.newBuilder();
        userPass.setIsPass(false);

        room.SendToAllInRoom(userPass.build().toByteString(), TKActionConst.OPEN_TURN);
    }

    private void HandleFined(TKRoom room) throws IOException
    {
        if(room.playersIsHIT.size() < 2)
            return;

        long totalFined = 0;

        // size - 1 ????? k t??nh s??? gold c???a 3 th??ng or t??? qu?? c???a player ???? win,
        // v?? trong qu?? tr??nh c???ng ti???n ph???t ???? c???ng lu??n c???a player c?? 3 th??ng or t??? qu?? dc win
        for (int i = 0; i < room.theFined.size() - 1; i++)
        {
            totalFined += room.theFined.get(i);
        }

        TKPlayer[] players = room.playersIsHIT.toArray(new TKPlayer[0]);

        TKPlayer playerWin = players[players.length - 1]; // player n??y cu???i c??ng trong room.playersIsHIT l?? ng?????i ra b??i sau c??ng n??n s??? th???ng s??? gold ph???t
        TKPlayer playerLose = players[players.length - 2];// player n??y k??? cu???i trong room.playersIsHIT l?? ng?????i ra h??ng or heo l???n nh???t n??n ch???u h???t


        room.AddMoney(totalFined, playerWin.getUser(), MoneyType.GOLD); // C???ng ti???n cho th???ng ch???t
        room.SubMoney(totalFined, playerLose.getUser(), MoneyType.GOLD); // Tr??? ti???n th???ng b??? ch???t

        playerWin.winMoney = totalFined;
        playerLose.winMoney = -totalFined;

        List<TKPlayer> users = new ArrayList<>();
        users.add(playerWin);
        users.add(playerLose);
        TKPlayer[] arUser = users.toArray(new TKPlayer[0]);
        // g???i s??? ti???n m???i xu???ng cho c??c client trong ph??ng
        room.SendNewMoneyToAllClient(arUser, TKActionConst.THEFINED);

        // Reset l???i s??? ti???n ph???t v?? player b??? ph???t
        room.theFined.clear();
        room.playersIsHIT.clear();
    }
    private void BloodBarsTaperedOff(int userId, TKRoom room) {
        TKPlayer newPlayer = new TKPlayer();

        newPlayer.timeToThrowCard = 0;

        // Hu??? timer c?? tr?????c khi th???c hi???n timer m???i
        room.timer.cancel();

        room.timer = new Timer();

        room.timerTask = new TimerTask() {
            @Override
            public void run() {
                newPlayer.timeToThrowCard += 0.016f;
                float time = newPlayer.timeToThrowCard;
                if (time <= TKActionConst.TIME_TO_THROW_CARD) {
                    TimeToThrowCards(userId, room, time, TKActionConst.TIME_TO_THROW_CARDS);
                } else {
                    room.timer.cancel();
                }
            }
        };

        room.timer.schedule(room.timerTask, 0, 16);
    }

    private void HandleBloodBar(XProtos.XMessage message) throws InvalidProtocolBufferException {
        ThirteenKillerProto.TKCountdown data = ThirteenKillerProto.TKCountdown.parseFrom(message.getData());

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());

        BloodBarsTaperedOff(data.getUserId(), room);
    }

    private void HandleGoFirst(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException
    {
        ThirteenKillerProto.TKUserPenalty user = ThirteenKillerProto.TKUserPenalty.parseFrom(message.getData());

        int roomId = user.getRoomId();

        TKRoom room = thirteenKillerGame.getRoomByID(roomId);

        if (user.getIsLoser())
            room.SetUserLose(ctx.getUserId());
        else
            room.SetUserGoFirst(ctx.getUserId());

        // L???y s??? ng?????i ???? t???i
        int countGoFirst = room.GetCountGoFirst();

        LinkedHashMap<Integer, TKPlayer> users = room.GetUsersInCurrentRoom();
        int size = users.size();

        // ???? ????nh xong v??n b??i
        if (countGoFirst == size - 1)
        {
            FinishGame(room, roomId);
        }
    }

    public void FinishGame(TKRoom room, int roomId) throws IOException
    {
        // T???t time ???????c n??m b??i
        room.timer.cancel();

        // X??? l?? th???ng thua v?? chung ti???n
        room.HandleWinOrLose();

        // y??u c???u master client g???i y/c ??i???m ng?????c ????? cbi b???t ?????u v??n m???i
        ReCountDown(room.masterClient.getId(), roomId, room);
    }

    private void ReCountDown(int userId, int roomID, TKRoom room)
    {
        ThirteenKillerProto.TKCountdown.Builder countdown = ThirteenKillerProto.TKCountdown.newBuilder();

        countdown.setRoomId(roomID);

        countdown.setUserId(userId);

        // Chu???n b??? b???t ?????u v??n m???i n??n y??u c???u master client y/c ??i???m ng?????c
        room.SendToAllInRoom(countdown.build().toByteString(), TKActionConst.COUNT_DOWN_AGAIN);
    }

    private void ResetAllParameter(TKRoom room)
    {
        room.ResetAllParameter();
    }

    private void HandleThrowCards(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        // L???y data t??? client
        ThirteenKillerProto.TKCards data = ThirteenKillerProto.TKCards.parseFrom(message.getData());
        List<ThirteenKillerProto.TKCardInfo> idCards = data.getListCardList();

        // L???y danh s??ch id Card t??? client g???i l??n
        List<Integer> newCards = new ArrayList<>();
        for (ThirteenKillerProto.TKCardInfo idCard : idCards)
            newCards.add(idCard.getCardId());

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());

        ThirteenKillerProto.TKCards.Builder build = ThirteenKillerProto.TKCards.newBuilder();
        int userId = data.getUserId();
        build.setUserId(userId);

        // Check c??c l?? b??i c?? h???p l??? kh??ng
        boolean check = CheckPenalty(newCards, room);
        if (check)
        {
            int errorCode = CheckErrorCode(newCards, room, ctx.getUserId());
            build.setErrorCode(errorCode);
        }
        else
        {
            build.setErrorCode(TKActionConst.PENALTY);
            room.countPass++;
        }

        //g???i nh???ng l?? b??i m?? user hi???n t???i ????nh ra cho nh???ng user kh??c xem
        build.addAllListCard(idCards);
        room.SendToTheRest(userId, build.build().toByteString(), TKActionConst.THROW_CARDS);
    }

    private boolean CheckPenalty(List<Integer> newCards, TKRoom room)
    {
        switch (newCards.size())
        {
            case 2:
                return TwoCards(newCards);

            case 3:
                return ThreeCards(newCards);

            case 4:
                return FourCard(newCards, room);

            case 5:
            case 7:
            case 9:
            case 10:
            case 11:
            case 12:
                return FiveCards(newCards);

            case 6:
            case 8:
                return SixCards(newCards, room);
        }

        return true;
    }

    private boolean SixCards(List<Integer> newCards, TKRoom room) {
        // Ki???m tra ph???i l?? s???nh li???n k
        int count = 0;
        for (int i = newCards.size() - 1; i > 0; i--) {
            int idCard1 = newCards.get(i) / 10;
            int idCard2 = newCards.get(i - 1) / 10;

            int result = idCard1 - idCard2;

            if (result == 1)
                count++;
        }
        // vd: s???nh l?? 5-6-7-8-9-10 th?? 10-9=1(count=1); 9-8=1(count=2); 8-7=1(count=3); 7-6=1(count=4); 6-5=1(count=5)
        // count = 5 v?? newCards.size() - 1 = 5
        // Th?? l?? s???nh li???n v?? tr??? v??? true
        if (count == (newCards.size() - 1))
            return true;

        else {
            boolean check = CheckThreePairOrFourPair(newCards);

            if (!check) return false;

            // Xu???ng ????y l?? n?? ???? tho?? 3 ????i th??ng or 4 ????i th??ng r... n??n ch??? c???n bi???t n?? bao nhi??u l?? n???a l?? bi???t
            if (newCards.size() == 6)
                room.Cards.isThreePairs = true;

            else
                room.Cards.isFourPairs = true;

        }
        return true;
    }

    private boolean FiveCards(List<Integer> newCards) {
        // N???u l?? 5 c??y s???nh li???n th?? c??y 5-4= 1 v?? 4-3=1.... 2-1=1
        // N???u c?? c??y n??o tr??? nhau ra kh??c 1 l?? l???ng
        // vd: s???nh 5-6-7-8-9- th?? 9-8=1; 8-7=1; 7-6=1; 6-5=1
        for (int i = newCards.size() - 1; i > 0; i--) {
            int idCard1 = newCards.get(i) / 10;
            int idCard2 = newCards.get(i - 1) / 10;

            int result = idCard1 - idCard2;

            if (result != 1)
                return false;
        }

        return true;
    }

    private boolean FourCard(List<Integer> newCards, TKRoom room) {
        int idCard1 = newCards.get(0) / 10;
        int idCard2 = newCards.get(1) / 10;
        int idCard3 = newCards.get(2) / 10;
        int idCard4 = newCards.get(3) / 10;

        // N???u l?? t??? qu??
        if (idCard1 == idCard2 && idCard2 == idCard3 && idCard3 == idCard4)
        {
            room.Cards.isFourOfaKind = true;
            return true;
        }

        // 4 c??y s???nh li???n
        else return idCard4 - idCard3 == 1 && idCard3 - idCard2 == 1 && idCard2 - idCard1 == 1;

    }

    private boolean ThreeCards(List<Integer> newCards) {
        int idCard1 = newCards.get(0) / 10;
        int idCard2 = newCards.get(1) / 10;
        int idCard3 = newCards.get(2) / 10;

        // 3 c??y c??ng qu??n b??i vd: 333
        if (idCard1 == idCard2 && idCard2 == idCard3)
            return true;

            // 3 c??y s???nh li???n
        else return idCard3 - idCard2 == 1 && idCard2 - idCard1 == 1;

    }

    private boolean TwoCards(List<Integer> newCards) {
        int idCard1 = newCards.get(0) / 10;
        int idCard2 = newCards.get(1) / 10;

        // N???u l?? ????i m?? 2 c??y l?? 2 qu??n b??i kh??c nhau s??? sai
        return idCard1 == idCard2;
    }

    private int CheckErrorCode(List<Integer> newCards, TKRoom room, int userId)
    {
        int sizeNewCard = newCards.size();
        int sizeOldCard = room.listOfPreCards.size();

        // N???u listOfPreCards ch??a c?? l?? n??o ngh??a l?? v??n m???i th?? th??m v??o
        if (room.listOfPreCards.size() == 0)
            room.listOfPreCards.addAll(newCards);

        else if (sizeNewCard < sizeOldCard || sizeNewCard > sizeOldCard)
        {
            boolean check = CheckCard(room, newCards);
            if (!check)
            {
                room.countLoser++;
                return TKActionConst.PENALTY;
            }
        }

        // Ng?????c l???i s??? l?????ng b???ng nhau... Good, gi??? x??t ?????n qu??n b??i
        else
        {
            int indexLast = room.listOfPreCards.size() - 1;
            for (int j = indexLast; j >= 0; j--)
            {
                /* card s??? c?? d???ng l?? Id = 30-31-32-33, 40-41-42-43, 50-53, 60-63,... ?????n 150-153
                // ?????u s??? th??? hi???n qu??n b??i, ??u??i s??? th??? hi???n n?????c b??i
                // chia h???t cho 10 ????? l???y s??? ?????u s???
                 */
                int idCard_Prefix = newCards.get(j) / 10; // vd: 30/10 = 3 qu??n b??i l?? con 3(d?? 0 l?? b??ch)
                int idPreCard_Prefix = room.listOfPreCards.get(j) / 10; // vd: 33/10 = 3 qu??n b??i l?? con 3(d?? 3 l?? r??)

                // N???u qu??n b??i m???i nh??? h??n qu??n b??i c?? th?? x??t ????nh l???ng lun...kh???i x??t ti???p
                if (idCard_Prefix < idPreCard_Prefix)
                {
                    room.countLoser++;
                    return TKActionConst.PENALTY;
                }

                /* N???u b???ng nhau v?? l?? c??y b??i cu???i
                // Th?? n?????c m???i ph???i l???n h??n n?????c c??, c??n nh???ng c??y b??i sau nh??? h??n c??ng k sao
                // N???u l???n h??n th?? qu?? t???t k c???n x??t...kkk
                 */
                if (idCard_Prefix == idPreCard_Prefix && j == indexLast) {
                    int idCard_Suffixes = newCards.get(j) % 10;
                    int idPreCard_Suffixes = room.listOfPreCards.get(j) % 10;

                    // N???u n?????c m???i nh??? h??n n?????c c?? th?? l???ng cmnr
                    if (idCard_Suffixes < idPreCard_Suffixes)
                    {
                        room.countLoser++;
                        return TKActionConst.PENALTY;
                    }
                }
            }
        }

        // N???u h???p l??? th?? th??m nh???ng l?? b??i m???i v??o
        room.listOfPreCards.removeAll(room.listOfPreCards);
        room.listOfPreCards.addAll(newCards);

        if (room.Cards.isFourOfaKind ||
            room.Cards.isThreePairs ||
            room.Cards.isFourPairs ||
            room.listOfPreCards.get(0) >= 150) // id card 150-153 l?? heo
            RecordTheFined(room.listOfPreCards, userId, room);

        return TKActionConst.SUCCESS;
    }

    private boolean CheckCard(TKRoom room, List<Integer> newCards)
    {
        if (room.Cards.isThreePairs)
        {
            // Ch???t heo
            if (room.listOfPreCards.size() == 1)
            {
                // L?? heo th?? ok
                // 150 - 153 l?? heo
                // Kh??ng ph???i th?? ch???t l???ng
                return room.listOfPreCards.get(0) >= 150;
            }

            // Ch???t 3 th??ng nh??? h??n
            else if (room.listOfPreCards.size() == 6)
            {
                boolean isThreePair = CheckThreePairOrFourPair(room.listOfPreCards);
                // N???u l?? 3 ????i th??ng th?? check xem b??i m???i c?? l???n h??n b??i c?? k
                // N???u nh??? h??n th?? return false ????? b??o l???ng
                if (isThreePair)
                    return CheckLastTwoCards(newCards, room.listOfPreCards);

                return false;
            }
        }

        else if (room.Cards.isFourOfaKind)
        {
            // Ch???t heo
            if (room.listOfPreCards.size() == 1)
            {
                // 150 - 153 l?? heo
                return room.listOfPreCards.get(0) >= 150;
            }

            // Ch???t t??? qu?? nh??? h??n
            else if (room.listOfPreCards.size() == 4)
            {
                boolean isFourOfaKind = CheckFourOfaKind(room.listOfPreCards);
                // So s??nh 2 t??? qu?? coi b??i m???i c?? nh??? h??n b??i c?? k, n???u nh??? h??n b??o false ????? bi???t l?? ????nh l???ng
                if (isFourOfaKind)
                    return CheckLastTwoCards(newCards, room.listOfPreCards);

                return false;
            }

            // Ch???t 3 th??ng
            else if (room.listOfPreCards.size() == 6)
            {
                // N???u b??i c?? l?? 3 th??ng th?? return true kh???i x??t t??p v?? t??? qu?? l???n h??n 3 th??ng
                return CheckThreePairOrFourPair(room.listOfPreCards);
            }
        }


        else if (room.Cards.isFourPairs)
        {
            // Ch???t heo
            if (room.listOfPreCards.size() == 1)
                return room.listOfPreCards.get(0) >= 150;

            // Ch???t t??? qu??
            else if (room.listOfPreCards.size() == 4)
                return CheckFourOfaKind(room.listOfPreCards);

            // Ch???t 3 th??ng
            else if (room.listOfPreCards.size() == 6)
                return CheckThreePairOrFourPair(room.listOfPreCards);

            // Ch???t 4 th??ng nh??? h??n
            else if(room.listOfPreCards.size() == 8)
            {
                boolean isFourPair = CheckThreePairOrFourPair(room.listOfPreCards);
                if (isFourPair)
                    return CheckLastTwoCards(newCards, room.listOfPreCards);
            }
        }

        return false;
    }

    private boolean CheckLastTwoCards(List<Integer> newCards, List<Integer> oldCards)
    {
        int indexLast = oldCards.size() - 1;

        /* card s??? c?? d???ng l?? Id = 30-31-32-33, 40-41-42-43, 50-53, 60-63,... ?????n 150-153
            // ?????u s??? th??? hi???n qu??n b??i, ??u??i s??? th??? hi???n n?????c b??i
            // chia h???t cho 10 ????? l???y s??? ?????u s???
        */
        int idCard_Prefix = newCards.get(indexLast) / 10; // vd: 30/10 = 3 qu??n b??i l?? con 3(d?? 0 l?? b??ch)
        int idPreCard_Prefix = oldCards.get(indexLast) / 10; // vd: 33/10 = 3 qu??n b??i l?? con 3(d?? 3 l?? r??)

        // N???u qu??n b??i m???i nh??? h??n qu??n b??i c?? th?? x??t ????nh l???ng lun...kh???i x??t ti???p
        if (idCard_Prefix < idPreCard_Prefix)
             return false;

        /* N???u b???ng nhau v?? l?? c??y b??i cu???i
            // Th?? n?????c m???i ph???i l???n h??n n?????c c??, c??n nh???ng c??y b??i sau nh??? h??n c??ng k sao
            // N???u l???n h??n th?? qu?? t???t k c???n x??t...kkk
        */
        if (idCard_Prefix == idPreCard_Prefix)
        {
             int idCard_Suffixes = newCards.get(indexLast) % 10;
             int idPreCard_Suffixes = oldCards.get(indexLast) % 10;

             // N???u n?????c m???i nh??? h??n n?????c c?? th?? l???ng cmnr
            return idCard_Suffixes >= idPreCard_Suffixes;
        }

        return true;
    }

    private void RecordTheFined(List<Integer> newCards, int userId, TKRoom room)
    {
        TKPlayer player = room.GetPlayer(userId);

        int sizeNewCard = newCards.size();

        // Heo
        if (sizeNewCard == 1)
        {
            int idCard = newCards.get(0);

            // 150 - 153 l?? id card c???a heo
            if (idCard == 150 || idCard == 151)
            {
                room.theFined.add(room.betLevel / 2); // Heo ??en n???a ti???n
                room.playersIsHIT.add(player);
            }

            else if (idCard == 152 || idCard == 153)
            {
                room.theFined.add(room.betLevel) ;// Heo ????? ????? ti???n
                room.playersIsHIT.add(player);
            }
        }

        // ????i heo
        else if (sizeNewCard == 2)
        {
            int count = 0;
            for (int idCard : newCards)
            {
                if (idCard == 150 || idCard == 151) count++;
                else if (idCard == 152 || idCard == 153) count += 2;
            }
            switch (count)
            {
                case 2:
                    room.theFined.add(room.betLevel) ;// 2 ??en l?? 1 c?????c
                    room.playersIsHIT.add(player);
                    break;
                case 3:
                    room.theFined.add(room.betLevel + room.betLevel / 2) ; // 1 ????? 1 ??en l?? 1.5 c?????c
                    room.playersIsHIT.add(player);
                    break;
                case 4:
                    room.theFined.add(room.betLevel * 2) ; // 2 ????? l?? 2 c?????c
                    room.playersIsHIT.add(player);
                    break;
            }

        }

        // T??? qu??
        else if (sizeNewCard == 4)
        {
            boolean isFourOfaKind = CheckFourOfaKind(newCards);

            if (isFourOfaKind)
            {
                room.theFined.add(room.betLevel * 2) ; // t??? qu?? l?? 2 c?????c
                room.playersIsHIT.add(player);
            }
        }

        // 3 th??ng
        else if (sizeNewCard == 6)
        {
            boolean isThreePair = CheckThreePairOrFourPair(newCards);

            if (isThreePair)
            {
                room.theFined.add(room.betLevel  + (room.betLevel / 2)) ; // 3 th??ng l?? 1.5 c?????c
                room.playersIsHIT.add(player);
            }
        }

        // 4 th??ng
        else if (sizeNewCard == 8)
        {
            boolean isFourPair = CheckThreePairOrFourPair(newCards);

            if (isFourPair)
            {
                room.theFined.add(room.betLevel * 4) ; // 4 th??ng l?? 4 c?????c
                room.playersIsHIT.add(player);
            }
        }

    }

    private boolean CheckThreePairOrFourPair(List<Integer> oldCards)
    {
        for (int j = 0; j < oldCards.size() - 1; j += 2)
        {
            int idCard1 = oldCards.get(j) / 10;
            int idCard2 = oldCards.get(j + 1) / 10;

            // N???u 2 qu??n b??i kh??c nhau th?? k ph???i ????i tr??? v??? false
            if (idCard1 != idCard2)
                return false;

            // N???u l?? ????i ki???m tra ph???i v???a ????i v???a s???nh k
            // N???u card2 - card1 kh??c 1 th?? kh??ng ph???i s???nh tr??? v??? false
            int index = j + 2;
            if (index < oldCards.size()) {
                idCard2 = oldCards.get(j + 2) / 10;
                if (idCard2 - idCard1 != 1)
                    return false;
            }
        }

        return true;
    }

    private boolean CheckFourOfaKind(List<Integer> oldCards)
    {
        int idCard1 = oldCards.get(0) / 10;
        int idCard2 = oldCards.get(1) / 10;
        int idCard3 = oldCards.get(2) / 10;
        int idCard4 = oldCards.get(3) / 10;

        return idCard1 == idCard2 && idCard2 == idCard3 && idCard3 == idCard4;
    }

    private void HandleDealCards(XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThirteenKillerProto.TKRoomInfo roomInfo = ThirteenKillerProto.TKRoomInfo.parseFrom(message.getData());

        int roomId = roomInfo.getRoomId();

        TKRoom room = thirteenKillerGame.getRoomByID(roomId);

        TKPlayer player = room.GetUserGoFirst();

        room.DealCards(player, room);

        countdown.cancel();
    }

    private void HandleCountDown(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException {
        ThirteenKillerProto.TKCountdown data = ThirteenKillerProto.TKCountdown.parseFrom(message.getData());

        TKPlayer masterClient = thirteenKillerGame.GetPlayer(data.getUserId());

        masterClient.timerCountdown = TKActionConst.TIME_TO_START_GAME;

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());

        countdown.cancel();

        countdown = new Timer();

        TimerTask newTimerTask = new TimerTask() {
            @Override
            public void run() {
                int timeCountdown = masterClient.timerCountdown--;
                if (timeCountdown >= 0) {
                    TimeToThrowCards(ctx.getUserId(), room, (float) timeCountdown, TKActionConst.COUNT_DOWN);
                } else {
                    countdown.cancel();
                }
            }
        };

        countdown.schedule(newTimerTask, 0, 1000);
    }

    private void TimeToThrowCards(int userId, TKRoom room, float countdown, int responseCode)
    {
        ThirteenKillerProto.TKCountdown.Builder countDown = ThirteenKillerProto.TKCountdown.newBuilder();

        countDown.setUserId(userId);

        countDown.setTime(countdown);

        room.SendToAllInRoom(countDown.build().toByteString(), responseCode);
    }
    private void HandleCreateRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThirteenKillerProto.TKRoomInfo room = ThirteenKillerProto.TKRoomInfo.parseFrom(message.getData());
        roomID++;
        long betLevel = room.getBetLevel();
        String tableOwner = room.getTableOwner();
        int maxPlayerOfRoom = room.getMaxPlayersOfRoom();
        int playerJoined = room.getPlayersJoinedRoom();

        thirteenKillerGame.AddRoom(ctx.getUser(), roomID, betLevel, tableOwner, maxPlayerOfRoom, playerJoined);
    }

    private void UpdateSceneLobby(ChannelContextInfo ctx)
    {
        int idAvatar = Integer.parseInt(ctx.getUser().getAvatar());

        TKPlayer player = thirteenKillerGame.GetPlayerLobby????(ctx.getUserId());
        if (player == null)
        {
            player = thirteenKillerGame.CreatePlayer(ctx.getUser());
            thirteenKillerGame.AddPlayerOnLobby(player);
        }
        player.idAvatar = idAvatar;

        ThirteenKillerProto.TKGameInfo.Builder builder = ThirteenKillerProto.TKGameInfo.newBuilder();
        thirteenKillerGame.GetRoom().values().forEach(tkRoom -> builder.addRooms(tkRoom.parseProtoBuilder()));

        builder.addAllIDOfDeletedRooms(thirteenKillerGame.idOfDeletedRooms);

        builder.setMoney(ctx.getUser().getMoneyGold());

        builder.setAvatar(idAvatar);

        ByteString gameInfo = builder.build().toByteString();
        thirteenKillerGame.SendMessage(ctx, gameInfo, TKActionConst.GAME_INFO);
    }

    private void UpdateSceneLobby()
    {
        ThirteenKillerProto.TKGameInfo.Builder builder = ThirteenKillerProto.TKGameInfo.newBuilder();
        thirteenKillerGame.GetRoom().values().forEach(tkRoom -> builder.addRooms(tkRoom.parseProtoBuilder()));

        builder.addAllIDOfDeletedRooms(thirteenKillerGame.idOfDeletedRooms);

        ByteString gameInfo = builder.build().toByteString();
        thirteenKillerGame.SendToAllLobby(gameInfo, TKActionConst.LOBBY_INFO);
    }

    private void HandleTryJoinRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThirteenKillerProto.TKRoomInfo tkRoomInfo = ThirteenKillerProto.TKRoomInfo.parseFrom(message.getData());
        TKRoom room;

        if (tkRoomInfo.getRoomId() == 0)// ph??ng m???i t???o
            room = thirteenKillerGame.getRoomByID(roomID);
        else
            room = thirteenKillerGame.getRoomByID(tkRoomInfo.getRoomId());

        ThirteenKillerProto.TKRoomInfo.Builder builder = room.parseProtoBuilder();

        long gold = ctx.getUser().getMoneyGold();

        // gold c???a user ph???i l???n h??n ho???c b???ng ti???n c?????c c???a b??n
        if (gold < (room.betLevel * 5))
        {
            builder.setErrorCode(TKActionConst.NOT_ENOUGH_GOLD);
            ByteString roomInfo = builder.build().toByteString();
            thirteenKillerGame.SendMessage(ctx, roomInfo, TKActionConst.TRY_JOIN_ROOM);
            return;
        }

        if (room.maxPlayer > room.playersJoinedRoom)
        {
            room.playersJoinedRoom++;

            thirteenKillerGame.AddPlayer(ctx.getUser());
            thirteenKillerGame.JoinRoom(ctx.getUser(), room);

            builder.setErrorCode(TKActionConst.SUCCESS);

            ByteString roomInfo = builder.build().toByteString();
            thirteenKillerGame.SendMessage(ctx, roomInfo, TKActionConst.TRY_JOIN_ROOM);
        }
        else if (room.maxPlayer == room.playersJoinedRoom)
        {
            builder.setErrorCode(TKActionConst.FULL_ROOM);
            ByteString roomInfo = builder.build().toByteString();
            thirteenKillerGame.SendMessage(ctx, roomInfo, TKActionConst.TRY_JOIN_ROOM);
        }

    }

    private void HandleJoinRoom(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        ThirteenKillerProto.TKRoomInfo data = ThirteenKillerProto.TKRoomInfo.parseFrom(message.getData());
        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());
        SendUsersJoinRoomToClient(room, data.getRoomId(), data.getBetLevel());
        thirteenKillerGame.RemovePlayerOnLobby(ctx.getUser());
        UpdateSceneLobby();
    }

    private void SendUsersJoinRoomToClient(TKRoom room, int roomId, long betLevel)
    {
        ThirteenKillerProto.TKRoomInfo.Builder roomInfo = ThirteenKillerProto.TKRoomInfo.newBuilder();
        roomInfo.setRoomId(roomId);
        roomInfo.setBetLevel(betLevel);

        LinkedHashMap<Integer, TKPlayer> listUser = room.GetUsersInCurrentRoom();
        TKPlayer[] users = listUser.values().toArray(new TKPlayer[0]);

        boolean hadMasterClient = false;

        for (int i = 0; i < 4; i++)
        {
            if (users.length > i && users[i] != null)
            {
                long money = users[i].getUser().getMoneyGold();
                String name = users[i].getUser().getUserName();
                int id = users[i].getId();
                int idAvatar = Integer.parseInt(users[i].getUser().getAvatar());

                ThirteenKillerProto.TKUserInfo.Builder userInfo = ThirteenKillerProto.TKUserInfo.newBuilder();
                userInfo.setMoney(money);
                userInfo.setNickName(name);
                userInfo.setPosUser(i + 1);
                userInfo.setUserId(id);
                userInfo.setIdAvatar(idAvatar);

                if (!hadMasterClient) {
                    TKPlayer player = room.GetPlayer(users[i].getId());
                    if (player.isMasterClient)
                        userInfo.setIsMasterClient(true);
                    hadMasterClient = true;
                }

                roomInfo.addUsers(i, userInfo);
            }
            else if (users.length < 4)
            {
                ThirteenKillerProto.TKUserInfo.Builder userInfo = ThirteenKillerProto.TKUserInfo.newBuilder();
                roomInfo.addUsers(i, userInfo);
            }
        }

        room.SendToAllInRoom(roomInfo.build().toByteString(), TKActionConst.UPDATE_USERS_JOIN_ROOM);
    }

    private void HandleLeaveGame(ChannelContextInfo ctx, XProtos.XMessage message) throws IOException
    {
        APISendMoney.postWithJson(APISendMoney.urlAPI ,APISendMoney.JsonObjectToString(
                ctx.getUser().getUserName(),
                Long.toString(ctx.getUser().getMoneyGold()),0,""));
        ThirteenKillerProto.TKRoomInfo tkRoomInfo = ThirteenKillerProto.TKRoomInfo.parseFrom(message.getData());
        TKRoom room = thirteenKillerGame.getRoomByID(tkRoomInfo.getRoomId());

        ThirteenKillerProto.TKRoomInfo.Builder builder = ThirteenKillerProto.TKRoomInfo.newBuilder();
        try {
            thirteenKillerGame.LeaveRoom(ctx.getUser());
            thirteenKillerGame.RemovePlayer(ctx.getUser());
            thirteenKillerGame.RemoveRoom(tkRoomInfo.getRoomId());

            room.playersJoinedRoom--;
            builder.setErrorCode(0);
        } catch (NotFoundPlayerInGame notFoundPlayerInGame) {
            builder.setErrorCode(GameErrorCode.PLAYER_NOT_FOUND_IN_GAME);
        } catch (Exception e) {
            builder.setErrorCode(TXErrorCode.ERROR_NOT_DEFINE);
        }


        ByteString errorCode = builder.build().toByteString();
        thirteenKillerGame.SendMessage(ctx, errorCode, TKActionConst.LEAVE_GAME);

        SendUserLeaveRoomToClient(room);
        UpdateSceneLobby();
    }
    private void SendUserLeaveRoomToClient(TKRoom room) {
        ThirteenKillerProto.TKRoomInfo.Builder roomInfo = ThirteenKillerProto.TKRoomInfo.newBuilder();
        LinkedHashMap<Integer, TKPlayer> listUser = room.GetUsersInCurrentRoom();
        TKPlayer[] users = listUser.values().toArray(new TKPlayer[0]);

        boolean hadMasterClient = false;

        for (int i = 0; i < 4; i++)
        {
            if (users.length > i)
            {
                if (users[i].getUser() != null) {
                    long money = users[i].getUser().getMoneyGold();
                    String name = users[i].getUser().getNickName();
                    int id = users[i].getId();

                    ThirteenKillerProto.TKUserInfo.Builder userInfo = ThirteenKillerProto.TKUserInfo.newBuilder();
                    userInfo.setMoney(money);
                    userInfo.setNickName(name);
                    userInfo.setPosUser(i + 1);
                    userInfo.setUserId(id);

                    if (!hadMasterClient) {
                        TKPlayer player = room.GetPlayer(users[i].getId());
                        if (player.isMasterClient)
                            userInfo.setIsMasterClient(true);
                        hadMasterClient = true;
                    }

                    roomInfo.addUsers(i, userInfo);
                } else {
                    ThirteenKillerProto.TKUserInfo.Builder userInfo = ThirteenKillerProto.TKUserInfo.newBuilder();
                    roomInfo.addUsers(i, userInfo);
                }
            }
            else
            {
                ThirteenKillerProto.TKUserInfo.Builder userInfo = ThirteenKillerProto.TKUserInfo.newBuilder();
                roomInfo.addUsers(i, userInfo);
            }
        }
        room.SendToAllInRoom(roomInfo.build().toByteString(), TKActionConst.UPDATE_USERS_LEAVE_ROOM);
    }

}
