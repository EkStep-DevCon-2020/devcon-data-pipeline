package org.ekstep.ep.samza.util;

import org.joda.time.DateTime;

import java.util.Map;

public class RegistryRequest {

    private String ver;
    private String ets;
    private Map<String, Object> params;
    private VisitorElement request;

    public RegistryRequest(VisitorElement request) {
        this.ver = "1.0";
        this.ets = String.valueOf(new DateTime().getMillis());
        this.request = request;
    }

    public String getVer() {
        return ver;
    }

    public String getEts() {
        return ets;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public VisitorElement getRequest() {
        return request;
    }
}
