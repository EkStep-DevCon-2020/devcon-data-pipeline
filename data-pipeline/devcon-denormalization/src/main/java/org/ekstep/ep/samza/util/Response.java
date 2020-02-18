package org.ekstep.ep.samza.util;

import java.util.Map;

public class Response {
    private String id;
    private String ver;
    private String ets;
    private Map<String, Object> params;
    private VisitorElement result;

    public VisitorElement getResult() {
        return result;
    }
}
