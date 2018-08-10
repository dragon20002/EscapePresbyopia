package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface Image2SpeechService {
    String BASE_URL = "http://my.ip.num.ber:8080";
    String CONTEXT = "ep-server";

    @GET(CONTEXT + "/api/image2speech")
    Call<List<String>> getLanguagesAndVoices();

    @POST(CONTEXT + "/api/image2speech/{voiceId}")
    @Multipart
    Call<Map<String, String>> postImage(@Part MultipartBody.Part image, @Path("voiceId") String voiceId);
}
