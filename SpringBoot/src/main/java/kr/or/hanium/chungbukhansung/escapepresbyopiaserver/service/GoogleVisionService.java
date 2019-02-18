package kr.or.hanium.chungbukhansung.escapepresbyopiaserver.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.Vertex;
import com.google.cloud.vision.v1.Word;
import com.google.protobuf.ByteString;

@Service
public class GoogleVisionService {
	
	@Autowired
	private ImageAnnotatorClient imageAnnotatorClient;
	
	public String[] getDocumentText(ByteString byteString) throws IOException {

		// request 생성
		List<AnnotateImageRequest> requests = new ArrayList<>();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
				.addFeatures(Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build())
				.setImage(Image.newBuilder().setContent(byteString).build())
				.build();
		requests.add(request);

		// google cloud platform에 request 전송
		String[] texts = { "", "" };
		BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(requests);
		List<AnnotateImageResponse> responses = response.getResponsesList();

		for (AnnotateImageResponse res : responses) {
			if (res.hasError()) {
				System.out.printf("Error: %s\n", res.getError().getMessage());
				return null;
			}

			// For full list of available annotations, see http://g.co/cloud/vision/docs
			TextAnnotation annotation = res.getFullTextAnnotation();
			for (Page page : annotation.getPagesList()) {
				String pageText = "";
				for (Block block : page.getBlocksList()) {
					String blockText = "";
					for (Paragraph para : block.getParagraphsList()) {
						String paraText = "";
						for (Word word : para.getWordsList()) {
							String wordText = "";
							for (Symbol symbol : word.getSymbolsList()) {
								wordText = wordText + symbol.getText();
							}
							if (!wordText.equals("|")) {
								if (wordText.equals(".") || wordText.equals("?") || wordText.equals("!") || wordText.equals(","))
									paraText = String.format("%s%s", paraText, wordText);
								else
									paraText = String.format("%s %s", paraText, wordText);
							}

							texts[1] += String.format("{\"text\":\"%s\",\"box\":[", wordText); 
							for (Vertex vertex : word.getBoundingBox().getVerticesList())
								texts[1] += String.format("{\"x\":%d,\"y\":%d},", vertex.getX(), vertex.getY());
							texts[1] += "]}\n";
						}
						blockText = blockText + paraText + "\n";
					}
					pageText = pageText + blockText;
				}
				texts[0] += pageText;
			}
		}

		return texts;
	}

}
