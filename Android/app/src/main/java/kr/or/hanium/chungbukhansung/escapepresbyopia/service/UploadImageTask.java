package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.or.hanium.chungbukhansung.escapepresbyopia.activity.WaitingActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageTask implements Callback<Map<String, String>> {
    public interface TaskListener {
        void onResponse(String imagePath, String text, String textMeta, String audio, String audioMeta);
        void onFailure(String imagePath);
    }

    private TaskListener listener;
    private List<String> imagePaths;

    public UploadImageTask(TaskListener listener) {
        this.listener = listener;
        imagePaths = new ArrayList<>();
    }

    public void request(String imagePath, String voiceId) {
        imagePaths.add(imagePath);
        File file = new File(imagePath);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        // HTTP 프로토콜로 서버에 이미지를 전송한다
        WaitingActivity.service.postImage(image, voiceId).enqueue(this);
    }

    // 서버로부터 응답이 오면 호출된다
    @Override
    public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
        if (imagePaths.size() < 1) return;
        String imagePath = imagePaths.remove(0);

        switch (response.code()) {
        case 200: //HTTP 상태코드 OK
            Map<String, String> map = response.body();
            if (map == null) return;
            listener.onResponse(imagePath,
                    map.get("text"), map.get("textMeta"),
                    map.get("audio"), map.get("audioMeta"));
            break;
        default:
        }
    }

    // 네트워크 연결 등의 문제로 변환 요청이 실패한 경우 호출된다
    @Override
    public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
        t.printStackTrace();
        String imagePath = imagePaths.remove(0);
        listener.onFailure(imagePath);
    }

}
