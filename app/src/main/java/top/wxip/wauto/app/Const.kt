package top.wxip.wauto.app

class Const {
    companion object {
        const val wechatPackageName = "com.tencent.mm"
        val wechatIDMap = mapOf("8.0.43" to WechatID().apply {
            msgItem = "com.tencent.mm:id/cj1"
            msgItemTitle = "com.tencent.mm:id/kbq"
            msgItemUnread = "com.tencent.mm:id/o_u"
            msgItemData = "com.tencent.mm:id/ht5"
            msgDetailItem = "com.tencent.mm:id/bn1"
            msgDetailSender = "com.tencent.mm:id/brc"
            msgDetailData = "com.tencent.mm:id/bkl"
        })
        var currentWechatID: WechatID = WechatID()
    }
}