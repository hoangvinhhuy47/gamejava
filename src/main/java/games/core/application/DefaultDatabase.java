package games.core.application;

import games.cashout.database.document.Card;
import games.cashout.database.model.MCard;
import games.features.database.document.TextRun;
import games.features.database.model.MTextRun;
import games.minigame.database.document.GiftCode;
import games.minigame.database.model.MGiftCode;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.time.ZonedDateTime;

/**
 * Created by tuanhoang on 8/23/17.
 * Dùng để khởi tạo 1 số database ban đầu, test là chính
 */

public class DefaultDatabase {

    private static DefaultDatabase instance;
    public static DefaultDatabase getInstance() {
        if (instance == null){
            instance = new DefaultDatabase();
        }
        return instance;
    }
    Logger logger = LogFactory.getLogger(this.getClass().getSimpleName());
    MGiftCode mGiftCode = MGiftCode.getInstance();
    MTextRun mTextRun = MTextRun.getInstance();
    MCard mCard = MCard.getInstance();

    public void init() {
        if (mGiftCode.getCountCollection() != giftCodes.length) {
            mGiftCode.dropCollection();
            for (GiftCode giftCode : giftCodes) {
                try {
                    mGiftCode.saveToMongo(giftCode);
                } catch (Exception e) {
                    logger.equals(e);
                }
            }
        }
        int countCard = (int)mCard.getCountCollection();
        if (countCard != cards.length) {
            mCard.dropCollection();
            for (Card card : cards) {
                try {
                    mCard.saveToMongo(card);
                } catch (Exception e) {
                    logger.equals(e);
                }
            }
        }

        if (mTextRun.getCountCollection() == 0) {
            for (TextRun textRun : textRuns) {
                try {
                    mTextRun.addTextRun(textRun);
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }

    public static Card[] cards = {
        new Card("cardcode_vt_20000_1", "cardserial", "vt", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_20000_2", "cardserial", "vt", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_20000_3", "cardserial", "vt", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_20000_4", "cardserial", "vt", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_20000_5", "cardserial", "vt", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_10000_1", "cardserial", "vt", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_10000_2", "cardserial", "vt", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_10000_3", "cardserial", "vt", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_10000_4", "cardserial", "vt", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vt_10000_5", "cardserial", "vt", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_20000_1", "cardserial", "mb", 20000, (int)ZonedDateTime.now().toEpochSecond()),

            new Card("cardcode_mb_20000_2", "cardserial", "mb", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_20000_3", "cardserial", "mb", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_20000_4", "cardserial", "mb", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_20000_5", "cardserial", "mb", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_10000_1", "cardserial", "mb", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_10000_2", "cardserial", "mb", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_10000_3", "cardserial", "mb", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_10000_4", "cardserial", "mb", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_mb_10000_5", "cardserial", "mb", 10000, (int)ZonedDateTime.now().toEpochSecond()),

            new Card("cardcode_vn_20000_1", "cardserial", "vn", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_20000_2", "cardserial", "vn", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_20000_3", "cardserial", "vn", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_20000_4", "cardserial", "vn", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_20000_5", "cardserial", "vn", 20000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_10000_1", "cardserial", "vn", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_10000_2", "cardserial", "vn", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_10000_3", "cardserial", "vn", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_10000_4", "cardserial", "vn", 10000, (int)ZonedDateTime.now().toEpochSecond()),
            new Card("cardcode_vn_10000_5", "cardserial", "vn", 10000, (int)ZonedDateTime.now().toEpochSecond())

    };

    public static GiftCode[] giftCodes = {
        new GiftCode("codedemo","Demo", 100000, 0, 0),
        new GiftCode("codedemo1","Demo", 100000, 0, 1),
        new GiftCode("codedemo2","Demo", 100000, 0, 2),
        new GiftCode("codedemo3","Demo", 100000, 0, 3),
        new GiftCode("codedemo4","Demo", 100000, 0, 4),
        new GiftCode("codedemo5","Demo", 100000, 0, 5),
        new GiftCode("codedemo6","Demo", 100000, 0, 6),
        new GiftCode("codedemo7","Demo", 100000, 0, 7),
        new GiftCode("codedemo8","Demo", 100000, 0, 8),
        new GiftCode("codedemo9","Demo", 100000, 0, 9),
        new GiftCode("codedemo10","Demo", 200000, 0, 0),
        new GiftCode("codedemo11","Demo", 200000, 0, 1),
        new GiftCode("codedemo12","Demo", 200000, 0, 2),
        new GiftCode("codedemo13","Demo", 200000, 0, 3),
        new GiftCode("codedemo14","Demo", 200000, 0, 4),
        new GiftCode("codedemo15","Demo", 200000, 0, 5),
        new GiftCode("codedemo16","Demo", 200000, 0, 6),
        new GiftCode("codedemo17","Demo", 200000, 0, 7),
        new GiftCode("codedemo18","Demo", 200000, 0, 8),
        new GiftCode("codedemo19","Demo", 200000, 0, 9),
        new GiftCode("codedemo20","Demo", 200000, 0, 10),
        new GiftCode("HDTsUPzRqUr7e","Hoàng Đẹp Trai", 100000, 0, 10),
            new GiftCode("HDTfUPzRqUr7e","Hoàng Đẹp Trai", 100000, 0, 10),
    };

    public static TextRun[] textRuns = {
        new TextRun("Chào mừng bạn đến với Săn Cá Huyền Thoại", "Link", true, 5, (int)ZonedDateTime.now().toEpochSecond(), ((int)ZonedDateTime.now().toEpochSecond() + 30 * 24 * 60 * 60), 600, true),
        new TextRun("Người chơi Long Tiến đã thắng được 1000k gold từ game bắn cá", "Link", true, 10, (int)ZonedDateTime.now().toEpochSecond(), ((int)ZonedDateTime.now().toEpochSecond() + 60 * 60), 20, true),
    };
}
