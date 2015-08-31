package org.springframework.boot.netflix.spectator;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kubek2k.springockito.annotations.WrapWithSpy;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.test.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.netflix.test.util.SpringockitoWebContextLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.netflix.spectator.api.Registry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={SpectatorMonitoringWebResourceInterceptorTestConfig.class}, loader = SpringockitoWebContextLoader.class)
@WebAppConfiguration
@TestPropertySource(properties={"spring.application.name=test"})
public class SpectatorMonitoringWebResourceInterceptorTest {

	@Autowired
	WebApplicationContext webAppContext;
	
	@Autowired
	@WrapWithSpy(beanName="spectatorMonitoringWebResourceInterceptor")
	SpectatorMonitoringWebResourceInterceptor interceptorSpy;
	
	@Autowired
	Registry registry;
	
	private MockMvc mvc;
	
	@Before
	public void init()
	{
		this.mvc = webAppContextSetup(webAppContext).build();
	}
	
	@After
	public void cleanup()
	{
		Mockito.reset(registry);
	}
	
	@Test
	public void testWiringOfInterceptor() throws Exception {
		
		mvc.perform(get("/test/some/request/10"))
			.andExpect(status().isOk());
		
		Mockito.verify(interceptorSpy, Mockito.times(1)).preHandle(
				Mockito.any(HttpServletRequest.class),
				Mockito.any(HttpServletResponse.class), 
				Mockito.any());
		
		Mockito.verify(interceptorSpy, Mockito.times(1)).afterCompletion(
				Mockito.any(HttpServletRequest.class),
				Mockito.any(HttpServletResponse.class), 
				Mockito.any(),
				Mockito.any(Exception.class));
		
	}
	
	@Test
	public void testMetricsGatheredOnSuccess() throws Exception {
		mvc.perform(get("/test/some/request/10"))
		.andExpect(status().isOk());
		
		ArgumentCaptor<String> metricName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> tags = ArgumentCaptor.forClass(String.class);
		Mockito.verify(registry).timer(metricName.capture(), tags.capture());
		assertEquals("REST.test", metricName.getValue());
		assertThat(tags.getAllValues(), containsInAnyOrder(
				"method", "GET", "url", "test_some_request_-id-", "handlerName", "testSomeRequest",
				"caller", "unknown", "exceptionType", "none", "status", "200"
				));
	}
	
	@Test
	public void testMetricsGatheredOnException() throws Exception {
		mvc.perform(get("/test/some/error/10"))
		.andExpect(status().is5xxServerError());
		
		ArgumentCaptor<String> metricName = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> tags = ArgumentCaptor.forClass(String.class);
		Mockito.verify(registry).timer(metricName.capture(), tags.capture());
		assertEquals("REST.test", metricName.getValue());
		assertThat(tags.getAllValues(), containsInAnyOrder(
				"method", "GET", "url", "test_some_error_-id-", "handlerName", "testSomeError",
				"caller", "unknown", "exceptionType", "none", "status", "500"
				));
	}
}

@Configuration
@EnableWebMvc
@ImportAutoConfiguration({SpectatorMvcMonitoringAutoConfiguration.class, ErrorMvcAutoConfiguration.class, ServerPropertiesAutoConfiguration.class})
class SpectatorMonitoringWebResourceInterceptorTestConfig
{
	@Bean
	public SpectatorMonitoringWebResourceInterceptorTestController testController()
	{
		return new SpectatorMonitoringWebResourceInterceptorTestController();
	}
	
	@Bean
	public Registry spectatorRegistry()
	{
		return Mockito.mock(Registry.class, Mockito.RETURNS_DEEP_STUBS);
	}   
}

@Controller
@ControllerAdvice
class SpectatorMonitoringWebResourceInterceptorTestController
{
	@RequestMapping("/test/some/request/{id}")
	@ResponseBody
	public String testSomeRequest(@PathVariable Long id)
	{
		return "yay:"+id;
	}
	
	@RequestMapping("/test/some/error/{id}")
	@ResponseBody
	public String testSomeError(@PathVariable Long id)
	{
		throw new RuntimeException("Boom!");
	}
	
	@ExceptionHandler(value = {RuntimeException.class})
	@ResponseStatus(code=HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView defaultErrorHandler(HttpServletRequest request, Exception e) {
        ModelAndView mav = new ModelAndView("error");
        return mav;
    }
}
