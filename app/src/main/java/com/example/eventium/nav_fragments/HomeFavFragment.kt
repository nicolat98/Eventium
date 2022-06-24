package com.example.eventium.nav_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventium.Event
import com.example.eventium.EventAdapter
import com.example.eventium.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFavFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFavFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var eventRecyclerView : RecyclerView
    private lateinit var eventList : ArrayList<Event>
    private lateinit var eventAdapter : EventAdapter
    private lateinit var db: DatabaseReference
    private var favorites = ArrayList<String>()
    private lateinit var auth: FirebaseAuth
    private lateinit var btn_fav_home : ImageButton
    private lateinit var btn_search_fav : ImageButton
    private lateinit var rl_search_fav : RelativeLayout
    private var fav_mode = false

    private lateinit var radio_group_src_fav : RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        //Toast.makeText(context, "Resumed...", Toast.LENGTH_SHORT).show()

        //ottengo la lista dei preferiti dell'utente
        db.child("user").child(auth.currentUser?.uid!!).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favorites.clear()
                for(element in snapshot.child("favorites").children){
                    favorites.add(element.value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        db.child("event").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()

                for(postSnapshot in snapshot.children){
                    val currentEvent = postSnapshot.getValue(Event::class.java)

                    if(favorites.contains("${currentEvent?.eid}")){
                        eventList.add(currentEvent!!)
                    }
                }

                eventList.sortBy {
                    it.name!!.uppercase()
                }

                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_home_fav, container, false)
        eventList = ArrayList()
        eventAdapter = EventAdapter(context, eventList)
        eventRecyclerView = view.findViewById(R.id.event_recycler_view_fav)
        eventRecyclerView.layoutManager = LinearLayoutManager(context)
        eventRecyclerView.adapter = eventAdapter
        btn_fav_home = view.findViewById(R.id.btn_fav_home_2)

        btn_search_fav = view.findViewById(R.id.btn_search_fav)
        radio_group_src_fav = view.findViewById(R.id.radio_group_search_fav)
        rl_search_fav = view.findViewById(R.id.rl_search_fav)
        rl_search_fav.visibility = View.GONE

        auth = FirebaseAuth.getInstance()

        db = FirebaseDatabase.getInstance().reference

        //ottengo la lista dei preferiti dell'utente
        db.child("user").child(auth.currentUser?.uid!!).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favorites.clear()
                for(element in snapshot.child("favorites").children){
                    favorites.add(element.value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        db.child("event").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()

                for(postSnapshot in snapshot.children){
                    val currentEvent = postSnapshot.getValue(Event::class.java)

                    if(favorites.contains("${currentEvent?.eid}")){
                        eventList.add(currentEvent!!)
                    }
                }

                eventList.sortBy {
                    it.name!!.uppercase()
                }

                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        btn_fav_home.setOnClickListener {
            //refreshFragment(context)
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.nav_container, HomeFragment())
            transaction?.disallowAddToBackStack()
            transaction?.commit()
        }

        btn_search_fav.setOnClickListener {
            if(rl_search_fav.visibility == View.VISIBLE){
                rl_search_fav.visibility = View.GONE
                btn_search_fav.setImageResource(R.drawable.ic_search)
            }else{
                rl_search_fav.visibility = View.VISIBLE
                btn_search_fav.setImageResource(R.drawable.ic_search_off)
            }
        }

        radio_group_src_fav.setOnCheckedChangeListener { radio_group_src, i ->

            if(i == R.id.radio_btn_order_name_fav){
                eventList.sortBy {
                    it.name!!.uppercase()
                }

                eventAdapter.notifyDataSetChanged()

            }else if(i == R.id.radio_btn_order_date_fav){
                eventList.sortWith(compareBy<Event>  { it.date!!.split("/")[2] }.thenBy { it.date!!.split("/")[1] }.thenBy { it.date!!.split("/")[0] }.thenBy { it.time })

                eventAdapter.notifyDataSetChanged()

            }else if(i == R.id.radio_btn_order_type_fav){
                eventList.sortBy {
                    it.type!!.uppercase()
                }

                eventAdapter.notifyDataSetChanged()
            }
        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFavFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFavFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}