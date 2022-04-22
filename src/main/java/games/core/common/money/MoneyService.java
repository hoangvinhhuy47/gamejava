package games.core.common.money;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import games.api.ApiResponseConst;
import games.api.ApiReturnObject;
import games.core.database.MongoManager;
import games.core.database.document.UserMoney;
import games.core.database.model.MUserMoney;
import games.core.user.IUser;
import games.core.user.User;
import games.core.user.UserCommand;
import games.core.user.UserService;
import libs.util.CEncrypt;
import libs.util.LogFactory;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.Path;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import static com.mongodb.client.model.Filters.eq;


/**
 * @author Cuong Nguyen Cao
 * Mọi hoạt động cộng trừ tiền của player phải thông qua class này
 * Lưu tiền vào database
 */

@Path("/api")
public class MoneyService {

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static MoneyService instance;
    private static MUserMoney mUserMoney = MUserMoney.getInstance();

    UserService userService;
    Logger logger;
    public static MoneyService getInstance()
    {
        if (instance == null) instance = new MoneyService();
        return instance;
    }

    private MoneyService() {
        logger = LogFactory.getLogger(this.getClass().getSimpleName());
        userService = UserService.getInstance();
    }


    public static String encodeString(String text)
            throws UnsupportedEncodingException
    {
        byte[] bytes = text.getBytes("UTF-8");
        String encodeString = Base64.encode(bytes);
        return encodeString;
    }

    public static String decodeString(String encodeText)
            throws UnsupportedEncodingException
    {
        byte[] decodeBytes = Base64.decode(encodeText);
        String str = new String(decodeBytes, "UTF-8");
        return str;
    }

    public TransactionData addMoneyS(Money moneyAdd, int userId, String from) throws IllegalAccessException, NoSuchFieldException, IOException {
        IUser user = userService.getUser(userId);

        if (user != null)
        {
            return addMoneyToSave(moneyAdd, user, true, from);
        }
        else
        {
            user = new User(userId, 0);
            UserMoney userMoney = new UserMoney(userId).updateDB();
            long goldFromBag = user.getMoneyGold();

            long newMoney = moneyAdd.getAmount();
            if (userMoney != null)
            {
                long goldAfter = userMoney.getMoneyGold() + newMoney;

                if (moneyAdd.getMoneyType() == MoneyType.GOLD)
                    userMoney.setMoneyGold(goldAfter);
                else
                    userMoney.setMoneySilver(goldAfter);

                if (newMoney >= 900000)
                {
                    Whitetxt(user, goldFromBag, goldAfter, 0, newMoney, from);
                }

//                String json = APISendMoney.JsonObjectToString(user.getUserName(), Long.toString(goldAfter),0,"");
//                APISendMoney.postWithJson(APISendMoney.urlAPI ,json);

                userMoney.saveDB();
                TransactionData transactionData = new TransactionData();
                transactionData.setStatus(TransactionStatus.SUCCESS);
                transactionData.setRealTransactionAmount(moneyAdd);
                transactionData.setRemainingAmount(user.getMoneyBag().get(moneyAdd.getMoneyType()));
                return transactionData;
            }
            else
            {
                TransactionData transactionData = new TransactionData();
                transactionData.setStatus(TransactionStatus.INPUT_ERROR);
                return transactionData;
            }

        }
    }

    public TransactionData addMoneyN(Money moneyAdd, IUser user)
    {
        TransactionData transactionData = new TransactionData();
        transactionData.setStatus(user.addMoney(moneyAdd));
        transactionData.setTransactionAmount(moneyAdd);

        if (transactionData.getStatus() == TransactionStatus.SUCCESS)
        {
            transactionData.setRealTransactionAmount(moneyAdd);
            transactionData.setRemainingAmount(user.getMoneyBag().get(moneyAdd.getMoneyType()));
        }
        return transactionData;
    }

    public TransactionData addMoneyN(Money moneyAdd, IUser user, String from) throws IOException {
        long goldFromBag = user.getMoneyGold();

        TransactionData transactionData = new TransactionData();
        transactionData.setStatus(user.addMoney(moneyAdd));
        transactionData.setTransactionAmount(moneyAdd);

        if (transactionData.getStatus() == TransactionStatus.SUCCESS)
        {
            long gold = user.GetMoney();
            long goldAfter = user.getMoneyGold();

            if ( gold >= 900000 || (gold <= 0 && moneyAdd.getAmount() >= 900000) || from.equals("Xem_Video"))
                Whitetxt(user, goldFromBag, goldAfter, gold, moneyAdd.getAmount(), from);

//            String json = APISendMoney.JsonObjectToString(user.getUserName(), Long.toString(goldAfter),0,"");
//            APISendMoney.postWithJson(APISendMoney.urlAPI ,json);

            transactionData.setRealTransactionAmount(moneyAdd);
            transactionData.setRemainingAmount(user.getMoneyBag().get(moneyAdd.getMoneyType()));
        }
        return transactionData;
    }

    public TransactionData addMoneyToSave(Money moneyAdd, IUser user, boolean needToSave, String from)
    {
        TransactionData transactionData = new TransactionData();
        try
        {
            //long gold = user.GetMoney();

            //Money newMoney = new Money(gold, MoneyType.GOLD);

            transactionData = this.addMoneyN(moneyAdd, user, from);

            if (needToSave)
            {
                user.saveUserMoney();
            }
        }
        catch (Exception e)
        {
            logger.error("Add money need save error - user id = " + user.getId(), e);
            transactionData.setStatus(TransactionStatus.NONE);
        }
        return transactionData;
    }

    private void Whitetxt(IUser user, long goldFromBag, long goldAfter,long goldWin, long goldPlus, String from)
    {
        try
        {
            //Tạo txt
//            File file = new File("config/data2/ok.txt");
//
//            String content = null;
//
//            if (file.exists())
//            {
//                FileWriter fw = new FileWriter(file, true);
//
//                content = user.getId() + "\t" + user.getUserName() + "\t" + goldFromBag + "\t" +
//                          goldAfter + "\t" + goldWin + "\t" + goldPlus + "\t" +
//                          from + "\t" + java.time.LocalDateTime.now() + "\n";
//
//                fw.write(content);
//                fw.close();
//            }
            String title = "UserId\tName\tGoldFromBag\tGoldAfter\tWin\tPlus\tFrom\t\tTime\n";
            String content = title + user.getId() + "\t" + user.getUserName() + "\t" + goldFromBag + "\t" +
                          goldAfter + "\t" + goldWin + "\t" + goldPlus + "\t" +
                          from + "\t" + java.time.LocalDateTime.now() + "\n";

            SendMail(content);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    private void SendMail(String content) throws MessagingException, javax.mail.MessagingException
    {

        //Tạo đối tượng Properties và chỉ định thông tin host, port
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", 465);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.port", 465);

        //Tạo đối tượng Session (phiên làm việc)
        Session s = Session.getInstance(props, new javax.mail.Authenticator()
        {
            protected PasswordAuthentication getPasswordAuthentication()
            {
                 String code = null;
                 try
                 {
                     code = decodeString("ZGRveGJzbWx2aWFlbWJ2ag==");
                 }
                 catch (UnsupportedEncodingException e)
                 {
                     e.printStackTrace();
                 }
                 return new PasswordAuthentication("chantuthien@gmail.com", code);

            }
        });


        //Tạo đối tượng messeage
        Message msg = new MimeMessage(s);
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse("egvietnam002@gmail.com")); //vannhienit egvietnam002
        msg.setSubject("Fishing Blockchain");
        msg.setText(content);

//        // 3) create MimeBodyPart object and set your message text
//        BodyPart messageBodyPart1 = new MimeBodyPart();
//        messageBodyPart1.setText("Gửi a");
//
//        // 4) create new MimeBodyPart object and set DataHandler object to this object
//        MimeBodyPart messageBodyPart2 = new MimeBodyPart();
//
//        String filename = "ok.txt";
//        DataSource source = new FileDataSource(filename);
//        messageBodyPart2.setDataHandler(new DataHandler(source));
//        messageBodyPart2.setFileName(filename);
//
//        // 5) create Multipart object and add MimeBodyPart objects to this object
//        Multipart multipart = new MimeMultipart();
//        multipart.addBodyPart(messageBodyPart1);
//        multipart.addBodyPart(messageBodyPart2);
//
//        // 6) set the multiplart object to the message object
//        msg.setContent(multipart);

        //Gửi mail
        Transport.send(msg);
    }
    private String Readtxt()
    {
        try
        {
            List<String> listStr = FileUtils.readLines(new File("config/notthing/ok.txt"), "utf-8");
            for (int i = 1; i< listStr.size(); i++)
            {
                String string = listStr.get(i);
                String[] str = string.split("\t");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public TransactionData subSilverMoney(Money moneySub, int userId, boolean subAnyway) throws IllegalAccessException, NoSuchFieldException, IOException {
        TransactionData transactionData = new TransactionData();
        if (moneySub.getAmount() < 0) {
            transactionData.setStatus(TransactionStatus.INPUT_ERROR);

            return transactionData;
        }

        IUser user = userService.getUser(userId);
        if (user != null) {
            return subMoney(moneySub, user, true, true);
        }
        else
        {
            UserMoney userMoney = new UserMoney(userId).updateDB();
            if (userMoney != null)
            {
                if (moneySub.getMoneyType() == MoneyType.GOLD)
                {
                    if (userMoney.getMoneyGold() > moneySub.getAmount() || subAnyway)
                    {
                        userMoney.setMoneyGold(userMoney.getMoneyGold() - moneySub.getAmount());
                    }
                    else
                    {
                        transactionData.setStatus(TransactionStatus.INPUT_ERROR);
                        return transactionData;
                    }
                }
                else
                {
                    if (userMoney.getMoneySilver() > moneySub.getAmount() || subAnyway)
                    {
                        userMoney.setMoneySilver(userMoney.getMoneySilver() - moneySub.getAmount());
                    }
                    else
                    {
                        transactionData.setStatus(TransactionStatus.INPUT_ERROR);
                        return transactionData;
                    }
                }

                userMoney.saveDB();

                transactionData.setStatus(TransactionStatus.SUCCESS);
                transactionData.setRealTransactionAmount(moneySub);
                transactionData.setRemainingAmount(user.getMoneyBag().get(moneySub.getMoneyType()));
                return transactionData;
            } else {
                //TransactionData transactionData = new TransactionData();
                transactionData.setStatus(TransactionStatus.INPUT_ERROR);
                return transactionData;
            }

        }
    }


    @Path("withdrawal") // Trừ tiền khi use rút tiền
    public ApiReturnObject Withdrawal(Map<String, Object> dataRequest) throws Exception
    {
        ApiReturnObject apiReturnObject = new ApiReturnObject();
        apiReturnObject.setCode(ApiResponseConst.SUCCESS);
        apiReturnObject.setData("SUCCESS");

        String userName = String.valueOf(dataRequest.get("user_name"));
        int userID = GetUserIDFrMongo(userName);
        User user = userService.getUser(userID);

        String password = String.valueOf(dataRequest.get("user_pass"));
        password = CEncrypt.getMD5(CEncrypt.sha256(password));

        try
        {
            if (user == null)
            {
                apiReturnObject.setCode(ApiResponseConst.NOT_FOUND);
                apiReturnObject.setData("NOT_FOUND");
            }

            String userPass = user.getUserPass();
            if (!password.equals(userPass))
            {
                apiReturnObject.setCode(ApiResponseConst.NOT_FOUND);
                apiReturnObject.setData("Wrong username or password!");
                return apiReturnObject;
            }

            String strGold = String.valueOf(dataRequest.get("money"));
            long gold = Long.valueOf(strGold);
            Money subMoney = new Money(gold, MoneyType.GOLD);
            subMoneyAndSaveMongo(subMoney, user, true, true);

            UserCommand.getInstance().forceUpdateMoneyToUser(userID, null);
        }
        catch(Exception e)
        {

        }
          return apiReturnObject;
    }

    private int GetUserIDFrMongo(String userName)
    {
        MongoClient client = MongoManager.getInstance().getClient();
        MongoDatabase database = client.getDatabase("fish");
        MongoCollection<Document> collection = database.getCollection("users");
        Document document = collection.find(eq("user_name", userName)).first();

        if (document != null)
        {
            return (int) document.get("_id");
        }

        return 0;
    }

    public TransactionData subMoneyAndSaveMongo(Money moneySub, IUser user, boolean subAnyway, boolean needToSave){
        TransactionData transactionData = new TransactionData();
        try {
            //Clone new instance save money of user before transaction
            Money moneyBeforeSub = user.getMoneyBag().get(moneySub.getMoneyType()).clone();
            //            //Do transaction
            transactionData.setStatus(user.subMoney(moneySub, subAnyway));
            transactionData.setTransactionAmount(moneySub);
            transactionData.setRemainingAmount(moneyBeforeSub);
            transactionData.setRealTransactionAmount(new Money(0, moneySub.getMoneyType()));
            //Check transaction status (if transaction success)
            if (transactionData.getStatus() == TransactionStatus.SUCCESS)
            {
                //if moneyBeforeSub > moneySub
                if (moneyBeforeSub.compare(moneySub) == 1){
                    transactionData.setRealTransactionAmount(moneySub);
                } else {
                    //If subAnyway and moneyBeforeSub < moneySub
                    if (subAnyway){
                        transactionData.setRealTransactionAmount(moneyBeforeSub);
                    }
                }
                //remain money of user
                transactionData.setRemainingAmount(user.getMoneyBag().get(moneySub.getMoneyType()));
            }
            if (needToSave)
            {
                user.saveUserMoney();
//                String json = APISendMoney.JsonObjectToString(user.getUserName(), Long.toString(user.getMoneyGold()),0,"");
//                APISendMoney.postWithJson(APISendMoney.urlAPI ,json);
            }
            return transactionData;
        } catch (Exception e) {
            e.printStackTrace();
            transactionData.setStatus(TransactionStatus.NONE);
            logger.error("Sub money - " + user.getId() + " error", e);
            return transactionData;
        }

    }

    public TransactionData subMoney(Money moneySub, IUser user, boolean subAnyway, boolean needToSave)
    {
        return subMoneyAndSaveMongo(moneySub, user, subAnyway, needToSave);
    }

    public TransactionData subMoneyFromTool(Money moneySub, int user_id, boolean subAnyway, boolean needToSave){
        IUser user = userService.getUser(user_id);
        return subMoneyAndSaveMongo(moneySub, user, subAnyway, needToSave);
    }

}
