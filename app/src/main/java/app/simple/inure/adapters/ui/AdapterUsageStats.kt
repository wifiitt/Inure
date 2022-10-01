package app.simple.inure.adapters.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.fastscroll.PopupTextProvider
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.ViewUtils.visible
import java.util.concurrent.TimeUnit

class AdapterUsageStats(private val list: ArrayList<PackageStats>) : RecyclerView.Adapter<VerticalListViewHolder>(), PopupTextProvider {

    private var adapterCallbacks: AdapterCallbacks? = null
    private var isLimitedToHours = StatisticsPreferences.isLimitToHours()
    private var isLoader = false // list.isEmpty()
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_usage_stats, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_usage_stats, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {

        val position = position_ - 1

        if (holder is Holder) {
            holder.icon.transitionName = "stats_app_$position"
            holder.icon.loadAppIcon(list[position].packageInfo!!.packageName)
            holder.name.text = list[position].packageInfo!!.applicationInfo.name
            holder.dataUp.text = list[position].mobileData?.tx?.toSize()
            holder.dataDown.text = list[position].mobileData?.rx?.toSize()
            holder.wifiUp.text = list[position].wifiData?.tx?.toSize()
            holder.wifiDown.text = list[position].wifiData?.rx?.toSize()

            holder.name.setStrikeThru(list[position].packageInfo?.applicationInfo?.enabled ?: false)

            with(list[position].totalTimeUsed) {
                holder.time.apply {
                    this.text = when {
                        TimeUnit.MILLISECONDS.toSeconds(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_seconds,
                                                   TimeUnit.MILLISECONDS.toSeconds(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toMinutes(this@with) < 60 -> {
                            this.context.getString(R.string.used_for_short,
                                                   TimeUnit.MILLISECONDS.toMinutes(this@with).toString())
                        }
                        TimeUnit.MILLISECONDS.toHours(this@with) < 24 -> {
                            this.context.getString(R.string.used_for_long,
                                                   TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                   (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                        }
                        else -> {
                            if (isLimitedToHours) {
                                this.context.getString(R.string.used_for_long,
                                                       TimeUnit.MILLISECONDS.toHours(this@with).toString(),
                                                       (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                            } else {
                                this.context.getString(R.string.used_for_days,
                                                       TimeUnit.MILLISECONDS.toDays(this@with).toString(),
                                                       (TimeUnit.MILLISECONDS.toHours(this@with) % 24).toString(),
                                                       (TimeUnit.MILLISECONDS.toMinutes(this@with) % 60).toString())
                            }
                        }
                    }
                }
            }

            holder.container.setOnClickListener {
                adapterCallbacks?.onAppClicked(list[position].packageInfo!!, holder.icon)
            }

            holder.container.setOnLongClickListener {
                adapterCallbacks?.onAppLongPressed(list[position].packageInfo!!, holder.icon)
                true
            }

        } else if (holder is Header) {
            holder.settings.setOnClickListener {
                adapterCallbacks?.onSettingsPressed(it)
            }

            holder.filter.setOnClickListener {
                adapterCallbacks?.onFilterPressed(it)
            }

            holder.sort.setOnClickListener {
                adapterCallbacks?.onSortPressed(it)
            }

            holder.search.setOnClickListener {
                adapterCallbacks?.onSearchPressed(it)
            }

            holder.total.text = String.format(holder.itemView.context.getString(R.string.total_apps), list.size)

            if (isLoader) holder.loader.visible(false)
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            GlideApp.with(holder.icon).clear(holder.icon)
        }
    }

    override fun getPopupText(position: Int): String {
        return list[position].packageInfo?.applicationInfo?.name?.substring(0, 1) ?: ""
    }

    fun setOnStatsCallbackListener(adapterCallbacks: AdapterCallbacks) {
        this.adapterCallbacks = adapterCallbacks
    }

    fun notifyAllData() {
        isLimitedToHours = StatisticsPreferences.isLimitToHours()
        for (i in list.indices) {
            notifyItemChanged(i.plus(1))
        }
    }

    fun enableLoader() {
        isLoader = true
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_usage_stats_container)
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val time: TypeFaceTextView = itemView.findViewById(R.id.total_time_used)
        val dataUp: TypeFaceTextView = itemView.findViewById(R.id.total_data_up_used)
        val dataDown: TypeFaceTextView = itemView.findViewById(R.id.total_data_down_used)
        val wifiUp: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_up_used)
        val wifiDown: TypeFaceTextView = itemView.findViewById(R.id.total_wifi_down_used)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.adapter_total_apps)
        val sort: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_sort_button)
        val filter: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_filter_button)
        val search: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_search_button)
        val settings: DynamicRippleImageButton = itemView.findViewById(R.id.adapter_header_configuration_button)
        val loader: CustomProgressBar = itemView.findViewById(R.id.loader)
    }
}