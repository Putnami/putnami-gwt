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
package fr.putnami.pwt.plugin.spring.rpc.server.service;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;

import javax.annotation.PostConstruct;

import fr.putnami.pwt.core.service.server.service.CommandExecutorRegistry;

@Component
public class CommandServiceScanProcessor extends InitDestroyAnnotationBeanPostProcessor
		implements InstantiationAwareBeanPostProcessor {

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private CommandExecutorRegistry registry;

	private Class<? extends Annotation> serviceAnnotation;

	public CommandServiceScanProcessor() {
		this(Service.class);
	}

	public CommandServiceScanProcessor(Class<? extends Annotation> serviceAnnotation) {
		this.serviceAnnotation = serviceAnnotation;
	}

	@PostConstruct
	public void afterPropertySet() {
		for (String beanName : this.applicationContext.getBeanNamesForAnnotation(this.serviceAnnotation)) {
            this.scanBean(this.applicationContext.getBean(beanName), beanName);
		}
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		scanBean(bean, beanName);
		return true;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean,
			String beanName) throws BeansException {
		return pvs;
	}

	public void setRegistry(CommandExecutorRegistry registry) {
		this.registry = registry;
	}

	private void scanBean(Object bean, String name) {
		Class<?> implClass = bean.getClass();
		if (AopUtils.isAopProxy(bean)) {
			implClass = AopUtils.getTargetClass(bean);
		} 
		Annotation serviceAnnotation = AnnotationUtils.findAnnotation(implClass, this.serviceAnnotation);
		if (serviceAnnotation != null) {
			for (Class<?> inter : implClass.getInterfaces()) {
				registry.injectService(inter, bean);
			}
		}
	}

}
