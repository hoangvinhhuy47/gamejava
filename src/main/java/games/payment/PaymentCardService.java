package games.payment;

import games.core.application.ServerConfig;
import games.payment.object.NewUserCardResponse;
import games.payment.object.UserCardRequest;
import libs.util.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PaymentCardService {
    private static PaymentCardService instance;
    public static PaymentCardService getInstance() {
        if (instance == null) instance = new PaymentCardService();
        return instance;
    }
    private static Logger logger;

    private static final String SERVER_CARD_URL = "http://112.78.3.60/the247/api/";
    private static final String ADD_CARD_API = "banthe/addcard";
    private static final String CHECK_CARD_API = "banthe/GetCardTransId?TransId=";
    private static final String LOGIN_URL = SERVER_CARD_URL + "account/login";
    private static final String ADD_CARD_API_URL = SERVER_CARD_URL + "banthe/addcard";
    private static final String CHECK_CARD_API_URL = SERVER_CARD_URL + "banthe/GetCardTransId";
    private static final String TOKEN = "E6J43TvHVTgnCbozO8lqz_dKgSuCc3Sitr8pvK50olGpZQW6Bl25xxRL8ANgUCIh3F9yWTRGfHoULXTGAWsEKBvVq3-4c_2KXnY5tqvVObNNtLVAzPC1Y3zwmZdKPsC6uRycs7uvb7M0xwWn79KLr4ql5xrq1FGyw4CHJoDF4S8yb-Zwgiwd0UN03sPoqotCYwGSfOk6PAoVR_cFG35pmLcZ5yVXOghC4RwVKLaQuiWcvGnNTuBL0_4hH4OcpB6As4XnxHBNpsqVw14HnKmjVUI_x6V77kVvEvdxzM9gTv3EMmE1StjCvWUHSQ63P9DsAEZDxZq74z6WQlMAYf-F_GKw00xBRSuF8z60EDD09QA9M5zjAN6u2OveWG8JiGlhRolzz-eHasKYnisxbOZNUlh4_iu1auViwGNhyT904R6r1kBjsl7BWr-aIsTMo5bjKPMO4eZ3OwQcOKGPD-f6GpzVSnNtvaXJMhg50NG6AVplnhNg0cUQ1aFA0ddsga0QhGmDv8JJBx5IbQ-6T97IBg";
    private static String token = null;

    private PaymentCardService(){
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
    }

    private static String getCardTransIdUrl(String transId){
        return CHECK_CARD_API_URL + "?TransId=" + transId;
    }

    private static String getToken(){
        if (token == null)
        {
            Map<String, String> params = new HashMap<>();
            params.put("UserName", ServerConfig.getInstance().getThe247UserName());
            params.put("Password", ServerConfig.getInstance().getThe247Password());
            try {
                SmileeHttpResponse response = HttpUtil.callPOSTWithParameters(LOGIN_URL, params, false);
                Map<String, Object> responseMap = JsonUtil.convertJsonToMap(response.getData());
                Map<String, Object> result = (Map<String, Object> )responseMap.get("Result");
                Map<String, Object> currentToken = (Map<String, Object>) result.get("CurrentToken");
                token = (String) currentToken.get("access_token");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return token;
    }


    public static void main(String[] args){
        UserCardRequest cardRequest = new UserCardRequest();
        cardRequest.setCardCode("319029760526928");
        cardRequest.setCardSerial("10004526710550");
        cardRequest.setCardTypeId(1);
        NewUserCardResponse cardResponse = getInstance().requestCard(10, cardRequest);

    }

    public NewUserCardResponse requestCard(int userId, UserCardRequest cardRequest)
    {
        HashMap<String, String> dataRequest = new HashMap<>();
        dataRequest.put("Code", cardRequest.getCardCode());
        dataRequest.put("Serial", cardRequest.getCardSerial());
        dataRequest.put("CardType", "" + cardRequest.getCardTypeId());
        dataRequest.put("Price", String.valueOf(cardRequest.getCardValue()));
        String transId = userId + "-" + Helper.getTimeStamp();
        dataRequest.put("TransId", transId);
        logger.info("REQUEST CARD TransId: " + transId);
        SmileeHttpResponse response;

        NewUserCardResponse userCardResponse = new NewUserCardResponse();
        int code = -99;
        int status = -99;
        try {
            response = HttpUtil.callPOSTWithParametersWithAuthen(ADD_CARD_API_URL, dataRequest, false, getToken());

            logger.info("The247 requestCard response:" + response.getData());

            Map<String, Object> responseMap = JsonUtil.convertJsonToMap(response.getData());
            code = (Integer)responseMap.get("Code");
            if (responseMap.get("Result") != null){
                Map<String, Object> result = (Map<String, Object> )responseMap.get("Result");
                if (result.get("Status") != null){
                    status = (int)result.get("Status");
                    logger.info("REQUEST CARD: Code = " + code + " Status: " + status);
                }
            }
        } catch (Exception e) {
            logger.error("Request to the247 error", e);
        }
        if(code == 200)
        {
            //tiep tuc goi de check card
            userCardResponse = verifyCard(transId, 1);
            userCardResponse.setCode(code);
        } else {
            //mua loi roi -> return luon
            userCardResponse.setCode(code);
            userCardResponse.setVerifyStatus(code);
        }
        userCardResponse.setStatus(status);

        return userCardResponse;
    }

    public NewUserCardResponse verifyCard(String transId, int retry_time) {
        logger.info("VERIFY CARD trans_id=" + transId);

        NewUserCardResponse userCardResponse = new NewUserCardResponse();
        userCardResponse.setVerifyStatus(-99);
        userCardResponse.setCode(-99);
        userCardResponse.setPrice(-1);
        SmileeHttpResponse response;
        try {
            response = HttpUtil.callGET(getCardTransIdUrl(transId), getToken());

            logger.info("The247 verifyCard response: " + response.getData());

            Map<String, Object> responseMap = JsonUtil.convertJsonToMap(response.getData());
            int codeHTTP = (Integer)responseMap.get("Code");
            userCardResponse.setCode(codeHTTP);
            logger.info("VERIFY CARD trans_id=" + transId +" httpCode = " + codeHTTP);
            if (codeHTTP == 200)
            {

                Map<String, Object> result = (Map<String, Object> )responseMap.get("Result");
                int status = (int)result.get("status");
                logger.info("VERIFY CARD trans_id=" + transId +" => Verify time: " + retry_time + ", status:" + status);

                if (status != 1 && status!=2 && status!=4 && retry_time <= 60)
                {
                    logger.info("VERIFY CARD trans_id=" +transId+ "" + " wait 10 second for next check");
                    Thread.sleep(10000);
                    return this.verifyCard(transId, retry_time+1);
                }
                double price = (double)result.get("amount");
                userCardResponse.setVerifyStatus(status);
                userCardResponse.setPrice(price);
            }

        } catch (Exception e) {
            logger.error("VERIFY CARD trans_id = " + transId + " FAIL", e);
        }
        return  userCardResponse;
    }
}
