package app.simple.inure.activities.association

import android.os.Bundle
import app.simple.inure.R
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.association.Text
import app.simple.inure.util.NullSafety.isNull

class TextViewerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, Text.newInstance())
                .commit()
        }
    }
}
