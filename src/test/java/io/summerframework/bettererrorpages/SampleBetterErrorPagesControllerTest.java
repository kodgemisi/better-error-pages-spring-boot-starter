package io.summerframework.bettererrorpages;

import org.springframework.web.servlet.ModelAndView;

class SampleBetterErrorPagesControllerTest extends AbstractBetterErrorPagesControllerTest {

	@Override
	String sampleTraceFileName() {
		return "sample_traces/sampleTrace.txt";
	}

	@Override
	void assertions(ModelAndView modelAndView) {

		errorContextListSizeIs(modelAndView, 4);
		stringOccursNTimesInStyledTrace(modelAndView, "class=\"own-class\"", 4);
		stringOccursNTimesInStyledTrace(modelAndView, "source-id=\"", 4);
	}
}