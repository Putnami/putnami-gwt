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
package fr.putnami.pwt.core.widget.rebind;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.resource.ResourceOracle;
import com.google.gwt.i18n.shared.GwtLocale;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import fr.putnami.pwt.core.widget.client.binder.UiBinderLocalized;

public class UiBinderLocalizedCreator {

	private static final String PROXY_SUFFIX = "_UiBinderLocalized";
	private static final String TEMPLATE_SUFFIX = ".ui.xml";

	private final JClassType binderType;

	private String binderProxyQualifiedName;
	private String binderProxySimpleName;
	private JClassType widgetType;
	private JClassType targetType;
	private GwtLocale locale;
	private String templateName;

	public UiBinderLocalizedCreator(JClassType binderType, GwtLocale locale) {
		this.binderType = binderType;
		this.locale = locale;

		for (JClassType interfaceType : binderType.getImplementedInterfaces()) {
			if (interfaceType.getQualifiedSourceName().equals(UiBinderLocalized.class.getCanonicalName())
				&& interfaceType instanceof JParameterizedType) {
				JParameterizedType paramType = (JParameterizedType) interfaceType;
				this.widgetType = paramType.getTypeArgs()[0];
				this.targetType = paramType.getTypeArgs()[1];
			}
		}
		UiTemplate templateAnnotation = binderType.getAnnotation(UiTemplate.class);
		if (templateAnnotation != null) {
			this.templateName = templateAnnotation.value().replace(UiBinderLocalizedCreator.TEMPLATE_SUFFIX, "");
		}
		if (this.templateName == null) {
			this.templateName = this.targetType.getSimpleSourceName();
		}
	}

	public String create(TreeLogger logger, GeneratorContext context) {
		Resource templateResource = this.getTemplateResource(context);
		if (templateResource == null) {
			throw new NullPointerException("no template found");
		}
		this.binderProxySimpleName =
			this.targetType.getSimpleSourceName() + "_" + this.binderType.getSimpleSourceName()
				+ UiBinderLocalizedCreator.PROXY_SUFFIX;
		if (this.locale != null) {
			this.binderProxySimpleName += "_" + this.locale.toString();
		}
		this.binderProxyQualifiedName = this.targetType.getPackage().getName() + "." + this.binderProxySimpleName;

		PrintWriter printWriter = this.getPrintWriter(logger, context, this.binderProxyQualifiedName);
		if (printWriter == null) {
			return this.binderProxyQualifiedName;
		}

		SourceWriter srcWriter = this.getSourceWriter(printWriter, context);

		srcWriter.println();
		srcWriter.indent();
		this.generateProxy(logger, srcWriter);
		srcWriter.println();
		srcWriter.outdent();

		srcWriter.commit(logger);

		return this.binderProxyQualifiedName;
	}

	private Resource getTemplateResource(GeneratorContext context) {
		String packageResourcePath = this.targetType.getPackage().getName().replace('.', '/') + "/";
		ResourceOracle resourceOracle = context.getResourcesOracle();
		Map<String, Resource> reourceMap = new HashMap<>();
		for (Resource resource : resourceOracle.getResources()) {
			reourceMap.put(resource.getPath(), resource);
		}
		String templatePath =
			packageResourcePath + this.templateName + "_" + this.locale + UiBinderLocalizedCreator.TEMPLATE_SUFFIX;
		Resource templateResource = reourceMap.get(templatePath);
		if (templateResource == null) {
			this.locale = null;
			templatePath = packageResourcePath + this.templateName + UiBinderLocalizedCreator.TEMPLATE_SUFFIX;
			templateResource = reourceMap.get(templatePath);
		}
		if (templateResource != null) {
			this.templateName = templatePath.replace(packageResourcePath, "");
		}
		return templateResource;
	}

	private void generateProxy(TreeLogger logger, SourceWriter srcWriter) {

		srcWriter.println("@UiTemplate(\"%s\")", this.templateName);
		srcWriter.println("interface Binder extends UiBinder<%s, %s> {", this.widgetType.getSimpleSourceName(),
			this.targetType.getSimpleSourceName());
		srcWriter.indent();
		srcWriter.println("UiBinder<%s, %s> BINDER = GWT.create(Binder.class);", this.widgetType.getSimpleSourceName(),
			this.targetType.getSimpleSourceName());
		srcWriter.outdent();
		srcWriter.println("}");
		srcWriter.println();
		srcWriter.println("@Override");
		srcWriter
			.println("public %s createAndBindUi(%s owner) {", this.widgetType.getSimpleSourceName(), this.targetType
				.getSimpleSourceName());
		srcWriter.indent();
		srcWriter.println("return Binder.BINDER.createAndBindUi(owner);");
		srcWriter.outdent();
		srcWriter.println("}");
	}

	private SourceWriter getSourceWriter(PrintWriter printWriter, GeneratorContext ctx) {

		String packageName = this.binderType.getPackage().getName();
		String className = this.binderProxySimpleName;

		ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, className);

		composerFactory.addImport(GWT.class.getName());
		composerFactory.addImport(UiBinder.class.getName());
		composerFactory.addImport(UiBinderLocalized.class.getName());
		composerFactory.addImport(UiTemplate.class.getName());

		composerFactory.addImport(this.binderType.getQualifiedSourceName());
		composerFactory.addImport(this.widgetType.getQualifiedSourceName());
		composerFactory.addImport(this.targetType.getQualifiedSourceName());

		composerFactory.addImplementedInterface(UiBinderLocalized.class.getSimpleName() + "<"
			+ this.widgetType.getSimpleSourceName() + "," + this.targetType.getSimpleSourceName() + ">");
		composerFactory.addImplementedInterface(UiBinder.class.getSimpleName() + "<"
			+ this.widgetType.getSimpleSourceName() + "," + this.targetType.getSimpleSourceName() + ">");
		composerFactory.addImplementedInterface(this.binderType.getSimpleSourceName());

		return composerFactory.createSourceWriter(ctx, printWriter);
	}

	private PrintWriter getPrintWriter(TreeLogger logger, GeneratorContext ctx, String targetQualifiedName) {
		String packageName = this.binderType.getPackage().getName();
		String className = this.binderProxySimpleName;
		return ctx.tryCreate(logger, packageName, className);
	}
}
