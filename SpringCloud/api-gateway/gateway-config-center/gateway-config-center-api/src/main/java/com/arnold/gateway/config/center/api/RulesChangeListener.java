/**
 * @projectName JianGateWay
 * @package tech.songjian.gateway.config.center.api
 * @className tech.songjian.gateway.config.center.api.RulesChangeListener
 */
package com.arnold.gateway.config.center.api;


import com.arnold.common.rule.Rule;

import java.util.List;

/**
 * RulesChangeListener
 * @description 规则变更的监听器
 * @author SongJian
 * @date 2023/6/10 13:46
 * @version
 */
public interface RulesChangeListener {

    /**
     * 回调方法：当规则变更时，触发
     * @param rules 规则列表
     */
    void onRulesChange(List<Rule> rules);
}
