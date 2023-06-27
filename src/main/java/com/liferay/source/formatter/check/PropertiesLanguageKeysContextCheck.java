/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.IOException;
import java.io.StringReader;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Qi Zhang
 */
public class PropertiesLanguageKeysContextCheck extends BaseFileCheck {

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws IOException {

		if (!fileName.endsWith("/content/Language.properties")) {
			return content;
		}

		List<String> allowedSingleWordLanguageKeys = getAttributeValues(
			_ALLOWED_SINGLE_WORD_LANGUAGE_KEYS_KEY, absolutePath);

		int contextDepth = GetterUtil.getInteger(
			getAttributeValue(_CONTEXT_DEPTH_KEY, absolutePath));

		List<String> forbiddenContextNames = getAttributeValues(
			_FORBIDDEN_CONTEXT_NAMES_KEY, absolutePath);

		Properties properties = new Properties();

		properties.load(new StringReader(content));

		Enumeration<String> enumeration =
			(Enumeration<String>)properties.propertyNames();

		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();

			if (key.matches("\\w+") &&
				StringUtil.equalsIgnoreCase(key, properties.getProperty(key)) &&
				!allowedSingleWordLanguageKeys.contains(key)) {

				addMessage(
					fileName,
					StringBundler.concat(
						"The single-word key '", key,
						"' should include a word of context at the end, ",
						"within a [], to indicate specific meaning"));

				continue;
			}

			if ((contextDepth != 0) &&
				((StringUtil.count(key, StringPool.DASH) + 1) !=
					contextDepth)) {

				continue;
			}

			Matcher matcher = _languageKeyPattern.matcher(key);

			if (!matcher.matches()) {
				continue;
			}

			if (properties.containsKey(matcher.group(1))) {
				addMessage(
					fileName,
					StringBundler.concat(
						"The key '", matcher.group(1), "' should include a ",
						"word of context at the end, within a [], to indicate ",
						"specific meaning"));
			}

			String bracketsContent = matcher.group(2);

			if ((bracketsContent.length() == 0) ||
				((bracketsContent.length() == 1) &&
				 !bracketsContent.equals("n") &&
				 !bracketsContent.equals("v")) ||
				(bracketsContent.matches("\\d+") && !key.contains("code") &&
				 !key.contains("status")) ||
				forbiddenContextNames.contains(bracketsContent)) {

				addMessage(
					fileName,
					StringBundler.concat(
						"The context '", bracketsContent,
						"' is invalid in the key '", key, "'"));
			}
		}

		return content;
	}

	private static final String _ALLOWED_SINGLE_WORD_LANGUAGE_KEYS_KEY =
		"allowedSingleWordLanguageKeys";

	private static final String _CONTEXT_DEPTH_KEY = "contextDepth";

	private static final String _FORBIDDEN_CONTEXT_NAMES_KEY =
		"forbiddenContextNames";

	private static final Pattern _languageKeyPattern = Pattern.compile(
		"([\\s\\S]+)\\[([\\s\\S]*)\\]");

}