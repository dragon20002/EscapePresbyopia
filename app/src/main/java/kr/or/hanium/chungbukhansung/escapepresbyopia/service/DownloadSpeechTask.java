package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.or.hanium.chungbukhansung.escapepresbyopia.activity.SpeechActivity;
import kr.or.hanium.chungbukhansung.escapepresbyopia.activity.WaitingActivity;

@SuppressLint("StaticFieldLeak")
class DownloadSpeechTask extends AsyncTask<String, Void, Void> {
    private final WaitingActivity activity;
    private final String imagePath;

    DownloadSpeechTask(WaitingActivity activity, String imagePath) {
        this.activity = activity;
        this.imagePath = imagePath;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String text = strings[0]; //이미지에서 추출한 텍스트 내용
        String speechUrlStr = strings[1]; //서버에 음성 파일이 저장된 url

        try {
            // url을 통해 음성 파일 다운로드
            URL speechUrl = new URL(speechUrlStr);
            URLConnection conn = speechUrl.openConnection();
            conn.connect();

            InputStream input = new BufferedInputStream(speechUrl.openStream());

            // 음성 파일 이름 설정
            @SuppressLint("SimpleDateFormat")
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String speechFileName = "MP3_" + timeStamp + "_";
            File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File speechFile = File.createTempFile(speechFileName, ".mp3", storageDir);
            speechFile.deleteOnExit();
            String speechPath = speechFile.getAbsolutePath();

            FileOutputStream output = new FileOutputStream(speechFile);

            // 음성 파일 다운로드
            byte data[] = new byte[8192];
            int len;
            while ((len = input.read(data)) != -1)
                output.write(data, 0, len);
            output.flush();

            // 음성 재생 화면으로 이동
            Intent intent = new Intent(activity, SpeechActivity.class);
            intent.putExtra("imagePath", imagePath); //사용자가 선택한 이미지 경로
            intent.putExtra("text", text); //이미지에서 추출한 텍스트 내용
            intent.putExtra("speechPath", speechPath); //음성 파일 경로
            activity.startActivity(intent);
            activity.finish();

            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO material retry dialog
            Toast.makeText(activity, "음성을 받는데 실패했습니다.\n다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        return null;
    }
}
