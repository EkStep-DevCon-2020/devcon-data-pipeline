package org.ekstep.ep.samza.task;

import org.apache.samza.task.MessageCollector;
import org.ekstep.ep.samza.core.BaseSink;
import org.ekstep.ep.samza.core.JobMetrics;
import org.ekstep.ep.samza.domain.Event;

public class DevconDenormSink extends BaseSink {

	private DevconDenormConfig config;

	public DevconDenormSink(MessageCollector collector, JobMetrics metrics, DevconDenormConfig config) {
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

	public void toFailedTopic(Event event, String failedMessage) {
		event.markFailure(failedMessage, config);
		toTopic(config.failedTopic(), event.did(), event.getJson());
		metrics.incFailedCounter();
	}

	public void toMalformedTopic(String message) {
		toTopic(config.malformedTopic(), null, message);
		metrics.incErrorCounter();
	}

	public void incrementSkippedCount(Event event) {
		metrics.incSkippedCounter();
	}

	public void incExpiredEventCount() {
		metrics.incExpiredEventCount();
	}

}
