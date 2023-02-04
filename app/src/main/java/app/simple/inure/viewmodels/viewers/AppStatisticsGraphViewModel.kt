package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.app.usage.UsageEvents
import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.collection.SparseArrayCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.UsageStatsViewModel
import app.simple.inure.models.AppUsageModel
import app.simple.inure.models.DataUsage
import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import app.simple.inure.util.CalendarUtils
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.LocaleHelper
import app.simple.inure.util.UsageInterval
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

class AppStatisticsGraphViewModel(application: Application, private val packageInfo: PackageInfo) : UsageStatsViewModel(application) {

    private val packageStats: MutableLiveData<PackageStats> by lazy {
        MutableLiveData<PackageStats>().also {
            loadStatsData()
        }
    }

    private val barChartData: MutableLiveData<ArrayList<BarEntry>> by lazy {
        MutableLiveData<ArrayList<BarEntry>>()
    }

    private val pieChartData: MutableLiveData<ArrayList<PieEntry>> by lazy {
        MutableLiveData<ArrayList<PieEntry>>()
    }

    fun getPackageStats(): LiveData<PackageStats> {
        return packageStats
    }

    fun getChartData(): LiveData<ArrayList<BarEntry>> {
        return barChartData
    }

    fun getPieChartData(): LiveData<ArrayList<PieEntry>> {
        return pieChartData
    }

    private fun loadStatsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(getUsageEvents()) {
                    if (this.appUsage?.size?.isNotZero() == true) {
                        packageStats.postValue(this)
                        loadPieChartData(this)
                        loadChartData(this)
                    } else {
                        warning.postValue(getString(R.string.usage_data_does_not_exist_for_this_app))
                    }
                }
            }.getOrElse {
                postError(it)
            }
        }
    }

    private fun getUsageEvents(): PackageStats {
        val packageStats = PackageStats()
        packageStats.packageInfo = packageInfo
        packageStats.appUsage = arrayListOf()

        val interval = UsageInterval.getTimeInterval()
        val events: UsageEvents = usageStatsManager.queryEvents(interval.startTime, interval.endTime)
        val event = UsageEvents.Event()

        var startTime: Long
        var endTime: Long
        var skipNew = false
        var iteration = 0

        while (events.hasNextEvent()) {
            if (!skipNew) events.getNextEvent(event)

            var eventTime = event.timeStamp

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) { // App is visible (foreground)
                startTime = eventTime

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    eventTime = event.timeStamp

                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        skipNew = true
                        break
                    } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        endTime = eventTime
                        skipNew = false
                        if (packageInfo.packageName.equals(event.packageName)) {
                            val time = endTime - startTime + 1
                            packageStats.appUsage?.add(iteration, AppUsageModel(startTime, time, endTime))
                            packageStats.launchCount = iteration.plus(1)
                            packageStats.totalTimeUsed += time
                            packageStats.lastUsageTime = endTime

                            iteration++
                        }
                        break
                    }
                }
            }
        }

        packageStats.appUsage?.reverse()
        getDataUsage(packageStats)

        return packageStats
    }

    private fun getDataUsage(packageStats: PackageStats) {
        var mobileData = SparseArrayCompat<DataUsage>()
        var wifiData = SparseArrayCompat<DataUsage>()

        kotlin.runCatching {
            mobileData = getMobileData(StatisticsPreferences.getInterval())
        }
        kotlin.runCatching {
            wifiData = getWifiData(StatisticsPreferences.getInterval())
        }

        val uid: Int = packageStats.packageInfo?.applicationInfo?.uid!!

        if (mobileData.containsKey(uid)) {
            packageStats.mobileData = mobileData[uid]
        } else {
            packageStats.mobileData = DataUsage.EMPTY
        }

        if (wifiData.containsKey(uid)) {
            packageStats.wifiData = wifiData[uid]
        } else {
            packageStats.wifiData = DataUsage.EMPTY
        }
    }

    private fun loadChartData(packageStats: PackageStats) {
        viewModelScope.launch(Dispatchers.Default) {
            val barEntries = arrayListOf<BarEntry>()

            packageStats.appUsage?.forEach { it ->
                val number = CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis()) //(7 - it.date.toLocalDate().dayOfWeek.value) % 7
                // Log.d("reversedWeekNumber", "$reversedWeekNumber: ${it.date.toLocalDate().dayOfWeek.value}")

                Log.d("BarChart", "Day: $number, Date: ${it.date.toDate()}, Label: ${getDayString(it.date.toLocalDate())}")

                try {
                    barEntries[number].y += it.startTime
                    barEntries[number].data = getDayString(it.date.toLocalDate())
                } catch (e: IndexOutOfBoundsException) {
                    barEntries.add(number, BarEntry(number.toFloat(), it.startTime.toFloat(), getDayString(it.date.toLocalDate())))
                }
            }

            // Flip x value of bar entries
            @Suppress("UNCHECKED_CAST")
            val copy = barEntries.clone() as ArrayList<BarEntry>

            for (i in 0 until copy.size) {
                barEntries[i].x = (copy.size - 1 - i).toFloat()
            }

            barChartData.postValue(barEntries)
        }
    }

    private fun loadPieChartData(packageStats: PackageStats) {
        viewModelScope.launch(Dispatchers.Default) {
            val pieEntries = arrayListOf<PieEntry>()

            packageStats.appUsage?.forEach {
                val numberOfDays = CalendarUtils.getDaysBetweenTwoDates(it.date, System.currentTimeMillis())

                Log.d("PieChart", "Day: $numberOfDays, Date: ${it.date.toDate()}, Label: ${getDayString(it.date.toLocalDate())}")

                try {
                    val pieEntry = PieEntry(pieEntries[numberOfDays].value + it.startTime, getDayString(it.date.toLocalDate()))
                    pieEntries.remove(pieEntries[numberOfDays])
                    pieEntries.add(numberOfDays, pieEntry)
                } catch (e: java.lang.IndexOutOfBoundsException) {
                    pieEntries.add(numberOfDays, PieEntry(it.startTime.toFloat(), getDayString(it.date.toLocalDate())))
                }
            }

            pieChartData.postValue(pieEntries)
        }
    }

    //    private fun calculateDailyAverage(pieEntries: List<PieEntry>) {
    //        var tally = 0
    //        var total = 0L
    //
    //        for (i in pieEntries.indices) {
    //            if (pieEntries[i].value.isNotZero()) {
    //                tally++
    //                total += pieEntries[i].value.toLong()
    //            }
    //        }
    //
    //        val average = total / pieEntries.size
    //
    //        dailyAverage.postValue(average)
    //    }

    private fun Context.getWeekName(weekNumber: Int): String {
        return when (weekNumber) {
            1 -> getString(R.string.sun)
            2 -> getString(R.string.mon)
            3 -> getString(R.string.tue)
            4 -> getString(R.string.wed)
            5 -> getString(R.string.thu)
            6 -> getString(R.string.fri)
            7 -> getString(R.string.sat)
            else -> ""
        }
    }

    private fun getDayString(date: LocalDate): String? {
        return date.dayOfWeek.getDisplayName(TextStyle.SHORT, LocaleHelper.getAppLocale())
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    @Suppress("unused")
    private fun loadTotalAppSize() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = getInstalledApps()

            var size = 0L

            for (i in apps.indices) {
                size += apps[i].applicationInfo.sourceDir.getDirectoryLength()
            }
        }
    }
}
