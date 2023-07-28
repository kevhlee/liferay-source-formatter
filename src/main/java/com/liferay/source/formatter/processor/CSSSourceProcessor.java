/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.processor;

import java.io.IOException;

import java.util.List;

/**
 * @author Hugo Huijser
 */
public class CSSSourceProcessor extends BaseSourceProcessor {

	@Override
	protected List<String> doGetFileNames() throws IOException {
		return getFileNames(
			new String[] {
				"**/.ivy/**", "**/.sass-cache/**", "**/__MACOSX/**",
				"**/_partial.scss", "**/_styled/css/compat/third_party/**",
				"**/_unstyled/css/**", "**/aui/**", "**/aui_deprecated.css",
				"**/bourbon/**", "**/converter/dependencies/**",
				"**/expected/**", "**/frontend-editors-web/**",
				"**/tools/node**"
			},
			getIncludes());
	}

	@Override
	protected String[] doGetIncludes() {
		return _INCLUDES;
	}

	private static final String[] _INCLUDES = {"**/*.css", "**/*.scss"};

}