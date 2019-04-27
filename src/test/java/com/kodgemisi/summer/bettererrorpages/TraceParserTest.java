package com.kodgemisi.summer.bettererrorpages;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created on April, 2019
 *
 * @author destan
 */
class TraceParserTest {

	@Test
	void getErrorContexts() {

		final String sampleTrace = Utils.readSampleFile("sampleTrace.txt");
		final TraceParser traceParser = new TraceParser("com.kodgemisi");

		final List<ErrorContext> errorContexts = traceParser.getErrorContexts(sampleTrace);

		assertEquals(errorContexts.size(), 4);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/traceLinesSuccess.csv", numLinesToSkip = 1)
	void parseTraceForClassShouldSucceed(String targetPackage, String wholeLine, String fullyQualifiedName, String packageName, String className,
			String fileName, String lineNumber) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		final Method method = Utils.getParseTraceForClassMethod();
		final TraceParser traceParser = new TraceParser(targetPackage);
		final Matcher matcher = (Matcher) method.invoke(traceParser, wholeLine);

		assertTrue(matcher.find(), "regex should match");
		assertTrue(matcher.groupCount() > 0);

		assertEquals(fullyQualifiedName, matcher.group(1), "fully qualified class name");
		assertEquals(packageName, matcher.group(2), "package name");
		assertEquals(className, matcher.group(3), "class name");
		assertEquals(fileName, matcher.group(4), "file name");
		assertEquals(lineNumber, matcher.group(5), "line number");
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/traceLinesFail.csv", numLinesToSkip = 1)
	void parseTraceForClassShouldFailToParse(String targetPackage, String wholeLine) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		final Method method = Utils.getParseTraceForClassMethod();
		final TraceParser traceParser = new TraceParser(targetPackage);
		final Matcher matcher = (Matcher) method.invoke(traceParser,wholeLine);

		assertFalse(matcher.find(), "regex should NOT match");
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/traceLinesForTemplateSuccess.csv", numLinesToSkip = 1, delimiter = ';')
	void parseTraceForTemplateShouldSucceed(String wholeLine, String templateName, String lineNumber) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

		final Method method = Utils.getparseTraceForTemplateMethod();
		final TraceParser traceParser = new TraceParser("doesnt.matter");
		final Matcher matcher = (Matcher) method.invoke(traceParser, wholeLine);

		assertTrue(matcher.find(), "regex should match");
		assertTrue(matcher.groupCount() > 0);

		assertEquals(templateName, matcher.group(1), "template name");
		assertEquals(lineNumber, matcher.group(2), "line number");
	}

}