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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ThymeleafExceptionUtils.class, ThymeleafExceptionUtilsTest.class})
public class ThymeleafExceptionUtilsTest {

	private static final String PACKAGE_NAME = "com.kodgemisi";

	private static final String SAMPLE_TRACE;

	private static final String SAMPLE_SOURCE;

	private static final String SPACE_CHARACTER = " ";

	static {
		final Resource sampleTraceResource = new ClassPathResource("sampleTrace.txt");
		final Resource sampleSourceResource = new ClassPathResource("sampleSource.txt");
		try {
			SAMPLE_TRACE = IOUtils.toString(sampleTraceResource.getInputStream(), StandardCharsets.UTF_8.name());
			SAMPLE_SOURCE = IOUtils.toString(sampleSourceResource.getInputStream(), StandardCharsets.UTF_8.name());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ThymeleafExceptionUtils thymeleafExceptionUtils = new ThymeleafExceptionUtils(PACKAGE_NAME);

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

		@SuppressWarnings("unchecked")
		List<ErrorContext> errorContexts = (List<ErrorContext>) method.invoke(thymeleafExceptionUtils, SAMPLE_TRACE);

		assertEquals(errorContexts.size(), 4);
	}

	@Test
	public void prependSpaceCharToSourceWhenFirstLineInEditorIsEmpty() throws Exception {

		Constructor<ErrorContext> constructor = ErrorContext.class.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class);
		constructor.setAccessible(true);
		ErrorContext errorContext = constructor.newInstance("doesn't matter for this test case", "doesn't matter for this test case", "doesn't matter for this test case", "doesn't matter for this test case", "52");

		PowerMockito.mockStatic(Files.class);
		PowerMockito.when(Files.readAllLines(Mockito.isA(Path.class))).thenReturn(Arrays.asList(SAMPLE_SOURCE.split("\n")));

		ThymeleafExceptionUtils mock = PowerMockito.mock(ThymeleafExceptionUtils.class);
		PowerMockito.when(mock, "getErrorContexts", SAMPLE_TRACE).thenReturn(Arrays.asList(errorContext));
		PowerMockito.when(mock, "getSourceFilePath", ArgumentMatchers.any()).thenReturn(Paths.get("/"));
		PowerMockito.when(mock.getListOfErrorContext(ArgumentMatchers.any())).thenCallRealMethod();

		List<ErrorContext> errorContexts = mock.getListOfErrorContext(SAMPLE_TRACE);


		assertThat(errorContexts.size(), is(1));
		assertThat(errorContexts.iterator().next(), is(errorContext));

		// -1 is due to lines starts with 1, arrays starts with 0
		// Empty lines doesn't have \n as the string is already split by \n so we expect an empty line to be empty string
		Assert.assertTrue("Original source code should have an empty line in 'firstLineNumber'th line", SAMPLE_SOURCE.split("\n")[errorContext.getFirstLineNumber()-1].isEmpty());
		Assert.assertTrue("Source should start with a space character", errorContext.getSourceCode().startsWith(SPACE_CHARACTER));
	}

}
