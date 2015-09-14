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
package fr.putnami.pwt.core.mvp.client;

import com.google.common.base.Objects;
import com.google.gwt.place.shared.Place;

public abstract class ViewPlace extends Place {

	private ViewPlace parent;
	private String token;

	public ViewPlace() {
		this(null, null);
	}

	public ViewPlace(String token) {
		this(null, token);
	}

	public ViewPlace(ViewPlace parent) {
		this(parent, null);
	}

	public ViewPlace(ViewPlace parent, String token) {
		this.parent = parent;
		this.token = token;
	}

	public void setParent(ViewPlace parent) {
		this.parent = parent;
	}

	public ViewPlace getParent() {
		return this.parent;
	}

	public String getToken() {
		return this.token;
	}

	/**
	 * @deprecated public only for factory, sould be final field
	 * @param token place token
	 */
	@Deprecated
	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.token, this.parent, this.getClass().getSimpleName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ViewPlace) {
			ViewPlace other = (ViewPlace) obj;
			return Objects.equal(this.token, other.token) && Objects.equal(this.parent, other.parent)
				&& Objects.equal(this.getClass().getSimpleName(), other.getClass().getSimpleName());
		}
		return false;
	}

}
