package org.ekstep.ep.samza.reader;


import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.ekstep.ep.samza.core.Logger;

import static java.text.MessageFormat.format;

public class Telemetry {
    private Map<String, Object> map;
    private Logger logger = new Logger(this.getClass());

    public Telemetry(Map<String, Object> map) {
        this.map = map;
    }

    public boolean add(String keyPath, Object value) {
        try {
            ParentType lastParent = lastParentMap(map, keyPath);
            lastParent.addChild(value);
            return true;
        } catch (Exception e) {
            logger.error("", format("Couldn't add value:{0} at  key: {0} for event:{1}", value, keyPath, map), e);
        }
        return false;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public <T> NullableValue<T> read(String keyPath) {
        try {
            ParentType parentMap = lastParentMap(map, keyPath);
            return new NullableValue<T>(parentMap.<T>readChild());
        } catch (Exception e) {
            logger.error("", format("Couldn't get key: {0} from event:{1}", keyPath, map), e);
            return new NullableValue<T>(null);
        }
    }

    public <T> NullableValue<T> readOrDefault(String keyPath, T defaultValue) {
        return read(keyPath).isNull() ? new NullableValue<T>(defaultValue) : read(keyPath);
    }

    public <T> T mustReadValue(String keyPath) throws TelemetryReaderException {
        NullableValue<T> val = read(keyPath);
        if (val.isNull()) {
            NullableValue<String> eid = read("eid");
            String message = keyPath + " is not available in the event";
            if (!eid.isNull()) {
                message = keyPath + " is not available in " + eid.value();
            }
            throw new TelemetryReaderException(message);
        }

        return val.value();
    }

    private ParentType lastParentMap(Map<String, Object> map, String keyPath) {
        Object parent = map;
        String[] keys = keyPath.split("\\.");
        int lastIndex = keys.length - 1;
        if (keys.length > 1) {
            for (int i = 0; i < lastIndex && parent != null; i++) {
                Object o;
                if (parent instanceof Map) {
                    o = new ParentMap((Map<String, Object>) parent, keys[i]).readChild();
                } else if (parent instanceof List) {
                    o = new ParentListOfMap((List<Map<String, Object>>) parent, keys[i]).readChild();
                } else {
                    o = new NullParent(parent, keys[i]).readChild();
                }
                parent = o;
            }
        }
        String lastKeyInPath = keys[lastIndex];
        if (parent instanceof Map) {
            return new ParentMap((Map<String, Object>) parent, lastKeyInPath);
        } else if (parent instanceof List) {
            return new ParentListOfMap((List<Map<String, Object>>) parent, lastKeyInPath);
        } else {
            return new NullParent(parent, lastKeyInPath);
        }
    }

    @Override
    public String toString() {
        return "Telemetry{" +
                "map=" + map +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Telemetry telemetry = (Telemetry) o;

        return map != null ? map.equals(telemetry.map) : telemetry.map == null;
    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }

    public String getUID() {
        return this.<String>read("uid").value();
    }

    public String id() {
        return this.<String>read("metadata.checksum").value();
    }

    public Date getTime() throws ParseException {
        return getTime("ts", "yyyy-MM-dd'T'HH:mm:ss");
    }

    public Date getTime(String key, String timePattern) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timePattern);
        String ts = this.<String>read(key).value();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        return simpleDateFormat.parse(ts);
    }


    public <T> void addFieldIfAbsent(String fieldName, T value) {
        if (read(fieldName).isNull()) {
            add(fieldName, value);
        }
    }

    public String getChannel() {
        return this.<String>read("channel").value();
    }

    public long getEts() throws TelemetryReaderException {
        double ets = this.<Double>mustReadValue("ets");
        return (long) ets;
    }

    public String getAtTimestamp(){
        Object timestamp = this.read("@timestamp").value();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if ( timestamp instanceof Number){
            Date date = new Date(((Number) timestamp).longValue());
            return simpleDateFormat.format(date);
        } else {
            return this.<String>read("@timestamp").value();
        }
    }
    
    public String getSyncts(){
        Object timestamp = this.read("syncts").value();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if ( timestamp instanceof Number){
            Date date = new Date(((Number) timestamp).longValue());
            return simpleDateFormat.format(date);
        } else {
            return this.<String>read("syncts").value();
        }
    }

    public Map<String, Object> getEdata(){
    	return this.<Map<String, Object>>read("edata.eks").value();
    }
    public String getJson(){
        return new Gson().toJson(map);
    }
}
