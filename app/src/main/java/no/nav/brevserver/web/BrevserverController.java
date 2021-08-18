package no.nav.brevserver.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class BrevserverController {

	@Value("${brevserver.server}")
	private String server;

	@GetMapping("/StartBrevKlient.jsp")
	public String startBrevklient(@RequestParam(name = "systemid") String systemid, @RequestParam(name = "dokid") String dokid, @RequestParam(name = "token") String token, @RequestParam(name = "height", required = false) String height, @RequestParam(name = "width", required = false) String width, Model model) {
		model.addAttribute("server", server);
		model.addAttribute("systemid", systemid);
		model.addAttribute("token", token);
		model.addAttribute("dokid", dokid);
		model.addAttribute("height", height!=null?height:800);
		model.addAttribute("width", width!=null?width:30);
		return "brevserver";
	}


	@GetMapping("/StartBrevklientApplet.jar")
	public ResponseEntity<Resource> serverApplet(HttpServletResponse response) throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream("StartBrevklientApplet.jar");
		ByteArrayResource resource = new ByteArrayResource(is.readAllBytes());
		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=StartBrevklientApplet.jar");
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		MediaType mediaType = new MediaType("application", "java-archive");
		return ResponseEntity.ok()
				.headers(header)
				.contentType(mediaType)
				.contentLength(resource.getByteArray().length)
				.body(resource);
	}

}