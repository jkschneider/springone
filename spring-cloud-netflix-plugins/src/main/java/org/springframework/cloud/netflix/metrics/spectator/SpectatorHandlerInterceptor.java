package org.springframework.cloud.netflix.metrics.spectator;

import com.netflix.spectator.api.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

public class SpectatorHandlerInterceptor extends HandlerInterceptorAdapter {
    @Value("${netflix.spectator.rest.metricName:rest}")
    String metricName;

    @Value("${netflix.spectator.rest.callerHeader:#{null}}")
    String callerHeader;

    @Autowired
    Registry registry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestContextHolder.getRequestAttributes().setAttribute("requestStartTime", registry.clock().wallTime(), SCOPE_REQUEST);
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

        registry.timer(metricName,
            "method", request.getMethod(),
            "uri", uri.isEmpty() ? "root" : uri,
            "caller", caller != null ? caller : "unknown",
            "exceptionType", exception != null ? exception.getClass().getSimpleName() : "none",
            "status", ((Integer) response.getStatus()).toString()
        ).record(registry.clock().wallTime() - startTime, TimeUnit.MILLISECONDS);
    }
}
