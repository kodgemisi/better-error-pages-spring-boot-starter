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

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Instances of this class are thread-safe.</p>
 *
 * Created on April, 2019
 *
 * @author destan
 */
@Slf4j
class TraceParser {

	private final Pattern classNameRegexPattern;

	private final Pattern templateNameRegexPattern = Pattern.compile("\\(template: \"(.+)\" - line (\\d+), col .+\\)");

	TraceParser(String packageName) {
		classNameRegexPattern = Pattern.compile("at ((" + packageName + "[a-z0-9\\.]*)\\.([A-Z]\\w*)).*\\((.+):(\\d+)\\)");
	}

	List<ErrorContext> getErrorContexts(String trace) {

		final Matcher matcher = this.parseTraceForClass(trace);
		final List<ErrorContext> errorContexts = new ArrayList<>();

		while (matcher.find() && matcher.groupCount() > 0) {
			errorContexts.add(ErrorContext.extractFromClassMatcher(matcher));
		}

		if (errorContexts.isEmpty()) {

			if(log.isTraceEnabled()) {
				log.trace("Trying to extract ErrorContexts for a template exception...");
			}

			final Matcher matcherForTemplateMeta = this.parseTraceForTemplate(trace);

			while (matcherForTemplateMeta.find() && matcherForTemplateMeta.groupCount() > 0) {

				if(log.isDebugEnabled()) {
					log.debug("ErrorContext  for a template exception is found.");
				}

				errorContexts.add(ErrorContext.extractFromTemplateMatcher(matcherForTemplateMeta));
				break;//No need to parse the rest as they will yield the same file name for template errors
			}
		}

		if(log.isTraceEnabled()) {
			log.trace("Returning ErrorContexts size {}", errorContexts.size());
		}
		return errorContexts;
	}

	/**
	 * <p>Parses lines in format {@code at <packageName>.SomeClass.someMethod(SomeClass.java:NN)}</p>
	 *
	 * <pre>
	 * matcher.group(0): whole line
	 * matcher.group(1): fully qualified class name
	 * matcher.group(2): package name
	 * matcher.group(3): class name
	 * matcher.group(4): file name
	 * matcher.group(5): line number
	 * </pre>
	 */
	private Matcher parseTraceForClass(String trace) {
		return classNameRegexPattern.matcher(trace);
	}

	/**
	 * <p>Parses lines in template exception trace with template name.</p>
	 *
	 * <pre>
	 * matcher.group(0): whole line
	 * matcher.group(1): template path
	 * matcher.group(2): line number
	 * </pre>
	 */
	private Matcher parseTraceForTemplate(String trace) {
		return templateNameRegexPattern.matcher(trace);
	}

}
