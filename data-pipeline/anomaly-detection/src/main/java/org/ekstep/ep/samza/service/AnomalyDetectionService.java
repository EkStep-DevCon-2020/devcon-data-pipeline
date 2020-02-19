package org.ekstep.ep.samza.service;

import com.google.gson.JsonSyntaxException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.List;

import org.ekstep.ep.samza.core.Logger;
import org.ekstep.ep.samza.domain.Event;
import org.ekstep.ep.samza.task.AnomalyDetectionConfig;
import org.ekstep.ep.samza.task.AnomalyDetectionSource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import org.ekstep.ep.samza.util.RedisConnect;

public class AnomalyDetectionService {
  private static Logger LOGGER = new Logger(AnomalyDetectionService.class);
  private final AnomalyDetectionConfig config;
  private RedisConnect redisConnect;
  private Jedis redisConnection;
  private List<String> whitelistedResources = new ArrayList<String>();

  public AnomalyDetectionService(RedisConnect redisConnect, AnomalyDetectionConfig config) {
    this.redisConnect = redisConnect;
    this.redisConnection = this.redisConnect.getConnection();
    this.config = config;
    whitelistedResources.add("Resource");
  }

  public void process(AnomalyDetectionSource source) throws Exception {
    Event event = null;

    try {
      event = source.getEvent();
      if ("ERROR".equals(event.eid())) {
        if (null != event.objectID() && whitelistedResources.contains(event.objectType())) {
          String value = redisConnection.get(event.objectID());
          Long ttl = redisConnection.ttl(event.objectID());
          int newValue = 1;
          if (null != value) {
            newValue = Integer.parseInt(value) + 1;
            redisConnection.setex(event.objectID(), ttl.intValue(), String.valueOf(newValue));
          } else {
            redisConnection.setex(event.objectID(), config.expirySeconds(), String.valueOf(newValue));
          }
          LOGGER.info("anomaly_detection", "Received error event for Resource " + event.objectID());

          if (newValue == 3) {
            LOGGER.info("anomaly_detection", "Flagging content " + event.objectID() + "since the error exceeded 3 times");
            String response = flagContent(event.objectID());
            LOGGER.info("anomaly_detection_flag_content", response);
            redisConnection.del(event.objectID());
          }
        }
      }

    } catch (JedisException e) {
      LOGGER.error("anomaly_detection", "JedisException: ", e);
    } catch (JsonSyntaxException e) {
      LOGGER.error("anomaly_detection", "JsonSyntaxException: ", e);
    }
  }

  private String flagContent(String contentId) {

    HttpResponse<JsonNode> response = Unirest
        .get("https://devcon.sunbirded.org/api/private/content/v3/read/" + contentId + "?fields=versionKey")
        .header("accept", "application/json")
        .header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIyZWU4YTgxNDNiZWE0NDU4YjQxMjcyNTU5ZDBhNTczMiJ9.7m4mIUaiPwh_o9cvJuyZuGrOdkfh0Nm0E_25Cl21kxE")
        .asJson();

    String versionKey = response.getBody().getObject().getJSONObject("result").getJSONObject("content")
        .getString("versionKey");

    response = Unirest.post("https://devcon.sunbirded.org/api/private/content/v3/flag/" + contentId)
        .header("accept", "application/json")
        .header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIyZWU4YTgxNDNiZWE0NDU4YjQxMjcyNTU5ZDBhNTczMiJ9.7m4mIUaiPwh_o9cvJuyZuGrOdkfh0Nm0E_25Cl21kxE")
        .body(
            "{\"request\":{\"flagReasons\":[\"Content play failed too many times\"],\"flaggedBy\":\"AnomalyDetectionJob\",\"versionKey\":\""
                + versionKey + "\"]}}")
        .asJson();
    return response.getBody().toString();
  }
}
