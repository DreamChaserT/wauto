package top.wxip.wauto.app

data class WechatMsg(
    val msgDetailTitle: String,
    val msgDetailSender: String,
    val msgDetailType: String,
    val msgDetailData: String,
)