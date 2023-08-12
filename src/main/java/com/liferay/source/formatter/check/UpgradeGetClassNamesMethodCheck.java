/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.portal.kernel.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tamyris Bernardo
 */
public class UpgradeGetClassNamesMethodCheck
	extends BaseUpgradeMatcherReplacementCheck {

	@Override
	protected String formatMatcherIteration(
		String content, String newContent, Matcher matcher) {

		String methodStart = matcher.group();

		if (!hasClassOrVariableName("Indexer", content, content, methodStart)) {
			return content;
		}

		return StringUtil.replace(
			newContent, methodStart,
			StringUtil.replace(
				methodStart, "getClassNames", "getSearchClassNames"));
	}

	@Override
	protected Pattern getPattern() {
		return Pattern.compile("\\w+\\.getClassNames\\(");
	}

	@Override
	protected String[] getValidExtensions() {
		return new String[] {"java", "jspf"};
	}

}