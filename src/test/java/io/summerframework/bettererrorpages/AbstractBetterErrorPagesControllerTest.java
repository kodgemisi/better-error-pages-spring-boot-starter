package io.summerframework.bettererrorpages;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfiguration.class)
abstract class AbstractBetterErrorPagesControllerTest {

	@InjectMocks
	private BetterErrorPagesController betterErrorPagesController;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest request;

	@Mock
	private ErrorAttributes errorAttributes;

	@Mock
	private ErrorProperties errorProperties;

	@Spy
	private BetterErrorPagesService betterErrorPagesService = betterErrorPagesService();

	@Test
	void shouldReturnDefaultMessage() {
		Map<String, Object> map = new HashMap<>();
		map.put("trace", Utils.readSampleFile(sampleTraceFileName()));
		Mockito.when(errorAttributes.getErrorAttributes(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean())).thenReturn(map);

		final ModelAndView modelAndView = betterErrorPagesController.errorHtml(request, response);

		Assertions.assertNotNull(modelAndView.getModel(), "has model");
		Assertions.assertNotNull(modelAndView.getModel().get("errorContextList"), "has errorContextList");

		final String styledTrace = styledTrace(modelAndView);
		for (ErrorContext errorContext : errorContextList(modelAndView)) {
			assertTrue(styledTrace.contains(errorContext.getId()), "every errorContext id appears at least once in styledTrace (" + errorContext.getId() +")");
		}

		assertions(modelAndView);
	}

	protected BetterErrorPagesService betterErrorPagesService() {
		return new BetterErrorPagesService("com.kodgemisi");
	}

	protected void stringOccursNTimesInStyledTrace(ModelAndView modelAndView, String target, int times) {
		final String styledTrace = styledTrace(modelAndView);
		Assertions.assertEquals(times, StringUtils.countOccurrencesOf(styledTrace, target), target + " occurs " + times + " times in styledTrace");
	}

	protected void errorContextListSizeIs(ModelAndView modelAndView, int size) {
		final List<ErrorContext> errorContexts = errorContextList(modelAndView);
		Assertions.assertEquals(size, errorContexts.size(), "errorContextList has the right size");
	}

	protected List<ErrorContext> errorContextList(ModelAndView modelAndView) {
		return (List<ErrorContext>) modelAndView.getModel().get("errorContextList");
	}

	protected String styledTrace(ModelAndView modelAndView) {
		return modelAndView.getModel().get("styledTrace").toString();
	}

	abstract String sampleTraceFileName();

	abstract void assertions(ModelAndView modelAndView);

}