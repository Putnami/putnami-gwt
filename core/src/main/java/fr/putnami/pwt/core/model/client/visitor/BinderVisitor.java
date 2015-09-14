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
package fr.putnami.pwt.core.model.client.visitor;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import fr.putnami.pwt.core.editor.client.Context;
import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.EditorCollection;
import fr.putnami.pwt.core.editor.client.EditorValue;
import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.aspect.ContextAspect;
import fr.putnami.pwt.core.editor.client.visitor.AbstractVisitor;
import fr.putnami.pwt.core.model.client.ModelDriver;
import fr.putnami.pwt.core.model.client.base.HasDrawable;
import fr.putnami.pwt.core.model.client.base.HasReadonly;
import fr.putnami.pwt.core.model.client.factory.ContextFactory;
import fr.putnami.pwt.core.model.client.util.ModelUtils;

public class BinderVisitor extends AbstractVisitor {

	public class TraversalEditorsAspect implements ContextAspect {
		private final List<Context<EditorValue>> contexts = Lists.newArrayList();

		public List<Context<EditorValue>> getContexts() {
			return this.contexts;
		}
	}

	private final ModelDriver<?> driver;
	private final Object object;

	public BinderVisitor(ModelDriver<?> driver, Object object) {
		this.driver = driver;
		this.object = object;
	}

	@Override
	public <A, B extends Editor> boolean visit(Context<B> context) {
		B editor = context.getEditor();
		Path path = context.getPath();

		A value = null;

		if (editor instanceof EditorValue) {
			value = (A) ModelUtils.resolveValue(this.object, this.driver.getModel(), path);
		}

		if (context != this.driver.getRootContext() && editor instanceof EditorValue) {
			EditorValue<A> editorValue = (EditorValue) editor;
			editorValue.edit(value);
		}

		if (context == this.driver.getRootContext() && editor instanceof EditorCollection && value instanceof Collection) {
			TraversalEditorsAspect aspect = context.getAspect(TraversalEditorsAspect.class);
			if (aspect == null) {
				aspect = new TraversalEditorsAspect();
				context.addAspect(aspect);
			}

			EditorCollection editorList = (EditorCollection) editor;
			Collection collectionToBind = (Collection) value;

			while (collectionToBind.size() < aspect.contexts.size()) {
				this.driver.removeContext(aspect.contexts.remove(collectionToBind.size()));
			}

			for (int i = 0; i < collectionToBind.size(); i++) {
				EditorValue traversalEditor = editorList.getEditorForTraversal(i);
				Context<EditorValue> contextCreated = (Context<EditorValue>) this.driver.getContext(traversalEditor);
				if (contextCreated == null) {
					contextCreated = ContextFactory.Util.get().createContext(this.driver, context, traversalEditor);
					if (editor instanceof HasReadonly) {
						this.driver.accept(new ReadonlyVisitor(editor, ((HasReadonly) editor).getReadonly(), true), contextCreated);
					}
					aspect.contexts.add(contextCreated);
				}
			}
		}
		if (editor instanceof HasDrawable) {
			((HasDrawable) editor).redraw();
		}

		return true;
	}

	@Override
	public VisitorTrigger trigerOn() {
		return VisitorTrigger.MANUAL;
	}

}
