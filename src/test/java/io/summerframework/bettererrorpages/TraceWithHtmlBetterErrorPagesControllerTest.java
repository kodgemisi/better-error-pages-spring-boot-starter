package io.summerframework.bettererrorpages;

import org.springframework.web.servlet.ModelAndView;

class TraceWithHtmlBetterErrorPagesControllerTest extends AbstractBetterErrorPagesControllerTest {

	@Override
	String sampleTraceFileName() {
		return "sample_traces/traceWithHtml.txt";
	}

	@Override
	void assertions(ModelAndView modelAndView) {

		errorContextListSizeIs(modelAndView, 3);
		stringOccursNTimesInStyledTrace(modelAndView, "class=\"own-class\"", 4);
		stringOccursNTimesInStyledTrace(modelAndView, "source-id=\"", 4);
		stringOccursNTimesInStyledTrace(modelAndView, "<img", 0);
	}
}