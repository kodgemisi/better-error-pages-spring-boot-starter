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
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Slf4j
class BetterErrorPagesEnabledCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		final String[] overrideDefaultProfiles = getPropertyAsArray(context);

		if (overrideDefaultProfiles != null) {
			log.trace("better-error-pages.profiles found {}", overrideDefaultProfiles);

			final boolean enabled = overrideDefaultProfiles.length > 0 && context.getEnvironment().acceptsProfiles(overrideDefaultProfiles);
			log.debug("Better Error Pages Enabled: {}", enabled);

			return enabled;
		}

		final MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(ConditionalOnActiveProfiles.class.getName());

		if (attrs != null) {

			log.trace("better-error-pages.profiles is not found. Checking for 'dev' and 'development' profiles...");

			for (Object value : attrs.get("value")) {
				if (context.getEnvironment().acceptsProfiles((String[]) value)) {
					log.info("Better Error Pages Enabled: true");
					return true;
				}
			}
			log.info("Better Error Pages Enabled: false");
			return false;
		}

		log.error("Better Error Pages Enabled: false");// Should never get here
		return false;
	}

	/**
	 * <p>When YAML list is given an array or list then {@code getProperty("better-error-pages.profiles")} returns null
	 * however {@code getProperty("better-error-pages.profiles[0]")} returns a value.</p>
	 *
	 * <p>This method first tries to extract a comma separated string as an array. If it fails then it tries to resolve a list in a loop.</p>
	 *
	 * @param context
	 * @return
	 * @see <a href="https://stackoverflow.com/a/42371700/878361">https://stackoverflow.com/a/42371700/878361</a>
	 */
	@Nullable
	private String[] getPropertyAsArray(ConditionContext context) {
		final String[] overrideDefaultProfiles = context.getEnvironment().getProperty("better-error-pages.profiles", String[].class);

		if (overrideDefaultProfiles != null) {
			return overrideDefaultProfiles;
		}

		if (context.getEnvironment().getProperty("better-error-pages.profiles[0]") != null) {
			final Set<String> result = new HashSet<>();
			String listItem;
			int index = 0;
			while (true) {
				listItem = context.getEnvironment().getProperty("better-error-pages.profiles[" + index++ + "]");

				if (listItem == null) {
					break;
				}

				result.add(listItem.trim());// trimming seems unnecessary but better safe than sorry
			}
			return result.toArray(new String[0]);
		}
		return null;
	}
}
