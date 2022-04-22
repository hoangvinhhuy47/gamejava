package games.core.common.money;

public class MoneyBag {
    private static long DEFAULT_TRIAL_MONEY = 10000;
    private Money gold;
    private Money silver;
    private Money trial;


    public MoneyBag(Long goldAmount, Long silverAmount){
        this.gold = new Money(goldAmount, MoneyType.GOLD);
        this.silver = new Money(silverAmount, MoneyType.SILVER);
        this.trial  = new Money(DEFAULT_TRIAL_MONEY, MoneyType.TRIAL);
    }

    public void resetMoneyTrial() {
        this.trial  = new Money(DEFAULT_TRIAL_MONEY, MoneyType.TRIAL);
    }
    MoneyBag(Money gold, Money silver){
        this.gold = gold;
        this.silver = silver;
    }

    public MoneyBag(){
        gold = new Money(0, MoneyType.GOLD);
        silver = new Money(0, MoneyType.SILVER);
    }

    public Money add(Money money){
        switch (money.getMoneyType()){
            case GOLD:
                gold.add(money.getAmount());
                return gold;
            case SILVER:
                silver.add(money.getAmount());
                return silver;
            case TRIAL:
                trial.add(money.getAmount());
            default:
                return new Money();
        }
    }

    public Money sub(Money money){
        switch (money.getMoneyType()){
            case GOLD:
                return gold.sub(money.getAmount());
            case SILVER:
                return silver.sub(money.getAmount());
            case TRIAL:
                return trial.sub(money.getAmount());
            default:
                return new Money();
        }
    }

    public boolean isEnoughMoney(Money money){
        switch (money.getMoneyType()){
            case GOLD:
                return this.gold.getAmount() >= money.getAmount();
            case SILVER:
                return this.silver.getAmount() >= money.getAmount();
            case TRIAL:
                return this.trial.getAmount() >= money.getAmount();
            default:
                return false;
        }
    }

    public Money get(MoneyType moneyType){
        switch (moneyType){
            case GOLD: return this.gold;
            case SILVER: return this.silver;
            case TRIAL: return this.trial;
            default: return new Money();
        }
    }

}
