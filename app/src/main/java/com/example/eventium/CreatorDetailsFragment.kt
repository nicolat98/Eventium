package com.example.eventium

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CreatorDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreatorDetailsFragment : Fragment() {
    private lateinit var btn_back : Button
    private lateinit var creator_title : TextView
    private lateinit var rating_bar : RatingBar


    private lateinit var eventRecyclerView : RecyclerView
    private lateinit var eventList : ArrayList<Event>
    private lateinit var eventAdapter : EventAdapter

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: DatabaseReference =  FirebaseDatabase.getInstance().reference

    private var creator : User? = null
    private var auth_user : User = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_creator_details, container, false)

        btn_back = view.findViewById(R.id.btn_details_creator_back)
        creator_title = view.findViewById(R.id.tv_details_creator_title)
        rating_bar = view.findViewById(R.id.rating_bar)


        eventList = ArrayList()
        eventAdapter = EventAdapter(context, eventList)
        eventRecyclerView = view.findViewById(R.id.event_recycler_view)
        eventRecyclerView.layoutManager = LinearLayoutManager(context)
        eventRecyclerView.adapter = eventAdapter


        db = FirebaseDatabase.getInstance().reference

        //creator = intent.getSerializableExtra("user") as? User

        var creator = arguments?.getSerializable("creator") as User

        //creator_title.text = creator_uid

        creator_title.text = creator!!.username
        rating_bar.rating = creator?.stars!!.toFloat()

        db.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if(currentUser!!.uid.equals(auth.currentUser!!.uid)){
                        auth_user = currentUser
                    }
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

                    if(creator!!.uid == currentEvent!!.uid){
                        eventList.add(currentEvent!!)
                    }

                }

                eventList.sortWith(compareBy<Event>  { it.date!!.split("/")[2] }.thenBy { it.date!!.split("/")[1] }.thenBy { it.date!!.split("/")[0] }.thenBy { it.time })

                //Ordina per data e ora
                //eventList.sortWith(compareBy<Event>  { it.date!!.split("/")[2] }.thenBy { it.date!!.split("/")[1] }.thenBy { it.date!!.split("/")[0] }.thenBy { it.time })

                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        btn_back.setOnClickListener {
            //in questo modo torno all'attività precedente
            /*finish()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)*/

            fragmentManager!!.popBackStack()
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
         * @return A new instance of fragment CreatorDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreatorDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}