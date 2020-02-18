package org.ekstep.ep.samza.util;

public class Visitor {

    private String name;
    private String osid;
    private String code;
    private String osCreatedAt;
    private String osUpdatedAt;

    public Visitor() {}

    public Visitor(String osid) {
        this.osid = osid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOsid() {
        return osid;
    }

    public void setOsid(String osid) {
        this.osid = osid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOsCreatedAt() {
        return osCreatedAt;
    }

    public void setOsCreatedAt(String osCreatedAt) {
        this.osCreatedAt = osCreatedAt;
    }

    public String getOsUpdatedAt() {
        return osUpdatedAt;
    }

    public void setOsUpdatedAt(String osUpdatedAt) {
        this.osUpdatedAt = osUpdatedAt;
    }
}
