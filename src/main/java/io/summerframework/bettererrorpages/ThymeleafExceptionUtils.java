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

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * Instances of this class are thread-safe.
 *
 * Created on April, 2018
 *
 * @author destan
 */
@Slf4j
public class ThymeleafExceptionUtils {

	private static final String SPAN_START = "<span class=\"own-class\">";

	private static final String SPAN_END = "</span>";

	private final String packageName;

	private final TraceParser traceParser;

	protected ThymeleafExceptionUtils(final String packageName) {
		this.packageName = packageName;
		this.traceParser = new TraceParser(packageName);
	}

	@ViewTemplateApi
	public String styledTrace(String trace) {
		if(trace == null) {
			return null;
		}
		return String.join("\n", Arrays.stream(trace.split("\n"))
				.map(s -> s.contains(packageName) ? (SPAN_START + s + SPAN_END) : s)
				.toArray(String[]::new));
	}

	@NonNull
	@ViewTemplateApi
	public List<ErrorContext> getListOfErrorContext(String trace) {

		if(trace == null || trace.trim().isEmpty()) {
			log.warn("Trace is null, this is normal for 404 errors but if error is different and you think there should be a trace please make sure that you have server.error.include-stacktrace=always");
			return Collections.emptyList();
		}

		return traceParser.getErrorContexts(trace);
	}

	/**
	 * <p>When calling toString methods of some context objects we may encounter lazy initialization exception which would crash the page.
	 * This method solves that issue.</p>
	 *
	 * <p>For example User's some fields may be lazy and SPRING_SECURITY_CONTEXT throws LazyInitializationException.</p>
	 * @param o
	 * @return
	 */
	@ViewTemplateApi
	public String toStringSafe(Object o) {
		try {
			return o.toString();
		}
		catch (Exception e) {
			return "Exception in toString(): " + e.getMessage();
		}
	}

}
