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

/* Generated By:JavaCC: Do not edit this line. SelectorParserConstants.java */
package org.apache.rocketmq.filter.parser;

/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
//ok  选择器解析器常数
public interface SelectorParserConstants {

    /**
     * End of File.
     */
    int EOF = 0;
    /**
     * RegularExpression Id.
     */
    int LINE_COMMENT = 6;
    /**
     * RegularExpression Id.
     */
    int BLOCK_COMMENT = 7;
    /**
     * RegularExpression Id.
     */
    int NOT = 8;
    /**
     * RegularExpression Id.
     */
    int AND = 9;
    /**
     * RegularExpression Id.
     */
    int OR = 10;
    /**
     * RegularExpression Id.
     */
    int BETWEEN = 11;
    /**
     * RegularExpression Id.
     */
    int IN = 12;
    /**
     * RegularExpression Id.
     */
    int TRUE = 13;
    /**
     * RegularExpression Id.
     */
    int FALSE = 14;
    /**
     * RegularExpression Id.
     */
    int NULL = 15;
    /**
     * RegularExpression Id.
     */
    int IS = 16;
    /**
     * RegularExpression Id.
     */
    int DECIMAL_LITERAL = 17;
    /**
     * RegularExpression Id.
     */
    int FLOATING_POINT_LITERAL = 18;
    /**
     * RegularExpression Id.
     */
    int EXPONENT = 19;
    /**
     * RegularExpression Id.
     */
    int STRING_LITERAL = 20;
    /**
     * RegularExpression Id.
     */
    int ID = 21;

    /**
     * Lexical state.
     */
    int DEFAULT = 0;

    /**
     * Literal token values.
     */
    String[] TOKEN_IMAGE = {
        "<EOF>",
        "\" \"",
        "\"\\t\"",
        "\"\\n\"",
        "\"\\r\"",
        "\"\\f\"",
        "<LINE_COMMENT>",
        "<BLOCK_COMMENT>",
        "\"NOT\"",
        "\"AND\"",
        "\"OR\"",
        "\"BETWEEN\"",
        "\"IN\"",
        "\"TRUE\"",
        "\"FALSE\"",
        "\"NULL\"",
        "\"IS\"",
        "<DECIMAL_LITERAL>",
        "<FLOATING_POINT_LITERAL>",
        "<EXPONENT>",
        "<STRING_LITERAL>",
        "<ID>",
        "\"=\"",
        "\"<>\"",
        "\">\"",
        "\">=\"",
        "\"<\"",
        "\"<=\"",
        "\"(\"",
        "\",\"",
        "\")\"",
        "\"+\"",
        "\"-\"",
    };

}
