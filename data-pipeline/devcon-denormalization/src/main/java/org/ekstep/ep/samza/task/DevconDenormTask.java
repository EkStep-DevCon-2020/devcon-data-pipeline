/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ekstep.ep.samza.task;

import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.ep.samza.core.JobMetrics;
import org.ekstep.ep.samza.service.DevconDenormService;
import org.ekstep.ep.samza.util.RedisConnect;
import org.ekstep.ep.samza.util.UserDataCache;

public class DevconDenormTask extends BaseSamzaTask {

    private DevconDenormConfig config;
    private UserDataCache userCache;
    private JobMetrics metrics;
    private RedisConnect redisConnect;
    private DevconDenormService service;

    public DevconDenormTask(Config config, TaskContext context, JobMetrics metrics, UserDataCache userCache, RedisConnect redisConnect) {
        init(config, context, metrics, userCache, redisConnect);
    }

    public DevconDenormTask() {

    }

    @Override
    public void init(Config config, TaskContext context) {
        init(config, context, metrics, userCache, redisConnect);
    }


    public void init(Config config, TaskContext context, JobMetrics jobMetrics, UserDataCache userCache, RedisConnect redisConnect) {

        this.config = new DevconDenormConfig(config);
        this.metrics = jobMetrics == null ? new JobMetrics(context, this.config.jobName()) : jobMetrics;
        this.userCache = userCache == null ? new UserDataCache(config, metrics, redisConnect) : userCache;
        service = new DevconDenormService(this.config, this.userCache);
        this.initTask(config, metrics);
    }

    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator taskCoordinator) {
        DevconDenormSource source = new DevconDenormSource(envelope);
        DevconDenormSink sink = new DevconDenormSink(collector, metrics, config);
        service.process(source, sink);
    }

}
