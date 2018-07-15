package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;

public class SpeechActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private ImageButton btnStop, btnPlay;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        Intent intent = getIntent();
        String imagePath = intent.getStringExtra("imagePath"); //이미지 경로
        String text = intent.getStringExtra("text"); //추출한 텍스트 내용
        String speechPath = intent.getStringExtra("speechPath"); //음성파일 경로

        // 사용자가 선택한 이미지를 보여준다
        ImageView imageView = findViewById(R.id.speechImageView);

        // 이미지뷰가 자동으로 90도 회전되서 나오는 현상 해결
        int exifDegree = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientation2Degree(exifOrientation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth())), true);
        Bitmap rotated = rotate(scaled, exifDegree);
        imageView.setImageBitmap(rotated);

        // 추출한 텍스트 내용을 보여준다
        TextView textView = findViewById(R.id.speechTextView);
        textView.setText(text);

        // 음성 파일을 재생하는 플레이어 설정
        btnStop = findViewById(R.id.speechBtnStop); //정지버튼
        btnStop.setOnClickListener(this);

        btnPlay = findViewById(R.id.speechBtnPlay); //재생 및 일시정지 버튼
        btnPlay.setOnClickListener(this);

        player = MediaPlayer.create(this, Uri.parse(speechPath)); //mp3파일을 재생하는 미디어 플레이어
        player.setOnCompletionListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
        case R.id.speechBtnStop:
            player.pause();
            onCompletion(player);
            break;
        case R.id.speechBtnPlay:
            if (player.isPlaying()) {
                player.pause();
                btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
            } else {
                player.start();
                btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
            }
            break;
        default:
        }
    }

    // mp3 재생이 끝나면 호출된다
    @Override
    public void onCompletion(MediaPlayer mp) {
        player.seekTo(0);
        btnPlay.setImageDrawable(getDrawable(android.R.drawable.ic_media_play));
    }

    /* 이미지가 90도 회전되는 현상 처리 */
    private int exifOrientation2Degree(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}