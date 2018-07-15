package kr.or.hanium.chungbukhansung.escapepresbyopia.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.or.hanium.chungbukhansung.escapepresbyopia.R;
import kr.or.hanium.chungbukhansung.escapepresbyopia.adapter.MainMenuAdapter;
import kr.or.hanium.chungbukhansung.escapepresbyopia.model.MainMenuItem;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * 메뉴 선택화면
 * - 사진 찍기
 * - 사진 선택
 * - 눈 건강 정보
 * - 설정
 */
public class MainActivity extends Activity implements RecyclerView.OnItemTouchListener {
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int RC_CAMERA_WRITE_AND_INTERNET = 0;

    private String mCurrentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 1. 메뉴 화면 설정 */
        RecyclerView menuRv;
        RecyclerView.LayoutManager layoutManager; //화면 모양 (가로 or 세로)
        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) { //세로 모드
            menuRv = findViewById(R.id.mainMenuRecyclerView);
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else { //가로 모드
            menuRv = findViewById(R.id.mainLandMenuRecyclerView);
            layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        }
        menuRv.setHasFixedSize(true); //사이즈 고정 (메뉴 크기에 변경되지 않음)
        menuRv.setLayoutManager(layoutManager); //위에서 설정한 화면 모양 적용

        /* 2. 메뉴 리스트(menuItems)에 사진찍기, 사진선택 등 메뉴 추가 */
        List<MainMenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MainMenuItem(getResources().getColor(R.color.menu_camera), R.drawable.ic_menu_camera, R.string.menu_camera));
        menuItems.add(new MainMenuItem(getResources().getColor(R.color.menu_gallery), R.drawable.ic_menu_gallery, R.string.menu_gallery));
        menuItems.add(new MainMenuItem(getResources().getColor(R.color.menu_eye_info), R.drawable.ic_menu_eye_info, R.string.menu_eye_info));
        menuItems.add(new MainMenuItem(getResources().getColor(R.color.menu_settings), R.drawable.ic_menu_settings, R.string.menu_settings));

        // Adapter : 메뉴 리스트를 메뉴 화면에 붙여준다
        RecyclerView.Adapter rvAdapter = new MainMenuAdapter(menuItems);
        menuRv.setAdapter(rvAdapter);

        /* 3. 메뉴 터치 이벤트 등록 */
        menuRv.addOnItemTouchListener(this);
    }

    //-- 메뉴 터치 이벤트 --//
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        onTouchEvent(rv, e);
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View view = rv.findChildViewUnder(e.getX(), e.getY());
        if (view == null) {
            for (int i = 0, n = rv.getChildCount(); i < n; i++)
                rv.getChildAt(i).setElevation(11);
            return;
        }

        int s = e.getAction() * 4 + rv.getChildLayoutPosition(view);
        switch (s) {
            /* ACTION_DOWN */
            case 0: case 1: case 2: case 3:
                view.setElevation(4);
                break;
            /* ACTION_UP */
            case 4:
                dispatchTakePictureIntent();
                view.setElevation(11);
                break;
            case 5:
                dispatchGalleryIntent();
                view.setElevation(11);
                break;
            case 6:
                Toast.makeText(this, "eye info", Toast.LENGTH_SHORT).show();
                view.setElevation(11);
                break;
            case 7:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                view.setElevation(11);
                break;
            default:
        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    //-- 각 메뉴의 기능 실행 --//
    /* 1. 사진 찍기 */
    private void dispatchTakePictureIntent() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_description), RC_CAMERA_WRITE_AND_INTERNET, permissions);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                @SuppressWarnings("SpellCheckingInspection") @SuppressLint("SimpleDateFormat")
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);
                photoFile.deleteOnExit();
                mCurrentImagePath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {

                @SuppressWarnings("SpellCheckingInspection") Uri photoURI = FileProvider.getUriForFile(this,
                        "kr.or.hanium.chungbukhansung.escapepresbyopia.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(Intent.createChooser(takePictureIntent, "카메라 선택"), REQUEST_TAKE_PHOTO);
            }
        } else {
            Toast.makeText(this, "기기에서 카메라앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /* 2. 사진 선택 */
    private void dispatchGalleryIntent() {
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE};
        if (!EasyPermissions.hasPermissions(this, permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_description), RC_CAMERA_WRITE_AND_INTERNET, permissions);
            return;
        }

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(galleryIntent, "이미지 선택"), REQUEST_PICK_IMAGE);
    }

    //-- 사진 찍기나 사진 선택 후 호출된다 --//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        Intent intent = new Intent(this, WaitingActivity.class);

        switch (requestCode) {
        case REQUEST_TAKE_PHOTO: //사진 찍기
            intent.putExtra("imagePath", mCurrentImagePath);
            startActivity(intent); //WaitingActivity로 이동
            break;
        case REQUEST_PICK_IMAGE: //사진 선택
            mCurrentImagePath = getRealPathFromUri(data.getData());
            intent.putExtra("imagePath", mCurrentImagePath);
            startActivity(intent); //WaitingActivity로 이동
            break;
        default:
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;

        int col = 0;
        if(cursor.moveToFirst())
            col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        String realPath = cursor.getString(col);
        cursor.close();
        return realPath;
    }

    //-- 권한 요청 --//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}
