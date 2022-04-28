package com.lakala.htk

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.google.gson.Gson
import com.hjq.permissions.OnPermission
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lakala.htk.databinding.ActivityHtkWebBinding
import com.lakala.htk.util.DimensionUtil
import com.lakala.htk.util.FileProvider7
import com.lakala.htk.util.SpannelUtil
import com.lakala.htk.util.ToastUtil
import com.tencent.smtt.export.external.interfaces.ConsoleMessage
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.sdk.ValueCallback
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import org.json.JSONObject
import wendu.dsbridge.CompletionHandler
import wendu.dsbridge.DWebView
import java.io.File
import java.util.*


open class HtkWebActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityHtkWebBinding

    private var isFullscreen: Boolean = false

    private var mUrl: String? = null
    private var mTitle: String? = null

    private var mFileUri: File? = null
    private var mFileVideoUri: File? = null
    private var mImageUri: Uri? = null
    private var mVideoUri: Uri? = null
    private val PHOTO_REQUEST = 100
    private val VIDEO_REQUEST = 101
    private val FILE_CHOOSER_RESULT_CODE = 10000
    private var mUploadMessage: ValueCallback<Uri>? = null
    private var mUploadMessageAboveL: ValueCallback<Array<Uri>>? = null


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        mBinding = ActivityHtkWebBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        isFullscreen = true

        initWebViewSettings()

        init()

        initToolBar(mTitle!!)

        if (mUrl!!.contains("?")) {
            val url = mUrl + "&" + getQueryParams()
            mBinding.webview.loadUrl(url)
        } else {
            val url = (mUrl + "?" + getQueryParams())
            mBinding.webview.loadUrl(url)
        }
    }


    private fun initToolBar(title : String){
        if (!mParams.isNeedToolBar) {
            if (Build.VERSION.SDK_INT >= 21) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
            return
        }
        mBinding.vs.inflate()
        val toolBar = mBinding.root.findViewById<Toolbar>(R.id.tool_bar)
        if (toolBar != null) {
            if (mParams.statusColor != 0) {
                mBinding.root.findViewById<View>(R.id.app_bar_layout).setBackgroundColor(ContextCompat.getColor(this, mParams.statusColor))
            }
            val rlBar = mBinding.root.findViewById<RelativeLayout>(R.id.rl_bar)
            rlBar.layoutParams.height += getStatusBarHeight()
            rlBar.setPadding(rlBar.paddingLeft,rlBar.paddingTop + getStatusBarHeight() ,rlBar.paddingRight,rlBar.paddingBottom)
            rlBar.requestLayout()
            setSupportActionBar(toolBar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            val tvTitle = mBinding.root.findViewById<TextView>(R.id.tv_title)
            toolBar!!.title = ""
            tvTitle!!.text = title
            if (mParams.backColor != 0) {
                tvTitle.setTextColor(ContextCompat.getColor(this, mParams.backColor))

                val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
                if(upArrow != null) {
                    upArrow.setColorFilter(ContextCompat.getColor(this, mParams.backColor), PorterDuff.Mode.SRC_ATOP)
                    if( supportActionBar != null) {
                        supportActionBar!!.setHomeAsUpIndicator(upArrow)
                    }
                }
            }

            toolBar.setNavigationOnClickListener {
                if (mBinding.webview.canGoBack()){
                    mBinding.webview.goBack()
                }else{
                    finish()
                }
            }
        }

    }

    fun getStatusBarHeight(): Int {
        if(sStatusBarH == 0) {
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            sStatusBarH = resources.getDimensionPixelSize(resourceId)
        }
        return  sStatusBarH
    }

    companion object {var sStatusBarH :Int = 0}

    private fun getQueryParams():String {
        return "temporaryToken=" + mParams.temporaryToken + "&appType=" + mParams.appType + "&channelId=" + mParams.channelId + "&statusBarHeight=${getStatusBarHeight() / Resources.getSystem().displayMetrics.density}" +"&navBarHeight=55"
    }

    lateinit var mParams:HtkParams
    private fun init() {
        mParams = intent.getParcelableExtra<HtkParams>("params")!!
        mTitle = mParams.title
        mUrl = mParams.url
        mFileUri = File(Environment.getExternalStorageDirectory(), "Documents/web_upload.png")
        if (!mFileUri!!.exists()){
            mFileUri!!.parentFile.mkdir()
        }

        mFileVideoUri = File(Environment.getExternalStorageDirectory(), "Documents/web_upload_video.mp4")
        if (!mFileVideoUri!!.exists()){
            mFileVideoUri!!.parentFile.mkdir()
        }
        mBinding.webview!!.webChromeClient = object : WebChromeClient() {

            override fun onGeolocationPermissionsShowPrompt(
                p0: String?,
                p1: GeolocationPermissionsCallback?
            ) {
                XXPermissions.with(this@HtkWebActivity)
                    .permission(
                        Permission.ACCESS_COARSE_LOCATION
                        ,Permission.ACCESS_FINE_LOCATION)
                    .request(object : OnPermission {

                        override fun hasPermission(granted: List<String>, isAll: Boolean) {
                            if (isAll) {
                                p1?.invoke(p0,true,true)
                            } else {
                                ToastUtil.toastWaring(this@HtkWebActivity,"获取权限成功，部分权限未正常授予")
                            }
                        }

                        override fun noPermission(denied: List<String>, quick: Boolean) {
                            p1?.invoke(p0,false,true)
                            if (quick) {
                                XXPermissions.gotoPermissionSettings(this@HtkWebActivity)
                            } else {
                                ToastUtil.toastWaring(this@HtkWebActivity,"获取权限失败")
                            }
                        }
                    })

            }

            override fun onConsoleMessage(p0: ConsoleMessage?): Boolean {
                Log.d("onConsoleMessage", p0?.message()!!)
                return true
            }

            override fun onReceivedTitle(p0: WebView?, p1: String?) {
                super.onReceivedTitle(p0, p1)
                if (TextUtils.isEmpty(p1) || p1!!.startsWith("http")){
                    return
                }
                val tvTitle = mBinding.root.findViewById<TextView>(R.id.tv_title)
                tvTitle?.text = p1
            }



            override fun onProgressChanged(p0: WebView?, newProgress: Int) {
                super.onProgressChanged(p0, newProgress)
                if (newProgress >= 100) {
                    mBinding.progressBar!!.visibility = View.GONE
                } else {
                    mBinding.progressBar!!.progress = newProgress
                }
            }

            //For Android  >= 4.1
            override fun openFileChooser(
                valueCallback: ValueCallback<Uri>,
                acceptType: String,
                capture: String
            ) {
                onOpenFileChooser(valueCallback,acceptType)
            }

            // For Android >= 5.0
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                showFileChooser(filePathCallback,fileChooserParams)
                return true
            }

        }

        mBinding.webview.webViewClient = object : WebViewClient() {


            override fun onReceivedSslError(p0: WebView?, p1: SslErrorHandler?, p2: SslError?) {
                p1?.proceed()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        DWebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        mBinding.webview.addJavascriptObject(JsApi(), "")

    }

    private fun showFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams
    ) {
        if (mUploadMessageAboveL != null) {
            mUploadMessageAboveL!!.onReceiveValue(null)
            mUploadMessageAboveL = null
        }
        mUploadMessageAboveL = filePathCallback

                XXPermissions.with(this)
                    .permission(
                        Permission.CAMERA,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE
                    ) //不指定权限则自动获取清单中的危险权限
                    .request(object : OnPermission {
                        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                        override fun hasPermission(
                            granted: List<String>,
                            isAll: Boolean
                        ) {
                            if (isDestroyed) {
                                return
                            }
                            if (isAll) {
                                var type: String =
                                    if (fileChooserParams?.acceptTypes != null
                                        && fileChooserParams.acceptTypes.isNotEmpty()
                                        && !TextUtils.isEmpty(fileChooserParams.acceptTypes[0])
                                    ) {
                                        fileChooserParams.acceptTypes[0]
                                    } else {
                                        "*/*"
                                    }
                                when (type) {
                                    "*/*" -> showSelector(type)
                                    "video/*" -> takeVideo(1)
                                    else -> {

                                        val dialog = MaterialDialog(this@HtkWebActivity)
                                            .title(null, "提示")
                                            .message(null, "请选择方式")
                                            .negativeButton(null,
                                                SpannelUtil.getSpannelStr(
                                                    "拍照",
                                                    resources.getColor(R.color.light_blue_600)
                                                ),
                                                object : DialogCallback {
                                                    override fun invoke(p1: MaterialDialog) {
                                                        takePhoto()
                                                    }

                                                })
                                            .positiveButton(null,
                                                SpannelUtil.getSpannelStr(
                                                    "图库",
                                                    resources.getColor(R.color.light_blue_600)
                                                ),
                                                object : DialogCallback {
                                                    override fun invoke(p1: MaterialDialog) {
                                                        openImageChooserActivity(type)
                                                    }
                                                }
                                            )
                                            .cornerRadius(DimensionUtil.dpToPx(2), null)
                                            .cancelable(false)
                                        dialog.show()
                                        dialog.setOnKeyListener { _, keyCode, keyEvent ->
                                            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                                                dialog.dismiss()
                                                if (mUploadMessage != null) {
                                                    mUploadMessage!!.onReceiveValue(null)
                                                    mUploadMessage = null
                                                }

                                                if (mUploadMessageAboveL != null) {
                                                    mUploadMessageAboveL!!.onReceiveValue(
                                                        null
                                                    )
                                                    mUploadMessageAboveL = null
                                                }
                                                true
                                            }
                                            false
                                        }
                                    }
                                }
                            }
                        }

                        override fun noPermission(
                            denied: List<String>,
                            quick: Boolean
                        ) {
                            if (mUploadMessageAboveL != null) {
                                mUploadMessageAboveL!!.onReceiveValue(null)
                                mUploadMessageAboveL = null
                            }
                            if (quick) {
                                ToastUtil.toastWaring(this@HtkWebActivity,"被永久拒绝授权，请手动授予权限")
                                //如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.gotoPermissionSettings(this@HtkWebActivity)
                            } else {
                                ToastUtil.toastWaring(this@HtkWebActivity,"获取权限失败,可能会影响您的使用")
                            }
                        }
                    })


    }


    private fun onOpenFileChooser(
        valueCallback: ValueCallback<Uri>,
        acceptType: String
    ) {
        if (mUploadMessage != null) {
            mUploadMessage!!.onReceiveValue(null)
        }
        mUploadMessage = valueCallback
                XXPermissions.with(this@HtkWebActivity)
                    .permission(
                        Permission.CAMERA,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE
                    ) //不指定权限则自动获取清单中的危险权限
                    .request(object : OnPermission {
                        override fun hasPermission(
                            granted: List<String>,
                            isAll: Boolean
                        ) {
                            if (isDestroyed) {
                                return
                            }
                            if (isAll) {
                                var type: String = if (!TextUtils.isEmpty(acceptType)) {
                                    acceptType
                                } else {
                                    "*/*"
                                }
                                when (type) {
                                    "*/*" -> showSelector(type)
                                    "video/*" -> takeVideo(1)
                                    else -> {
                                        val dialog = MaterialDialog(this@HtkWebActivity)
                                            .title(null, "提示")
                                            .message(null, "请选择方式")
                                            .negativeButton(null,
                                                SpannelUtil.getSpannelStr("拍照",resources.getColor(R.color.light_blue_600)),
                                                object : DialogCallback {
                                                    override fun invoke(p1: MaterialDialog) {
                                                        takePhoto()
                                                    }
                                                })
                                            .positiveButton(null,
                                                SpannelUtil.getSpannelStr("图库", resources.getColor(R.color.light_blue_600)),
                                                object : DialogCallback {
                                                    override fun invoke(p1: MaterialDialog) {
                                                        openImageChooserActivity(type)

                                                    }
                                                }
                                            )
                                            .cornerRadius(DimensionUtil.dpToPx(2), null)
                                            .cancelable(false)
                                        dialog.show()
                                        dialog.setOnKeyListener { _, keyCode, keyEvent ->
                                            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_DOWN) {
                                                dialog.dismiss()
                                                if (mUploadMessage != null) {
                                                    mUploadMessage!!.onReceiveValue(null)
                                                    mUploadMessage = null
                                                }

                                                if (mUploadMessageAboveL != null) {
                                                    mUploadMessageAboveL!!.onReceiveValue(
                                                        null
                                                    )
                                                    mUploadMessageAboveL = null
                                                }
                                                true
                                            }
                                            false
                                        }
                                    }
                                }

                            }
                        }

                        override fun noPermission(
                            denied: List<String>,
                            quick: Boolean
                        ) {
                            if (mUploadMessage != null) {
                                mUploadMessage!!.onReceiveValue(null)
                                mUploadMessage = null
                            }
                            if (quick) {
                                ToastUtil.toastWaring(this@HtkWebActivity,"被永久拒绝授权，请手动授予权限")
                                //如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.gotoPermissionSettings(this@HtkWebActivity)
                            } else {
                                ToastUtil.toastWaring(this@HtkWebActivity,"获取权限失败,可能会影响您的使用")

                            }
                        }
                    })
    }

    private fun openImageChooserActivity(type: String) {
        mVideoUri = FileProvider7.getUriForFile(this, mFileUri)
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = type
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    /**
     * 拍照
     */
    private fun takePhoto() {
        mImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(applicationContext, "$packageName.basefileProvider", mFileUri!!)
        } else {
            Uri.fromFile(mFileUri)
        }
//        mImageUri = FileProvider7.getUriForFile(activity, mFileUri!!)
        takePicture(mImageUri!!, PHOTO_REQUEST)
    }

    private fun takePicture(imageUri: Uri, requestCode: Int) {
        //调用系统相机
        val intentCamera = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        //        mImageUri= new File(Environment.getExternalStorageDirectory(), FileUtilsphoto.getPhotoFileName_new());
        intentCamera.action = MediaStore.ACTION_IMAGE_CAPTURE
        intentCamera.putExtra("android.intent.extras.CAMERA_FACING", 0) // 调用后置摄像头
        //将拍照结果保存至photo_file的Uri中，不保留在相册中
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        startActivityForResult(intentCamera, requestCode)
    }

    private fun takeVideo(camera: Int = 0) {
        mVideoUri = FileProvider7.getUriForFile(this, mFileVideoUri)
        //调用系统相机
        val intentCamera = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intentCamera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        }
        intentCamera.action = MediaStore.ACTION_VIDEO_CAPTURE
        intentCamera.addCategory(Intent.CATEGORY_DEFAULT)
        intentCamera.putExtra("android.intent.extras.CAMERA_FACING", camera); // 调用前置摄像头
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mVideoUri)
        startActivityForResult(intentCamera, VIDEO_REQUEST)
    }

    private fun showSelector(type: String) {
        val cusView = LayoutInflater.from(this).inflate(R.layout.dialog_photo_layout, null)
        val tvMessage = cusView.findViewById<TextView>(R.id.tv_message)
        val tvPhoto = cusView.findViewById<TextView>(R.id.tv_photo)
        val tvVideo = cusView.findViewById<TextView>(R.id.tv_video)
        var tvFile = cusView.findViewById<TextView>(R.id.tv_file)
        tvMessage.text = "请选择方式"

        val dialog = MaterialDialog(this)
            .customView(null, cusView!!)
            .cornerRadius(DimensionUtil.dpToPx(2), null)
            .cancelable(false)
        dialog.show()
        cusView.setPadding(0, 0, 0, 0)
        tvPhoto.setOnClickListener {
            takePhoto()
            dialog.dismiss()
        }

        tvVideo.setOnClickListener {
            takeVideo()
            dialog.dismiss()
        }
        tvFile.setOnClickListener {
            openImageChooserActivity(type)
            dialog.dismiss()
        }

    }



    private fun initWebViewSettings() {
        DWebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val webSetting = mBinding.webview.settings
        webSetting.setGeolocationEnabled(true)
        webSetting.allowContentAccess = true
        webSetting.javaScriptEnabled = true
        webSetting.defaultTextEncodingName = "UTF-8"
        webSetting.javaScriptCanOpenWindowsAutomatically = true
        webSetting.allowFileAccess = true
        webSetting.layoutAlgorithm = com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webSetting.builtInZoomControls = true
        webSetting.useWideViewPort = true
        webSetting.setSupportMultipleWindows(true)
        webSetting.domStorageEnabled = true
        webSetting.cacheMode = WebSettings.LOAD_NO_CACHE

        // settings 的设计
        webSetting.setAppCachePath(getDir("appcache", 0).path)
        webSetting.databasePath = getDir("databases", 0).path
        webSetting.setGeolocationDatabasePath(getDir("geolocation", 0).path)
        webSetting.loadWithOverviewMode = true
        webSetting.setSupportZoom(false)
    }




    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveLFile(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || mUploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        mUploadMessageAboveL!!.onReceiveValue(results)
        mUploadMessageAboveL = null
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode != PHOTO_REQUEST && requestCode != VIDEO_REQUEST) || mUploadMessageAboveL == null) {
            return
        }
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {

            if (data == null) {
                results = if (requestCode == PHOTO_REQUEST) {
                    arrayOf(mImageUri!!)
                } else {
                    arrayOf(mVideoUri!!)
                }
            } else {
                val dataString = data!!.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        mUploadMessageAboveL!!.onReceiveValue(results)
        mUploadMessageAboveL = null

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FILE_CHOOSER_RESULT_CODE -> {
                if (null == mUploadMessage && null == mUploadMessageAboveL) return
                if (mUploadMessageAboveL != null) {
                    onActivityResultAboveLFile(requestCode, resultCode, data)
                } else if (mUploadMessage != null) {
                    val result =
                        if (data == null || resultCode != Activity.RESULT_OK) null else data.data
                    mUploadMessage!!.onReceiveValue(result)
                    mUploadMessage = null
                }
            }

            VIDEO_REQUEST -> {
                if (null == mUploadMessage && null == mUploadMessageAboveL) return
                if (mUploadMessageAboveL != null) {
                    onActivityResultAboveL(requestCode, resultCode, data)
                } else if (mUploadMessage != null) {
                    val result =
                        if (data == null || resultCode != Activity.RESULT_OK) null else data.data
                    mUploadMessage!!.onReceiveValue(result)
                    mUploadMessage = null
                }
            }

            PHOTO_REQUEST -> {
                if (null == mUploadMessage && null == mUploadMessageAboveL) return
                if (mUploadMessageAboveL != null) {
                    onActivityResultAboveL(requestCode, resultCode, data)
                } else if (mUploadMessage != null) {
                    val result =
                        if (data == null || resultCode != Activity.RESULT_OK) null else data.data
                    mUploadMessage!!.onReceiveValue(result)
                    mUploadMessage = null
                }

            }
        }
    }


    inner class JsApi {
        @JavascriptInterface
        fun log(data: Any) {
            Log.d("Htk", data.toString())
        }

        @JavascriptInterface
        fun htkGoBack(data: Any){
            if (mBinding.webview.canGoBack()) {
                mBinding.webview.goBack()
            } else {
                finish()
            }
        }

        @JavascriptInterface
        fun htkClose(data: Any){
            finish()
        }

        @JavascriptInterface
        fun htkGoHuijiSign(data: Any){
            data as JSONObject
            val htkParams = HtkParams()
            htkParams.appType = mParams.appType
            htkParams.channelId = mParams.channelId
            htkParams.temporaryToken = mParams.temporaryToken
            htkParams.statusColor = mParams.statusColor
            htkParams.backColor = mParams.backColor
            htkParams.url = data.optString("url")
            htkParams.title = "汇积签约"
            htkParams.isNeedToolBar = true
            htkParams.ext = data.optJSONObject("data")


            val intent = Intent(this@HtkWebActivity,HtkWebActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("params",htkParams)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        @JavascriptInterface
        fun htkAgentInfo(data: Any, handler: CompletionHandler<Any>){
            handler.complete(Gson().toJson(mParams.ext))
        }

        @JavascriptInterface
        fun htkSdkInfo(data: Any, handler: CompletionHandler<Any>){
            val map = TreeMap<String,String>()
            map["osType"] = "Android"
            map["model"] = Build.MODEL
            map["sysVersion"] = Build.VERSION.RELEASE
            map["deviceType"] = Build.BRAND
            handler.complete(Gson().toJson(map))
        }



    }
}