package com.rokid.opencvtest

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.camera.view.PreviewView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.rokid.aicoredemo.R
import com.rokid.opencvtest.preview.BaseCameraXActivity
import kotlinx.android.synthetic.main.activity_camerax_demo.*

/** Helper type alias used for analysis use case callbacks */

class AICoreCameraXActivity : BaseCameraXActivity() {
    //帧数据是否保存为图片
    var mCheckPreviewImage = false
    //每次发送数据时，需要使用不同的frame id
    var mCurrFrameId = 0
    //是否有勾选
    var mHasDetectTypeChecked = false
    var tvOrb :TextView ?= null

    override fun getPreviewView(): PreviewView {
        return camerax_view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_camerax_demo)
        //手机不锁屏
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        tvOrb = findViewById(R.id.tv_orb) as TextView
        showSystemUI()

        //此处待优化
        super.onCreate(savedInstanceState)
    }

    //app启动后只需要确认权限一次
    var mHasRequestPerms = false
    override fun onStart() {
        super.onStart()
        //权限申请
        if (!mHasRequestPerms){
            XXPermissions.with(this)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.CAMERA)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: List<String>, all: Boolean) {}

                    override fun onDenied(permissions: List<String>, never: Boolean) {
                    }
                })

            mHasRequestPerms = true;
        }

    }



    // 数据返回
    override fun handleNV21Data(nv21: ByteArray, width: Int, height: Int) {
        var orbsize = 0;
        tvOrb?.text = "ORB 特征点数: ===>>> ${orbsize}"
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    companion object {
        private const val TAG = "AICoreCameraXActivity"
    }

}
