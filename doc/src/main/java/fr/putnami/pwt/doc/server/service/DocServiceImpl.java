package fr.putnami.pwt.doc.server.service;

import org.springframework.stereotype.Service;

import fr.putnami.pwt.doc.shared.service.DocService;

@Service
public class DocServiceImpl implements DocService {

	@Override
	public String sayHi(String name) {
		return "Welcome " + name + "!";
	}

}
