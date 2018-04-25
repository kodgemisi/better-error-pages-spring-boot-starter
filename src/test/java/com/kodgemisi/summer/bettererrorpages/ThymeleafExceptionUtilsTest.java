/*
 *  Copyright © 2018 Kod Gemisi Ltd.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is “Incompatible With Secondary Licenses”, as defined by
 * the Mozilla Public License, v. 2.0.
 *
 */

package com.kodgemisi.summer.bettererrorpages;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class ThymeleafExceptionUtilsTest {

	private static final String PROJECT_PATH = "";

	private static final String PACKAGE_NAME = "com.kodgemisi";

	private static final String SAMPLE_TRACE;

	static {
		final Resource resource = new ClassPathResource("sampleTrace.txt");
		try {
			SAMPLE_TRACE = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8.name());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ThymeleafExceptionUtils thymeleafExceptionUtils = new ThymeleafExceptionUtils(PROJECT_PATH, PACKAGE_NAME);

	@Test
	public void styledTraceTest() {
		final String styledTrace = thymeleafExceptionUtils.styledTrace(SAMPLE_TRACE);

		assertThat(styledTrace, containsString("<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.DemoClass.error(NonPublicController.java:29)</span>"));
		assertThat(styledTrace, containsString("<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.NonPublicController.demo(NonPublicController.java:18)</span>"));
		assertThat(styledTrace, containsString("<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.DemoClass2.error(NonPublicController.java:36)</span>"));
		assertThat(styledTrace, containsString("<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.DemoClass.error(NonPublicController.java:26)</span>"));
	}

	@Test
	public void getErrorContextsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		Method method = ThymeleafExceptionUtils.class.getDeclaredMethod("getErrorContexts", String.class);
		method.setAccessible(true);
		List<ThymeleafExceptionUtils.ErrorContext> errorContexts = (List<ThymeleafExceptionUtils.ErrorContext>) method.invoke(thymeleafExceptionUtils, SAMPLE_TRACE);

		assertEquals(errorContexts.size(), 4);
	}

}
