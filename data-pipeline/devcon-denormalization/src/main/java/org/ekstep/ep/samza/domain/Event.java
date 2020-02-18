package org.ekstep.ep.samza.domain;


import org.ekstep.ep.samza.events.domain.Events;
import org.ekstep.ep.samza.task.DevconDenormConfig;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Event extends Events {
    private DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public Event(Map<String, Object> map) {
        super(map);
    }

    public String profileId() {
        return telemetry.<String>read("profileId").value();
    }

    public String stallId() {
        return telemetry.<String>read("stallId").value();
    }

    public String ideaId() {
        return telemetry.<String>read("ideaId").value();
    }

    public void addUserData(Map<String, Object> userData) {
        telemetry.add("profileName", userData.get("name"));
    }

    public void markFailure(String error, DevconDenormConfig config) {
        telemetry.addFieldIfAbsent("flags", new HashMap<String, Boolean>());
        telemetry.add("flags.denorm_processed", false);

        telemetry.addFieldIfAbsent("metadata", new HashMap<String, Object>());
        telemetry.add("metadata.denorm_error", error);
        telemetry.add("metadata.src", config.jobName());
    }


}

