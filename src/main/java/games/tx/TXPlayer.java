package games.tx;

import games.core.application.ServerConfig;
import games.core.common.money.Money;
import games.core.common.money.MoneyType;
import games.core.database.model.MFundHistory;
import games.core.datatype.BasePlayer;
import games.tx.room.TXRoom;

public class TXPlayer extends BasePlayer {

    private static ServerConfig serverConfig = ServerConfig.getInstance();

    /**
     * Các line user đang đặt
     */

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

    protected TXRoom room;

    protected int stakeStep;

    /**
     * Mức tiền đặt của user
     */
    protected Money goldStake = new Money(0, MoneyType.GOLD);


    public Money getGoldStake() {
        return goldStake;
    }

    public int getStakeStep() {
        return stakeStep;
    }

    /**
     * Được gọi khi player join vào room và khi leave room
     * -Để set room và mức đặt, số tiền đặt của room cho player
     * -Nếu room = null tức là khi leave room thì set mức đặt và số tiền đặt về 0
     * @param room : Room instance
     */
    public void setRoom(TXRoom room) {
        this.room = room;
        if (room != null){

        } else {
            this.stakeStep = 0;
            this.goldStake.setAmount(0);
        }
    }

    public TXRoom getRoom() {
        return room;
    }

    public synchronized long getAndLockJackpotAmount(){
        if (this.currentJackpot == 0){
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

}
