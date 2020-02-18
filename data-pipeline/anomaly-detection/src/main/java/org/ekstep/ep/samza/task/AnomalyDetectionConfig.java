package org.ekstep.ep.samza.task;


import java.util.List;

import org.apache.samza.config.Config;

public class AnomalyDetectionConfig {

    private final String JOB_NAME = "AnomalyDetection";
    private final String metricsTopic;
    private final int dupStore;
    private int expirySeconds;
    private List<String> includedProducerIds;

    public AnomalyDetectionConfig(Config config) {
        dupStore = config.getInt("redis.database.anomalystore.id", 13);
        expirySeconds = config.getInt("redis.database.key.expiry.seconds", 600);
        metricsTopic = config.get("output.metrics.topic.name", "pipeline_metrics");
    }

    public String metricsTopic() {
        return metricsTopic;
    }

    public String jobName() {
        return JOB_NAME;
    }

    public int dupStore() {
        return dupStore;
    }

    public int expirySeconds() {
        return expirySeconds;
    }

    public List<String> inclusiveProducerIds() {
        return includedProducerIds;
    }

}