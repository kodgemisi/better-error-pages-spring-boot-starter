package io.summerframework.bettererrorpages;

import com.kodgemisi.bettererrorpagesdemo.DemoClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created on April, 2019
 *
 * @author destan
 */
class ErrorContextTest {

	@Test
	void constructorCalledFromExtractFromClassMatcher() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

		final Constructor<ErrorContext> ctor = ErrorContext.class.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class, String.class);
		ctor.setAccessible(true);

		final Class<?> clazz = DemoClass.class;
		final String fullyQualifiedClassName = clazz.getCanonicalName();
		final String packageName = clazz.getPackage().getName();
		final String className = clazz.getSimpleName();
		final String fileName = "DemoClass.java";
		final String errorLineNumber = "8";

		final ErrorContext errorContext = ctor.newInstance("full trace", fullyQualifiedClassName, packageName, className, fileName, errorLineNumber);

		assertEquals(8, errorContext.getErrorLineNumber());
		assertEquals(errorContext.getSourceCodePath(), errorContext.getSourceCodePath());
		assertTrue(errorContext.getSourceCode().contains("class DemoClass {"));
		assertTrue(errorContext.getSourceCodePath().contains("/src/test/java/com/kodgemisi/bettererrorpagesdemo/DemoClass.java"));
		assertEquals(ErrorContext.FileType.JAVA, errorContext.getFileType());
	}

	@ParameterizedTest
	@ValueSource(strings = { "products/list", "products/list.html" })
	void constructorCalledFromExtractFromTemplateMatcher(String templateName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

		final Constructor<ErrorContext> ctor = ErrorContext.class.getDeclaredConstructor(String.class, String.class, String.class);
		ctor.setAccessible(true);

		final ErrorContext errorContext = ctor.newInstance("full trace", templateName, "13");

		assertEquals(13, errorContext.getErrorLineNumber());
		assertEquals("templates/products/list.html", errorContext.getFullyQualifiedClassName());
		assertEquals("templates/products/list.html", errorContext.getClassName());
		assertEquals("templates/products/list.html", errorContext.getFileName());
		assertEquals("", errorContext.getPackageName());
		assertEquals(ErrorContext.FileType.HTML, errorContext.getFileType());
		assertTrue(errorContext.getSourceCode().contains("This is a test template"));
		assertTrue(errorContext.getSourceCodePath().contains("/target/test-classes/templates/products/list.html"));
	}

	@Test
	void getSourceFilePathFromRegularClass() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

		final Method method = ErrorContext.class.getDeclaredMethod("getSourceFilePath");
		method.setAccessible(true);

		final Constructor<ErrorContext> ctor = ErrorContext.class.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class, String.class);
		ctor.setAccessible(true);

		final Class<?> clazz = BetterErrorPagesArchiveController.class;
		final String fullyQualifiedClassName = clazz.getCanonicalName();
		final String packageName = clazz.getPackage().getName();
		final String className = clazz.getSimpleName();
		final String fileName = "BetterErrorPagesArchiveController.java";
		final String errorLineNumber = "52";

		final ErrorContext errorContext = ctor.newInstance("full trace", fullyQualifiedClassName, packageName, className, fileName, errorLineNumber);

		method.invoke(errorContext);

		assertTrue(errorContext.getSourceCodePath().contains("/src/main/java/io/summerframework/bettererrorpages/BetterErrorPagesArchiveController.java"));
	}

	@Test
	void getSourceFilePathFromTestClass() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

		final Method method = ErrorContext.class.getDeclaredMethod("getSourceFilePath");
		method.setAccessible(true);

		final Constructor<ErrorContext> ctor = ErrorContext.class.getDeclaredConstructor(String.class, String.class, String.class, String.class, String.class, String.class);
		ctor.setAccessible(true);

		final Class<?> clazz = DemoClass.class;
		final String fullyQualifiedClassName = clazz.getCanonicalName();
		final String packageName = clazz.getPackage().getName();
		final String className = clazz.getSimpleName();
		final String fileName = "DemoClass.java";
		final String errorLineNumber = "8";

		final ErrorContext errorContext = ctor.newInstance("full trace", fullyQualifiedClassName, packageName, className, fileName, errorLineNumber);

		method.invoke(errorContext);

		assertTrue(errorContext.getSourceCodePath().contains("/src/test/java/com/kodgemisi/bettererrorpagesdemo/DemoClass.java"));
	}

}