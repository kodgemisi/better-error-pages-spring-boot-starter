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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BetterErrorPagesArchiveController {

	private final ThymeleafExceptionUtils thymeleafExceptionUtils;

	private final BetterErrorPagesService betterErrorPagesService;

	protected BetterErrorPagesArchiveController(ThymeleafExceptionUtils thymeleafExceptionUtils, BetterErrorPagesService betterErrorPagesService) {
		this.thymeleafExceptionUtils = thymeleafExceptionUtils;
		this.betterErrorPagesService = betterErrorPagesService;
	}

	@GetMapping(value = "/{id}", produces = "text/html")
	public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) {
		final HttpStatus status = getStatus(request);
		response.setStatus(status.value());

		final Optional<Map<String, Object>> errorAttributesOptional = betterErrorPagesService.getErrorAttributesById(id);

		final Map<String, Object> errorAttributes = errorAttributesOptional.orElseThrow(ErrorArchiveNotFoundException::new);
		errorAttributes.put("betterErrorPagesTimestampMs", Long.MAX_VALUE);// prevent removal of seen archives

		final Map<String, Object> model = new HashMap<>();
		model.put("thymeleafExceptionUtils", thymeleafExceptionUtils);
		model.putAll(errorAttributes);

		return new ModelAndView("better-error-pages", model);
	}

	@ExceptionHandler(ErrorArchiveNotFoundException.class)
	String handle404(Model model, HttpServletResponse response) {
		model.addAttribute("timeout", betterErrorPagesService.getTimeout());
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return "better-error-pages-404";
	}

	/**
	 * From {@link org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController#getStatus(javax.servlet.http.HttpServletRequest)}
	 *
	 * @param request
	 * @return
	 */
	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		if (statusCode == null) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
		try {
			return HttpStatus.valueOf(statusCode);
		}
		catch (Exception ex) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	static class ErrorArchiveNotFoundException extends RuntimeException {

	}

}
