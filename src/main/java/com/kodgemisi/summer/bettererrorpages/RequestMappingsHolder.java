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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created on May, 2018
 *
 * @author destan
 */
@Slf4j
class RequestMappingsHolder implements ApplicationListener<ContextRefreshedEvent> {

	private final RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Getter
	private Collection<MappingInfo> mappings;

	RequestMappingsHolder(RequestMappingHandlerMapping requestMappingHandlerMapping) {
		this.requestMappingHandlerMapping = requestMappingHandlerMapping;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		final List<MappingInfo> mappingInfos = new ArrayList<>();

		requestMappingHandlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
			requestMappingInfo.getPatternsCondition().getPatterns().forEach(pattern -> {
				mappingInfos.add(new MappingInfo(pattern, requestMappingInfo.getMethodsCondition().toString(), handlerMethod.getBeanType().getSimpleName(), handlerMethod.getMethod().getName()));
			});
		});

		Collections.sort(mappingInfos);
		mappings = Collections.unmodifiableCollection(mappingInfos);
	}

	@AllArgsConstructor
	@Getter
	static class MappingInfo implements Comparable<MappingInfo> {

		private final String pattern;
		private final String httpVerb;
		private final String className;
		private final String methodName;

		@ViewTemplateApi
		public String getCombinedMethodName() {
			return className + "#" + methodName;
		}

		@Override
		public int compareTo(MappingInfo o) {
			return this.pattern.compareTo(o.pattern);
		}
	}

}
