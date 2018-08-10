package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Map;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.activity.WaitingActivity;
import kr.or.hanium.chungbukhansung.escapepresbyopia.view.EPDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadImageTask implements Callback<Map<String, String>>,EPDialog.DialogButtonListener {

    private final WaitingActivity activity;
    private final String imagePath;

    public UploadImageTask(WaitingActivity activity, String imagePath) {
        this.activity = activity;
        this.imagePath = imagePath;
        request();
    }

    private void request() {
        File file = new File(imagePath);

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(), reqFile);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String voiceId = prefs.getString("voiceId", "Seoyeon");

        // HTTP 프로토콜로 서버에 이미지를 전송한다
        WaitingActivity.service.postImage(image, voiceId).enqueue(this);
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
            String textMeta = map.get("textMeta"); //이미지에서 추출한 텍스트 위치
            String audio = map.get("audio"); //서버에 저장된 음성 파일의 URL
            String audioMeta = map.get("audioMeta"); //텍스트가 음성파일 몇 초에 나타나는지 있는 메타파일

            // 음성 파일 다운로드 쓰레드 실행
            new DownloadSpeechTask(activity, imagePath, text, textMeta, audio, audioMeta).execute();
            break;
        default:
        }
    }

    // 네트워크 연결 등의 문제로 변환 요청이 실패한 경우 호출된다
    @Override
    public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
        t.printStackTrace();
        new EPDialog()
                .setListener(this)
                .setMessage("네트워크 연결을 확인해주세요.\n다시 시도하시겠습니까?")
                .show(activity.getFragmentManager(), "음성 변환 실패");
    }

    @Override
    public void onDialogPositive() {
        request();
    }

    @Override
    public void onDialogNegative() {
        activity.finish();
    }

}
