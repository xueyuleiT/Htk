package com.lakala.htk.util

import android.content.Context
import android.content.res.Resources


class DimensionUtil {
    companion object {

       open fun dpToPx(dp: Int): Float {
            return (dp * Resources.getSystem().displayMetrics.density)
        }

        fun dpToPx(context: Context, dpValue: Float): Float {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale)
        }
    }

}