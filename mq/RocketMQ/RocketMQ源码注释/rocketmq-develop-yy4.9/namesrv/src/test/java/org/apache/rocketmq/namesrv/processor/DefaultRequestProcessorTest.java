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
package org.apache.rocketmq.namesrv.processor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.common.namesrv.NamesrvConfig;
import org.apache.rocketmq.common.namesrv.RegisterBrokerResult;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.common.protocol.header.namesrv.DeleteKVConfigRequestHeader;
import org.apache.rocketmq.common.protocol.header.namesrv.GetKVConfigRequestHeader;
import org.apache.rocketmq.common.protocol.header.namesrv.GetKVConfigResponseHeader;
import org.apache.rocketmq.common.protocol.header.namesrv.PutKVConfigRequestHeader;
import org.apache.rocketmq.common.protocol.header.namesrv.RegisterBrokerRequestHeader;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.rocketmq.namesrv.routeinfo.RouteInfoManager;
import org.apache.rocketmq.remoting.exception.RemotingCommandException;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultRequestProcessorTest {
    private DefaultRequestProcessor defaultRequestProcessor;

    private NamesrvController namesrvController;

    private NamesrvConfig namesrvConfig;

    private NettyServerConfig nettyServerConfig;

    private RouteInfoManager routeInfoManager;

    private InternalLogger logger;

    @Before
    public void init() throws Exception {
        namesrvConfig = new NamesrvConfig();
        nettyServerConfig = new NettyServerConfig();
        routeInfoManager = new RouteInfoManager();

        namesrvController = new NamesrvController(namesrvConfig, nettyServerConfig);

        Field field = NamesrvController.class.getDeclaredField("routeInfoManager");
        field.setAccessible(true);
        field.set(namesrvController, routeInfoManager);
        defaultRequestProcessor = new DefaultRequestProcessor(namesrvController);

        registerRouteInfoManager();

        logger = mock(InternalLogger.class);
        setFinalStatic(DefaultRequestProcessor.class.getDeclaredField("log"), logger);
    }

    @Test
    public void testProcessRequest_PutKVConfig() throws RemotingCommandException {
        PutKVConfigRequestHeader header = new PutKVConfigRequestHeader();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.PUT_KV_CONFIG,
            header);
        request.addExtField("namespace", "namespace");
        request.addExtField("key", "key");
        request.addExtField("value", "value");

        RemotingCommand response = defaultRequestProcessor.processRequest(null, request);

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getRemark()).isNull();

        assertThat(namesrvController.getKvConfigManager().getKVConfig("namespace", "key"))
            .isEqualTo("value");
    }

    @Test
    public void testProcessRequest_GetKVConfigReturnNotNull() throws RemotingCommandException {
        namesrvController.getKvConfigManager().putKVConfig("namespace", "key", "value");

        GetKVConfigRequestHeader header = new GetKVConfigRequestHeader();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_KV_CONFIG,
            header);
        request.addExtField("namespace", "namespace");
        request.addExtField("key", "key");

        RemotingCommand response = defaultRequestProcessor.processRequest(null, request);

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getRemark()).isNull();

        GetKVConfigResponseHeader responseHeader = (GetKVConfigResponseHeader) response
            .readCustomHeader();

        assertThat(responseHeader.getValue()).isEqualTo("value");
    }

    @Test
    public void testProcessRequest_GetKVConfigReturnNull() throws RemotingCommandException {
        GetKVConfigRequestHeader header = new GetKVConfigRequestHeader();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_KV_CONFIG,
            header);
        request.addExtField("namespace", "namespace");
        request.addExtField("key", "key");

        RemotingCommand response = defaultRequestProcessor.processRequest(null, request);

        assertThat(response.getCode()).isEqualTo(ResponseCode.QUERY_NOT_FOUND);
        assertThat(response.getRemark()).isEqualTo("No config item, Namespace: namespace Key: key");

        GetKVConfigResponseHeader responseHeader = (GetKVConfigResponseHeader) response
            .readCustomHeader();

        assertThat(responseHeader.getValue()).isNull();
    }

    @Test
    public void testProcessRequest_DeleteKVConfig() throws RemotingCommandException {
        namesrvController.getKvConfigManager().putKVConfig("namespace", "key", "value");

        DeleteKVConfigRequestHeader header = new DeleteKVConfigRequestHeader();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.DELETE_KV_CONFIG,
            header);
        request.addExtField("namespace", "namespace");
        request.addExtField("key", "key");

        RemotingCommand response = defaultRequestProcessor.processRequest(null, request);

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getRemark()).isNull();

        assertThat(namesrvController.getKvConfigManager().getKVConfig("namespace", "key"))
            .isNull();
    }

    @Test
    public void testProcessRequest_RegisterBroker() throws RemotingCommandException,
        NoSuchFieldException, IllegalAccessException {
        RemotingCommand request = genSampleRegisterCmd(true);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);

        RemotingCommand response = defaultRequestProcessor.processRequest(ctx, request);

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getRemark()).isNull();

        RouteInfoManager routes = namesrvController.getRouteInfoManager();
        Field brokerAddrTable = RouteInfoManager.class.getDeclaredField("brokerAddrTable");
        brokerAddrTable.setAccessible(true);

        BrokerData broker = new BrokerData();
        broker.setBrokerName("broker");
        broker.setBrokerAddrs((HashMap) Maps.newHashMap(new Long(2333), "10.10.1.1"));

        assertThat((Map) brokerAddrTable.get(routes))
            .contains(new HashMap.SimpleEntry("broker", broker));
    }

    @Test
    public void testProcessRequest_RegisterBrokerWithFilterServer() throws RemotingCommandException,
        NoSuchFieldException, IllegalAccessException {
        RemotingCommand request = genSampleRegisterCmd(true);

        // version >= MQVersion.Version.V3_0_11.ordinal() to register with filter server
        request.setVersion(100);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);

        RemotingCommand response = defaultRequestProcessor.processRequest(ctx, request);

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getRemark()).isNull();

        RouteInfoManager routes = namesrvController.getRouteInfoManager();
        Field brokerAddrTable = RouteInfoManager.class.getDeclaredField("brokerAddrTable");
        brokerAddrTable.setAccessible(true);

        BrokerData broker = new BrokerData();
        broker.setBrokerName("broker");
        broker.setBrokerAddrs((HashMap) Maps.newHashMap(new Long(2333), "10.10.1.1"));

        assertThat((Map) brokerAddrTable.get(routes))
            .contains(new HashMap.SimpleEntry("broker", broker));
    }

    @Test
    public void testProcessRequest_UnregisterBroker() throws RemotingCommandException, NoSuchFieldException, IllegalAccessException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);

        //Register broker
        RemotingCommand regRequest = genSampleRegisterCmd(true);
        defaultRequestProcessor.processRequest(ctx, regRequest);

        //Unregister broker
        RemotingCommand unregRequest = genSampleRegisterCmd(false);
        RemotingCommand unregResponse = defaultRequestProcessor.processRequest(ctx, unregRequest);

        assertThat(unregResponse.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(unregResponse.getRemark()).isNull();

        RouteInfoManager routes = namesrvController.getRouteInfoManager();
        Field brokerAddrTable = RouteInfoManager.class.getDeclaredField("brokerAddrTable");
        brokerAddrTable.setAccessible(true);

        assertThat((Map) brokerAddrTable.get(routes)).isNotEmpty();
    }

    @Test
    public void testGetRouteInfoByTopic() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_ROUTEINFO_BY_TOPIC);
        RemotingCommand remotingCommandSuccess = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommandSuccess.getCode()).isEqualTo(ResponseCode.SUCCESS);
        request.getExtFields().put("topic", "test");
        RemotingCommand remotingCommandNoTopicRouteInfo = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommandNoTopicRouteInfo.getCode()).isEqualTo(ResponseCode.TOPIC_NOT_EXIST);
    }

    @Test
    public void testGetBrokerClusterInfo() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_BROKER_CLUSTER_INFO);
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testWipeWritePermOfBroker() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.WIPE_WRITE_PERM_OF_BROKER);
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetAllTopicListFromNameserver() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_ALL_TOPIC_LIST_FROM_NAMESERVER);
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testDeleteTopicInNamesrv() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.DELETE_TOPIC_IN_NAMESRV);
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetKVListByNamespace() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_KVLIST_BY_NAMESPACE);
        request.addExtField("namespace", "default-namespace-1");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.QUERY_NOT_FOUND);
        namesrvController.getKvConfigManager().putKVConfig("default-namespace-1", "key", "value");
        RemotingCommand remotingCommandSuccess = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommandSuccess.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetTopicsByCluster() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_TOPICS_BY_CLUSTER);
        request.addExtField("cluster", "default-cluster");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetSystemTopicListFromNs() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_SYSTEM_TOPIC_LIST_FROM_NS);
        request.addExtField("cluster", "default-cluster");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetUnitTopicList() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_UNIT_TOPIC_LIST);
        request.addExtField("cluster", "default-cluster");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetHasUnitSubTopicList() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_HAS_UNIT_SUB_TOPIC_LIST);
        request.addExtField("cluster", "default-cluster");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetHasUnitSubUnUnitTopicList() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_HAS_UNIT_SUB_UNUNIT_TOPIC_LIST);
        request.addExtField("cluster", "default-cluster");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testUpdateConfig() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.UPDATE_NAMESRV_CONFIG);
        request.addExtField("cluster", "default-cluster");
        Map<String, String> propertiesMap = new HashMap<>();
        propertiesMap.put("key", "value");
        request.setBody(propertiesMap.toString().getBytes());
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    @Test
    public void testGetConfig() throws RemotingCommandException {
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(null);
        RemotingCommand request = getRemotingCommand(RequestCode.GET_NAMESRV_CONFIG);
        request.addExtField("cluster", "default-cluster");
        RemotingCommand remotingCommand = defaultRequestProcessor.processRequest(ctx, request);
        assertThat(remotingCommand.getCode()).isEqualTo(ResponseCode.SUCCESS);
    }

    private RemotingCommand getRemotingCommand(int code) {
        RegisterBrokerRequestHeader header = new RegisterBrokerRequestHeader();
        header.setBrokerName("broker");
        RemotingCommand request = RemotingCommand.createRequestCommand(code, header);
        request.addExtField("brokerName", "broker");
        request.addExtField("brokerAddr", "10.10.1.1");
        request.addExtField("clusterName", "cluster");
        request.addExtField("haServerAddr", "10.10.2.1");
        request.addExtField("brokerId", "2333");
        request.addExtField("topic", "unit-test");
        return request;
    }

    private static RemotingCommand genSampleRegisterCmd(boolean reg) {
        RegisterBrokerRequestHeader header = new RegisterBrokerRequestHeader();
        header.setBrokerName("broker");
        RemotingCommand request = RemotingCommand.createRequestCommand(
            reg ? RequestCode.REGISTER_BROKER : RequestCode.UNREGISTER_BROKER, header);
        request.addExtField("brokerName", "broker");
        request.addExtField("brokerAddr", "10.10.1.1");
        request.addExtField("clusterName", "cluster");
        request.addExtField("haServerAddr", "10.10.2.1");
        request.addExtField("brokerId", "2333");
        return request;
    }

    private static void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }

    private void registerRouteInfoManager() {
        TopicConfigSerializeWrapper topicConfigSerializeWrapper = new TopicConfigSerializeWrapper();
        ConcurrentHashMap<String, TopicConfig> topicConfigConcurrentHashMap = new ConcurrentHashMap<>();
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setWriteQueueNums(8);
        topicConfig.setTopicName("unit-test");
        topicConfig.setPerm(6);
        topicConfig.setReadQueueNums(8);
        topicConfig.setOrder(false);
        topicConfigConcurrentHashMap.put("unit-test", topicConfig);
        topicConfigSerializeWrapper.setTopicConfigTable(topicConfigConcurrentHashMap);
        Channel channel = mock(Channel.class);
        RegisterBrokerResult registerBrokerResult = routeInfoManager.registerBroker("default-cluster", "127.0.0.1:10911", "default-broker", 0, "127.0.0.1:1001",
            topicConfigSerializeWrapper, new ArrayList<String>(), channel);

    }
}