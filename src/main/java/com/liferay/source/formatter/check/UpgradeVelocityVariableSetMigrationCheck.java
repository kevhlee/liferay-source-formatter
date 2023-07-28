/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.CharPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.source.formatter.check.constants.VelocityMigrationConstants;
import com.liferay.source.formatter.check.util.VelocityMigrationUtil;

/**
 * @author Nícolas Moura
 */
public class UpgradeVelocityVariableSetMigrationCheck
	extends BaseUpgradeVelocityMigrationCheck {

	@Override
	protected String migrateContent(String content) {
		String[] lines = StringUtil.splitLines(content);

		for (String line : lines) {
			if (line.contains(VelocityMigrationConstants.VELOCITY_SET)) {
				String newLine = line.replace(
					VelocityMigrationConstants.VELOCITY_SET, "<#assign");

				newLine = VelocityMigrationUtil.removeFirstParenthesis(newLine);
				newLine = StringUtil.replaceLast(
					newLine, CharPool.CLOSE_PARENTHESIS,
					CharPool.SPACE +
						VelocityMigrationConstants.FREEMARKER_TAG_END);

				content = StringUtil.replace(content, line, newLine);
			}
		}

		return content;
	}

}