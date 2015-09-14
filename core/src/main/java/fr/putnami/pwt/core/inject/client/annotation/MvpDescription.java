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
package fr.putnami.pwt.core.inject.client.annotation;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MvpDescription {
	/**
	 * The display is a <code>AcceptsOneWidget</code> which will receive the view when presented.
	 *
	 * @return displayer
	 */
	Class<? extends AcceptsOneWidget> display() default AcceptsOneWidget.class;

	/**
	 * The place to present when no place are presented
	 *
	 * @return the default place to go
	 */
	Class<? extends Place> defaultPlace() default Place.class;

	/**
	 * Each activity class will be registered in the MvpController with the following :
	 * GWT.&lt;ActivityFactory&gt; create(class)
	 *
	 * @return activities to register
	 */
	Class<?>[] activities() default {};

	/**
	 * Add the MvpController.get().handleCurrentHistory() instruction at the end of the module load
	 *
	 * @return handle the current history token
	 */
	boolean handleCurrentHistory() default true;
}
