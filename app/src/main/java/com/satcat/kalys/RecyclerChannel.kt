package com.satcat.kalys

import com.satcat.kalys.Models.ChatChannel

interface RecyclerChannel {
    val header : Boolean
    val sectionPosition: Int

}

class RecyclerChannelChild(channel: ChatChannel, position : Int) : RecyclerChannel {
    override val header = false
    override val sectionPosition = position
    val channel = channel

}

class RecyclerChannelHeader(headerText: String, position : Int) : RecyclerChannel {
    override val header = true
    override val sectionPosition = position
    val headerText = headerText

}


