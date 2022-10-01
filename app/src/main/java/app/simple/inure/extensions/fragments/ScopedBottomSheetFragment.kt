package app.simple.inure.extensions.fragments

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import app.simple.inure.R
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.util.ViewUtils
import com.google.android.material.R.id.design_bottom_sheet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class ScopedBottomSheetFragment : BottomSheetDialogFragment(),
                                           SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * [ScopedBottomSheetFragment]'s own [PackageInfo] instance, needs
     * to be initialized before use
     *
     * @throws UninitializedPropertyAccessException
     */
    lateinit var packageInfo: PackageInfo

    open val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.attributes?.windowAnimations = R.style.BottomDialogAnimation

        if (BehaviourPreferences.isDimmingOn()) {
            dialog?.window?.setDimAmount(ViewUtils.getDimValue(requireContext()))
        } else {
            dialog?.window?.setDimAmount(0f)
        }

        dialog?.setOnShowListener { dialog ->
            /**
             * In a previous life I used this method to get handles to the positive and negative buttons
             * of a dialog in order to change their Typeface. Good ol' days.
             */
            val sheetDialog = dialog as BottomSheetDialog

            /**
             * This is gotten directly from the source of BottomSheetDialog
             * in the wrapInBottomSheet() method
             */
            val bottomSheet = sheetDialog.findViewById<View>(design_bottom_sheet) as FrameLayout

            /**
             *  Right here!
             *  Make sure the dialog pops up being fully expanded
             */
            BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED

            /**
             * Also make sure the dialog doesn't half close when we don't want
             * it to be, so we close them
             */
            BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Called when any preferences is changed using [getSharedPreferences]
     *
     * Override this to get any preferences change events inside
     * the fragment
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    protected fun showError(error: String) {
        try {
            val e = Error.newInstance(error)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            })
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    protected fun showError(error: Throwable) {
        try {
            val e = Error.newInstance(error.stackTraceToString())
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            })
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    /**
     * Return the {@link Application} this fragment is currently associated with.
     */
    protected fun requireApplication(): Application {
        return requireActivity().application
    }

    protected fun openSettings() {
        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
    }

    /**
     * Open fragment using slide animation
     *
     * If the fragment does not need to be pushed into backstack
     * leave the [tag] unattended
     *
     * @param fragment [Fragment]
     * @param tag back stack tag for fragment
     */
    protected fun openFragmentSlide(fragment: ScopedFragment, tag: String? = null) {
        (parentFragment as ScopedFragment).clearReEnterTransition()
        (parentFragment as ScopedFragment).clearExitTransition()

        requireActivity().supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            .replace(R.id.app_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }
}