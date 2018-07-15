package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Image2SpeechService {

    @POST("/api/image")
    @Multipart
    Call<Map<String, String>> postImage(@Part MultipartBody.Part image);
}
