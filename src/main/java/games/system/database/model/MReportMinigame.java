package games.system.database.model;

import games.core.database.MongoManager;
import games.system.database.MongoSystemConst;
import games.system.database.document.ReportMinigame;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;

/**
 * Created by tuanhoang on 9/8/17.
 */
@CollectionClass(MongoSystemConst.COLL_REPORT_MINIGAME)
public class MReportMinigame extends MGModelAdapter<ReportMinigame> implements MongoSystemConst{
    public static MReportMinigame instance;
    public static MReportMinigame getInstance() {
        if (instance == null) instance = new MReportMinigame();
        return instance;
    }

    protected MReportMinigame() {
        super(MongoManager.getInstance().getDatabaseTools());
    }

    public ReportMinigame getReportMinigameByDay(int day) throws NoSuchFieldException, IllegalAccessException {
        ReportMinigame reportMinigame = new ReportMinigame();
        reportMinigame.setDay(day);
        try {
            this.updateFromMongo(reportMinigame);
        } catch (Exception e) {
            this.saveToMongo(reportMinigame);
        }
        return reportMinigame;
    }
}
