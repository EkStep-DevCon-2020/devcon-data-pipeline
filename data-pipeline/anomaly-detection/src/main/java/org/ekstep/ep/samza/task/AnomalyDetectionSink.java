package org.ekstep.ep.samza.task;

import org.apache.samza.task.MessageCollector;
import org.ekstep.ep.samza.core.BaseSink;
import org.ekstep.ep.samza.core.JobMetrics;
import org.ekstep.ep.samza.domain.Event;

public class AnomalyDetectionSink extends BaseSink {

    private AnomalyDetectionConfig config;

    public AnomalyDetectionSink(MessageCollector collector, JobMetrics metrics, AnomalyDetectionConfig config) {
        super(collector, metrics);
        this.config = config;
    }

    public void toSuccessTopic(Event event) {
        toTopic(config.successTopic(), event.did(), event.getJson());
        metrics.incSuccessCounter();
    }

    public void toSuccessTopic(String key, String eventJson) {
        toTopic(config.successTopic(), key, eventJson);
        metrics.incSuccessCounter();
    }

}
