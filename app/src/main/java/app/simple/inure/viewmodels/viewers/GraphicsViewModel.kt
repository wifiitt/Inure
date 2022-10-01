package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.GraphicsPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class GraphicsViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    var keyword: String = ""
        set(value) {
            field = value
            getGraphicsData()
        }

    private val graphics: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getGraphicsData()
        }
    }

    fun getGraphics(): LiveData<MutableList<String>> {
        return graphics
    }

    private fun getGraphicsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getGraphicsFiles(packageInfo.applicationInfo.sourceDir, keyword)) {
                    if (this.isEmpty() && keyword.isEmpty()) throw NullPointerException()

                    graphics.postValue(apply {
                        sortBy {
                            it.lowercase(Locale.getDefault())
                        }
                    })
                }
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(666)
                } else {
                    error.postValue(it)
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GraphicsPreferences.png,
            GraphicsPreferences.jpg,
            GraphicsPreferences.jpeg,
            GraphicsPreferences.gif,
            GraphicsPreferences.webp,
            GraphicsPreferences.svg,
            -> {
                getGraphicsData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}