package games.payment.database.document;

import games.cashout.constant.CardType;
import games.core.common.data.DataCommonManager;
import games.payment.database.model.MPaymentHistory;
import games.payment.object.PaymentItem;
import libs.util.CRandom;

import static org.junit.Assert.*;

public class DPaymentHistoryTest {

    public static String getRandomCardType(){
        CRandom cRandom = new CRandom();
        int cardTypeId = cRandom.randInt(1, 3);
        CardType cardType = CardType.getById(cardTypeId);
        if (cardType!=null){
            return cardType.getValue();
        }
        return "vt";
    }

    public static PaymentItem getRandomPaymentItem(){
        CRandom cRandom = new CRandom();
        int randInt = cRandom.randInt(21, 26);
        return DataCommonManager.getInstance().getPaymentItem(randInt);
    }

    public static void initTestData(){
        for (int i = 0; i < 100; i++) {
            DPaymentHistory dPaymentHistory = new DPaymentHistory();
            PaymentItem paymentItem = getRandomPaymentItem();
            dPaymentHistory.setCardType(getRandomCardType());
            dPaymentHistory.setCardValue(paymentItem.getValue());
            dPaymentHistory.setValue(paymentItem.getRealValue());
            dPaymentHistory.setType(DPaymentHistory.CASHOUT);
            dPaymentHistory.setStatus(DPaymentHistory.PEDDING);
            dPaymentHistory.setId((int)MPaymentHistory.getInstance().getNextId());
            dPaymentHistory.setUserId(1);
            dPaymentHistory.setTimestamp((int) (System.currentTimeMillis()/1000));
            dPaymentHistory.saveDB();
        }

    }

    public static void main(String[] args) {
        initTestData();
    }

}