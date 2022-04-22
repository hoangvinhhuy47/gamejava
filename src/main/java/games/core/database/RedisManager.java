package games.core.database;

import games.core.application.ServerConfig;
import libs.database.redis.RedisInstance;


/**
 * @author Cuong Nguyen Cao
 */
public class RedisManager extends RedisInstance {
    private String prefix = "fish";

    public static RedisManager instance;

    public static RedisManager getInstance() {
        if (instance == null) instance = new RedisManager();
        return instance;
    }
    private RedisManager(){
        super(ServerConfig.getInstance().getRedisHost(), ServerConfig.getInstance().getRedisPort());
    }

    public RedisManager(String host, int port){
        super(host, port);
    }
    

}
