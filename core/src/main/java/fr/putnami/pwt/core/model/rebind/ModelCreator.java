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
package fr.putnami.pwt.core.model.rebind;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.thirdparty.guava.common.collect.Sets;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import fr.putnami.pwt.core.model.client.model.AbstractModel;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.model.PropertyDescription;
import fr.putnami.pwt.core.model.client.util.PrimitiveUtils;

public class ModelCreator {

	private static final Set<String> DISCARD_MODEL_TYPES = Sets.newHashSet(
		// Primitives
		Boolean.class.getName(), Byte.class.getName(), Character.class.getName(), Double.class.getName(), Float.class
			.getName(), Integer.class.getName(), Long.class.getName(), Short.class.getName(),
		// BigDecimal
		BigDecimal.class.getName(),
		// Others
		Object.class.getName(), String.class.getName(), Date.class.getName(), Class.class.getName(),
		Exception.class.getName(), Object[].class.getName(),
		RuntimeException.class.getName(), Throwable.class.getName(),
		// Collections
		List.class.getName(), Set.class.getName(), Map.class.getName(),
		// Miscellus
		"?");

	private static final Set<String> COLLECTION_TYPES = Sets.newHashSet(List.class.getName(), Collection.class.getName());

	private JClassType beanType;
	private String proxyModelQualifiedName;

	private Set<JType> imports = Sets.newHashSet();
	private Map<JType, String> subModels = Maps.newHashMap();
	private Map<String, JType> propertyTypes = Maps.newHashMap();
	private Map<String, JType> publicFields = Maps.newHashMap();
	private Map<String, JMethod> getters = Maps.newHashMap();
	private Map<String, JMethod> setters = Maps.newHashMap();

	private JClassType parentType;

	public ModelCreator(JClassType beanType) {
		this.beanType = beanType;
		this.proxyModelQualifiedName = beanType.getQualifiedSourceName() + "_Model";
		this.proxyModelQualifiedName = this.proxyModelQualifiedName
			.replace(beanType.getName(), beanType.getName().replace('.', '_'));
	}

	public String create(TreeLogger logger, GeneratorContext context) {
		PrintWriter printWriter = this.getPrintWriter(logger, context, this.proxyModelQualifiedName);
		if (printWriter == null) {
			return this.proxyModelQualifiedName;
		}

		JField[] fields = this.beanType.getFields();
		JMethod[] methods = this.beanType.getMethods();

		this.parentType = this.beanType.getSuperclass();
		this.imports.add(this.parentType);

		this.listPublicFields(fields);
		this.listGetters(methods);
		this.listSetters(methods);

		this.createSubModels(logger, context);

		SourceWriter srcWriter = this.getSourceWriter(printWriter, context);

		srcWriter.indent();
		srcWriter.println();
		this.generateSingleton(logger, srcWriter);
		srcWriter.println();
		srcWriter.println();
		this.generateStaticInitializer(logger, srcWriter);
		srcWriter.println();
		this.generateConstructor(logger, srcWriter);
		srcWriter.println();
		this.generateCreate(logger, srcWriter);
		srcWriter.println();
		this.generateInternalSet(logger, srcWriter);
		srcWriter.println();
		this.generateInternalGet(logger, srcWriter);

		srcWriter.outdent();

		srcWriter.commit(logger);
		return this.proxyModelQualifiedName;
	}

	private void createSubModels(TreeLogger logger, GeneratorContext context) {
		for (JType jType : this.imports) {
			String subModelType = createSubModel(logger, context, jType);
			if (subModelType != null) {
				this.subModels.put(jType, subModelType);
			}
		}
	}

	private void generateSingleton(TreeLogger logger, SourceWriter srcWriter) {
		String className =
			this.proxyModelQualifiedName.indexOf('.') == -1 ? this.proxyModelQualifiedName : this.proxyModelQualifiedName
				.substring(this.proxyModelQualifiedName.lastIndexOf('.') + 1,
					this.proxyModelQualifiedName.length());
		srcWriter.println("public static final %s INSTANCE = new %s();", className, className);
	}

	private void generateStaticInitializer(TreeLogger logger, SourceWriter srcWriter) {

		srcWriter.println("protected static final Map<String, PropertyDescription> PROPERTIES = Maps.newHashMap();");
		srcWriter.println("static{");
		srcWriter.indent();

		for (String propertyName : this.propertyTypes.keySet()) {
			JType propertyType = this.propertyTypes.get(propertyName);
			String simplePropertyTypeName = propertyType.getSimpleSourceName();
			String modelName = this.subModels.get(propertyType);
			if (modelName != null) {
				modelName += ".INSTANCE";
			}
			if (ModelCreator.COLLECTION_TYPES.contains(propertyType.getQualifiedSourceName())) {
				JParameterizedType parametrizedType = propertyType.isParameterized();
				JType subType = propertyType;
				if (parametrizedType != null) {
					subType = parametrizedType.getTypeArgs()[0];
					String submodelName = this.subModels.get(subType);
					if (submodelName != null) {
						submodelName += ".INSTANCE";
					} else if (!"?".equals(subType.getSimpleSourceName())) {
						submodelName = subType.getSimpleSourceName() + ".class";
					} else {
						submodelName = "(Class) null";
					}
					modelName =
						String.format("new ModelCollection(%s.class, %s)",
							propertyType.getSimpleSourceName(), submodelName);
				} else {
					logger.branch(Type.WARN, String.format(
						"Property [%s] on bean %s is a raw collection type. You cannot use it on editors.",
						propertyName, this.beanType.getQualifiedSourceName()));
					modelName = "new ModelCollection((Model) null)";
				}
			}
			Boolean getter = this.getters.containsKey(propertyName);
			Boolean setter = this.setters.containsKey(propertyName);
			Boolean isFinal = true;
			if (propertyType instanceof JClassType) {
				try {
					Class<?> propClass = getClass().getClassLoader().loadClass(propertyType.getQualifiedSourceName());
					isFinal = Modifier.isFinal(propClass.getModifiers());
				} catch (ClassNotFoundException e) {
					JClassType classType = (JClassType) propertyType;
					isFinal = classType.isFinal();
				}
			}

			srcWriter.print("PROPERTIES.put(\"%s\", newPropertyDescription(\"%s\", %s.class, %s, %s, %s, %s",
				propertyName, propertyName, simplePropertyTypeName, modelName, getter, setter, isFinal);
			this.generateValidators(srcWriter, propertyName);
			srcWriter.println("));");
		}
		srcWriter.outdent();
		srcWriter.println("}");

		srcWriter.println("protected Map<String, PropertyDescription> getProperties(){");
		srcWriter.println("	return PROPERTIES;");
		srcWriter.println("}");
	}

	private void generateValidators(SourceWriter w, String propertyName) {
		JField field = this.beanType.getField(propertyName);
		if (field != null) {
			appendTrueValidator(w, field);
			appendFalseValidator(w, field);
			appendFutureValidator(w, field);
			appendMaxValidator(w, field);
			appendMinValidator(w, field);
			appendNotNullValidator(w, field);
			appendNullValidator(w, field);
			appendPastValidator(w, field);
			appendPatternValidator(w, field);
			appendSizeValidator(w, field);
		}
	}

	private void appendSizeValidator(SourceWriter w, JField field) {
		Size sizeAnnotation = field.getAnnotation(Size.class);
		if (sizeAnnotation != null) {
			w.println(", new SizeValidator(\"%s\", %s, %s)", sizeAnnotation.message(), sizeAnnotation.min(),
				sizeAnnotation.max());
		}
	}

	private void appendPatternValidator(SourceWriter w, JField field) {
		Pattern patternAnnotation = field.getAnnotation(Pattern.class);
		if (patternAnnotation != null) {
			w.println(", new PatternValidator(\"%s\", \"%s\")", patternAnnotation.message(), patternAnnotation.regexp()
				.replace("\\", "\\\\"), patternAnnotation.flags());
		}
	}

	private void appendPastValidator(SourceWriter w, JField field) {
		Past pastAnnotation = field.getAnnotation(Past.class);
		if (pastAnnotation != null) {
			w.println(", new PastValidator(\"%s\")", pastAnnotation.message());
		}
	}

	private void appendNullValidator(SourceWriter w, JField field) {
		Null nullAnnotation = field.getAnnotation(Null.class);
		if (nullAnnotation != null) {
			w.println(", new NullValidator(\"%s\")", nullAnnotation.message());
		}
	}

	private void appendNotNullValidator(SourceWriter w, JField field) {
		NotNull notNullAnnotation = field.getAnnotation(NotNull.class);
		if (notNullAnnotation != null) {
			w.println(", new NotNullValidator(\"%s\")", notNullAnnotation.message());
		}
	}

	private void appendMinValidator(SourceWriter w, JField field) {
		Min minAnnotation = field.getAnnotation(Min.class);
		if (minAnnotation != null) {
			w.println(", new MinValidator(\"%s\", %s)", minAnnotation.message(), minAnnotation.value());
		}
	}

	private void appendMaxValidator(SourceWriter w, JField field) {
		Max maxAnnotation = field.getAnnotation(Max.class);
		if (maxAnnotation != null) {
			w.println(", new MaxValidator(\"%s\", %s)", maxAnnotation.message(), maxAnnotation.value());
		}
	}

	private void appendFutureValidator(SourceWriter w, JField field) {
		Future futureAnnotation = field.getAnnotation(Future.class);
		if (futureAnnotation != null) {
			w.println(", new FutureValidator(\"%s\")", futureAnnotation.message());
		}
	}

	private void appendFalseValidator(SourceWriter w, JField field) {
		AssertFalse falseAnnotation = field.getAnnotation(AssertFalse.class);
		if (falseAnnotation != null) {
			w.println(", new AssertFalseValidator(\"%s\")", falseAnnotation.message());
		}
	}

	private void appendTrueValidator(SourceWriter w, JField field) {
		AssertTrue trueAnnotation = field.getAnnotation(AssertTrue.class);
		if (trueAnnotation != null) {
			w.println(", new AssertTrueValidator(\"%s\")", trueAnnotation.message());
		}
	}

	private void generateConstructor(TreeLogger logger, SourceWriter srcWriter) {
		int lastIndex = this.proxyModelQualifiedName.lastIndexOf('.');
		String className =
			lastIndex == -1 ? this.proxyModelQualifiedName : this.proxyModelQualifiedName.substring(lastIndex + 1,
				this.proxyModelQualifiedName.length());
		srcWriter.println("public %s(){", className);
		srcWriter.indent();

		if (this.subModels.get(this.parentType) != null) {
			srcWriter.println("super(%s.INSTANCE, %s.class);", this.subModels.get(this.parentType),
				this.beanType.getSimpleSourceName());
			srcWriter.println();
		} else {
			srcWriter.println("super(%s.class);", this.beanType.getSimpleSourceName());
			srcWriter.println();
		}

		srcWriter.outdent();
		srcWriter.println("}");
	}

	private void generateCreate(TreeLogger logger, SourceWriter srcWriter) {
		srcWriter.println("public %s newInstance() {", this.beanType.getSimpleSourceName());
		srcWriter.indent();
		if (!this.beanType.isAbstract()) {
			srcWriter.println("return new %s();", this.beanType.getSimpleSourceName());
		} else {
			srcWriter.println("throw new RuntimeException(\"Can not instantiate the abstract class %s\");",
				this.beanType.getSimpleSourceName());
		}
		srcWriter.outdent();
		srcWriter.println("}");
	}

	private void generateInternalGet(TreeLogger logger, SourceWriter srcWriter) {
		srcWriter.println("protected <P> P internalGet(%s bean, String fieldName){", this.beanType.getSimpleSourceName());
		srcWriter.indent();
		for (String propertyName : this.propertyTypes.keySet()) {
			JType propertyType = this.propertyTypes.get(propertyName);
			JPrimitiveType primitiveType = propertyType.isPrimitive();
			JMethod getter = this.getters.get(propertyName);
			if (getter != null) {
				if (primitiveType != null) {
					String boxedName = primitiveType.getQualifiedBoxedSourceName();
					boxedName = boxedName.substring(boxedName.lastIndexOf(".") + 1, boxedName.length());
					srcWriter.println("if(\"%s\".equals(fieldName)){  return (P) PrimitiveUtils.castTo%s(bean.%s()); }",
						propertyName, boxedName, getter.getName());
				} else {
					srcWriter.println("if(\"%s\".equals(fieldName)){  return (P) bean.%s(); }", propertyName, getter.getName());
				}
			} else if (this.publicFields.containsKey(propertyName)) {
				if (primitiveType != null) {
					String boxedName = primitiveType.getQualifiedBoxedSourceName();
					boxedName = boxedName.substring(boxedName.lastIndexOf(".") + 1, boxedName.length());
					srcWriter.println("if(\"%s\".equals(fieldName)){  return (P) PrimitiveUtils.castTo%s(bean.%s); }",
						propertyName,
						boxedName, propertyName);
				} else {
					srcWriter.println("if(\"%s\".equals(fieldName)){  return (P) bean.%s; }", propertyName, propertyName);
				}
			}
		}
		srcWriter.println();
		srcWriter.println("return null;");
		srcWriter.outdent();
		srcWriter.println("}");
	}

	private void generateInternalSet(TreeLogger logger, SourceWriter srcWriter) {
		srcWriter.println("protected <P> void internalSet(%s bean, String fieldName, P value){",
			this.beanType.getSimpleSourceName());
		srcWriter.indent();
		for (String propertyName : this.propertyTypes.keySet()) {
			JType propertyType = this.propertyTypes.get(propertyName);
			JPrimitiveType primitiveType = propertyType.isPrimitive();
			JMethod setter = this.setters.get(propertyName);
			if (setter != null) {
				if (primitiveType != null) {
					srcWriter.println("if(\"%s\".equals(fieldName)){  bean.%s((%s) PrimitiveUtils.asPrimitive((%s)value)); }",
						propertyName, setter.getName(), propertyType.getSimpleSourceName(),
						primitiveType.getQualifiedBoxedSourceName());
				} else {
					srcWriter.println("if(\"%s\".equals(fieldName)){  bean.%s((%s) value); }", propertyName, setter.getName(),
						propertyType.getSimpleSourceName());
				}
			} else if (this.publicFields.containsKey(propertyName)) {
				if (primitiveType != null) {
					srcWriter.println("if(\"%s\".equals(fieldName)){ bean.%s = PrimitiveUtils.asPrimitive((%s) value); }",
						propertyName, propertyName, primitiveType.getQualifiedBoxedSourceName());
				} else {
					srcWriter.println("if(\"%s\".equals(fieldName)){  bean.%s = (%s) value; }", propertyName, propertyName,
						propertyType.getSimpleSourceName());
				}
			}
		}
		srcWriter.outdent();
		srcWriter.println("}");
	}

	private void listPublicFields(JField[] fields) {
		for (JField field : fields) {
			if (field.isPublic()) {
				this.publicFields.put(field.getName(), field.getType());
				this.propertyTypes.put(field.getName(), field.getType());
				this.addImport(field.getType());
			}
		}
	}

	private void listSetters(JMethod[] methods) {
		for (JMethod method : methods) {
			if (method.getName().startsWith("set") && method.getParameters().length == 1
				&& method.getReturnType().equals(JPrimitiveType.VOID) && method.isPublic()) {
				this.setters.put(this.extractPropertyNameFromMethod(method), method);
				this.propertyTypes.put(this.extractPropertyNameFromMethod(method), method.getParameters()[0].getType());
				this.addImport(method.getParameters()[0].getType());
			}
		}
	}

	private void listGetters(JMethod[] methods) {
		for (JMethod method : methods) {
			if (method.getName().startsWith("get") || method.getName().startsWith("is") && method.getParameters().length == 0
				&& !method.getReturnType().equals(JPrimitiveType.VOID) && method.isPublic()) {
				this.getters.put(this.extractPropertyNameFromMethod(method), method);
				this.propertyTypes.put(this.extractPropertyNameFromMethod(method), method.getReturnType());
				this.addImport(method.getReturnType());
			}
		}
	}

	private void addImport(JType type) {
		JParameterizedType parametrizedType = type.isParameterized();
		if (parametrizedType != null) {
			this.imports.add(parametrizedType.getRawType());
			this.imports.addAll(Lists.newArrayList(parametrizedType.getTypeArgs()));
		} else {
			this.imports.add(type);
		}
	}

	private String extractPropertyNameFromMethod(JMethod method) {
		String propertyName = method.getName();
		if (propertyName.startsWith("is")) {
			propertyName = propertyName.substring(2, propertyName.length());
		} else {
			propertyName = propertyName.substring(3, propertyName.length());
		}
		String firstChar = propertyName.substring(0, 1);
		propertyName = propertyName.replaceFirst(firstChar, firstChar.toLowerCase());

		return propertyName;
	}

	private SourceWriter getSourceWriter(PrintWriter printWriter, GeneratorContext ctx) {
		String packageName =
			this.proxyModelQualifiedName.indexOf('.') == -1 ? "" : this.proxyModelQualifiedName.substring(0,
				this.proxyModelQualifiedName.lastIndexOf('.'));
		String className =
			this.proxyModelQualifiedName.indexOf('.') == -1 ? this.proxyModelQualifiedName : this.proxyModelQualifiedName
				.substring(this.proxyModelQualifiedName.lastIndexOf('.') + 1, this.proxyModelQualifiedName.length());

		ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, className);

		composerFactory.addImport(Map.class.getName());
		composerFactory.addImport(Maps.class.getName());
		composerFactory.addImport(GWT.class.getName());
		composerFactory.addImport(Model.class.getName());
		composerFactory.addImport(AbstractModel.class.getName());
		composerFactory.addImport(ModelCollection.class.getName());
		composerFactory.addImport(PropertyDescription.class.getName());
		composerFactory.addImport(PrimitiveUtils.class.getName());
		composerFactory.addImport(this.beanType.getQualifiedSourceName());
		composerFactory.addImport("fr.putnami.pwt.core.editor.client.validator.*");

		for (JType jType : this.imports) {
			if (jType.isPrimitive() != null) {
				continue;
			}
			if ("?".equals(jType.getSimpleSourceName())) {
				continue;
			}
			composerFactory.addImport(jType.getQualifiedSourceName());
		}
		for (String submodel : this.subModels.values()) {
			composerFactory.addImport(submodel);
		}

		composerFactory
			.setSuperclass(AbstractModel.class.getSimpleName() + "<" + this.beanType.getSimpleSourceName() + ">");
		return composerFactory.createSourceWriter(ctx, printWriter);
	}

	private PrintWriter getPrintWriter(TreeLogger logger, GeneratorContext ctx, String targetQualifiedName) {
		String packageName =
			this.proxyModelQualifiedName.indexOf('.') == -1 ? "" : this.proxyModelQualifiedName.substring(0,
				this.proxyModelQualifiedName.lastIndexOf('.'));
		int lastIndex = this.proxyModelQualifiedName.lastIndexOf('.');
		String className =
			lastIndex == -1 ? this.proxyModelQualifiedName : this.proxyModelQualifiedName.substring(lastIndex + 1,
				this.proxyModelQualifiedName.length());

		return ctx.tryCreate(logger, packageName, className);
	}

	public static String createSubModel(TreeLogger logger, GeneratorContext context, JType jType) {
		if (jType == null) {
			return null;
		}
		if (jType.isEnum() != null) {
			return null;
		}
		if (ModelCreator.DISCARD_MODEL_TYPES.contains(jType.getQualifiedSourceName())) {
			return null;
		}
		if (jType instanceof JClassType) {
			ModelCreator creator = new ModelCreator((JClassType) jType);
			return creator.create(logger, context);
		}
		return null;
	}

}
