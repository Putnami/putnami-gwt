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
package fr.putnami.pwt.core.mvp.client;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.event.client.EventBus;
import fr.putnami.pwt.core.mvp.client.event.MayStopActivityEvent;
import fr.putnami.pwt.core.mvp.client.event.MayStopActivityEvent.HasMayStopActivityHandlers;
import fr.putnami.pwt.core.mvp.client.event.StartActivityEvent;
import fr.putnami.pwt.core.mvp.client.event.StartActivityEvent.HasStartActivityHandlers;
import fr.putnami.pwt.core.mvp.client.event.StopActivityEvent;
import fr.putnami.pwt.core.mvp.client.event.StopActivityEvent.Handler;
import fr.putnami.pwt.core.mvp.client.event.StopActivityEvent.HasStopActivityHandlers;
import fr.putnami.pwt.core.mvp.client.util.MvpUtils;

public class MvpController extends PlaceController implements
PlaceHistoryMapper,
ActivityMapper,
HasStartActivityHandlers,
HasStopActivityHandlers,
HasMayStopActivityHandlers
{

	private static MvpController instance;

	public static MvpController get() {
		if (MvpController.instance == null) {
			MvpController.instance = new MvpController();
		}
		return MvpController.instance;
	}

	private static final String PLACE_CROWLER_DELIMITER = "!";
	private static final String PLACE_SEPARATOR = "/";
	private static final char PLACE_TOKEN_SEPARATOR = '=';

	private final Map<String, ActivityFactory> ACTIVITY_FACTORIES = Maps.newHashMap();

	private final ActivityManager activityManager;
	private final PlaceHistoryHandler historyHandler;

	private Place defaultPlace = Place.NOWHERE;
	private Place currentPlace = Place.NOWHERE;

	private HandlerRegistration historyRegistration;

	protected MvpController() {
		super(EventBus.get());

		this.activityManager = new ActivityManager(this, EventBus.get());
		this.historyHandler = new PlaceHistoryHandler(this);

		this.setDefaultPlace(this.defaultPlace);
	}

	@Override
	public Place getWhere() {
		return this.currentPlace;
	}

	public void goBack() {
		History.back();
	}

	public void goForward() {
		History.forward();
	}

	@Override
	public void goTo(final Place newPlace) {

		if (this.getWhere().equals(newPlace)) {
			return;
		}

		PlaceChangeRequestEvent willChange = new PlaceChangeRequestEvent(newPlace);
		EventBus.get().fireEvent(willChange);
		String warning = willChange.getWarning();
		if (warning == null || Window.confirm(warning)) {
			this.doGo(newPlace);
		}
		else {
			goTo(this.getWhere());
		}
	}

	public void goToDefaultPlace() {
		if (defaultPlace != null) {
			goTo(defaultPlace);
		}
		else {
			handleCurrentHistory();
		}
	}

	public void handleCurrentHistory() {
		this.historyHandler.handleCurrentHistory();
	}

	public void registerActivity(ActivityFactory placeViewMapper) {
		for (String preffix : placeViewMapper.getPlacePrefixes()) {
			if (!preffix.startsWith("!")) {
				preffix = "!" + preffix;
			}
			this.ACTIVITY_FACTORIES.put(preffix, placeViewMapper);
		}
	}

	@Override
	public Activity getActivity(Place place) {
		String key = MvpUtils.getPlacePrefix(place);
		ActivityFactory activityFactory = ACTIVITY_FACTORIES.get(key);
		if (activityFactory != null) {
			return activityFactory.createActivity(place);
		}
		return null;
	}

	@Override
	public Place getPlace(String token) {
		String[] placesToken = token.split(PLACE_SEPARATOR);
		Place result = null;
		for (String placeToken : placesToken) {
			Place localPlace = getSimplePlace(placeToken);
			if (localPlace != null && localPlace instanceof ViewPlace && result instanceof ViewPlace) {
				((ViewPlace) localPlace).setParent((ViewPlace) result);
			}
			result = localPlace;
		}
		return result;
	}

	private Place getSimplePlace(String token) {
		int colonAt = token.indexOf(PLACE_TOKEN_SEPARATOR);
		String prefix = token;
		String rest = null;
		if (colonAt > 0) {
			prefix = token.substring(0, colonAt);
			rest = token.substring(colonAt + 1);
		}
		if (!prefix.startsWith(PLACE_CROWLER_DELIMITER)) {
			prefix = PLACE_CROWLER_DELIMITER + prefix;
		}
		ActivityFactory activityFactory = this.ACTIVITY_FACTORIES.get(prefix);
		if (activityFactory instanceof PlaceTokenizer) {
			return ((PlaceTokenizer) activityFactory).getPlace(rest);
		}
		return null;
	}

	@Override
	public String getToken(Place place) {
		if (place == null) {
			return null;
		}
		String parentToken = null;
		if (place instanceof ViewPlace && ((ViewPlace) place).getParent() != null) {
			parentToken = getToken(((ViewPlace) place).getParent());
		}
		String prefix = MvpUtils.getPlacePrefix(place);
		String token = null;
		ActivityFactory activityFactory = this.ACTIVITY_FACTORIES.get(prefix);
		if (activityFactory instanceof PlaceTokenizer) {
			token = ((PlaceTokenizer) activityFactory).getToken(place);
		}

		String result = "";
		if (parentToken != null) {
			result = parentToken + PLACE_SEPARATOR;
			if (prefix.startsWith("!")) {
				prefix = prefix.substring(1);
			}
		}
		if (token != null) {
			result += prefix.length() == 0 ? token : prefix + PLACE_TOKEN_SEPARATOR + token;
		}
		else {
			result += prefix;
		}
		return result;
	}

	public void setDisplay(AcceptsOneWidget display) {
		this.activityManager.setDisplay(display);
	}

	public void setDefaultPlace(Place defaultPlace) {
		this.defaultPlace = defaultPlace;
		if (this.historyRegistration != null) {
			this.historyRegistration.removeHandler();
		}
		this.historyRegistration = this.historyHandler.register(this, EventBus.get(), this.defaultPlace);

	}

	private void doGo(Place newPlace) {
		this.currentPlace = newPlace;
		EventBus.get().fireEvent(new PlaceChangeEvent(newPlace));
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		EventBus.get().fireEvent(event);
	}

	@Override
	public HandlerRegistration addStopActivityHandler(Handler handler) {
		return EventBus.get().addHandler(StopActivityEvent.TYPE, handler);
	}

	@Override
	public HandlerRegistration addStartActivityHandler(fr.putnami.pwt.core.mvp.client.event.StartActivityEvent.Handler handler) {
		return EventBus.get().addHandler(StartActivityEvent.TYPE, handler);
	}

	@Override
	public HandlerRegistration addMayStopActivityHandler(fr.putnami.pwt.core.mvp.client.event.MayStopActivityEvent.Handler handler) {
		return EventBus.get().addHandler(MayStopActivityEvent.TYPE, handler);
	}
}
