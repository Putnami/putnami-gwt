/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.editor.client.util;

import com.google.common.collect.Iterables;

import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.Path.PathElement;

public final class PathUtils {

	private PathUtils() {
	}

	public static Path evalPath(String pathString) {
		if (pathString == null || pathString.length() == 0) {
			return new Path();
		}
		Path path = new Path();
		String[] pathElements = pathString.split("\\.");
		for (String pathToken : pathElements) {
			String elementName = pathToken;
			Integer indexKey = null;
			int brakeIndex = pathToken.indexOf('[');
			if (brakeIndex != -1) {
				elementName = pathToken.substring(0, brakeIndex);
				indexKey = Integer.valueOf(pathToken.substring(brakeIndex + 1, pathToken.length() - 1));
			}
			path.add(new PathElement(elementName, indexKey));
		}
		return path;
	}

	public static String concatPath(String parentPath, String path) {
		StringBuilder sb = new StringBuilder();
		if (parentPath != null && parentPath.length() > 0) {
			sb.append(parentPath).append(Path.SEPARATOR_PATH);
		}
		sb.append(path);
		return sb.toString();
	}

	public static boolean isRoot(String path) {
		return path == null || Path.ROOT_PATH.equals(path);
	}

	public static boolean isRoot(Path path) {
		return path == null || path.isRoot();
	}

	public static boolean isCollection(Path path) {
		return path != null && !path.isEmpty() && Iterables.getLast(path).getIndexKey() != null;
	}
}
