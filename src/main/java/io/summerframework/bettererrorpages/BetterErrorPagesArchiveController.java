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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created on April, 2018
 *
 * @author destan
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
class BetterErrorPagesArchiveController {

	static final String MODEL_KEY = "BETTER_ERROR_PAGES_ARCHIVED_ATTRIBUTES";

	private final ArchivedErrorPagesService archivedErrorPagesService;

	protected BetterErrorPagesArchiveController(ArchivedErrorPagesService archivedErrorPagesService) {
		this.archivedErrorPagesService = archivedErrorPagesService;
	}

	@GetMapping(value = "/{id}", produces = "text/html")
	String errorHtml(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id,
			@Value("${server.error.path:${error.path:/error}}") String errorPath) {

		final Map<String, Object> errorAttributes = archivedErrorPagesService.getErrorAttributesById(id).orElseThrow(ErrorArchiveNotFoundException::new);

		errorAttributes.put("betterErrorPagesTimestampMs", Long.MAX_VALUE);// prevent removal of seen archives

		request.setAttribute(MODEL_KEY, errorAttributes);

		return "forward:" + errorPath;
	}

	@ExceptionHandler(ErrorArchiveNotFoundException.class)
	String handle404(Model model, HttpServletResponse response) {
		model.addAttribute("timeout", archivedErrorPagesService.getTimeout());
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return "better-error-pages-404";
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	static class ErrorArchiveNotFoundException extends RuntimeException {

	}

}
