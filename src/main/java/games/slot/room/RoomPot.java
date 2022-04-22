package games.slot.room;

import games.core.common.money.Money;
import games.core.database.document.DRoomInfo;
import games.core.exception.currency.CannotLessThanZero;

public class RoomPot {
    private long amount;
    private DRoomInfo roomInfo;

    public RoomPot(DRoomInfo roomInfo)
    {
        this.roomInfo = roomInfo;
        this.amount = roomInfo.getPot();
    }

    void plusByPercent()
    {
        double potPercent = roomInfo.getPotPercent();
        long finalAmount = (long)Math.floor(this.amount * (potPercent/100));
        this.plus(finalAmount);
    }

    public void plus(Money money){
        this.plus(money.getAmount());
    }

    public void plus(long amount)
    {
        try {
            roomInfo.plusPot(amount);
            this.amount = roomInfo.getPot();
        } catch (CannotLessThanZero cannotLessThanZero) {
            cannotLessThanZero.printStackTrace();
        }
    }

    public void sub(Money money){
        this.sub(money.getAmount());
    }

    /**
     * Lấy ra số tiền trong hũ ứng với jackpotPercent (phần trăm lấy ra khỏi hũ)
     */

    public long jackpot()
    {
        long moneyJackpot = Math.round(roomInfo.getPotPercent()/100 * roomInfo.getPot());
        sub(moneyJackpot);
        return moneyJackpot;
    }

    public boolean isReachMax(){
        return this.getAmount() >= this.roomInfo.getMaxJackpot();
    }

    public long withdraw(int percent)
    {
        long moneyWithdraw = Math.round(roomInfo.getPotPercent()/100 * roomInfo.getPot());
        sub(moneyWithdraw);
        return moneyWithdraw;
    }

    public void sub(long amount)
    {
        try {
            roomInfo.subPot(amount);
            this.amount = roomInfo.getPot();
        } catch (CannotLessThanZero cannotLessThanZero) {
            cannotLessThanZero.printStackTrace();
        }
    }

    public long getAmount() {
        return amount;
    }
}
