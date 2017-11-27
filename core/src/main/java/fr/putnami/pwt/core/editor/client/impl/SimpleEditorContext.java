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
/**
 * This file is part of pwt.
 * <p>
 * pwt is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * pwt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with pwt. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.editor.client.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import java.util.Map;

import fr.putnami.pwt.core.editor.client.Context;
import fr.putnami.pwt.core.editor.client.Driver;
import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.aspect.ContextAspect;

public class SimpleEditorContext<E extends Editor> implements Context<E> {

	private final Driver<?> driver;
	private final Context<?> parentContext;
	private final Map<Class<?>, ContextAspect> aspects = Maps.newHashMap();

	private final Path path;
	private final E editor;

	public <A, B extends Editor> SimpleEditorContext(Driver<?> driver, Context<?> parentContext, E editor, Path path) {
		this.driver = driver;
		this.parentContext = parentContext;
		this.path = path;
		this.editor = editor;
	}

	@Override
	public boolean isRootContext() {
		return this.driver.getRootEditor() == this.editor;
	}

	@Override
	public <D extends Driver<?>> D getDriver() {
		return (D) this.driver;
	}

	@Override
	public Context<?> getParentContext() {
		return this.parentContext;
	}

	@Override
	public Path getPath() {
		return this.path;
	}

	@Override
	public E getEditor() {
		return this.editor;
	}

	@Override
	public <A extends ContextAspect> A getAspect(Class<A> contextType) {
		return (A) this.aspects.get(contextType);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.editor);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SimpleEditorContext) {
			return Objects.equal(this.editor, ((SimpleEditorContext<?>) obj).editor);
		}
		return false;
	}

	@Override
	public <A extends ContextAspect> void addAspect(A contextAspect) {
		Class cls = contextAspect.getClass();
		this.aspects.put(cls, contextAspect);
		Class currentSuperClass = cls.getSuperclass();
		while (!Object.class.equals(currentSuperClass)) {
			if (!this.aspects.containsKey(currentSuperClass)) {
				this.aspects.put(currentSuperClass, contextAspect);
			}
			currentSuperClass = currentSuperClass.getSuperclass();
		}
	}

	@Override
	public <A extends ContextAspect> boolean hasAspect(Class<A> contextType) {
		return this.aspects.containsKey(contextType);
	}

	@Override
	public String toString() {
		MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
		helper.add("editor", this.editor.getClass().getSimpleName());
		helper.add("path", this.path);
		return helper.toString();
	}
}
