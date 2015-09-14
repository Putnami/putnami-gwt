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
package fr.putnami.pwt.core.model.client.util;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.Path.PathElement;
import fr.putnami.pwt.core.editor.client.util.PathUtils;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.model.PropertyDescription;

public final class ModelUtils {

	private ModelUtils() {
	}

	public static <A> PropertyDescription resolveProperty(Model<?> model, Path path) {
		if (model == null || path.isEmpty()) {
			return null;
		}
		String firstElementName = path.get(0).getElementName();
		if (path.size() == 1) {
			return model.getProperty(firstElementName);
		} else if (Path.ROOT_PATH.equals(firstElementName)) {
			return ModelUtils.resolveProperty(model, path.subPath(1));
		} else {
			PropertyDescription propertyDescription = model.getProperty(firstElementName);
			Model<Object> subModel = propertyDescription.getModel();
			return ModelUtils.resolveProperty(subModel, path.subPath(1));
		}
	}

	public static <A> Collection<Validator<A>> resolveValidators(Model<?> model, Path path) {
		PropertyDescription propertyDescription = ModelUtils.resolveProperty(model, path);
		Collection<Validator<?>> validators = null;
		if (propertyDescription != null) {
			validators = propertyDescription.getValidators();
		}
		return (Collection<Validator<A>>) (validators == null ? Collections.<Validator<A>> emptyList() : validators);
	}

	public static <A, B> Model<A> resolveModel(Model<B> model, Path path) {
		PropertyDescription propertyDescription = ModelUtils.resolveProperty(model, path);
		if (propertyDescription != null) {
			return (Model<A>) propertyDescription.getModel();
		}
		if (PathUtils.isCollection(path)) {
			return (Model<A>) model.getLeafModel();
		}
		return (Model<A>) model;
	}

	public static <A, B> Class<A> resolveType(Model<B> model, Path path) {
		if (path.isEmpty()) {
			return (Class<A>) model.getLeafType();
		}
		PropertyDescription propertyDescription = ModelUtils.resolveProperty(model, path);
		if (propertyDescription != null) {
			return (Class<A>) propertyDescription.getClazz();
		}
		return null;
	}

	public static <A, B> A resolveValue(Object bean, Model<B> model, Path path) {
		if (model == null || path.isEmpty()) {
			return (A) bean;
		}

		PathElement firstElement = path.get(0);
		String firstElementName = firstElement.getElementName();
		firstElementName = firstElementName == null ? Path.ROOT_PATH : firstElementName;
		Integer firstElementIndex = firstElement.getIndexKey();

		Object value = bean;
		Model<?> leafModel = model;
		if (leafModel instanceof ModelCollection) {
			leafModel = ((ModelCollection<?>) model).getLeafModel();
		}

		if (leafModel != null && !Path.ROOT_PATH.equals(firstElementName)) {
			value = leafModel.get(bean, firstElementName);
		}

		if (firstElementIndex != null && value instanceof Collection) {
			Collection<?> collection = (Collection<?>) value;
			if (collection.size() > firstElement.getIndexKey()) {
				value = Iterables.get(collection, firstElementIndex);
			} else {
				value = null;
			}
		}

		if (path.size() == 1) {
			return (A) value;
		}
		if (!Path.ROOT_PATH.equals(firstElementName)) {
			leafModel = leafModel.getProperty(firstElementName).getModel();
		}
		return ModelUtils.resolveValue(value, leafModel, path.subPath(1));
	}

	public static <A, B> A bindValue(A bean, Model<A> model, Path path, B value) {
		if (model == null || path.isEmpty()) {
			return bean;
		}
		A targetBean = bean;

		PathElement firstElement = path.get(0);
		String firstElementName = firstElement.getElementName();
		Integer firstElementIndex = firstElement.getIndexKey();

		if (path.size() == 1) {
			if (firstElementIndex == null) {
				if (targetBean == null && value != null) {
					targetBean = model.newInstance();
					model.set(targetBean, firstElementName, value);
				} else {
					model.set(targetBean, firstElementName, value);
				}
				model.set(targetBean, firstElementName, value);
			} else {
				Object o;
				if (Path.ROOT_PATH.equals(firstElementName)) {
					o = targetBean;
				} else {
					o = model.get(targetBean, firstElementName);
					if (o == null) {
						o = model.newInstance();
						model.set(targetBean, firstElementName, o);
					}
				}
				if (o instanceof List) {
					List list = (List) o;
					while (list.size() <= firstElementIndex.intValue()) {
						list.add(null);
					}
					list.set(firstElementIndex, value);
				}
			}
		} else {
			if (Path.ROOT_PATH.equals(firstElementName) && firstElementIndex == null) {
				ModelUtils.bindValue(targetBean, model, path.subPath(1), value);
			} else if (firstElementIndex != null) {
				Object o;
				if (Path.ROOT_PATH.equals(firstElementName)) {
					o = targetBean;
				} else {
					o = model.get(targetBean, firstElementName);
					if (o == null) {
						o = model.newInstance();
						model.set(targetBean, firstElementName, o);
					}
				}
				if (o instanceof List) {
					List list = (List) o;
					while (list.size() <= firstElementIndex.intValue()) {
						list.add(null);
					}
					Object subBean = list.get(firstElementIndex);

					Model<Object> subModel = model.getLeafModel();
					if (subBean == null) {
						subBean = subModel.newInstance();
						list.set(firstElementIndex, subBean);
					}
					ModelUtils.bindValue(subBean, subModel, path.subPath(1), value);
				}
			} else {
				Model<Object> subModel = model.getProperty(firstElementName).getModel();
				Object subBean = model.get(targetBean, firstElementName);
				if (subBean == null) {
					subBean = subModel.newInstance();
					model.set(targetBean, firstElementName, subBean);
				}
				ModelUtils.bindValue(subBean, subModel, path.subPath(1), value);
			}
		}
		return targetBean;
	}

	public static List<Class<?>> getTypeHierachy(Class<?> propertyType) {
		List<Class<?>> result = Lists.newArrayList();
		if (propertyType != null) {
			Class<?> parentClass = propertyType.getSuperclass();
			while (parentClass != null) {
				result.add(parentClass);
				parentClass = parentClass.getSuperclass();
			}
		}
		return result;
	}

	public static boolean isEnumType(Class<?> propertyType) {
		for (Class<?> parentClass : ModelUtils.getTypeHierachy(propertyType)) {
			if (Enum.class.equals(parentClass)) {
				return true;
			}
		}
		return false;
	}

}
