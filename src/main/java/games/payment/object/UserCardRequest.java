package games.payment.object;

/**
 * Created by tuanhoang on 10/4/17.
 */
public class UserCardRequest {
    private String cardSerial;
    private String cardCode;
    private String cardType;
    private int cardTypeId;
    private String accountName;
    private String appCode;
    private String refCode;
    private int cardValue;
    /**
     * Get the value of refCode
     *
     * @return the value of refCode */
    public String getRefCode() {
        return refCode;
    }
    /**
     * Set the value of refCode
     *
     * @param refCode new value of refCode */
    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }
    /**
     * Get the value of appCode
     *
     * @return the value of appCode */
    public String getAppCode() {
        return appCode;
    }
    /**
     * Set the value of appCode
     *
     * @param appCode new value of appCode */
    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }
    /**
     * Get the value of accountName
     *
     * @return the value of accountName
     */
    public String getAccountName() {
        return accountName;
    }
    /**
     * Set the value of accountName
     *
     * @param accountName new value of accountName
     */
    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
    /**
     * Get the value of cardType
     *
     * @return the value of cardType */
    public String getCardType() {
        return cardType;
    }
    /**
     * Set the value of cardType
     *
     * @param cardType new value of cardType */
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    /**
     * Get the value of cardCode
     *
     * @return the value of cardCode */
    public String getCardCode() {
        return cardCode;
    }
    /**
     * Set the value of cardCode
     *
     * @param cardCode new value of cardCode */
    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }
    /**
     * Get the value of cardSerial
     *
     * @return the value of cardSerial */
    public String getCardSerial() {
        return cardSerial;
    }
    /**
     * Set the value of cardSerial
     *
     * @param cardSerial new value of cardSerial */
    public void setCardSerial(String cardSerial) {
        this.cardSerial = cardSerial;
    }

    public int getCardTypeId() {
        return cardTypeId;
    }

    public void setCardTypeId(int cardTypeId) {
        this.cardTypeId = cardTypeId;
    }

    public int getCardValue() {
        return cardValue;
    }

    public void setCardValue(int cardValue) {
        this.cardValue = cardValue;
    }

    @Override
    public String toString() {
        String pattern =
                "UserCardRequest: " +
                " User: %s" +
                " CardCode: %s, " +
                " CardSerial: %s, " +
                " CardType: %s, " +
                " CardTypeId: %s, " +
                " CardValue: %s";
        return String.format(
                pattern,
                getAccountName(),
                getCardCode(),
                getCardSerial(),
                getCardType(),
                getCardTypeId(),
                getCardValue()
                );
    }
}
