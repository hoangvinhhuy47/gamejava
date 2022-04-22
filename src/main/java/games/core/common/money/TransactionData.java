package games.core.common.money;

/**
 * @author Cuong Nguyen Cao
 */

public class TransactionData {

    /**
     * Status of transaction
     */
    private TransactionStatus status;

    /**
     * Amount of transaction
     */
    private Money transactionAmount;

    /**
     * real Amount sub money (if transactionAmount > Total money of user -> = Total money)
     */
    private Money realTransactionAmount;

    /**
     * Amount of money after transaction
     */
    private Money remainingAmount;

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setRemainingAmount(Money remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void setRealTransactionAmount(Money realTransactionAmount) {
        this.realTransactionAmount = realTransactionAmount;
    }

    public void setTransactionAmount(Money transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Money getTransactionAmount() {
        return transactionAmount;
    }

    public Money getRealTransactionAmount() {
        return realTransactionAmount;
    }

    public Money getRemainingAmount() {
        return remainingAmount;
    }
}
