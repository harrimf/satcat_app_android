package com.satcat.kalys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.satcat.kalys.Managers.SocketIOManager
import com.satcat.kalys.Models.ChatChannel
import com.satcat.kalys.Models.ChatGroup
import com.shuhart.stickyheader.StickyHeaderItemDecorator
import io.realm.Realm
import io.realm.kotlin.where
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AllChannelsFragment : Fragment()
{
    private lateinit var channelsAdapter : HeaderAdapter
//
//    var groups: List<ChatGroup> = emptyList()
//
//    var memberChannels : ArrayList<ArrayList<ChatChannel>> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_all_channels, container, false)

        //return inflater.inflate(R.layout.fragment_groups, container, false)

        initRecyclerView(view!!.findViewById(R.id.recycler_channels))


        return view
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        handleReceive()

    }
    

    private fun initRecyclerView(recycler : RecyclerView) {



        recycler.apply {
            layoutManager = LinearLayoutManager(activity)
            channelsAdapter = HeaderAdapter(HeaderAdapter.HeaderRecyclerType.ALLCHANNELS, recyclerList())
            adapter = channelsAdapter
        }

        StickyHeaderItemDecorator(channelsAdapter).attachToRecyclerView(recycler)

    }

   private fun recyclerList() : ArrayList<RecyclerChannel> {
        val realm: Realm = Realm.getDefaultInstance()

        val list  = realm.where<ChatChannel>().sort("GroupID").equalTo("IsMember", true).equalTo("Removable", true).findAll().toList()

        val newList = ArrayList<RecyclerChannel>()

        var sectionPosition = 0

        if(list.count() > 0) {
            val group = realm.where<ChatGroup>().equalTo("ID", list[0].GroupID).findFirst()
            newList.add(RecyclerChannelHeader(group!!.Title, sectionPosition))
            newList.add(RecyclerChannelChild(list[0], sectionPosition))
        }


        if(list.count() > 1) {
            for (i in 1 until list.count()) {

                if (list[i - 1].GroupID == list[i].GroupID) {
                    //add normally
                    newList.add(RecyclerChannelChild(list[i], sectionPosition))

                } else {
                    sectionPosition = newList.count()

                    val group = realm.where<ChatGroup>().equalTo("ID", list[i].GroupID).findFirst()
                    newList.add(RecyclerChannelHeader(group!!.Title, sectionPosition))
                    newList.add(RecyclerChannelChild(list[i], sectionPosition))

                }
            }
        }

        return newList

    }

    fun handleReceive() {

//        groups = realm.where<ChatGroup>().equalTo("Removable", true).findAll().toList()
//
//        var fillGroupChannels : ArrayList<ArrayList<ChatChannel>> = ArrayList()
//
//        for(group in groups) {
//            var groupChannels : ArrayList<ChatChannel> = ArrayList()
//
//            for(channel in group.Channels) {
//                if(channel.IsMember) {
//                    groupChannels.add(channel)
//                }
//            }
//            fillGroupChannels.add(groupChannels)
//        }
//        memberChannels = fillGroupChannels

        channelsAdapter.submitList(recyclerList()) //TODO: Handle 2 lists for recycler
        channelsAdapter.notifyDataSetChanged()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveChannelEvent(event: SocketIOManager.ReceiveChannelEvent): Unit {
        handleReceive()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveMessageEvent(event: SocketIOManager.ReceiveMessageEvent): Unit {
        handleReceive()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveUserEvent(event: SocketIOManager.ReceiveUserEvent): Unit {
        handleReceive()
    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}