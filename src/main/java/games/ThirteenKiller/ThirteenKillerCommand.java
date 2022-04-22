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
        // Lấy data từ client
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

        //gửi những lá bài cho những user khác xem
        room.SendToTheRest(userID, build.build().toByteString(), TKActionConst.FLIP_CARDS);

        // Reset lại tất cả các tham số để bắt đầu ván mới
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
            if (id == 150 || id == 151)         // id = 150 và 151 là heo đen nên phạt nữa tiền
            {
                totalFined += (room.betLevel / 2);
                cardListOfFines.add(id);
            }
            else if (id == 152 || id == 153)    // id = 152 và 153 là heo đỏ nên phạt đủ tiền
            {
                totalFined += room.betLevel;
                cardListOfFines.add(id);
            }
        }

        // Check 3 thông
        if (idCards.size() >= 6)
        {
            /** Công thức chỉ lấy 6 cây ra để xét lần lượt cho đến khi hết bài
              * Ví dụ có 7 lá bài(tức là có 7 phần tử bắt đầu từ 0 - 6) thì ta lấy 6 cây ra dc 2 lần (l1: 0-5, l2: 1-6)
              * Ví dụ có 9 lá (tức là có 9 phần tử từ 0 - 8) thì ta lấy 6 cây ra dc 4 lần (l1: 0-5, l2: 1-6, l3: 2-7, l4:3-8)
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

        // Cộng tiền cho người tới ba và trừ tiền người thua bét bị chết heo hoặc hàng
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

            // gửi số tiền mới xuống cho các client trong phòng
            room.SendNewMoneyToAllClient(players, TKActionConst.THEFINED);
        }

        return cardListOfFines;
    }

    private void HandleNewTurn(XProtos.XMessage message) throws IOException
    {
        ThirteenKillerProto.TKUserPass data = ThirteenKillerProto.TKUserPass.parseFrom(message.getData());

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());

        // user đã bỏ lượt
        TKPlayer player = room.GetPlayer(data.getUserId());
        player.isPass = data.getIsPass();
        if (player.isPass)
            room.countPass++;

        // user sẽ đi lượt tiếp theo
        TKPlayer playerOfNextTurn = room.GetPlayerOfNextTurn(data.getUserId());

        BloodBarsTaperedOff(playerOfNextTurn.getId(), room);


        LinkedHashMap<Integer, TKPlayer> users = room.GetUsersInCurrentRoom();
        int size = users.size();
        // Nếu có 4 user mà 3 user đã pass thì mở lượt đánh lại cho tất cả người chơi
        if (room.countPass + room.countLoser + room.countGoFirst >= size - 1)
        {
            // xử lý tiền phạt heo, 3 đôi thông hoặc tứ quý,... nếu có
            if (room.Cards.isFourOfaKind || room.Cards.isThreePairs || room.Cards.isFourPairs)
                HandleFined(room);

            // reset lại lượt cho tất cả người chơi
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

        // size - 1 để k tính số gold của 3 thông or tứ quý của player đã win,
        // vì trong quá trình cộng tiền phạt đã cộng luôn của player có 3 thông or tứ quý dc win
        for (int i = 0; i < room.theFined.size() - 1; i++)
        {
            totalFined += room.theFined.get(i);
        }

        TKPlayer[] players = room.playersIsHIT.toArray(new TKPlayer[0]);

        TKPlayer playerWin = players[players.length - 1]; // player này cuối cùng trong room.playersIsHIT là người ra bài sau cùng nên sẽ thắng số gold phạt
        TKPlayer playerLose = players[players.length - 2];// player này kế cuối trong room.playersIsHIT là người ra hàng or heo lớn nhất nên chịu hết


        room.AddMoney(totalFined, playerWin.getUser(), MoneyType.GOLD); // Cộng tiền cho thằng chặt
        room.SubMoney(totalFined, playerLose.getUser(), MoneyType.GOLD); // Trừ tiền thằng bị chặt

        playerWin.winMoney = totalFined;
        playerLose.winMoney = -totalFined;

        List<TKPlayer> users = new ArrayList<>();
        users.add(playerWin);
        users.add(playerLose);
        TKPlayer[] arUser = users.toArray(new TKPlayer[0]);
        // gửi số tiền mới xuống cho các client trong phòng
        room.SendNewMoneyToAllClient(arUser, TKActionConst.THEFINED);

        // Reset lại số tiền phạt và player bị phạt
        room.theFined.clear();
        room.playersIsHIT.clear();
    }
    private void BloodBarsTaperedOff(int userId, TKRoom room) {
        TKPlayer newPlayer = new TKPlayer();

        newPlayer.timeToThrowCard = 0;

        // Huỷ timer cũ trước khi thực hiện timer mới
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

        // Lấy số người đã tới
        int countGoFirst = room.GetCountGoFirst();

        LinkedHashMap<Integer, TKPlayer> users = room.GetUsersInCurrentRoom();
        int size = users.size();

        // Đã đánh xong ván bài
        if (countGoFirst == size - 1)
        {
            FinishGame(room, roomId);
        }
    }

    public void FinishGame(TKRoom room, int roomId) throws IOException
    {
        // Tắt time được ném bài
        room.timer.cancel();

        // Xử lý thắng thua và chung tiền
        room.HandleWinOrLose();

        // yêu cầu master client gửi y/c điếm ngược để cbi bắt đầu ván mới
        ReCountDown(room.masterClient.getId(), roomId, room);
    }

    private void ReCountDown(int userId, int roomID, TKRoom room)
    {
        ThirteenKillerProto.TKCountdown.Builder countdown = ThirteenKillerProto.TKCountdown.newBuilder();

        countdown.setRoomId(roomID);

        countdown.setUserId(userId);

        // Chuẩn bị bắt đầu ván mới nên yêu cầu master client y/c điếm ngược
        room.SendToAllInRoom(countdown.build().toByteString(), TKActionConst.COUNT_DOWN_AGAIN);
    }

    private void ResetAllParameter(TKRoom room)
    {
        room.ResetAllParameter();
    }

    private void HandleThrowCards(ChannelContextInfo ctx, XProtos.XMessage message) throws InvalidProtocolBufferException
    {
        // Lấy data từ client
        ThirteenKillerProto.TKCards data = ThirteenKillerProto.TKCards.parseFrom(message.getData());
        List<ThirteenKillerProto.TKCardInfo> idCards = data.getListCardList();

        // Lấy danh sách id Card từ client gửi lên
        List<Integer> newCards = new ArrayList<>();
        for (ThirteenKillerProto.TKCardInfo idCard : idCards)
            newCards.add(idCard.getCardId());

        TKRoom room = thirteenKillerGame.getRoomByID(data.getRoomId());

        ThirteenKillerProto.TKCards.Builder build = ThirteenKillerProto.TKCards.newBuilder();
        int userId = data.getUserId();
        build.setUserId(userId);

        // Check các lá bài có hợp lệ không
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

        //gửi những lá bài mà user hiện tại đánh ra cho những user khác xem
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
        // Kiểm tra phải là sảnh liền k
        int count = 0;
        for (int i = newCards.size() - 1; i > 0; i--) {
            int idCard1 = newCards.get(i) / 10;
            int idCard2 = newCards.get(i - 1) / 10;

            int result = idCard1 - idCard2;

            if (result == 1)
                count++;
        }
        // vd: sảnh là 5-6-7-8-9-10 thì 10-9=1(count=1); 9-8=1(count=2); 8-7=1(count=3); 7-6=1(count=4); 6-5=1(count=5)
        // count = 5 và newCards.size() - 1 = 5
        // Thì là sảnh liền và trả về true
        if (count == (newCards.size() - 1))
            return true;

        else {
            boolean check = CheckThreePairOrFourPair(newCards);

            if (!check) return false;

            // Xuống đây là nó đã thoã 3 đôi thông or 4 đôi thông r... nên chỉ cần biết nó bao nhiêu lá nữa là biết
            if (newCards.size() == 6)
                room.Cards.isThreePairs = true;

            else
                room.Cards.isFourPairs = true;

        }
        return true;
    }

    private boolean FiveCards(List<Integer> newCards) {
        // Nếu là 5 cây sảnh liền thì cây 5-4= 1 và 4-3=1.... 2-1=1
        // Nếu có cây nào trừ nhau ra khác 1 là lủng
        // vd: sảnh 5-6-7-8-9- thì 9-8=1; 8-7=1; 7-6=1; 6-5=1
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

        // Nếu là tứ quý
        if (idCard1 == idCard2 && idCard2 == idCard3 && idCard3 == idCard4)
        {
            room.Cards.isFourOfaKind = true;
            return true;
        }

        // 4 cây sảnh liền
        else return idCard4 - idCard3 == 1 && idCard3 - idCard2 == 1 && idCard2 - idCard1 == 1;

    }

    private boolean ThreeCards(List<Integer> newCards) {
        int idCard1 = newCards.get(0) / 10;
        int idCard2 = newCards.get(1) / 10;
        int idCard3 = newCards.get(2) / 10;

        // 3 cây cùng quân bài vd: 333
        if (idCard1 == idCard2 && idCard2 == idCard3)
            return true;

            // 3 cây sảnh liền
        else return idCard3 - idCard2 == 1 && idCard2 - idCard1 == 1;

    }

    private boolean TwoCards(List<Integer> newCards) {
        int idCard1 = newCards.get(0) / 10;
        int idCard2 = newCards.get(1) / 10;

        // Nếu là đôi mà 2 cây là 2 quân bài khác nhau sẽ sai
        return idCard1 == idCard2;
    }

    private int CheckErrorCode(List<Integer> newCards, TKRoom room, int userId)
    {
        int sizeNewCard = newCards.size();
        int sizeOldCard = room.listOfPreCards.size();

        // Nếu listOfPreCards chưa có lá nào nghĩa là ván mới thì thêm vào
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

        // Ngược lại số lượng bằng nhau... Good, giờ xét đến quân bài
        else
        {
            int indexLast = room.listOfPreCards.size() - 1;
            for (int j = indexLast; j >= 0; j--)
            {
                /* card sẽ có dạng là Id = 30-31-32-33, 40-41-42-43, 50-53, 60-63,... đến 150-153
                // đầu số thể hiện quân bài, đuôi số thể hiện nước bài
                // chia hết cho 10 để lấy số đầu số
                 */
                int idCard_Prefix = newCards.get(j) / 10; // vd: 30/10 = 3 quân bài là con 3(dư 0 là bích)
                int idPreCard_Prefix = room.listOfPreCards.get(j) / 10; // vd: 33/10 = 3 quân bài là con 3(dư 3 là rô)

                // Nếu quân bài mới nhỏ hơn quân bài cũ thì xét đánh lủng lun...khỏi xét tiếp
                if (idCard_Prefix < idPreCard_Prefix)
                {
                    room.countLoser++;
                    return TKActionConst.PENALTY;
                }

                /* Nếu bằng nhau và là cây bài cuối
                // Thì nước mới phải lớn hơn nước cũ, còn những cây bài sau nhỏ hơn cũng k sao
                // Nếu lớn hơn thì quá tốt k cần xét...kkk
                 */
                if (idCard_Prefix == idPreCard_Prefix && j == indexLast) {
                    int idCard_Suffixes = newCards.get(j) % 10;
                    int idPreCard_Suffixes = room.listOfPreCards.get(j) % 10;

                    // Nếu nước mới nhỏ hơn nước cũ thì lủng cmnr
                    if (idCard_Suffixes < idPreCard_Suffixes)
                    {
                        room.countLoser++;
                        return TKActionConst.PENALTY;
                    }
                }
            }
        }

        // Nếu hợp lệ thì thêm những lá bài mới vào
        room.listOfPreCards.removeAll(room.listOfPreCards);
        room.listOfPreCards.addAll(newCards);

        if (room.Cards.isFourOfaKind ||
            room.Cards.isThreePairs ||
            room.Cards.isFourPairs ||
            room.listOfPreCards.get(0) >= 150) // id card 150-153 là heo
            RecordTheFined(room.listOfPreCards, userId, room);

        return TKActionConst.SUCCESS;
    }

    private boolean CheckCard(TKRoom room, List<Integer> newCards)
    {
        if (room.Cards.isThreePairs)
        {
            // Chặt heo
            if (room.listOfPreCards.size() == 1)
            {
                // Là heo thì ok
                // 150 - 153 là heo
                // Không phải thì chặt lủng
                return room.listOfPreCards.get(0) >= 150;
            }

            // Chặt 3 thông nhỏ hơn
            else if (room.listOfPreCards.size() == 6)
            {
                boolean isThreePair = CheckThreePairOrFourPair(room.listOfPreCards);
                // Nếu là 3 đôi thông thì check xem bài mới có lớn hơn bài cũ k
                // Nếu nhỏ hơn thì return false để báo lủng
                if (isThreePair)
                    return CheckLastTwoCards(newCards, room.listOfPreCards);

                return false;
            }
        }

        else if (room.Cards.isFourOfaKind)
        {
            // Chặt heo
            if (room.listOfPreCards.size() == 1)
            {
                // 150 - 153 là heo
                return room.listOfPreCards.get(0) >= 150;
            }

            // Chặt tứ quý nhỏ hơn
            else if (room.listOfPreCards.size() == 4)
            {
                boolean isFourOfaKind = CheckFourOfaKind(room.listOfPreCards);
                // So sánh 2 tứ quý coi bài mới có nhỏ hơn bài cũ k, nếu nhỏ hơn báo false để biết là đánh lủng
                if (isFourOfaKind)
                    return CheckLastTwoCards(newCards, room.listOfPreCards);

                return false;
            }

            // Chặt 3 thông
            else if (room.listOfPreCards.size() == 6)
            {
                // Nếu bài cũ là 3 thông thì return true khỏi xét típ vì tứ quý lớn hơn 3 thông
                return CheckThreePairOrFourPair(room.listOfPreCards);
            }
        }


        else if (room.Cards.isFourPairs)
        {
            // Chặt heo
            if (room.listOfPreCards.size() == 1)
                return room.listOfPreCards.get(0) >= 150;

            // Chặt tứ quý
            else if (room.listOfPreCards.size() == 4)
                return CheckFourOfaKind(room.listOfPreCards);

            // Chặt 3 thông
            else if (room.listOfPreCards.size() == 6)
                return CheckThreePairOrFourPair(room.listOfPreCards);

            // Chặt 4 thông nhỏ hơn
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

        /* card sẽ có dạng là Id = 30-31-32-33, 40-41-42-43, 50-53, 60-63,... đến 150-153
            // đầu số thể hiện quân bài, đuôi số thể hiện nước bài
            // chia hết cho 10 để lấy số đầu số
        */
        int idCard_Prefix = newCards.get(indexLast) / 10; // vd: 30/10 = 3 quân bài là con 3(dư 0 là bích)
        int idPreCard_Prefix = oldCards.get(indexLast) / 10; // vd: 33/10 = 3 quân bài là con 3(dư 3 là rô)

        // Nếu quân bài mới nhỏ hơn quân bài cũ thì xét đánh lủng lun...khỏi xét tiếp
        if (idCard_Prefix < idPreCard_Prefix)
             return false;

        /* Nếu bằng nhau và là cây bài cuối
            // Thì nước mới phải lớn hơn nước cũ, còn những cây bài sau nhỏ hơn cũng k sao
            // Nếu lớn hơn thì quá tốt k cần xét...kkk
        */
        if (idCard_Prefix == idPreCard_Prefix)
        {
             int idCard_Suffixes = newCards.get(indexLast) % 10;
             int idPreCard_Suffixes = oldCards.get(indexLast) % 10;

             // Nếu nước mới nhỏ hơn nước cũ thì lủng cmnr
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

            // 150 - 153 là id card của heo
            if (idCard == 150 || idCard == 151)
            {
                room.theFined.add(room.betLevel / 2); // Heo đen nữa tiền
                room.playersIsHIT.add(player);
            }

            else if (idCard == 152 || idCard == 153)
            {
                room.theFined.add(room.betLevel) ;// Heo đỏ đủ tiền
                room.playersIsHIT.add(player);
            }
        }

        // Đôi heo
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
                    room.theFined.add(room.betLevel) ;// 2 đen là 1 cược
                    room.playersIsHIT.add(player);
                    break;
                case 3:
                    room.theFined.add(room.betLevel + room.betLevel / 2) ; // 1 đỏ 1 đen là 1.5 cược
                    room.playersIsHIT.add(player);
                    break;
                case 4:
                    room.theFined.add(room.betLevel * 2) ; // 2 đỏ là 2 cược
                    room.playersIsHIT.add(player);
                    break;
            }

        }

        // Tứ quý
        else if (sizeNewCard == 4)
        {
            boolean isFourOfaKind = CheckFourOfaKind(newCards);

            if (isFourOfaKind)
            {
                room.theFined.add(room.betLevel * 2) ; // tứ quý là 2 cược
                room.playersIsHIT.add(player);
            }
        }

        // 3 thông
        else if (sizeNewCard == 6)
        {
            boolean isThreePair = CheckThreePairOrFourPair(newCards);

            if (isThreePair)
            {
                room.theFined.add(room.betLevel  + (room.betLevel / 2)) ; // 3 thông là 1.5 cược
                room.playersIsHIT.add(player);
            }
        }

        // 4 thông
        else if (sizeNewCard == 8)
        {
            boolean isFourPair = CheckThreePairOrFourPair(newCards);

            if (isFourPair)
            {
                room.theFined.add(room.betLevel * 4) ; // 4 thông là 4 cược
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

            // Nếu 2 quân bài khác nhau thì k phải đôi trả về false
            if (idCard1 != idCard2)
                return false;

            // Nếu là đôi kiểm tra phải vừa đôi vừa sảnh k
            // Nếu card2 - card1 khác 1 thì không phải sảnh trả về false
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

        TKPlayer player = thirteenKillerGame.GetPlayerLobbỵ̣(ctx.getUserId());
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

        if (tkRoomInfo.getRoomId() == 0)// phòng mới tạo
            room = thirteenKillerGame.getRoomByID(roomID);
        else
            room = thirteenKillerGame.getRoomByID(tkRoomInfo.getRoomId());

        ThirteenKillerProto.TKRoomInfo.Builder builder = room.parseProtoBuilder();

        long gold = ctx.getUser().getMoneyGold();

        // gold của user phải lớn hơn hoặc bằng tiền cược của bàn
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
