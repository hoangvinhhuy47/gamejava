package games.slot.data;

import com.google.protobuf.Message;
import games.core.common.money.Money;
import games.core.common.money.MoneyType;
import games.core.proto.ProtoSerializer;
import games.slot.SlotGameConfig;
import games.slot.SlotPlayer;
import games.slot.exception.ItemBonusIsOpened;
import games.slot.exception.OpenBonusItemLimit;
import games.slot.exception.WrongSlotLineWinType;
import libs.util.CRandom;
import libs.util.PrettyPrinter2DArray;
import libs.util.data.HashByInt;
import message.SlotGameProto;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ResultBoard<L extends SlotLine, R extends SlotReward> implements ProtoSerializer<SlotGameProto.SpinSlotResponse> {

    protected static SlotConfig slotConfig = SlotConfig.getInstance();
    /**
     * Mảng 2 chiều chứa kết qua quay (item id)
     */
    private int[][] board;

    /**
     * Dùng để log cái board 1 cách trực quan
     */
    private static final PrettyPrinter2DArray printer = new PrettyPrinter2DArray(System.out);

    /**
     * Player truyền vào khi quay, để biết result board này của player nào
     * Để biết được nó đang đặt những line nào
     */
    public SlotPlayer player;

    /**
     * Số tiền mà player thắng được
     */
    protected Money money;

    /**
     * Số tiền ăn được từ jackpot
     */
    protected Money jackpotMoney;

    /**
     * Số tiền ăn được trừ jackpot
     */
    protected Money normalWinMoney;


    /**
     * Mảng result để trả về cho client từ List này client có thể parse thành mảng 2 chiều
     */
    protected ArrayList<SlotGameProto.ResultItemIndex> resultItems;

    /**
     * Config của game slot đó (Là main hay mini)
     * Loại line, loại reward nào
     */
    private SlotGameConfig<L, R> config;

    private SpinResult spinResult;

    private int totalFreeSpinItem = 0;

    private WinResult freeSpinResult = new WinResult();

    private int totalBonusItem = 0;

    private BonusWinResult bonusWinResult = new BonusWinResult();

    private ListBonusWinResult listBonusWinResult = new ListBonusWinResult();

    ResultBoard(SlotPlayer player, SlotGameConfig<L, R> slotGameConfig) {
        this.player = player;

        // Release jackpot vì logic control quay lại nhiều lần cho đến khi thoả điều kiện
        // Nếu không release jackpot thì có thể nó sẽ bị dính jackpot ở lượt quay đã bị bỏ qua
        // do không đủ điều kiện
        this.player.resetJackpotAmount();

        this.config = slotGameConfig;
        board = createRawBoard();
        spinResult = new SpinResult();

//        // Nếu được lượt free.
//        if (getFreeSpinResult().isWin()){
//            player.plusFreeSpin(getFreeSpinResult().getFreeSpinAmount());
//        }
    }

    ResultBoard(SlotPlayer player, SlotGameConfig<L, R> config, int[][] rawBoard){
        this.player = player;
        this.player.resetJackpotAmount();
        this.config = config;
        board = rawBoard;
        processRawBoard(rawBoard);
    }

    public HashByInt<L> getLines() {
        return config.getLines();
    }

    public HashByInt<SlotReel> getReels() {
        return config.getReels();
    }

    public HashByInt<R> getRewards() {
        return config.getRewards();
    }

    protected abstract int numRow();
    protected abstract int numCol();
    protected abstract int itemWill();
    protected abstract int itemBonus();
    protected abstract int itemFreeSpin();
    protected abstract int itemJackpot();

    /**
     * Lấy số lần nhân tiền của item ứng với số lần kề nhau
     * Ví dụ: itemId = 12 (Táo) với 5 lần kề nhau sẽ được x500
     * @param itemId mã item
     * @param adjacentCount số lần kề nhau
     * @return số lần được nhân
     */
    private int getRewardMultipleOfItem(int itemId, int adjacentCount){
        if (getRewards().get(itemId) == null){
            return 0;
        }
        Integer multiple =  getRewards().get(itemId).getMultiple(adjacentCount);
        if (multiple == null){
            return 0;
        }
        return multiple;
    }

    /**
     *
     * @param itemId mã item từ config
     * @param adjacentCount số lần item kề nhau
     * @return số tiền user nhận được khi ăn được item nào đó
     */
    private long getMoneyWinOneItem(int itemId, int adjacentCount){
        return player.getGoldStake().getAmount() * getRewardMultipleOfItem(itemId, adjacentCount);
    }

    /**
     * Lấy ra số tiền user ăn được trong lượt quay này
     * @return số tiền ăn được
     */
    public long getMoneyWin(){
        if (money == null){
            long totalMoney = getSpinResult().getTotalMoney();
            money = new Money(totalMoney, MoneyType.GOLD);
        }
        return money.getAmount();
    }

    public long getMoneyCanWin(){
        if (bonusWinResult.isWin() && !bonusWinResult.isWin5Item()){
            return getMoneyWin() + bonusWinResult.getMaxMoneyCanWin();
        }
        return getMoneyWin();
    }

    public long getJackpotMoneyWin(){
        if (jackpotMoney == null){
            jackpotMoney = new Money(getSpinResult().getJackpotMoney(), MoneyType.GOLD);
        }
        return jackpotMoney.getAmount();
    }

    public long getNormalMoneyWin(){
        if (normalWinMoney == null){
            normalWinMoney = new Money(getSpinResult().getNormalMoney(), MoneyType.GOLD);
        }
        return normalWinMoney.getAmount();
    }

    private SlotGameProto.ResultItemIndex createProtoResultItemIndex(int col, int row, int itemId){
        SlotGameProto.ResultItemIndex.Builder builder = SlotGameProto.ResultItemIndex.newBuilder();
        builder.setItemId(itemId);
        builder.setCol(col);
        builder.setRow(row);
        return builder.build();
    }

    /**
     * Tạo ra bảng kết quả
     * @return mảng 2 chiều chứa id các item
     */
    protected int[][] createRawBoard() {
        totalFreeSpinItem = 0;
        totalBonusItem = 0;
        bonusWinResult = new BonusWinResult();
        listBonusWinResult = new ListBonusWinResult();
        freeSpinResult = new WinResult();
        resultItems = new ArrayList<>();
        CRandom random = new CRandom();
        int[][] rawBoard = new int[numRow()][numCol()];
        int column = 0;
//        int totalBonusItem = 0;
//        int totalFreeSpinItem = 0;
        int requireBonusItemNumber = 0;
        if (player.getNumLine() >= 10){
            int randIntBonus = random.randInt(0, 10000);
            if (randIntBonus < slotConfig.getBonusConfig().getRate5Bonus()){
                requireBonusItemNumber = 5;
            } else if (randIntBonus < slotConfig.getBonusConfig().getRate4Bonus()){
                requireBonusItemNumber = 4;
            } else if (randIntBonus < slotConfig.getBonusConfig().getRate3Bonus()){
                requireBonusItemNumber = 3;
            }
        }

        for (int reelId: getReels().keySet()) {
            SlotReel reel = getReels().get(reelId);
            ArrayList<Integer> items = reel.getItems();
            int position = random.randInt(1, items.size()-2);
            boolean isHaveBonus = false; // Cờ xem col này có item bonus hay chưa
            int colMustHaveBonus = random.randInt(0,2);
            for (int i=0; i < 3; i++){
                int _item = items.get(position+i-1);
                while (
                        (isHaveBonus && _item == itemBonus()) //Đảm bảo rằng 1 colum không có 2 item bonus
                        || (this.player.getNumLine() < 10 // Đảm bảo dưới 10 line thì sẽ không ra quá 2 item freeSpin và bonus
                            && (
                                    (totalBonusItem > 1 && _item == itemBonus())
                                    || (totalFreeSpinItem > 1 && _item == itemFreeSpin())
                               )
                           )
                        || ( // Đảm bảo không có quá 5 itemBonus hoặc 5 itemFreeSpin
                                (totalBonusItem > 4 && _item == itemBonus())
                                || (totalFreeSpinItem > 4 && _item == itemFreeSpin())
                           )
                ){
                    position = random.randInt(1, items.size()-2);
                    _item = items.get(position+i-1);
                }
                if (_item == itemBonus()){
                    isHaveBonus = true;
                }
                // End control

                if (requireBonusItemNumber > totalBonusItem && !isHaveBonus && i == colMustHaveBonus){ // Đảm bảo rằng số lượng item bonus phải đủ nếu trúng tỉ lệ ra bonus
                       _item = itemBonus();
                       isHaveBonus = true;
                }
                while ( // đảm bảo rằng sẽ không có nhiều hơn số item bonus cần thiết
                        (
                           (requireBonusItemNumber != 0 && totalBonusItem > requireBonusItemNumber-1)
                           || (requireBonusItemNumber == 0 && totalBonusItem > 1)
                        )
                        && _item == itemBonus()
                ){
                    while (_item == itemBonus() || _item == itemFreeSpin()){
                        position = random.randInt(1, items.size()-2);
                        _item = items.get(position+i-1);
                    }
                }
                rawBoard[i][column] = _item;
                processRawItem(_item, i, column);
            }
            column++;
        }
        if (bonusWinResult.isWin() && !bonusWinResult.isWin5Item()){
            listBonusWinResult.add(bonusWinResult);
        }
//        printer.print(rawBoard);
        return rawBoard;
    }

    protected void processRawBoard(int[][] rawBoard){
        resultItems = new ArrayList<>();
        freeSpinResult = new WinResult();
        for (int row = 0; row < rawBoard.length; row++) {
            for (int col = 0; col < rawBoard[row].length; col++) {
                int item = rawBoard[row][col];
                processRawItem(item, row, col);
            }
        }
        spinResult = new SpinResult();
        if (bonusWinResult.isWin()){
            listBonusWinResult.add(bonusWinResult);
        }
    }

    private void processRawItem(int item, int row, int col){
        if (item == itemFreeSpin()){
            freeSpinResult.setMoneyAmount(freeSpinResult.moneyAmount+1);
            freeSpinResult.add(new BoardItem(row, col, item));
            totalFreeSpinItem++;
        } else if (item == itemBonus()){
            bonusWinResult.add(new BoardItem(row, col, item));
            totalBonusItem++;
        }
        resultItems.add(createProtoResultItemIndex(col, row, item));
    }

    public void runRecheck(){
        spinResult = new SpinResult();
        freeSpinResult = new WinResult();
        resultItems.clear();
        for (int row = 0; row < this.board.length; row++) {
            for (int col = 0; col < this.board[row].length; col++) {
                int _item = this.board[row][col];
                processRawItem(_item, row, col);
            }
        }
        if (bonusWinResult.isWin()){
            listBonusWinResult.add(bonusWinResult);
        }
    }


    /**
     * Lấy ra số vàng user ăn được
     * @return Gold số vàng
     */
    public Money getMoney() {
        if (money == null){
            money = new Money(getMoneyWin(), MoneyType.GOLD);
        }
        return money;
    }

    public Money getJackpotMoney(){
        this.getJackpotMoneyWin();
        return jackpotMoney;
    }

    public Money getNormalWinMoney(){
        this.getNormalMoneyWin();
        return normalWinMoney;
    }

    public WinResult getFreeSpinResult() {
        return freeSpinResult;
    }

    class BoardItem implements ProtoSerializer<SlotGameProto.ResultItemIndex>{
        int row;
        int col;
        int item;

        BoardItem(){}

        BoardItem(int row, int col, int item){
            this.row = row;
            this.col = col;
            this.item = item;
        }

        boolean isBonus(){
            return item == itemBonus();
        }

        boolean isWill(){
            return item == itemWill();
        }

        boolean isNormal(){
            return !isBonus() && !isWill() && !isFreeSpin();
        }

        boolean isFreeSpin(){
            return item == itemFreeSpin();
        }

        boolean isJackpot(){
            return item == itemJackpot();
        }

        boolean isFirstItem(){
            return this.col == 0;
        }

        /**
         * Kiểm tra xem có phải cùng item hay không
         */
        boolean isSame(BoardItem otherItem){
            return item == otherItem.item;
        }

        /**
         * kiểm tra xem có cùng item - line - col hay không
         */
        boolean isEquals(BoardItem otherItem){
            return item == otherItem.item && col == otherItem.col && row == otherItem.row;
        }

        @Override
        public SlotGameProto.ResultItemIndex.Builder parseProtoBuilder() {
            SlotGameProto.ResultItemIndex.Builder builder = SlotGameProto.ResultItemIndex.newBuilder();
            builder.setCol(this.col);
            builder.setItemId(this.item);
            builder.setRow(this.row);
            return builder;
        }
    }

    class HashBoardItem extends HashByInt<BoardItem>{
        HashBoardItem(MainSlotLine line){
            super(line.getLength());
            this.line = line;
            normalWinResult = new WinResult(this.line.getId());
//            bonusWinResult = new BonusWinResult(this.line.getId());
            int lineLength = line.getLength();
            int[] rowOfCol = line.getRowOfCol();
            for (int i=0; i < lineLength; i++){
                BoardItem boardItem = new BoardItem();
                boardItem.col = i;
                boardItem.row = rowOfCol[i];
                boardItem.item = board[boardItem.row][boardItem.col];
                this.put(boardItem);
            }

        }

        private MainSlotLine line;
        private BoardItem flagItem = null;
        private BoardItem interItem = null;
        private BoardItem beforeItem = null;
        private int adjacentCount = 1;
        private WinResult normalWinResult;
//        private BonusWinResult bonusWinResult;
        private int willItemCount;
        private long moneyWinAmount = 0;

        boolean isFull(){
            return this.size() == numCol();
        }

        private BoardItem getFirstWillItem(){
            for (BoardItem boardItem : this.values()) {
                if (boardItem.isWill()){
                    return boardItem;
                }
            }
            return null;
        }

        @Override
        public BoardItem put(BoardItem boardItem) {
            if (this.isFull()){
                return this.get(numCol());
            }
            if (this.size() > 0){
                beforeItem = this.get(this.size()-1);
            }
            if (boardItem.isWill()){
                willItemCount++;
            }
            if (boardItem.isFirstItem()){
                flagItem = boardItem;
                interItem = boardItem;
                if (flagItem.isNormal() || flagItem.isWill()){
                    normalWinResult.add(flagItem);
                }
//                else if (flagItem.isBonus()){
//                    bonusWinResult.add(flagItem);
//                }
            } else {
                // gán flagItem là thằng tiếp sau thằng WILL
                if (flagItem.isWill() && beforeItem.isWill() && willItemCount < 3){
                    if (!boardItem.isBonus() && !boardItem.isFreeSpin()){
                        flagItem = boardItem;
                    }
                } else if (willItemCount > 2){
                    flagItem = getFirstWillItem();
                }
            }

            BoardItem preItem = super.put(boardItem.col, boardItem);
            if (this.size() == numCol()){
                this.calculate();
            }
            return preItem;
        }

        private boolean nextItem(){
            if (this.interItem == null){
                return false;
            }
            this.interItem = this.get(this.interItem.col+1);
            return this.interItem != null;
        }

        private void calculate(){
            if (flagItem.isFreeSpin() || flagItem.isBonus()){
                normalWinResult.clear();
                return;
            }
            boolean isBreak = false;
            while (nextItem()){
                if (!interItem.isFreeSpin() && !interItem.isBonus()){
                    if (interItem.isSame(flagItem) || interItem.isWill()){
                        adjacentCount++;
                        normalWinResult.add(interItem);
                    } else {
                        isBreak = true;
                    }
                } else {
                    isBreak = true;
                }
                if (isBreak){
                    if (getMultipleCount() <= 0){
                        this.normalWinResult.clear();
//                        this.bonusWinResult.clear();
                    }
                    break;
                }
            }
        }

        int getMultipleCount(){
            return getRewardMultipleOfItem(this.flagItem.item, adjacentCount);
        }

        long getWinMoney(){
            if (this.moneyWinAmount == 0){
                //            if (isWinBonus()){
//                System.out.println("Win line bonus " + this.getLine() + bonusWinResult.getTotalMoney());
//                return bonusWinResult.getTotalMoney();
//            } else
                this.moneyWinAmount = normalWinResult.getMoneyAmount();
            }

            return this.moneyWinAmount;
        }

        boolean isWin(){
            return isWinNormal() || isWinJackpot(); // || isWinBonus();
        }

        boolean isWinNormal(){
            return normalWinResult.isWin() && !normalWinResult.isWinJackpot();
        }

//        boolean isWinBonus(){
//            return bonusWinResult.isWin();
//        }

        boolean isWinJackpot(){
            return this.normalWinResult.isWin() && normalWinResult.isWinJackpot();
        }

        MainSlotLine getLine() {
            return line;
        }
    }

    public class WinResult extends ArrayList<BoardItem> implements ProtoSerializer<SlotGameProto.WinResultProto>{

        int willItemCount = 0;
        BoardItem flagItem = null;
        boolean isExecute = false;
        long moneyAmount = 0;
        int lineId;
        boolean isChangeFlagItemBecauseWill = false;
        WinResult(){
            super(ResultBoard.this.numCol());
        }

        WinResult(int lineId){
            super(ResultBoard.this.numCol());
            this.lineId = lineId;
        }

        void execute(){
            if (!isExecute){
                if (flagItem == null){
                    this.clear();
                } else {
                    if (flagItem.isJackpot() && size() == 5){
                        isExecute = true;
                        return;
                    }
                    if (flagItem.isFreeSpin() && size() < 3
                            || getRewardMultipleOfItem(flagItem.item, size()) <= 0){
                        this.clear();
                    }
                }
                isExecute = true;
            }
        }

        private long getMoneyAmount(){
            execute();
            if (getFreeSpinAmount() > 0){
                return getFreeSpinAmount();
            }
            if (flagItem != null && moneyAmount == 0){
                if (isWinJackpot()){
                    moneyAmount = player.getAndLockJackpotAmount();
                } else {
                    if (size() > 0 && !flagItem.isBonus() && !isWinJackpot()){
                        moneyAmount = getMoneyWinOneItem(flagItem.item, this.size());
                    }
                }
            }

            return moneyAmount;
        }

        public int getFreeSpinAmount(){
            if (flagItem != null && flagItem.isFreeSpin()){
                return getRewardMultipleOfItem(itemFreeSpin(), this.size());
            }
            return 0;
        }

        void setMoneyAmount(long moneyAmount) {
            this.moneyAmount = moneyAmount;
        }

        public boolean isWin(){
            execute();
            return this.size() > 0;
        }

        boolean isWinJackpot(){
            execute();
            if (this.flagItem == null){
                return false;
            }
            return this.flagItem.isJackpot() && this.size() == numCol(); // && isAllSame();
        }

        boolean isAllSame(){
            for (BoardItem boardItem : this) {
                if (!flagItem.isSame(boardItem)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean add(BoardItem boardItem) {
            if (this.size() == 5){
                return false;
            }
            if (boardItem.col == 0){
                flagItem = boardItem;
                if (flagItem.isWill()){
                    this.isChangeFlagItemBecauseWill = true;
                }
            }

            if (boardItem.isWill()){
                willItemCount++;
                // Nếu will item nhiều hơn 2 thì lấy thưởng của will item
                if (willItemCount > 2){
                    flagItem = boardItem;
                }
            } else {
                if (willItemCount < 3 && isChangeFlagItemBecauseWill){
                    flagItem = boardItem;
                    isChangeFlagItemBecauseWill = false;
                }
            }

            if (boardItem.isFreeSpin() || boardItem.isBonus()){
                flagItem = boardItem;
            }
            return super.add(boardItem);
        }

        /**
         * Thêm vào trừ các item có cùng row-col-item_id
         *
         */
        public void addAllUnion(Collection<? extends BoardItem> c) {
            for (BoardItem boardItem : c) {
                boolean isSame = false;
                for (BoardItem item : this) {
                    if (boardItem.isEquals(item)){
                        isSame = true;
                        break;
                    }
                }
                if (!isSame){
                    super.add(boardItem);
                }
            }
        }

        List<SlotGameProto.ResultItemIndex> getResultItemIndexProtoList(){
            return this.stream().map(ProtoSerializer::getProtoMessage).collect(Collectors.toList());
        }

        @Override
        public SlotGameProto.WinResultProto.Builder parseProtoBuilder() {
            SlotGameProto.WinResultProto.Builder builder = SlotGameProto.WinResultProto.newBuilder();
            builder.setMoney(getMoneyAmount());
            builder.addAllItemIndex(this.getResultItemIndexProtoList());
            builder.setLine(getLineId());
            return builder;
        }

        int getLineId() {
            return lineId;
        }

    }

    public class BonusWinResult extends ArrayList<Long> implements ProtoSerializer<SlotGameProto.BonusWinResultProto>{

        WinResult winResult;
        boolean isExecute = false;
        CRandom random = new CRandom();
        long totalMoney = 0;
        private SlotConfig.BonusConfig bonusConfig = slotConfig.getBonusConfig();
        int[][] rawResult = new int[3][5];
        int maxMoneyCanWin = 0;
        OpenedItems openedItems = new OpenedItems();
        int openedEmptyItem = 0;
        int baseMoney = 0; // Tiền gốc(x4 cho 3 bonus (không tính random x8, x12), x20 cho 4 bonus)
        int currentBaseMoney = 0; // Tiền gốc hiện tại (Đã được tăng sau khi mở ô trống)

        public class OpenedItems extends HashMap<String, BonusItem> implements ProtoSerializer<SlotGameProto.BonusResultAllResponse>{

            public BonusItem put(BonusItem value) {
                if (!this.canOpen()){ // Nếu mở quá 10 ô (không tính ô empty)
                    throw new OpenBonusItemLimit();
                }
                totalMoney += value.money;
                return super.put(getHashKey(value.row, value.col), value);
            }

            public long getTotalMoney(){
                return totalMoney;
            }

            @Override
            public SlotGameProto.BonusResultAllResponse.Builder parseProtoBuilder() {
                SlotGameProto.BonusResultAllResponse.Builder builder = SlotGameProto.BonusResultAllResponse.newBuilder();
                this.values().forEach(bonusItem -> builder.addBonusItems(bonusItem.parseProtoBuilder()));
                builder.setTotalMoney(this.getTotalMoney());
                return builder;
            }

            boolean canOpen(){
                return this.size() < 10 + openedEmptyItem;
            }

        }

        class BonusItem implements ProtoSerializer<SlotGameProto.BonusResultItemResponse>{
            int col;
            int row;
            int money;
            BonusItem(int row, int col, int money){
                this.col = col;
                this.row = row;
                this.money = money;
            }

            @Override
            public SlotGameProto.BonusResultItemResponse.Builder parseProtoBuilder() {
                SlotGameProto.BonusResultItemResponse.Builder builder = SlotGameProto.BonusResultItemResponse.newBuilder();
                builder.setCol(col);
                builder.setRow(row);
                builder.setMoney(money);
                return builder;
            }
        }

        private String getHashKey(int row, int col){
            return String.format("%d_%d", row, col);
        }

        public void openItem(int row, int col){
            if (!openedItems.canOpen()){
                throw new OpenBonusItemLimit();
            }
            String key = getHashKey(row, col);
            if (openedItems.containsKey(key)){
                throw new ItemBonusIsOpened();
            }
            int money = rawResult[row][col];
            if (money == 0){ // Nếu mở trúng hũ rỗng
                openedEmptyItem++;
            } else { // Số tiền khi mở trúng ô cố định phải giựa trên số hũ rỗng đã mở
                money = money + (openedEmptyItem * baseMoney);
            }
            openedItems.put(new BonusItem(row, col, money));
        }

        public void openAll(){
            while (openedItems.size() < 10+openedEmptyItem){
                int random_row = random.randInt(0, rawResult.length-1);
                int random_col = random.randInt(0, rawResult[0].length-1);
                try {
                    openItem(random_row, random_col);
                } catch (ItemBonusIsOpened ignore){

                }
            }
        }

        public boolean canOpen(){
            return openedItems.canOpen();
        }

        BonusWinResult(int lineId){
            winResult = new WinResult(lineId);
        }

        BonusWinResult(){
            winResult = new WinResult();
        }

        private void calculateBaseMoney(){
            int multiple = 0;
            baseMoney = (int)(bonusConfig.getBase3Multiple() * player.getGoldStake().getAmount());
            if (getBonusItemNumber() == 3){
                int rand_int = random.randInt(0, 1000);
                if (rand_int < bonusConfig.getPercentBase3Multiple3() * 10){
                    multiple = bonusConfig.getBase3Multiple3();
                } else if (rand_int < bonusConfig.getPercentBase3Multiple2() * 10){
                    multiple = bonusConfig.getBase3Multiple2();
                } else {
                    multiple = bonusConfig.getBase3Multiple();
                }
            } else if (getBonusItemNumber() == 4){
                multiple = bonusConfig.getBase4Multiple();
            }
            currentBaseMoney = (int)(multiple * player.getGoldStake().getAmount());
        }

        private int calculateBigWinMoney(){
            int min = bonusConfig.getBigWinMultiplePaddingMin();
            int max = bonusConfig.getBigWinMultiplePaddingMax();
            if (getBonusItemNumber() == 3){
                min += bonusConfig.getBase3Multiple();
                max += bonusConfig.getBase3Multiple();
            } else if (getBonusItemNumber() == 4){
                min += bonusConfig.getBase4Multiple();
                max += bonusConfig.getBase4Multiple();
            }
            int rand_int = random.randInt(min, max);
            return (int) player.getGoldStake().getAmount() * rand_int;
        }

        void generateRawResult(){
            int remainBigWin = bonusConfig.getBigWinNumber();
            int remainEmpty = bonusConfig.getEmptyNumber();
            this.calculateBaseMoney();
            for (int i=0; i<rawResult.length; i++){
                for (int j=0; j<rawResult[i].length; j++){
                    int rand_int = random.randInt(0,2);
                    if (rand_int == 0){
                        rawResult[i][j] = currentBaseMoney;
                    } else if (rand_int == 1){
                        if (remainBigWin > 0){
                            rawResult[i][j] = calculateBigWinMoney();
                            remainBigWin--;
                        } else {
                            rawResult[i][j] = currentBaseMoney;
                        }
                    } else {
                        if (remainEmpty > 0){
                            rawResult[i][j] = 0;
                            remainEmpty--;
                        } else {
                            rawResult[i][j] = currentBaseMoney;
                        }
                    }
                }
            }

        }

        public int getBonusItemNumber(){
            return this.winResult.size();
        }

        public int getMaxMoneyCanWin(){
            if (getBonusItemNumber() < 3 || getBonusItemNumber() > 5){
                return 0;
            }

            if (maxMoneyCanWin != 0){
                return maxMoneyCanWin;
            }

            if (getBonusItemNumber() == 5){
                maxMoneyCanWin = (int) player.getGoldStake().getAmount() * bonusConfig.getBase5Multiple();
            } else {
                int baseMultiple = bonusConfig.getBase3Multiple() * 3;
                if (getBonusItemNumber() == 4){
                    baseMultiple = bonusConfig.getBase4Multiple() + bonusConfig.getRate3Bonus() * 2;
                }
                maxMoneyCanWin = (int) player.getGoldStake().getAmount() * (
                        (baseMultiple * 7) + ((baseMultiple + bonusConfig.getBigWinMultiplePaddingMax()) * 3)
                );

                // 1 line * (baseMultiple * 7) = ăn được 7 hũ thường khi 2 hũ đầu tiền mở trống
                // 1 line * ((baseMultiple + bonusConfig.getBigWinMultiplePaddingMax()) * 3) = ăn được 3 hũ lớn với số random to nhất
                // => đây là trường hợp nó ăn được nhiều nhất
            }
            return maxMoneyCanWin;
        }

        void execute(){
            if (!isExecute){
                if (this.isWin()){
                    if (getBonusItemNumber() == 5){
                        totalMoney = getMaxMoneyCanWin();
                        winResult.setMoneyAmount(totalMoney);
                    } else {
                        generateRawResult();
                        winResult.setMoneyAmount(0);
                    }
                    isExecute = true;
                } else {
                    this.clear();
                }
            }
        }

        public boolean isWin(){
            return this.winResult.flagItem != null && this.winResult.flagItem.isBonus() && this.winResult.size() > 2;
        }

        public boolean isWin5Item(){
            return isWin() && getBonusItemNumber() == 5;
        }

        public void add(BoardItem item) {
            winResult.add(item);
        }

        @Override
        public void clear(){
            super.clear();
            winResult.clear();
        }

        public long getTotalMoney() {
            execute();
            return totalMoney;
        }

        @Override
        public SlotGameProto.BonusWinResultProto.Builder parseProtoBuilder() {
            execute();
            SlotGameProto.BonusWinResultProto.Builder builder = SlotGameProto.BonusWinResultProto.newBuilder();
            builder.addAllMoneyList(this);
            builder.setWinResult(winResult.getProtoMessage());
            return builder;
        }

        void merge(BonusWinResult bonusWinResult){
            this.isExecute = true;
            this.addAll(bonusWinResult);
            this.totalMoney += bonusWinResult.getTotalMoney();
            this.winResult.isExecute = true;
            this.winResult.addAllUnion(bonusWinResult.getWinResult());
            this.winResult.setMoneyAmount(totalMoney);
        }

        WinResult getWinResult(){
            return winResult;
        }

        public OpenedItems getOpenedItems() {
            return openedItems;
        }
    }

    class ListWinResult extends ArrayList<WinResult> implements ProtoSerializer<SlotGameProto.WinResultProto> {

        long totalMoney = 0;
        boolean isExecute = false;
        List<SlotGameProto.ResultItemIndex> resultItemIndexList;
        void execute(){
            if (!isExecute){
                resultItemIndexList = new ArrayList<>(this.size());
                this.forEach(winResult -> {
                    totalMoney += winResult.getMoneyAmount();
                    resultItemIndexList.addAll(winResult.getResultItemIndexProtoList());
                });
                isExecute = true;
            }
        }

        long getTotalMoney(){
            execute();
            return totalMoney;
        }

        List<SlotGameProto.ResultItemIndex> getResultItemIndexList() {
            execute();
            return resultItemIndexList;
        }

        @Override
        public SlotGameProto.WinResultProto.Builder parseProtoBuilder() {
            SlotGameProto.WinResultProto.Builder builder = SlotGameProto.WinResultProto.newBuilder();
            builder.setMoney(getTotalMoney());
            builder.addAllItemIndex(this.getResultItemIndexList());
            return builder;
        }

        public List<SlotGameProto.WinResultProto> getProtoList(){
            return this.stream().map(ProtoSerializer::getProtoMessage).collect(Collectors.toList());
        }
    }

    class ListBonusWinResult extends ArrayList<BonusWinResult>{

        BonusWinResult mergedResult = null;

        List<SlotGameProto.BonusWinResultProto> getProtoList(){
            return this.stream().map(ProtoSerializer::getProtoMessage).collect(Collectors.toList());
        }

        BonusWinResult getMergedResult(){
            if (mergedResult == null){
                 mergedResult = new BonusWinResult();
                for (BonusWinResult bonusWinResult : this) {
                    mergedResult.merge(bonusWinResult);
                }
            }
            return mergedResult;
        }

        List<SlotGameProto.BonusWinResultProto> getMergedProto(){
            List<SlotGameProto.BonusWinResultProto> mergedProto = new ArrayList<>();
            mergedProto.add(getMergedResult().getProtoMessage());
            return mergedProto;
        }

        /**
         * Tổng số lượt lật bài
         */
        public int getTotalReward(){
            return getMergedResult().size();
        }
    }

    class SpinResult extends ArrayList<HashBoardItem>{
        SpinResult(){
            super(player.getLines().size());
            player.getLines().forEach(mainSlotLine -> {
                this.add(new HashBoardItem(mainSlotLine));
            });
            execute();
        }

        /**
         * Class này dùng để lưu xem line nào thắng và thắng kiểu gì
         * Mục đích để log
         */
        class WinLineResult{
            MainSlotLine line;
            SlotLineWinType winType;
            long totalMoney;
            WinLineResult(HashBoardItem hashBoardItem){
                if (hashBoardItem.isWin()){
                    this.line = hashBoardItem.line;
                    this.totalMoney = hashBoardItem.getWinMoney();
                    if (hashBoardItem.isWinNormal()){
                        this.winType = SlotLineWinType.NORMAL;
                    }
//                    else if (hashBoardItem.isWinBonus()){
//                        this.winType = SlotLineWinType.BONUS;
//                    }
                    else if (hashBoardItem.isWinJackpot()){
                        this.winType = SlotLineWinType.JACKPOT;
                    } else {
                        throw new WrongSlotLineWinType(getPlayer());
                    }
                }
            }

            @Override
            public String toString() {
                return String.format("\n%s - %s - gold: %d", line.toString(), winType.name(), this.totalMoney);
            }
        }

        private ListWinResult listWinResult = new ListWinResult();
        private ListWinResult listJackpotWinResult = new ListWinResult();
//        private ListBonusWinResult listBonusWinResult = new ListBonusWinResult();
        private ArrayList<MainSlotLine> winLines = new ArrayList<>();
        private ArrayList<WinLineResult> winLineResults = new ArrayList<>();
        private long totalMoney;
        private long jackpotMoney;
        private long normalMoney;

        private void execute(){
            this.forEach(hashBoardItem -> {
                if (hashBoardItem.isWin()){
                    this.winLines.add(hashBoardItem.getLine());
                    this.winLineResults.add(new WinLineResult(hashBoardItem));
                    if (hashBoardItem.isWinJackpot()){
                        this.listJackpotWinResult.add(hashBoardItem.normalWinResult);
                        this.jackpotMoney += hashBoardItem.getWinMoney();
                    } else if (hashBoardItem.isWinNormal()){
                        this.listWinResult.add(hashBoardItem.normalWinResult);
                    }
//                    else if (hashBoardItem.isWinBonus()){
//                        this.listBonusWinResult.add(hashBoardItem.bonusWinResult);
//                    }

                    if (!hashBoardItem.isWinJackpot()){
                        this.normalMoney += hashBoardItem.getWinMoney();
                    }

                    this.totalMoney += hashBoardItem.getWinMoney();
                }
            });
            if (getBonusWinResult().isWin5Item()){
                this.totalMoney += getBonusWinResult().getTotalMoney();
            }
        }

        ArrayList<MainSlotLine> getWinLines() {
            return winLines;
        }

        public ArrayList<WinLineResult> getWinLineResults() {
            return winLineResults;
        }

        ListWinResult getListWinResult() {
            return listWinResult;
        }

        ListWinResult getListJackpotWinResult() {
            return listJackpotWinResult;
        }

//        ListBonusWinResult getListBonusWinResult() {
//            return listBonusWinResult;
//        }

        long getTotalMoney() {
            return totalMoney;
        }

        public long getJackpotMoney() {
            return jackpotMoney;
        }

        public long getNormalMoney() {
            return normalMoney;
        }

//        /**
//         * Lấy tổng số lượt lật bài khi bonus
//         */
//        public int getTotalBonusReward(){
//            return listBonusWinResult.getTotalReward();
//        }

        @Override
        public String toString() {
            return getWinLineResults().toString();
        }
    }

    private SpinResult getSpinResult() {
        return spinResult;
    }

    private ListBonusWinResult getListBonusWinResult(){
        return listBonusWinResult;
    }

    public BonusWinResult getBonusWinResult(){
        return bonusWinResult;
    }

    /**
     * Lấy tổng số lượt lật bài
     */
    public int getTotalBonusReward(){
        return getListBonusWinResult().getTotalReward();
//        return getSpinResult().getTotalBonusReward();
    }

    @Override
    public SlotGameProto.SpinSlotResponse.Builder parseProtoBuilder() {
        SlotGameProto.SpinSlotResponse.Builder responseBuilder = SlotGameProto.SpinSlotResponse.newBuilder();
        SpinResult spinResult = getSpinResult();
        responseBuilder.addAllLine(spinResult.getWinLines().stream().map(SlotLine::getId).collect(Collectors.toList()));
        responseBuilder.addAllResultItemIndex(resultItems);
        if (!spinResult.getListWinResult().isEmpty()){
            responseBuilder.addAllNormal(spinResult.getListWinResult().getProtoList());
        }
        if (getBonusWinResult().isWin5Item()){
            responseBuilder.addNormal(getBonusWinResult().getWinResult().getProtoMessage());
        }
        if (!getListBonusWinResult().isEmpty()){
            responseBuilder.addAllBonus(getListBonusWinResult().getMergedProto());
        }
        if (!spinResult.getListJackpotWinResult().isEmpty()){
            responseBuilder.addAllJackpot(spinResult.getListJackpotWinResult().getProtoList());
        }
        if (getFreeSpinResult().isWin()){
            responseBuilder.setFreeSpin(getFreeSpinResult().getProtoMessage());
        }
        responseBuilder.setMoney(spinResult.getTotalMoney());
        return responseBuilder;
    }

    public static SlotGameProto.SpinSlotResponse getErrorProto(int errorCode){
        SlotGameProto.SpinSlotResponse.Builder responseBuilder = SlotGameProto.SpinSlotResponse.newBuilder();
        responseBuilder.setErrorCode(errorCode);
        return responseBuilder.build();
    }

    public static SlotGameProto.BonusResultAllResponse getBonusResultAllErrorProto(int errorCode){
        SlotGameProto.BonusResultAllResponse.Builder builder = SlotGameProto.BonusResultAllResponse.newBuilder();
        builder.setErrorCode(errorCode);
        return builder.build();
    }

    public SlotPlayer getPlayer() {
        return player;
    }

//    public int getNumberLineWinBonus(){
//        return this.getSpinResult().getListBonusWinResult().size();
//    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("Player[%d]", player.getId()));
        if (player.isFreeSpin()){
            stringBuilder.append(String.format(" is freeSpin[%d],", getPlayer().getFreeSpin()));
        } else {
            stringBuilder.append(String.format(" is spend[%d] gold,", getPlayer().getTotalStake().getAmount()));
        }
        stringBuilder.append(String.format(" win total[%d] gold", getMoneyWin()));
        if (getFreeSpinResult().getFreeSpinAmount() > 0){
            stringBuilder.append(
                    String.format(", and win [%d] freeSpin", getFreeSpinResult().getFreeSpinAmount())
            );
        }
        if (getBonusWinResult().getTotalMoney()>0){
            stringBuilder.append(
                    String.format(", and win [%d] bonus", getBonusWinResult().getTotalMoney())
            );
        }
        stringBuilder.append(String.format("\nBoard: %s", Arrays.deepToString(this.board)));
        stringBuilder.append(String.format("\nResult: %s", getSpinResult().toString()));

        return stringBuilder.toString();
    }

    public String printRawBoardCSV(){
        StringBuilder builder = new StringBuilder();
        for (int[] ints : this.board) {
            for (int anInt : ints) {
                if (builder.length() != 0) {
                    builder.append(",");
                }
                builder.append(anInt);
            }
        }
        return builder.toString();
    }
}
