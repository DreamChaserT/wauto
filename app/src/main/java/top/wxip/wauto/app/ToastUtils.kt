package top.wxip.wauto.app

import android.content.Context
import android.widget.Toast
import com.blankj.utilcode.util.ThreadUtils

class ToastUtils {
    companion object {
        fun showShort(ctx: Context, msg: String) {
            ThreadUtils.runOnUiThread {
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}