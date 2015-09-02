package org.springframework.boot.netflix.spectator;

import java.util.concurrent.TimeUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

import com.netflix.servo.monitor.DynamicTimer
import com.netflix.servo.monitor.MonitorConfig
import com.netflix.spectator.api.Registry


public class SpectatorLoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	@Autowired
	Registry registry
	
	@Value('${netflix.spectator.restClient.metricName:restclient}')
	String metricName

	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, final ClientHttpRequestExecution execution)
			throws IOException {
		
		String urlTemplate = RestTemplateUrlTemplateHolder.getRestTempalteUrlTemplate() != null ? RestTemplateUrlTemplateHolder.getRestTempalteUrlTemplate() : "Unknown";
		long startTime = registry.clock().wallTime();
		String status = "CLIENT_ERROR";
		try {
			 ClientHttpResponse response = execution.execute(request, body);
			 status = response.getRawStatusCode().toString();
			 return response;
		} 
		finally
		{
			recordTime(request.getMethod().name(), urlTemplate, request.getURI().getHost(), startTime, status);
		}
	}

	protected void recordTime(String method, String uriString, String clientName, Long startTimeMillis, String status) {
		final String formattedUriString = uriString.replaceAll("^https?://[^/]+/", "").replaceAll("/", "_").replaceAll("[{}]", "-");

		registry.timer(metricName,
			'method', method,
			'uri', formattedUriString,
			'status', status,
			'clientName', clientName != null ? clientName : "Unknown"
			).record(registry.clock().wallTime() - startTimeMillis, TimeUnit.MILLISECONDS);
	}
 
}
