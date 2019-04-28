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

	/**
	 * <p>{@code (?:.+\[)} and {@code *\} parts are to match optional brackets ({@literal [} {@literal ]})</p>
	 * <br>
	 * <p>Template names are sometimes in form <br>
	 *  {@code (template: "class path resource [templates/index.html]" - line 2, col 100)} <br>and sometimes<br>
	 * 	{@code (template: "products/syntaxError" - line 3, col 5)}</p>
	 */
	private final Pattern templateNameRegexPattern = Pattern.compile("\\(template: \"(?:.+\\[){0,1}(.+?)]*\" - line (\\d+), col .+\\)");

	TraceParser(String packageName) {
		classNameRegexPattern = Pattern.compile("at ((" + packageName + "[a-z0-9\\.]*)\\.([A-Z]\\w*)).*\\((.+):(\\d+)\\)");
	}

	List<ErrorContext> getErrorContexts(String trace) {

		// Checking for class file info in trace
		final Matcher matcher = this.parseTraceForClass(trace);
		final List<ErrorContext> errorContexts = new ArrayList<>();

		while (matcher.find()) {

			final ErrorContext errorContext = ErrorContext.extractFromClassMatcher(matcher);

			if(!errorContexts.contains(errorContext)) {
				errorContexts.add(errorContext);
			}
		}

		// Checking for template file info in trace
		final Matcher matcherForTemplateMeta = this.parseTraceForTemplate(trace);

		if (matcherForTemplateMeta.find()) {

			if(log.isDebugEnabled()) {
				log.debug("ErrorContext  for a template exception is found.");
			}

			errorContexts.add(ErrorContext.extractFromTemplateMatcher(matcherForTemplateMeta));
			//No need to parse the rest as they will yield the same file name for template errors
			//Hence we are not looping for the remaining findings of 'matcherForTemplateMeta'
		}

		if(log.isTraceEnabled()) {
			log.trace("Returning ErrorContexts size {}", errorContexts.size());
		}
		return errorContexts;
	}

	String getMatchedContent(String traceLine) {
		final Matcher classMatcher = classNameRegexPattern.matcher(traceLine);
		if(classMatcher.find()) {
			return classMatcher.group(0);
		}

		final Matcher templateMatcher = templateNameRegexPattern.matcher(traceLine);
		if(templateMatcher.find()) {
			return templateMatcher.group(0);
		}

		return null;
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
