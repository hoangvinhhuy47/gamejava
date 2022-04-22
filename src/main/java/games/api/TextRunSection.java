package games.api;

import games.features.database.MongoFeatureConst;
import games.features.database.document.TextRun;
import games.features.pushmessage.PushMessageService;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.Map;

public class TextRunSection implements IApiSection {
    private static TextRunSection instance;
    PushMessageService pushMessageService;

    public static TextRunSection getInstance() {
        if (instance == null) instance = new TextRunSection();
        return instance;
    }

    private Logger logger;
    TextRunSection() {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        pushMessageService = PushMessageService.getInstance();
    }

    @Override
    public ApiReturnObject process(String action, Map<String, Object> data) {
        ApiReturnObject apiReturnObject = new ApiReturnObject();
        switch (action){
            case CREATE_TEXT_RUN:
                try {
                logger.info("Create text run");
                    createTextRun(data);
                    apiReturnObject.setData(data);
                } catch (Exception e){
                   logger.error("Create text run error - ", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
            case RELOAD_TEXT_RUN:
                try{
                logger.info("Reload text run");
                    reloadTextRun();
                } catch (Exception e){
                    logger.error("reload text run error - ", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
        }
        return apiReturnObject;
    }

    private void createTextRun(Map requestData) throws NoSuchFieldException, IllegalAccessException {
        TextRun textRun = new TextRun();
        textRun.setActive(Boolean.valueOf((String)requestData.get(MongoFeatureConst.FIELD_MESSAGE_IS_ACTIVE)));
        textRun.setAuto(Boolean.valueOf((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_IS_AUTO)));
        textRun.setContent((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_CONTENT));
        textRun.setLink((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_LINK));
        textRun.setLoopInterval(Integer.valueOf((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_LOOP_INTERVAL)));
        textRun.setTimeStart(Integer.valueOf((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_TIME_START)));
        textRun.setTimeEnd(Integer.valueOf((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_TIME_END)));
        textRun.setPriority(Integer.valueOf((String) requestData.get(MongoFeatureConst.FIELD_MESSAGE_PRIORITY)));
        //MTextRun.getInstance().addTextRun(textRun);

        pushMessageService.addTextRun(textRun);
    }

    private void reloadTextRun(){
        pushMessageService.forceTextRunCurrentForAllUser();
    }
}
