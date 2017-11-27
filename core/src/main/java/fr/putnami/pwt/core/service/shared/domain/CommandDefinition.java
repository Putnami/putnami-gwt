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
package fr.putnami.pwt.core.service.shared.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

public class CommandDefinition implements Serializable {

	private String serviceName;
	private String methodName;
	private List<String> paramTypes;
	private String returnType;

	public CommandDefinition() {
	}

	public CommandDefinition(String serviceName, String methodName, String returnType, String... paramTypes) {
		super();
		this.serviceName = serviceName;
		this.methodName = methodName;
		this.returnType = returnType;
		this.paramTypes = Lists.newArrayList(paramTypes);
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getReturnType() {
		return this.returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public List<String> getParamTypes() {
		return this.paramTypes;
	}

	public void setParamTypes(List<String> paramTypes) {
		this.paramTypes = paramTypes;
	}

	@Override
	public String toString() {
		MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(CommandDefinition.class);
		helper.add("service", this.serviceName);
		helper.add("method", this.methodName);
		helper.add("return", this.returnType);
		helper.add("params", this.paramTypes);
		return helper.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.serviceName, this.methodName, this.returnType, this.paramTypes);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommandDefinition) {
			CommandDefinition other = (CommandDefinition) obj;
			return Objects.equal(this.serviceName, other.serviceName)
				&& Objects.equal(this.methodName, other.methodName)
				&& Objects.equal(this.returnType, other.returnType)
				&& Objects.equal(this.paramTypes, other.paramTypes);
		}
		return false;
	}

}
