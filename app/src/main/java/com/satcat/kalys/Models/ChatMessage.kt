package com.satcat.kalys.Models

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.satcat.kalys.KalysApplication
import com.satcat.kalys.Managers.ImageStorageManager
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.kotlin.where
import org.json.JSONObject
import java.util.*

open class ChatMessage(

    var ID:String = UUID.randomUUID().toString(),
    var CreatedDate: Date = Date(),
    var Text: String = "",
    var Sender: User? = null,
    var ImagePath: String = "",
    @LinkingObjects("Messages") val Channel: RealmResults<ChatChannel>? = null,
    var ChannelID: String = "",
    var Networked: Boolean = true



):RealmObject() {

    fun giveJson(): JSONObject {

        val imageString = if(ImageStorageManager.shared.convertBitmapToString(getImage()) != ImageStorageManager.shared.convertBitmapToString(BitmapFactory.decodeResource(
                KalysApplication.getContext().resources, R.drawable.ic_dialog_email))) ImageStorageManager.shared.convertBitmapToString(getImage()) else ""

        val messageJson = JSONObject()
        messageJson.put("ID", ID)
        messageJson.put("Text", Text)
        messageJson.put("Sender", Sender?.giveJson())
        messageJson.put("ChannelID", ChannelID)
        messageJson.put("Image", imageString) // get image from imagepath

        return messageJson
    }

    fun setFromJSON(obj: JSONObject) {
        this.ID = obj.getString("ID")
        this.Text = obj.getString("Text")
        this.ChannelID = obj.getString("ChannelID")
        //attachChannel()

    }

    fun getImage(): Bitmap {
        return ImageStorageManager.shared.getImage(this.ImagePath)!!
    }

//    fun attachChannel() {
//
//        val realm:Realm = Realm.getDefaultInstance()
//
//        val channel = realm.where<ChatChannel>().equalTo("ID", this.ChannelID).findFirst()
//
//        if (channel != null) {
//            channel.Messages.add(this) //already in realm transaction
//        }
//
//    }

}