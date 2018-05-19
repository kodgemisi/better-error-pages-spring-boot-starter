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

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.regex.Matcher;

@Getter
class ErrorContext {

	private static final String TEMPLATES_PATH = "templates/";

	public static final String TEMPLATES_SUFFIX = ".html";

	private final FileType fileType;

	private String fullyQualifiedClassName;

	private String packageName;

	/**
	 * Class name or template name in the form of "templates/acme/index.html"
	 */
	private String className;

	/**
	 * For inner classes and non-public classes their file name is different
	 */
	private String fileName;

	private int errorLineNumber;

	@Setter
	private String sourceCodePath;

	@Setter
	private String sourceCode;

	@Setter
	private int firstLineNumber;

	private ErrorContext(String templateName, String errorLineNumber) {
		this.errorLineNumber = Integer.parseInt(errorLineNumber);

		// If the error is in a layout then templateName comes with .html suffix due to non-standard exception reporting of Thymeleaf
		this.fileName = templateName.endsWith(TEMPLATES_SUFFIX) ? TEMPLATES_PATH + templateName : TEMPLATES_PATH + templateName + TEMPLATES_SUFFIX;
		this.packageName = "";
		this.className = fileName;
		this.fullyQualifiedClassName = this.fileName;
		fileType = FileType.HTML;
	}

	private ErrorContext(String fullyQualifiedClassName, String packageName, String className, String fileName, String errorLineNumber) {
		this.fullyQualifiedClassName = fullyQualifiedClassName;
		this.packageName = packageName;
		this.className = className;
		this.fileName = fileName;
		this.errorLineNumber = Integer.parseInt(errorLineNumber);
		fileType = FileType.JAVA;
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
		return new ErrorContext(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
	}

	static ErrorContext extractFromTemplateMatcher(Matcher matcher) {
		return new ErrorContext(matcher.group(1), matcher.group(2));
	}

	public String getId() {
		return fullyQualifiedClassName + ":" + errorLineNumber;
	}

	String getRelativePathOfClass() {
		return this.getPackageName().replaceAll("\\.", File.separator) + File.separator + this.getFileName();
	}

	enum FileType {
		JAVA, HTML
	}
}
