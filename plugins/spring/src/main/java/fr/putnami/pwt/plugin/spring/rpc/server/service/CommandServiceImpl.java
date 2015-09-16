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

import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import fr.putnami.pwt.core.service.server.service.CommandExecutor;
import fr.putnami.pwt.core.service.server.service.CommandExecutorRegistry;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.service.CommandService;

public class CommandServiceImpl implements CommandService {

	@Autowired
	private CommandExecutorRegistry executorRegistry;

	@Override
	public List<CommandResponse> executeCommands(List<CommandRequest> commands) {
		List<CommandResponse> result = Lists.newArrayList();
		for (CommandRequest request : commands) {
			CommandExecutor executor = this.executorRegistry.resolveCommandExecutor(request.getCommandDefinition());
			result.add(executor.executeCommand(request));
		}
		return result;
	}

}
