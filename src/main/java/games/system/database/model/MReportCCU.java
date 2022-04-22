package games.system.database.model;

import games.core.database.MongoManager;
import games.system.database.MongoSystemConst;
import games.system.database.document.ReportCCU;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;

/**
 * Created by tuanhoang on 9/8/17.
 */
@CollectionClass(MongoSystemConst.COLL_REPORT_CCU)
public class MReportCCU extends MGModelAdapter<ReportCCU> implements MongoSystemConst{

    public static MReportCCU instance;
    public static MReportCCU getInstance() {
        if (instance == null) instance = new MReportCCU();
        return instance;
    }

    protected MReportCCU() {
        super(MongoManager.getInstance().getDatabaseTools());
    }

    public ReportCCU getReportCCUByDay(int day) throws NoSuchFieldException, IllegalAccessException {
        ReportCCU reportCCU = new ReportCCU();
        reportCCU.setDay(day);
        try {
            this.updateFromMongo(reportCCU);
        } catch (Exception e) {
           this.saveToMongo(reportCCU);
        }
        return reportCCU;
    }
}
