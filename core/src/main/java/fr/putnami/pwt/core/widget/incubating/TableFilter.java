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
package fr.putnami.pwt.core.widget.incubating;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.util.PathUtils;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.util.ModelUtils;
import fr.putnami.pwt.core.widget.client.Button;
import fr.putnami.pwt.core.widget.client.Button.Type;
import fr.putnami.pwt.core.widget.client.Container;
import fr.putnami.pwt.core.widget.client.InputText;
import fr.putnami.pwt.core.widget.client.TableTH;
import fr.putnami.pwt.core.widget.client.base.AbstractTableColumnAspect;
import fr.putnami.pwt.core.widget.client.base.HasHeaderCell;
import fr.putnami.pwt.core.widget.client.constant.IconType;
import fr.putnami.pwt.core.widget.client.event.ButtonEvent;

public class TableFilter<T> extends AbstractTableColumnAspect<T> implements HasHeaderCell {

	private final Button<T> button = new Button<T>();

	private Boolean activate = false;

	private String filterValue;
	private final InputText inputText = new InputText();
	private TableTH<?> headerCell;

	private HandlerRegistration valueChangeRegistration;

	public TableFilter() {
		endConstruct();
	}

	protected TableFilter(TableFilter<T> source) {
		super(source);
		endConstruct();
	}

	private void endConstruct() {
		button.setIconType(IconType.FILTER);
		button.setType(Type.ICON);
		button.setSize(Button.Size.SMALL);

		button.addButtonHandler(new ButtonEvent.Handler() {
			@Override
			public void onButtonAction(ButtonEvent event) {
				toggle();
			}
		});
	}

	@Override
	public AbstractTableColumnAspect<T> cloneAspect() {
		return new TableFilter<T>(this);
	}

	@Override
	public Widget asWidget() {
		return button;
	}

	@Override
	public void setHeaderCell(TableTH<?> headerCell) {
		this.headerCell = headerCell;
	}

	private void toggle() {
		this.activate = !activate;
		redraw();
		getDriver().resetDisplay();
	}

	public void redraw() {
		button.setIconType(IconType.FILTER);
		button.setActive(activate);

		Container row = (Container) ((Widget) headerCell).getParent();

		headerCell.clear();
		if (valueChangeRegistration != null) {
			valueChangeRegistration.removeHandler();
			valueChangeRegistration = null;
		}
		boolean showFilterRow = false;
		if (activate) {
			headerCell.append(inputText);
			valueChangeRegistration = inputText.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					String newValue = inputText.flush();
					if (!Objects.equal(filterValue, newValue)) {
						filterValue = newValue;
						getDriver().resetDisplay();
					}
				}
			});

			Widget headerCell = button.getParent().getParent();
			headerCell.getElement().getStyle().setWidth(headerCell.getOffsetWidth(), Unit.PX);

			showFilterRow = true;
		}
		if (!showFilterRow) {
			for (Widget cell : row) {
				if (cell instanceof TableTH) {
					showFilterRow |= Iterators.size(((TableTH) cell).iterator()) > 0;
					if (showFilterRow) {
						break;
					}
				}
			}
		}
		row.setVisible(showFilterRow);
	}

	@Override
	public int getPrecedence() {
		return PRECEDENCE_NORMAL + 3;
	}

	@Override
	public <A, B extends Editor> boolean beforeVisit() {
		if (activate && filterValue != null) {
			final Model<T> leafModel = getDriver().getModel().getLeafModel();
			final Path path = PathUtils.evalPath(getColumnPath());

			List<T> list = getDriver().getDisplayedValue();
			list = Lists.newArrayList(Iterables.filter(list, new Predicate<T>() {

				@Override
				public boolean apply(T input) {
					Object p1 = ModelUtils.resolveValue(input, leafModel, path);
					if (p1 instanceof String) {
						return ((String) p1).toLowerCase().contains(filterValue.toLowerCase());
					}
					return true;
				}
			}));
			getDriver().setDisplayedValue(list);
		}
		return true;
	}
}
