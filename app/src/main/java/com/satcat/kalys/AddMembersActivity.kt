package com.satcat.kalys

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import com.satcat.kalys.Models.User
import io.realm.Realm
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

class AddMembersActivity : AppCompatActivity() {

    companion object {
        var searchGroupOnly = false
        var fromOptions = false
        lateinit var associatedGroup: ChatGroup
        lateinit var editedChannel: ChatChannel

    }

    lateinit var userAdapter : RecyclerAdapter

    var users: List<User> = emptyList()

    var usersAdded: MutableSet<User> = mutableSetOf()
    var userIDs : MutableSet<String> = mutableSetOf()

    private lateinit var realm : Realm

//    var searchGroupOnly = false
//    var fromOptions = false
//    lateinit var associatedGroup: ChatGroup
//    lateinit var editedChannel: ChatChannel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_members)

        supportActionBar!!.title = "Add Members"


        realm = Realm.getDefaultInstance()

//        searchGroupOnly = intent.getBooleanExtra("searchGroupOnly", false)
//        fromOptions = intent.getBooleanExtra("fromOptions", false)
//        editedChannel = realm.where<ChatChannel>().equalTo("ID", intent.getStringExtra("channelID")).findFirst()!!
//        if(searchGroupOnly) associatedGroup = realm.where<ChatGroup>().equalTo("ID", intent.getStringExtra("groupID")).findFirst()!!

        val context = this.applicationContext

        val searchTxt : EditText =  findViewById(R.id.add_members_search_field)
        searchTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                val searchJSON = JSONObject()
                searchJSON.put("Text", searchTxt.text.toString())
                if(searchGroupOnly) searchJSON.put("GroupID", associatedGroup.ID)

                if (SocketIOManager.shared.socketConnected()) {
                    SocketIOManager.shared.findUsers(searchJSON)
                } else {
                    val toast = Toast.makeText(context, "Could not connect to the network, try again later", Toast.LENGTH_LONG)
                    toast.show()
                }

            }
        })


        val addBtn: Button =  findViewById(R.id.add_members_add_btn)
        val addDirectBtn: Button =  findViewById(R.id.add_members_add_btn_direct)

        if(fromOptions) {
            addDirectBtn.isVisible = true
            addBtn.isVisible = false
        }

        for (user in editedChannel.Members) {
            userIDs.add(user.ID)

        }

        addBtn.setOnClickListener {

            var finalUsersAdded : MutableSet<User> = mutableSetOf()

            for (user in usersAdded) {

                var foundMembers = realm.where<User>().equalTo("ID", user.ID).findAll().toList()

                if (!foundMembers.isEmpty()) {
                    //TODO: save image and imagepath propert on User
                    finalUsersAdded.add(foundMembers.first())


                } else {
                    //TODO: save image and imagepath propert on User
                    finalUsersAdded.add(user)

                }

            }

            realm.executeTransaction {
                editedChannel.Members.addAll(finalUsersAdded)
            }

            this.onBackPressed() //maybe finish

        }

        addDirectBtn.setOnClickListener {

            //I think it is useless
            for (user in usersAdded) {
                for (member in editedChannel.Members) {
                    if(member.ID == user.ID) {
                        usersAdded.remove(user)
                    }
                }
            }

            var finalUsersAdded : MutableSet<User> = mutableSetOf()

            for (user in usersAdded ) {
                var foundMembers = realm.where<User>().equalTo("ID", user.ID).findAll().toList()

                if(!foundMembers.isEmpty()) {
                    finalUsersAdded.add(foundMembers.first())
                } else {
                    finalUsersAdded.add(user)
                }

            }

            for (user in finalUsersAdded) {
                if(SocketIOManager.shared.socketConnected()) {
                    SocketIOManager.shared.sendUser(user.giveJson(), editedChannel.ID)
                } else {
                    realm.executeTransaction {
                        editedChannel.OfflineMembers.add(user)
                    }
                }
            }

            this.onBackPressed() //maybe finish

        }


        //Assume have filled userIDs and users added

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recycler : RecyclerView = findViewById(R.id.recycler_add_members)


        initRecyclerView(recycler)
        userAdapter.submitList(users)

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)

        realm = Realm.getDefaultInstance()

    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            userAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.ADDMEMBERS) //give string for group
            //can use decorator
            adapter = userAdapter

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveSearchUsersEvent(event: SocketIOManager.ReceiveSearchUsersEvent): Unit {

        val usersArray = event.json.getJSONArray("users") as JSONArray
        //update list

        var dataList : ArrayList<User> = ArrayList()


        for(i in 0 until usersArray.length()) {
            val user = User()
            user.setFromJSON(usersArray.getJSONObject(i))

            dataList.add(user)

        }

        users = dataList
        userAdapter.submitList(users)
        userAdapter.notifyDataSetChanged()


        }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}