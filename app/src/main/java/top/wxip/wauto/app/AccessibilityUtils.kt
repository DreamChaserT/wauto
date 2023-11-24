package top.wxip.wauto.app

import android.text.SpannableString
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityUtils {
    companion object {
        fun findOneByID(root: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
            val lst = findByID(root, id)
            if (lst.isNotEmpty()) {
                return lst[0]
            }
            return null
        }

        fun findByID(root: AccessibilityNodeInfo, id: String): List<AccessibilityNodeInfo> {
            return root.findAccessibilityNodeInfosByViewId(id)
        }

        private fun getText(node: AccessibilityNodeInfo?): String {
            if (null != node) {
                val text = node.text
                if (text is SpannableString) {
                    return text.toString()
                } else if (text is String) {
                    return text
                }
            }
            return ""
        }

        fun getTextByID(root: AccessibilityNodeInfo, id: String): String {
            return getText(findOneByID(root, id))
        }
    }
}