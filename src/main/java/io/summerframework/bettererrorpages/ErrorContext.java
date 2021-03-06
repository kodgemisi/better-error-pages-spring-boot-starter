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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

@Slf4j
@Getter
class ErrorContext {

	enum FileType {
		JAVA, HTML
	}

	private static final String TEMPLATES_SUFFIX = ".html";

	private static final String TEMPLATES_PATH = "templates/";

	private static final char SPACE_CHARACTER = ' ';

	private final FileType fileType;

	private final String fullyQualifiedClassName;

	private final String packageName;

	/**
	 * Class name or template name in the form of "templates/acme/index.html"
	 */
	private final String className;

	/**
	 * For inner classes and non-public classes their file name is different
	 */
	private final String fileName;

	private final String traceLine;

	private int errorLineNumber;

	@Setter
	private String sourceCodePath;

	@Setter
	private String sourceCode;

	@Setter
	private int firstLineNumber;

	private ErrorContext(String traceLine, String templateName, String errorLineNumber) {
		this.traceLine = traceLine;
		this.errorLineNumber = Integer.parseInt(errorLineNumber);

		// If the error is in a layout then templateName comes with .html suffix due to non-standard exception reporting of Thymeleaf
		// Sample layout trace: (template: "layouts/default" - line 17, col 22)
		// However when the error from a custom Thymeleaf component (like a fragment or a Thymeleaf processor) then there is also the prefix.
		// Sample: (template: "class path resource [templates/index.html]" - line 2, col 100)
		// Hence we should check if it already exists before adding the prefix AND the suffix.
		final String calculatedSuffix = templateName.endsWith(TEMPLATES_SUFFIX) ? "" : TEMPLATES_SUFFIX;
		final String calculatedPrefix = templateName.startsWith(TEMPLATES_PATH) ? "" : TEMPLATES_PATH;
		this.fileName = calculatedPrefix + templateName + calculatedSuffix;

		this.packageName = "";
		this.className = fileName;
		this.fullyQualifiedClassName = this.fileName;
		fileType = FileType.HTML;

		this.parseSourceCode();
	}

	private ErrorContext(String traceLine, String fullyQualifiedClassName, String packageName, String className, String fileName, String errorLineNumber) {
		this.traceLine = traceLine;
		this.fullyQualifiedClassName = fullyQualifiedClassName;
		this.packageName = packageName;
		this.className = className;
		this.fileName = fileName;
		this.errorLineNumber = Integer.parseInt(errorLineNumber);
		fileType = FileType.JAVA;

		this.parseSourceCode();
	}


	@ViewTemplateApi
	public String getId() {
		return fullyQualifiedClassName + ":" + errorLineNumber;
	}

	/**
	 * <pre>
	 * matcher.group(0): whole line like at com.kodgemisi.demo.ExampleController.error(ExampleController.java:69)
	 * matcher.group(1): fully qualified class name like like com.kodgemisi.demo.ExampleController
	 * matcher.group(2): package name like com.kodgemisi.demo
	 * matcher.group(3): class name like ExampleController
	 * matcher.group(4): file name like ExampleController.java
	 * matcher.group(5): line number like 69
	 * </pre>
	 */
	static ErrorContext extractFromClassMatcher(Matcher matcher) {
		return new ErrorContext(matcher.group(0), matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
	}

	static ErrorContext extractFromTemplateMatcher(Matcher matcher) {
		return new ErrorContext(matcher.group(0), matcher.group(1), matcher.group(2));
	}

	private void parseSourceCode() {

		try {
			final Path sourceFilePath = getSourceFilePath();

			if (log.isTraceEnabled()) {
				log.trace("sourceFilePath is {}", sourceFilePath);
			}

			final int firstLineNumber = Math.max(this.getErrorLineNumber() - 6, 0);
			final int lastLineNumber = this.getErrorLineNumber() + 5;
			final AtomicInteger index = new AtomicInteger();

			String sourceCode = String.join("\n", Files.readAllLines(sourceFilePath).stream().filter(s -> {
				final int currentLineNumber = index.getAndIncrement();
				return currentLineNumber >= firstLineNumber && currentLineNumber < lastLineNumber;
			}).toArray(String[]::new));

			// When the first line is a new line then the ACE editor ignores it, this is a fix for that behavior.
			if (sourceCode.startsWith("\n")) {
				sourceCode = SPACE_CHARACTER + sourceCode;
			}

			this.setFirstLineNumber(firstLineNumber + 1);
			this.setSourceCode(sourceCode);
			this.setSourceCodePath(sourceFilePath.toString());
		}
		catch (IOException | ClassNotFoundException e) {
			if(log.isDebugEnabled()) {
				log.debug(e.getMessage(), e);
			}
			this.setSourceCode("Cannot parse source file, exception is logged if better error pages logging level is debug.");
			//TODO add parsing stacktrace to ErrorContext in order to make it easier to report errors via only submitting the produced HTML page.
		}
	}

	private Path getSourceFilePath() throws ClassNotFoundException, IOException {

		assert this.fileType != null && this.fullyQualifiedClassName != null && this.className != null
				&& this.fileName != null : "ErrorContext should be fully initialized before this method is called.";

		final String classFullPath;
		if (this.getFileType() == ErrorContext.FileType.JAVA) {

			final URL classUrl = Class.forName(this.getFullyQualifiedClassName()).getResource(this.getClassName() + ".class");

			if (classUrl.getProtocol().equals("jar")) {
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
										.replace(".class", ".java")

										//this addition is only necessary for non-jar executions as the tests only run in this way.
										.replace("/target/test-classes", "/src/test/java");
				//@formatter:on
			}

			if (!classFullPath.endsWith(this.getFileName())) {
				//that means this is an inner class so we need to replace the file name
				return Paths.get(classFullPath.replace(this.getClassName() + ".java", this.getFileName()));
			}
		}
		else {

			// Using classloader to load resource because it searches from the root of classpath even if the class is in some folder
			final URL templateUrl = BetterErrorPagesService.class.getClassLoader().getResource(this.getFileName());

			if (templateUrl == null) {
				throw new IOException(this.getFileName() + " cannot be found.");
			}
			else {
				if (templateUrl.getProtocol().equals("jar")) {
					classFullPath = templateUrl.getPath().replaceFirst("\\/target.*classes\\!", "/src/main/resources").substring(5);
				}
				else {
					classFullPath = templateUrl.getPath();
				}
			}
		}

		return Paths.get(classFullPath);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ErrorContext that = (ErrorContext) o;

		return this.getId().equals(that.getId());

	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
}
