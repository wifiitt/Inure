package app.simple.inure.extensions.viewmodels

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.Misc
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.GeneratedDataPreferences
import app.simple.inure.util.ColorUtils.toHexColor
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.FlagUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

open class DataGeneratorViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var flags: Long = 0

    private val generatedAppDataPath: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getGeneratedDataPath(): LiveData<String> {
        return generatedAppDataPath
    }

    private var apps: ArrayList<PackageInfo> = arrayListOf()

    fun generateAppsData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(Misc.delay)
            this@DataGeneratorViewModel.apps = apps

            val path = applicationContext().cacheDir.absolutePath +
                    "/all_apps_generated_data.${GeneratedDataPreferences.getGeneratedDataType()}"

            with(File(path)) {
                if (exists()) {
                    if (delete()) {
                        Log.d("DataGeneratorViewModel", "Deleted old generated data file")
                    } else {
                        Log.d("DataGeneratorViewModel", "Failed to delete old generated data file")
                    }
                }
            }

            val stringBuilder = getGeneratedString()

            FileOutputStream(path).use { fileOutputStream ->
                OutputStreamWriter(fileOutputStream).use {
                    // skip the formatting here, the viewer should format the files
                    it.write(stringBuilder.toString())
                }
            }

            generatedAppDataPath.postValue(path)
        }
    }

    @JvmName("generateAppsData1")
    fun generateAppsData(apps: ArrayList<BatchPackageInfo>) {
        val list = arrayListOf<PackageInfo>()
        apps.forEach {
            list.add(it.packageInfo)
        }
        generateAppsData(list)
    }

    private fun getGeneratedString(): StringBuilder {
        flags = GeneratedDataPreferences.getGeneratorFlags()

        return when (GeneratedDataPreferences.getGeneratedDataType()) {
            GeneratedDataPreferences.XML -> generateXML()
            GeneratedDataPreferences.JSON -> generateJSON()
            GeneratedDataPreferences.TXT -> generateTXT()
            GeneratedDataPreferences.CSV -> generateCSV()
            GeneratedDataPreferences.HTML -> generateHTML()
            GeneratedDataPreferences.MD -> generateMD()
            else -> StringBuilder()
        }
    }

    private fun generateXML(): StringBuilder {
        val stringBuilder = StringBuilder()
        stringBuilder.append("<!-- Generated by Inure -->\r\n")
        stringBuilder.append("<!-- Total Apps: ${apps.size} -->\r\n")
        stringBuilder.append("<!-- Generated on ${System.currentTimeMillis().toDate()} -->\r\n\n")
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        stringBuilder.append("<generated_data_of_apps>\n")

        for (app in apps) {
            // Generate xml
            stringBuilder.append("\n\t<app>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
                stringBuilder.append("\t\t<name>${app.applicationInfo.name}</name>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
                stringBuilder.append("\t\t<package_name>${app.packageName}</package_name>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
                stringBuilder.append("\t\t<version_name>${app.versionName}</version_name>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
                stringBuilder.append("\t\t<first_install_time>${app.firstInstallTime.toDate()}</first_install_time>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
                stringBuilder.append("\t\t<last_update_time>${app.lastUpdateTime.toDate()}</last_update_time>\n")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                    stringBuilder.append("\t\t<minimum_sdk>${app.applicationInfo.minSdkVersion}</minimum_sdk>\n")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
                stringBuilder.append("\t\t<target_sdk>${app.applicationInfo.targetSdkVersion}</target_sdk>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE)) {
                stringBuilder.append("\t\t<size>${app.getSize()}</size>\n")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
                stringBuilder.append("\t\t<play_store_link>https://play.google.com/store/apps/details?id=${app.packageName}</play_store_link>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
                stringBuilder.append("\t\t<fdroid_link>https://f-droid.org/en/packages/${app.packageName}</fdroid_link>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
                stringBuilder.append("\t\t<amazon_store_link>https://www.amazon.com/gp/mas/dl/android?p=${app.packageName}</amazon_store_link>\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
                stringBuilder.append("\t\t<galaxy_store_link>https://galaxystore.samsung.com/detail/${app.packageName}</galaxy_store_link>\n")

            stringBuilder.append("\t<app>\n")
        }

        stringBuilder.append("\n</generated_data_of_apps>")

        return stringBuilder
    }

    private fun generateTXT(): StringBuilder {
        val stringBuilder = StringBuilder()
        stringBuilder.append("// Generated by Inure\r\n")
        stringBuilder.append("// Total Apps: ${apps.size}\r\n")
        stringBuilder.append("// Generated on ${System.currentTimeMillis().toDate()}\r\n")

        for (i in apps.indices) {
            stringBuilder.append("\n\n")
            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
                stringBuilder.append("Name: ${apps[i].applicationInfo.name}\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
                stringBuilder.append(apps[i].applicationInfo.packageName + "\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
                stringBuilder.append(apps[i].versionName + "\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
                stringBuilder.append(apps[i].firstInstallTime.toDate() + "\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
                stringBuilder.append(apps[i].lastUpdateTime.toDate() + "\n")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                    stringBuilder.append(apps[i].applicationInfo.minSdkVersion.toString() + "\n")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
                stringBuilder.append(apps[i].applicationInfo.targetSdkVersion.toString() + "\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE)) {
                stringBuilder.append("${apps[i].getSize()}\n")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
                stringBuilder.append("https://play.google.com/store/apps/details?id=${apps[i].packageName}\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
                stringBuilder.append("https://f-droid.org/en/packages/${apps[i].packageName}\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
                stringBuilder.append("https://www.amazon.com/gp/mas/dl/android?p=${apps[i].packageName}\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
                stringBuilder.append("https://galaxystore.samsung.com/detail/${apps[i].packageName}\n")
        }

        stringBuilder.append("\n")

        return stringBuilder
    }

    private fun generateJSON(): StringBuilder {
        val stringBuilder = StringBuilder()

        stringBuilder.append("{")
        stringBuilder.append("\n\t\"generated_by\": \"Inure\",")
        stringBuilder.append("\n\t\"total_apps\": ${apps.size},")
        stringBuilder.append("\n\t\"generated_on\": \"${System.currentTimeMillis().toDate()}\",")
        stringBuilder.append("\n\t\"apps\": [")

        for (app in apps) {
            stringBuilder.append("\n\t\t{")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME)) {
                stringBuilder.append("\n\t\t\t\"name\": \"${app.applicationInfo.name}\",")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME)) {
                stringBuilder.append("\n\t\t\t\"package_name\": \"${app.packageName}\",")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
                    stringBuilder.append("\n\t\t\t\"version_name\": \"${app.versionName}\",")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
                    stringBuilder.append("\n\t\t\t\"first_install_time\": \"${app.firstInstallTime.toDate()}\",")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
                    stringBuilder.append("\n\t\t\t\"last_update_time\": \"${app.lastUpdateTime.toDate()}\",")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                        stringBuilder.append("\n\t\t\t\"minimum_sdk\": \"${app.applicationInfo.minSdkVersion}\",")
                }

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
                    stringBuilder.append("\n\t\t\t\"target_sdk\": \"${app.applicationInfo.targetSdkVersion}\",")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE)) {
                    stringBuilder.append("\n\t\t\t\"size\": \"${app.getSize()}\",")
                }

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
                    stringBuilder.append("\n\t\t\t\"play_store_link\": \"https://play.google.com/store/apps/details?id=${app.packageName}\",")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
                    stringBuilder.append("\n\t\t\t\"fdroid_link\": \"https://f-droid.org/en/packages/${app.packageName}\"")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
                    stringBuilder.append("\n\t\t\t\"amazon_store_link\": \"https://www.amazon.com/gp/mas/dl/android?p=${app.packageName}\"")

                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
                    stringBuilder.append("\n\t\t\t\"galaxy_store_link\": \"https://galaxystore.samsung.com/detail/${app.packageName}\"")

                stringBuilder.append("\n\t\t},")
            }

            stringBuilder.append("\n\t]")
            stringBuilder.append("\n}")

            stringBuilder.append("\n")
        }

        return stringBuilder
    }

    private fun generateCSV(): StringBuilder {
        val stringBuilder = StringBuilder()

        // Create csv headers first
        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
            stringBuilder.append("\"Name,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
            stringBuilder.append("\"Package Name,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
            stringBuilder.append("\"Version Name,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
            stringBuilder.append("\"First Install Time,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
            stringBuilder.append("\"Last Update Time,\"")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                stringBuilder.append("\"Minimum SDK,\"")
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
            stringBuilder.append("\"Target SDK,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
            stringBuilder.append("\"Size,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
            stringBuilder.append("\"Play Store Link,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
            stringBuilder.append("\"F-Droid Link,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
            stringBuilder.append("\"Amazon Store Link,\"")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
            stringBuilder.append("\"Galaxy Store Link,\"")

        stringBuilder.append("\n")

        for (app in apps) {
            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
                stringBuilder.append("\"${app.applicationInfo.name}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
                stringBuilder.append("\"${app.packageName}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
                stringBuilder.append("\"${app.versionName}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
                stringBuilder.append("\"${app.firstInstallTime.toDate()}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
                stringBuilder.append("\"${app.lastUpdateTime.toDate()}\",")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                    stringBuilder.append("\"${app.applicationInfo.minSdkVersion}\",")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
                stringBuilder.append("\"${app.applicationInfo.targetSdkVersion}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
                stringBuilder.append("\"${app.getSize()}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
                stringBuilder.append("\"https://play.google.com/store/apps/details?id=${app.packageName}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
                stringBuilder.append("\"https://f-droid.org/en/packages/${app.packageName}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
                stringBuilder.append("\"https://www.amazon.com/gp/mas/dl/android?p=${app.packageName}\",")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
                stringBuilder.append("\"https://galaxystore.samsung.com/detail/${app.packageName}\",")

            stringBuilder.append("\n")
        }

        stringBuilder.append("\n")

        return stringBuilder
    }

    private fun generateHTML(): StringBuilder {
        val stringBuilder = StringBuilder()

        stringBuilder.append("<!DOCTYPE html>\r\n")
        stringBuilder.append("<html>\r\n")
        stringBuilder.append("<head>\r\n")
        stringBuilder.append("\t<title>Generated by Inure App Manager</title>\r\n")
        // Title color to black

        stringBuilder.append("\t<style>\r\n")
        stringBuilder.append("\t\ttable {\r\n")
        stringBuilder.append("\t\t\tfont-family: arial, sans-serif;\r\n")
        stringBuilder.append("\t\t\ttable-layout: fixed;\r\n")
        stringBuilder.append("\t\t\tborder-collapse: collapse;\r\n")
        // Corner radius
        stringBuilder.append("\t\t\tborder-radius: 10px;\r\n")
        // Border color
        stringBuilder.append("\t\t\tborder: 1px solid ${AppearancePreferences.getAccentColor().toHexColor()};\r\n")
        stringBuilder.append("\t\t\twidth: 100%;\r\n")
        stringBuilder.append("\t\t}\r\n")
        stringBuilder.append("\t\tth, td {\r\n")
        stringBuilder.append("\t\t\ttext-align: left;\r\n")
        stringBuilder.append("\t\t\tpadding: 8px;\r\n")
        // Break long words to prevent overlapping
        stringBuilder.append("\t\t\tword-wrap: break-word;\r\n")
        // Text color to black
        stringBuilder.append("\t\t\tcolor: #000000;\r\n")
        stringBuilder.append("\t\t}\r\n")
        stringBuilder.append("\t\ttr:nth-child(even){background-color: #f2f2f2}\r\n")
        stringBuilder.append("\t\tth {\r\n")
        stringBuilder.append("\t\t\tbackground-color: ${AppearancePreferences.getAccentColor().toHexColor()};\r\n")
        stringBuilder.append("\t\t\tcolor: white;\r\n")
        stringBuilder.append("\t\t}\r\n")
        stringBuilder.append("\t</style>\r\n")
        stringBuilder.append("\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\r\n")
        stringBuilder.append("\t<meta charset=\"UTF-8\">\r\n")
        stringBuilder.append("\t<meta name=\"description\" content=\"Generated by Inure\">\r\n")
        stringBuilder.append("\t<meta name=\"keywords\" content=\"Inure, Android, APK, App, Application, Package, Name, Version, Code, First Install Time, Last Update Time, Play Store, F-Droid\">\r\n")
        stringBuilder.append("\t<meta name=\"author\" content=\"Inure App Manager\">\r\n")
        stringBuilder.append("\t<meta name=\"theme-color\" content=\"#4CAF50\">\r\n")
        stringBuilder.append("\t<meta name=\"robots\" content=\"noindex, nofollow\">\r\n")
        stringBuilder.append("</head>\r\n")
        stringBuilder.append("<body>\r\n")
        stringBuilder.append("\t<table>\r\n")
        stringBuilder.append("\t\t<tr>\r\n")

        // Add Generated by Inure App Manager heading
        // h2 color to black
        stringBuilder.append("\t\t\t<h2 style=\"color: #000000\">Generated by Inure App Manager</h2>\r\n")
        // Add date and time
        stringBuilder.append("\t\t\t<h5 style=\"color: #000000\">${System.currentTimeMillis().toDate()}</h5>\r\n")
        stringBuilder.append("\t\t</tr>\r\n")
        stringBuilder.append("\t\t<tr>\r\n")

        // Add serial number
        stringBuilder.append("\t\t\t<th style=\"width:5%\">S. No.</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
            stringBuilder.append("\t\t\t<th>Name</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
            stringBuilder.append("\t\t\t<th>Package Name</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
            stringBuilder.append("\t\t\t<th>Version</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
            stringBuilder.append("\t\t\t<th>First Install Time</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
            stringBuilder.append("\t\t\t<th>Last Update Time</th>\r\n")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                stringBuilder.append("\t\t\t<th>Minimum SDK</th>\r\n")
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
            stringBuilder.append("\t\t\t<th>Target SDK</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
            stringBuilder.append("\t\t\t<th>Size</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
            stringBuilder.append("\t\t\t<th>Play Store Link</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
            stringBuilder.append("\t\t\t<th>F-Droid Link</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
            stringBuilder.append("\t\t\t<th>Amazon Store Link</th>\r\n")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
            stringBuilder.append("\t\t\t<th>Galaxy Store Link</th>\r\n")

        stringBuilder.append("\t\t</tr>\r\n")

        for (app in apps) {
            stringBuilder.append("\t\t<tr>\r\n")

            // Add serial number
            stringBuilder.append("\t\t\t<td>${apps.indexOf(app) + 1}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
                stringBuilder.append("\t\t\t<td>${app.applicationInfo.name}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
                stringBuilder.append("\t\t\t<td>${app.packageName}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
                stringBuilder.append("\t\t\t<td>${app.versionName}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
                stringBuilder.append("\t\t\t<td>${app.firstInstallTime.toDate()}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
                stringBuilder.append("\t\t\t<td>${app.lastUpdateTime.toDate()}</td>\r\n")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                    stringBuilder.append("\t\t\t<td>${app.applicationInfo.minSdkVersion}</td>\r\n")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
                stringBuilder.append("\t\t\t<td>${app.applicationInfo.targetSdkVersion}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
                stringBuilder.append("\t\t\t<td>${app.getSize()}</td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
                stringBuilder.append("\t\t\t<td><a href=\"https://play.google.com/store/apps/details?id=${app.packageName}\">Play Store</a></td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
                stringBuilder.append("\t\t\t<td><a href=\"https://f-droid.org/en/packages/${app.packageName}\">F-Droid</a></td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
                stringBuilder.append("\t\t\t<td><a href=\"https://www.amazon.com/gp/mas/dl/android?p=${app.packageName}\">Amazon Store</a></td>\r\n")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
                stringBuilder.append("\t\t\t<td><a href=\"https://galaxystore.samsung.com/detail/${app.packageName}\">Galaxy Store</a></td>\r\n")

            stringBuilder.append("\t\t</tr>\r\n")
        }

        stringBuilder.append("\t</table>\r\n")
        // Add padding
        stringBuilder.append("\t<br>")
        stringBuilder.append("</body>\r\n")
        stringBuilder.append("</html>\r\n")

        return stringBuilder
    }

    private fun generateMD(): StringBuilder {
        val stringBuilder = StringBuilder()

        stringBuilder.append("### Generated by Inure App Manager\r\n")
        stringBuilder.append("#### ${System.currentTimeMillis().toDate()}\r\n")

        stringBuilder.append("|")

        // Add serial number
        stringBuilder.append(" S. No. |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
            stringBuilder.append(" Name |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
            stringBuilder.append(" Package Name |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
            stringBuilder.append(" Version Name |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
            stringBuilder.append(" First Install Time |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
            stringBuilder.append(" Last Update Time |")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                stringBuilder.append(" Minimum SDK |")
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
            stringBuilder.append(" Target SDK |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
            stringBuilder.append(" Size |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
            stringBuilder.append(" Play Store Link |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
            stringBuilder.append(" F-Droid Link |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
            stringBuilder.append(" Amazon Store Link |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
            stringBuilder.append(" Galaxy Store Link |")

        stringBuilder.append("\r\n")

        stringBuilder.append("|")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
            stringBuilder.append(" --- |")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                stringBuilder.append(" --- |")
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
            stringBuilder.append(" --- |")

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
            stringBuilder.append(" --- |")

        stringBuilder.append("\r\n")

        for (app in apps) {
            stringBuilder.append("|")

            // Add serial number
            stringBuilder.append(" ${apps.indexOf(app) + 1} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME))
                stringBuilder.append(" ${app.applicationInfo.name} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME))
                stringBuilder.append(" ${app.packageName} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION))
                stringBuilder.append(" ${app.versionName} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE))
                stringBuilder.append(" ${app.firstInstallTime.toDate()} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE))
                stringBuilder.append(" ${app.lastUpdateTime.toDate()} |")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK))
                    stringBuilder.append(" ${app.applicationInfo.minSdkVersion} |")
            }

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK))
                stringBuilder.append(" ${app.applicationInfo.targetSdkVersion} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE))
                stringBuilder.append(" ${app.getSize()} |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE))
                stringBuilder.append(" [Play Store](https://play.google.com/store/apps/details?id=${app.packageName}) |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID))
                stringBuilder.append(" [F-Droid](https://f-droid.org/en/packages/${app.packageName}) |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.AMAZON_STORE))
                stringBuilder.append(" [Amazon Store](https://www.amazon.com/gp/mas/dl/android?p=${app.packageName}) |")

            if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.GALAXY_STORE))
                stringBuilder.append(" [Galaxy Store](https://galaxy.store/${app.packageName}) |")

            stringBuilder.append("\r\n")
        }

        stringBuilder.append("\r\n")
        stringBuilder.append("</br>\r\n")

        return stringBuilder
    }

    fun clearGeneratedAppsDataLiveData() {
        generatedAppDataPath.postValue(null)
    }

    private fun PackageInfo.getSize(): String {
        val appSize = applicationInfo.sourceDir.toFile().length()
        val splitSourceDirs = applicationInfo.splitSourceDirs?.getDirectorySize() ?: 0L

        return (appSize + splitSourceDirs).toSize()
    }
}