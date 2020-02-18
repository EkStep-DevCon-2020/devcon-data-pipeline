package org.ekstep.ep.samza.service;

import com.google.gson.Gson;
import org.ekstep.ep.samza.core.Logger;
import org.ekstep.ep.samza.domain.Event;
import org.ekstep.ep.samza.task.DevconDenormConfig;
import org.ekstep.ep.samza.task.DevconDenormSink;
import org.ekstep.ep.samza.task.DevconDenormSource;
import org.ekstep.ep.samza.util.UserDataCache;
import org.ekstep.ep.samza.util.UserKey;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DevconDenormService {

    private static Logger LOGGER = new Logger(DevconDenormService.class);
    private final DevconDenormConfig config;
    private UserDataCache userDataCache;
    private Map<UserKey, Long> visitorStallEntry = new HashMap<>();
    private Gson gson = new Gson();

    public DevconDenormService(DevconDenormConfig config, UserDataCache userDataCache) {
        this.config = config;
        this.userDataCache = userDataCache;
    }

    public void process(DevconDenormSource source, DevconDenormSink sink) {
        Event event = source.getEvent();
        String eid = event.eid();
        String profileId = event.profileId();
        String stallId = event.stallId();
        String ideaId = event.ideaId();
        try {

            Map<String, Object> userData = userDataCache.getUserData(profileId);
            event.addUserData(userData);

            if ("DC_VISIT".equalsIgnoreCase(eid)) {
                UserKey userKey = new UserKey(profileId, stallId, ideaId);
                if (visitorStallEntry.containsKey(userKey)) {
                    Long lastIdeaEntryEts = visitorStallEntry.get(userKey);
                    Long currentIdeaEntryEts = event.ets();
                    Long timeSpent = currentIdeaEntryEts - lastIdeaEntryEts; // In milliseconds
                    Map<String, Object> exitEventMap = new HashMap<>();
                    exitEventMap.put("eid", "DC_EXIT");
                    exitEventMap.put("profileId", profileId);
                    exitEventMap.put("stallId", stallId);
                    exitEventMap.put("ideaId", ideaId);
                    exitEventMap.put("ets", new DateTime().getMillis());
                    exitEventMap.put("profileName", userData.get("name"));
                    Map<String, Object> edata = new HashMap<>();
                    edata.put("duration", timeSpent/1000);
                    exitEventMap.put("edata", edata);
                    sink.toSuccessTopic(event.did(), gson.toJson(exitEventMap));
                    visitorStallEntry.put(userKey, event.ets());
                } else {
                    visitorStallEntry.put(userKey, event.ets());
                }
            }

        } catch (IOException ex) {
            LOGGER.error("DevconDenorm", "Exception when denormalizing devcon events" + ex.getMessage());
        }
        sink.toSuccessTopic(event);
    }


}