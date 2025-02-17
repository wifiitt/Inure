package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.text.Spannable
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.APKParser.getApkArchitecture
import app.simple.inure.apk.parsers.APKParser.getDexData
import app.simple.inure.apk.parsers.APKParser.getGlEsVersion
import app.simple.inure.apk.parsers.APKParser.getMinSDK
import app.simple.inure.apk.parsers.APKParser.getNativeLibraries
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.bean.DexClass
import java.io.File
import java.text.NumberFormat

class InstallerInformationViewModel(application: Application, private val file: File) : WrappedViewModel(application) {

    private var packageInfo: PackageInfo? = null

    private val information: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<@StringRes Int, Spannable>>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                loadInformation()
            }
        }
    }

    fun getInformation(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return information
    }

    private fun loadInformation() {
        kotlin.runCatching {
            packageInfo = packageManager.getPackageArchiveInfo(file)

            if (packageManager.isPackageInstalled(packageName = packageInfo!!.packageName)) {
                packageInfo = packageManager.getPackageInfo(packageInfo!!.packageName)
            }
        }.onFailure {
            postError(it)
            return
        }

        val list = arrayListOf<Pair<Int, Spannable>>()

        list.add(getPackageName())
        list.add(getVersion())
        list.add(getVersionCode())

        if (packageInfo.isNotNull()) {
            list.add(getUID())
            list.add(getInstallDate())
            list.add(getUpdateDate())
            list.add(getInstallerName())
        }

        list.add(getMinSDK())
        list.add(getTargetSDK())
        list.add(getGlesVersion())
        list.add(getArchitecture())
        list.add(getNativeLibraries())
        list.add(getMethodCount())
        list.add(getFeatures())
        //  list.add(getApkPath())

        information.postValue(list)
    }

    private fun getPackageName(): Pair<Int, Spannable> {
        return Pair(R.string.package_name,
                    packageInfo!!.packageName.applySecondaryTextColor())
    }

    private fun getVersion(): Pair<Int, Spannable> {
        return Pair(R.string.version,
                    packageInfo!!.versionName?.applySecondaryTextColor() ?: "N/A".applySecondaryTextColor())
    }

    private fun getVersionCode(): Pair<Int, Spannable> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Pair(R.string.version_code,
                 packageInfo!!.longVersionCode.toString().applySecondaryTextColor())
        } else {
            @Suppress("DEPRECATION")
            Pair(R.string.version_code,
                 packageInfo!!.versionCode.toString().applySecondaryTextColor())
        }
    }

    private fun getApkPath(): Pair<Int, Spannable> {
        return Pair(R.string.apk_base_package,
                    file.path.applySecondaryTextColor())
    }

    private fun getGlesVersion(): Pair<Int, Spannable> {
        val glesVersion = kotlin.runCatching {
            file.getGlEsVersion()
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.gles_version,
                    glesVersion.applySecondaryTextColor())
    }

    private fun getArchitecture(): Pair<Int, Spannable> {
        return Pair(R.string.architecture,
                    file.getApkArchitecture(context).toString().applyAccentColor())
    }

    private fun getNativeLibraries(): Pair<Int, Spannable> {
        return Pair(R.string.native_libraries,
                    file.getNativeLibraries(context).toString().applySecondaryTextColor())
    }

    private fun getUID(): Pair<Int, Spannable> {
        return Pair(R.string.uid,
                    packageInfo!!.applicationInfo.uid.toString().applySecondaryTextColor())
    }

    private fun getInstallDate(): Pair<Int, Spannable> {
        return Pair(R.string.install_date,
                    packageInfo!!.getApplicationInstallTime(context, FormattingPreferences.getDateFormat()).applyAccentColor())
    }

    private fun getUpdateDate(): Pair<Int, Spannable> {
        return Pair(R.string.update_date,
                    packageInfo!!.getApplicationLastUpdateTime(context, FormattingPreferences.getDateFormat()).applyAccentColor())
    }

    private fun getMinSDK(): Pair<Int, Spannable> {
        val minSdk = kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                "${packageInfo!!.applicationInfo.minSdkVersion}," +
                        " ${SDKHelper.getSdkTitle(packageInfo!!.applicationInfo!!.minSdkVersion)}"
            } else {
                file.getMinSDK()
            }
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.minimum_sdk,
                    minSdk.applyAccentColor())
    }

    private fun getTargetSDK(): Pair<Int, Spannable> {
        val targetSdk = kotlin.runCatching {
            "${packageInfo!!.applicationInfo.targetSdkVersion}, " +
                    SDKHelper.getSdkTitle(packageInfo!!.applicationInfo.targetSdkVersion)
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.target_sdk,
                    targetSdk.applyAccentColor())
    }

    private fun getMethodCount(): Pair<Int, Spannable> {
        val method = kotlin.runCatching {
            var count = 0
            val dexClasses: Array<DexClass> = file.getDexData()

            for (i in dexClasses) {
                count += i.javaClass.methods.size
            }

            if (dexClasses.size > 1) {
                String.format(getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(count))
            } else {
                String.format(getString(R.string.single_dex), NumberFormat.getNumberInstance().format(count))
            }
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.method_count,
                    method.applySecondaryTextColor())
    }

    private fun getInstallerName(): Pair<Int, Spannable> {
        @Suppress("deprecation")
        val name = kotlin.runCatching {
            val p0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageInfo!!.packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageInfo!!.packageName)
            }

            PackageUtils.getApplicationName(context, p0!!)
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.installer,
                    name!!.applySecondaryTextColor())
    }

    private fun getFeatures(): Pair<Int, Spannable> {
        val features = StringBuilder()

        try {
            for (feature in packageInfo!!.reqFeatures) {
                if (features.isEmpty()) {
                    features.append(feature.name)
                } else {
                    features.append("\n")
                    features.append(feature.name)
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            features.append(getString(R.string.not_available))
        } finally {
            if (features.isEmpty()) {
                features.append(getString(R.string.not_available))
            }
        }

        return Pair(R.string.uses_feature,
                    features.toString().applySecondaryTextColor())
    }
}