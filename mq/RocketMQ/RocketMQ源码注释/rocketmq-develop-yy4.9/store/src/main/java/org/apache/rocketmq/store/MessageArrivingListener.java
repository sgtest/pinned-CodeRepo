/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.store;

import java.util.Map;
//ok  消息到达监听器接口
public interface MessageArrivingListener {

    /**
     * Notify that a new message arrives in a consume queue  通知一个新消息到达消费队列
     * @param topic topic name
     * @param queueId consume queue id
     * @param logicOffset consume queue offset
     * @param tagsCode message tags hash code
     * @param msgStoreTime message store time
     * @param filterBitMap message bloom filter
     * @param properties message properties
     */
    void arriving(String topic, int queueId, long logicOffset, long tagsCode,
        long msgStoreTime, byte[] filterBitMap, Map<String, String> properties);
}
