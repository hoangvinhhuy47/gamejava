package games.cashout.database.model;

import games.cashout.database.MongoCashoutConst;
import games.cashout.database.document.IPAddressCheck;
import games.core.database.MongoManager;
import libs.database.mongodb.MGModelAdapter;
import libs.database.mongodb.annotation.CollectionClass;
import org.bson.Document;

/**
 * Created by tuanhoang on 9/1/17.
 */
@CollectionClass(MongoCashoutConst.COLL_IP_CHECK)
public class MIPAddressCheck  extends MGModelAdapter<IPAddressCheck> implements MongoCashoutConst{

    public static MIPAddressCheck instance;

    public static MIPAddressCheck getInstance() {
        if (instance == null) instance = new MIPAddressCheck();
        return instance;
    }

    protected MIPAddressCheck() {
        super(MongoManager.getInstance().getDatabase());
    }

    public IPAddressCheck getIPAddressCheckByIP(String ipAddress) throws NoSuchFieldException, IllegalAccessException {
        IPAddressCheck ipAddressCheck = new IPAddressCheck(ipAddress);
        try {
            this.updateFromMongo(ipAddressCheck);
        } catch (Exception e) {
            ipAddressCheck.setCountCard(0);
            ipAddressCheck.setListUserId(new Document());
            ipAddressCheck.setListCardRecieved(new Document());
            this.saveToMongo(ipAddressCheck);
        }
        return ipAddressCheck;
    }
}
