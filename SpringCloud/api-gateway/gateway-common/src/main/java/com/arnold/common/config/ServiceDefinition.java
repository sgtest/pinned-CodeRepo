package com.arnold.common.config;

import com.arnold.common.config.invoker.AbstractServiceInvoker;
import com.arnold.common.config.invoker.ServiceInvoker;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.io.Serializable;
import java.util.Map;

/**
 * 一个服务定义会对应多个服务实例
 */
@Builder
@Data
public class ServiceDefinition implements Serializable {

    private String uniqueId;

    private String serviceId;

    private String version;

    //http、dubbo
    private String protocol;

    private String patternPath;

    private String envType;

    @Builder.Default
    private boolean enable = true;

    private Map<String/*invokerPath*/, AbstractServiceInvoker> invokerMap;

    @Tolerate
    public ServiceDefinition() {
    }


}
