package org.ekstep.ep.samza.task;


import org.apache.samza.config.Config;

import java.util.ArrayList;
import java.util.List;

public class DevconDenormConfig {

    private final String JOB_NAME = "DevconDenorm";

    private String successTopic;
    private String failedTopic;
    private String malformedTopic;

    public DevconDenormConfig(Config config) {
        successTopic = config.get("output.success.topic.name", "telemetry.denorm");
        failedTopic = config.get("output.failed.topic.name", "telemetry.failed");
        malformedTopic = config.get("output.malformed.topic.name", "telemetry.malformed");
    }

    public String successTopic() {
        return successTopic;
    }

    public String failedTopic() {
        return failedTopic;
    }

    public String malformedTopic() {
        return malformedTopic;
    }

    public String jobName() {
        return JOB_NAME;
    }


}