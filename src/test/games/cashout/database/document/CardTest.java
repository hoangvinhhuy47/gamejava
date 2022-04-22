package games.cashout.database.document;

import games.cashout.constant.CardType;
import games.payment.database.document.DPaymentHistoryTest;
import games.payment.object.PaymentItem;

import static org.junit.Assert.*;

public class CardTest {



    public static void initTestData(){
        for (int i = 0; i < 1000; i++) {
            Card card = new Card();
            String cardType = DPaymentHistoryTest.getRandomCardType();
            PaymentItem paymentItem = DPaymentHistoryTest.getRandomPaymentItem();
            String cardCode = String.format("%s_code_%d", cardType, i);
            card.setCardCode(cardCode);
            card.setUsed(false);
            card.setCardSerial(String.format("%s_serial_%d", cardType, i));
            card.setCardType(cardType);
            card.setCardValue(paymentItem.getValue());
            card.saveDB();
        }
    }

    public static void main(String[] args) {
        initTestData();

    }

}