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

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BetterErrorPagesController extends BasicErrorController {

	private final BetterErrorPagesService betterErrorPagesService;

	private final ArchivedErrorPagesService archivedErrorPagesService;

	private final RequestMappingsHolder requestMappingsHolder;

	private final String errorPath;

	protected BetterErrorPagesController(ErrorAttributes errorAttributes, ErrorProperties errorProperties, List<ErrorViewResolver> errorViewResolvers,
			BetterErrorPagesService betterErrorPagesService, ArchivedErrorPagesService archivedErrorPagesService,
			RequestMappingsHolder requestMappingsHolder, String errorPath) {
		super(errorAttributes, errorProperties, errorViewResolvers);
		this.betterErrorPagesService = betterErrorPagesService;
		this.archivedErrorPagesService = archivedErrorPagesService;
		this.requestMappingsHolder = requestMappingsHolder;
		this.errorPath = errorPath;
	}

	@Override
	protected ModelAndView resolveErrorView(HttpServletRequest request, HttpServletResponse response, HttpStatus status, Map<String, Object> model) {
		return new ModelAndView("better-error-pages", model);
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

	@Override
	@RequestMapping(produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {

		final HttpStatus status = getStatus(request);
		final Map<String, Object> errorAttributes = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML));

		if (request.getDispatcherType() == DispatcherType.FORWARD) {
			// Coming from BetterErrorPagesArchiveController
			final Map<String, Object> archivedErrorAttributes = (Map<String, Object>) request.getAttribute(BetterErrorPagesArchiveController.MODEL_KEY);

			if (archivedErrorAttributes != null) {
				errorAttributes.putAll(archivedErrorAttributes);
			}
		}

		final Map<String, Object> model = new HashMap<>(errorAttributes);

		if (model.containsKey("trace")) {
			final String trace = (String) model.get("trace");
			// Caveat: `getListOfErrorContext` must be called before `styledTrace`. Call order is important. See javadoc of `styledTrace`.
			model.put("errorContextList", betterErrorPagesService.getListOfErrorContext(trace));
			model.put("styledTrace", betterErrorPagesService.styledTrace(trace));
		}

		if (status.equals(HttpStatus.NOT_FOUND)) {
			model.put("mappings", requestMappingsHolder.getMappings());
		}

		response.setStatus(status.value());
		final ModelAndView modelAndView = resolveErrorView(request, response, status, model);
		return (modelAndView == null ? new ModelAndView("error", model) : modelAndView);
	}

	@Override
	@RequestMapping
	@ResponseBody
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		final ResponseEntity<Map<String, Object>> responseEntity = super.error(request);

		if (responseEntity.hasBody() && responseEntity.getBody().containsKey("trace")) {
			final String errorId = UUID.randomUUID().toString();
			archivedErrorPagesService.putErrorTrace(errorId, responseEntity.getBody());

			//@formatter:off
			return ResponseEntity
					.status(responseEntity.getStatusCode())
					.headers(responseEntity.getHeaders())
					.header("better-error-pages", getErrorPath(request, errorId))
					.body(responseEntity.getBody());
			//@formatter:on
		}

		return responseEntity;
	}

	private String getErrorPath(HttpServletRequest request, String errorId) {
		return request.getRequestURL().toString().replace(request.getRequestURI(), this.errorPath) + "/" + errorId;
	}

}
