package org.springframework.cloud.netflix.metrics.spectator;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.sandbox.BucketFunction;
import com.netflix.spectator.sandbox.BucketFunctions;
import com.netflix.spectator.sandbox.BucketTimer;

public class SpectatorHandlerInterceptor extends HandlerInterceptorAdapter {
    @Value("${netflix.spectator.rest.metricName:rest}")
    String metricName;

    @Value("${netflix.spectator.rest.callerHeader:#{null}}")
    String callerHeader;
    
    @Value("${netflix.spectator.rest.maxAge:10000}")
    Long maxAge;
    
    @Autowired
    Registry registry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestContextHolder.getRequestAttributes().setAttribute("requestStartTime", registry.clock().monotonicTime(), SCOPE_REQUEST);
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        RequestContextHolder.getRequestAttributes().setAttribute("exception", ex, SCOPE_REQUEST);
        Long startTime = (Long) RequestContextHolder.getRequestAttributes().getAttribute("requestStartTime", SCOPE_REQUEST);
        if (startTime != null)
            recordMetric(request, response, handler, startTime);
        super.afterCompletion(request, response, handler, ex);
    }

    protected void recordMetric(HttpServletRequest request, HttpServletResponse response, Object handler, Long startTime) {
        // transform paths like /foo/bar/{user} to foo_bar_-user- because Atlas does everything with query params without escaping
        // the metric name
        String uri = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
                .toString().substring(1).replaceAll("/", "_").replaceAll("[{}]", "-");

        String caller = "unknown";
        if(callerHeader != null)
            caller = request.getHeader(callerHeader);

        Object exception = request.getAttribute("exception");
        Id timerId = registry.createId(metricName,
                "method", request.getMethod(),
                "uri", uri.isEmpty() ? "root" : uri,
                "handlerName", handler instanceof HandlerMethod ? ((HandlerMethod)handler).getMethod().getName() : "unknown",
                "caller", caller != null ? caller : "unknown",
                "exceptionType", exception != null ? exception.getClass().getSimpleName() : "none",
                "status", ((Integer) response.getStatus()).toString());
        BucketFunction f = BucketFunctions.latency(maxAge, TimeUnit.MILLISECONDS);
        BucketTimer t = BucketTimer.get(registry, timerId, f);
        t.record(registry.clock().monotonicTime() - startTime, TimeUnit.NANOSECONDS);
    }
}
