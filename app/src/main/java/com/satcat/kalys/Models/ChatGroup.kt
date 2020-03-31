package com.satcat.kalys.Models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import io.realm.RealmList
import io.realm.RealmObject
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class ChatGroup(

    var ID:String = UUID.randomUUID().toString(),
    var Title:String = "",
    var Notice:String = "",
    var IsPrivate:Boolean = false,
    var ImagePath: String = "",
    var Channels: RealmList<ChatChannel> = RealmList(),
    var Networked:Boolean = true,
    var Updated:Boolean = true,
    var Removable:Boolean = true


    ):RealmObject() {

    fun giveJson(): JSONObject {

        val stringImage = "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"


        val groupJson = JSONObject()
        groupJson.put("ID", ID)
        groupJson.put("Title", Title)
        groupJson.put("Notice", Notice)
        groupJson.put("IsPrivate", IsPrivate)
        groupJson.put("Channels", this.convertChannelsToJSON(Channels))
        groupJson.put("Image", stringImage)

        return groupJson
    }

    fun setFromJSON(obj: JSONObject) {
        this.ID = obj.getString("ID")
        this.Title = obj.getString("Title")
        this.Notice = obj.getString("Notice")
        this.IsPrivate = obj.getBoolean("IsPrivate")

    }

    fun getImage(): Bitmap {
        return BitmapFactory.decodeFile(ImagePath) // needs changing to actual image
    }


    private fun convertChannelsToJSON(channels: RealmList<ChatChannel>): JSONArray {

        val jsonChannels = JSONArray()

        for (member in channels) {
            jsonChannels.put(member.giveJson())
        }

        return jsonChannels

    }

}