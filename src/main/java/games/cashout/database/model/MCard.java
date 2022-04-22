package games.cashout.database.model;

import games.cashout.database.MongoCashoutConst;
import games.cashout.database.document.Card;
import games.core.database.MongoManager;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tuanhoang on 8/31/17.
 */
@CollectionClass(MongoCashoutConst.COLL_CARD)
public class MCard extends MGModelAdapter<Card> implements MongoCashoutConst{
    public static MCard instance;

    public static MCard getInstance() {
        if (instance == null) instance = new MCard();
        return instance;
    }

    protected MCard() {
        super(MongoManager.getInstance().getDatabase());
    }

    public List<Card> getCardByCardTypeAndValue(String cardType, int value, int limit) {
        List<Document> listDoc = this.mgCollection.find(new Document(FIELD_CARD_TYPE, cardType)
                .append(FIELD_CARD_VALUE, value)
                .append(FIELD_CARD_IS_USED, false))
                .limit(limit).into(new ArrayList<>());

        List<Card> listCards = new ArrayList<>();
        for (Document document : listDoc) {
            Card card = new Card();
            try {
                this.createByDocument(card, document);
                listCards.add(card);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return listCards;
    }


    public Card getValidCardBy(String cardType, int value){
        List<Card> cards = getCardByCardTypeAndValue(cardType, value, 1);
        if (cards.size() > 0){
            return cards.get(0);
        }
        return null;
    }

}
