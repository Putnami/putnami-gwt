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

import com.google.common.collect.Maps;

import java.util.Map;

import fr.putnami.pwt.core.editor.client.Context;
import fr.putnami.pwt.core.editor.client.Editor;
import fr.putnami.pwt.core.editor.client.EditorCollection;
import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.factory.CloneableWidget;
import fr.putnami.pwt.core.editor.client.util.PathUtils;
import fr.putnami.pwt.core.editor.client.visitor.AbstractVisitor;
import fr.putnami.pwt.core.model.client.ModelDriver;
import fr.putnami.pwt.core.model.client.base.EditorProvider;
import fr.putnami.pwt.core.model.client.base.HasEditorProvider;
import fr.putnami.pwt.core.model.client.base.HasInputEditorFactory;
import fr.putnami.pwt.core.model.client.base.HasOutputEditorFactory;
import fr.putnami.pwt.core.model.client.base.HasWidgetFactory;
import fr.putnami.pwt.core.model.client.factory.ContextFactory;
import fr.putnami.pwt.core.model.client.factory.EditorFactoryManager;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.util.ModelUtils;

public class EditorFactoryVisitor extends AbstractVisitor {

	private static final class InternalEditorProvider implements EditorProvider {
		final Context<?> parentContext;
		final Class<?> propertyType;

		CloneableWidget inputFactory;
		CloneableWidget outputFactory;

		private Map<Integer, Editor> inputEditors;
		private Map<Integer, Editor> outputEditors;

		private InternalEditorProvider(Context<?> parentContext, Class<?> propertyType, CloneableWidget inputFactory,
			CloneableWidget outputFactory) {
			super();
			this.parentContext = parentContext;
			this.propertyType = propertyType;
			this.inputFactory = inputFactory;
			this.outputFactory = outputFactory;
		}

		@Override
		public <E extends Editor> E getEditor(Boolean readonly) {
			return this.ensureEditor(readonly, null);
		}

		@Override
		public <E extends Editor> E getEditorForTraversal(Boolean readonly, Integer index) {
			return this.ensureEditor(readonly, index);
		}

		private <E extends Editor> E ensureEditor(Boolean readonly, Integer index) {
			// provide output if readonly
			if (!Boolean.FALSE.equals(readonly)) {
				return this.newOutput(index);
			}
			// provide input if not readonly
			return this.newInput(index);
		}

		private <E extends Editor> E newOutput(Integer index) {
			if (this.outputEditors == null) {
				this.outputEditors = Maps.newHashMap();
			}
			E editor = (E) outputEditors.get(index);
			if (editor != null) {
				return editor;
			}

			if (this.outputFactory == null) {
				editor = (E) EditorFactoryManager.get()
					.createOutputForType(this.propertyType, this.parentContext).cloneWidget();
			} else if (index == null) {
				editor = (E) this.outputFactory;
			} else {
				editor = (E) this.outputFactory.cloneWidget();
			}

			assert editor != null : "outputEditor can not create for " + this.propertyType;

			this.outputEditors.put(index, editor);
			this.initEditor(editor, index);
			return editor;
		}

		private <E extends Editor> E newInput(Integer index) {
			if (this.inputEditors == null) {
				this.inputEditors = Maps.newHashMap();
			}
			E editor = (E) inputEditors.get(index);
			if (editor != null) {
				return editor;
			}

			if (this.inputFactory == null) {
				editor = (E) EditorFactoryManager.get()
					.createInputForType(this.propertyType, this.parentContext).cloneWidget();
			} else if (index == null) {
				editor = (E) this.inputFactory;
			} else {
				editor = (E) this.inputFactory.cloneWidget();
			}

			assert editor != null : "inputEditor can not create for " + this.propertyType;

			this.inputEditors.put(index, editor);
			this.initEditor(editor, index);
			return editor;
		}

		void initEditor(Editor editor, Integer index) {
			if (index != null && !index.equals(-1)) {
				String path = "[" + index + "]";
				editor.setPath(path);
			}
			ModelDriver<?> driver = this.parentContext.getDriver();
			Context<?> context = ContextFactory.Util.get().createContext(driver, this.parentContext, editor);
			driver.accept(new BinderVisitor(driver, driver.getValue()), context);
		}
	}

	@Override
	public <A, B extends Editor> boolean visit(Context<B> context) {
		Editor editor = context.getEditor();
		if (editor instanceof HasEditorProvider) {
			Path path = context.getPath();
			Class<A> propertyType = null;
			ModelDriver<?> driver = context.getDriver();
			Model<?> model = ModelUtils.resolveModel(driver.getModel(), path);
			if (model instanceof ModelCollection
				&& (editor instanceof EditorCollection || PathUtils.isCollection(path))) {
				propertyType = (Class<A>) model.getLeafType();
			} else {
				propertyType = ModelUtils.resolveType(driver.getModel(), path);
			}

			CloneableWidget widgetFactory = null;
			CloneableWidget inputFactory = null;
			CloneableWidget outputFactory = null;

			if (editor instanceof HasWidgetFactory) {
				widgetFactory = ((HasWidgetFactory) editor).getWidgetFactory();
			}

			if (editor instanceof HasInputEditorFactory) {
				inputFactory = ((HasInputEditorFactory) editor).getInputFactory();
			}
			if (inputFactory == null) {
				inputFactory = widgetFactory;
			}

			if (editor instanceof HasOutputEditorFactory) {
				outputFactory = ((HasOutputEditorFactory) editor).getOutputFactory();
			}
			if (outputFactory == null) {
				outputFactory = widgetFactory;
			}

			EditorProvider provider = new InternalEditorProvider(context, propertyType, inputFactory, outputFactory);

			((HasEditorProvider) editor).setEditorProvider(provider);
		}

		return true;
	}

	@Override
	public VisitorTrigger trigerOn() {
		return VisitorTrigger.INITALIZE;
	}

}
