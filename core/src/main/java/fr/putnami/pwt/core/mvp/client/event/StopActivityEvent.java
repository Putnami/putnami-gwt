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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;

public final class StopActivityEvent extends GwtEvent<StopActivityEvent.Handler> {

	public interface Handler extends EventHandler {

		void onStopActivity(StopActivityEvent event);
	}

	public interface HasStopActivityHandlers extends HasHandlers {

		HandlerRegistration addStopActivityHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Activity activity;
	private final Place place;
	private final IsWidget view;
	private final boolean cancelActivity;

	private StopActivityEvent(Activity activity, Place place, IsWidget view, boolean cancelActivity) {
		super();
		this.activity = activity;
		this.place = place;
		this.view = view;
		this.cancelActivity = cancelActivity;
		this.setSource(place);
	}

	@Override
	protected void dispatch(StopActivityEvent.Handler handler) {
		handler.onStopActivity(this);
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return StopActivityEvent.TYPE;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public IsWidget getView() {
		return view;
	}

	public Place getPlace() {
		return place;
	}

	public boolean isCancelActivity() {
		return this.cancelActivity;
	}

	public static StopActivityEvent fire(Activity activity, Place place, IsWidget view, boolean cancelActivity) {
		StopActivityEvent event = new StopActivityEvent(activity, place, view, cancelActivity);
		EventBus.get().fireEventFromSource(event, place);
		return event;
	}

}
