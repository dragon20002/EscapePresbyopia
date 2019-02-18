package kr.or.hanium.chungbukhansung.escapepresbyopiaserver;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;

@SpringBootApplication
public class EscapePresbyopiaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EscapePresbyopiaServerApplication.class, args);
	}
	
	@Bean
	public ImageAnnotatorClient imageAnnotatorClient(CredentialsProvider credentialsProvider) throws IOException {
		ImageAnnotatorSettings clientSettings = ImageAnnotatorSettings.newBuilder()
				.setCredentialsProvider(credentialsProvider)
				.build();

		return ImageAnnotatorClient.create(clientSettings);
	}
}
