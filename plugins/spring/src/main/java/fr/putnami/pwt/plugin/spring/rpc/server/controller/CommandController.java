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
package fr.putnami.pwt.plugin.spring.rpc.server.controller;

import com.google.gwt.user.server.rpc.RPCServletUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.putnami.pwt.core.serialization.ppc.server.PpcServerSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcReader;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcSerializer;
import fr.putnami.pwt.core.serialization.ppc.shared.PpcWriter;
import fr.putnami.pwt.core.service.shared.domain.CommandRequest;
import fr.putnami.pwt.core.service.shared.domain.CommandResponse;
import fr.putnami.pwt.core.service.shared.service.CommandService;

@Controller
public class CommandController {
	private static final int BUFFER_SIZE = 4096;

	private final Log logger = LogFactory.getLog(this.getClass());

	private static final PpcSerializer serializer = new PpcServerSerializer();

	@Autowired
	private CommandService commandService;

	@RequestMapping(value = "/commandService", method = RequestMethod.POST, consumes = "application/x-ppc")
	public void processPostRpc(HttpServletRequest request, HttpServletResponse response,
		@RequestBody String payload)
		throws Throwable {

			PpcReader reader = serializer.newReader();
			reader.prepare(payload);
			List<CommandRequest> commands = reader.readObject();
			List<CommandResponse> responses = commandService.executeCommands(commands);

			PpcWriter writer = serializer.newWriter();
			writer.write(responses);

			String responseContent = writer.flush();
			byte[] responseBytes = responseContent.getBytes(RPCServletUtils.CHARSET_UTF8);

			boolean gzipEncode =
				RPCServletUtils.acceptsGzipEncoding(request)
					&& RPCServletUtils.exceedsUncompressedContentLengthLimit(responseContent);

			if (gzipEncode) {
				ByteArrayOutputStream output = null;
				GZIPOutputStream gzipOutputStream = null;
				try {
					output = new ByteArrayOutputStream(responseBytes.length);
					gzipOutputStream = new GZIPOutputStream(output);
					gzipOutputStream.write(responseBytes);
					gzipOutputStream.finish();
					gzipOutputStream.flush();
					response.setHeader("Content-Encoding", "gzip");
					responseBytes = output.toByteArray();
				} catch (IOException e) {
					logger.error("Unable to compress response", e);
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					return;
				} finally {
					if (null != gzipOutputStream) {
						gzipOutputStream.close();
					}
					if (null != output) {
						output.close();
					}
				}
			}

			// Send the reply.
			response.setContentLength(responseBytes.length);
			response.setContentType("application/json; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			response.setHeader("Content-Disposition", "attachment");
			response.getOutputStream().write(responseBytes);
	}

}
