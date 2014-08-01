package fr.putnami.pwt.plugin.spring.file.server.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.io.Files;

import fr.putnami.pwt.core.widget.shared.domain.FileDto;

@Component
public class DefaultFileTransfertStore implements FileTransfertStore {

	@Value("${filetransfertcontroller.tempdir}")
	private File tempdir;

	private final Map<String, FileDto> files = Maps.newConcurrentMap();

	@PostConstruct
	public void postConstruct() {
		clearFiles(tempdir);
	}

	@PreDestroy
	public void destroy() {
		clearFiles(tempdir);
	}

	@Override
	public InputStream read(String fileId) {
		try {
			return new FileInputStream(new File(tempdir, fileId));
		}
		catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public OutputStream write(String fileId, String fileName, String contentType) {
		try {
			FileDto fileDto = new FileDto();
			fileDto.setToken(fileId);
			fileDto.setName(fileName);
			fileDto.setMime(contentType);
			fileDto.setExtension(FilenameUtils.getExtension(fileName));

			files.put(fileId, fileDto);

			File target = new File(tempdir, fileId);
			Files.createParentDirs(target);
			return new FileOutputStream(target);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public FileDto getFileBean(String fileId) {
		FileDto dto = files.get(fileId);
		if (dto == null) {
			return null;
		}
		dto.setContentLength(new File(tempdir, fileId).length());
		return dto;
	}

	private void clearFiles(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				clearFiles(child);
			}
			if (file.listFiles().length == 0) {
				file.delete();
			}
		}
		else if (file.isFile()) {
			file.delete();
		}
	}

}
