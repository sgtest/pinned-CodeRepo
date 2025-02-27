package com.arnold.core.helper;

import com.arnold.common.config.DynamicServiceManager;
import com.arnold.common.config.ServiceDefinition;
import com.arnold.common.config.invoker.HttpServiceInvoker;
import com.arnold.common.config.invoker.ServiceInvoker;
import com.arnold.common.constants.BasicConst;
import com.arnold.common.constants.GatewayConst;
import com.arnold.common.enums.ResponseCode;
import com.arnold.common.exception.ResponseException;
import com.arnold.common.rule.Rule;
import com.arnold.core.context.GatewayContext;
import com.arnold.core.request.GatewayRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.arnold.common.constants.BasicConst.DIT_SEPARATOR;


public class RequestHelper {

	/**
	 * 根据请求和 netty 上下文构建 网关的上下文
	 * @param request
	 * @param ctx
	 * @return
	 */
	public static GatewayContext buildGatewayContext(FullHttpRequest request, ChannelHandlerContext ctx) {

		//	构建请求对象 GatewayRequest
		GatewayRequest gateWayRequest = buildGatewayRequest(request, ctx);

		//	根据请求对象里的 uniqueId，获取资源服务信息(也就是服务定义信息)
		ServiceDefinition serviceDefinition =
				DynamicServiceManager.getInstance().getServiceDefinition(gateWayRequest.getTargetServiceUniqueId());
		if(serviceDefinition == null) {
			throw new ResponseException(ResponseCode.SERVICE_DEFINITION_NOT_FOUND);
		}

//		ServiceDefinition serviceDefinition = ServiceDefinition.builder()
//				.serviceId("demo")
//				.enable(true)
//				.version("v1")
//				.patternPath("**")
//				.envType("dev")
//				.protocol(GatewayProtocol.HTTP)
//				.build();



		//	根据请求对象获取服务定义对应的方法调用，然后获取对应的规则
//		ServiceInvoker serviceInvoker = new HttpServiceInvoker();
//		serviceInvoker.setInvokerPath(gateWayRequest.getPath());
//		serviceInvoker.setTimeout(500);

		// 根据请求对象，获取规则
		Rule rule = getRule(gateWayRequest, serviceDefinition.getServiceId());
//		Rule rule = new Rule();

		//	构建我们而定 GateWayContext 对象
		GatewayContext gatewayContext = new GatewayContext(
				serviceDefinition.getProtocol(),
				ctx,
				HttpUtil.isKeepAlive(request),
				gateWayRequest,
				rule, 0);

		// 后续服务发现做完，这里都要改成动态的——已经在负载均衡算法中实现
//		gatewayContext.getRequest().setModifyHost("127.0.0.1:8082");
		return gatewayContext;
	}

	/**
	 *构建 Request 请求对象
	 */
	private static GatewayRequest buildGatewayRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {

		//netty的httpHeaders
		HttpHeaders headers = fullHttpRequest.headers();

		//	从 header 头获取必须要传入的关键属性 uniqueId
		String uniqueId = headers.get(GatewayConst.UNIQUE_ID);

		String host = headers.get(HttpHeaderNames.HOST);
		HttpMethod method = fullHttpRequest.method();
		String uri = fullHttpRequest.uri();
		String clientIp = getClientIp(ctx, fullHttpRequest);
		String contentType = HttpUtil.getMimeType(fullHttpRequest) == null ? null : HttpUtil.getMimeType(fullHttpRequest).toString();

		Charset charset = HttpUtil.getCharset(fullHttpRequest, StandardCharsets.UTF_8);

		GatewayRequest gatewayRequest = new GatewayRequest(uniqueId,
				charset,
				clientIp,
				host,
				uri,
				method,
				contentType,
				headers,
				fullHttpRequest);

		return gatewayRequest;
	}

	/**
	 * 获取客户端ip
	 */
	private static String getClientIp(ChannelHandlerContext ctx, FullHttpRequest request) {
		String xForwardedValue = request.headers().get(BasicConst.HTTP_FORWARD_SEPARATOR);

		String clientIp = null;
		if(StringUtils.isNotEmpty(xForwardedValue)) {
			List<String> values = Arrays.asList(xForwardedValue.split(", "));
			if(values.size() >= 1 && StringUtils.isNotBlank(values.get(0))) {
				clientIp = values.get(0);
			}
		}
		if(clientIp == null) {
			InetSocketAddress inetSocketAddress = (InetSocketAddress)ctx.channel().remoteAddress();
			clientIp = inetSocketAddress.getAddress().getHostAddress();
		}
		return clientIp;
	}

	/**
	 * 根据请求，获取 Rule 对象
	 *
	 * @param gateWayRequest
	 * @param serviceId
	 * @return
	 */
	private static Rule getRule(GatewayRequest gateWayRequest, String serviceId) {
		String key = serviceId + DIT_SEPARATOR + gateWayRequest.getPath();
		//全路径匹配
		Rule rule = DynamicServiceManager.getInstance().getRuleByPath(key);

		if (rule != null) {
			return rule;
		}

		//前缀匹配
		return DynamicServiceManager.getInstance().getRuleByServiceId(serviceId)
				.stream().filter(r -> gateWayRequest.getPath().startsWith(r.getPrefix()))
				.findAny()
				.orElseThrow(() -> new ResponseException(ResponseCode.PATH_NO_MATCHED));
	}
}
