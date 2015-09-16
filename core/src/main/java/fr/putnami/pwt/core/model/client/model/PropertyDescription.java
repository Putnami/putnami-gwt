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
package fr.putnami.pwt.core.model.client.model;

import com.google.common.collect.Lists;

import java.util.Collection;

import fr.putnami.pwt.core.editor.client.validator.Validator;

public final class PropertyDescription {

	private String name;
	private Class<?> clazz;
	private Model<?> model;
	private boolean hasGetter;
	private boolean hasSetter;
	private Collection<Validator<?>> validators;
	private boolean isFinal;
	private boolean isTransient;
	private boolean isPublic;

	public PropertyDescription() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getClazz() {
		return this.clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public <M> Model<M> getModel() {
		return (Model<M>) this.model;
	}

	public void setModel(Model<?> model) {
		this.model = model;
	}

	public boolean isHasGetter() {
		return this.hasGetter;
	}

	public void setHasGetter(boolean hasGetter) {
		this.hasGetter = hasGetter;
	}

	public boolean isHasSetter() {
		return this.hasSetter;
	}

	public void setHasSetter(boolean hasSetter) {
		this.hasSetter = hasSetter;
	}

	public Collection<Validator<?>> getValidators() {
		return this.validators;
	}

	public void setValidators(Collection<Validator<?>> validators) {
		this.validators = validators;
	}

	public void addValidator(Validator<?> validator) {
		if (this.validators == null) {
			this.validators = Lists.newArrayList();
		}
		this.validators.add(validator);
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

	public boolean isTransient() {
		return isTransient;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean isPublic() {
		return isPublic;
	}

}
