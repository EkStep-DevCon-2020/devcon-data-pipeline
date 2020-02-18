package org.ekstep.ep.samza.service;

import org.ekstep.ep.samza.core.Logger;
import org.ekstep.ep.samza.domain.Event;
import org.ekstep.ep.samza.task.DevconDenormConfig;
import org.ekstep.ep.samza.task.DevconDenormSink;
import org.ekstep.ep.samza.task.DevconDenormSource;
import org.ekstep.ep.samza.util.UserDataCache;

import java.io.IOException;
import java.util.Map;

public class DevconDenormService {

    private static Logger LOGGER = new Logger(DevconDenormService.class);
    private final DevconDenormConfig config;
    private UserDataCache userDataCache;

    public DevconDenormService(DevconDenormConfig config, UserDataCache userDataCache) {
        this.config = config;
        this.userDataCache = userDataCache;
    }

    public void process(DevconDenormSource source, DevconDenormSink sink) {
        Event event = source.getEvent();
        try {
            Map<String, Object> userData = userDataCache.getUserData(event.profileId());
            event.addUserData(userData);
        } catch (IOException ex) {
            LOGGER.error("DevconDenorm", "Exception when denormalizing devcon events" + ex.getMessage());
        }
        sink.toSuccessTopic(event);
    }


}