package com.sampro.habesharadios.mediaservices

sealed class PlayerStatus(open val playerStatus: String?) {
    data class Other(override val playerStatus: String? = null) : PlayerStatus(playerStatus)
    data class Playing(override val playerStatus: String) : PlayerStatus(playerStatus)
    data class Loading(override val playerStatus: String) : PlayerStatus(playerStatus)
    data class Paused(override val playerStatus: String) : PlayerStatus(playerStatus)
    data class Cancelled(override val playerStatus: String? = null) : PlayerStatus(playerStatus)
    data class Ended(override val playerStatus: String) : PlayerStatus(playerStatus)
    data class Error(override val playerStatus: String, val exception: Exception?) : PlayerStatus(playerStatus)
}