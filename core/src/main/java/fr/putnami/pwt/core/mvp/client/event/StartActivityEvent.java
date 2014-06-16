/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.mvp.client.event;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;

public final class StartActivityEvent extends GwtEvent<StartActivityEvent.Handler> {

	public interface Handler extends EventHandler {

		void onStartActivity(StartActivityEvent event);
	}

	public interface HasStartActivityHandlers extends HasHandlers {

		HandlerRegistration addStartActivityHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Activity activity;
	private final Place place;
	private final AcceptsOneWidget container;
	private final IsWidget view;

	private StartActivityEvent(Activity activity, Place place, AcceptsOneWidget container, IsWidget view) {
		this.setSource(place);
		this.activity = activity;
		this.place = place;
		this.container = container;
		this.view = view;
	}

	@Override
	protected void dispatch(StartActivityEvent.Handler handler) {
		handler.onStartActivity(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return StartActivityEvent.TYPE;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public Place getPlace() {
		return this.place;
	}

	public AcceptsOneWidget getContainer() {
		return this.container;
	}

	public IsWidget getView() {
		return view;
	}

	public static StartActivityEvent fire(Activity activity, Place place, AcceptsOneWidget container, IsWidget view) {
		StartActivityEvent event = new StartActivityEvent(activity, place, container, view);
		EventBus.get().fireEventFromSource(event, place);
		return event;
	}

}
