package org.ekstep.ep.samza.domain;


import java.util.Map;

import org.ekstep.ep.samza.reader.NullableValue;
import org.ekstep.ep.samza.reader.Telemetry;

import com.google.gson.Gson;

public class Event {
    private final Telemetry telemetry;

    public Event(Map<String, Object> map) {
        this.telemetry = new Telemetry(map);
    }

    public Map<String, Object> getMap() {
        return telemetry.getMap();
    }

    public String getJson() {
        Gson gson = new Gson();
        String json = gson.toJson(getMap());
        return json;
    }

    public String getChecksum() {

        String checksum = id();
        if (checksum != null)
            return checksum;

        return mid();
    }

    public String id() {
        NullableValue<String> checksum = telemetry.read("metadata.checksum");
        return checksum.value();
    }

    public String mid() {
        NullableValue<String> checksum = telemetry.read("mid");
        return checksum.value();
    }
    
    public String did() {
        NullableValue<String> checksum = telemetry.read("context.did");
        return checksum.value();
    }

    public String producerId() {
        NullableValue<String> producerId = telemetry.read("context.pdata.id");
        return producerId.value();
    }

    public final String producerPid() {
        NullableValue<String> producerPid = telemetry.read("context.pdata.pid");
        return producerPid.value();
    }

    public String eid() {
        NullableValue<String> eid = telemetry.read("eid");
        return eid.value();
    }

    public String objectID() {
        if (objectFieldsPresent()) {
            return telemetry.<String>read("object.id").value();
        }
        else return null;
    }

    public String objectType() {
        if (objectFieldsPresent()) {
            return telemetry.<String>read("object.type").value();
        }
        else return null;
    }

    public boolean objectFieldsPresent() {
        String objectId = telemetry.<String>read("object.id").value();
        String objectType = telemetry.<String>read("object.type").value();
        return objectId != null && objectType != null && !objectId.isEmpty() && !objectType.isEmpty();
    }

    public void addEventType() {
        telemetry.add("type", "events");
    }

    @Override
    public String toString() {
        return "Event{" +
                "telemetry=" + telemetry +
                '}';
    }

}

