package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.model.Meta;
import kr.or.hanium.chungbukhansung.escapepresbyopia.utils.ImageEditor;

public class SpeechActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private TextView speechTextView;

    private AppCompatImageButton btnPlay;
    private MediaPlayer player;
    private RetrieveMediaPlayerTimeTask task;

    private String imagePath;
    private String text;
    private List<Meta> metas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        /* 데이터 초기화 */
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath"); //이미지 경로
        text = intent.getStringExtra("text");
        String textMeta = intent.getStringExtra("textMeta");
        String audio = intent.getStringExtra("audio");
        String audioMeta = intent.getStringExtra("audioMeta");
        metas = Meta.createMetas(textMeta, audioMeta);

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

        /* 미디어 플레이어 생성 */
        try {
            player = MediaPlayer.create(this, Uri.parse(audio)); //mp3파일을 재생하는 미디어 플레이어
            player.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "음성을 받아오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            btnStop.setEnabled(false);
            btnPlay.setEnabled(false);
        }
        task = null;
    }

    @Override
    public void onBackPressed() {
        try {
            player.stop();
            player = null;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        // 전체화면
        case R.id.speechImageFullScreenButton:
            startActivity(new Intent(this, FullscreenActivity.class)
                    .putExtra("imagePath", imagePath));
            break;

        // 글자 크기 조절
        case R.id.speechTextScaleDownButton:
            float size = speechTextView.getTextSize() / getResources().getDisplayMetrics().scaledDensity - 2;
            if (size > 0)
                speechTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            break;

        case R.id.speechTextScaleUpButton:
            size = speechTextView.getTextSize() / getResources().getDisplayMetrics().scaledDensity + 2;
            if (size < 72)
                speechTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            break;

        // 미디어 버튼
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
                if (task == null) {
                    task = new RetrieveMediaPlayerTimeTask();
                    task.execute();
                }
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
        if (task != null) {
            task.running = false;
            task = null;
        }
        btnPlay.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
    }

    @SuppressLint("StaticFieldLeak")
    private class RetrieveMediaPlayerTimeTask extends AsyncTask<Void, Meta, Void> {
        private boolean running = true;
        private int start = 0, end = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            if (metas.size() < 1) return null;

            while (running) {
                try {
                    Thread.sleep(333);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int current = player.getCurrentPosition();

                if (start <= current && current <= end) continue;

                Meta meta = null, nextMeta = metas.get(0);
                for (Meta m : metas) {
                    meta = nextMeta;
                    nextMeta = m;
                    if (meta.getAudio().time <= current && current <= nextMeta.getAudio().time) break;
                }

                start = (meta != null) ? meta.getAudio().time : 0;
                end = nextMeta.getAudio().time;

                // TODO update text view and image.
                publishProgress(meta, nextMeta);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Meta... metas) {
            Meta meta = metas[0], nextMeta = metas[1];
            Log.i(SpeechActivity.class.getName(), String.format(Locale.KOREAN, "%s (%d ~ %d)", meta.getText().text, start, end));
        }
    }

}
