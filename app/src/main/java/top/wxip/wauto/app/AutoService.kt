package top.wxip.wauto.app

import android.accessibilityservice.AccessibilityService
import android.text.SpannableString
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import kotlin.concurrent.thread

class AutoService : AccessibilityService() {

    private var eventTypes = setOf(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
    private var currentPackageName = ""

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (null == event) {
            return
        }
        if (eventTypes.contains(event.eventType)) {
            currentPackageName = event.packageName as String
        }
    }

    override fun onInterrupt() {
        LogUtils.e("无障碍模式中断!")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        thread {
            LogUtils.i("自动跳转微信")
            ToastUtils.showShort(applicationContext, "自动跳转微信")
            AppUtils.launchApp(Const.wechatPackageName)
            while (true) {
                Thread.sleep(2000)
                // 检测当前是否在微信页面
                if (currentPackageName == Const.wechatPackageName) {
                    // 当前在微信页面,执行逻辑
                    // 获取数据列表
                    val root = rootInActiveWindow
                    // 聊天记录列表
                    val msgItemRaw =
                        root.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgItem)
                    if (msgItemRaw.isEmpty()) {
                        ToastUtils.showShort(applicationContext, "请打开微信首页")
                        LogUtils.e("找不到聊天记录列表")
                        continue
                    }
                    for (i in 0 until msgItemRaw.size) {
                        val item = msgItemRaw[i]
                        val msgItemTitleRaw =
                            item.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgItemTitle)
                        val msgItemUnreadRaw =
                            item.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgItemUnread)
                        val msgItemDataRaw =
                            item.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgItemData)

                        var msgItemTitle = ""
                        var msgItemUnread = 0
                        var msgItemData = ""
                        if (msgItemTitleRaw.isNotEmpty()) {
                            msgItemTitle = (msgItemTitleRaw[0].text as SpannableString).toString()
                        }
                        if (msgItemUnreadRaw.isNotEmpty()) {
                            msgItemUnread = (msgItemUnreadRaw[0].text as String).toInt()
                        }
                        if (msgItemDataRaw.isNotEmpty()) {
                            msgItemData = (msgItemDataRaw[0].text as SpannableString).toString()
                        }
                        if (msgItemUnread > 0) {
                            // 当前列表存在未读消息,点开,获取详细数据
                            if (item.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                Thread.sleep(1000)
                                val msgDetailItemRaw =
                                    rootInActiveWindow.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgDetailItem)
                                val msgDetailItemLen = msgDetailItemRaw.size
                                for (i in 0 until msgItemUnread) {
                                    if (msgDetailItemLen - i - 1 > 0) {
                                        // 从后往前读
                                        val msgDetailItem =
                                            msgDetailItemRaw[msgDetailItemLen - i - 1]
                                        val msgDetailSenderRaw =
                                            msgDetailItem.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgDetailSender)
                                        val msgDetailDataRaw =
                                            msgDetailItem.findAccessibilityNodeInfosByViewId(Const.currentWechatID.msgDetailData)
                                        var msgDetailSender = ""
                                        var msgDetailData = ""
                                        if (msgDetailSenderRaw.isNotEmpty()) {
                                            msgDetailSender =
                                                (msgDetailSenderRaw[0].text as SpannableString).toString()
                                        }
                                        if (msgDetailDataRaw.isNotEmpty()) {
                                            msgDetailData =
                                                (msgDetailDataRaw[0].text as SpannableString).toString()
                                        }
                                        LogUtils.i("msgItemTitle:${msgItemTitle}\nmsgItemUnread:${msgItemUnread}\nmsgItemData:${msgItemData}\nmsgDetailSender:${msgDetailSender}\nmsgDetailData:${msgDetailData}")
                                    } else {
                                        break
                                    }
                                }
                                if (!performGlobalAction(GLOBAL_ACTION_BACK)) {
                                    LogUtils.e("回退失败")
                                }
                            } else {
                                LogUtils.e("无法打开消息列表")
                            }
                            break
                        }
                    }
                }
            }
        }
    }
}