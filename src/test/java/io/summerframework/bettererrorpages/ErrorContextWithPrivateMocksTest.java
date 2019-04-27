package io.summerframework.bettererrorpages;

import io.summerframework.bettererrorpages.ErrorContext;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created on April, 2019
 *
 * @author destan
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ErrorContext.class)
public class ErrorContextWithPrivateMocksTest {

	@org.junit.Test
	public void parseSourceCodeShouldPrefixNewLineAtFirstLineWithEmptySpace() throws Exception {

		final ErrorContext errorContext = PowerMockito.mock(ErrorContext.class);

		final Resource sampleFileAsResource = new ClassPathResource("sampleSourceBlankFirstLine.txt");
		final Path sampleSourcePath = Paths.get(sampleFileAsResource.getURI());

		PowerMockito.when(errorContext, "getSourceFilePath").thenReturn(sampleSourcePath);
		PowerMockito.when(errorContext, "parseSourceCode").thenCallRealMethod();
		PowerMockito.when(errorContext, "setSourceCode", anyString()).thenCallRealMethod();
		PowerMockito.when(errorContext, "getSourceCode").thenCallRealMethod();

		Whitebox.invokeMethod(errorContext, "parseSourceCode");

		assertTrue(errorContext.getSourceCode().startsWith(" "), "There should be a white space instead of new line as the first character");
	}

}