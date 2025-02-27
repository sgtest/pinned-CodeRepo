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

package org.apache.rocketmq.filter;

import org.apache.rocketmq.filter.expression.Expression;
import org.apache.rocketmq.filter.expression.MQFilterException;

/**
 * Filter spi interface.
 */
//过滤器spi接口，支持自定义，需要实现解析和类型两个方法
public interface FilterSpi {

    /**
     * Compile.
     */
    Expression compile(final String expr) throws MQFilterException;

    /**
     * Which type.
     */
    String ofType();
}
