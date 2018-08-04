package kr.or.hanium.chungbukhansung.escapepresbyopia.service;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

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
import kr.or.hanium.chungbukhansung.escapepresbyopia.view.EPDialog;

@SuppressLint("StaticFieldLeak")
class DownloadSpeechTask extends AsyncTask<String, Void, Void> implements EPDialog.DialogButtonListener {
    private final WaitingActivity activity;
    private final String imagePath;
    private String text, textMeta, audio, audioMeta;

    /**
     * @param text 이미지에서 추출한 텍스트 내용
     * @param textMeta 이미지에서 추출한 텍스트 위치
     * @param audio 서버에 저장된 음성 파일의 URL (MP3)
     * @param audioMeta 텍스트가 음성파일 몇 초에 나타나는지 있는 메타파일 (JSON)
     */
    DownloadSpeechTask(WaitingActivity activity, String imagePath, String text, String textMeta, String audio, String audioMeta) {
        this.activity = activity;
        this.imagePath = imagePath;
        this.text = text;
        this.textMeta = textMeta;
        this.audio = audio;
        this.audioMeta = audioMeta;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            String speechPath = download(audio, "mp3");
            String audioMetaPath = download(audioMeta, "json");

            // 음성 재생 화면으로 이동
            Intent intent = new Intent(activity, SpeechActivity.class);
            intent.putExtra("imagePath", imagePath); //사용자가 선택한 이미지 경로
            intent.putExtra("text", text); //이미지에서 추출한 텍스트 내용
            intent.putExtra("textMeta", textMeta);
            intent.putExtra("speechPath", speechPath); //음성 파일 경로
            intent.putExtra("audioMetaPath", audioMetaPath);
            activity.startActivity(intent);
            activity.finish();

        } catch (IOException e) {
            e.printStackTrace();
            new EPDialog()
                    .setListener(this)
                    .setMessage("네트워크 연결을 확인해주세요.\n다시 시도하시겠습니까?")
                    .show(activity.getFragmentManager(), "음성 받기 실패");
        }
        return null;
    }

    private String download(String fileURL, String fileType) throws IOException {
        // url을 통해 파일 다운로드
        URL speechUrl = new URL(fileURL);
        URLConnection conn = speechUrl.openConnection();
        conn.connect();

        InputStream input = new BufferedInputStream(speechUrl.openStream());

        // 파일 이름 설정
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String speechFileName = fileType + timeStamp;
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = File.createTempFile(speechFileName, "." + fileType, storageDir);
        file.deleteOnExit();
        String filePath = file.getAbsolutePath();

        FileOutputStream output = new FileOutputStream(file);

        // 파일 다운로드
        byte data[] = new byte[8192];
        int len;
        while ((len = input.read(data)) != -1)
            output.write(data, 0, len);
        output.flush();

        input.close();
        output.close();

        return filePath;
    }

    @Override
    public void onDialogPositive() {
        new DownloadSpeechTask(activity, imagePath, text, textMeta, audio, audioMeta).execute();
    }

    @Override
    public void onDialogNegative() {
        activity.finish();
    }

}
