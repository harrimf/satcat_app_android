package com.satcat.kalys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager.ReceiveChannelEvent
import com.satcat.kalys.Managers.SocketIOManager.ReceiveMessageEvent
import com.satcat.kalys.Managers.SocketIOManager.ReceiveUserEvent
import com.satcat.kalys.Models.ChatGroup
import io.realm.Realm
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class GroupsFragment : Fragment()
{
    private lateinit var groupsAdapter : RecyclerAdapter
    private lateinit var explainLbl : TextView

    var groups: List<ChatGroup> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_groups, container, false)

        explainLbl = view!!.findViewById(R.id.groups_explain_lbl)

        val recycler : RecyclerView = view!!.findViewById(R.id.recycler_groups)
        initRecyclerView(recycler)


        return view

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        handleReceive()
    }

    private fun initRecyclerView(recycler : RecyclerView) {
        recycler.apply {
            layoutManager = GridLayoutManager(activity, 2)
            groupsAdapter = RecyclerAdapter(RecyclerAdapter.RecyclerType.GROUPS) //give string for group
            //can use decorator
            adapter = groupsAdapter

        }

    }



    fun handleReceive() {
        val realm: Realm = Realm.getDefaultInstance()

        groups = realm.where<ChatGroup>().equalTo("Removable", true).findAll().toList()
        groupsAdapter.submitList(groups)
        groupsAdapter.notifyDataSetChanged()

        if(groups.count() > 0) {
            explainLbl.isVisible = false
            val parent = activity as StartTabActivity
            parent.startTabText.isVisible = false

        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveChannelEvent(event: ReceiveChannelEvent): Unit {
        handleReceive()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMessageEvent(event: ReceiveMessageEvent): Unit {
        handleReceive()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveUserEvent(event: ReceiveUserEvent): Unit {
        handleReceive()

    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}