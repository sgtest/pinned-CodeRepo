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
//ok  放消息状态  成功、刷盘超时、同步从节点超时、从节点不可用、创建mf失败
//    消息不合法、属性大小越界、os页缓存繁忙、未知错误、消费队列数量溢出
public enum PutMessageStatus {
    PUT_OK,
    FLUSH_DISK_TIMEOUT,
    FLUSH_SLAVE_TIMEOUT,
    SLAVE_NOT_AVAILABLE,
    SERVICE_NOT_AVAILABLE,
    CREATE_MAPEDFILE_FAILED,
    MESSAGE_ILLEGAL,
    PROPERTIES_SIZE_EXCEEDED,
    OS_PAGECACHE_BUSY,
    UNKNOWN_ERROR,
    LMQ_CONSUME_QUEUE_NUM_EXCEEDED,
}
