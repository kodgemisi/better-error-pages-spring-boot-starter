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
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	/**
	 * matcher.group(0): whole line
	 * matcher.group(1): fully qualified class name
	 * matcher.group(2): package name
	 * matcher.group(3): class name
	 * matcher.group(4): file name
	 * matcher.group(5): line number
	 */
	private final Pattern classNameRegexPattern;

	/**
	 * matcher.group(0): whole line
	 * matcher.group(1): template path
	 * matcher.group(2): line number
	 */
	private final Pattern templateNameRegexPattern = Pattern.compile("\\(template: \"(.+)\" - line (\\d+), col .+\\)");

	protected ThymeleafExceptionUtils(String packageName) {
		this.packageName = packageName;
		classNameRegexPattern = Pattern.compile("at ((" + this.packageName + "[a-z0-9\\.]*)\\.([A-Z]\\w*)).*\\((.+):(\\d+)\\)");
	}

	@ViewTemplateApi
	public String styledTrace(String trace) {
		if(trace == null) {
			return null;
		}
		return colorizeTrace(trace);
	}

	@NonNull
	@ViewTemplateApi
	public List<ErrorContext> getListOfErrorContext(String trace) {

		if(trace == null) {
			log.warn("Trace is null, this is normal for 404 errors but if error is different and you think there should be a trace please make sure that you have server.error.include-stacktrace=always");
			return Collections.emptyList();
		}

		final List<ErrorContext> errorContexts = getErrorContexts(trace);

		for(ErrorContext errorContext : errorContexts) {

			try {
				final Path sourceFilePath = getSourceFilePath(errorContext);

				if(log.isTraceEnabled()) {
					log.trace("sourceFilePath is {}", sourceFilePath);
				}

				final int firstLineNumber = Math.max(errorContext.getErrorLineNumber() - 6, 0);
				final int lastLineNumber = errorContext.getErrorLineNumber() + 5;
				final AtomicInteger index = new AtomicInteger();

				String sourceCode = String.join("\n", Files.readAllLines(sourceFilePath).stream()
						.filter(s -> {
							final int currentLineNumber = index.getAndIncrement();
							return currentLineNumber >= firstLineNumber && currentLineNumber < lastLineNumber;
						})
						.toArray(String[]::new));

				// When the first line is a blank line the ACE editor ignores it, this is a fix for that behavior.
				if(sourceCode.startsWith("\n")) {
					sourceCode = " " + sourceCode;
				}

				errorContext.setFirstLineNumber(firstLineNumber + 1);
				errorContext.setSourceCode(sourceCode);
				errorContext.setSourceCodePath(sourceFilePath.toString());
			}
			catch (IOException e) {
				log.error(e.getMessage(), e);
				errorContext.setSourceCode("Cannot parse source file, exception is logged.");
			}
		}
		return errorContexts;
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
			return "Lazy initialization exception in toString(): " + e.getMessage();
		}
	}

	private String colorizeTrace(String trace) {
		return String.join("\n", Arrays.stream(trace.split("\n"))
				.map(s -> s.contains(packageName) ? (SPAN_START + s + SPAN_END) : s)
				.toArray(String[]::new));
	}

	private List<ErrorContext> getErrorContexts(String trace) {
		final Matcher matcher = classNameRegexPattern.matcher(trace);
		final List<ErrorContext> errorContexts = new ArrayList<>();
		while(matcher.find() && matcher.groupCount() > 0) {
			errorContexts.add(ErrorContext.extractFromClassMatcher(matcher));
		}

		if(errorContexts.isEmpty()) {
			log.trace("Trying to extract ErrorContexts for a template exception...");
			final Matcher matcherForTemplateMeta = templateNameRegexPattern.matcher(trace);

			while(matcherForTemplateMeta.find() && matcherForTemplateMeta.groupCount() > 0) {
				log.debug("ErrorContext  for a template exception is found.");
				errorContexts.add(ErrorContext.extractFromTemplateMatcher(matcherForTemplateMeta));
				break;//No need to parse the rest as they will yield the same file name for template errors
			}
		}

		log.trace("Returning ErrorContexts size {}", errorContexts.size());
		return errorContexts;
	}

	private Path getSourceFilePath(ErrorContext errorContext) throws IOException {
		try {
			final String classFullPath;
			if(errorContext.getFileType() == ErrorContext.FileType.JAVA) {

				final URL classUrl =  Class.forName(errorContext.getFullyQualifiedClassName()).getResource(errorContext.getClassName() + ".class");

				if(classUrl.getProtocol().equals("jar")) {
					//@formatter:off
					classFullPath = classUrl.getPath()
							.replaceFirst("\\/target.*classes\\!", "/src/main/java")
							.replace(".class", ".java")
							.substring(5);// paths of files inside jars are reported with "file:" prefix.
					//@formatter:on
				}
				else {
					//@formatter:off
					classFullPath = classUrl.getPath()
											.replace("/target/classes", "/src/main/java")
											.replace(".class", ".java");
					//@formatter:on
				}

				if(!classFullPath.endsWith(errorContext.getFileName())) {
					//that means this is an inner class so we need to replace the file name
					return Paths.get(classFullPath.replace(errorContext.getClassName() + ".java", errorContext.getFileName()));
				}
			}
			else {

				// Using classloader to load resource because it searches from the root of classpath even if the class is in some folder
				final URL templateUrl = ThymeleafExceptionUtils.class.getClassLoader().getResource(errorContext.getFileName());

				if(templateUrl == null) {
					classFullPath = "";
				}
				else {
					if(templateUrl.getProtocol().equals("jar")) {
						classFullPath = templateUrl.getPath().replaceFirst("\\/target.*classes\\!", "/src/main/resources").substring(5);
					}
					else {
						classFullPath = templateUrl.getPath();
					}
				}
			}

			return Paths.get(classFullPath);
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

}
