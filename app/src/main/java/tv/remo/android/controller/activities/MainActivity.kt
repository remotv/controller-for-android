package tv.remo.android.controller.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import org.btelman.controlsdk.enums.Operation
import org.btelman.controlsdk.interfaces.ControlSdkApi
import org.btelman.controlsdk.models.ComponentHolder
import org.btelman.controlsdk.services.ControlSDKServiceConnection
import org.btelman.controlsdk.services.observeAutoCreate
import tv.remo.android.controller.R
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.models.api.Message
import tv.remo.android.controller.sdk.utils.ChatUtil
import tv.remo.android.controller.sdk.utils.ComponentBuilderUtil

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var recording = false
    private val arrayList = ArrayList<ComponentHolder<*>>()
    private var controlSDKServiceApi: ControlSdkApi? = null
    private lateinit var handler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        setContentView(R.layout.activity_main)
        setupControlSDK()
        setupUI()
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.powerButton -> powerCycle()
            R.id.settingsButton -> launchSettings()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && recording) hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        controlSDKServiceApi?.disconnectFromService()
    }

    private fun setupControlSDK() {
        controlSDKServiceApi = ControlSDKServiceConnection.getNewInstance(this)
        controlSDKServiceApi?.getServiceStateObserver()?.observeAutoCreate(this, operationObserver)
        controlSDKServiceApi?.getServiceBoundObserver()?.observeAutoCreate(this){ connected ->
            powerButton.isEnabled = connected == Operation.OK
        }
        controlSDKServiceApi?.connectToService()
        createComponentHolders()
    }

    private fun setupUI() {
        remoChatView.setOnTouchListener { _, _ ->
            handleSleepLayoutTouch()
            return@setOnTouchListener false
        }
        settingsButton.setOnClickListener(this)
        powerButton?.setOnClickListener(this)
    }

    val operationObserver : (Operation) -> Unit = { serviceStatus ->
        powerButton?.let {
            powerButton.setTextColor(parseColorForOperation(serviceStatus))
            val isLoading = serviceStatus == Operation.LOADING
            powerButton.isEnabled = !isLoading
            if(isLoading) return@let //processing command. Disable button
            recording = serviceStatus == Operation.OK
            if(recording) {
                handleSleepLayoutTouch()
            }
            else{
                remoChatView.keepScreenOn = false //go ahead and remove the flag
            }
        }
    }

    private fun handleSleepLayoutTouch(): Boolean {
        showSystemUI()
        RemoSettingsUtil.with(this){
            if(it.keepScreenOn.getPref()){
                remoChatView.keepScreenOn = true //Could be attached to any view, but this is fine
            }
            if(it.hideScreenControls.getPref()){
                startSleepDelayed()
            }
        }
        return false
    }

    private fun startSleepDelayed() {
        buttonGroupMainActivity.visibility = View.VISIBLE
        handler.removeCallbacks(hideScreenRunnable)
        handler.postDelayed(hideScreenRunnable, 10000) //10 second delay
    }

    private val hideScreenRunnable = Runnable {
        if (recording){
            buttonGroupMainActivity.visibility = View.GONE
            hideSystemUI()
        }
    }

    private fun UnitTestRunChat(){
        val json = "{\"message\":\"test\",\"sender\":\"ReconDelta090\",\"sender_id\":\"user-6a9591cc-f3d3-4e47-a208-e749679a899a\",\"chat_id\":\"chat-8a05f730-c663-434d-9f24-6d8c24453c5f\",\"server_id\":\"serv-46437781-4a9b-4531-9db1-74bc2f818b58\",\"id\":\"mesg-ec54bf9a-23d4-4fa6-b6f1-9a5c8e3e2440\",\"time_stamp\":1574296097994,\"broadcast\":\"\",\"channel_id\":\"chan-7a304995-cba0-463c-81a6-ffeffc059058\",\"display_message\":true,\"badges\":[\"owner\"],\"type\":\"\"}"
        Gson().fromJson(json, Message::class.java).also { rawMessage ->
            ChatUtil.broadcastChatMessage(this, rawMessage)
            ChatUtil.broadcastChatMessage(this, rawMessage) //should just be ignored
        }
    }

    private fun launchSettings() {
        if(controlSDKServiceApi?.getServiceStateObserver()?.value == Operation.OK){
            powerCycle()
        }
        startActivity(SettingsActivity.getIntent(this))
        finish()
    }

    private fun powerCycle() {
        when(controlSDKServiceApi?.getServiceStateObserver()?.value){
            Operation.NOT_OK -> {
                arrayList.forEach {
                    controlSDKServiceApi?.attachToLifecycle(it)
                }
                controlSDKServiceApi?.enable()
            }
            Operation.LOADING -> {} //do nothing
            Operation.OK -> {
                arrayList.forEach {
                    controlSDKServiceApi?.detachFromLifecycle(it)
                }
                controlSDKServiceApi?.disable()
            }
            null -> powerButton.setTextColor(parseColorForOperation(null))
        }
    }

    private fun createComponentHolders() {
        RemoSettingsUtil.with(this){ settings ->
            arrayList.add(ComponentBuilderUtil.createSocketComponent(settings))
            arrayList.addAll(ComponentBuilderUtil.createTTSComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createStreamingComponents(settings))
            arrayList.addAll(ComponentBuilderUtil.createHardwareComponents(settings))
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, MainActivity::class.java)
        }

        fun parseColorForOperation(state : Operation?) : Int{
            val color : Int = when(state){
                Operation.OK -> Color.GREEN
                Operation.NOT_OK -> Color.RED
                Operation.LOADING -> Color.YELLOW
                null -> Color.CYAN
                else -> Color.BLACK
            }
            return color
        }
    }
}

/**
 *
 * 2019-11-20 18:26:42.268 7347-22008/tv.remo.android.controller D/SOCKET: {"e":"ROBOT_SERVER_UPDATED"}
2019-11-20 18:27:52.924 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ROBOT_VALIDATED","d":{"id":"rbot-154b0f8a-8546-4aac-b1d0-720ebc4079b9","host":"serv-46437781-4a9b-4531-9db1-74bc2f818b58"}}
2019-11-20 18:27:53.038 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ACTIVE_USERS_UPDATED","d":[{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}},{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}}]}
2019-11-20 18:27:53.149 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"SEND_ROBOT_SERVER_INFO","d":{"channels":[{"host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"chan-9e44738e-6915-4d35-a076-3fa8c6476378","name":"General","chat":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","controls":"cont-681be524-9576-4336-81b9-94b29cf7d2ab","display":"","created":"1573348795600","status":{"test_value":true},"settings":{"access":"@everyone","public":true},"robot":null},{"host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"chan-efea332a-019e-4f2c-a6d3-e18a879ed5de","name":"BeepBoop","chat":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","controls":"cont-b1663702-236a-4c62-8d1b-1583904c9ce8","display":"","created":"1573348804432","status":{"test_value":true},"settings":{"access":"@everyone","public":true},"robot":null},{"host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"chan-7a304995-cba0-463c-81a6-ffeffc059058","name":"DevZone","chat":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","controls":"cont-53e1b32f-f91d-4c0a-8778-4838aca76d6f","display":"","created":"1573866493437","status":{"test_value":true},"settings":{"access":"@everyone","public":true},"robot":null},{"host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03","name":"RVR1","chat":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","controls":"cont-d4af98dc-97b0-4729-9dbd-3ab3684511aa","display":"","created":"1573348810093","status":{"test_value":true},"settings":{"access":"@everyone","public":true},"robot":null},{"host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"chan-40f1c6d5-930d-4ab9-857d-f3c72931ddf7","name":"MirrorModBot","chat":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","controls":"cont-7852499a-9921-4f62-b9c2-c575dc7bdd9b","display":"","created":"1573348827461","status":{"test_value":true},"settings":{"access":"@everyone","public":true},"robot":null},{"host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"chan-ebea9a41-60a8-4126-aa32-bda0d63f6c51","name":"Sketchy","chat":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","controls":"cont-6fa5deea-9169-4c31-b525-9414b7fc8967","display":"","created":"1573348839169","status":{"test_value":true},"settings":{"access":"@everyone","public":true},"robot":null}],"users":[{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}},{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}}],"invites":[{"id":"join-3850bf2d-13e1-417b-8581-ea2362e9ffb1","created_by":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","created":"1571260990045","expires":"","status":"active","alias":null,"is_default":null},{"id":"join-375d722d-4b2c-4b5f-b703-803eaded5b95","created_by":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","created":"1574287842194","expires":"","status":"active","alias":"dz8p11s","is_default":false}]}}
2019-11-20 18:27:53.467 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"SEND_CHAT","d":{"name":"General","id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","host_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","messages":[{"message":"better","sender":"Admanta","sender_id":"user-6437a137-8fc8-468a-b5b8-a39d5fffa124","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-9bf30a3b-d951-4d64-a37e-7a540a2d0ac2","time_stamp":"1574290400096","broadcast":"","display_message":true,"badges":[],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"thank you","sender":"Admanta","sender_id":"user-6437a137-8fc8-468a-b5b8-a39d5fffa124","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-9306c612-9859-41da-96f9-c290a52de333","time_stamp":"1574290403949","broadcast":"","display_message":true,"badges":[],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"I need to make turning adjustable separate from forward speed still","sender":"ReconDelta090","sender_id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-8ede21d2-1a1b-4b4e-9111-c008181d1321","time_stamp":"1574290424108","broadcast":"","display_message":true,"badges":["owner"],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"don't smash into things please","sender":"ReconDelta090","sender_id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-0f82f02c-d517-4d04-9ff1-e1e1da62062a","time_stamp":"1574290772608","broadcast":"","display_message":true,"badges":["owner"],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"sorryt","sender":"stickbot","sender_id":"user-fa67779f-1a91-4a46-9e2c-9ceea87aab84","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-c95fb6e3-f5d6-4290-900a-dba40fee40db","time_stamp":"1574290781932","broadcast":"","display_message":true,"badges":[],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"sideway time wheeee","sender":"stickbot","sender_id":"user-fa67779f-1a91-4a46-9e2c-9ceea87aab84","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-46ec8efb-f211-4c8d-a2e5-5a589c2ddff8","time_stamp":"1574290787786","broadcast":"","display_message":true,"badges":[],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"/bitrate 512","sender":"ReconDelta090","sender_id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-aa7483a5-e1d5-47ae-b485-647b2fa8e4ef","time_stamp":"1574290900271","broadcast":"","display_message":true,"badges":["owner"],"type":"site-command","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"Reloading streaming...","sender":"GalaxyS4","sender_id":"rbot-7f9daffe-cc71-49db-a224-431911f71861","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-e9aa472e-578b-46ac-b6a6-0ce0cb879ece","time_stamp":"1574290900395","broadcast":"","display_message":true,"badges":["robot"],"type":"robot","channel_id":""},{"message":"sorry bro","sender":"stickbot","sender_id":"user-fa67779f-1a91-4a46-9e2c-9ceea87aab84","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-47e59db6-0c16-4893-955d-02b752c72ed2","time_stamp":"1574290911452","broadcast":"","display_message":true,"badges":[],"type":"","channel_id":"chan-0a46e41f-2eaf-461e-813b-b4e08fbc6e03"},{"message":"Robot connected. Commands cleared","sender":"LGK4","sender_id":"rbot-85c1c985-abbc-4238-b853-6f55aa8425ef","chat_id":"chat-8a05f730-
2019-11-20 18:27:57.400 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ROBOT_SERVER_UPDATED"}
2019-11-20 18:28:03.239 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ACTIVE_USERS_UPDATED","d":[{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}},{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}}]}
2019-11-20 18:28:12.422 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ROBOT_SERVER_UPDATED"}
2019-11-20 18:28:17.527 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"MESSAGE_RECEIVED","d":{"message":"test","sender":"ReconDelta090","sender_id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","chat_id":"chat-8a05f730-c663-434d-9f24-6d8c24453c5f","server_id":"serv-46437781-4a9b-4531-9db1-74bc2f818b58","id":"mesg-ec54bf9a-23d4-4fa6-b6f1-9a5c8e3e2440","time_stamp":1574296097994,"broadcast":"","channel_id":"chan-7a304995-cba0-463c-81a6-ffeffc059058","display_message":true,"badges":["owner"],"type":""}}
2019-11-20 18:28:18.255 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ACTIVE_USERS_UPDATED","d":[{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}},{"username":"ReconDelta090","id":"user-6a9591cc-f3d3-4e47-a208-e749679a899a","created":"1571259587676","type":null,"status":{"timeout":false}}]}
2019-11-20 18:28:27.448 7347-22086/tv.remo.android.controller D/SOCKET: {"e":"ROBOT_SERVER_UPDATED"}
 */
