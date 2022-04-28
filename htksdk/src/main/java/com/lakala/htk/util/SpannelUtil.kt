package com.lakala.htk.util

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

class SpannelUtil {
    companion object{
        fun getSpannelStr(text:String,color: Int):SpannableString{
            var spannableString = SpannableString(text)
            val span = ForegroundColorSpan(color)
            spannableString.setSpan(span, 0, text.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            return spannableString
        }

        fun getSpannelStr(text:String,color: Int,start:Int,end : Int):SpannableString{
            var spannableString = SpannableString(text)
            val span = ForegroundColorSpan(color)
            spannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            return spannableString
        }
    }
}