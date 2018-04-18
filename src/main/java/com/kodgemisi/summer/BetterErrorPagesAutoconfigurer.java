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

package com.kodgemisi.summer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.annotation.PostConstruct;
import javax.servlet.Servlet;
import java.net.URL;
import java.util.List;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Configuration
@BetterErrorPagesEnabled({"dev", "development"})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, SpringTemplateEngine.class })
@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
@AutoConfigureBefore({ ErrorMvcAutoConfiguration.class, WebMvcAutoConfiguration.class })
@AllArgsConstructor
@Slf4j
class BetterErrorPagesAutoconfigurer {

	private final ServerProperties serverProperties;

	private final List<ErrorViewResolver> errorViewResolvers;

	private final SpringTemplateEngine templateEngine;

	/**
	 * <p>
	 * If return type is {@link org.springframework.boot.web.servlet.error.ErrorController} and {@link com.kodgemisi.summer.BetterErrorPagesController}
	 * is not instantiated before {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping} kicks in
	 * then {@link org.springframework.beans.factory.support.AbstractBeanFactory#getType(java.lang.String)}
	 * resolves beanType as ErrorController which eventually results in not evaluating/assuming our class as a controller and not registering {@code /error} mapping.
	 * </p>
	 *
	 * @param errorAttributes autowired
	 * @param packageName     from properties with key {@code better-error-pages.package-name}
	 * @return a configured instance of BetterErrorPagesController
	 */
	@Bean
	BetterErrorPagesController betterErrorPagesController(ErrorAttributes errorAttributes, @Value("${better-error-pages.package-name}") String packageName) {
		return new BetterErrorPagesController(errorAttributes, this.serverProperties.getError(), errorViewResolvers, thymeleafExceptionUtils(packageName));
	}

	@Bean
	ThymeleafExceptionUtils thymeleafExceptionUtils(String packageName) {
		final String projectPath = getProjectPath();
		log.debug("projectPath is determined as {}", projectPath);
		return new ThymeleafExceptionUtils(projectPath, packageName);
	}

	@PostConstruct
	public void addCustomTemplateResolver() {
		final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

		//@formatter:off
		final String templatesPath =
				BetterErrorPagesAutoconfigurer.class.getPackage().getImplementationTitle() +
				"-" +
				BetterErrorPagesAutoconfigurer.class.getPackage().getImplementationVersion() +
				".jar/templates/";
		//@formatter:on

		log.debug("templatesPath: {}", templatesPath);

		templateResolver.setPrefix(templatesPath);
		templateResolver.setCacheable(true);
		templateResolver.setName("Better Error Pages Template Resolver");
		templateEngine.addTemplateResolver(templateResolver);
	}

	private String getProjectPath() {
		URL propertiesFile = ThymeleafExceptionUtils.class.getResource("/application.yml");
		if (propertiesFile == null) {
			log.debug("application.yml cannot be found, checking application-dev.yml...");
			propertiesFile = ThymeleafExceptionUtils.class.getResource("/application-dev.yml");
		}
		if (propertiesFile == null) {
			log.debug("application-dev.yml cannot be found, checking application.properties...");
			propertiesFile = ThymeleafExceptionUtils.class.getResource("/application.properties");
		}
		if (propertiesFile == null) {
			log.debug("application.properties cannot be found, checking application-dev.properties...");
			propertiesFile = ThymeleafExceptionUtils.class.getResource("/application-dev.properties");
		}

		if (propertiesFile == null) {
			throw new IllegalStateException("Your project must have one of following files application.yml, application-dev.yml, application.properties or application-dev.properties for Better Error Pages to work");
		}

		// When host project runs as jar it reports resource path with file: prefix
		// like file:/home/johndoe/development/workspaces/hostprojectname/target/hostprojectname-1.0.0.jar!/BOOT-INF/classes!/application.yml
		if (propertiesFile.getPath().startsWith("file:")) {
			return propertiesFile.getPath().substring(5).split("target")[0] + "src/main/java/";
		}

		// When host project runs from IDE instead of run as a jar then it reports the properties' file path without file: prefix and with
		// like /home/johndoe/development/workspaces/hostprojectname/target/classes/application.yml
		return propertiesFile.getPath().split("target")[0] + "src/main/java/";
	}

}
