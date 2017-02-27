package com.morgan.demo.screenshot;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.morgan.lib.base.BaseScreenShotActivity;
import com.morgan.lib.dir.FileManager;
import com.morgan.lib.util.DateUtils;
import com.morgan.lib.util.ImageUtils;
import com.morgan.lib.util.ToastUtils;
import com.morgan.main.R;

public class ScreenShotActivity extends BaseScreenShotActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_activity_screen_shot);
    }

    public void onClick(View v) {
        startScreenShot();
    }

    @Override
    public void onComplete(Bitmap bm) {
        ImageUtils.saveImage(
                null,
                FileManager.getInstance().getMainPath() + File.separator
                        + DateUtils.getCurrentTime(DateUtils.FORMAT_NO_SYMBOL) + ".jpg");
        ToastUtils.toast(R.string.ok);
    }
}
