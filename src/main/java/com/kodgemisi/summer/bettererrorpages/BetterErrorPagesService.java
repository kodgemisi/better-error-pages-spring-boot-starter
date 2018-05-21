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

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created on May, 2018
 *
 * @author destan
 */
class BetterErrorPagesService {

	private final long timeout;

	private final Map<String, Map<String, Object>> errorArchive;

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	BetterErrorPagesService(long timeout) {
		this.timeout = timeout;
		this.errorArchive = Collections.synchronizedMap(new HashMap<>());//TODO put a limit like in FIFO fixed size queues

		final Runnable clearJob = this::clearOldErrors;
		scheduler.scheduleAtFixedRate(clearJob, this.timeout, this.timeout, TimeUnit.MILLISECONDS);
	}

	void putErrorTrace(String errorId, Map<String, Object> errorAttributes) {
		errorAttributes.put("archived", true);
		errorAttributes.put("betterErrorPagesErrorId", errorId);
		errorAttributes.put("betterErrorPagesTimestampMs", System.currentTimeMillis());
		errorArchive.put(errorId, errorAttributes);
	}

	Optional<Map<String, Object>> getErrorAttributesById(String id) {
		return Optional.ofNullable(this.errorArchive.get(id));
	}

	private void clearOldErrors() {
		final List<String> idsToBeRemoved = errorArchive.values().stream()
				.filter(errorAttributes -> (System.currentTimeMillis() - ((Long)errorAttributes.get("betterErrorPagesTimestampMs"))) > timeout)
				.map(errorAttributes -> errorAttributes.get("betterErrorPagesErrorId").toString())
				.collect(Collectors.toList());

		// To keep synchronized behavior of the map we need to use only map's methods to modify map content
		// That's why we don't use "map.entrySet().removeIf" here
		idsToBeRemoved.forEach(errorArchive::remove);
	}

	@ViewTemplateApi
	public long getTimeout() {
		return timeout;
	}
}
