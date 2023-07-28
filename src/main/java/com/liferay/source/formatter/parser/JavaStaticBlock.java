/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.parser;

/**
 * @author Hugo Huijser
 */
public class JavaStaticBlock extends BaseJavaTerm {

	public JavaStaticBlock(String content, int lineNumber) {
		super(null, content, null, lineNumber, false, false, true);
	}

}