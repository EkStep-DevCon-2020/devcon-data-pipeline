package org.ekstep.ep.samza.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.ep.samza.core.Logger;
import org.ekstep.ep.samza.domain.Event;
import org.ekstep.ep.samza.task.AnomalyDetectionConfig;
import org.ekstep.ep.samza.task.AnomalyDetectionSink;
import org.ekstep.ep.samza.task.AnomalyDetectionSource;

import org.joda.time.DateTime;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import org.ekstep.ep.samza.util.RedisConnect;

public class AnomalyDetectionService {
  private static Logger LOGGER = new Logger(AnomalyDetectionService.class);
  private final AnomalyDetectionConfig config;
  private RedisConnect redisConnect;
  private Jedis redisConnection;
  private List<String> whitelistedResources = new ArrayList<>();

  private Gson gson = new Gson();

  public AnomalyDetectionService(RedisConnect redisConnect, AnomalyDetectionConfig config) {
    this.redisConnect = redisConnect;
    this.redisConnection = this.redisConnect.getConnection();
    this.config = config;
    whitelistedResources.add("Resource");
    whitelistedResources.add("Content");
  }

  public void process(AnomalyDetectionSource source, AnomalyDetectionSink sink) throws Exception {
    Event event = null;

    try {
      event = source.getEvent();
      if ("ERROR".equals(event.eid())) {
        String objectId = event.objectID();

        if (null != objectId && whitelistedResources.contains(event.objectType()) && config.whitelistedObjectIds().contains(objectId)) {
          String value = redisConnection.get(objectId);
          Long ttl = redisConnection.ttl(objectId);
          int newValue = 1;
          if (null != value) {
            newValue = Integer.parseInt(value) + 1;
            redisConnection.setex(objectId, ttl.intValue(), String.valueOf(newValue));
          } else {
            redisConnection.setex(objectId, config.expirySeconds(), String.valueOf(newValue));
          }
          LOGGER.info("anomaly_detection", "Received error event for Resource " + objectId);

          if (newValue == 3) {
            LOGGER.info("anomaly_detection", "Flagging content " + objectId + "since the error exceeded 3 times");
            String response = flagContent(objectId);
            LOGGER.info("anomaly_detection_flag_content", response);
            redisConnection.del(objectId);

            Map<String, Object> exitEventMap = new HashMap<>();
            exitEventMap.put("eid", "DC_ANOMALY");
            exitEventMap.put("profileId", "profileId");
            exitEventMap.put("stallId", "STA5");
            exitEventMap.put("stallName", "Analytics");
            exitEventMap.put("ideaId", "IDE37");
            exitEventMap.put("ideaName", "Error Dashboards and Devcon Dashboard");
            exitEventMap.put("ets", new DateTime().getMillis());
            exitEventMap.put("contentId", objectId);
            sink.toSuccessTopic(event.did(), gson.toJson(exitEventMap));

          }
        }
      }

    } catch (JedisException e) {
      LOGGER.error("anomaly_detection", "JedisException: ", e);
    } catch (JsonSyntaxException e) {
      LOGGER.error("anomaly_detection", "JsonSyntaxException: ", e);
    }
  }

  private void generateTelemetryEvent(String profileId, String contentId) {


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
