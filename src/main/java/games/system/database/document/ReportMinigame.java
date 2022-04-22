package games.system.database.document;

import games.system.database.MongoSystemConst;
import games.system.database.model.MReportMinigame;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;

/**
 * Created by tuanhoang on 9/8/17.
 */
@CollectionClass(MongoSystemConst.COLL_REPORT_MINIGAME)
public class ReportMinigame extends MGDocument implements MongoSystemConst {

    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int day = 0;

    @DocumentField(FIELD_REPORT_MINIGAME_TICKET_FROM_GIFT_CODE)
    private int ticketGiftCode = 0;

    @DocumentField(FIELD_REPORT_MINIGAME_TICKET_FROM_WHEEL)
    private int ticketWheel = 0;

    @DocumentField(FIELD_REPORT_MINIGAME_TICKET_FROM_PROMOTE_CODE)
    private int ticketPromoteCode = 0;

    @DocumentField(FIELD_REPORT_MINIGAME_TICKET_FROM_LOGIN)
    private int ticketLogin = 0;

    @DocumentField(FIELD_REPORT_MINIGAME_TICKET_FROM_ADMIN)
    private int ticketAdmin = 0;

    @Override
    public MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        MReportMinigame.getInstance().updateFromMongo(this);
        return this;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        MReportMinigame.getInstance().saveToMongo(this);
        return this;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
        this.changed.put(FIELD_DEFAULT_ID, this.day);
    }

    public int getTicketGiftCode() {
        return ticketGiftCode;
    }

    public void setTicketGiftCode(int ticketGiftCode) {
        this.ticketGiftCode = ticketGiftCode;
        changed.put(FIELD_REPORT_MINIGAME_TICKET_FROM_GIFT_CODE, this.ticketGiftCode);
    }

    public int getTicketWheel() {
        return ticketWheel;
    }

    public void setTicketWheel(int ticketWheel) {
        this.ticketWheel = ticketWheel;
        this.changed.put(FIELD_REPORT_MINIGAME_TICKET_FROM_WHEEL, this.ticketWheel);
    }

    public int getTicketPromoteCode() {
        return ticketPromoteCode;
    }

    public void setTicketPromoteCode(int ticketPromoteCode) {
        this.ticketPromoteCode = ticketPromoteCode;
        this.changed.put(FIELD_REPORT_MINIGAME_TICKET_FROM_PROMOTE_CODE, this.ticketPromoteCode);
    }

    public int getTicketLogin() {
        return ticketLogin;
    }

    public void setTicketLogin(int ticketLogin) {
        this.ticketLogin = ticketLogin;
        this.changed.put(FIELD_REPORT_MINIGAME_TICKET_FROM_LOGIN, this.ticketLogin);
    }

    public int getTicketAdmin() {
        return ticketAdmin;
    }

    public void setTicketAdmin(int ticketAdmin) {
        this.ticketAdmin = ticketAdmin;
        this.changed.put(FIELD_REPORT_MINIGAME_TICKET_FROM_ADMIN, this.ticketAdmin);
    }
}
