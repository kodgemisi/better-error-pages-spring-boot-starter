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
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created on April, 2018
 *
 * @author destan
 * @author sedooe
 */
@ConfigurationProperties(prefix = "better-error-pages")
@Getter
@Setter
class BetterErrorPagesConfigurationProperties {

	/**
	 * A package name whose classes' source code will be parsed and displayed in error pages.
	 */
	private String packageName;

	/**
	 * Override default profiles to enable Better Error Pages.
	 */
	private String[] profiles = {"dev", "development"};

	/**
	 * Timeout for archived rest endpoint errors to be cleared if never accessed.
	 */
	private long archiveTimeout = 900000;
}
