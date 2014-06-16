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

public final class MayStopActivityEvent extends GwtEvent<MayStopActivityEvent.Handler> {

	public interface Handler extends EventHandler {

		void onMayStopActivity(MayStopActivityEvent event);
	}

	public interface HasMayStopActivityHandlers extends HasHandlers {

		HandlerRegistration addMayStopActivityHandler(Handler handler);
	}

	public static final Type<Handler> TYPE = new Type<Handler>();

	private final Activity activity;
	private final Place place;
	private final IsWidget view;

	private String message;

	private MayStopActivityEvent(Activity activity, Place place, IsWidget view) {
		super();
		this.activity = activity;
		this.place = place;
		this.view = view;
		this.setSource(place);
	}

	@Override
	protected void dispatch(MayStopActivityEvent.Handler handler) {
		if (this.message == null) {
			handler.onMayStopActivity(this);
		}
	}

	@Override
	public Type<Handler> getAssociatedType() {
		return MayStopActivityEvent.TYPE;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public IsWidget getView() {
		return view;
	}

	public Place getPlace() {
		return place;
	}

	public Activity getActivity() {
		return this.activity;
	}

	public static MayStopActivityEvent fire(Activity activity, Place place, IsWidget view) {
		MayStopActivityEvent event = new MayStopActivityEvent(activity, place, view);
		EventBus.get().fireEventFromSource(event, place);
		return event;
	}

}
