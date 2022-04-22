package games.api;

import games.cashout.CashoutService;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

import java.util.Map;

public class CashoutSection implements IApiSection {
    private static CashoutSection instance;
    public static CashoutSection getInstance() {
        if (instance == null) instance = new CashoutSection();
        return instance;
    }
    private Logger logger;
    private CashoutService cashoutService;

    private CashoutSection() {

        cashoutService = CashoutService.getInstance();
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }
    @Override
    public ApiReturnObject process(String action, Map<String, Object> data) {
        ApiReturnObject apiReturnObject = new ApiReturnObject();
        switch (action){
            case ACCEPT_RECEIPT:
                try{
                    logger.info("accept - " + data.toString() + " - data size = " + data.size());
                    int receiptId = Integer.parseInt((String) data.get("_id"));
                    String adminName = (String) data.get("approver");
                    logger.info("Accept reciept - id = " + receiptId + " - admin = " + adminName);
                    apiReturnObject.setCode(acceptReceipt(receiptId, adminName));
                } catch (Exception e){
                    logger.error("Accept reciept error", e);
                    apiReturnObject.setCode(ApiResponseConst.REQUEST_CANNOT_FINISH);
                }
                break;
        }
        return apiReturnObject;
    }

    private int acceptReceipt(int receiptId, String adminName) throws Exception {
        //TODO accept receipt return error_code success = 200
        int error_code = cashoutService.acceptReciept(receiptId, adminName);
        if (error_code == 0) error_code = ApiResponseConst.SUCCESS;
        return error_code;
    }
}
