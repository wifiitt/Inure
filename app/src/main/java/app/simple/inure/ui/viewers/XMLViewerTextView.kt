package app.simple.inure.ui.viewers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.MimeConstants
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.fastscroll.FastScrollerBuilder
import app.simple.inure.decorations.padding.PaddingAwareNestedScrollView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.menus.CodeViewerMenu
import app.simple.inure.dialogs.miscellaneous.LargeString.Companion.showLargeStringDialog
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.XMLViewerViewModelFactory
import app.simple.inure.popups.viewers.PopupXmlViewer
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.text.EditTextHelper.findMatches
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.viewers.XMLViewerViewModel
import java.io.IOException

class XMLViewerTextView : KeyboardScopedFragment() {

    private lateinit var text: TypeFaceEditText
    private lateinit var icon: ImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var options: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var scrollView: PaddingAwareNestedScrollView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var searchContainer: ThemeLinearLayout
    private lateinit var searchInput: TypeFaceEditText
    private lateinit var previous: DynamicRippleImageButton
    private lateinit var next: DynamicRippleImageButton
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var count: TypeFaceTextView

    private lateinit var componentsViewModel: XMLViewerViewModel
    private lateinit var applicationInfoFactory: XMLViewerViewModelFactory

    private var matches: ArrayList<Pair<Int, Int>>? = null
    private var position = -1

    private val exportManifest = registerForActivityResult(CreateDocument(MimeConstants.xmlType)) { uri: Uri? ->
        if (uri == null) {
            // Back button pressed.
            return@registerForActivityResult
        }
        try {
            requireContext().contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream == null) throw IOException()
                outputStream.write(text.text.toString().toByteArray())
                outputStream.flush()
                Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), R.string.failed, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_xml_viewer, container, false)

        text = view.findViewById(R.id.text_viewer)
        name = view.findViewById(R.id.xml_name)
        icon = view.findViewById(R.id.xml_viewer_header_icon)
        progress = view.findViewById(R.id.xml_loader)
        options = view.findViewById(R.id.xml_viewer_options)
        settings = view.findViewById(R.id.xml_viewer_settings)
        scrollView = view.findViewById(R.id.xml_nested_scroll_view)
        search = view.findViewById(R.id.search)
        searchContainer = view.findViewById(R.id.search_container)
        searchInput = view.findViewById(R.id.input)
        previous = view.findViewById(R.id.previous)
        next = view.findViewById(R.id.next)
        clear = view.findViewById(R.id.clear)
        count = view.findViewById(R.id.count)

        name.text = requireArguments().getString("path_to_xml")!!

        applicationInfoFactory = XMLViewerViewModelFactory(packageInfo,
                                                           requireArguments().getBoolean(BundleConstants.isManifest),
                                                           requireArguments().getString(BundleConstants.pathToXml)!!,
                                                           requireArguments().getBoolean(BundleConstants.isRaw, false))

        componentsViewModel = ViewModelProvider(this, applicationInfoFactory)[XMLViewerViewModel::class.java]

        FastScrollerBuilder(scrollView).setupAesthetics().build()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments().getBoolean(BundleConstants.isManifest)) {
            icon.setImageResource(R.drawable.ic_android)
        } else {
            icon.setImageResource(R.drawable.ic_file_xml)
        }

        startPostponedEnterTransition()

        componentsViewModel.getSpanned().observe(viewLifecycleOwner) {
            if (it.length > FormattingPreferences.getLargeStringLimit()) {
                childFragmentManager.showLargeStringDialog(it.length) {
                    postDelayed {
                        text.setText(it)
                    }
                }
            } else {
                text.setText(it)
            }

            progress.gone()
            options.visible(true)
            settings.visible(true)
            search.visible(animate = true)
        }

        componentsViewModel.getError().observe(viewLifecycleOwner) {
            progress.gone()
            showError(it)
        }

        options.setOnClickListener {
            PopupXmlViewer(it).setOnPopupClickedListener(object : PopupXmlViewer.PopupXmlCallbacks {
                override fun onPopupItemClicked(source: String) {
                    when (source) {
                        getString(R.string.copy) -> {
                            val clipboard: ClipboardManager? = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText("xml", text.text.toString())
                            clipboard?.setPrimaryClip(clip)
                        }
                        getString(R.string.export) -> {
                            val name = with(name.text.toString()) {
                                substring(lastIndexOf("/") + 1, length)
                            }

                            val fileName: String = packageInfo.packageName + "_" + name
                            exportManifest.launch(fileName)
                        }
                    }
                }
            })
        }

        settings.setOnClickListener {
            CodeViewerMenu.newInstance()
                .show(childFragmentManager, "code_viewer_menu")
        }

        search.setOnClickListener {
            if (searchContainer.isVisible) {
                searchInput.hideInput()
                searchContainer.gone()
            } else {
                searchInput.showInput()
                searchContainer.visible(false)
            }
        }

        searchInput.doOnTextChanged { text, _, _, _ ->
            matches?.clear()
            matches = this.text.findMatches(text.toString())

            if (matches?.isNotEmpty() == true) position = 0
            jumpToMatch(position)
        }

        next.setOnClickListener {
            if (position < (matches?.size?.minus(1) ?: 0)) {
                jumpToMatch(++position)

            }
        }

        previous.setOnClickListener {
            if (position > 0) {
                jumpToMatch(--position)
            }
        }

        clear.setOnClickListener {
            if (searchInput.text?.isEmpty() == true) {
                searchInput.hideInput()
                searchContainer.gone()
            } else {
                searchInput.text?.clear()
                count.text = "0"
            }
        }
    }

    private fun jumpToMatch(position: Int) {
        matches?.let {
            if (it.isNotEmpty()) {
                if (position in 0 until it.size) {
                    count.text = buildString {
                        append(position.plus(1))
                        append("/")
                        append(it.size)
                    }

                    val layout = this.text.layout
                    scrollView.scrollTo(0, layout.getLineTop(layout.getLineForOffset(it[position].first)))
                    updateTextHighlight()
                }
            } else {
                count.text = "0"
                updateTextHighlight()
            }
        }
    }

    private fun updateTextHighlight() {
        kotlin.runCatching {
            matches?.let {
                val spans: Array<BackgroundColorSpan> = text.text?.getSpans(0, text.text!!.length, BackgroundColorSpan::class.java)!!

                for (span in spans) {
                    text.text?.removeSpan(span)
                }

                for (i in matches?.indices!!) {
                    if (i == position) {
                        text.text?.setSpan(BackgroundColorSpan(Misc.textHighlightFocused),
                                           it[i].first, it[i].second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    } else {
                        text.text?.setSpan(BackgroundColorSpan(Misc.textHighlightUnfocused),
                                           it[i].first, it[i].second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo?, isManifest: Boolean, pathToXml: String?, isRaw: Boolean = false): XMLViewerTextView {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putBoolean(BundleConstants.isManifest, isManifest)
            args.putBoolean(BundleConstants.isRaw, isRaw)
            args.putString(BundleConstants.pathToXml, pathToXml)
            val fragment = XMLViewerTextView()
            fragment.arguments = args
            return fragment
        }
    }
}
