package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;

/**
 * 앱 전체 화면 흐름
 * InitialActivity >> MainActivity >> WaitingActivity >> SpeechActivity
 *
 * 앱 초기화면
 * 1.5초 후 MainActivity 화면으로 전환된다.
 */
public class InitialActivity extends Activity {
    // 하단 진행바
    public ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        progressBar = findViewById(R.id.initialProgressBar);

        // 진행바를 진행시키는 쓰레드 실행
        new InitialActivityTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    public class InitialActivityTask extends AsyncTask<Void, Integer, Void> {
        private final int HOLDING_TIME = 500;

        // 쓰레드 시작 전에 호출된다
        @Override
        protected void onPreExecute() {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setMax(HOLDING_TIME);
            }
        }

        // 쓰레드 : 뷰 이외의 코드를 수행하는 쓰레드
        @Override
        protected Void doInBackground(Void... aVoid) {
            long startTimeMillis = System.currentTimeMillis();
            long currentTimeMillis = startTimeMillis;
            long finishTimeMillis = startTimeMillis + HOLDING_TIME;

            while (currentTimeMillis < finishTimeMillis) {
                long pastTimeMillis = currentTimeMillis;
                currentTimeMillis = System.currentTimeMillis();

                publishProgress((int) (currentTimeMillis - pastTimeMillis)); // >> onProgressUpdate 호출

                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        // doInBackground 함수에서 publishProgress()를 호출하면 호출된다
        // 화면에 관련된 코드를 수행한다
        @Override
        protected void onProgressUpdate(Integer... diffs) {
            if (progressBar != null)
                progressBar.incrementProgressBy(diffs[0]);
        }

        // 쓰레드 종료 후에 호출된다
        @Override
        protected void onPostExecute(Void aVoid) {
            startActivity(new Intent(InitialActivity.this, MainActivity.class));
            finish();
        }
    }
}
