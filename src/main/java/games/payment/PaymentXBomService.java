//package games.payment;
//
//import games.payment.constant.CardType;
//import games.payment.object.UserCardRequest;
//import libs.util.LogFactory;
//import org.apache.log4j.Logger;
//
//import javax.xml.soap.*;
//import javax.xml.transform.Source;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.stream.StreamResult;
//import java.io.ByteArrayOutputStream;
//import java.io.StringWriter;
//import java.security.MessageDigest;
//
///**
// * Created by tuanhoang on 10/4/17.
// */
//public class PaymentXBomService {
//    private static PaymentXBomService instance;
//    private static Logger logger;
//
//    public static PaymentXBomService getInstance() {
//        //if (instance == null) instance = new PaymentXBomService();
//        return instance;
//    }
//    PaymentXBomService() {
//        logger = LogFactory.getLogger(this.getClass().getSimpleName());
//    }
//    private final static String BASE_URL = "http://103.90.220.78/Payment/VPGService.asmx";//"http://103.90.220.78/adminpayv2/";
//    private final static String PARTNER_CODE = "mrh";
//    private final static String SERVICE_CODE = "cardtelco";
//    private final static String COMMAND_CODE = "usecard";
//    private final static String PARTNER_KEY = "92f8278b47c1294e0c28e9dd87ead0f6";
//    public  String getMD5(String input) {
//        MessageDigest md = null;
//        try {
//            md = MessageDigest.getInstance("MD5");
//            byte[] inputByteArray = md.digest(input.getBytes("UTF-8"));
//            StringBuilder sb = new StringBuilder();
//            for (byte b : inputByteArray) {
//                sb.append(String.format("%02X", b));
//            }
//            String hash = sb.toString().toLowerCase();
//            return hash;
//        } catch (Exception e) {
//            logger.error("MD5 error", e);
//        }
//        return "";
//    }
//    private SOAPMessage createSOAPRequest(String partnerCode, String serviceCode,
//                                                 String commandCode, String requestContent, String signature) {
//        try {
//            MessageFactory messageFactory = MessageFactory.newInstance();
//            SOAPMessage soapMessage = messageFactory.createMessage();
//            SOAPPart soapPart = soapMessage.getSOAPPart();
//            String serverURI = "http://103.90.220.78/PaymentGateway/VPGService.asmx";
//            SOAPEnvelope envelope = soapPart.getEnvelope();
//            envelope.addNamespaceDeclaration("xsd", "http://www.w3.org/2001/XMLSchema");
//            SOAPBody soapBody = envelope.getBody();
//            SOAPElement sbeRequest = soapBody.addChildElement("Request", "",
//                    "http://tempuri.org/");
//            SOAPElement sbePartnerCode = sbeRequest.addChildElement("partnerCode");
//            sbePartnerCode.addTextNode(partnerCode);
//            SOAPElement sbeServiceCode = sbeRequest.addChildElement("serviceCode");
//            sbeServiceCode.addTextNode(serviceCode);
//            SOAPElement sbeCommandCode = sbeRequest.addChildElement("commandCode");
//            sbeCommandCode.addTextNode(commandCode);
//            SOAPElement sbeRequestContent = sbeRequest.addChildElement("requestContent");
//            sbeRequestContent.addTextNode(requestContent);
//            SOAPElement sbeSignature = sbeRequest.addChildElement("signature");
//            sbeSignature.addTextNode(signature);
//            MimeHeaders headers = soapMessage.getMimeHeaders();
//            headers.addHeader("SOAPAction", "http://tempuri.org/Request");
//            soapMessage.saveChanges();
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            soapMessage.writeTo(out);
//            String strMsg = new String(out.toByteArray());
//            return soapMessage;
//        } catch (Exception e) {
//            logger.error("createSOAPRequest error", e);
//        }
//        return null;
//    }
//    private String getSOAPResponse(SOAPMessage soapResponse) {
//        try {
//            TransformerFactory transformerFactory = TransformerFactory.newInstance();
//            Transformer transformer = transformerFactory.newTransformer();
//            Source sourceContent = soapResponse.getSOAPPart().getContent();
//            StringWriter writer = new StringWriter();
//            StreamResult result = new StreamResult(writer);
//            transformer.transform(sourceContent, result);
//            String resultString = writer.toString();
//            return resultString;
//        } catch (Exception e) {
//            logger.error("getSOAPResponse error", e);
//        }
//        return null;
//    }
//
////    private SOAPMessage requestPayment(String partnerCode, String serviceCode,
////                                      String commandCode, String requestContent, String signature) {
////        try {
////            SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
////            SOAPConnection soapConnection = soapConnectionFactory.createConnection();
////            URL endpoint = new URL(PaymentXBomService.getInstance().BASE_URL);
////            SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(partnerCode,
////                    serviceCode, commandCode, requestContent, signature),
////                    endpoint);
////            return soapResponse;
////        } catch (SOAPException | UnsupportedOperationException ex) {
////            logger.error("requestPayment error", ex);
////        } catch (MalformedURLException e) {
////            e.printStackTrace();
////        }
////        return null;
////    }
////    public UserCardResponse requestCard(UserCardRequest uCard) {
////        try {
////            Gson gson = new Gson();
////
////            String uCardJSon = gson.toJson(uCard);
////            logger.info("Request card - " + uCardJSon);
////            SOAPMessage resp = PaymentXBomService.getInstance().requestPayment(PARTNER_CODE,
////                    SERVICE_CODE, COMMAND_CODE, uCardJSon, getMD5(PARTNER_CODE + SERVICE_CODE +
////                            COMMAND_CODE + uCardJSon + PARTNER_KEY));
////            String result = getSOAPResponse(resp);
////            DOMParser parser = new DOMParser();
////
////            parser.parse(new InputSource(new java.io.StringReader(result)));
////            Document doc = parser.getDocument();
////            String message = doc.getDocumentElement().getTextContent();
////            logger.info("Response card - " + message);
////            UserCardResponse response = gson.fromJson(message, UserCardResponse.class);
////            //System.out.printf("resposne code = " + response.getResponseCode());
////            return response;
////        } catch (Exception e) {
////            logger.error("requestCard error", e);
////        }
////        return null;
////    }
//
//
//    public static void main(String[] args) {
//        UserCardRequest ucard = new UserCardRequest();
//        ucard.setCardCode("017905615036");
//        ucard.setCardSerial("063421000000645");
//        ucard.setCardType(CardType.MOBIPHONE.getValue());
//        ucard.setAccountName("acc00001");
//
//        //String result = requestCard(ucard);
//
////        System.out.println(result);
//
//
//    }
//}
