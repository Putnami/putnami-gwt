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
package fr.putnami.pwt.core.widget.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.IsWidget;

import fr.putnami.pwt.core.editor.client.EditorOutput;
import fr.putnami.pwt.core.editor.client.helper.TakesValueEditorWrapper;
import fr.putnami.pwt.core.theme.client.CssStyle;
import fr.putnami.pwt.core.widget.client.base.AbstractWidget;
import fr.putnami.pwt.core.widget.client.util.StyleUtils;

public class Image extends AbstractWidget implements EditorOutput<String> {

	private static final String PROP_MAX_WIDTH = "maxWidth";
	private static final String PROP_MAX_HEIGHT = "maxHeight";
	private static final String PROP_WIDTH = "width";
	private static final String PROP_HEIGHT = "height";

	public enum Type implements CssStyle {
			DEFAULT(null),
			ROUNDED("img-rounded"),
			CIRCLE("img-circle"),
			THUMBNAIL("img-thumbnail");

		private final String style;

		private Type(String style) {
			this.style = style;
		}

		@Override
		public String get() {
			return this.style;
		}
	}

	private final ImageElement imgElement;

	private boolean keepPropertions = true;

	private Type type;
	private String src;
	private String alt;

	private int widthPx = -1;
	private int heightPx = -1;

	public Image() {
		super(ImageElement.TAG);
		this.imgElement = ImageElement.as(this.getElement());
	}

	protected Image(Image source) {
		super(source);
		this.imgElement = ImageElement.as(this.getElement());

		this.setSrc(source.src);
		this.setAlt(source.alt);
		this.widthPx = source.widthPx;
		this.heightPx = source.heightPx;
		this.keepPropertions = source.keepPropertions;

		this.resetSize();
	}

	@Override
	public IsWidget cloneWidget() {
		return new Image(this);
	}

	public String getSrc() {
		return this.src;
	}

	public void setSrc(String src) {
		this.src = src;
		if (this.src != null) {
			if (this.src.startsWith("/")) {
				this.src = GWT.getModuleName() + this.src;
			}
			this.imgElement.setSrc(this.src);
		}
	}

	public String getAlt() {
		return this.alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
		this.imgElement.setAlt(alt);
	}

	@Override
	public String getValue() {
		return this.src;
	}

	@Override
	public void edit(String value) {
		this.setSrc(value);
	}

	@Override
	public LeafValueEditor<String> asEditor() {
		return new TakesValueEditorWrapper<String>(this);
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
		StyleUtils.addStyle(this, type);
	}

	public boolean isKeepPropertions() {
		return this.keepPropertions;
	}

	public void setKeepPropertions(boolean keepPropertions) {
		this.keepPropertions = keepPropertions;
		this.resetSize();
	}

	public Integer getWidthPx() {
		return this.widthPx;
	}

	public void setWidthPx(Integer widthPx) {
		this.widthPx = widthPx;
		this.resetSize();
	}

	public Integer getHeightPx() {
		return this.heightPx;
	}

	public void setHeightPx(Integer heightPx) {
		this.heightPx = heightPx;
		this.resetSize();
	}

	private void resetSize() {
		Style imgStyle = this.imgElement.getStyle();
		imgStyle.clearProperty(Image.PROP_WIDTH);
		imgStyle.clearProperty(Image.PROP_HEIGHT);
		imgStyle.clearProperty(Image.PROP_MAX_WIDTH);
		imgStyle.clearProperty(Image.PROP_MAX_HEIGHT);

		if (this.widthPx > 0) {
			imgStyle.setPropertyPx(this.keepPropertions ? Image.PROP_MAX_WIDTH : Image.PROP_WIDTH, this.widthPx);
		}
		if (this.heightPx > 0) {
			imgStyle.setPropertyPx(this.keepPropertions ? Image.PROP_MAX_HEIGHT : Image.PROP_HEIGHT, this.heightPx);
		}
	}

}
