package org.ekstep.ep.samza.util;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.samza.config.Config;
import org.ekstep.ep.samza.core.JobMetrics;
import org.ekstep.ep.samza.core.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UserDataCache {

    private static Logger LOGGER = new Logger(UserDataCache.class);

    private RedisConnect redisPool;
    private Jedis redisConnection;
    private Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();
    private Gson gson = new Gson();
    private JobMetrics metrics;
    private int databaseIndex;

    private HttpClient httpClient;


    public UserDataCache(Config config, JobMetrics metrics, RedisConnect redisConnect) {
        this.metrics = metrics;
        this.databaseIndex = config.getInt("redis.userDB.index", 12);
        this.redisPool = null == redisConnect ? new RedisConnect(config) : redisConnect;
        this.redisConnection = this.redisPool.getConnection(databaseIndex);
        this.httpClient = new HttpClient(config.get("devcon.registry.host", "https://devcon.sunbirded.org"));
    }

    public Map<String, Object> getUserData(String userId) throws IOException {
        Map<String, Object> userDataMap;
        try {
            userDataMap = getUserDataFromCache(userId);
        } catch (JedisException ex) {
            redisPool.resetConnection();
            try (Jedis redisConn = redisPool.getConnection(databaseIndex)) {
                this.redisConnection = redisConn;
                userDataMap = getUserDataFromCache(userId);
            }
        }

        if (null != userDataMap && !userDataMap.isEmpty()) {
            userDataMap.keySet().retainAll(Collections.singletonList("visitorName"));
        } else {
            try {
                Visitor visitor = httpClient.getVisitorInfo(userId);
                String visitorData = gson.toJson(visitor);
                redisConnection.set(userId, gson.toJson(visitor));
                userDataMap = gson.fromJson(visitorData, mapType);
            }
            catch (Exception e) {
                LOGGER.error("DevconDenorm", "Exception when denormalizing visitor info" + e.getMessage());
            }
        }
        return userDataMap;
    }

    private Map<String, Object> getUserDataFromCache(String userId) {
        Map<String, Object> cacheData = new HashMap<>();
        String data = redisConnection.get(userId);
        if (data != null && !data.isEmpty()) {
            cacheData = gson.fromJson(data, mapType);
        }
        return cacheData;
    }
}
