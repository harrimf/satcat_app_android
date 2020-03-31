package com.satcat.kalys.Models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class ChatChannel(

    var ID:String = UUID.randomUUID().toString(),
    var Title:String = "",
    var Notice:String = "",
    var NoticeSender: User? = null,
    var IsMain:Boolean = false,
    var IsMember:Boolean = true,
    var IsPrivate:Boolean = false,
    var CreatedDate:Date? = null, //could be Date()
    var NotificationCount:Int = 0,
    var ReceivedNotice: Boolean = false,
    var Messages: RealmList<ChatMessage> = RealmList(),
    var Members: RealmList<User> = RealmList(),
    @LinkingObjects("Channels")
    val Group: RealmResults<ChatGroup>? = null,
    var GroupID:String = "",
    var Networked:Boolean = true,
    var Updated:Boolean = true,
    var Removable:Boolean = true,
    var OfflineMembers: RealmList<User> = RealmList()

):RealmObject() {

    fun giveJson(): JSONObject {

        val channelJson = JSONObject()
        channelJson.put("ID", ID)
        channelJson.put("Title", Title)
        channelJson.put("Notice", Notice)
        channelJson.put("IsMain", IsMain)
        channelJson.put("IsPrivate", IsPrivate)
        channelJson.put("NoticeSender", NoticeSender?.giveJson())
        channelJson.put("Members", this.convertMembersToJSON(Members))
        channelJson.put("GroupID", GroupID)


        return channelJson
    }

    fun setFromJSON(obj: JSONObject) {
        this.ID = obj.getString("ID")
        this.Title = obj.getString("Title")
        this.Notice = obj.getString("Notice")
        this.IsMain = obj.getBoolean("IsMain")
        this.IsPrivate = obj.getBoolean("IsPrivate")
        this.GroupID = obj.getString("GroupID")

    }

    private fun convertMembersToJSON(members: RealmList<User>): JSONArray {

        val jsonMembers = JSONArray()

        for (member in members ) {
            jsonMembers.put(member.giveJson())
        }

        return jsonMembers

    }

}
