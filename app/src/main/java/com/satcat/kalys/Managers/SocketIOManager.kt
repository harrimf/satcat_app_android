package com.satcat.kalys.Managers

import android.util.Base64
import android.util.Log
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.Ack
import com.github.nkzawa.socketio.client.Manager
import com.github.nkzawa.socketio.client.Socket
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import com.satcat.kalys.Models.ChatMessage
import com.satcat.kalys.Models.User
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.text.DateFormat.getDateTimeInstance
import java.text.SimpleDateFormat


open class SocketIOManager {


    companion object {
        @JvmStatic
        val shared = SocketIOManager()
    }

    var socket: Socket
    var manager : Manager


    open class RegisterUserEvent(var json : JSONObject)

    open class LoginUserEvent(var json : JSONObject)

    open class ReceiveChannelEvent()

    open class ReceiveMessageEvent()

    open class ReceiveUserEvent()

    open class ReceiveGroupEvent()

    open class ReceiveSearchGroupsEvent(var json: JSONObject)

    open class ReceiveSearchUsersEvent(var json: JSONObject)

    open class ReceiveUsernameCheckEvent(var json: JSONObject)

    open class ReceiveUserPasswordUpdateEvent(var json: JSONObject)




    init {


        val opts = Manager.Options()
        opts.reconnection = true


        manager = Manager(URI.create("http://192.168.2.9:1104"), opts)

        socket = manager.socket("/") //default namespace

        socket.on(Socket.EVENT_CONNECT) { args ->
            Log.d("test4", "Succesfully connected ${args}")
            shared.silentLogin()

        }

        socket.on(Socket.EVENT_RECONNECT) { args ->
            Log.d("test4", "Succesfully Reconnected ${args}")
            shared.silentLogin()

        }

        socket.on("message", Emitter.Listener { args ->

            val realm: Realm = Realm.getDefaultInstance()

            val obj = args[0] as JSONObject

            val channel = realm.where<ChatChannel>().equalTo("ID", obj.getString("ChannelID")).findFirst()

            val sender = realm.where<User>().equalTo("ID", obj.getString("SenderID")).findFirst()


            realm.executeTransaction {
                var message = realm.createObject<ChatMessage>()
                message.setFromJSON(obj)

                //Update Channel
                channel!!.NotificationCount += 1
                channel!!.Messages.add(message)

                //Attach User
                message.Sender = sender

            }

            if(!obj.isNull("Image")) {
                val imageName = obj.getString("ID") + "_main.png"
                val imageData = obj.getString("Image")

                this.saveImageFromString(imageData, imageName)

                val addedMessage = realm.where<ChatMessage>().equalTo("ID", obj.getString("ID")).findFirst()

                realm.executeTransaction {
                    addedMessage?.ImagePath = imageName
                }
            }

            EventBus.getDefault().post(ReceiveMessageEvent())

        })

        socket.on("group", Emitter.Listener { args ->
            val realm: Realm = Realm.getDefaultInstance()


            val obj = args[0] as JSONObject

            val checkGroup = realm.where<ChatGroup>().equalTo("ID", obj.getString("ID")).findFirst()

            if (checkGroup != null) {
                //Group Exists -> go into edit mode

                if(!checkGroup.Removable) { //Handles case where you delete a group but the server sends it to you before you're removed on the server
                    val deleteGroups = realm.where<ChatGroup>().equalTo("Removable", false).findAll()

                    for (group in deleteGroups) {
                        this.deleteGroup(group.giveJson()) //Socketevent to notfiy all users

                        for (channel in group.Channels) {
                            realm.executeTransaction {
                                channel.deleteFromRealm()
                            }
                        }

                        group.deleteFromRealm()
                    }
                } else {
                    realm.executeTransaction {
                        checkGroup.Title = obj.getString("Title")
                        checkGroup.Notice = obj.getString("Notice")
                        checkGroup.Channels.first()!!.Title = obj.getString("Title") + " Channel"
                    }

                    val base64String = Base64.decode(obj.getString("Image"), Base64.DEFAULT )

                    if(false/*TODO: checkGroup.getImage() != BitmapFactory.decodeByteArray(base64String, 0, base64String.count()) */) { //convert to Base64 String
                        val imageName = obj.getString("ID") + "_main.png"
                        val imageData = obj.getString("Image")

                        this.saveImageFromString(imageData, imageName)

                        realm.executeTransaction {
                            checkGroup.ImagePath = imageName
                        }
                    }
                }

            } else {
                //Group is New -> make new group

                val imageName = obj.getString("ID") + "_main.png"
                val imageData = obj.getString("Image")

                this.saveImageFromString(imageData, imageName)

                val attachedChannels = realm.where<ChatChannel>().equalTo("GroupID", obj.getString("ID")).findAll()


                realm.executeTransaction {
                    val newGroup = realm.createObject<ChatGroup>()
                    newGroup.setFromJSON(obj)
                    newGroup.ImagePath = imageName

                    for(channel in attachedChannels) {
                        newGroup.Channels.add(channel)
                    }
                }
            }

            EventBus.getDefault().post(ReceiveGroupEvent())


        })

        socket.on("channel", Emitter.Listener { args ->

            val realm: Realm = Realm.getDefaultInstance()


            val obj = args[0] as JSONObject

            val channel = realm.where<ChatChannel>().equalTo("ID", obj.getString("ID")).findFirst()

            val members:JSONArray = obj.getJSONArray("Members")


            if(channel != null) {

                for (i in 0..(members.length() - 1)) {

                    var jsonMember = members.getJSONObject(i)

                    val searchMember = realm.where<User>().equalTo("ID", jsonMember.getString("ID")).findFirst()

                    if(searchMember != null) {

                        //Update Member attributes
                        realm.executeTransaction {
                            searchMember.FirstName = jsonMember.getString("FirstName")
                            searchMember.LastName = jsonMember.getString("LastName")
                            searchMember.Email = jsonMember.getString("Email")
                            searchMember.PhoneNumber = jsonMember.getString("PhoneNumber")

                        }

                        val base64String = Base64.decode(jsonMember.getString("Image"), Base64.DEFAULT )

                        //Update Image if necessary
                        if(false/*TODO: searchMember.getImage() != BitmapFactory.decodeByteArray(base64String, 0, base64String.count())*/ ) { //convert to Base64 String
                            val imageName = jsonMember.getString("ID") + "_main.png"
                            val imageData = jsonMember.getString("Image")

                            this.saveImageFromString(imageData, imageName)

                            realm.executeTransaction {
                                searchMember.ImagePath = imageName
                            }
                        }

                        //Check if member is in channel
                        var isNewMember = true
                        for(dupMember in channel.Members) {
                            if(dupMember.ID == searchMember.ID) {
                                isNewMember = false
                            }
                        }

                        if(isNewMember) {
                            realm.executeTransaction {
                                channel.Members.add(searchMember)
                            }
                        }

                    } else {
                        val imageName = jsonMember.getString("ID") + "_main.png"
                        val imageData = jsonMember.getString("Image")

                        this.saveImageFromString(imageData, imageName)

                        realm.executeTransaction {
                            var createUser = realm.createObject<User>()
                            createUser.setFromJSON(jsonMember)
                            createUser.ImagePath = imageName

                            channel.Members.add(createUser)

                        }
                    }
                }

                //Attach NoticeSender
                val noticeMember: User? = realm.where<User>().equalTo("ID", obj.getString("NoticeSenderID")).findFirst()

                if(noticeMember != null) {
                    realm.executeTransaction {
                        channel.NoticeSender = noticeMember
                    }
                } else { //Applies in the case that noticesender is not in existing group of members
                    for (i in 0..(members.length() - 1)) {

                        val jsonMember = members.getJSONObject(i)

                        if(jsonMember.getString("ID") == obj.getString("NoticeSenderID")) {

                            realm.executeTransaction {
                                val createUser = realm.createObject<User>()
                                createUser.setFromJSON(jsonMember)
                                channel.Members.add(createUser)
                            }

                        }
                    }

                }

                //Delete Extra Members if needed

                if(members.length() < channel.Members.count()) {
                    val IDList = mutableListOf<String>()
                    for (i in 0..(members.length() - 1)) {
                        val jsonMember = members.getJSONObject(i)
                        IDList.add(jsonMember.getString("ID"))

                    }

                    val deleteIndicesList = mutableListOf<Int>()

                    var i = 0
                    for (checkMember in channel.Members) {
                        if(!IDList.contains(checkMember.ID)) {
                            //add Index of member to delete if it is not contained in the ID of the members list received from the server
                            deleteIndicesList.add(i)
                            i -= 1
                        }
                        i+= 1
                    }

                    for (index in deleteIndicesList) {
                        realm.executeTransaction {
                            channel.Members.removeAt(index) // can use RemoveIf
                        }
                    }
                }

                realm.executeTransaction {
                    if (channel.Notice != obj.getString("Notice") || channel.NoticeSender!!.ID != obj.getString("NoticeSenderID")) {
                        channel.ReceivedNotice = true
                    }
                    channel.Title = obj.getString("Title")
                    channel.Notice = obj.getString("Notice")
                    channel.IsPrivate = obj.getBoolean("IsPrivate")
                }

                this.setChannelMember(channel)

            }


            if(channel == null){

                realm.executeTransaction {
                    val createChannel= realm.createObject<ChatChannel>()
                    createChannel.setFromJSON(obj)
                }

                val newChannel = realm.where<ChatChannel>().equalTo("ID", obj.getString("ID")).findFirst()


                for (i in 0..(members.length() - 1)) {

                    val jsonMember = members.getJSONObject(i)

                    val searchMember = realm.where<User>().equalTo("ID", jsonMember.getString("ID")).findFirst()

                    if(searchMember != null) {

                        val base64String = Base64.decode(jsonMember.getString("Image"), Base64.DEFAULT )

                        //Update Image if necessary
                        if(false/* TODO: searchMember.getImage() != BitmapFactory.decodeByteArray(base64String, 0, base64String.count()) */) { //convert to Base64 String
                            val imageName = jsonMember.getString("ID") + "_main.png"
                            val imageData = jsonMember.getString("Image")

                            this.saveImageFromString(imageData, imageName)

                            realm.executeTransaction {
                                searchMember.ImagePath = imageName
                            }
                        }

                        //Add members to new Channel
                        realm.executeTransaction {
                            newChannel!!.Members.add(searchMember)
                        }

                    } else {

                        val imageName = jsonMember.getString("ID") + "_main.png"
                        val imageData = jsonMember.getString("Image")

                        this.saveImageFromString(imageData, imageName)

                        realm.executeTransaction {
                            var createUser = realm.createObject<User>()
                            createUser.setFromJSON(jsonMember)
                            createUser.ImagePath = imageName

                            newChannel!!.Members.add(createUser)
                        }
                    }
                }

                //Attach NoticeSender
                val noticeMember = realm.where<User>().equalTo("ID", obj.getString("NoticeSenderID")).findFirst()

                if(noticeMember != null) {
                    realm.executeTransaction {
                        newChannel!!.NoticeSender = noticeMember
                    }
                } else { //Applies in the case that noticesender is not in existing group of members
                    for (i in 0..(members.length() - 1)) {

                        var jsonMember = members.getJSONObject(i)

                        if(jsonMember.getString("ID") == obj.getString("NoticeSenderID")) {

                            realm.executeTransaction {
                                var createUser = realm.createObject<User>()
                                createUser.setFromJSON(jsonMember)
                                newChannel!!.Members.add(createUser)
                            }

                        }
                    }

                }

                //Attach channel to Group
                val attachGroup = realm.where<ChatGroup>().equalTo("ID", obj.getString("GroupID")).findFirst()

                if(attachGroup != null) {
                    realm.executeTransaction {
                        attachGroup.Channels.add(newChannel)
                    }
                }

                //Handle Messages

                if(!obj.isNull("Messages")) {
                    val messages:JSONArray = obj.getJSONArray("Messages")

                    for (i in 0..(messages.length() - 1)) {

                        realm.executeTransaction {
                            var createMessage = realm.createObject<ChatMessage>()
                            createMessage.setFromJSON(messages.getJSONObject(i))
                            newChannel!!.Messages.add(createMessage) //add message to channel
                        }

                        val newMessage = realm.where<ChatMessage>().equalTo("ID", messages.getJSONObject(i).getString("ID")).findFirst()

                        //Set message image
                        if(!messages.getJSONObject(i).isNull("Image")) {
                            val imageName = messages.getJSONObject(i).getString("ID") + "_main.png"
                            val imageData = messages.getJSONObject(i).getString("Image")

                            this.saveImageFromString(imageData, imageName)

                            realm.executeTransaction {
                                newMessage!!.ImagePath = imageName
                            }

                        }

                        //Set correct message date
                        var format = SimpleDateFormat("dd-MM-yyyy kk:mm:ss")
                        var date = format.parse(messages.getJSONObject(i).getString("CreatedDate")) //use getDateTimeInstance() for local formatting or DD-MM-YYYY HH:mm:ss
                        realm.executeTransaction {
                            newMessage!!.CreatedDate = date
                        }

                        //Attach sender based on ID
                        val searchMember = realm.where<User>().equalTo("ID", messages.getJSONObject(i).getString("SenderID")).findFirst()

                        if(searchMember != null) {
                            realm.executeTransaction {
                                newMessage!!.Sender = searchMember
                            }

                        } else { //Applies in the case that sender is not in existing group of members
                            for (p in 0..(members.length() - 1)) {

                                var jsonMember = members.getJSONObject(p)

                                if(jsonMember.getString("ID") == messages.getJSONObject(i).getString("SenderID")) {

                                    realm.executeTransaction {
                                        var createUser = realm.createObject<User>()
                                        createUser.setFromJSON(jsonMember)
                                        newMessage!!.Sender = createUser

                                    }

                                }
                            }

                        }
                    }
                }
                this.setChannelMember(newChannel!!)
            }

            EventBus.getDefault().post(ReceiveChannelEvent())


        })

        socket.on("user", Emitter.Listener { args ->

            val realm: Realm = Realm.getDefaultInstance()


            val data = args[0] as JSONArray
            val jsonMember = data[0] as JSONObject
            val jsonChannelID = data[1]  as String

            val channel = realm.where<ChatChannel>().equalTo("ID", jsonChannelID).findFirst()

            if (channel != null) {
                val IDList = mutableListOf<String>()
                for (i in 0..(channel.Members.count() - 1)) {
                    IDList.add(channel.Members[i]!!.ID)
                }

                if(IDList.contains(jsonMember.getString("ID"))) {

                    val searchMember = realm.where<User>().equalTo("ID", jsonMember.getString("ID")).findFirst()

                    if(searchMember != null) {
                        realm.executeTransaction {
                            searchMember.FirstName = jsonMember.getString("FirstName")
                            searchMember.LastName = jsonMember.getString("LastName")
                            searchMember.Email = jsonMember.getString("Email")
                            searchMember.PhoneNumber = jsonMember.getString("PhoneNumber")
                        }


                        val base64String = Base64.decode(jsonMember.getString("Image"), Base64.DEFAULT )

                        if(false/*TODO: searchMember.getImage() != BitmapFactory.decodeByteArray(base64String, 0, base64String.count())*/) {
                            val imageName = jsonMember.getString("ID") + "_main.png"
                            val imageData = jsonMember.getString("Image")

                            this.saveImageFromString(imageData, imageName)

                            realm.executeTransaction {
                                searchMember.ImagePath = imageName
                            }
                        }
                    }

                }

                if(!IDList.contains(jsonMember.getString("ID"))) {
                    val searchMember = realm.where<User>().equalTo("ID", jsonMember.getString("ID")).findFirst()

                    if(searchMember != null) {

                        val base64String = Base64.decode(jsonMember.getString("Image"), Base64.DEFAULT )

                        if(false/*TODO: searchMember.getImage() != BitmapFactory.decodeByteArray(base64String, 0, base64String.count())*/) {
                            val imageName = jsonMember.getString("ID") + "_main.png"
                            val imageData = jsonMember.getString("Image")

                            this.saveImageFromString(imageData, imageName)

                            realm.executeTransaction {
                                searchMember.ImagePath = imageName
                            }
                        }

                        realm.executeTransaction {
                            channel.Members.add(searchMember)
                        }

                    }

                    if(searchMember == null) {
                        val imageName = jsonMember.getString("ID") + "_main.png"
                        val imageData = jsonMember.getString("Image")

                        this.saveImageFromString(imageData, imageName)

                        realm.executeTransaction {
                            var createUser = realm.createObject<User>()
                            createUser.setFromJSON(jsonMember)
                            createUser.ImagePath = imageName
                            channel.Members.add(createUser)
                        }
                    }
                }

            }

            EventBus.getDefault().post(ReceiveUserEvent())


        })


        socket.on("deleteChannel", Emitter.Listener { args ->

            val realm: Realm = Realm.getDefaultInstance()

            val deleteID = args[0] as String

            val channel = realm.where<ChatChannel>().equalTo("ID", deleteID).findFirst()

            if(channel != null) {
                if(!channel.IsMember) {
                    realm.executeTransaction {
                        channel.deleteFromRealm()
                    }
                }
            }

        })


        socket.on("findUsersResponse", Emitter.Listener { args ->
            val jsonUsers = args[0] as JSONArray
            //need to decode image to bitmap from base64 encoding
            //send with observer

            val sendObj = JSONObject()
            sendObj.put("users", jsonUsers)

            EventBus.getDefault().post(ReceiveSearchUsersEvent(sendObj))
            //TODO: Handle images
            //need to decode image to bitmap from base64 encoding
            //send with observer


        })

        socket.on("findGroupsResponse", Emitter.Listener { args ->
            val objArray = args[0] as JSONArray
            val jsonGroups = objArray[0] as JSONArray
            val jsonExtras = objArray[1] as JSONArray // contains ChannelCount and MemberCount

            val sendObj = JSONObject()
            sendObj.put("groups", jsonGroups)
            sendObj.put("extras", jsonExtras)


            EventBus.getDefault().post(ReceiveSearchGroupsEvent(sendObj))
            //TODO: Handle image
            //need to decode image to bitmap from base64 encoding
            //send with observer

        })

//        socket.on("userUpdateResponse", Emitter.Listener { args ->
//            // get value from "Attribute" & "Value" JSON properties
//            val jsonUserUpdate = args[0] as JSONObject
//
//            EventBus.getDefault().post(ReceiveUserUpdateEvent(jsonUserUpdate))
//
//        })

        socket.on("passwordResponse", Emitter.Listener { args ->
            // get value from "Code" & "Status" JSON properties
            val jsonPassword = args[0] as JSONObject


            EventBus.getDefault().post(ReceiveUserPasswordUpdateEvent(jsonPassword))
        })

        socket.on("loginResponse", Emitter.Listener { args ->
            val loginResponse = args[0] as JSONObject

            EventBus.getDefault().post(LoginUserEvent(loginResponse))


        })

        socket.on("silentLoginResponse", Emitter.Listener { args ->

            val realm: Realm = Realm.getDefaultInstance()


            val loginResponse = args[0] as JSONObject

            if(loginResponse.getBoolean("status")) {

                //Networkable changed
                val networkableGroups = realm.where<ChatGroup>().equalTo("Networked", false).findAll().toList()

                for (group in networkableGroups) {
                    this.createGroup(group.giveJson())

                    realm.executeTransaction {
                        group.Networked = true
                    }
                }

                val networkableChannels = realm.where<ChatChannel>().equalTo("Networked", false).findAll().toList()

                for (channel in networkableChannels) {
                    this.createChannel(channel.giveJson())

                    realm.executeTransaction {
                        channel.Networked = true
                    }
                }

                val networkableMessaages = realm.where<ChatMessage>().equalTo("Networked", false).sort("CreatedDate", Sort.ASCENDING).findAll().toList()

                for (message in networkableMessaages) {
                    this.sendMessage(message.giveJson())

                    realm.executeTransaction {
                        message.Networked = true
                    }
                }

                //Updateable changed

                val updateableGroups = realm.where<ChatGroup>().equalTo("Updated", false).findAll().toList()

                for (group in updateableGroups) {
                    this.updateGroup(group.giveJson())
                    realm.executeTransaction {
                        group.Updated = true
                    }
                }

                val updateableChannels = realm.where<ChatChannel>().equalTo("Updated", false).findAll().toList()

                for (channel in updateableChannels) {
                    this.updateChannel(channel.giveJson())
                    realm.executeTransaction {
                        channel.Updated = true
                    }
                }

                val updateableUsers = realm.where<User>().equalTo("Updated", false).findAll().toList()

                for (user in updateableUsers) {
                    this.updateUser(user.giveJson())
                    realm.executeTransaction {
                        user.Updated = true
                    }
                }

                val deleteableChannels = realm.where<ChatChannel>().equalTo("Removable", false).findAll().toList()

                for (channel in deleteableChannels) {
                    this.deleteChannel(channel.giveJson())
                    realm.executeTransaction {
                        channel.Removable = true
                    }
                }

                //check offline members

                val channels = realm.where<ChatChannel>().findAll().toList()

                for (channel in channels) {
                    if(channel.OfflineMembers.toList().count() > 0) {
                        for (member in channel.OfflineMembers) {
                            this.sendUser(member.giveJson(), channel.ID)

                            realm.executeTransaction {
                                channel.OfflineMembers.clear()
                            }
                        }
                    }
                }

            }

        })

        socket.on("registerResponse", Emitter.Listener { args ->

            val jsonRegisterResponse = args[0] as JSONObject

            //liveRegisterResponse.postValue(jsonRegisterResponse)
            EventBus.getDefault().post(RegisterUserEvent(jsonRegisterResponse))

        })

    }

    fun setChannelMember(channel: ChatChannel) {

        val realm: Realm = Realm.getDefaultInstance()


        var memberCheck = false

        for (member in channel.Members) {
            if(member.ID == UserManager.shared.getUser().ID) {
                memberCheck = true
            }
        }

        if(memberCheck) {
            realm.executeTransaction {
                channel.IsMember = true
            }
        } else {
            realm.executeTransaction {
                channel.IsMember = false
            }
        }


    }

    fun saveImageFromString(base64String: String, name: String) {

    }

    fun socketConnected(): Boolean {
        return socket.connected()
    }

    fun sendMessage(message: JSONObject) {
        socket.emit("createMessage", message)
    }

    fun sendUser(user: JSONObject, channelID: String) {

        val userChannelObj = JSONObject()
        userChannelObj.put("User", user)
        userChannelObj.put("ChannelID", channelID)

        socket.emit("sendUser", userChannelObj)
    }

    fun updateGroup(group: JSONObject) {
        socket.emit("updateGroup", group)
    }

    fun updateChannel(channel: JSONObject) {
        socket.emit("updateChannel", channel)
    }

    fun updateUser(user: JSONObject) {
        socket.emit("updateUser", user)
    }

    fun updatePassword(password: String) {
        socket.emit("updatePassword", password)
    }

    fun createGroup(group: JSONObject) {
        socket.emit("createGroup", group)
    }

    fun createChannel(channel: JSONObject) {
        socket.emit("createChannel", channel)
    }

    fun deleteChannel(channel: JSONObject) {
        socket.emit("leaveChannel", channel)
    }

    fun deleteGroup(group: JSONObject) {
        socket.emit("leaveGroup", group)
    }

    fun joinGroup(group: JSONObject) {
        socket.emit("joinGroup", group)
    }

    fun findUsers(queryObj: JSONObject) {
        socket.emit("findUsers", queryObj)
    }

    fun findGroups(queryObj: JSONObject ) {
        socket.emit("findGroups", queryObj)
    }

    fun login(user: JSONObject) {
        socket.emit("login", user)
    }

    fun checkUsername(name: String) {
        socket.emit("checkUsername", name, Ack { args ->
            if (args != null) {
                val value = args[0] as Boolean
                val obj = JSONObject()
                obj.put("Status", value)
                EventBus.getDefault().post(ReceiveUsernameCheckEvent(obj))
            }
        })
    }

    fun adjustToken(token: String) { //maybe not needed on Android
        socket.emit("tokenUpdate", token)
    }

    fun silentLogin() {
        if(UserManager.shared.isOneLocalUser()) {
            socket.emit("silentLogin", UserManager.shared.getUser().giveJson())
        }
    }

    fun register(user: JSONObject) {
        socket.emit("register", user)
    }

    fun logout() {
        socket.emit("logout", UserManager.shared.getUser().giveJson()) //use UserManager
    }

    fun establishConnection() {
        socket.connect()
        Log.d("test4", "ESTABLISH CONNECTION")
    }

    fun closeConnection() {
        socket.disconnect()
        Log.d("test5", "CLOSE CONNNECTION")

    }

}