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

package io.summerframework.bettererrorpages;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BetterErrorPagesServiceTest {

	private static final String PACKAGE_NAME = "com.kodgemisi";

	private final BetterErrorPagesService betterErrorPagesService = new BetterErrorPagesService(PACKAGE_NAME);

	@Test
	void styledTraceTest() {
		final String sampleTrace = Utils.readSampleFile("sampleTrace.txt");
		final String styledTrace = betterErrorPagesService.styledTrace(sampleTrace);

		assertTrue(styledTrace.contains(
				"<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.DemoClass.error(NonPublicController.java:29)</span>"));
		assertTrue(styledTrace.contains(
				"<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.NonPublicController.demo(NonPublicController.java:18)</span>"));
		assertTrue(styledTrace.contains(
				"<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.DemoClass2.error(NonPublicController.java:36)</span>"));
		assertTrue(styledTrace.contains(
				"<span class=\"own-class\">\tat com.kodgemisi.bettererrorpagesdemo.DemoClass.error(NonPublicController.java:26)</span>"));
	}

	@Test
	void getErrorContextsTest() {

		final String sampleTrace = Utils.readSampleFile("sampleTrace.txt");

		final List<ErrorContext> errorContexts = betterErrorPagesService.getListOfErrorContext(sampleTrace);
		assertEquals(errorContexts.size(), 4);
	}

	@ParameterizedTest
	@NullSource
	@EmptySource
	@ValueSource(strings = { " ", "   ", "\t", "\n" })
	void getErrorContextsWithEmptyParameter(String blankTrace) {

		final List<ErrorContext> emptyErrorContexts = betterErrorPagesService.getListOfErrorContext(blankTrace);
		assertEquals(emptyErrorContexts, Collections.emptyList());
	}

}
