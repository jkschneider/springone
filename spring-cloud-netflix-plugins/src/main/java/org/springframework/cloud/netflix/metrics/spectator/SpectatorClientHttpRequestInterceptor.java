package org.springframework.cloud.netflix.metrics.spectator;

import com.netflix.spectator.api.Id;
import com.netflix.spectator.api.Registry;
import com.netflix.spectator.sandbox.BucketFunction;
import com.netflix.spectator.sandbox.BucketFunctions;
import com.netflix.spectator.sandbox.BucketTimer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SpectatorClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    @Autowired
    Registry registry;

    @Value("${netflix.spectator.restClient.metricName:restclient}")
    String metricName;
    
    @Value("${netflix.spectator.restClient.maxAge:10000}")
    Long maxAge;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String urlTemplate = RestTemplateUrlTemplateHolder.getRestTemplateUrlTemplate();
        if(urlTemplate == null)
            urlTemplate = "none";

        long startTime = registry.clock().monotonicTime();
        String status = "CLIENT_ERROR";
        try {
            ClientHttpResponse response = execution.execute(request, body);
            status = ((Integer) response.getRawStatusCode()).toString();
            return response;
        }
        finally {
            String host = request.getURI().getHost();

            Id timerId = registry.createId(metricName,
                    "method", request.getMethod().name(),
                    "uri", urlTemplate.replaceAll("^https?://[^/]+/", "").replaceAll("/", "_").replaceAll("[{}]", "-"),
                    "status", status,
                    "clientName", host != null ? host : "none"
            );
            
            BucketFunction f = BucketFunctions.latency(maxAge, TimeUnit.MILLISECONDS);
            BucketTimer t = BucketTimer.get(registry, timerId, f);
            t.record(registry.clock().monotonicTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
