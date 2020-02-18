package org.ekstep.ep.task;

import com.fiftyonred.mock_jedis.MockJedis;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.samza.Partition;
import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.ep.fixture.EventFixture;
import org.ekstep.ep.samza.core.JobMetrics;
import org.ekstep.ep.samza.task.DevconDenormTask;
import org.ekstep.ep.samza.util.RedisConnect;
import org.ekstep.ep.samza.util.UserDataCache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Type;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

public class DevconDenormTaskTest {

    private static final String SUCCESS_TOPIC = "dc.events.denorm";
    private static final String FAILED_TOPIC = "dc.events.failed";
    private static final String MALFORMED_TOPIC = "dc.events.malformed";
    private static final Integer ignorePeriodInMonths = 12;

    private MessageCollector collectorMock;
    private TaskContext contextMock;
    private MetricsRegistry metricsRegistry;
    private Counter counter;
    private TaskCoordinator coordinatorMock;
    private IncomingMessageEnvelope envelopeMock;
    private Config configMock;
    private UserDataCache userCacheMock;
    private DevconDenormTask deNormalizationTask;

    private JobMetrics jobMetrics;
    private Jedis jedisMock = new MockJedis("test");
    private Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        collectorMock = mock(MessageCollector.class);
        contextMock = mock(TaskContext.class);
        metricsRegistry = mock(MetricsRegistry.class);
        counter = mock(Counter.class);
        coordinatorMock = mock(TaskCoordinator.class);
        envelopeMock = mock(IncomingMessageEnvelope.class);
        configMock = mock(Config.class);
        userCacheMock = mock(UserDataCache.class);
        RedisConnect redisConnectMock = mock(RedisConnect.class);
        jobMetrics = mock(JobMetrics.class);


        stub(configMock.get("output.success.topic.name", SUCCESS_TOPIC)).toReturn(SUCCESS_TOPIC);
        stub(configMock.get("output.failed.topic.name", FAILED_TOPIC)).toReturn(FAILED_TOPIC);
        stub(configMock.get("output.malformed.topic.name", MALFORMED_TOPIC)).toReturn(MALFORMED_TOPIC);
        stub(configMock.get("devcon.registry.host", "http://test-domain")).toReturn("http://test-domain");

        stub(configMock.getInt("redis.userDB.index", 12)).toReturn(12);
        stub(redisConnectMock.getConnection(12)).toReturn(jedisMock);

        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);
        stub(envelopeMock.getOffset()).toReturn("2");
        stub(envelopeMock.getSystemStreamPartition())
                .toReturn(new SystemStreamPartition("kafka", "dc.events.raw", new Partition(1)));

        // UserDataCache userCacheMock = new UserDataCache(configMock, jobMetrics, redisConnectMock);
        deNormalizationTask = new DevconDenormTask(configMock, contextMock, jobMetrics, userCacheMock, redisConnectMock);
    }


    @Test
    public void shouldSendEventsToSuccessTopicUserName() throws Exception {
        stub(envelopeMock.getMessage()).toReturn(EventFixture.DC_VISIT_EVENT);
        // jedisMock.set("1-42e4d777-ea7b-4f29-abd6-115e236028c2","{\"grade\":[4,5],\"district\":\"Bengaluru\",\"state\":\"Karnataka\"}");
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", "visitor-1"); userData.put("osid", "1-42e4d777-ea7b-4f29-abd6-115e236028c2"); userData.put("code", "visitor-1-code");
        userData.put("osUpdatedAt", "2020-02-18T11:49:37.930Z"); userData.put("osCreatedAt", "2020-02-18T11:49:37.930Z");

        stub(userCacheMock.getUserData("1-42e4d777-ea7b-4f29-abd6-115e236028c2")).toReturn(userData);
        deNormalizationTask.process(envelopeMock, collectorMock, coordinatorMock);
        Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
        verify(collectorMock).send(argThat(new ArgumentMatcher<OutgoingMessageEnvelope>() {
            @Override
            public boolean matches(Object o) {
                OutgoingMessageEnvelope outgoingMessageEnvelope = (OutgoingMessageEnvelope) o;
                String outputMessage = (String) outgoingMessageEnvelope.getMessage();
                Map<String, Object> outputEvent = new Gson().fromJson(outputMessage, mapType);
                String profileId = outputEvent.get("profileId").toString();
                String profileName = outputEvent.get("profileName").toString();
                assertEquals("visitor-1", profileName);
                assertEquals("1-42e4d777-ea7b-4f29-abd6-115e236028c2", profileId);
                return true;
            }
        }));
    }

}
