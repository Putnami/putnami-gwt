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
package fr.putnami.pwt.plugin.spring.rpc.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import fr.putnami.pwt.core.service.server.service.CommandExecutorRegistry;
import fr.putnami.pwt.core.service.server.service.CommandExecutorRegistryImpl;
import fr.putnami.pwt.core.service.shared.service.CommandService;
import fr.putnami.pwt.plugin.spring.rpc.server.controller.CommandController;
import fr.putnami.pwt.plugin.spring.rpc.server.filter.RequestContextFilter;
import fr.putnami.pwt.plugin.spring.rpc.server.service.CommandServiceImpl;
import fr.putnami.pwt.plugin.spring.rpc.server.service.CommandServiceScanProcessor;

@Configuration
public class ComandServiceConfig {
	@Bean
	@Order(Ordered.LOWEST_PRECEDENCE)
	public CommandController commandServiceController() {
		return new CommandController();
	}

	@Bean
	public RequestContextFilter requestContextInterceptor() {
		return new RequestContextFilter();
	}

	@Bean
	public CommandExecutorRegistry commandExecutorRegistry() {
		return new CommandExecutorRegistryImpl();
	}

	@Bean
	@Autowired
	public CommandServiceScanProcessor serviceScanProcessor(CommandExecutorRegistry registry) {
		CommandServiceScanProcessor serviceScanProcessor = new CommandServiceScanProcessor();
		serviceScanProcessor.setRegistry(registry);
		return serviceScanProcessor;
	}

	@Bean
	public CommandService commandService() {
		return new CommandServiceImpl();
	}

}
