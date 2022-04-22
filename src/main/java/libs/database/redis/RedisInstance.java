package libs.database.redis;

import libs.util.LogFactory;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisInstance {
    private Jedis jedis;
    private JedisPool jedisPool;
    private JedisPoolConfig jedisPoolConfig;
    private static Logger logger = LogFactory.getLogger(RedisInstance.class.getSimpleName());
    protected RedisInstance(String host, int port){
        jedisPoolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(jedisPoolConfig, host, port);
        jedis = jedisPool.getResource();
        try{
            jedis.select(0);
        } catch (Exception e){
            logger.error(e);
        }
    }
    protected RedisInstance(String host){
        jedisPoolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(jedisPoolConfig, host);
        jedis = jedisPool.getResource();
        try{
            jedis.select(0);
        } catch (Exception e){
            logger.error(e);
        }
    }

    public Jedis getResource(int index) {
        jedis.select(index);
        return jedis;
    }
}
