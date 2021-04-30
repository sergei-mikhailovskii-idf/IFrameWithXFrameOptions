package com.mikhailovskii.iframewithxframeoptions

import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.mikhailovskii.iframewithxframeoptions.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import java.net.MalformedURLException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpUI()

        binding.btnSearch.setOnClickListener {
            val url = binding.etUrl.text.toString()
            binding.webview.loadUrl(url)
        }
    }

    private fun setUpUI() {
        WebView.setWebContentsDebuggingEnabled(true)

        val settings = binding.webview.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.builtInZoomControls = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        binding.webview.webChromeClient = WebChromeClient()
        binding.webview.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return try {
                    val cordovaResponse = super.shouldInterceptRequest(view, request)
                    if (cordovaResponse != null) {
                        return cordovaResponse
                    }
                    val url = request!!.url.toString()
                    val httpClient = OkHttpClient()
                    val okRequest: Request = Request.Builder()
                            .url(url)
                            .build()
                    val response: Response = httpClient.newCall(okRequest).execute()
                    val modifiedResponse: Response = response.newBuilder()
                            .removeHeader("x-frame-options")
                            .removeHeader("frame-options")
                            .build()
                    WebResourceResponse("text/html",
                            modifiedResponse.header("content-encoding", "utf-8"),
                            modifiedResponse.body?.byteStream()
                    )
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                    null
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

}