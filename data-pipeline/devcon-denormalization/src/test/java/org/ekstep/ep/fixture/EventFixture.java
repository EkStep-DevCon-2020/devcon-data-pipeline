package org.ekstep.ep.fixture;

import org.joda.time.DateTime;

public class EventFixture {
    public static Long current_ets = new DateTime().getMillis();

    public static final String DC_VISIT_EVENT = "{\n" +
            "  \"eid\": \"DC_VISIT\",\n" +
            "  \"mid\": \"gsyhbvtfd99a6c2723d67aaa649190ba\",\n" +
            "  \"ets\": 1547613077259,\n" +
            "  \"did\": \"8ceeb01fd99a6c2723d67aaa649190ba\",\n" +
            "  \"profileId\": \"1-42e4d777-ea7b-4f29-abd6-115e236028c2\",\n" +
            "  \"stallId\": \"STA2\",\n" +
            "  \"ideaId\": \"IDE1\",\n" +
            "  \"edata\": {\n" +
            "    \n" +
            "  }\n" +
            "}";
}
