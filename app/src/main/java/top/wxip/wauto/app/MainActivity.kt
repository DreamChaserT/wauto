package top.wxip.wauto.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.AppUtils

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        val ctx = applicationContext;
        super.onCreate(savedInstanceState)

        // 隐藏标题栏
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        // 读取微信版本号
        val wechatVersion = AppUtils.getAppVersionName(Const.wechatPackageName)
        if (wechatVersion.isNotBlank()) {
            val tvWechatVersion = findViewById<TextView>(R.id.tv_wechat_version)
            tvWechatVersion.text = "微信版本号:$wechatVersion"
        }

        val btnStart = findViewById<Button>(R.id.btn_start)
        btnStart.setOnClickListener {
            // 检测微信是否安装
            if (wechatVersion.isBlank()) {
                showShort(ctx, "请安装微信")
                return@setOnClickListener
            } else {
                // 检测版本是否支持
                if (!Const.wechatIDMap.contains(wechatVersion)) {
                    showShort(ctx, "当前版本不支持")
                    return@setOnClickListener
                } else {
                    Const.currentWechatID = Const.wechatIDMap[wechatVersion]!!
                }
            }
            val accessibilityOK = checkAccessibility(ctx)
            if (!Environment.isExternalStorageManager()) {
                showShort(ctx, "请允许访问所有文件")
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent)
            } else if (!accessibilityOK) {
                // 未开启无障碍
                showShort(ctx, "请打开无障碍模式")
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    ctx.startActivity(intent)
                }
            } else {
                ToastUtils.showShort(applicationContext, "正在打开微信")
                AppUtils.launchApp(Const.wechatPackageName)
            }
        }
    }

    /**
     * 检测无障碍是否启动
     */
    private fun checkAccessibility(ctx: Context): Boolean {
        var accessibilityEnabled = 0
        val service = packageName + "/" + AutoService::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                ctx.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (_: Settings.SettingNotFoundException) {
        }
        val splitter = SimpleStringSplitter(':')
        if (1 == accessibilityEnabled) {
            val settingValue: String? = Settings.Secure.getString(
                ctx.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (null != settingValue) {
                splitter.setString(settingValue)
                while (splitter.hasNext()) {
                    val accessibilityService = splitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * short toast
     */
    private fun showShort(ctx: Context, msg: String) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }
}