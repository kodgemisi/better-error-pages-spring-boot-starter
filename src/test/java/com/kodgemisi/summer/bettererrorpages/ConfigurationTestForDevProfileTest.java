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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"better-error-pages.package-name:com.kodgemisi", "logging.level.com.kodgemisi=trace"}, classes = UserConfiguration.class)
@TestPropertySource(properties = {"spring.profiles.active=dev"})
@EnableAutoConfiguration
public class ConfigurationTestForDevProfileTest {

	@Autowired
	private WebApplicationContext applicationContext;

	@Test
	public void enabledWhenNoExpilicitBetterErrorPagesProfileSet() {
		ThymeleafExceptionUtils thymeleafExceptionUtils = applicationContext.getBean(ThymeleafExceptionUtils.class);
		Assert.assertNotNull(thymeleafExceptionUtils);

		BetterErrorPagesController betterErrorPagesController = applicationContext.getBean(BetterErrorPagesController.class);
		Assert.assertNotNull(betterErrorPagesController);
	}
}
