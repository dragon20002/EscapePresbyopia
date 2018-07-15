package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.activity.WaitingActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageTask implements Callback<Map<String, String>> {

    private final WaitingActivity activity;
    private final String imagePath;

    public UploadImageTask(WaitingActivity activity, String imagePath) {
        this.activity = activity;
        this.imagePath = imagePath;

        File file = new File(imagePath);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        // HTTP 프로토콜로 서버에 이미지를 전송한다
        WaitingActivity.service.postImage(image).enqueue(this);
    }

    // 서버로부터 응답이 오면 호출된다
    @Override
    public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
        int code = response.code();
        switch (code) {
        case 200: //HTTP 상태코드 OK
            activity.textView.setText(activity.getString(R.string.waiting_speech));
            Map<String, String> map = response.body();
            if (map == null) return;

            String text = map.get("text"); //이미지에서 추출한 텍스트 내용
            String speechUrlStr = map.get("url"); //서버에 저장된 음성 파일의 URL

            // 음성 파일 다운로드 쓰레드 실행
            new DownloadSpeechTask(activity, imagePath).execute(text, speechUrlStr);
            break;
        default:
        }
    }

    // 네트워크 연결 등의 문제로 변환 요청이 실패한 경우 호출된다
    @Override
    public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
        t.printStackTrace();
        // TODO material retry dialog
        Toast.makeText(activity, "음성 변환에 실패했습니다.\n네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
        activity.finish();
    }
}
