package kr.or.hanium.chungbukhansung.escapepresbyopiaserver.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SpeechMarkType;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;

//https://docs.aws.amazon.com/ko_kr/sdk-for-java/v1/developer-guide/credentials.html
@Service
public class AWSPollyService {
	private static final String END_POINT = "https://polly.ap-northeast-2.amazonaws.com";
	private static final String REGION = "ap-northeast-2";
	private static final String API_KEY_FILE = "/var/lib/tomcat8/apikey/credentials";
	//private static final String API_KEY_FILE = "D://haruu//apikey//credentials";
	
	private AmazonPolly client = AmazonPollyClientBuilder.standard()
			.withEndpointConfiguration(new EndpointConfiguration(END_POINT, REGION))
			.withCredentials(new ProfileCredentialsProvider(API_KEY_FILE, "default"))
			.build();

	public List<String> getLanguagesAndVoices() {
		List<String> lvList = new ArrayList<>();
		
		DescribeVoicesRequest request = new DescribeVoicesRequest();
		
		try {
			String nextToken;
			do {
				DescribeVoicesResult result = client.describeVoices(request);
				nextToken = result.getNextToken();
				request.setNextToken(nextToken);

				for (Voice v : result.getVoices())
					lvList.add(String.format("%s,%s", v.getLanguageName(), v.getName()));

			} while (nextToken != null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lvList;
	}

	public String synthesizeSpeechMarks(String text, String voiceId) {
		SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
				.withOutputFormat(OutputFormat.Json).withSpeechMarkTypes(SpeechMarkType.Word)
				.withVoiceId(voiceId).withText(text);

		SynthesizeSpeechResult synthesizeSpeechResult = client.synthesizeSpeech(synthesizeSpeechRequest);
		byte[] buffer = new byte[2048];
		int readBytes;

		String audioMeta = "";
		try (InputStream in = synthesizeSpeechResult.getAudioStream()) {
			while ((readBytes = in.read(buffer)) > 0) {
				audioMeta += new String(buffer, 0, readBytes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return audioMeta;
	}

	public void synthesizeSpeech(String fileName, String text, String voiceId) {
		SynthesizeSpeechRequest synthesizeSpeechRequest = new SynthesizeSpeechRequest()
				.withOutputFormat(OutputFormat.Mp3)
				.withVoiceId(voiceId).withText(text);

		File file = new File(fileName);
		file.deleteOnExit();
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			SynthesizeSpeechResult synthesizeSpeechResult = client.synthesizeSpeech(synthesizeSpeechRequest);
			byte[] buffer = new byte[2048];
			int readBytes;

			try (InputStream in = synthesizeSpeechResult.getAudioStream()) {
				while ((readBytes = in.read(buffer)) > 0) {
					outputStream.write(buffer, 0, readBytes);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception caught: " + e);
		}
	}

}
