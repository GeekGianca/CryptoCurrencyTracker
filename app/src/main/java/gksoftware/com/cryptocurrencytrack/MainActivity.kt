package gksoftware.com.cryptocurrencytrack

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Adapter
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import gksoftware.com.cryptocurrencytrack.Adapter.CoinAdapter
import gksoftware.com.cryptocurrencytrack.Common.Common
import gksoftware.com.cryptocurrencytrack.Interface.ILoadMore
import gksoftware.com.cryptocurrencytrack.Model.CoinModel
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(), ILoadMore {
    //Variable
    internal var items:MutableList<CoinModel> = ArrayList()
    internal lateinit var adapter: CoinAdapter
    internal lateinit var client:OkHttpClient
    internal lateinit var request: Request

    override fun onLoadMore() {
        if (items.size <= Common.MAX_COIN_LOAD)
            loadNext10Coins(items.size)
        else
            Toast.makeText(this@MainActivity, "Data max is "+Common.MAX_COIN_LOAD, Toast.LENGTH_SHORT)
                .show()
    }

    private fun loadNext10Coins(size: Int) {
        client = OkHttpClient()
        request = Request.Builder()
            .url(String.format("https://api.coinmarketcap.com/v1/ticker/?start=%d&limit=10", size))
            .build()
        swipetorefresh.isRefreshing = true
        client.newCall(request)
            .enqueue(object :Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Error", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body()!!.string()
                    val gson = Gson()
                    val newItems = gson.fromJson<List<CoinModel>>(body, object :TypeToken<List<CoinModel>>(){}.type)
                    runOnUiThread {
                        items.addAll(newItems)
                        adapter.setLoaded()
                        adapter.updateData(items)
                        swipetorefresh.isRefreshing = false
                    }
                }
            })
    }

    private fun loadFirst10Coins() {
        client = OkHttpClient()
        request = Request.Builder()
            .url(String.format("https://api.coinmarketcap.com/v1/ticker/?start=0&limit=10"))
            .build()
        client.newCall(request)
            .enqueue(object :Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("Error", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body()!!.string()
                    val gson = Gson()
                    items = gson.fromJson(body, object :TypeToken<List<CoinModel>>(){}.type)
                    runOnUiThread {
                        adapter.updateData(items)
                    }
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipetorefresh.post { loadFirst10Coins() }
        swipetorefresh.setOnRefreshListener {
            items.clear()
            loadFirst10Coins()
            setUpAdapter()
        }
        recyclercoin.layoutManager = LinearLayoutManager(this)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        adapter = CoinAdapter(recyclercoin, this@MainActivity, items)
        recyclercoin.adapter = adapter
        adapter.setLoadMore(this)
    }
}
