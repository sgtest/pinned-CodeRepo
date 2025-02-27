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
package org.apache.rocketmq.acl.plain;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.rocketmq.acl.common.AclException;
import org.apache.rocketmq.acl.common.AclUtils;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.InternalLogger;
import org.apache.rocketmq.logging.InternalLoggerFactory;
//ok  远程地址工厂类
public class RemoteAddressStrategyFactory {

    private static final InternalLogger log = InternalLoggerFactory.getLogger(LoggerName.COMMON_LOGGER_NAME);

    public static final NullRemoteAddressStrategy NULL_NET_ADDRESS_STRATEGY = new NullRemoteAddressStrategy();
    public static final BlankRemoteAddressStrategy BLANK_NET_ADDRESS_STRATEGY = new BlankRemoteAddressStrategy();

    public RemoteAddressStrategy getRemoteAddressStrategy(PlainAccessResource plainAccessResource) {
        return getRemoteAddressStrategy(plainAccessResource.getWhiteRemoteAddress());
    }

    //根据ip地址获取远程地址策略
    public RemoteAddressStrategy getRemoteAddressStrategy(String remoteAddr) {
        //如果ip为空，就是blank策略
        if (StringUtils.isBlank(remoteAddr)) {
            return BLANK_NET_ADDRESS_STRATEGY;
        }
        //ip为*就是Null策略
        if ("*".equals(remoteAddr) || "*.*.*.*".equals(remoteAddr) || "*:*:*:*:*:*:*:*".equals(remoteAddr)) {
            return NULL_NET_ADDRESS_STRATEGY;
        }
        //这里是multi策略的逻辑判断
        if (remoteAddr.endsWith("}")) {
            if (AclUtils.isColon(remoteAddr)) {
                String[] strArray = StringUtils.split(remoteAddr, ":");
                String last = strArray[strArray.length - 1];
                if (!last.startsWith("{")) {
                    throw new AclException(String.format("MultipleRemoteAddressStrategy netaddress examine scope Exception netaddress", remoteAddr));
                }
                return new MultipleRemoteAddressStrategy(AclUtils.getAddresses(remoteAddr, last));
            } else {
                String[] strArray = StringUtils.split(remoteAddr, ".");
                // However a right IP String provided by user,it always can be divided into 4 parts by '.'.
                if (strArray.length < 4) {
                    throw new AclException(String.format("MultipleRemoteAddressStrategy has got a/some wrong format IP(s) ", remoteAddr));
                }
                String lastStr = strArray[strArray.length - 1];
                if (!lastStr.startsWith("{")) {
                    throw new AclException(String.format("MultipleRemoteAddressStrategy netaddress examine scope Exception netaddress", remoteAddr));
                }
                return new MultipleRemoteAddressStrategy(AclUtils.getAddresses(remoteAddr, lastStr));
            }
            //带逗号也是multi策略
        } else if (AclUtils.isComma(remoteAddr)) {
            return new MultipleRemoteAddressStrategy(StringUtils.split(remoteAddr, ","));
            //带*或者带-是range策略
        } else if (AclUtils.isAsterisk(remoteAddr) || AclUtils.isMinus(remoteAddr)) {
            return new RangeRemoteAddressStrategy(remoteAddr);
        }
        //上面都没匹配到，就是one策略
        return new OneRemoteAddressStrategy(remoteAddr);

    }

    //无远程地址策略，永远能匹配到
    public static class NullRemoteAddressStrategy implements RemoteAddressStrategy {
        @Override
        public boolean match(PlainAccessResource plainAccessResource) {
            return true;
        }
    }

    //空白远程地址策略，永远匹配不到
    public static class BlankRemoteAddressStrategy implements RemoteAddressStrategy {
        @Override
        public boolean match(PlainAccessResource plainAccessResource) {
            return false;
        }
    }

    //多远程地址策略
    public static class MultipleRemoteAddressStrategy implements RemoteAddressStrategy {
        private final Set<String> multipleSet = new HashSet<>();
        //构造方法需要闯入地址数组
        public MultipleRemoteAddressStrategy(String[] strArray) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            //遍历传入的地址列表，如果是ipv4就直接放入，如果是ipv6就扩展后放入，否则抛异常
            for (String netaddress : strArray) {
                if (validator.isValidInet4Address(netaddress)) {
                    multipleSet.add(netaddress);
                } else if (validator.isValidInet6Address(netaddress)) {
                    multipleSet.add(AclUtils.expandIP(netaddress, 8));
                } else {
                    throw new AclException(String.format("Netaddress examine Exception netaddress is %s", netaddress));
                }
            }
        }
        @Override
        public boolean match(PlainAccessResource plainAccessResource) {
            InetAddressValidator validator = InetAddressValidator.getInstance();
            //获取传入的普通进入资源的白名单远程地址，如果是ipv6就扩展该地址，最后判断地址列表中是否包含该地址
            String whiteRemoteAddress = plainAccessResource.getWhiteRemoteAddress();
            if (validator.isValidInet6Address(whiteRemoteAddress)) {
                whiteRemoteAddress = AclUtils.expandIP(whiteRemoteAddress, 8);
            }
            return multipleSet.contains(whiteRemoteAddress);
        }
    }
    //ok  单一远程地址策略
    public static class OneRemoteAddressStrategy implements RemoteAddressStrategy {
        private String netaddress;
        //构造方法需要传入一个网络地址
        public OneRemoteAddressStrategy(String netaddress) {
            this.netaddress = netaddress;
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if (!(validator.isValidInet4Address(netaddress) || validator.isValidInet6Address(netaddress))) {
                throw new AclException(String.format("Netaddress examine Exception netaddress is %s", netaddress));
            }
        }
        //看是否匹配到构造方法中传入的地址
        @Override
        public boolean match(PlainAccessResource plainAccessResource) {
            String writeRemoteAddress = AclUtils.expandIP(plainAccessResource.getWhiteRemoteAddress(), 8).toUpperCase();
            return AclUtils.expandIP(netaddress, 8).toUpperCase().equals(writeRemoteAddress);
        }
    }

    //ok  范围远程地址策略
    public static class RangeRemoteAddressStrategy implements RemoteAddressStrategy {
        private String head;
        private int start;
        private int end;
        private int index;
        public RangeRemoteAddressStrategy(String remoteAddr) {
//            IPv6 Address  如果地址带冒号，说明可能是ipv6
            if (AclUtils.isColon(remoteAddr)) {
                //检查是否是ipv6 并设置head、index、start、end
                //后续传入ip之后根据这四个属性判断ip是否在允许的ip网段内
                AclUtils.IPv6AddressCheck(remoteAddr);
                String[] strArray = StringUtils.split(remoteAddr, ":");
                for (int i = 1; i < strArray.length; i++) {
                    if (ipv6Analysis(strArray, i)) {
                        AclUtils.verify(remoteAddr, index - 1);
                        String preAddress = AclUtils.v6ipProcess(remoteAddr);
                        this.index = StringUtils.split(preAddress, ":").length;
                        this.head = preAddress;
                        break;
                    }
                }
            } else {
                String[] strArray = StringUtils.split(remoteAddr, ".");
                if (analysis(strArray, 1) || analysis(strArray, 2) || analysis(strArray, 3)) {
                    AclUtils.verify(remoteAddr, index - 1);
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < index; j++) {
                        sb.append(strArray[j].trim()).append(".");
                    }
                    this.head = sb.toString();
                }
            }
        }

        private boolean analysis(String[] strArray, int index) {
            String value = strArray[index].trim();
            this.index = index;
            if ("*".equals(value)) {
                setValue(0, 255);
            } else if (AclUtils.isMinus(value)) {
                if (value.indexOf("-") == 0) {
                    throw new AclException(String.format("RangeRemoteAddressStrategy netaddress examine scope Exception value %s ", value));

                }
                String[] valueArray = StringUtils.split(value, "-");
                this.start = Integer.parseInt(valueArray[0]);
                this.end = Integer.parseInt(valueArray[1]);
                if (!(AclUtils.isScope(end) && AclUtils.isScope(start) && start <= end)) {
                    throw new AclException(String.format("RangeRemoteAddressStrategy netaddress examine scope Exception start is %s , end is %s", start, end));
                }
            }
            return this.end > 0;
        }

        private boolean ipv6Analysis(String[] strArray, int index) {
            String value = strArray[index].trim();
            this.index = index;
            if ("*".equals(value)) {
                int min = Integer.parseInt("0", 16);
                int max = Integer.parseInt("ffff", 16);
                setValue(min, max);
            } else if (AclUtils.isMinus(value)) {
                if (value.indexOf("-") == 0) {
                    throw new AclException(String.format("RangeRemoteAddressStrategy netaddress examine scope Exception value %s ", value));
                }
                String[] valueArray = StringUtils.split(value, "-");
                this.start = Integer.parseInt(valueArray[0], 16);
                this.end = Integer.parseInt(valueArray[1], 16);
                if (!(AclUtils.isIPv6Scope(end) && AclUtils.isIPv6Scope(start) && start <= end)) {
                    throw new AclException(String.format("RangeRemoteAddressStrategy netaddress examine scope Exception start is %s , end is %s", start, end));
                }
            }
            return this.end > 0 ? true : false;
        }

        private void setValue(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public boolean match(PlainAccessResource plainAccessResource) {
            String netAddress = plainAccessResource.getWhiteRemoteAddress();
            InetAddressValidator validator = InetAddressValidator.getInstance();
            if (validator.isValidInet4Address(netAddress)) {
                if (netAddress.startsWith(this.head)) {
                    String value;
                    if (index == 3) {
                        value = netAddress.substring(this.head.length());
                    } else if (index == 2) {
                        value = netAddress.substring(this.head.length(), netAddress.lastIndexOf('.'));
                    } else {
                        value = netAddress.substring(this.head.length(), netAddress.lastIndexOf('.', netAddress.lastIndexOf('.') - 1));
                    }
                    Integer address = Integer.valueOf(value);
                    if (address >= this.start && address <= this.end) {
                        return true;
                    }
                }
            } else if (validator.isValidInet6Address(netAddress)) {
                netAddress = AclUtils.expandIP(netAddress, 8).toUpperCase();
                if (netAddress.startsWith(this.head)) {
                    String value = netAddress.substring(5 * index, 5 * index + 4);
                    Integer address = Integer.parseInt(value, 16);
                    if (address >= this.start && address <= this.end) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
