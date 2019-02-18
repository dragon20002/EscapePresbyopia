package kr.or.hanium.chungbukhansung.escapepresbyopiaserver.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.google.protobuf.ByteString;

import kr.or.hanium.chungbukhansung.escapepresbyopiaserver.service.AWSPollyService;
import kr.or.hanium.chungbukhansung.escapepresbyopiaserver.service.GoogleVisionService;

@RestController
@RequestMapping("/api/image2speech")
public class Image2SpeechController {
	private static final String CONTEXT = "ep-server";
	
	@Autowired
	GoogleVisionService gvService;
	
	@Autowired
	AWSPollyService apService;

	@Autowired
	private ServletContext context;
	
	@GetMapping("")
	ResponseEntity<List<String>> getLanguagesAndVoices() {
		return new ResponseEntity<>(apService.getLanguagesAndVoices(), HttpStatus.OK);
	}

	@PostMapping("/{voiceId}")
    ResponseEntity<Map<String, String>> postImage(HttpServletRequest request, @RequestParam("image") MultipartFile image, @PathVariable("voiceId") String voiceId) throws IOException {
		Map<String, String> map = new HashMap<>();

		/* Google VISION */
		ByteString byteString = ByteString.readFrom(image.getInputStream());
		String[] texts = gvService.getDocumentText(byteString);
		if (texts == null || texts[0] == null)
			new ResponseEntity<>(HttpStatus.NO_CONTENT);

		map.put("text", texts[0]);
		map.put("textMeta", texts[1]);

		/* AWS POLLY */
		// 경로 확인
		String realPath = context.getRealPath("/");
		String saveDir = "files/audio/";
		File dir = new File(realPath + saveDir);
		if (!dir.exists())
			dir.mkdirs();

		// 파일명 생성
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
		String date = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss").format(new Date());
		String fileName = String.format("%s_%s", sessionId, date);

		// Text to Speech
		apService.synthesizeSpeech(realPath + saveDir + fileName + ".mp3", texts[0], voiceId);
		String audioMeta = apService.synthesizeSpeechMarks(texts[0], voiceId);

		String filesBase = String.format("http://%s:%s/%s/files/audio", request.getServerName(), request.getServerPort(), CONTEXT);
		map.put("audio", String.format("%s/%s.mp3", filesBase, fileName));
		map.put("audioMeta", audioMeta);

		return new ResponseEntity<>(map, HttpStatus.OK);
	}

}
