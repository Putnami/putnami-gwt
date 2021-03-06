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
package fr.putnami.pwt.core.widget.client.util;

import com.google.gwt.dom.client.Element;

public final class SelectionUtils {

	private SelectionUtils() {
	}

	public static native void disableTextSelectInternal(Element e, boolean disable)
	/*-{
	    if (disable) {
	      e.ondrag = function () { return false; };
	      e.onselectstart = function () { return false; };
	    } else {
	      e.ondrag = null;
	      e.onselectstart = null;
	    }
	  }-*/;
}
