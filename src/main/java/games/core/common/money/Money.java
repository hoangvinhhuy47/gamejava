package games.core.common.money;

/**
 * @author Cuong Nguyen Cao
 *
 */
public class Money {
    private long amount;
    private MoneyType moneyType;
    public Money(){
        amount = 0;
        moneyType = MoneyType.NONE;
    }
    public Money(long amount, MoneyType moneyType)
    {
        if (amount < 0) amount = 0;
        this.amount = amount;
        this.moneyType = moneyType;
    }

    public void setAmount(long amount) {
        if (amount < 0) amount = 0;
        this.amount = amount;
    }

    public MoneyType getMoneyType() {
        return moneyType;
    }

    public long getAmount() {
        return amount;
    }

    public int compare(Money o){
        if (this.getAmount() > o.getAmount()){
            return 1;
        } else {
            if (this.getAmount() < o.getAmount()){
                return -1;
            }
        }
        return 0;
    }

    public Money sub(long subAmount){
        setAmount(getAmount()-subAmount);
        return this;
    }

    public Money add(long addAmount)
    {
        setAmount(getAmount()+addAmount);
        return this;
    }

    /**
     * For example: 1000 gold: input 10 => 900 gold
     */
    public Money subPercent(double percent){
        long amountRemain = (long)Math.floor(this.amount * ((100-percent)/100));
        setAmount(amountRemain);
        return this;
    }

    /**
     * For example: 1000 gold: input 10 => 100 gold
     */
    public Money setPercent(double percent){
        long amountRemain = (long) Math.floor(this.amount * (percent/100));
        setAmount(amountRemain);
        return this;
    }

    @Override
    public Money clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e){
            return new Money(this.getAmount(), this.getMoneyType());
        }
        return new Money(this.getAmount(), this.getMoneyType());
    }

}
