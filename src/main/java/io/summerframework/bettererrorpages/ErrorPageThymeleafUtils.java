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

import lombok.extern.slf4j.Slf4j;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Slf4j
public class ErrorPageThymeleafUtils {

	private ErrorPageThymeleafUtils() {
		throw new UnsupportedOperationException("Cannot be instantiated!");
	}

	/**
	 * <p>When calling toString methods of some context objects we may encounter lazy initialization exception which would crash the page.
	 * This method solves that issue.</p>
	 *
	 * <p>For example User's some fields may be lazy and SPRING_SECURITY_CONTEXT throws LazyInitializationException.</p>
	 *
	 * @param o
	 * @return
	 */
	@ViewTemplateApi
	public static String toStringSafe(Object o) {
		try {
			return o.toString();
		}
		catch (Exception e) {
			return "Exception in toString(): " + e.getMessage();
		}
	}

}
