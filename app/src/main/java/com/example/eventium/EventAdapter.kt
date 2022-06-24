package com.example.eventium

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventium.nav_fragments.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.*
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.util.*


//classe che gestisce la lista di eventi
class EventAdapter(val context: Context?, val eventList: ArrayList<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private lateinit var communicator: Communicator

    private var db: DatabaseReference =  FirebaseDatabase.getInstance().reference
    private var auth: FirebaseAuth = getInstance()
    private lateinit var authUser : User
    private var user_favorites = ArrayList<String>()
    lateinit var imagesRef : StorageReference
    val storage = Firebase.storage
    val storageRef = storage.reference

    class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val event_title = itemView.findViewById<TextView>(R.id.tv_event_title)
        val event_date_hour = itemView.findViewById<TextView>(R.id.tv_event_date_hour)
        val event_location = itemView.findViewById<TextView>(R.id.tv_event_location)
        val event_creator = itemView.findViewById<TextView>(R.id.tv_event_creator)
        val event_num_max = itemView.findViewById<TextView>(R.id.tv_num_max)
        //val event_image = itemView.findViewById<ImageButton>(R.id.ib_event_image)
        val event_image = itemView.findViewById<ImageView>(R.id.iv_event)
        val card_view = itemView.findViewById<CardView>(R.id.card_view_event)
        val btn_like = itemView.findViewById<ImageButton>(R.id.btn_like)
        val card_view_num_max = itemView.findViewById<CardView>(R.id.material_card_view_num_max)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.event_layout, parent, false)

        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentEvent = eventList[position]

        //ottengo la lista di utenti e cerco quello che ha creato l'evento
        db.child("user").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)
                    if(currentUser!!.uid.equals(currentEvent.uid)){
                        //println("${currentEvent.eid}, ${currentEvent.uid}, ${currentUser.username}")
                        holder.event_creator.text = currentUser.username
                        //eventCreator = currentUser
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        db.child("user").child(auth.currentUser?.uid!!).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val favorites = ArrayList<String>()
                for(element in snapshot.child("favorites").children){
                    favorites.add(element.value.toString())
                }
                authUser = User(snapshot.child("username").value.toString(), snapshot.child("email").value.toString(), snapshot.key, favorites)

                /*
                println(authUser.username)
                println(authUser.email)
                println(authUser.uid)
                println(authUser.favorites)*/

                //inizializzo il bottone
                if(authUser.favorites!!.contains("${currentEvent.eid}")){
                    holder.btn_like.setImageResource(R.drawable.ic_favorite)
                }else{
                    holder.btn_like.setImageResource(R.drawable.ic_favorite_border)
                }

                //creo la lista di favoriti, copiando quella dell'utente
                user_favorites.clear()
                authUser.favorites!!.forEach {
                    user_favorites.add(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        holder.event_title.text = currentEvent.name
        holder.event_date_hour.text = "${currentEvent.date} - ${currentEvent.time}"
        holder.event_location.text = currentEvent.location


        if(currentEvent.num_part!! < currentEvent.max_part!!){
            holder.event_num_max.text = "${currentEvent.num_part} / ${currentEvent.max_part}"
            //holder.card_view_num_max.setCardBackgroundColor(R.color.green)
            //holder.card_view_num_max.background.setTint(R.color.green)
        }else{
            holder.event_num_max.text = "full"
            //????????????????????????????
            //holder.card_view_num_max.setBackgroundColor(R.color.red)
            //holder.card_view_num_max.background.setTint(R.color.red)

        }

        //imagesRef = storageRef.child(imagePath)
        //holder.event_image.setImageResource(R.drawable.ic_arrow_left)

        //carico la foto dallo storage
        imagesRef = storageRef.child("${currentEvent.eid}/images/event_image.png")
        val localfile = File.createTempFile("tempImage", "png")

        imagesRef.getFile(localfile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            holder.event_image.setImageBitmap(bitmap)
        }.addOnFailureListener{
            Toast.makeText(context, "Download failed...", Toast.LENGTH_SHORT).show()
        }


        holder.btn_like.setOnClickListener{

            //controllo se l'evento corrente appartiene ai favoriti
            if(user_favorites.contains("${currentEvent.eid}")){
                holder.btn_like.setImageResource(R.drawable.ic_favorite_border)
                user_favorites.remove("${currentEvent.eid}")

                val updatedUser = mapOf<String, Any?>(
                    "favorites" to user_favorites
                )
                //tolgo dai preferiti nel db
                db.child("user").child(auth.currentUser?.uid!!).updateChildren(updatedUser)

            }else{
                user_favorites.add("${currentEvent.eid}")
                holder.btn_like.setImageResource(R.drawable.ic_favorite)

                val updatedUser = mapOf<String, Any?>(
                    "favorites" to user_favorites
                )

                //aggiungo ai preferiti nel db
                db.child("user").child(auth.currentUser?.uid!!).updateChildren(updatedUser) //lo aggiungo al db
            }
        }

        holder.card_view.setOnClickListener{
            val activity = holder.itemView.context as Activity

            communicator = activity as Communicator
            communicator.passEvent(currentEvent)

            //println(activity.fragmentManager.findFragmentById(R.id.nav_container).toString())


            /*
            val intent = Intent(context, EventDetailsActivity::class.java)
            val activity = holder.itemView.context as Activity
            intent.putExtra("event",  currentEvent)
            intent.putExtra("creator", currentEvent.uid)

            context!!.startActivity(intent)
            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)*/
            //activity.finish()


        }
    }


    override fun getItemCount(): Int {
        return eventList.size
    }

}