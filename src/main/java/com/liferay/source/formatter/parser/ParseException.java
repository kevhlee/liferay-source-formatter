/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.parser;

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Hugo Huijser
 */
public class ParseException extends PortalException {

	public ParseException() {
	}

	public ParseException(String msg) {
		super(msg);
	}

	public ParseException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

	public ParseException(Throwable throwable) {
		super(throwable);
	}

}