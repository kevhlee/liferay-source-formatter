/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.exception;

/**
 * @author Nícolas Moura
 */
public class UpgradeCatchAllException extends Exception {

	public UpgradeCatchAllException(String message) {
		super(message);
	}

}