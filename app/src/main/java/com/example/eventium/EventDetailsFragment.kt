package com.example.eventium

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.findFragment
import com.example.eventium.nav_fragments.HomeFavFragment
import com.example.eventium.nav_fragments.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var event_name : TextView
    private lateinit var image : ImageView
    private lateinit var event_type : TextView
    private lateinit var event_location : TextView
    private lateinit var event_date_time : TextView
    private lateinit var event_num_max: TextView
    private lateinit var event_description : TextView
    private lateinit var creator_name : TextView
    private lateinit var btn_fav : ImageButton
    private var user_favorites = ArrayList<String>()
    private lateinit var btn_contact : Button
    private lateinit var btn_partecipate : Button
    private lateinit var ll_bottom : LinearLayout
    private lateinit var event : Event

    private lateinit var communicator: Communicator

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: DatabaseReference =  FirebaseDatabase.getInstance().reference
    lateinit var imagesRef : StorageReference
    val storage = Firebase.storage
    val storageRef = storage.reference

    private var event_creator : User = User()
    private var auth_user : User = User()
    private lateinit var btn_back : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_event_details, container, false)

        event_name = view.findViewById(R.id.tv_details_title)
        btn_back = view.findViewById(R.id.btn_details_event_back)
        btn_fav = view.findViewById(R.id.btn_fav_details_event)
        image = view.findViewById(R.id.iv_details_event_image)
        event_type = view.findViewById(R.id.tv_details_typology)
        event_location = view.findViewById(R.id.tv_details_location)
        event_date_time = view.findViewById(R.id.tv_details_date_time)
        event_num_max = view.findViewById(R.id.tv_details_num_max)
        event_description = view.findViewById(R.id.tv_details_description)
        creator_name = view.findViewById(R.id.tv_details_creator)
        btn_contact = view.findViewById(R.id.btn_contact)
        btn_partecipate = view.findViewById(R.id.btn_partecipate)
        ll_bottom = view.findViewById(R.id.ll_bottom_buttons)
        event = Event()




        //ricevo i dati
        var event = arguments?.getSerializable("event") as Event

        var event_creator_uid = arguments?.getString("event_creator_uid")

        //event_name.text = arguments?.getString("event_name")

        //creator_username = findViewById(R.id.tv_details_creator)



        event_name.text = event?.name

        event_type.text = event!!.type
        event_location.text = event.location
        event_date_time.text = "${event.date} - ${event.time}"
        event_num_max.text = "${event.num_part} / ${event.max_part}"
        event_description.text = event.description

        //carico la foto dallo storage
        imagesRef = storageRef.child("${event.eid}/images/event_image.png")
        val localfile = File.createTempFile("tempImage", "png")
        imagesRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            image.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(context, "Download failed...", Toast.LENGTH_SHORT).show()
        }


        //val creatorUid = activity!!.intent.getStringExtra("creator")


        //ottengo la lista di utenti e cerco quello che ha creato l'evento e quello attualmente loggato
        db.child("user").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)

                    if(currentUser!!.uid.equals(event_creator_uid.toString())){
                        event_creator = currentUser
                    }
                    if(currentUser!!.uid.equals(auth.currentUser!!.uid)){
                        auth_user = currentUser
                    }
                }

                creator_name.text = "${event_creator.username} (${event_creator.stars} \u2605)" //simbolo stella


                //coloro il bottone favoriti
                if(auth_user.favorites!!.contains(event.eid)){
                    btn_fav.setImageResource(R.drawable.ic_favorite)
                }

                auth_user.favorites!!.forEach {
                    user_favorites.add(it)
                }

                //modifico il bottone in base all'utente
                if(auth.currentUser!!.uid == event_creator!!.uid){
                    ll_bottom.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        btn_contact.setOnClickListener {
            // aprire la chat
            println(auth.currentUser!!.uid)
            println(event_creator!!.uid)

            Toast.makeText(context, "Chat aperta con ${event_creator!!.username}", Toast.LENGTH_SHORT).show()
        }



        btn_fav.setOnClickListener{
            //Se è già nei preferiti lo tolgo, altrimenti lo aggiungo
            if(auth_user.favorites!!.contains(event.eid)){
                btn_fav.setImageResource(R.drawable.ic_favorite_border)
                user_favorites.remove("${event.eid}")

                val updatedUser = mapOf<String, Any?>(
                    "favorites" to user_favorites
                )
                //tolgo dai preferiti nel db
                db.child("user").child(auth.currentUser?.uid!!).updateChildren(updatedUser)
            }else{
                user_favorites.add("${event.eid}")
                btn_fav.setImageResource(R.drawable.ic_favorite)

                val updatedUser = mapOf<String, Any?>(
                    "favorites" to user_favorites
                )

                //aggiungo ai preferiti nel db
                db.child("user").child(auth.currentUser?.uid!!).updateChildren(updatedUser) //lo aggiungo al db
            }
        }



        btn_back.setOnClickListener {
            /*
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.setCustomAnimations(R.anim.slide_from_left, R.anim.slide_to_right)
            transaction?.replace(R.id.nav_container, HomeFragment())
            transaction?.disallowAddToBackStack()
            transaction?.commit()*/

            //torna al fragment precedente
            fragmentManager!!.popBackStack()

        }

        creator_name.setOnClickListener {
            Toast.makeText(context, "Cliccato", Toast.LENGTH_SHORT).show()

            communicator = activity as Communicator
            communicator.passUser(event_creator)

            /*
            val intent = Intent(context, CreatorDetailsActivity::class.java)

            intent.putExtra("user",  event_creator)

            startActivity(intent)
            activity!!.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)*/
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
         * @return A new instance of fragment EventDetailsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}