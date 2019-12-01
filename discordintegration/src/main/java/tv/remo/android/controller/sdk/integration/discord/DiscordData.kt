package tv.remo.android.controller.sdk.integration.discord

import android.os.Bundle

/**
 * Container for information on the discord bot
 */
data class DiscordData (
    val token : String,
    val permissions : Int,
    val channel : String? = null
){

    fun toBundle(bundle: Bundle = Bundle()) : Bundle{
        return bundle.apply {
            putString(TOKEN_KEY, token)
            putInt(PERMISSIONS_KEY, permissions)
            putString(CHANNEL_KEY, channel)
        }
    }

    companion object{
        private const val TOKEN_KEY = "discordToken"
        private const val PERMISSIONS_KEY = "discordPerms"
        private const val CHANNEL_KEY = "discordChan"

        fun fromBundle(bundle : Bundle?) : DiscordData?{
            bundle?.apply {
                val token = getString(TOKEN_KEY) ?: return null
                val perms = getInt(PERMISSIONS_KEY).takeIf { it != 0 } ?: return null
                val channel = getString(CHANNEL_KEY)
                return DiscordData(token, perms, channel)
            }
            return null
        }
    }

}