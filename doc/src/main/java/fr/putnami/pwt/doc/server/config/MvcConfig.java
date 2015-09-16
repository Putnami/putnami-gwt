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
package fr.putnami.pwt.doc.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import fr.putnami.pwt.doc.server.controller.PutnamiController;
import fr.putnami.pwt.doc.server.service.DocServiceImpl;
import fr.putnami.pwt.doc.shared.service.DocService;

@Configuration
@EnableWebMvc
public class MvcConfig extends WebMvcConfigurerAdapter {

	private static final int RESOURCE_CACHE_PERIOD = 60 * 60 * 24 * 7; // 1 week in second

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("favicon.ico")
			.addResourceLocations("/assets/favicon.ico").setCachePeriod(RESOURCE_CACHE_PERIOD);
		registry.addResourceHandler("robots.txt")
			.addResourceLocations("assets/robots.txt").setCachePeriod(RESOURCE_CACHE_PERIOD);
		registry.addResourceHandler("/assets/**")
			.addResourceLocations("/assets/").setCachePeriod(RESOURCE_CACHE_PERIOD);
		registry.addResourceHandler("/Documentation/**")
			.addResourceLocations("/Documentation/").setCachePeriod(RESOURCE_CACHE_PERIOD);
	}

	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/pages/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Bean
	public PutnamiController putnamiController() {
		return new PutnamiController();
	}

	@Bean
	public DocService docService() {
		return new DocServiceImpl();
	}

	@Bean
	public CharacterEncodingFilter characterEncodingFilter() {
		CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
		characterEncodingFilter.setEncoding("UTF-8");
		characterEncodingFilter.setForceEncoding(true);
		return characterEncodingFilter;
	}
}
