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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

	public static void main(String[] args) {
		SpringApplication.run(UserConfiguration.class, args);
	}

	/**
	 * Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'org.springframework.boot.autoconfigure.web.ErrorProperties' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
	 * @return
	 */
	@Bean
	ErrorProperties errorProperties() {
		return new ErrorProperties();
	}

}