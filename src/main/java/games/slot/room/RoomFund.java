package games.slot.room;

import games.core.common.money.Money;
import games.core.database.document.DRoomInfo;
import games.core.exception.currency.CannotLessThanZero;

public class RoomFund {
    private long amount;
    private long dealerAmount;
    private DRoomInfo roomInfo;
    public RoomFund(DRoomInfo roomInfo){
        this.roomInfo = roomInfo;
        this.amount = roomInfo.getFund();
        this.dealerAmount = roomInfo.getFundDealer();
    }

    public void plus(Money money){
        this.plus(money.getAmount());
    }

    public void plus(long money){
        try {
            roomInfo.plusFund(money);
            this.amount += money;
        } catch (CannotLessThanZero cannotLessThanZero) {
            cannotLessThanZero.printStackTrace();
        }
    }

    public void sub(Money money){
        this.sub(money.getAmount());
    }

    public void sub(long money){
        try {
            roomInfo.subFund(money);
            this.amount -= money;
        } catch (CannotLessThanZero cannotLessThanZero) {
            cannotLessThanZero.printStackTrace();
        }
    }

    public void subDealer(long money){
        roomInfo.subFundDealer(money);
        this.dealerAmount -= money;
    }

    public void plusDealer(long money){
        roomInfo.plusFundDealer(money);
        this.dealerAmount += money;
    }

    public long getAmount() {
        return amount;
    }

    public long getDealerAmount() {
        return dealerAmount;
    }

    public void setFundAnd(long fund, long fundDealer, long jackpot){
        roomInfo.setFund(fund);
        roomInfo.setPot(jackpot);
        roomInfo.setFundDealer(fundDealer);
        roomInfo.saveDB();
        this.amount = fund;
    }
}
