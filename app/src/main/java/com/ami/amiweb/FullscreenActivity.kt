package com.ami.amiweb

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.net.NetworkInterface
import java.net.Socket
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    private var mediaplayer: MediaPlayer = MediaPlayer()

//    private var base_uri = "http://192.168.0.129:4200/"

//    private var base_uri = "http://hexiangu.food-life.co.jp/"

    private var base_uri = "http://app.new-food-life.shop/"

    private var charset = "GBK"

    private lateinit var bluetoothDevice: BluetoothDevice;
    private var hasBluetoothDevice = false;

    private var hasSocket = false;
    private lateinit var socket: BluetoothSocket;
    private lateinit var mmOutputStream: OutputStream;
    private lateinit var mmInputStream: InputStream;
    override fun onPostResume() {
        super.onPostResume()

        actionBar?.hide()
    }




    @SuppressLint("JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true



        val web_view: WebView = findViewById(R.id.main_web)


        web_view.loadUrl(base_uri)

        Log.v("test","hello test")
        mediaplayer.setDataSource(base_uri + "/assets/audio/kitchen.mp3");
        mediaplayer.prepare()
        web_view.addJavascriptInterface(this, "amiJs")

        // Get the web view settings instance
        val settings = web_view.settings

        // Enable java script in web view
        settings.javaScriptEnabled = true

        // Enable and setup web view cache
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setAppCachePath(cacheDir.path)


//        // Enable zooming in web view
//        settings.setSupportZoom(true)
//        settings.builtInZoomControls = true
//        settings.displayZoomControls = true


        // Zoom web view text
       // settings.textZoom = 125


        // Enable disable images in web view
        settings.blockNetworkImage = false
        // Whether the WebView should load image resources
        settings.loadsImagesAutomatically = true


//        // More web view settings
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            settings.safeBrowsingEnabled = true  // api 26
//        }
        //settings.pluginState = WebSettings.PluginState.ON
        settings.useWideViewPort = true
        //settings.loadWithOverviewMode = true
        settings.javaScriptCanOpenWindowsAutomatically = true
       // settings.mediaPlaybackRequiresUserGesture = false


        // More optional settings, you can enable it by yourself
        settings.domStorageEnabled = true
        settings.setSupportMultipleWindows(true)
        settings.loadWithOverviewMode = true
        settings.allowContentAccess = true
        settings.setGeolocationEnabled(true)
        //settings.allowUniversalAccessFromFileURLs = true
        settings.allowFileAccess = true

        // WebView settings
        web_view.fitsSystemWindows = true
        web_view.clearCache(true);


    }

    @JavascriptInterface
    fun start_kitchen_mp3(menu_no_str: String) {
        Log.v("test", menu_no_str)
        Log.v("test","hello start_kitchen_mp3")
        mediaplayer.seekTo(0)
        mediaplayer.start()
//        var menu_no_list = menu_no_str.trim('[').trim(']').split(',')
//        for(menu_no in menu_no_list) {
//            Log.v("test", menu_no)
//            mediaplayer.reset()
//            mediaplayer.setDataSource(base_uri + "/assets/audio/menu/" + menu_no + ".mp3");
//            mediaplayer.prepare()
////            mediaplayer.seekTo(0)
//            mediaplayer.start()
//        }
    }

    @JavascriptInterface
    fun getMac(): String{
        val manager = getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        Log.v("test", info.macAddress.toUpperCase())
//        return info.macAddress.toUpperCase()


        try {
            val all: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (nif in all) {
                if (!(nif.getName() as java.lang.String).equalsIgnoreCase("wlan0")) continue
                val macBytes: ByteArray = nif.getHardwareAddress() ?: return ""
                val res1 = StringBuilder()
                for (b in macBytes) {
                    res1.append(Integer.toHexString(b.toInt()) + ":")
                }
                if (res1.length > 0) {
                    res1.deleteCharAt(res1.length - 1)
                }
                return res1.toString().replace("ffffff", "").toUpperCase()
            }
        } catch (ex: java.lang.Exception) {
            //handle exception
        }
        return "02:00:00:00:00:00"
    }

    @JavascriptInterface
    fun open_cash_box() {
        var device = getBluetoothDevice()
        if (device != null) {
            printMsg(device, "", 3)
        } else {
            Log.v("test", "device not found")
        }
    }

    @JavascriptInterface
    fun accounting_day_print(data: String){

        var device = getBluetoothDevice()
        Log.v("test", data)
        Log.v("test", "---------------")
        if (device != null) {
            printMsg(device, data, 4)
        } else {
            Log.v("test", "device not found")
        }
    }

    @JavascriptInterface
    fun counter_print(data: String){

        var device = getBluetoothDevice()
        Log.v("test", data)
        Log.v("test", "---------------")
        if (device != null) {
            printMsg(device, data, 1)
        } else {
            Log.v("test", "device not found")
        }
    }

    @JavascriptInterface
    fun print_takeout(data: String){

        var device = getBluetoothDevice()
        Log.v("test----228", data)
        if (device != null) {
            printMsg(device, data, 10)
        } else {
            Log.v("test", "device not found")
        }
    }

    @JavascriptInterface
    fun print_kitchen_task(data: String){

        var device = getBluetoothDevice()
        Log.v("test----240", data)
        if (device != null) {
            printMsg(device, data, 11)
        } else {
            Log.v("test", "device not found")
        }
    }


    @JavascriptInterface
    fun print_stream(data: String){

        var device = getBluetoothDevice()
        Log.v("test----", data)
        if (device != null) {
            printMsg(device, data, 99)
        } else {
            Log.v("test", "device not found")
        }
    }

    @JavascriptInterface
    fun detail_print(data: String){

        var device = getBluetoothDevice()
        Log.v("test", data)
        Log.v("test", "---------------")
        if (device != null) {
            printMsg(device, data, 2)
        } else {
            Log.v("test", "device not found")
        }
    }

    fun getBluetoothDevice(): BluetoothDevice? {
        if ( this.hasBluetoothDevice ) {
            return this.bluetoothDevice
        }
        try{
            var mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            val paireddevice = mBluetoothAdapter.bondedDevices
            Log.v("def", "sdfasdfasdf")
            for (device: BluetoothDevice in paireddevice) {
                Log.v("test", device.getName())
                Log.v("uuids", device.getUuids().toString())
                Log.v("test", device.getAddress())
                this.bluetoothDevice = device
                this.hasBluetoothDevice = true
                return this.bluetoothDevice
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    fun printMsg(device: BluetoothDevice, data: String, print_type: Int) {
        try {
            if ( !this.hasSocket ) {
                Log.v("test", "start printMSg")
                var uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    device.setPin("1234".toByteArray())
                }
                this.socket = device.createRfcommSocketToServiceRecord(uuid);

                this.socket.connect();
                this.hasSocket = true;
                Log.v("test", "connection ok")

                this.mmOutputStream = this.socket.getOutputStream();

                Log.v("test", "getOutputStream ok")
                this.mmInputStream = this.socket.getInputStream();
                Log.v("test", "getInputStream ok")
            }



            if ( data!="" ) {
                var order = JSONObject(data);
                var shopInfo = order.getJSONObject("shopInfo")
                var charset = shopInfo.getString("charset")

                if (print_type == 1) {
                    if (charset == "GBK") {
                        printCounter(this.mmOutputStream, data)
                    } else {
                        printCounterFoodLife(this.mmOutputStream, data)
                    }
                }
                if (print_type == 2) {
                    if (charset == "GBK") {
                        printDetail(this.mmOutputStream, data)
                    } else {
                        printDetailFoodLife(this.mmOutputStream, data)
                    }

                }

                if (print_type == 4) {
                    printAccountingDay(this.mmOutputStream, data)
                }

                // print kitchen
                if (print_type == 10) {
                    printTakeout(this.mmOutputStream, data)
                }


                // print kitchen
                if (print_type == 11) {
                    printKitchenTask(this.mmOutputStream, data)
                }
                if( print_type == 99 ) {
                    printStream(this.mmOutputStream, data)
                }

            }
            if( print_type==3 ) {
                openCashBox(this.mmOutputStream)
//                printLogo(mmOutputStream)
            }

//            mmOutputStream.close()
//            mmInputStream.close()
//            this.socket.close()


        } catch (e: Exception) {
            Log.v("exceptin", e.toString())
        }
    }




    fun printKitchenTask(mmOutputStream: OutputStream, printData: String) {
        Log.v("test", printData)
        var printData = JSONObject(printData);
        var tasks = printData.getJSONArray("tasks")

        var shopInfo = printData.getJSONObject("shopInfo")
        for(m in 0 until tasks.length()) {
            var data = tasks.getJSONObject(m)
            var charset = shopInfo.getString("charset")

            if (charset != "" && charset != null) {
                this.charset = charset
            }

            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            // 強調印字の指定
            mmOutputStream.write(byteArrayOf(0x1B, 0x45))
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
            writeMsg(mmOutputStream, ("NO." + data.getString("seat_no" ) + "\n"))
            // 強調印字の解除
            mmOutputStream.write(byteArrayOf(0x1B, 0x46))
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 0))
            writeMsg(mmOutputStream, (data.getString("menu_name") + "  x " + data.getString("count") + "個\n" ))

            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))

            // 打印并走纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

            // 切纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x64, 3))
        }
    }



    fun printTakeout(mmOutputStream: OutputStream, printData: String) {
        Log.v("test", printData)
        var printData = JSONObject(printData);
        var orders = printData.getJSONArray("orders")

        var shopInfo = printData.getJSONObject("shopInfo")
        for(m in 0 until orders.length()) {
            var data = orders.getJSONObject(m)
            Log.v("test", orders.getString(m))
            var ship_addr = data.getString("ship_addr")
            var ship_name = data.getString("ship_name")
            var ship_tel = data.getString("ship_tel")
            var ship_time = data.getString("ship_time")
            var charset = shopInfo.getString("charset")

            var detail = data.getJSONArray("detail")
            if (charset != "" && charset != null) {
                this.charset = charset
            }

            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))

            // 有地址配送
            if(ship_addr!="") {
                writeMsg(mmOutputStream, "届け先住所\n")
                // 強調印字の指定
                mmOutputStream.write(byteArrayOf(0x1B, 0x45))
                // 加粗
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 0))
                writeMsg(mmOutputStream, ship_addr + "\n")
                // 強調印字の解除
                mmOutputStream.write(byteArrayOf(0x1B, 0x46))
                // 加粗
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
                writeMsg(mmOutputStream, "配達時間\n")
                // 強調印字の指定
                mmOutputStream.write(byteArrayOf(0x1B, 0x45))
                // 加粗
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
                writeMsg(mmOutputStream, ship_time + "\n")
                // 強調印字の解除
                mmOutputStream.write(byteArrayOf(0x1B, 0x46))
            } else {
                // 没地址的是自取
                // 強調印字の指定
                mmOutputStream.write(byteArrayOf(0x1B, 0x45))
                writeMsg(mmOutputStream, "自取時間\n")
                // 加粗
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
                writeMsg(mmOutputStream, ship_time + "\n")
                // 強調印字の解除
                mmOutputStream.write(byteArrayOf(0x1B, 0x46))
            }
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
            writeMsg(mmOutputStream, "連絡先：" + ship_tel + "\n")
            writeMsg(mmOutputStream, "受取人：" + ship_name + "\n")

            //left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
            writeMsg(mmOutputStream, "------------------------------\n")

            //left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
            writeMsg(mmOutputStream, "注 文 明 細\n")
            // 打印并走纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

            for (i in 0 until detail.length()) {
                // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
                var detail = detail.getJSONObject(i);

                // left对齐
                mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
                writeMsg(mmOutputStream, (detail.getString("menu_name") + "\n" ))
                mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
                writeMsg(mmOutputStream, ("￥"+ format_price(detail.getString("price")) + "    x " + detail.getString("count") + "個\n"))
            }

            //left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
            writeMsg(mmOutputStream, "------------------------------\n")

            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, "合計")
            // 強調印字の指定
            mmOutputStream.write(byteArrayOf(0x1B, 0x45))

            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
            writeMsg(mmOutputStream, ("  ￥"+ format_price(data.getString("amounts_actually")) + "\n"))
            // 強調印字の指定
            mmOutputStream.write(byteArrayOf(0x1B, 0x46))

            // 打印并走纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

            // ページモード解除
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x31))

            // 縮小印刷機能コマンド
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x63, 0, 0))

            //left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))

            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
            Log.v("test", shopInfo.getString("name").length.toString() );
            // 強調印字の指定
            mmOutputStream.write(byteArrayOf(0x1B, 0x45))

            writeMsg(mmOutputStream, shopInfo.getString("name") + "\n")
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
            // 強調印字の解除
            mmOutputStream.write(byteArrayOf(0x1B, 0x46))
            //left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, "営業時間: " + shopInfo.getString("time1") + "\n")
            writeMsg(mmOutputStream, "           " + shopInfo.getString("time2") + "\n")

            writeMsg(mmOutputStream, shopInfo.getString("post") + "\n" + shopInfo.getString("addr1") + "\n" + shopInfo.getString("addr2") + "\n")
            writeMsg(mmOutputStream, "TEL: " + shopInfo.getString("tel") + "\n")

            // 打印并走纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))

            // 切纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x64, 3))
        }
    }


    fun printAccountingDay(mmOutputStream: OutputStream, data: String) {
        Log.v("test", data)
        var data = JSONObject(data);
        var shopInfo = data.getJSONObject("shopInfo")
        var charset = shopInfo.getString("charset")

        var accounting = data.getJSONArray("accounting")
        if ( charset!="" && charset!=null ) {
            this.charset = charset
        }

        // ページモード解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x31))

        // 縮小印刷機能コマンド
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x63, 0, 0))

        //居中对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))

        var name_length = shopInfo.getString("name").length
        if ( name_length>4 ) {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        } else {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
        }
        Log.v("test", shopInfo.getString("name").length.toString() );
        // 加粗
//        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 2, 2))
        // 強調印字の指定
        mmOutputStream.write(byteArrayOf(0x1B, 0x45))

        writeMsg(mmOutputStream, shopInfo.getString("name") + "\n")
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        // 強調印字の解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x46))
        writeMsg(mmOutputStream,  data.getString("time") + "\n")

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
        writeMsg(mmOutputStream, "------------------------------\n")

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
        writeMsg(mmOutputStream, "明　細\n")


        for(i in 0 until accounting.length()) {
            // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
            var detail = accounting.getJSONObject(i);

            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, (detail.getString("display_name") + "\n") )

            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
            writeMsg(mmOutputStream, "￥"+ format_price(detail.getString("amounts_actually"))  + "\n")
        }

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
        writeMsg(mmOutputStream, "------------------------------\n")

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

         // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
        writeMsg(mmOutputStream, "合計\n")

        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(data.getString("amounts_actually")) + "\n"))

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))

        // 切纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 3))
    }


    fun printLogo(mmOutputStream: OutputStream) {
        mmOutputStream.write(byteArrayOf(0x1B, 0x4C, 0x01, 0x03))
    }

    fun openCashBox(mmOutputStream: OutputStream) {
        //钱箱
        mmOutputStream.write(byteArrayOf(0x07))
//        Thread.sleep(1000)
    }

    fun printStream(mmOutputStream: OutputStream, str: String) {
        var result = JSONObject(str)
        this.charset = result.getString("charset")
        var data = result.getJSONArray("data")
        // ページモード解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x31))
        // 縮小印刷機能コマンド
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x63, 0, 0))
        for( key in 0 until data.length() ) {
            var detail = data.getJSONArray(key);
            Log.v("test", detail.get(0).toString() );
            var str = detail.getString(0) + "\n"
            if ( str.length<1 ) {
                continue
            }

            var x45 = (detail.getInt(2)==1)
            if ( x45 ) {
                // 強調印字の指定
                mmOutputStream.write(byteArrayOf(0x1B, 0x45))
            }
            //对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, detail.getInt(3).toByte()))

            // 加粗
            var x69 = detail.getInt(1)
            if (x69==1) {
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
            } else if(x69==2) {
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 1))
            } else if(x69==3) {
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
            } else if(x69==4) {
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 2))
            } else {
                mmOutputStream.write(byteArrayOf(0x1B, 0x69, 2, 2))
            }

            writeMsg(mmOutputStream, str)

            if ( x45 ) {
                // 強調印字の解除
                mmOutputStream.write(byteArrayOf(0x1B, 0x46))
            }
        }

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))

        // 切纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 3))
    }
    fun printDetailFoodLife(mmOutputStream: OutputStream, data: String) {
        Log.v("test", data)
        var order = JSONObject(data);
        var details = order.getJSONObject("details")
        var taxDetails = order.getJSONObject("taxDetails")
        var money = order.getJSONObject("money")
        var shopInfo = order.getJSONObject("shopInfo")
        var charset = shopInfo.getString("charset")
        if ( charset!="" && charset!=null ) {
            this.charset = charset
        }

        if( order.has("hasStream")) {
            Log.v("-------", order.getString("hasStream"))
            if (order.getString("hasStream").toInt() > 0) {
                var printData = order.getJSONArray("printData")
                var jsonData =  JSONObject()
                jsonData.put("data", printData)
                jsonData.put("charset", this.charset)
                printStream(mmOutputStream, jsonData.toString())
                return
            }
        }

        // ページモード解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x31))
//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x32, 1))

        // 縮小印刷機能コマンド
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x63, 0, 0))

        //居中对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))

        var name_length = shopInfo.getString("name").length
        if ( name_length>7 ) {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        } else if(name_length>4 ) {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
        } else {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 2, 2))
        }
        Log.v("test", shopInfo.getString("name").length.toString() );
        // 加粗
//        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 2, 2))
        // 強調印字の指定
        mmOutputStream.write(byteArrayOf(0x1B, 0x45))

        writeMsg(mmOutputStream, shopInfo.getString("name") + "\n")
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        // 強調印字の解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x46))
        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
        writeMsg(mmOutputStream, "営業時間: " + shopInfo.getString("time1") + "\n")
        writeMsg(mmOutputStream, "           " + shopInfo.getString("time2") + "\n")


        writeMsg(mmOutputStream, shopInfo.getString("post") + "\n" + shopInfo.getString("addr1") + "\n" + shopInfo.getString("addr2") + "\n")
        writeMsg(mmOutputStream, "TEL:  " + shopInfo.getString("tel") + "\n")
        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
        writeMsg(mmOutputStream, "------------------------------\n")
        writeMsg(mmOutputStream, "領 収 書\n")


        var sIterator = details.keys();
        while(sIterator.hasNext()) {
            // 获得key
            var key = sIterator.next();
            // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
            var detail = details.getJSONObject(key);

            if (detail.getString("price").toInt()>0 ) {

                // left对齐
                mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
                writeMsg(mmOutputStream, (detail.getString("menu_no") + " " + detail.getString("menu_name") + "\n"))

                // right对齐
                mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
//                if (detail.getString("ori_price")!="null" && (detail.getString("ori_price").toInt() > detail.getString("price").toInt())) {
//
////                    mmOutputStream.write(byteArrayOf(0x1B, 0x2D, 1))
////                    writeMsg(mmOutputStream, (detail.getString("count") + "コ  X " + detail.getString("ori_price") + " ￥"
////                            + format_price((detail.getString("ori_price").toInt() * detail.getString("count").toInt()).toString()) + "\n"))
////
////                    mmOutputStream.write(byteArrayOf(0x1B, 0x2D, 0))
//                }
                writeMsg(mmOutputStream, (detail.getString("count") + "コ  X " + detail.getString("price") + " ￥" + format_price(detail.getString("total")) + "\n"))
            }
        }


        // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
        writeMsg(mmOutputStream, "小計(税抜)\n")
        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("total")) + "\n"))


        // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
        writeMsg(mmOutputStream, ("消費税等("+ order.getString("fax") + "%)\n"))
        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("tax_value")) + "\n"))


        if ( taxDetails.length()>0 ) {

            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, "-- 税込メニュー\n")
            sIterator = taxDetails.keys();
            while(sIterator.hasNext()) {
                // 获得key
                var key = sIterator.next();
                // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
                var detail = taxDetails.getJSONObject(key);

                if (detail.getString("price").toInt()>0 ) {

                    // left对齐
                    mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
                    writeMsg(mmOutputStream, (detail.getString("menu_no") + " " + detail.getString("menu_name") + "\n"))

                    // right对齐
                    mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
                    writeMsg(mmOutputStream, (detail.getString("count") + "コ  X " + detail.getString("price") + " ￥" + format_price(detail.getString("total")) + "\n"))
                }
            }


            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, "小計(税込)\n")
            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
            writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("price_tax_in")) + "\n"))
        }


        if(money.getString("cut").toInt()>0) {
            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, ("割引(" + money.getString("cut") + "%)\n"))

            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
            writeMsg(mmOutputStream, ("-￥" + format_price(money.getString("cut_value")) + "\n"))
        }

        if(money.getString("reduce").toInt()>0) {
            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
            writeMsg(mmOutputStream, ("減額\n"))

            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
            writeMsg(mmOutputStream, ("-￥" + format_price(money.getString("reduce")) + "\n"))
        }

        // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
        writeMsg(mmOutputStream, "合計\n")

        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("amounts_actually")) + "\n"))

        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 1))
        writeMsg(mmOutputStream, "------------------------------\n")

        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))

        // 縮小印刷機能コマンド
//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x63, 1, 1))
        writeMsg(mmOutputStream, "毎度ありがとうございます。\n")

        //日文
        writeMsg(mmOutputStream, "ご来店をお待ちしております！\n")

        // 打印并走纸
//        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        val currentTime = System.currentTimeMillis()
        val timeNow = SimpleDateFormat("yyyy年MM月dd日 HH時mm分").format(currentTime)
        writeMsg(mmOutputStream, (timeNow + "\n"))

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))

        // 切纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 3))
    }


    fun printCounterFoodLife(mmOutputStream: OutputStream, data: String) {

        // JIS 漢字モード解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x71))
        // ページモード
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x30))

        // ページモード 文字の印字方向
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x32, 3))

//        横方向始点＝[(xL＋xH×256)×1/8] mm
//        ・ 縦方向始点＝[(yL＋yH×256)×1/8] mm
//        ・ 横方向長さ＝[(dxL＋dxH×256)×1/8] mm
//        ・ 縦方向長さ＝[(dyL＋dyH×256)×1/8] mm
//        ・ スタンダードモード選択時、このコマンドを入力
        // ページモード 印字領域の設定
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x33, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x01, 0x7F, 0x04))
        // QR code
//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x79, 0x53, 0x30, 2))
//        // QR code 誤り訂正率
//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x79, 0x53, 0x31, 2))
//        // QR code セルサイズの設定
//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x79, 0x53, 0x32, 3))
//        // QR code データの設定

//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x79, 0x44, 0x32, 9, 2, 0, 0 ))
        //, 0x77, 0x77, 0x77, 0x2E, 0x6F, 0x63, 0x6F, 0x6D, 0x69, 0x6E, 0x63, 0x2E, 0x63, 0x6F, 0x6D, 0x0A, 0x0A, 0x0A
//        mmOutputStream.write("http://www.google.com".toByteArray())

        // QR code
//        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x79, 0x50))


        var order = JSONObject(data);
        var shopInfo = order.getJSONObject("shopInfo");
        var charset = shopInfo.getString("charset")
        if ( charset!="" && charset!=null ) {
            this.charset = charset
        }

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))
        // 加粗
//        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 2))
        // 強調印字の指定
        mmOutputStream.write(byteArrayOf(0x1B, 0x45))

        var name_length = shopInfo.getString("name").length
        if ( name_length>4 ) {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        } else {
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 2))
        }
        writeMsg(mmOutputStream, shopInfo.getString("name"))

        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        // 打印并走纸
//        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))

        // 絶対位置移動
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x41, 0x7F, 0x01))
        writeMsg(mmOutputStream, "領 収 書")


        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        // 強調印字の解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x46))
        // 絶対位置移動
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x41, 0x3A, 0x03))
        val currentTime = System.currentTimeMillis()
        val timeNow = SimpleDateFormat("yyyy年MM月dd日 HH時mm分\n").format(currentTime)
        writeMsg(mmOutputStream, (timeNow))

        // 絶対位置移動
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x41, 0x3A, 0x03))
        writeMsg(mmOutputStream, ("No." + order.getString("no") + "\n"))


        // 打印并走纸
//        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 1))
        // 強調印字の指定
//        mmOutputStream.write(byteArrayOf(0x1B, 0x45))

        writeMsg(mmOutputStream, "　         　　　　　　　　　様\n")


        //アンダーラインの指定
//        mmOutputStream.write(byteArrayOf(0x1B, 0x5F, 1))

        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))
        // 強調印字の指定
        mmOutputStream.write(byteArrayOf(0x1B, 0x45))
//
        writeMsg(mmOutputStream, "          --------------------------------------------------------\n")
        // 強調印字の解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x46))
        //アンダーラインの指定
//        mmOutputStream.write(byteArrayOf(0x1B, 0x5F, 0))

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 1, 1))
        writeMsg(mmOutputStream, ("         　　　　  ￥" + format_price(order.getString("amounts_actually")) + "―\n"))

//        //アンダーラインの解除
//        mmOutputStream.write(byteArrayOf(0x1B, 0x2D, 0))
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))

        //アンダーラインの指定
//        mmOutputStream.write(byteArrayOf(0x1B, 0x5F, 1))
        // 強調印字の指定
        mmOutputStream.write(byteArrayOf(0x1B, 0x45))
        writeMsg(mmOutputStream, "          --------------------------------------------------------\n")

        //アンダーラインの指定
//        mmOutputStream.write(byteArrayOf(0x1B, 0x5F, 0))
        // 強調印字の解除
        mmOutputStream.write(byteArrayOf(0x1B, 0x46))

        writeMsg(mmOutputStream, ("(内、消費税等 ￥"+ format_price(order.getString("amounts_actually_tax")) +"含みます) \n但し飲食代として、上記正に領収いたました。\n"))

        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x69, 0, 0))

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x61, 0))
//        writeMsg(mmOutputStream, "営業時間: 11:30am~15:00pm   17:30pm~23:00pm\n")
        writeMsg(mmOutputStream, "営業時間: " + shopInfo.getString("time1") + "  " + shopInfo.getString("time2") + "\n")
//        writeMsg(mmOutputStream, "〒110-0005  東京都台東区上野4丁⽬4-5上野C-Roadビル 3F  TEL: 03-6806-0583\n")
        writeMsg(mmOutputStream, shopInfo.getString("post") + "  " + shopInfo.getString("addr1") + shopInfo.getString("addr2")
                    + "   TEL: " + shopInfo.getString("tel") + "\n")

        // ぺージモードのデータ印字
        mmOutputStream.write(byteArrayOf(0x1B, 0x1D, 0x50, 0x37))

        //钱箱
//        mmOutputStream.write(byteArrayOf(0x1B, 0x70, 48, 1, 1))
//        mmOutputStream.write(byteArrayOf(0x07))

        // 打印并走纸
//        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
        // 切纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 3))

    }



    fun printDetail(mmOutputStream: OutputStream, data: String) {
        Log.v("test", data)
        var order = JSONObject(data);
        var details = order.getJSONObject("details")
        var money = order.getJSONObject("money")
        var taxDetails = order.getJSONObject("taxDetails")

        //居中对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x45, 1))
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))

        writeMsg(mmOutputStream, "手作り餃子\n")
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x22))

        writeMsg(mmOutputStream, "西遊記\n")
        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 30))

        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x45, 0))
        // 宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        writeMsg(mmOutputStream, "営業時間: 11:00am~15:00pm\n")
        writeMsg(mmOutputStream, "          17:00pm~21:00pm\n")
        writeMsg(mmOutputStream, "定 休 日: 月曜日\n")


        writeMsg(mmOutputStream, "〒174-0046\n東京都板橋区蓮根3-9-7\nメトロード西台二番館B3\n")
        writeMsg(mmOutputStream, "TEL: 03-5918-7693\n")
        writeMsg(mmOutputStream, "アルバイトパートさん募集中★\n")
        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))
        writeMsg(mmOutputStream, "------------------------------\n")
        writeMsg(mmOutputStream, "領 収 書\n")


        var sIterator = details.keys();
        while(sIterator.hasNext()) {
            // 获得key
            var key = sIterator.next();
            // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
            var detail = details.getJSONObject(key);
            if (detail.getString("price").toInt()>0) {

                // left对齐
                mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
                writeMsg(mmOutputStream, (detail.getString("menu_no") + " " + detail.getString("menu_name") + "\n"))

                // right对齐
                mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
                writeMsg(mmOutputStream, (detail.getString("count") + "コ  X " + detail.getString("price") + " ￥" + format_price(detail.getString("total")) + "\n"))
            }
        }

        // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        writeMsg(mmOutputStream, "小計(税抜)\n")
        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("total")) + "\n"))


        // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        writeMsg(mmOutputStream, ("消費税等("+ money.getString("tax") + "%)\n"))
        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("tax_value")) + "\n"))


        if ( taxDetails.length()>0 ) {

            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
            writeMsg(mmOutputStream, "-- 税込メニュー\n")
            sIterator = taxDetails.keys();
            while(sIterator.hasNext()) {
                // 获得key
                var key = sIterator.next();
                // 根据key获得value, value也可以是JSONObject,JSONArray,使用对应的参数接收即可
                var detail = taxDetails.getJSONObject(key);

                if (detail.getString("price").toInt()>0 ) {

                    // left对齐
                    mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
                    writeMsg(mmOutputStream, (detail.getString("menu_no") + " " + detail.getString("menu_name") + "\n"))

                    // left对齐
                    mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
                    writeMsg(mmOutputStream, (detail.getString("count") + "コ  X " + detail.getString("price") + " ￥" + format_price(detail.getString("total")) + "\n"))
                }
            }


            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
            writeMsg(mmOutputStream, "小計(税込)\n")
            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
            writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("price_tax_in")) + "\n"))
        }


        if(money.getString("cut").toInt()>0) {
            // left对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
            writeMsg(mmOutputStream, ("割引(" + money.getString("cut") + "%)\n"))

            // right对齐
            mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
            writeMsg(mmOutputStream, ("-￥" + format_price(money.getString("cut_value")) + "\n"))
        }


        // 对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))
        writeMsg(mmOutputStream, "------------------------------\n")

        // left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        writeMsg(mmOutputStream, "合計\n")

        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))
        writeMsg(mmOutputStream, ("￥"+ format_price(money.getString("amounts_actually")) + "\n"))

        // right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        writeMsg(mmOutputStream, "毎度ありがとうございます。\n")

        //日文
        writeMsg(mmOutputStream, "ご来店をお待ちしております！\n")

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        val currentTime = System.currentTimeMillis()
        val timeNow = SimpleDateFormat("yyyy年MM月dd日 HH時MM分").format(currentTime)
        writeMsg(mmOutputStream, (timeNow + "\n"))

        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 4))

//                    var mediaplayer: MediaPlayer = MediaPlayer()
//                    mediaplayer.setDataSource("http://www.food-life.co.jp/test.m4a")
//                    mediaplayer.prepare()
//                    mediaplayer.start()
//                    Thread.sleep(7000)
        Thread.sleep(2000)
         // 切纸
         mmOutputStream.write(byteArrayOf(0x1D, 0x56, 0))
    }

    fun printCounter(mmOutputStream: OutputStream, data: String) {
        var order = JSONObject(data);

        // 重置打印机
//        mmOutputStream.write(byteArrayOf(0x1B, 0x40))
//                    // 页模式
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x4C))
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x4C))
//                    // 打印方式由上到下
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x54, 51))
//                    // 倒置模式
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x7B, 1))

        //日文 打印机设置
//        mmOutputStream.write(byteArrayOf(0x1F, 0x1B, 0x1F, 0x46, 0x4F, 0x4E, 0x54, 0x03))

        //日文
//        mmOutputStream.write(byteArrayOf(0x1B, 0x52, 8))
        //居中对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

        //字符代码表 日文
//        mmOutputStream.write(byteArrayOf(0x1B, 0x74, 71))
        //字符代码表 日文
//        mmOutputStream.write(byteArrayOf(0x1B, 0x74, 1))

//                    //顺时针90度
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x56, 1))

        // 顺时针90度　恢复
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x56, 0))

//                    // 默认宽高
//                    mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
//                    mmOutputStream.write(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()).toByteArray())
//                    mmOutputStream.write("\n".toByteArray())

        // 图片
//        printImg()
        // 行间距
//        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 80))


            // 打印并走纸
            mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
            // 加粗
            mmOutputStream.write(byteArrayOf(0x1B, 0x45, 1))
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
        var msg = ("手作り餃子\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x22))
        msg = ("西遊記\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))

        // 取消加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x45, 0))
        // 两倍宽高
//      mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x11))


        // 默认宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 0))
        // 打印并走纸
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 40))
        // 行间距
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x33, 127))
        Log.v("test", "1")
        // 打印并走纸
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))

        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 0))

//        msg = ("株式会社フ-トライフソリュ-ションズ\n").toByteArray(Charset.forName("GBK"))
//        mmOutputStream.write(msg)

        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 30))

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        msg = ( "営業時間: 11:00am~15:00pm\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        msg = ( "　　　　  17:00pm~21:00pm\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        msg = ( "定 休 日: 月曜日\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)


        msg = ( "〒174-0046\n東京都板橋区蓮根3-9-7\nメトロード西台二番館B3\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        msg = ("TEL: 03-5918-7693\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        msg = ("アルバイトパートさん募集中★\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)


        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 40))
        // 行间距
//        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 120))
        // 默认宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))


        //center对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x45, 1))
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x11))
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        msg = ("領 収 書\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)

        //right对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 2))

        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 0))
        //下划线
//        mmOutputStream.write(byteArrayOf(0x1C, 0x2D, 1))
        mmOutputStream.write("　　　　　　　様\n".toByteArray(Charset.forName("GBK")))
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
        mmOutputStream.write("------------------------------\n".toByteArray(Charset.forName("GBK")))
        // 两倍宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x11))
        msg = ("　　￥" + format_price(order.getString("amounts_actually")) + "―\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
        mmOutputStream.write("------------------------------\n".toByteArray(Charset.forName("GBK")))
        // 取消加粗

        // 取消下划线
//        mmOutputStream.write(byteArrayOf(0x1C, 0x2D, 0))

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))
        // 行间距
        mmOutputStream.write(byteArrayOf(0x1B, 0x33, 30))
        // 宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x10))

        // 默认宽高
        mmOutputStream.write(byteArrayOf(0x1D, 0x21, 0x00))
        //居中对齐
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x61, 1))

        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x45, 0))

        msg = ("(消費税等￥"+ format_price(order.getString("amounts_actually_tax")) +") \n但し飲食代として、\n上記正に領収いたました。\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)


        // 行间距
        // 加粗
        mmOutputStream.write(byteArrayOf(0x1B, 0x45, 0))
        // 行间距
        mmOutputStream.write(byteArrayOf(0x1D, 0x50, 4, 0))

        //left对齐
        mmOutputStream.write(byteArrayOf(0x1B, 0x61, 0))

//        msg = (".............................\n").toByteArray(Charset.forName("GBK"))
//        mmOutputStream.write(msg)
        msg = ("毎度ありがとうございます。\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        //日文
        msg = ("ご来店をお待ちしております！\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 1))
        val currentTime = System.currentTimeMillis()
        val timeNow = SimpleDateFormat("yyyy年MM月dd日 HH時MM分").format(currentTime)
        msg = (timeNow + "\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)

        msg = ("No." + order.getString("no") + "\n").toByteArray(Charset.forName("GBK"))
        mmOutputStream.write(msg)
        // 开启钱箱
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x70, 48, 1, 1))
//                    mmOutputStream.write(byteArrayOf(0x1B, 0x70, 48, 1, 1))


        // 打印并走纸
        mmOutputStream.write(byteArrayOf(0x1B, 0x64, 4))

//                    var mediaplayer: MediaPlayer = MediaPlayer()
//                    mediaplayer.setDataSource("http://www.food-life.co.jp/test.m4a")
//                    mediaplayer.prepare()
//                    mediaplayer.start()
//                    Thread.sleep(7000)
        Thread.sleep(1000)
        //切纸
//        mmOutputStream.write(byteArrayOf(0x1D, 0x56, 0))
    }

    fun writeMsg(mmOutputStream: OutputStream, str: String) {
//        var msg = str.toByteArray(Charset.forName("GBK"))
        var msg = str.toByteArray(Charset.forName(this.charset))
        mmOutputStream.write(msg)
    }

    fun format_price(price: String): String {
        return DecimalFormat(",###").format(price.toInt()).toString()
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        //his.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        this.setContentView(R.layout.your_layout_name_here);



        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }


    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.

                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}


