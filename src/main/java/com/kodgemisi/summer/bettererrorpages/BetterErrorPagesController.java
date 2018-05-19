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

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BetterErrorPagesController extends BasicErrorController {

	private final ThymeleafExceptionUtils thymeleafExceptionUtils;

	private final BetterErrorPagesService betterErrorPagesService;

	private final String errorPath;

	protected BetterErrorPagesController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers,
			ThymeleafExceptionUtils thymeleafExceptionUtils, BetterErrorPagesService betterErrorPagesService, String errorPath) {
		super(errorAttributes, errorProperties, errorViewResolvers);
		this.thymeleafExceptionUtils = thymeleafExceptionUtils;
		this.betterErrorPagesService = betterErrorPagesService;
		this.errorPath = errorPath;
	}

	@Override
	@RequestMapping(produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
		HttpStatus status = getStatus(request);
		final Map<String, Object> errorAttributes = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML));
		errorAttributes.put("thymeleafExceptionUtils", thymeleafExceptionUtils);
		final Map<String, Object> model = Collections.unmodifiableMap(errorAttributes);
		response.setStatus(status.value());
		ModelAndView modelAndView = resolveErrorView(request, response, status, model);
		return (modelAndView == null ? new ModelAndView("error", model) : modelAndView);
	}

	@Override
	@RequestMapping
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		final ResponseEntity<Map<String, Object>> responseEntity = super.error(request);

		if(responseEntity.getBody().containsKey("trace")) {
			final String errorId = UUID.randomUUID().toString();
			betterErrorPagesService.putErrorTrace(errorId, responseEntity.getBody());
//			responseEntity.getHeaders().add("better-error-pages", request.getRequestURL().toString().replace(request.getRequestURI(), "/error-archive/" + errorId));

			return ResponseEntity
					.status(responseEntity.getStatusCode())
					.headers(responseEntity.getHeaders())
					.header("better-error-pages", getErrorPath(request, errorId))
					.body(responseEntity.getBody());
		}

		return responseEntity;
	}

	@Override
	protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status, Map<String, Object> model) {
		return new ModelAndView("better-error-pages", model);
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

	private String getErrorPath(HttpServletRequest request, String errorId) {
		return request.getRequestURL().toString().replace(request.getRequestURI(), this.errorPath) + "/" + errorId;
	}

}
