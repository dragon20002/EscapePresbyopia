package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.service.Image2SpeechService;
import kr.or.hanium.chungbukhansung.escapepresbyopia.service.UploadImageTask;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *  Waiting Activity >> Upload Image Callback >> Download Speech
 */
public class WaitingActivity extends Activity {
    public static Image2SpeechService service;

    public ImageView imageView; //변환 대기 시간동안 눈 운동 정보가 있는 이미지를 볼 수 있다
    public TextView textView; //현재 진행상태를 텍스트로 설명한다

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        imageView = findViewById(R.id.waitingImageView);
        textView = findViewById(R.id.waitingTextView);

        // 이전 화면(MainActivity)로부터 사용자가 선택한 이미지 경로를 가져온다
        String mCurrentImagePath = getIntent().getStringExtra("imagePath");

        // 서버와 연결
        if (service == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://192.168.0.200:8080")
                    .addConverterFactory(GsonConverterFactory.create()).client(client).build();
            service = retrofit.create(Image2SpeechService.class);
        }

        // 이미지 업로드 콜백 등록
        new UploadImageTask(this, mCurrentImagePath);
    }
}
