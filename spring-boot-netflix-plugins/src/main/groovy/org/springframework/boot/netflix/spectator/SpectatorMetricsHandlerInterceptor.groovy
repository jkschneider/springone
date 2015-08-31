package org.springframework.boot.netflix.spectator

import com.netflix.spectator.api.Registry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.concurrent.TimeUnit

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST

class SpectatorMetricsHandlerInterceptor extends HandlerInterceptorAdapter {
    @Value('${spring.application.name:test}')
    String metricName

    @Value('${netflix.spectator.callerHeader:#{null}}')
    String callerHeader

    @Autowired
    Registry registry

    @Override
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RequestContextHolder.getRequestAttributes().setAttribute('requestStartTime', registry.clock().wallTime(), SCOPE_REQUEST)
        super.preHandle(request, response, handler)
    }

    @Override
    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestContextHolder.requestAttributes.setAttribute('exception', ex, SCOPE_REQUEST)
        def startTime = RequestContextHolder.getRequestAttributes().getAttribute('requestStartTime', SCOPE_REQUEST) as Long
        if (startTime)
            recordMetric(request, response, handler, startTime)
        super.afterCompletion(request, response, handler, ex)
    }

    protected void recordMetric(HttpServletRequest request, HttpServletResponse response, Object handler, Long startTime) {
        // transform paths like /foo/bar/{user} to foo_bar_-user- because Atlas does everything with query params without escaping
        // the metric name
        def uri = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)
                .toString().substring(1).replaceAll('/', '_').replaceAll('[{}]', '-')

        registry.timer(metricName,
            'method', request.method,
            'uri', uri.empty ? 'root' : uri,
            'caller', callerHeader ? (request.getHeader(callerHeader) ?: 'unknown') : 'unknown',
            'exceptionType', request.getAttribute('exception')?.class?.simpleName ?: 'none',
            'status', response.getStatus() as String
        ).record(registry.clock().wallTime() - startTime, TimeUnit.MILLISECONDS)
    }
}
