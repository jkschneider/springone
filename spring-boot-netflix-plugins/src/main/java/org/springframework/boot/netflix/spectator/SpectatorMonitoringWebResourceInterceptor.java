package org.springframework.boot.netflix.spectator;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.netflix.spectator.api.Registry;

@Order(Ordered.LOWEST_PRECEDENCE)
public class SpectatorMonitoringWebResourceInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	private Registry registry;
	
	@Value("#{environment['spring.mvc.spectator.metricName']?:'REST.'+(environment['spring.application.name']?:'application')}")
	protected String metricName ="REST.applicationasdf";
	@Value("#{environment['spring.mvc.spectator.callerHeader']}")
	protected String callerHeader="x-netflix.client.appid";

	
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		RequestContextHolder.getRequestAttributes().setAttribute("requestStartTime", 
				registry.clock().monotonicTime(), RequestAttributes.SCOPE_REQUEST);
		return super.preHandle(request, response, handler);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		RequestContextHolder.getRequestAttributes().setAttribute("exception", ex, RequestAttributes.SCOPE_REQUEST);
		
		Long startTime = (Long) RequestContextHolder.getRequestAttributes().getAttribute("requestStartTime", RequestAttributes.SCOPE_REQUEST);
		if(startTime != null)
		{
			recordMetric(request, response, handler, startTime);
		}
		super.afterCompletion(request, response, handler, ex);
	}	
	
	protected void recordMetric(HttpServletRequest request, HttpServletResponse response, Object handler, Long startTime) {
		String url = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
				.toString().substring(1).replaceAll("/", "_").replaceAll("[{}]", "-");
		
		registry.timer(this.metricName,
				"method", request.getMethod(),
				"url", url.isEmpty() ? "root" : url,
				"handlerName", handler instanceof HandlerMethod ? ((HandlerMethod)handler).getMethod().getName() : "unknown",
				"caller", request.getHeader(this.callerHeader) != null ? request.getHeader(this.callerHeader) : "unknown",
				"exceptionType", request.getAttribute("exception") != null ? request.getAttribute("exception").getClass().getSimpleName() : "none",
				"status", Integer.toString(response.getStatus()))
				.record(registry.clock().wallTime() - startTime, TimeUnit.MILLISECONDS);
	}
	
}