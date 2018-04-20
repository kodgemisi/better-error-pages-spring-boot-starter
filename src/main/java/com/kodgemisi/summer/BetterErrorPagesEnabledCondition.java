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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;

@Slf4j
class BetterErrorPagesEnabledCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(BetterErrorPagesEnabled.class.getName());

		final String overrideDefaultProfiles = context.getEnvironment().getProperty("better-error-pages.profiles");
		if(overrideDefaultProfiles != null) {

			log.trace("better-error-pages.profiles found {}", overrideDefaultProfiles);

			if(overrideDefaultProfiles.trim().isEmpty()) {
				throw new IllegalArgumentException("better-error-pages.profiles cannot be empty. Either delete the property or give it a valid value.");
			}

			String[] customProfiles = Arrays.stream(overrideDefaultProfiles.split(",")).filter(s -> !s.trim().isEmpty()).map(String::trim).toArray(String[]::new);

			final boolean enabled = customProfiles.length > 0 && context.getEnvironment().acceptsProfiles(customProfiles);
			log.debug("Better Error Pages Enabled: {}", enabled);

			return enabled;
		}
		else if (attrs != null) {

			log.trace("better-error-pages.profiles is not found. Checking for 'dev' and 'development' profiles...");

			for (Object value : attrs.get("value")) {
				if (context.getEnvironment().acceptsProfiles((String[]) value)) {
					log.debug("Better Error Pages Enabled: true");
					return true;
				}
			}
			log.debug("Better Error Pages Enabled: false");
			return false;
		}

		log.error("Better Error Pages Enabled: false");// Should never get here
		return false;
	}
}