package games.core.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import games.core.application.ServerConfig;
import libs.util.LogFactory;
import org.apache.log4j.Logger;

public class MongoManager
{
    private ServerConfig serverConfig;
    private MongoClient client;
    private MongoDatabase database;
    private MongoDatabase tx_database;
    private MongoDatabase databaseTools;

    private String host;
    private int port;
    private String user;
    private String pass;
    private static MongoManager _instance;
    private static Logger logger = LogFactory.getLogger(MongoManager.class.getSimpleName());
    public static MongoManager getInstance() {
        if (_instance == null) _instance = new MongoManager();
        return _instance;
    }
    private MongoManager()
    {
        serverConfig = ServerConfig.getInstance();
        host = serverConfig.getMongoHost();
        port = serverConfig.getMongoPort();
        user = serverConfig.getMongoUser();
        pass = serverConfig.getMongoPass();
        String database = serverConfig.getMongoDBName();
        String databaseTools = serverConfig.getMongoDBToolsName();
        customInit();
        setDatabase(database);
        setDatabaseTools(databaseTools);
    }

    MongoManager(String host, int port){
        this.host = host;
        this.port = port;
        customInit();
    }
    private void customInit(){
        MongoClientURI mongoClientURI;
        if ((user!= null && pass != null) && (!user.equals("") && !pass.equals(""))){
            mongoClientURI = new MongoClientURI(String.format("mongodb://%s:%s@%s:%s",user, pass, host, port));
        } else {
            mongoClientURI = new MongoClientURI(String.format("mongodb://%s:%s", host, port));
        }
        client = new MongoClient(mongoClientURI);
    }

    public MongoClient getClient() {
        return client;
    }

    public void setDatabase(String databaseName){
        this.database = client.getDatabase(databaseName);
    }

    public void setDatabaseTools(String databaseName){
        this.databaseTools = client.getDatabase(databaseName);
    }

    public MongoDatabase getDatabaseTools() {return databaseTools;}
    public MongoDatabase getDatabase() {
        return database;
    }

    public void setTXDatabase(String databaseName)
    {
        tx_database = client.getDatabase(databaseName);
    }

    public MongoDatabase getTXDatabase()
    {
        return tx_database;
    }
}
