package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.BuildConfig
import app.simple.inure.extensions.viewmodels.RootServiceViewModel
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.nio.FileSystemManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedPreferencesViewModel(packageInfo: PackageInfo, application: Application) : RootServiceViewModel(application) {

    private val path = packageInfo.applicationInfo.dataDir + "/shared_prefs/"

    private val sharedPrefsFiles: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    fun getSharedPrefs(): LiveData<ArrayList<String>> {
        return sharedPrefsFiles
    }

    private fun loadSharedPrefsFiles(fileSystemManager: FileSystemManager?) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                kotlin.runCatching {
                    Shell.enableVerboseLogging = BuildConfig.DEBUG
                    Shell.setDefaultBuilder(Shell.Builder.create()
                                                .setContext(applicationContext())
                                                .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                                .setTimeout(10))
                }

                with(fileSystemManager?.getFile(path)) {
                    val list = this?.list()?.toList() as ArrayList<String>?
                    sharedPrefsFiles.postValue(list!!)
                }
            }.getOrElse {
                it.printStackTrace()
                error.postValue(it)
            }
        }
    }

    override fun runRootProcess(fileSystemManager: FileSystemManager?) {
        loadSharedPrefsFiles(fileSystemManager)
    }
}