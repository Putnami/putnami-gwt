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
package fr.putnami.pwt.plugin.spring.web.filter;

import com.google.common.collect.Lists;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class DelegatingChainFilter extends GenericFilterBean {

	private final List<Filter> filters = Lists.newArrayList();

	@Autowired
	private WebApplicationContext webApplicationContext;

	private void scanBean(Object bean, String name) {
		Class<?> implClass = bean.getClass();
		if (AopUtils.isAopProxy(bean)) {
			implClass = AopUtils.getTargetClass(bean);
		}
		if (Filter.class.isAssignableFrom(implClass)) {
			filters.add((Filter) bean);
		}
	}

	@Override
	protected void initFilterBean() throws ServletException {
		findWebApplicationContext();
		for (String beanName : webApplicationContext.getBeanNamesForType(Filter.class)) {
			scanBean(webApplicationContext.getBean(beanName), beanName);
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
		ServletException {
		new IterableFilterChain(filters, chain).doFilter(req, res);
	}

	protected WebApplicationContext findWebApplicationContext() {
		if (this.webApplicationContext == null) {
			webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		}
		return webApplicationContext;
	}

	private class IterableFilterChain implements FilterChain {
		private final FilterChain defaultChain;
		private Iterator<Filter> filterIterator;

		IterableFilterChain(List<Filter> filters, FilterChain chain) {
			this.filterIterator = filters.iterator();
			this.defaultChain = chain;
		}

		@Override
		public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
			if (filterIterator.hasNext()) {
				Filter filter = filterIterator.next();
				filter.doFilter(req, res, this);
			} else {
				defaultChain.doFilter(req, res);
			}
		}
	}
}
