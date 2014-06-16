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
package fr.putnami.pwt.core.event.client;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.SimpleEventBus;

public class EventBus extends SimpleEventBus {

	private static EventBus instance;

	public static EventBus get() {
		if (EventBus.instance == null) {
			EventBus.instance = new EventBus();
		}
		return EventBus.instance;
	}

	private EventBus() {

	}

	@Override
	protected <H> H getHandler(Event.Type<H> type, int index) {
		return super.getHandler(type, index);
	}

	@Override
	protected int getHandlerCount(Event.Type<?> eventKey) {
		return super.getHandlerCount(eventKey);
	}

	@Override
	protected boolean isEventHandled(Event.Type<?> eventKey) {
		return super.isEventHandled(eventKey);
	}

}
