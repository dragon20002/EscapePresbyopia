package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.service.Image2SpeechService;
import kr.or.hanium.chungbukhansung.escapepresbyopia.service.UploadImageTask;
import kr.or.hanium.chungbukhansung.escapepresbyopia.view.EPDialog;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *  Waiting Activity >> Upload Image Callback >> Download Speech
 */
public class WaitingActivity extends Activity implements UploadImageTask.TaskListener {
    public static Image2SpeechService service;

    private UploadImageTask task;

    public ImageView imageView; //변환 대기 시간동안 눈 운동 정보가 있는 이미지를 볼 수 있다
    public TextView textView; //현재 진행상태를 텍스트로 설명한다

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        imageView = findViewById(R.id.waitingImageView);
        textView = findViewById(R.id.waitingTextView);

        // 이전 화면(MainActivity)로부터 사용자가 선택한 이미지 경로를 가져온다
        String imagePath = getIntent().getStringExtra("imagePath");

        // 서버와 연결
        if (service == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            Retrofit retrofit = new Retrofit.Builder().baseUrl(Image2SpeechService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).client(client).build();
            service = retrofit.create(Image2SpeechService.class);
        }

        // 이미지 업로드
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String voiceId = prefs.getString("voiceId", "Seoyeon");
        task = new UploadImageTask(this);
        task.request(imagePath, voiceId);
    }

    @Override
    public void onResponse(String imagePath, String text, String textMeta, String audio, String audioMeta) {
        textView.setText(getString(R.string.waiting_speech));

        // 음성 재생 화면으로 이동
        Intent intent = new Intent(this, SpeechActivity.class);
        intent.putExtra("imagePath", imagePath); //사용자가 선택한 이미지 경로
        intent.putExtra("text", text); //이미지에서 추출한 텍스트 내용
        intent.putExtra("textMeta", textMeta);
        intent.putExtra("audio", audio); //음성 파일 경로
        intent.putExtra("audioMeta", audioMeta);
        startActivity(intent);
        finish();
    }

    // 네트워크 연결 등의 문제로 변환 요청이 실패한 경우 호출된다
    @Override
    public void onFailure(final String imagePath) {
        new EPDialog()
                .setMessage("네트워크 연결을 확인해주세요.\n다시 시도하시겠습니까?")
                .setListener(new EPDialog.DialogButtonListener() {
                    @Override
                    public void onDialogPositive() {
                        // 이미지 업로드
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WaitingActivity.this);
                        String voiceId = prefs.getString("voiceId", "Seoyeon");
                        task.request(imagePath, voiceId);
                    }

                    @Override
                    public void onDialogNegative() {
                        finish();
                    }
                })
                .show(getFragmentManager(), "음성 변환 실패");
    }
}
