/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.NaturalOrderStringComparator;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.tools.ToolsUtil;
import com.liferay.source.formatter.check.util.BNDSourceUtil;
import com.liferay.source.formatter.check.util.JavaSourceUtil;
import com.liferay.source.formatter.util.FileUtil;
import com.liferay.source.formatter.util.SourceFormatterUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alan Huang
 */
public class PropertiesFeatureFlagsCheck extends BaseFileCheck {

	@Override
	public void setAllFileNames(List<String> allFileNames) {
		_allFileNames = allFileNames;
	}

	@Override
	protected String doProcess(
			String fileName, String absolutePath, String content)
		throws IOException {

		if (!absolutePath.endsWith("/portal-impl/src/portal.properties")) {
			return content;
		}

		_checkUnnecessaryFeatureFlags(fileName, content);

		content = _generateFeatureFlagProperties(content);
		content = _generateFeatureFlagUIProperties(fileName, content);

		return content;
	}

	private void _checkUnnecessaryFeatureFlags(String fileName, String content)
		throws IOException {

		Properties properties = new Properties();

		properties.load(new StringReader(content));

		Enumeration<String> enumeration =
			(Enumeration<String>)properties.propertyNames();

		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();

			if (!key.startsWith("feature.flag.") || !key.endsWith(".type")) {
				continue;
			}

			String value = properties.getProperty(key);

			if (StringUtil.equals(value, "dev")) {
				addMessage(
					fileName,
					"Remove unnecessary property '" + key +
						"', since 'dev' is the default value");
			}
		}
	}

	private String _generateFeatureFlagProperties(String content)
		throws IOException {

		List<String> featureFlagKeys = new ArrayList<>();

		List<String> fileNames = SourceFormatterUtil.filterFileNames(
			_allFileNames, new String[] {"**/test/**"},
			new String[] {
				"**/bnd.bnd", "**/*.java", "**/*.js", "**/*.json", "**/*.jsp",
				"**/*.jspf", "**/*.jsx", "**/*.ts", "**/*.tsx"
			},
			getSourceFormatterExcludes(), true);

		for (String fileName : fileNames) {
			fileName = StringUtil.replace(
				fileName, CharPool.BACK_SLASH, CharPool.SLASH);

			String fileContent = FileUtil.read(new File(fileName));

			if (fileName.endsWith("bnd.bnd")) {
				String liferaySiteInitializerFeatureFlagKey =
					BNDSourceUtil.getDefinitionValue(
						fileContent, "Liferay-Site-Initializer-Feature-Flag");

				if (liferaySiteInitializerFeatureFlagKey == null) {
					continue;
				}

				featureFlagKeys.add(liferaySiteInitializerFeatureFlagKey);
			}
			else if (fileName.endsWith(".java")) {
				featureFlagKeys.addAll(
					_getFeatureFlagKeys(fileContent, _featureFlagPattern1));
				featureFlagKeys.addAll(_getFeatureFlagKeys(fileContent, true));
			}
			else if (fileName.endsWith(".json")) {
				featureFlagKeys.addAll(
					_getFeatureFlagKeys(fileContent, _featureFlagPattern4));
			}
			else if (fileName.endsWith(".jsp") || fileName.endsWith(".jspf")) {
				featureFlagKeys.addAll(
					_getFeatureFlagKeys(fileContent, _featureFlagPattern3));
				featureFlagKeys.addAll(_getFeatureFlagKeys(fileContent, false));
			}
			else {
				featureFlagKeys.addAll(
					_getFeatureFlagKeys(fileContent, _featureFlagPattern3));
			}
		}

		ListUtil.distinct(featureFlagKeys, new NaturalOrderStringComparator());

		Matcher matcher = _featureFlagsPattern.matcher(content);

		if (matcher.find()) {
			String matchedFeatureFlags = matcher.group(2);

			if (featureFlagKeys.isEmpty()) {
				if (matchedFeatureFlags.contains("feature.flag.")) {
					return StringUtil.replaceFirst(
						content, matchedFeatureFlags, StringPool.BLANK,
						matcher.start(2));
				}

				return content;
			}

			List<String> deprecationFeatureFlagKeys = new ArrayList<>();

			Matcher deprecationFeatureFlagKeyMatcher =
				_deprecationFeatureFlagPattern.matcher(content);

			while (deprecationFeatureFlagKeyMatcher.find()) {
				deprecationFeatureFlagKeys.add(
					deprecationFeatureFlagKeyMatcher.group(1));
			}

			StringBundler sb = new StringBundler(featureFlagKeys.size() * 15);

			for (String featureFlagKey : featureFlagKeys) {
				String featureFlagPropertyKey =
					"feature.flag." + featureFlagKey;

				String environmentVariable =
					ToolsUtil.encodeEnvironmentProperty(featureFlagPropertyKey);

				sb.append(StringPool.NEW_LINE);
				sb.append(StringPool.NEW_LINE);
				sb.append(StringPool.FOUR_SPACES);
				sb.append(StringPool.POUND);
				sb.append(StringPool.NEW_LINE);
				sb.append("    # Env: ");
				sb.append(environmentVariable);
				sb.append(StringPool.NEW_LINE);
				sb.append(StringPool.FOUR_SPACES);
				sb.append(StringPool.POUND);
				sb.append(StringPool.NEW_LINE);
				sb.append(StringPool.FOUR_SPACES);
				sb.append(featureFlagPropertyKey);
				sb.append(StringPool.EQUAL);

				if (deprecationFeatureFlagKeys.contains(featureFlagKey)) {
					sb.append(true);
				}
				else {
					sb.append(false);
				}
			}

			if (matchedFeatureFlags.contains("feature.flag.")) {
				content = StringUtil.replaceFirst(
					content, matchedFeatureFlags, sb.toString(),
					matcher.start(2));
			}
			else {
				content = StringUtil.insert(
					content, sb.toString(), matcher.start(2));
			}
		}

		return content;
	}

	private String _generateFeatureFlagUIProperties(
		Map<String, String> properties) {

		StringBundler sb = new StringBundler(properties.size() * 15);

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			String key = entry.getKey();

			String environmentVariable = ToolsUtil.encodeEnvironmentProperty(
				key);

			sb.append(StringPool.NEW_LINE);
			sb.append(StringPool.NEW_LINE);
			sb.append(StringPool.FOUR_SPACES);
			sb.append(StringPool.POUND);
			sb.append(StringPool.NEW_LINE);
			sb.append("    # Env: ");
			sb.append(environmentVariable);
			sb.append(StringPool.NEW_LINE);
			sb.append(StringPool.FOUR_SPACES);
			sb.append(StringPool.POUND);
			sb.append(StringPool.NEW_LINE);
			sb.append(StringPool.FOUR_SPACES);
			sb.append(key);
			sb.append(StringPool.EQUAL);
			sb.append(entry.getValue());
		}

		return sb.toString();
	}

	private String _generateFeatureFlagUIProperties(
			String fileName, String content)
		throws IOException {

		Matcher matcher = _featureFlagUIPattern.matcher(content);

		if (!matcher.find()) {
			return content;
		}

		String matchedFeatureFlags = matcher.group(2);

		if (!matchedFeatureFlags.contains("feature.flag.")) {
			return content;
		}

		Map<String, String> featureFlagUIPropertiesMap = new TreeMap<>(
			new NaturalOrderStringComparator());
		Map<String, String> featureFlagUICommonPropertiesMap = new TreeMap<>(
			new NaturalOrderStringComparator());

		Properties properties = new Properties();

		properties.load(new StringReader(matcher.group()));

		Enumeration<String> enumeration =
			(Enumeration<String>)properties.propertyNames();

		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();

			String value = properties.getProperty(key);

			if (!key.matches("feature\\.flag\\.[A-Z]+-\\d+\\.\\w+")) {
				featureFlagUICommonPropertiesMap.put(key, value);

				continue;
			}

			if (key.endsWith(".type") &&
				StringUtil.contains("beta,deprecation,release", value)) {

				int x = key.lastIndexOf(".");

				for (String enforcePropertyName : _ENFORCE_PROPERTY_NAMES) {
					String featureFlagUIPropertyName =
						key.substring(0, x) + "." + enforcePropertyName;

					if (!properties.containsKey(featureFlagUIPropertyName)) {
						addMessage(
							fileName,
							"Missing property '" + featureFlagUIPropertyName +
								"' in ## Feature Flag UI block");
					}
				}
			}

			featureFlagUIPropertiesMap.put(key, value);
		}

		String featureFlagUIProperties =
			_generateFeatureFlagUIProperties(featureFlagUICommonPropertiesMap) +
				_generateFeatureFlagUIProperties(featureFlagUIPropertiesMap);

		if (!StringUtil.equals(matchedFeatureFlags, featureFlagUIProperties)) {
			return StringUtil.replaceFirst(
				content, matchedFeatureFlags, featureFlagUIProperties,
				matcher.start(2));
		}

		return content;
	}

	private List<String> _getFeatureFlagKeys(
		String content, boolean javaSource) {

		List<String> featureFlagKeys = new ArrayList<>();

		Matcher matcher = _featureFlagPattern2.matcher(content);

		while (matcher.find()) {
			String methodCall = null;

			if (javaSource) {
				methodCall = JavaSourceUtil.getMethodCall(
					content, matcher.start());
			}
			else {
				methodCall = JavaSourceUtil.getMethodCall(
					content.substring(matcher.start()), 0);
			}

			List<String> parameterList = JavaSourceUtil.getParameterList(
				methodCall);

			if (parameterList.isEmpty()) {
				return featureFlagKeys;
			}

			String parameter = null;

			if (parameterList.size() == 1) {
				parameter = parameterList.get(0);
			}
			else {
				parameter = parameterList.get(1);
			}

			if ((parameter != null) && parameter.endsWith(StringPool.QUOTE) &&
				parameter.startsWith(StringPool.QUOTE)) {

				featureFlagKeys.add(StringUtil.unquote(parameter));
			}
		}

		return featureFlagKeys;
	}

	private List<String> _getFeatureFlagKeys(String content, Pattern pattern) {
		List<String> featureFlagKeys = new ArrayList<>();

		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			featureFlagKeys.add(matcher.group(1));
		}

		return featureFlagKeys;
	}

	private static final String[] _ENFORCE_PROPERTY_NAMES = {
		"description", "title"
	};

	private static final Pattern _deprecationFeatureFlagPattern =
		Pattern.compile("feature\\.flag\\.([A-Z]+-\\d+)\\.type=deprecation");
	private static final Pattern _featureFlagPattern1 = Pattern.compile(
		"feature\\.flag[.=]([A-Z]+-\\d+)");
	private static final Pattern _featureFlagPattern2 = Pattern.compile(
		"FeatureFlagManagerUtil\\.isEnabled\\(");
	private static final Pattern _featureFlagPattern3 = Pattern.compile(
		"Liferay\\.FeatureFlags\\['(.+?)'\\]");
	private static final Pattern _featureFlagPattern4 = Pattern.compile(
		"\"featureFlag\": \"(.+?)\"");
	private static final Pattern _featureFlagsPattern = Pattern.compile(
		"(\n|\\A)##\n## Feature Flag\n##(\n\n[\\s\\S]*?)(?=(\n\n##|\\Z))");
	private static final Pattern _featureFlagUIPattern = Pattern.compile(
		"(\n|\\A)##\n## Feature Flag UI\n##(\n\n[\\s\\S]*?)(?=(\n\n##|\\Z))");

	private List<String> _allFileNames;

}