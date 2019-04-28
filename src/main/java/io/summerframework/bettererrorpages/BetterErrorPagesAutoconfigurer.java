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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.Servlet;
import java.util.List;
import java.util.Set;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Configuration
@ConditionalOnActiveProfiles
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, SpringTemplateEngine.class })
@AutoConfigureBefore({ ErrorMvcAutoConfiguration.class, WebMvcAutoConfiguration.class })
@EnableConfigurationProperties(BetterErrorPagesConfigurationProperties.class)
@AllArgsConstructor
@Slf4j
class BetterErrorPagesAutoconfigurer {

	private final ServerProperties serverProperties;

	private final List<ErrorViewResolver> errorViewResolvers;

	private final BetterErrorPagesConfigurationProperties betterErrorPagesConfigurationProperties;

	/**
	 * <p>
	 * If return type is {@link org.springframework.boot.web.servlet.error.ErrorController} and {@link BetterErrorPagesController}
	 * is not instantiated before {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping} kicks in
	 * then {@link org.springframework.beans.factory.support.AbstractBeanFactory#getType(java.lang.String)}
	 * resolves beanType as ErrorController which eventually results in not evaluating/assuming our class as a controller and not registering {@code /error} mapping.
	 * </p>
	 *
	 * @param requestMappingHandlerMapping
	 * @param errorAttributes autowired
	 * @param archivedErrorPagesService
	 * @param errorPath
	 * @param requestMappingsHolder
	 * @return a configured instance of BetterErrorPagesController
	 */
	@Bean
	BetterErrorPagesController betterErrorPagesController(ErrorAttributes errorAttributes, BetterErrorPagesService betterErrorPagesService,
			ArchivedErrorPagesService archivedErrorPagesService, @Value("${server.error.path:${error.path:/error}}") String errorPath,
			RequestMappingsHolder requestMappingsHolder) {

		final ErrorProperties errorProperties = this.serverProperties.getError();
		errorProperties.setIncludeStacktrace(ErrorProperties.IncludeStacktrace.ALWAYS);
		errorProperties.setIncludeException(true);

		return new BetterErrorPagesController(errorAttributes, errorProperties, errorViewResolvers, betterErrorPagesService,
											  archivedErrorPagesService,
											  requestMappingsHolder, errorPath);
	}

	@Bean
	ArchivedErrorPagesService archivedErrorPagesService() {
		return new ArchivedErrorPagesService(betterErrorPagesConfigurationProperties.getArchiveTimeout());
	}

	@Bean
	RequestMappingsHolder requestMappingsHolder(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		return new RequestMappingsHolder(requestMappingHandlerMapping);
	}

	@Bean
	BetterErrorPagesArchiveController betterErrorPagesArchiveController(ArchivedErrorPagesService archivedErrorPagesService) {
		return new BetterErrorPagesArchiveController(archivedErrorPagesService);
	}

	@Bean
	BetterErrorPagesService thymeleafExceptionUtils() {

		String packageName = betterErrorPagesConfigurationProperties.getPackageName();

		if(StringUtils.isEmpty(packageName)) {
			final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
			scanner.addIncludeFilter(new AnnotationTypeFilter(SpringBootApplication.class));

			Set<BeanDefinition> applicationClasses = scanner.findCandidateComponents("com");//first try this because giving a base significantly improves performance
			if(applicationClasses.isEmpty()) {
				applicationClasses = scanner.findCandidateComponents("");
			}

			if(applicationClasses.isEmpty()) {
				throw new IllegalArgumentException("Cannot find any class annotated with @SpringBootApplication");
			}

			final BeanDefinition beanDefinition = applicationClasses.iterator().next();
			packageName = beanDefinition.getBeanClassName().replaceAll("\\.[A-Z0-9_].*", "");
		}

		log.debug("packageName is determined as {}", packageName);

		return new BetterErrorPagesService(packageName);
	}

}
