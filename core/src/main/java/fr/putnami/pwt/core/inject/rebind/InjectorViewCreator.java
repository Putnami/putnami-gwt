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
package fr.putnami.pwt.core.inject.rebind;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.user.rebind.SourceWriter;

import java.util.Collection;

import fr.putnami.pwt.core.inject.rebind.base.AbstractInjectorCreator;
import fr.putnami.pwt.core.inject.rebind.base.InjectorDelegateFactorty;
import fr.putnami.pwt.core.inject.rebind.base.InjectorWritterAfterPresent;
import fr.putnami.pwt.core.inject.rebind.base.InjectorWritterBeforePresent;
import fr.putnami.pwt.core.inject.rebind.base.InjectorWritterPresent;
import fr.putnami.pwt.core.inject.rebind.factory.DecoratorPresenterCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.ErrorHandlerCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.InitializeFormCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.MayStopActivityCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.ModelCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.PostconstructCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.PresenterCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.ResourceCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.SecurityCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.ServiceCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.StopActivityCreatorFactory;
import fr.putnami.pwt.core.inject.rebind.factory.TemplatedCreatorFactory;

public class InjectorViewCreator extends AbstractInjectorCreator {

	public InjectorViewCreator(JClassType injectableType) {
		super(injectableType);
	}

	@Override
	protected Collection<InjectorDelegateFactorty> getFactories() {
		Collection<InjectorDelegateFactorty> factories = Lists.newArrayList();
		factories.add(new ResourceCreatorFactory());
		factories.add(new ServiceCreatorFactory());
		factories.add(new SecurityCreatorFactory());
		factories.add(new PresenterCreatorFactory());
		factories.add(new MayStopActivityCreatorFactory());
		factories.add(new StopActivityCreatorFactory());
		factories.add(new ErrorHandlerCreatorFactory());
		factories.add(new DecoratorPresenterCreatorFactory());
		factories.add(new ModelCreatorFactory());
		factories.add(new TemplatedCreatorFactory());
		factories.add(new PostconstructCreatorFactory());
		factories.add(new InitializeFormCreatorFactory());
		return factories;
	}

	@Override
	protected void doCreate(TreeLogger logger, GeneratorContext context, SourceWriter srcWriter) {
		super.doCreate(logger, context, srcWriter);

		// presenter
		srcWriter.println("public <P extends Place> void present(P place, final AcceptsOneWidget displayer){");
		srcWriter.indent();

		for (InjectorWritterBeforePresent delegate : Iterables.filter(this.delegates, InjectorWritterBeforePresent.class)) {
			delegate.writeBeforePresent(srcWriter);
		}
		for (InjectorWritterPresent delegate : Iterables.filter(this.delegates, InjectorWritterPresent.class)) {
			delegate.writePresent(srcWriter);
		}
		for (InjectorWritterAfterPresent delegate : Iterables.filter(this.delegates, InjectorWritterAfterPresent.class)) {
			delegate.writeAfterPresent(srcWriter);
		}
		srcWriter.outdent();
		srcWriter.println("}");
	}
}
