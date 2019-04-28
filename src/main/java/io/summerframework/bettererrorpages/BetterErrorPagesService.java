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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.unbescape.html.HtmlEscape;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Instances of this class are thread-safe.
 * <p>
 * Created on April, 2018
 *
 * @author destan
 */
@Slf4j
class BetterErrorPagesService {

	private static final String SPAN_START = "<span class=\"own-class\" source-id=\"SI\">";

	private static final String SPAN_END = "</span>";

	private static final String ERROR_CONTEXT_MAP_KEY = "ERROR_CONTEXT_MAP";

	private final TraceParser traceParser;

	protected BetterErrorPagesService(final String packageName) {
		this.traceParser = new TraceParser(packageName);
	}

	private static HttpServletRequest getCurrentHttpRequest() {
		final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes instanceof ServletRequestAttributes) {
			return ((ServletRequestAttributes) requestAttributes).getRequest();
		}

		throw new IllegalStateException("Cannot find ServletRequestAttributes");
	}

	private String decorateTraceLineIfCorrespondingErrorContextExists(String line) {

		final String sourceCodeId = getSourceCodeIdFromTraceLine(line);
		final String escapedHtml5 = HtmlEscape.escapeHtml5(line);

		if (sourceCodeId == null) {
			return escapedHtml5;
		}

		return (SPAN_START.replace("SI", sourceCodeId) + escapedHtml5 + SPAN_END);
	}

	/**
	 * @param line a single line from an exception trace
	 * @return corresponding 'code-editor' id in HTML, null if there is no error context for this line.
	 */
	private String getSourceCodeIdFromTraceLine(String line) {

		final String lineAsMapKey = traceParser.getMatchedContent(line);

		if (lineAsMapKey == null) {
			return null;
		}

		// This map is created in getListOfErrorContext
		final Map<String, ErrorContext> errorContextMap = (Map<String, ErrorContext>) getCurrentHttpRequest().getAttribute(ERROR_CONTEXT_MAP_KEY);
		return errorContextMap.get(lineAsMapKey).getId();
	}

	/**
	 * This method must be called after {@link #getListOfErrorContext(String)}.
	 */
	String styledTrace(String trace) {
		if (trace == null) {
			return null;
		}

		//@formatter:off
		return String.join("\n", Arrays.stream(trace.split("\n"))
												.map(this::decorateTraceLineIfCorrespondingErrorContextExists)
												.toArray(String[]::new));
		//@formatter:on

	}

	@NonNull
	List<ErrorContext> getListOfErrorContext(String trace) {

		if (trace == null || trace.trim().isEmpty()) {
			log.warn(
					"Trace is null, this is normal for 404 errors but if error is different and you think there should be a trace please make sure that you have server.error.include-stacktrace=always");
			return Collections.emptyList();
		}

		final List<ErrorContext> errorContexts = traceParser.getErrorContexts(trace);

		final Map<String, ErrorContext> map = errorContexts.stream().collect(Collectors.toMap(ErrorContext::getTraceLine, Function.identity()));
		getCurrentHttpRequest().setAttribute(ERROR_CONTEXT_MAP_KEY, map);

		return errorContexts;
	}

}
