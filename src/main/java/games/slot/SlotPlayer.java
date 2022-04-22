package games.slot;

import games.core.application.ServerConfig;
import games.core.common.money.Money;
import games.core.common.money.MoneyService;
import games.core.common.money.MoneyType;
import games.core.database.model.MFundHistory;
import games.core.datatype.BasePlayer;
import games.slot.data.MainSlotLine;
import games.slot.data.ResultBoard;
import games.slot.exception.InvalidStakeStep;
import games.slot.exception.PlayerLineNotValid;
import games.slot.room.SlotRoom;
import libs.util.data.HashByInt;

import java.util.ArrayList;
import java.util.List;

public class SlotPlayer extends BasePlayer {

    private static ServerConfig serverConfig = ServerConfig.getInstance();

    /**
     * Các line user đang đặt
     */
    private ArrayList<MainSlotLine> lines = new ArrayList<>();

    /**
     * Tổng tiền player đang đặt
     */
    private Money totalStake = new Money(0, MoneyType.GOLD);

    /**
     * Lượt quay free hiện tại
     */
    private int freeSpin = 0;

    /**
     * Số tiền nó thắng jackpot ở lượt quay hiện tại
     */
    private long currentJackpot = 0;

    private ResultBoard.BonusWinResult bonusWinResult = null;

    void plusFreeSpin(int freeSpin) {
        this.freeSpin += freeSpin;
    }

    void subFreeSpin(int freeSpin){
        this.freeSpin -= freeSpin;
        if (this.freeSpin < 0){
            this.freeSpin = 0;
        }
    }

    public boolean isFreeSpin() {
        return this.freeSpin > 0;
    }

    public int getFreeSpin(){
        return freeSpin;
    }

    public void resetFreeSpin(){
        this.freeSpin = 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    protected SlotRoom room;

    protected int stakeStep;

    /**
     * Mức tiền đặt của user
     */
    protected Money goldStake = new Money(0, MoneyType.GOLD);


    /**
     * Cập nhật lại line mà player đặt
     * @param lines List<Integer>
     */
    public void updateLines(List<Integer> lines, HashByInt<MainSlotLine> configLines) throws PlayerLineNotValid {
        this.lines.clear();
        for (int line: lines){
            MainSlotLine configLine = configLines.getOrDefault(line, null);
            if (configLine == null){
                throw new PlayerLineNotValid(this);
            }
            this.lines.add(configLine);
        }
    }

    public Money getGoldStake() {
        return goldStake;
    }

    public int getStakeStep() {
        return stakeStep;
    }

    public void increaseStakeStep() throws InvalidStakeStep {
        if (this.stakeStep+1>room.getRoomStake().getMaxStep()){
            throw new InvalidStakeStep(this, this.stakeStep+1);
        }
        this.goldStake.setAmount(room.getRoomStake().getMoneyByStep(++this.stakeStep));
    }

    public void decreaseStakeStep() throws InvalidStakeStep {
        if (this.stakeStep-1<room.getRoomStake().getMinStep()){
            throw new InvalidStakeStep(this, this.stakeStep-1);
        }
        this.goldStake.setAmount(room.getRoomStake().getMoneyByStep(--this.stakeStep));
    }

    public void setStakeStep(int step) throws InvalidStakeStep {
//        int minStep = room.getRoomStake().getMinStep();
//        int maxStep = room.getRoomStake().getMaxStep();
//        if (step < minStep || step > maxStep){
//            throw new InvalidStakeStep(this, step);
//        }
        this.stakeStep = step;
        this.goldStake.setAmount(room.getRoomStake().getMoneyByStep(step));
    }

    /**
     * Được gọi khi player join vào room và khi leave room
     * -Để set room và mức đặt, số tiền đặt của room cho player
     * -Nếu room = null tức là khi leave room thì set mức đặt và số tiền đặt về 0
     * @param room : Room instance
     */
    public void setRoom(SlotRoom room) {
        this.room = room;
        if (room != null){
            this.stakeStep = room.getRoomStake().getDefaultStep();
            this.goldStake.setAmount(room.getRoomStake().getDefaultMoney());
        } else {
            this.stakeStep = 0;
            this.goldStake.setAmount(0);
        }
    }

    public SlotRoom getRoom() {
        return room;
    }

    public void onLeaveRoom(){
        setRoom(null);
        this.executeBonusWinResult();
    }

    public ArrayList<MainSlotLine> getLines() {
        return lines;
    }

    public Money getTotalStake() {
        long total = this.getNumLine() * this.getGoldStake().getAmount();
        totalStake.setAmount(total);
        return totalStake;
    }

    public Money getAddToPotStakeMoney(){
        Money potMoney = getTotalStake().clone();
        return potMoney.setPercent(serverConfig.getJackpotRate());
    }

    public Money getAddToFundDealerStakeMoney(){
        Money fundMoney = getTotalStake().clone();
        return fundMoney.setPercent(serverConfig.getDealerRate());
    }

    /**
     * Lấy số dòng mà user đang đặt
     * @return int
     */
    public int getNumLine() {
        return this.lines.size();
    }

    /**
     * Lấy jackpot ra từ hũ và khoá nó lại đã trúng từng này jackpot sau khi tạo ResultBoard
     * - Mỗi lần tạo mới ResultBoard cần phải gọi hàm resetJackpotAmount (Đã gọi trong constructor của ResultBoard)
     *  để tránh trường hợp tạo đi tạo lại ResultBoard sẽ bị dính do logic ràng buộc
     */
    public synchronized long getAndLockJackpotAmount(){
        if (this.currentJackpot == 0){
            this.currentJackpot = this.getRoom().getRoomPot().getAmount();
        }
        return this.currentJackpot;
    }

    public void resetJackpotAmount(){
        this.currentJackpot = 0;
    }

    /**
     * Được gọi khi có thằng nổ hũ
     * Ví dụ: Khi trong hũ có 1100 => player ăn hết 1100 gold
     *  Nhưng yêu cầu trong quỹ phải luôn có ít nhất 500 gold (minJackpot)
     *  nên sau khi player ăn jackpot thì phải lấy từ trong quỹ ra 500 để đưa vào hũ
     */
    public synchronized void releaseJackpotAmount(){
        if (this.currentJackpot == 0){
            return;
        }
        try {
            long jackpotMoneyWin = this.currentJackpot;
            if (jackpotMoneyWin<=0){
                return;
            }
            long fundBeforeValue = getRoom().getRoomFund().getAmount(); // Tiền của quỹ trước khi trừ

            this.getRoom().getRoomPot().sub(jackpotMoneyWin); // Trừ tiền trong hũ

            //Số tiền cần bù vào hũ sau khi rút (minJackpot - potAfterSub)
            long moneySubFund = this.getRoom().getRoomInfo().getMinJackpot() - this.getRoom().getRoomPot().getAmount();
            if (moneySubFund > 0){
                // Rút từ quỹ ra để cộng vào hũ
                this.getRoom().getRoomPot().plus(moneySubFund);
                this.getRoom().getRoomFund().sub(moneySubFund);
                MFundHistory.getInstance().logSubFund(
                        SlotConst.SLOT_OCEAN_ID, this.getRoom().getId(),
                        fundBeforeValue, moneySubFund, "Hệ thống"
                );

            }
        } finally {
            this.resetJackpotAmount();
        }
    }

    public ResultBoard.BonusWinResult getBonusWinResult() {
        return bonusWinResult;
    }

    public void setBonusWinResult(ResultBoard.BonusWinResult bonusWinResult) {
        this.bonusWinResult = bonusWinResult;
    }

    public void executeBonusWinResult()
    {
        if (bonusWinResult != null){
            if (bonusWinResult.canOpen()){
                bonusWinResult.openAll();
            }
            Money money = new Money(bonusWinResult.getOpenedItems().getTotalMoney(), MoneyType.GOLD);
            this.getUser().SetMoney(money.getAmount());
            MoneyService.getInstance().addMoneyToSave(money, this.getUser(), true, "Slot_Game_Bonus");
            this.bonusWinResult = null;
        }
    }
}
