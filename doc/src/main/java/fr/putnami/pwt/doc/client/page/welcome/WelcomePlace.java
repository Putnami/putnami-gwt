/**
 * This file is part of pwt-doc.
 *
 * pwt-doc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt-doc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt-doc.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.doc.client.page.welcome;

import com.google.gwt.place.shared.PlaceTokenizer;

import fr.putnami.pwt.core.mvp.client.ViewPlace;
import fr.putnami.pwt.core.mvp.client.annotation.ActivityDescrition;

@ActivityDescrition(view = WelcomePage.class)
public class WelcomePlace extends ViewPlace implements PlaceTokenizer<WelcomePlace> {

	@Override
	public WelcomePlace getPlace(String token) {
		return null;
	}

	@Override
	public String getToken(WelcomePlace place) {
		return null;
	}
}
