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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Indicates that annotated method or field is called from a view template like Thymeleaf so the developer should not be tricked by IDEs' unused warnings or
 * "could be private" suggestions.
 *
 * Created on May, 2018
 *
 * @author destan
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({METHOD, FIELD})
public @interface ViewTemplateApi {

}
