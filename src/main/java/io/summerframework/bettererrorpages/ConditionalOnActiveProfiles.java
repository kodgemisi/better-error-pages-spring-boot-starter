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

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Created on April, 2018
 *
 * @author sedooe
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(BetterErrorPagesEnabledCondition.class)
public @interface ConditionalOnActiveProfiles {

	/**
	 * The set of profiles for which the annotated component should be registered.
	 */
	String[] value() default {"dev", "development"};

}
