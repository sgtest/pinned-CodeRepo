package com.tangyh.lamp.activiti.config;

import com.tangyh.basic.boot.config.BaseConfig;
import com.tangyh.basic.log.event.SysLogListener;
import com.tangyh.lamp.oauth.api.LogApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zuihou
 * @date 2017-12-15 14:42
 */
@Configuration
public class ActivitiWebConfiguration extends BaseConfig {

    /**
     * lamp.log.enabled = true 并且 lamp.log.type=DB时实例该类
     *
     */
    @Bean
    @ConditionalOnExpression("${lamp.log.enabled:true} && 'DB'.equals('${lamp.log.type:LOGGER}')")
    public SysLogListener sysLogListener(LogApi logApi) {
        return new SysLogListener(logApi::save);
    }
}
