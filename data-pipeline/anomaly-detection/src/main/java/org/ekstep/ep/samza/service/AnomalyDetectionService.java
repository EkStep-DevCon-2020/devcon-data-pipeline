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
      if ("ERROR".equals(event.mid())) {
        if (null != event.objectID() && whitelistedResources.contains(event.objectType())) {
          String value = redisConnection.get(event.objectID());
          Long ttl = redisConnection.ttl(event.objectID());
          int newValue = 1;
          if (null != value) {
            newValue = Integer.parseInt(value) + 1;
            redisConnection.setex(event.objectID(), ttl.intValue(), newValue + "");
          } else {
            redisConnection.setex(event.objectID(), 600, newValue + "");
          }
          if (newValue == 3) {
            flagContent(event.objectID());
            redisConnection.del(event.objectID());
          }
        }
      }

    } catch (JedisException e) {

    } catch (JsonSyntaxException e) {

    }
  }

  private void flagContent(String contentId) {

    HttpResponse<JsonNode> response = Unirest
        .get("https://devcon.sunbirded.org/content/v3/read/" + contentId + "?fields=versionKey")
        .header("accept", "application/json")
        .header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIyZWU4YTgxNDNiZWE0NDU4YjQxMjcyNTU5ZDBhNTczMiJ9.7m4mIUaiPwh_o9cvJuyZuGrOdkfh0Nm0E_25Cl21kxE")
        .asJson();

    String versionKey = response.getBody().getObject().getJSONObject("result").getJSONObject("content")
        .getString("versionKey");

    response = Unirest.post("https://devcon.sunbirded.org/apicontent/v1/flag/" + contentId)
        .header("accept", "application/json")
        .header("Authorization",
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiIyZWU4YTgxNDNiZWE0NDU4YjQxMjcyNTU5ZDBhNTczMiJ9.7m4mIUaiPwh_o9cvJuyZuGrOdkfh0Nm0E_25Cl21kxE")
        .body(
            "{\"request\":{\"flagReasons\":[\"Content play failed too many times\"],\"flaggedBy\":\"AnomalyDetectionJob\",\"versionKey\":\""
                + versionKey + "\",\"flags\":[\"NotWorking\"]}}")
        .asJson();
  }
}
