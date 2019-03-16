package gksoftware.com.cryptocurrencytrack.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import gksoftware.com.cryptocurrencytrack.Common.Common
import gksoftware.com.cryptocurrencytrack.Interface.ILoadMore
import gksoftware.com.cryptocurrencytrack.Model.CoinModel
import gksoftware.com.cryptocurrencytrack.R
import kotlinx.android.synthetic.main.coin_item_layout.view.*
import java.lang.StringBuilder
import java.text.DecimalFormat

class CoinViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
    var coinIcon = itemView.coinIcon
    var coinSymbol = itemView.coinSymbol
    var coinName = itemView.coinName
    var coinPrice = itemView.priceUsd
    var oneHourChange = itemView.oneHour
    var twentyFourChange = itemView.twentyFourHour
    var sevenDayChange = itemView.sevenDay

}

class CoinAdapter(recyclerView: RecyclerView, internal var activity: Activity, var items:List<CoinModel>):
    RecyclerView.Adapter<CoinViewHolder>() {

    internal var loadMore:ILoadMore?=null
    var isLoading:Boolean=false
    var visibleThreshold=5
    var lastVisibleItem:Int=0
    var totalItemCount:Int=0

    init {
        val linearLayout = recyclerView.layoutManager as LinearLayoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayout.itemCount
                lastVisibleItem = linearLayout.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= lastVisibleItem+visibleThreshold){
                    if (loadMore != null)
                        loadMore!!.onLoadMore()
                    isLoading = true
                }
            }
        })
    }

    fun setLoadMore(loadMore: ILoadMore){
        this.loadMore = loadMore
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CoinViewHolder {
        var view = LayoutInflater.from(activity)
            .inflate(R.layout.coin_item_layout, p0, false)
        return CoinViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(p0: CoinViewHolder, p1: Int) {
        val coinModel = items.get(p1)
        var item = p0
        val df = DecimalFormat("###")
        df.maximumFractionDigits = 4
        item.coinName.text = coinModel.name
        item.coinSymbol.text = coinModel.symbol
        var parseF = df.parse(coinModel.price_usd)
        item.coinPrice.text = df.format(parseF)
        //item.coinPrice.text = coinModel.price_usd
        item.coinName.text = coinModel.name
        item.coinName.text = coinModel.name
        item.oneHourChange.text = coinModel.percent_change_1h+"%"
        item.twentyFourChange.text = coinModel.percent_change_24h+"%"
        item.sevenDayChange.text = coinModel.percent_change_7d+"%"

        Picasso.with(activity.baseContext)
            .load(StringBuilder(Common.imageUrl).append(coinModel.symbol!!.toLowerCase())
                .append(".png")
                .toString())
            .into(item.coinIcon)
        item.oneHourChange.setTextColor(if (coinModel.percent_change_1h!!.contains("-"))
        Color.parseColor("#FF0000")
        else
        Color.parseColor("#32CD32"))

        item.twentyFourChange.setTextColor(if (coinModel.percent_change_24h!!.contains("-"))
            Color.parseColor("#FF0000")
        else
            Color.parseColor("#32CD32"))

        item.sevenDayChange.setTextColor(if (coinModel.percent_change_7d!!.contains("-"))
            Color.parseColor("#FF0000")
        else
            Color.parseColor("#32CD32"))
    }

    fun setLoaded(){
        isLoading = false
    }

    fun updateData(coinModels:List<CoinModel>){
        this.items = coinModels
        notifyDataSetChanged()
    }
}