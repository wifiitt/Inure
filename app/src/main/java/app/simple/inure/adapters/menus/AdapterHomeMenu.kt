package app.simple.inure.adapters.menus

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.constants.Colors
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayoutWithFactor
import app.simple.inure.decorations.theme.ThemeIcon
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.popups.home.PopupMenuLayout
import app.simple.inure.preferences.AccessibilityPreferences
import app.simple.inure.preferences.HomePreferences
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.RecyclerViewUtils

class AdapterHomeMenu(private val list: List<Pair<Int, Int>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private lateinit var adapterHomeMenuCallbacks: AdapterHomeMenuCallbacks

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewUtils.TYPE_ITEM -> {
                when (HomePreferences.getMenuLayout()) {
                    PopupMenuLayout.VERTICAL -> {
                        Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_menu_vertical, parent, false))
                    }
                    PopupMenuLayout.GRID -> {
                        Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_menu, parent, false))
                    }
                    else -> {
                        throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
                    }
                }
            }
            RecyclerViewUtils.TYPE_DIVIDER -> {
                Divider(LayoutInflater.from(parent.context).inflate(R.layout.adapter_divider_preferences, parent, false))
            }
            else -> {
                throw RuntimeException("there is no type that matches the type $viewType + make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position: Int) {
        if (holder is Holder) {
            holder.icon.transitionName = holder.itemView.context.getString(list[position].second)
            holder.icon.setImageResource(list[position].first)
            holder.text.text = holder.itemView.context.getString(list[position].second)

            if (AccessibilityPreferences.isColorfulIcons()) {
                holder.icon.imageTintList = ColorStateList(arrayOf(intArrayOf(
                        android.R.attr.state_enabled
                ), intArrayOf()), intArrayOf(
                        Colors.getColors()[position],
                        Colors.getColors()[position]
                ))
            }

            holder.container.setOnClickListener {
                adapterHomeMenuCallbacks.onMenuItemClicked(list[position].second, holder.icon)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].first.isZero()) {
            RecyclerViewUtils.TYPE_DIVIDER
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val icon: ThemeIcon = itemView.findViewById(R.id.adapter_app_info_menu_icon)
        val text: TypeFaceTextView = itemView.findViewById(R.id.adapter_app_info_menu_text)
        val container: DynamicRippleLinearLayoutWithFactor = itemView.findViewById(R.id.adapter_app_info_menu_container)

        init {
            text.isSelected = true

            if (AccessibilityPreferences.isColorfulIcons()) {
                icon.setTintType(3)
            }
        }
    }

    inner class Divider(itemView: View) : VerticalListViewHolder(itemView)

    fun setOnAppInfoMenuCallback(adapterHomeMenuCallbacks: AdapterHomeMenuCallbacks) {
        this.adapterHomeMenuCallbacks = adapterHomeMenuCallbacks
    }

    interface AdapterHomeMenuCallbacks {
        fun onMenuItemClicked(source: Int, icon: ImageView)
    }
}