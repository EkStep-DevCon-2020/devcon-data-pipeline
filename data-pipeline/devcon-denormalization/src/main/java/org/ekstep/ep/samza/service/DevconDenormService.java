package org.ekstep.ep.samza.service;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.ekstep.ep.samza.core.Logger;
import org.ekstep.ep.samza.domain.Event;
import org.ekstep.ep.samza.task.DevconDenormConfig;
import org.ekstep.ep.samza.task.DevconDenormSink;
import org.ekstep.ep.samza.task.DevconDenormSource;
import org.ekstep.ep.samza.util.StallDataCache;
import org.ekstep.ep.samza.util.UserDataCache;
import org.ekstep.ep.samza.util.UserKey;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DevconDenormService {

    private static Logger LOGGER = new Logger(DevconDenormService.class);
    private final DevconDenormConfig config;
    private UserDataCache userDataCache;
    private StallDataCache stallDataCache;
    private Map<String, String> visitorStallEntry = new HashMap<>();
    private Gson gson = new Gson();
    private Type mapType = new TypeToken<Map<String, Object>>() {}.getType();

    public DevconDenormService(DevconDenormConfig config, UserDataCache userDataCache, StallDataCache stallDataCache) {
        this.config = config;
        this.userDataCache = userDataCache;
        this.stallDataCache = stallDataCache;
    }

    public Long lastIdeaEts(Map<String, Object> data) {
        Object ets = data.get("ets");
        if (ets.getClass().equals(Double.class)) {
            return ((Double) ets).longValue();
        }
        return ((Long) ets);
    }

    public void process(DevconDenormSource source, DevconDenormSink sink) {
        Event event = source.getEvent();
        String eid = event.eid();
        String profileId = event.profileId();
        String stallId = event.stallId();
        String ideaId = event.ideaId();
        try {
            if (null != stallId && !stallId.isEmpty()) {
                String stallName = stallDataCache.getStallName(stallId);
                LOGGER.info("Denorm", String.format("stallId %s :: denorm stallName %s", stallId, stallName));
                event.addStallData(stallName);
            }

            if (null != ideaId && !ideaId.isEmpty()) {
                String ideaName = stallDataCache.getIdeaName(ideaId);
                LOGGER.info("Denorm", String.format("ideaId %s :: denorm ideaName %s", ideaId, ideaName));
                event.addIdeaData(ideaName);
            }

            if (null != profileId && !profileId.isEmpty()) {
                Map<String, Object> userData = userDataCache.getUserData(profileId);
                event.addUserData(userData);

                if ("DC_VISIT".equalsIgnoreCase(eid)) {
                    // UserKey userKey = new UserKey(profileId, stallId, ideaId);
                    if (visitorStallEntry.containsKey(profileId)) {
                        Map<String, Object> data = gson.fromJson(visitorStallEntry.get(profileId), mapType);
                        Long lastIdeaEntryEts = lastIdeaEts(data);
                        String lastStallId = (String) data.get("stallId");
                        String lastIdeaId = (String) data.get("ideaId");

                        Long currentIdeaEntryEts = event.ets();
                        LOGGER.info("DevconDenorm", "currentIdeaEntryEts: " + currentIdeaEntryEts + " lastIdeaEntryEts: " + lastIdeaEntryEts);
                        Long timeSpent = (currentIdeaEntryEts - lastIdeaEntryEts)/1000; // In seconds

                        Map<String, Object> exitEventMap = new HashMap<>();
                        exitEventMap.put("eid", "DC_EXIT");
                        exitEventMap.put("profileId", profileId);
                        exitEventMap.put("stallId", lastStallId);
                        exitEventMap.put("ideaId", lastIdeaId);
                        exitEventMap.put("ets", new DateTime().getMillis());
                        exitEventMap.put("profileName", userData.get("name"));
                        Map<String, Object> edata = new HashMap<>();
                        edata.put("duration", timeSpent);
                        exitEventMap.put("edata", edata);
                        sink.toSuccessTopic(event.did(), gson.toJson(exitEventMap));

                        Map<String, Object> currentStallData = new HashMap<>();
                        currentStallData.put("ets", event.ets());
                        currentStallData.put("stallId", stallId);
                        currentStallData.put("ideaId", ideaId);
                        visitorStallEntry.put(profileId, gson.toJson(currentStallData));
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("stallId", stallId);
                        data.put("ideaId", ideaId);
                        data.put("ets", event.ets());
                        visitorStallEntry.put(profileId, gson.toJson(data));
                    }
                }
            }

        }
        catch(IOException ex){
                LOGGER.error("DevconDenorm", "Exception when denormalizing devcon events" + ex.getMessage());
        }
        sink.toSuccessTopic(event);
    }


}