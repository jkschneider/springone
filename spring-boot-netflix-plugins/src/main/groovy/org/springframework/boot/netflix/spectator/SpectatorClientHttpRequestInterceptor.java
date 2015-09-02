package org.springframework.boot.netflix.spectator;

import com.netflix.spectator.api.Registry;
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

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String urlTemplate = RestTemplateUrlTemplateHolder.getRestTemplateUrlTemplate();
        if(urlTemplate == null)
            urlTemplate = "none";

        long startTime = registry.clock().wallTime();
        String status = "CLIENT_ERROR";
        try {
            ClientHttpResponse response = execution.execute(request, body);
            status = ((Integer) response.getRawStatusCode()).toString();
            return response;
        }
        finally {
            String host = request.getURI().getHost();

            registry.timer(metricName,
                    "method", request.getMethod().name(),
                    "uri", urlTemplate.replaceAll("^https?://[^/]+/", "").replaceAll("/", "_").replaceAll("[{}]", "-"),
                    "status", status,
                    "clientName", host != null ? host : "none"
            ).record(registry.clock().wallTime() - startTime, TimeUnit.MILLISECONDS);
        }
    }
}
