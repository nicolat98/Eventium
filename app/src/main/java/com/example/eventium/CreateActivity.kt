package com.example.eventium

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class CreateActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{

    private lateinit var btn_close : Button
    private lateinit var btn_confirm_create : Button
    private lateinit var et_event_name : EditText
    //private lateinit var et_event_type : EditText
    //private lateinit var et_event_date : EditText
    private lateinit var tv_select_date : TextView
    private lateinit var tv_select_time : TextView
    private lateinit var et_event_location : EditText
    private lateinit var et_event_max_part : EditText
    private lateinit var et_event_description : EditText
    private lateinit var sp_event_type : Spinner
    private lateinit var imagePath : String
    private var image_added : Boolean = false

    //immagine
    private lateinit var ib_event_image : ImageButton
    val REQUEST_CODE = 100

    private var sameDay : Boolean =  false


    //firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference

    lateinit var imagesRef : StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        btn_close = findViewById(R.id.btn_close)
        btn_confirm_create = findViewById(R.id.btn_confirm_create)
        et_event_name = findViewById(R.id.et_event_name)
        //et_event_type = findViewById(R.id.et_event_type)
        //et_event_date = findViewById(R.id.et_event_date)
        et_event_location = findViewById(R.id.et_event_location)
        et_event_max_part = findViewById(R.id.et_event_max_part)
        sp_event_type = findViewById(R.id.sp_event_type)
        tv_select_date = findViewById(R.id.tv_select_date)
        tv_select_time = findViewById(R.id.tv_select_time)
        ib_event_image = findViewById(R.id.ib_event_image)
        et_event_description = findViewById(R.id.et_event_description)
        var eid = UUID.randomUUID().toString()
        //imagePath = "${auth.currentUser?.uid!!}/images/event_image.png"

        //firebase auth
        auth = FirebaseAuth.getInstance()
        //firebase storage
        val storage = Firebase.storage
        val storageRef = storage.reference
        imagesRef = storageRef.child("${eid}/images/event_image.png") //nome dell'immagine che inserisco, da modificare


        //creo l'adapter per il dropdown menu (spinner)
        ArrayAdapter.createFromResource(
            this,
            R.array.spinner_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            sp_event_type.adapter = adapter
        }
        sp_event_type.onItemSelectedListener = this


        //select date & time
        tv_select_date.setOnClickListener {
            clickDatePicker()
        }

        tv_select_time.setOnClickListener {
            clickTimePicker()
        }

        ib_event_image.setOnClickListener{
            //Toast.makeText(baseContext, "Image clicked", Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }



        btn_close.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_from_up, R.anim.slide_to_bottom)
            finish()
        }

        btn_confirm_create.setOnClickListener{
            val event_name = et_event_name.text.toString()
            val event_type = sp_event_type.selectedItem.toString()
            val event_date = tv_select_date.text.toString()
            val event_time = tv_select_time.text.toString()
            val event_location = et_event_location.text.toString()
            val event_max_part = et_event_max_part.text
            val event_description = et_event_description.text.toString()


            if(event_name.isEmpty() || event_type == resources.getStringArray(R.array.spinner_array)[0] || event_date.equals("Select date") || event_time.equals("Select time") || event_location.isEmpty() || !image_added || event_max_part.isEmpty() || event_description.isEmpty()){
                Toast.makeText(baseContext, "Empty text...", Toast.LENGTH_SHORT).show()
            }else{
                addEventToDB(event_name, event_type, event_date,event_time, event_location, event_max_part.toString().toInt(), event_description,eid,  auth.currentUser?.uid!! )
                Toast.makeText(baseContext, "Event created !!!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_up, R.anim.slide_to_bottom)
                finish()
            }
        }
    }



    private fun addEventToDB(eventName: String, eventType: String, eventDate: String, eventTime:String,  eventLocation: String,  eventMaxPart: Int, eventDescription : String, eid: String, uid:String) {
        db = FirebaseDatabase.getInstance().reference

        db.child("event").child(eid).setValue(Event(eventName, eventType, eventDate, eventTime, eventLocation, eventMaxPart,0,eventDescription, eid, uid)) //lo aggiungo al db
    }



    //metodi ereditati per lo spinner
    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
        parent!!.getItemAtPosition(pos)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }


    //date picker
    private fun clickDatePicker() {
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val day = myCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener{ view, selectedyear, selectedmonth, selecteddayofmonth ->
                //Toast.makeText(this, "$selecteddayofmonth/${selectedmonth+1}/$selectedyear", Toast.LENGTH_SHORT).show()
                val selDate = "$selecteddayofmonth/${selectedmonth+1}/$selectedyear"

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
                val selectedDate = sdf.parse(selDate)
                val selectedDateInDays = selectedDate.time/86_400_000 //from num of millisconds to days

                /*
                * N milliseconds
                * /1000 (seconds)
                * /60 (minutes)
                * /60 hours
                * /24 days
                * */

                val currentDateInMillis = System.currentTimeMillis()
                val currentDateInDays = currentDateInMillis/86_400_000
                val diffDates = selectedDateInDays - currentDateInDays +1

                if(diffDates.toInt().equals(0)) {
                    sameDay = true
                    tv_select_date.text = selDate
                    tv_select_time.text = "Select time"
                }else if(diffDates > 0){
                    tv_select_date.text = selDate
                    tv_select_time.text = "Select time"
                    sameDay = false
                }else{
                    Toast.makeText(this, "Date non valid...", Toast.LENGTH_SHORT).show()
                }
                //tv_select_time.text = diffDates.toString()
            },
            year,
            month,
            day
        ).show()
    }

    private fun clickTimePicker() {
        val myCalendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener{ view, hour, minute ->
                var selHour = "${String.format("%02d", hour)} : ${String.format("%02d", minute)}"
                //Toast.makeText(this, selHour, Toast.LENGTH_SHORT).show()
                if(sameDay){
                    var currentHour = myCalendar.get(Calendar.HOUR_OF_DAY)
                    var currentMinute = myCalendar.get(Calendar.MINUTE)
                    //Toast.makeText(this, "$currentHour : $currentMinute", Toast.LENGTH_SHORT).show()
                    if(hour < currentHour){
                        Toast.makeText(this, "Ora non valida", Toast.LENGTH_SHORT).show()
                    }else if(currentHour.equals(hour) && minute < currentMinute){
                        Toast.makeText(this, "Minuto non valido", Toast.LENGTH_SHORT).show()
                    }else{
                        tv_select_time.text = selHour
                    }
                }else{
                    tv_select_time.text = selHour
                }
            },
            myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            ib_event_image.setImageURI(data?.data)

            //save on firebase storage
            ib_event_image.isDrawingCacheEnabled = true
            ib_event_image.buildDrawingCache()
            val bitmap = (ib_event_image.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = imagesRef.putBytes(data)
            uploadTask.addOnFailureListener{
                Toast.makeText(this, "Upload failed...", Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                Toast.makeText(this, "Upload successful...", Toast.LENGTH_SHORT).show()
                image_added = true
            }
        }
    }
}