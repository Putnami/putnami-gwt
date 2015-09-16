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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import fr.putnami.pwt.core.editor.client.validator.Validator;

public abstract class AbstractModel<T> implements Model<T> {

	protected final Class<?> beanType;
	protected final Class<?> leafType;
	protected final Model<?> parentModel;

	public AbstractModel(Class<?> beanType) {
		this.beanType = beanType;
		this.leafType = beanType;
		this.parentModel = null;
	}

	public AbstractModel(Class<?> beanType, Class<?> leafType) {
		this.beanType = beanType;
		this.leafType = leafType;
		this.parentModel = null;
	}

	public AbstractModel(Model<?> parentModel, Class<?> beanType) {
		this.beanType = beanType;
		this.leafType = beanType;
		this.parentModel = parentModel;
	}

	@Override
	public <A> Model<A> getLeafModel() {
		return (Model<A>) this;
	}

	@Override
	public Class<?> getLeafType() {
		return this.leafType;
	}

	@Override
	public Class<T> getBeanType() {
		return (Class<T>) this.beanType;
	}

	@Override
	public T cast(Object objet) {
		return (T) objet;
	}

	@Override
	public Model<?> getParentModel() {
		return this.parentModel;
	}

	@Override
	public <P> P get(Object bean, String fieldName) {
		if (bean == null) {
			return null;
		}
		PropertyDescription property = this.getProperties().get(fieldName);
		if (property != null) {
			return this.internalGet((T) bean, property.getName());
		}
		if (this.parentModel != null) {
			return this.parentModel.get(bean, fieldName);
		}

		return null;
	}

	@Override
	public <P> boolean set(Object bean, String fieldName, P value) {
		boolean result = false;
		if (bean == null) {
			return result;
		}
		PropertyDescription property = this.getProperties().get(fieldName);
		if (property != null) {
			this.internalSet((T) bean, property.getName(), value);
			result |= true;
		}
		if (this.parentModel != null) {
			result |= this.parentModel.set(bean, fieldName, value);
		}
		return result;
	}

	@Override
	public PropertyDescription getProperty(String fieldName) {
		PropertyDescription property = this.getProperties().get(fieldName);
		if (property != null) {
			return property;
		}
		if (this.parentModel != null) {
			return this.parentModel.getProperty(fieldName);
		}
		return null;
	}

	@Override
	public Iterable<String> getPropertyNames() {
		List<String> propertyNames = null;
		if (this.parentModel != null) {
			propertyNames = Lists.newArrayList(this.parentModel.getPropertyNames());
		} else {
			propertyNames = Lists.newArrayList();
		}
		propertyNames.addAll(this.getProperties().keySet());
		return Iterables.unmodifiableIterable(propertyNames);
	}

	@Override
	public T cloneBean(T beanToClone) {
		return this.cloneBean(beanToClone, Maps.newHashMap());
	}

	@Override
	public T cloneBean(T beanToClone, Map<Object, Object> alreadyClonedValues) {
		if (beanToClone == null) {
			return null;
		}
		T newInstance = (T) alreadyClonedValues.get(beanToClone);
		if (newInstance != null) {
			return newInstance;
		}
		newInstance = this.newInstance();
		alreadyClonedValues.put(beanToClone, newInstance);
		for (String prop : this.getPropertyNames()) {
			Object newValue = this.get(beanToClone, prop);
			PropertyDescription propDescription = this.getProperty(prop);
			if (propDescription != null && propDescription.getModel() != null) {
				Model<Object> subModel = propDescription.getModel();
				newValue = subModel.cloneBean(this.get(beanToClone, prop), alreadyClonedValues);
			}
			this.set(newInstance, prop, newValue);
		}
		return newInstance;
	}

	protected abstract Map<String, PropertyDescription> getProperties();

	protected abstract <P> void internalSet(T bean, String fieldName, P value);

	protected abstract <P> P internalGet(T bean, String fieldName);

	protected static PropertyDescription newPropertyDescription(String name, Class<?> clazz, Model<?> model,
		boolean hasGetter, boolean hasSetter, boolean isPublic, boolean isFinal, boolean isTransient,
		Validator<?>... validators) {
		PropertyDescription prop = new PropertyDescription();
		prop.setName(name);
		prop.setClazz(clazz);
		prop.setHasGetter(hasGetter);
		prop.setHasSetter(hasSetter);
		prop.setModel(model);
		prop.setPublic(isPublic);
		prop.setFinal(isFinal);
		prop.setTransient(isTransient);
		for (Validator<?> validator : validators) {
			prop.addValidator(validator);
		}
		return prop;
	}

}
