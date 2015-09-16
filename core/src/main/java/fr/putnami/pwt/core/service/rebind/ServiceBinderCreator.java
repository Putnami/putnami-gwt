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
package fr.putnami.pwt.core.service.rebind;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.JParameterizedType;
import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcToken.RpcTokenImplementation;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import fr.putnami.pwt.core.model.rebind.ModelCreator;
import fr.putnami.pwt.core.service.client.AbstractServiceBinder;
import fr.putnami.pwt.core.service.client.CallbackAdapter;
import fr.putnami.pwt.core.service.client.CommandController;
import fr.putnami.pwt.core.service.client.CommandParam;
import fr.putnami.pwt.core.service.client.annotation.AsyncHandler;
import fr.putnami.pwt.core.service.client.annotation.LazyCommand;
import fr.putnami.pwt.core.service.client.annotation.QuietCommand;
import fr.putnami.pwt.core.service.shared.domain.CommandDefinition;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;

public class ServiceBinderCreator {

	private final JClassType serviceType;
	private final JClassType serviceBinderType;
	private final JClassType handlerType;

	private String proxyModelQualifiedName;

	private Collection<JType> imports = Sets.newHashSet();

	public ServiceBinderCreator(JClassType serviceBinderType, JClassType serviceType, JClassType handlerType) {
		this.serviceBinderType = serviceBinderType;
		this.serviceType = serviceType;
		this.handlerType = handlerType;
		this.proxyModelQualifiedName =
			this.handlerType.getQualifiedSourceName() + "_" + this.serviceType.getSimpleSourceName() + "_ServiceBinder";
	}

	public String create(TreeLogger logger, GeneratorContext context) throws UnableToCompleteException,
		NotFoundException {
		PrintWriter printWriter = this.getPrintWriter(logger, context);
		if (printWriter == null) {
			return this.proxyModelQualifiedName;
		}

		this.collectImports();

		SourceWriter srcWriter = this.getSourceWriter(printWriter, context);
		srcWriter.indent();
		srcWriter.println();
		this.createSerializer(logger, context, srcWriter);
		srcWriter.println();
		this.generateServiceImplementation(logger, srcWriter);
		this.generateServiceImplementationWithCallback(logger, srcWriter);

		srcWriter.outdent();
		srcWriter.commit(logger);
		return this.proxyModelQualifiedName;
	}

	private void createSerializer(TreeLogger logger, GeneratorContext context, SourceWriter srcWriter)
		throws UnableToCompleteException,
		NotFoundException {

		final Collection<JType> typesToSerialize = Sets.newHashSet();

		JMethod[] methods = this.serviceType.getOverridableMethods();
		TypeOracle typeOracle = context.getTypeOracle();

		JClassType rteType = typeOracle.getType(RpcTokenException.class.getName());
		JClassType rpcTokenClass = typeOracle.getType(RpcToken.class.getName());
		RpcTokenImplementation tokenClassToUse = this.serviceType.findAnnotationInTypeHierarchy(
			RpcTokenImplementation.class);
		if (tokenClassToUse != null) {
			JClassType rpcTokenType = typeOracle.getType(tokenClassToUse.value());
			if (rpcTokenType.isAssignableTo(rpcTokenClass)) {
				typesToSerialize.add(rpcTokenType);
				typesToSerialize.add(rteType);
			} else {
				logger.branch(TreeLogger.ERROR,
					"RPC token class " + tokenClassToUse.value() + " must implement " + RpcToken.class.getName(), null);
				throw new UnableToCompleteException();
			}
		} else {
			JClassType[] rpcTokenSubclasses = rpcTokenClass.getSubtypes();
			for (JClassType rpcTokenSubclass : rpcTokenSubclasses) {
				typesToSerialize.add(rpcTokenSubclass);
			}
			if (rpcTokenSubclasses.length > 0) {
				typesToSerialize.add(rteType);
			}
		}
		typesToSerialize.add(typeOracle.getType(CommandDefinition.class.getName()));
		typesToSerialize.add(typeOracle.getType(CommandRequest.class.getName()));
		typesToSerialize.add(typeOracle.getType(CommandResponse.class.getName()));

		for (JMethod method : methods) {
			JType returnType = method.getReturnType();
			if (returnType != JPrimitiveType.VOID) {
				extractAllType(returnType, typesToSerialize);
			}
			for (JParameter param : method.getParameters()) {
				JType paramType = param.getType();
				extractAllType(paramType, typesToSerialize);
			}
			for (JType ex : method.getThrows()) {
				extractAllType(ex, typesToSerialize);
			}
		}

		srcWriter.println("static {");
		srcWriter.indent();
		srcWriter.println(
			"fr.putnami.pwt.core.serialization.ppc.shared.MarshallerRegistry registry = fr.putnami.pwt.core.serialization.ppc.client.PpcClientSerializer.get().getMarshallerRegistry();");

		for (JType jType : typesToSerialize) {
			String modelClassName = ModelCreator.createSubModel(logger, context, jType);
			if (modelClassName != null) {
				srcWriter.println("fr.putnami.pwt.core.serialization.ppc.client.ModelMarshaller.register(registry, new %s());",
					modelClassName);
			}
			if (jType.isEnum() != null) {
				srcWriter.println(
					"registry.register(new fr.putnami.pwt.core.serialization.ppc.client.EnumMarshaller(%s.class));",
					jType.getQualifiedBinaryName());
			}
		}
		srcWriter.outdent();
		srcWriter.println("}");
	}

	private void extractAllType(JType type, Collection<JType> collect) {
		if (collect.contains(type)) {
			return;
		}

		collect.add(type);

		if (type instanceof JParameterizedType) {
			JParameterizedType parameterizedType = (JParameterizedType) type;
			for (JType paramType : parameterizedType.getTypeArgs()) {
				extractAllType(paramType, collect);
			}
		}
	}

	private static class CallbackMethod {
		String successMethodName;
		String failureMethodName;

		@Override
		public int hashCode() {
			return Objects.hashCode(this.successMethodName, this.failureMethodName);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CallbackMethod) {
				CallbackMethod other = (CallbackMethod) obj;
				return Objects.equal(this.successMethodName, other.successMethodName)
					&& Objects.equal(this.failureMethodName, other.failureMethodName);
			}
			return false;
		}
	}

	private Collection<CallbackMethod> listCallbackMethods(TreeLogger logger, JMethod serviceMethod) {
		Collection<CallbackMethod> callbackMethods = Sets.newHashSet();
		for (JMethod handlerMethod : this.handlerType.getOverridableMethods()) {
			AsyncHandler serviceHandlerAnnotation = handlerMethod.getAnnotation(AsyncHandler.class);
			if (serviceHandlerAnnotation == null) {
				continue;
			}
			if (handlerMethod.getParameterTypes() == null || handlerMethod.getParameterTypes().length != 1) {
				logger.log(Type.WARN, "the service handler " + this.handlerType.getSimpleSourceName() + "."
					+ handlerMethod.getName() + " skipped : shall have one parameter");
				continue;
			}
			String methodName = serviceHandlerAnnotation.method();
			if (AsyncHandler.DEFAULT_METHOD_NAME.equals(methodName)) {
				methodName = handlerMethod.getName();
				methodName = methodName.replaceFirst("on", "");
				if (methodName.lastIndexOf("Event") != -1) {
					methodName = methodName.substring(0, methodName.lastIndexOf("Event"));
				} else if (methodName.lastIndexOf("Thrown") != -1) {
					methodName = methodName.substring(0, methodName.lastIndexOf("Thrown"));
				}
				methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
			}

			if (serviceMethod.getName().equals(methodName)) {
				JType paramType = handlerMethod.getParameterTypes()[0];
				String paramQualifiedName = paramType.getQualifiedSourceName();
				JType returnType = serviceMethod.getReturnType();
				String returnQualifiedName = returnType.getQualifiedSourceName();
				if (returnType instanceof JPrimitiveType) {
					returnQualifiedName = ((JPrimitiveType) returnType).getQualifiedBoxedSourceName();
				}

				if (returnQualifiedName.equals(paramQualifiedName)) {
					CallbackMethod method = new CallbackMethod();
					method.successMethodName = handlerMethod.getName();
					callbackMethods.add(method);
					continue;
				} else if (Throwable.class.getCanonicalName().equals(paramQualifiedName)) {
					CallbackMethod method = new CallbackMethod();
					method.failureMethodName = handlerMethod.getName();
					callbackMethods.add(method);
				}
			}
		}

		return callbackMethods;
	}

	private void generateServiceImplementationWithCallback(TreeLogger logger, SourceWriter srcWriter)
		throws UnableToCompleteException {
		for (JMethod method : this.serviceBinderType.getMethods()) {
			JParameter[] methodParams = method.getParameters();
			JParameter callbackParam = null;
			if (methodParams != null && methodParams.length > 0) {
				callbackParam = methodParams[methodParams.length - 1];
			}
			if (callbackParam == null) {
				break;
			}
			this.writeStartMethod(srcWriter, method);
			this.writeCommandDefinition(logger, srcWriter, method, callbackParam);
			this.writeCommandParam(srcWriter, method, null, callbackParam);
			this.writeEndMethod(srcWriter, method);
		}
	}

	private void generateServiceImplementation(TreeLogger logger, SourceWriter srcWriter)
		throws UnableToCompleteException {
		Set<String> addedMethods = Sets.newHashSet();
		for (JMethod method : this.serviceType.getOverridableMethods()) {
			if (!addedMethods.contains(method.getName())) {
				addedMethods.add(method.getName());
				this.writeStartMethod(srcWriter, method);
				this.writeCommandDefinition(logger, srcWriter, method, null);
				this.writeCommandParam(srcWriter, method, this.listCallbackMethods(logger, method), null);
				this.writeEndMethod(srcWriter, method);
			}
		}
	}

	private void writeStartMethod(SourceWriter srcWriter, JMethod method) {
		srcWriter.print("public %s %s(", this.typeAsString(method.getReturnType(), false), method.getName());
		int i = 0;
		for (JParameter parameter : method.getParameters()) {
			if (i++ > 0) {
				srcWriter.print(", ");
			}
			srcWriter.print("%s $%d_%s", this.typeAsString(parameter.getType(), false), i, parameter.getName());
		}
		srcWriter.println("){");
		srcWriter.indent();
	}

	private void writeEndMethod(SourceWriter srcWriter, JMethod method) {
		srcWriter.println("CommandController.get().invokeCommand(commandDefinition, commandParam);");
		if (method.getReturnType().equals(JPrimitiveType.BOOLEAN)) {
			srcWriter.println("return false;");
		} else if (method.getReturnType().equals(JPrimitiveType.BYTE) || method.getReturnType().equals(JPrimitiveType.CHAR)
			|| method.getReturnType().equals(JPrimitiveType.DOUBLE) || method.getReturnType().equals(JPrimitiveType.FLOAT)
			|| method.getReturnType().equals(JPrimitiveType.INT) || method.getReturnType().equals(JPrimitiveType.LONG)
			|| method.getReturnType().equals(JPrimitiveType.SHORT)) {
			srcWriter.println("return 0;");
		} else if (!method.getReturnType().equals(JPrimitiveType.VOID)) {
			srcWriter.println("return null;");
		}
		srcWriter.outdent();
		srcWriter.println("}");
	}

	private void writeCommandDefinition(TreeLogger logger, SourceWriter srcWriter, JMethod method,
		JParameter callbackParameter) throws UnableToCompleteException {
		srcWriter.print("CommandDefinition commandDefinition  = new CommandDefinition(");
		srcWriter.print("\"%s\", ", this.serviceType.getQualifiedSourceName());
		srcWriter.print("\"%s\", ", method.getName());
		if (callbackParameter != null) {
			JParameterizedType parameterizedCallback = callbackParameter.getType().isParameterized();
			if (parameterizedCallback != null) {
				srcWriter.print("\"%s\" ", parameterizedCallback.getTypeArgs()[0].getQualifiedSourceName());
			} else {
				logger.branch(TreeLogger.ERROR, "Callback argument type for method " + method.getName()
					+ " is not parametrized", null);
				throw new UnableToCompleteException();
			}

		} else {
			srcWriter.print("\"%s\" ", method.getReturnType().getQualifiedSourceName());
		}
		for (JParameter parameter : method.getParameters()) {
			if (!parameter.equals(callbackParameter)) {
				srcWriter.print(", \"%s\"", parameter.getType().getQualifiedSourceName());
			}
		}
		srcWriter.println(");");
	}

	private void writeCommandParam(SourceWriter srcWriter, JMethod method, Collection<CallbackMethod> callbackSuccess,
		JParameter callbackParameter) {
		boolean lazy = method.getAnnotation(LazyCommand.class) != null;
		boolean quiet = method.getAnnotation(QuietCommand.class) != null;

		srcWriter.print("CommandParam commandParam = new CommandParam(");
		srcWriter.print("%s, ", lazy);
		srcWriter.print("%s, ", quiet);
		srcWriter.print("Lists.newArrayList(Arrays.asList(");
		int i = 0;
		for (JParameter parameter : method.getParameters()) {
			if (!parameter.equals(callbackParameter)) {
				if (i++ > 0) {
					srcWriter.print(", ");
				}
				srcWriter.print("$%d_%s", i, parameter.getName());
			}
		}
		srcWriter.println(")), ");
		srcWriter.indent();
		srcWriter.println("Lists.newArrayList(");
		srcWriter.indent();
		i = 0;
		if (callbackParameter != null) {
			srcWriter.print("$%d_%s", method.getParameters().length, callbackParameter.getName());
			i++;
		}

		if (callbackSuccess != null) {
			for (CallbackMethod callbackMethod : callbackSuccess) {
				if (i++ > 0) {
					srcWriter.print(", ");
				}
				srcWriter.println("new CallbackAdapter<%s>(){", this.typeAsString(method.getReturnType(), true));
				srcWriter.indent();
				if (callbackMethod.successMethodName != null) {
					srcWriter.println("public void onSuccess(%s result){", this.typeAsString(method.getReturnType(), true));
					srcWriter.indent();
					srcWriter.println("getHandler().%s(result);", callbackMethod.successMethodName);
					srcWriter.outdent();
					srcWriter.println("}");
				}
				if (callbackMethod.failureMethodName != null) {
					srcWriter
						.println("public void onFailure(Throwable caught){", this.typeAsString(method.getReturnType(), true));
					srcWriter.indent();
					srcWriter.println("getHandler().%s(caught);", callbackMethod.failureMethodName);
					srcWriter.outdent();
					srcWriter.println("}");
				}
				srcWriter.outdent();
				srcWriter.print("}");
			}
		}
		srcWriter.outdent();
		srcWriter.println("));");
		srcWriter.outdent();
	}

	private String typeAsString(JType type, boolean translatePrimitives) {
		StringBuilder sb = new StringBuilder();
		if (translatePrimitives && type instanceof JPrimitiveType) {
			sb.append(((JPrimitiveType) type).getQualifiedBoxedSourceName());
		} else {
			sb.append(type.getSimpleSourceName());
			if (type instanceof JParameterizedType) {
				JParameterizedType parameterizedType = (JParameterizedType) type;
				sb.append("<");
				int i = 0;
				for (JType paramType : parameterizedType.getTypeArgs()) {
					if (i++ > 0) {
						sb.append(", ");
					}
					sb.append(this.typeAsString(paramType, false));
				}
				sb.append(">");
			}
		}
		return sb.toString();
	}

	private void collectImports() {
		Collection<JType> toImports = Sets.newHashSet();
		for (JMethod method : this.serviceType.getOverridableMethods()) {
			toImports.add(method.getReturnType());
			for (JParameter parameter : method.getParameters()) {
				toImports.add(parameter.getType());
			}
		}
		this.imports.addAll(toImports);
		for (JType importType : this.imports) {
			if (importType instanceof JParameterizedType) {
				JParameterizedType parameterizedType = (JParameterizedType) importType;
				for (JType paramType : parameterizedType.getTypeArgs()) {
					toImports.add(paramType);
				}
			}
		}
		this.imports.addAll(toImports);
	}

	private SourceWriter getSourceWriter(PrintWriter printWriter, GeneratorContext ctx) {

		String packageName = this.handlerType.getPackage().getName();
		String className =
			this.proxyModelQualifiedName.indexOf('.') == -1 ? this.proxyModelQualifiedName : this.proxyModelQualifiedName
				.substring(
					this.proxyModelQualifiedName.lastIndexOf('.') + 1, this.proxyModelQualifiedName.length());

		ClassSourceFileComposerFactory composerFactory = new ClassSourceFileComposerFactory(packageName, className);

		composerFactory.addImport(AsyncCallback.class.getName());

		composerFactory.addImport(AbstractServiceBinder.class.getName());
		composerFactory.addImport(Arrays.class.getName());
		composerFactory.addImport(Lists.class.getName());
		composerFactory.addImport(CommandDefinition.class.getName());
		composerFactory.addImport(CommandParam.class.getName());
		composerFactory.addImport(CallbackAdapter.class.getName());
		composerFactory.addImport(CommandController.class.getName());

		composerFactory.addImport(this.serviceType.getQualifiedSourceName());
		composerFactory.addImport(this.serviceBinderType.getQualifiedSourceName());

		for (JType jType : this.imports) {
			if (jType.isPrimitive() != null) {
				continue;
			}
			composerFactory.addImport(jType.getQualifiedSourceName());
		}

		composerFactory.setSuperclass(AbstractServiceBinder.class.getSimpleName()
			+ "<" + this.handlerType.getSimpleSourceName() + ", " + this.serviceType.getSimpleSourceName() + ">");

		composerFactory.addImplementedInterface(this.serviceType.getSimpleSourceName());
		composerFactory.addImplementedInterface(this.serviceBinderType.getSimpleSourceName());

		return composerFactory.createSourceWriter(ctx, printWriter);
	}

	private PrintWriter getPrintWriter(TreeLogger logger, GeneratorContext ctx) {
		String packageName =
			this.proxyModelQualifiedName.indexOf('.') == -1 ? "" : this.proxyModelQualifiedName.substring(0,
				this.proxyModelQualifiedName.lastIndexOf('.'));
		String className = this.proxyModelQualifiedName.indexOf('.') == -1 ? this.proxyModelQualifiedName
			: this.proxyModelQualifiedName.substring(
				this.proxyModelQualifiedName.lastIndexOf('.') + 1, this.proxyModelQualifiedName.length());

		return ctx.tryCreate(logger, packageName, className);
	}

}
