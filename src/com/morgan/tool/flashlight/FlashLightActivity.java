package com.morgan.tool.flashlight;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.morgan.lib.base.BaseActionBarActivity;
import com.morgan.lib.util.HardwareUtils;
import com.morgan.lib.util.ToastUtils;
import com.morgan.main.R;

/**
 * 手电筒功能界面, 使用时不要忘了申请相应权限
 * 
 * @author Morgan.Ji
 * @version 1.0
 * @date 2016-01-10
 */
public class FlashLightActivity extends BaseActionBarActivity {

    private Button mSwitchBtn;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tool_activity_flash_light);
        mSwitchBtn = (Button) findViewById(R.id.flash_light_switch);
        mSwitchBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null == mCamera) {
                    mCamera = HardwareUtils.openFlightLight();
                    mSwitchBtn.setText(R.string.close);
                } else {
                    HardwareUtils.closeFlightLight(mCamera);
                    mCamera = null;
                    mSwitchBtn.setText(R.string.open);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mCamera && !HardwareUtils.isBackCameraAvailable()) {
            mSwitchBtn.setEnabled(false);
            ToastUtils.toastLong(this, R.string.tip_device_has_no_flash);
        }
    }

    @Override
    protected void onDestroy() {
        if (null != mCamera) {
            HardwareUtils.closeFlightLight(mCamera);
        }
        super.onDestroy();
    }
}
