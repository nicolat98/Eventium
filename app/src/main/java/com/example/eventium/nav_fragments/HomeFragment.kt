package com.example.eventium.nav_fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventium.*
import com.example.eventium.FragmentUtil.refreshFragment
import com.example.eventium.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
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
    private lateinit var btn_search : ImageButton
    private lateinit var rl_search : RelativeLayout
    private var fav_mode = false

    private lateinit var radio_group_src : RadioGroup


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

        db.child("event").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()

                for(postSnapshot in snapshot.children){
                    val currentEvent = postSnapshot.getValue(Event::class.java)
                    eventList.add(currentEvent!!)
                }

                eventList.sortBy {
                    it.name!!.uppercase()
                }

                //Ordina per data e ora
                //eventList.sortWith(compareBy<Event>  { it.date!!.split("/")[2] }.thenBy { it.date!!.split("/")[1] }.thenBy { it.date!!.split("/")[0] }.thenBy { it.time })

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
        val view: View = inflater!!.inflate(R.layout.fragment_home, container, false)
        eventList = ArrayList()
        eventAdapter = EventAdapter(context, eventList)
        eventRecyclerView = view.findViewById(R.id.event_recycler_view)
        eventRecyclerView.layoutManager = LinearLayoutManager(context)
        eventRecyclerView.adapter = eventAdapter
        btn_fav_home = view.findViewById(R.id.btn_fav_home)
        btn_search = view.findViewById(R.id.btn_search)
        radio_group_src = view.findViewById(R.id.radio_group_search)
        rl_search = view.findViewById(R.id.rl_search)
        rl_search.visibility = View.GONE

        auth = FirebaseAuth.getInstance()

        db = FirebaseDatabase.getInstance().reference

        db.child("event").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()

                for(postSnapshot in snapshot.children){
                    val currentEvent = postSnapshot.getValue(Event::class.java)
                    eventList.add(currentEvent!!)
                }

                eventList.sortBy {
                    it.name!!.uppercase()
                }

                //Ordina per data e ora
                //eventList.sortWith(compareBy<Event>  { it.date!!.split("/")[2] }.thenBy { it.date!!.split("/")[1] }.thenBy { it.date!!.split("/")[0] }.thenBy { it.time })

                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        //ottengo la lista dei preferiti dell'utente
        db.child("user").child(auth.currentUser?.uid!!).addListenerForSingleValueEvent(object: ValueEventListener{
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

        btn_fav_home.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.nav_container, HomeFavFragment())
            transaction?.disallowAddToBackStack()
            transaction?.commit()
        }

        btn_search.setOnClickListener {
            if(rl_search.visibility == View.VISIBLE){
                rl_search.visibility = View.GONE
                btn_search.setImageResource(R.drawable.ic_search)
            }else{
                rl_search.visibility = View.VISIBLE
                btn_search.setImageResource(R.drawable.ic_search_off)
            }
        }

        radio_group_src.setOnCheckedChangeListener { radio_group_src, i ->

            if(i == R.id.radio_btn_order_name){
                eventList.sortBy {
                    it.name!!.uppercase()
                }

                eventAdapter.notifyDataSetChanged()

            }else if(i == R.id.radio_btn_order_date){
                eventList.sortWith(compareBy<Event>  { it.date!!.split("/")[2] }.thenBy { it.date!!.split("/")[1] }.thenBy { it.date!!.split("/")[0] }.thenBy { it.time })

                eventAdapter.notifyDataSetChanged()

            }else if(i == R.id.radio_btn_order_type){
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
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}