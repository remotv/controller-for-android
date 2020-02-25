package tv.remo.android.controller.sdk.components

import android.content.Context
import android.os.Bundle
import android.util.Log
import org.btelman.controlsdk.enums.ComponentType
import org.btelman.controlsdk.models.Component
import org.btelman.controlsdk.models.ComponentEventObject
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence
import tv.remo.android.controller.sdk.RemoSettingsUtil
import tv.remo.android.controller.sdk.interfaces.RemoCommandSender
import tv.remo.android.controller.sdk.models.api.Channel
import java.util.*

/**
 * MQTT component
 *
 * Note: Do not instantiate in the activity! Must pass it to the ControlSDK Service
 */
class MqttComponent : Component() , RemoCommandSender, MqttCallback {
    private var channel: Channel? = null
    var client : MqttClient? = null

    override fun onInitializeComponent(applicationContext: Context, bundle: Bundle?) {
        super.onInitializeComponent(applicationContext, bundle)
        RemoSettingsUtil.with(applicationContext){

        }
    }

    override fun disableInternal() {
        disconnectMQTT()
        client?.close()
    }

    override fun enableInternal() {
        client = MqttClient("ssl://url:8883", UUID.randomUUID().toString(),
            MqttDefaultFilePersistence(context!!.filesDir.absolutePath)
        )
    }

    override fun getType(): ComponentType {
        return ComponentType.CUSTOM
    }

    override fun handleExternalMessage(message: ComponentEventObject) : Boolean{
        if(message.source is RemoCommandSender){
            when(message.data){
                is Channel -> {
                    resetMQTT(message.data as Channel)
                }
                is String -> {
                    //do something!
                }
            }
        }
        return false
    }

    private fun resetMQTT(channel: Channel?) {
        disconnectMQTT()
        channel?.let {
            connectMQTT(channel)
        }
    }

    private fun connectMQTT(channel: Channel){
        client?.let {
            val connectOptions = MqttConnectOptions().also { options->
                options.setWill(TopicBuilder.buildTopic(JOIN_TOPIC, channel),
                    "null-from-will".toByteArray(), 1, false)
                options.isCleanSession = true
                options.userName = "user"
                options.password = "pass".toCharArray()
            }
            it.setCallback(this)
            it.connect(connectOptions)
            this.channel = channel
            TopicBuilder(it, JOIN_TOPIC, channel, data = Date().toString()).send() //register channel and persist
            Log.d("AAA", "Connected")
        }
    }

    private fun disconnectMQTT(){
        client?.takeIf { channel != null }?.let {
            TopicBuilder(it, JOIN_TOPIC, channel!!, data = null).send() //unregister channel
            it.disconnect()
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {

    }

    override fun connectionLost(cause: Throwable?) {

    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {

    }

    data class TopicBuilder(
        val client : IMqttClient,
        val topic : String,
        val channel: Channel,
        val qos : Int = 1,
        var data : Any? = null
    ){
        fun send(persist : Boolean = false){
            val dataToSend = parse(data)
            client.publish(buildTopic(topic, channel), dataToSend, qos, persist)
        }

        companion object{
            fun buildTopic(topic : String, channel: Channel) : String{
                return String.format(topic, channel.id)
            }

            fun parse(data : Any?) : ByteArray{
                return when(data){
                    is String -> {
                       data.toByteArray()
                    }
                    else -> {
                        "null".toByteArray()
                    }
                }
            }
        }
    }

    companion object{
        private const val CHANNEL = "%1\$s"
        private const val ROOT_TOPIC = "remo"
        private const val JOIN_TOPIC = "$ROOT_TOPIC/device/$CHANNEL"
        private const val AVAILABLE_TOPICS_TOPIC = "$ROOT_TOPIC/$CHANNEL/topics" //acts like discovery, and should be retained
        private const val SEND_BUTTON_COMMAND_TOPIC = "$ROOT_TOPIC/$CHANNEL/site/buttonCommand"
        private const val SEND_CHAT_TOPIC = "$ROOT_TOPIC/$CHANNEL/site/chat"
        private const val BATTERY_ROBOT_TOPIC = "$ROOT_TOPIC/$CHANNEL/robot/battery"
        private const val BATTERY_PHONE_TOPIC = "$ROOT_TOPIC/$CHANNEL/phone/battery"
    }
}