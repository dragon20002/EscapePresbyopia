package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v7.widget.AppCompatImageButton;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.model.AudioMetaItem;
import kr.or.hanium.chungbukhansung.escapepresbyopia.utils.ImageEditor;

public class SpeechActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private TextView speechTextView;

    private AppCompatImageButton btnPlay;
    private MediaPlayer player;

    private String imagePath;
    private String text, textMeta;
    private List<AudioMetaItem> audioMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        /* 데이터 초기화 */
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath"); //이미지 경로
        text = intent.getStringExtra("text");
        textMeta = intent.getStringExtra("textMeta");
        String speechPath = intent.getStringExtra("speechPath");
        String audioMetaPath = intent.getStringExtra("audioMetaPath");

        audioMeta = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(audioMetaPath));
            for (String line = reader.readLine(); line != null; line = reader.readLine())
                audioMeta.add(new Gson().fromJson(line, AudioMetaItem.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 뷰 초기화 */
        // 사용자가 선택한 이미지를 보여준다
        ImageView speechImageView = findViewById(R.id.speechImageView);

        // 이미지뷰가 자동으로 90도 회전되서 나오는 현상 해결
        int exifDegree = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = ImageEditor.exifOrientation2Degree(exifOrientation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth())), true);
        Bitmap rotated = ImageEditor.rotate(scaled, exifDegree);
        speechImageView.setImageBitmap(rotated);

        // 이미지 풀스크린 전환 버튼
        AppCompatImageButton btnFullScreen = findViewById(R.id.speechImageFullScreenButton);
        btnFullScreen.setOnClickListener(this);

        // 텍스트 크기 조절 버튼
        AppCompatImageButton btnScaleDown = findViewById(R.id.speechTextScaleDownButton);
        btnScaleDown.setOnClickListener(this);
        AppCompatImageButton btnScaleUp = findViewById(R.id.speechTextScaleUpButton);
        btnScaleUp.setOnClickListener(this);

        // 추출한 텍스트 내용을 보여준다
        speechTextView = findViewById(R.id.speechTextView);
        speechTextView.setText(text);

        // 음성 파일을 재생하는 플레이어 설정
        AppCompatImageButton btnStop = findViewById(R.id.speechBtnStop);
        btnStop.setOnClickListener(this);

        btnPlay = findViewById(R.id.speechBtnPlay); //재생 및 일시정지 버튼
        btnPlay.setOnClickListener(this);

        try {
            player = MediaPlayer.create(this, Uri.parse(speechPath)); //mp3파일을 재생하는 미디어 플레이어
            player.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            player.pause();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        float size;

        switch (v.getId()) {
        case R.id.speechImageFullScreenButton:
            startActivity(new Intent(this, FullscreenActivity.class)
                    .putExtra("imagePath", imagePath));
            break;

        case R.id.speechTextScaleDownButton:
            size = speechTextView.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 2;
            if (size > 0)
                speechTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            break;

        case R.id.speechTextScaleUpButton:
            size = speechTextView.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 2;
            if (size < 72)
                speechTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            break;

        case R.id.speechBtnStop:
            player.pause();
            onCompletion(player);
            break;

        case R.id.speechBtnPlay:
            if (player.isPlaying()) {
                player.pause();
                btnPlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
            } else {
                player.start();
                btnPlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
            }
            break;

        default:
        }
    }

    // mp3 재생이 끝나면 호출된다
    @Override
    public void onCompletion(MediaPlayer mp) {
        player.seekTo(0);
        btnPlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
    }

}
