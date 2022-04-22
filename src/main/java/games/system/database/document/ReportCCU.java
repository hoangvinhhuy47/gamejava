package games.system.database.document;

import games.system.database.MongoSystemConst;
import libs.database.mongodb.MGDocument;
import libs.database.mongodb.annotation.CollectionClass;
import libs.database.mongodb.annotation.DocumentField;
import libs.database.mongodb.annotation.DocumentFieldPK;
import libs.database.mongodb.exception.NotFoundDocumentInDatabase;
import libs.database.mongodb.exception.NullCollection;

/**
 * Created by tuanhoang on 9/8/17.
 */
@CollectionClass(MongoSystemConst.COLL_REPORT_CCU)
public class ReportCCU extends MGDocument implements MongoSystemConst {

    @DocumentField(FIELD_DEFAULT_ID)
    @DocumentFieldPK
    private int day = 0;

    @DocumentField(FIELD_REPORT_CCU_ACU)
    private int acu = 0;

    @DocumentField(FIELD_REPORT_CCU_COUNT_UPDATE)
    private int count = 0;

    @DocumentField(FIELD_REPORT_CCU_PCU)
    private int pcu = 0;

    @DocumentField(FIELD_REPORT_CCU_ACU_IN_GAME)
    private int acuInGame = 0;

    @DocumentField(FIELD_REPORT_CCU_COUNT_UPDATE_IN_GAME)
    private int countInGame = 0;

    @DocumentField(FIELD_REPORT_CCU_PCU_IN_GAME)
    private int pcuInGame = 0;

    public void updateByCCU(int ccu) {
        if (pcu < ccu) {
            this.setPcu(ccu);
        }

        int total = this.acu * this.count + ccu;
        this.count ++;
        this.setAcu((int)Math.ceil(total * 1.0f / this.count));
        this.setCount(this.count);
    }

    public void updateByCCUInGame(int ccu) {
        if (pcuInGame < ccu) {
            this.setPcuInGame(ccu);
        }

        int total = this.acuInGame * this.countInGame + ccu;
        this.countInGame ++;
        this.setAcuInGame((int)Math.ceil(total * 1.0f / this.countInGame));
        this.setCountInGame(this.countInGame);
    }


    @Override
    public MGDocument updateDB() throws IllegalAccessException, NullCollection, NotFoundDocumentInDatabase {
        return null;
    }

    @Override
    public MGDocument saveDB() throws IllegalAccessException, NoSuchFieldException {
        return null;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
        this.changed.put(FIELD_DEFAULT_ID, this.day);
    }

    public int getAcu() {
        return acu;
    }

    public void setAcu(int acu) {
        this.acu = acu;
        this.changed.put(FIELD_REPORT_CCU_ACU, this.acu);
    }

    public int getPcu() {
        return pcu;
    }

    public void setPcu(int pcu) {
        this.pcu = pcu;
        this.changed.put(FIELD_REPORT_CCU_PCU, this.pcu);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
        this.changed.put(FIELD_REPORT_CCU_COUNT_UPDATE, this.count);
    }

    public int getAcuInGame() {
        return acuInGame;
    }

    public void setAcuInGame(int acuInGame) {
        this.acuInGame = acuInGame;
        this.changed.put(FIELD_REPORT_CCU_ACU_IN_GAME, this.acuInGame);
    }

    public int getCountInGame() {
        return countInGame;
    }

    public void setCountInGame(int countInGame) {
        this.countInGame = countInGame;
        this.changed.put(FIELD_REPORT_CCU_COUNT_UPDATE_IN_GAME, this.countInGame);
    }

    public int getPcuInGame() {
        return pcuInGame;
    }

    public void setPcuInGame(int pcuInGame) {
        this.pcuInGame = pcuInGame;
        this.changed.put(FIELD_REPORT_CCU_PCU_IN_GAME, this.pcuInGame);
    }
}
