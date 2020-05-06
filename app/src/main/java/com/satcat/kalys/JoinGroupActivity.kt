package com.satcat.kalys

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Managers.SocketIOManager.ReceiveSearchGroupsEvent
import com.satcat.kalys.Models.ChatGroup
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONArray
import org.json.JSONObject

class JoinGroupActivity : AppCompatActivity() {

    private lateinit var groupsAdapter : RecyclerAdapter

    var groupDataList : List<Pair<ChatGroup, JSONObject>> = emptyList()

    private lateinit var realm: Realm


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_group)

        supportActionBar!!.title = "Join Group"


        realm = Realm.getDefaultInstance()

        val context = this.applicationContext

        val searchTxt : EditText =  findViewById(R.id.join_group_search_field)
        searchTxt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                val searchJSON = JSONObject()
                searchJSON.put("Text", searchTxt.text.toString())

                if (SocketIOManager.shared.socketConnected()) {
                    SocketIOManager.shared.findGroups(searchJSON)
                } else {
                    val toast = Toast.makeText(context, "Could not connect to the network, try again later", Toast.LENGTH_LONG)
                    toast.show()
                }

            }
        })


        val recycler : RecyclerView = findViewById(R.id.recycler_join_group)

        initRecyclerView(recycler)
        groupsAdapter.submitList(groupDataList)

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = LinearLayoutManager(this.context)
            groupsAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.JOINGROUPS) //give string for group
            //can use decorator
            adapter = groupsAdapter

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveChannelEvent(event: SocketIOManager.ReceiveChannelEvent): Unit {
        finish() //TODO: need to check if it is the correct group

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveGroupsEvent(event: ReceiveSearchGroupsEvent): Unit {

        val groupsArray = event.json.getJSONArray("groups") as JSONArray
        val extrasArray = event.json.getJSONArray("extras") as JSONArray

        var dataList : ArrayList<Pair<ChatGroup, JSONObject>> = ArrayList()

        for(i in 0 until groupsArray.length()) {
            val group = ChatGroup()
            group.setFromJSON(groupsArray.getJSONObject(i))

            val extra = extrasArray.getJSONObject(i)

            dataList.add(Pair(group, extra))  //maybe add image to tuple

        }

        groupDataList = dataList
        groupsAdapter.submitList(groupDataList)
        groupsAdapter.notifyDataSetChanged()

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