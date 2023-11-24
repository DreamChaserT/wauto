package top.wxip.wauto.app

class Const {
    companion object {
        const val wechatPackageName = "com.tencent.mm"
        val wechatIDMap = mapOf("8.0.43" to WechatID().apply {
            msgItem = "com.tencent.mm:id/cj1"
            msgItemUnread = "com.tencent.mm:id/o_u"

            msgDetailItem = "com.tencent.mm:id/bn1"
            msgDetailTitle = "com.tencent.mm:id/obn"
            msgDetailSender = "com.tencent.mm:id/brc"
            msgDetailTextData = "com.tencent.mm:id/bkl"

            msgDetailGeoData = "com.tencent.mm:id/bp8"
            msgDetailGeoDetailData = "com.tencent.mm:id/bp6"

            msgDetailPicItem = "com.tencent.mm:id/bkg"
            msgDetailPicDownload = "com.tencent.mm:id/d2y"
        })
        var currentWechatID: WechatID = WechatID()
    }
}