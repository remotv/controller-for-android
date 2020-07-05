package tv.remo.android.controller.activities

import androidx.appcompat.app.AppCompatActivity
import org.btelman.logutil.kotlin.LogLevel
import org.btelman.logutil.kotlin.LogUtil
import org.btelman.logutil.kotlin.LogUtilInstance

abstract class BaseActivity : AppCompatActivity() {
    protected val log = LogUtil(javaClass.simpleName, LogUtilInstance("AAAA", LogLevel.DEBUG))
}