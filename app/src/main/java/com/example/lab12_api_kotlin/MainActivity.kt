package com.example.lab12_api_kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerReceiver(receiver, IntentFilter("Mymessage"))
        btn_query.setOnClickListener(View.OnClickListener {
            val req = Request.Builder()
                .url("https://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=55ec6d6e-dc5c-4268-a725-d04cc262172b\n")
                .build()
            OkHttpClient().newCall(req).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("查詢失敗", e.toString())
                }
                @Throws(IOException::class)
                override fun onResponse(
                    call: Call,
                    response: Response
                ) {
                    sendBroadcast(Intent("Mymessage").putExtra("json", response.body!!.string()))
                }
            })
        })
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val data = Gson().fromJson(intent.extras!!.getString("json"), Data::class.java)
            val items =
                arrayOfNulls<String>(data.result!!.results.size)
            for (i in items.indices) {
                items[i] = "\n列車即將進入:" +
                        data.result!!.results[i].Station +
                        "\n列車行駛目的地:" +
                        data.result!!.results[i].Destination
            }
            runOnUiThread {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("台北捷運列車到站站名")
                    .setItems(items, null)
                    .show()
            }
        }
    }
}

internal class Data {
    var result: Result? = null
    internal abstract inner class Result {
        abstract var results: Array<Results>
        internal inner class Results {
            var Station: String? = null
            var Destination: String? = null
        }
    }
}

