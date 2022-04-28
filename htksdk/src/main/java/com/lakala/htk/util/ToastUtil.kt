package com.lakala.htk.util

import android.content.Context
import android.text.TextUtils
import es.dmoral.toasty.Toasty

class ToastUtil {
    companion object{
        fun toastSuccess(context: Context,msg : String){
            Toasty.success(context,msg).show()
        }

        fun toastSuccess(context: Context,id : Int){
            Toasty.success(context, context.resources.getString(id)).show()
        }

  
        fun toastWaring(context: Context,msg : String?){
            if (TextUtils.isEmpty(msg)) {
                return
            }
            Toasty.warning(context,msg!!).show()
        }

        fun toastWarning(context: Context,id : Int){
            Toasty.warning(context, context.resources.getString(id)).show()
        }

        fun toastError(context: Context,msg : String?){
            if (!TextUtils.isEmpty(msg)) {
                Toasty.error(context,msg!!).show()
            }
        }

        fun toastError(context: Context,id : Int){
            Toasty.error(context, context.resources.getString(id)).show()
        }
    }
}