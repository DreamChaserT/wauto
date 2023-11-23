package top.wxip.wauto.app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.CacheDiskStaticUtils
import com.blankj.utilcode.util.LogUtils
import java.lang.NumberFormatException
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
                    val root = rootInActiveWindow ?: continue
                    // 聊天记录列表
                    val msgItemList =
                        AccessibilityUtils.findByID(root, Const.currentWechatID.msgItem)
                    if (msgItemList.isEmpty()) {
                        ToastUtils.showShort(applicationContext, "请打开微信首页")
                        LogUtils.e("找不到聊天记录列表")
                        continue
                    }
                    for (msgItem in msgItemList) {
                        var msgItemUnread = 0
                        try {
                            msgItemUnread = AccessibilityUtils.getTextByID(
                                msgItem,
                                Const.currentWechatID.msgItemUnread
                            ).toInt()
                        } catch (_: NumberFormatException) {
                        }
                        if (msgItemUnread > 0) {
                            // 当前列表存在未读消息,点开,获取详细数据
                            if (msgItem.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                                Thread.sleep(500)
                                var tries = 0;
                                var firstLst: List<WechatMsg>
                                var secondLst = listOf<WechatMsg>()
                                // 等待别人消息发送完
                                while (tries < 10) {
                                    firstLst = readMsgDetailList(rootInActiveWindow)
                                    rootInActiveWindow.refresh()
                                    Thread.sleep(500)
                                    secondLst = readMsgDetailList(rootInActiveWindow)
                                    if (firstLst.size == secondLst.size) {
                                        break
                                    }
                                    tries++
                                }
                                // 去除已经收到的消息
                                val targetLst = filterPreviousMsg(secondLst)
                                for (wechatMsg in targetLst) {
                                    LogUtils.i(wechatMsg.toString())
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

    /**
     * 读取消息列表
     */
    private fun readMsgDetailList(root: AccessibilityNodeInfo): List<WechatMsg> {
        val result = ArrayList<WechatMsg>()
        var msgDetailTitle =
            AccessibilityUtils.getTextByID(root, Const.currentWechatID.msgDetailTitle)
        // 去除多余字符
        val msgDetailTitleLen = msgDetailTitle.lastIndexOf("(")
        if (msgDetailTitleLen > 0) {
            msgDetailTitle = msgDetailTitle.substring(0, msgDetailTitleLen)
        }
        // 读取列表
        val msgDetailList = AccessibilityUtils.findByID(root, Const.currentWechatID.msgDetailItem)
        for (msgDetail in msgDetailList) {
            val msgDetailSender =
                AccessibilityUtils.getTextByID(msgDetail, Const.currentWechatID.msgDetailSender)
            // text
            val textData =
                AccessibilityUtils.getTextByID(msgDetail, Const.currentWechatID.msgDetailTextData)
            if (textData.isNotBlank()) {
                result.add(WechatMsg(msgDetailTitle, msgDetailSender, "text", textData))
                continue
            }
            // geo
            val geoData =
                AccessibilityUtils.getTextByID(msgDetail, Const.currentWechatID.msgDetailGeoData)
            val geoDetail = AccessibilityUtils.getTextByID(
                msgDetail,
                Const.currentWechatID.msgDetailGeoDetailData
            )
            if (geoData.isNotBlank() || geoDetail.isNotBlank()) {
                result.add(WechatMsg(msgDetailTitle, msgDetailSender, "geo", "$geoData $geoDetail"))
                continue
            }
        }
        return result
    }

    /**
     * 过滤已经读过的消息
     */
    private fun filterPreviousMsg(lst: List<WechatMsg>): List<WechatMsg> {
        if (lst.isNotEmpty()) {
            val cacheKey = lst[0].msgDetailTitle
            val result = ArrayList<WechatMsg>()
            val lastMsg = CacheDiskStaticUtils.getString(cacheKey)
            var firstIdx = 0
            // 寻找上一条消息
            if (!lastMsg.isNullOrEmpty()) {
                for (i in lst.indices) {
                    if (lst[i].msgDetailData == lastMsg) {
                        firstIdx = i + 1
                        break
                    }
                }
            }
            // 复制新消息到result
            for (i in firstIdx until lst.size) {
                result.add(lst[i])
            }
            // 将最后一条消息写入缓存
            if (result.isNotEmpty()) {
                CacheDiskStaticUtils.put(cacheKey, result[result.size - 1].msgDetailData)
            }
            return result
        }
        return lst
    }

}