package com.example.bstatus

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


data class User(
    val displayName: String = "",
    val status: String = ""
)

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "Main Activity"
    }
    private lateinit var auth: FirebaseAuth

    private val db = Firebase.firestore

    var notificationReceiver : NotificationReceiver? = null

    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.extras?.getString(MyFirebaseMessagingService.NOTIFICATION_MESSAGE)?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        val query = db.collection("users")
        val options = FirestoreRecyclerOptions.Builder<User>().setQuery(query, User::class.java)
            .setLifecycleOwner(this).build()
        val adapter = object: FirestoreRecyclerAdapter<User, UserViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val view = LayoutInflater.from(this@MainActivity)
                    .inflate(android.R.layout.simple_list_item_2, parent, false)
                return UserViewHolder(view)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                val tvName: TextView = holder.itemView.findViewById(android.R.id.text1)
                val tvStatus: TextView = holder.itemView.findViewById(android.R.id.text2)
                tvName.text = model.displayName
                tvStatus.text = model.status
            }
        }
        val rvUsers = findViewById<View>(R.id.rvUsers) as RecyclerView
        rvUsers.adapter = adapter
        rvUsers.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miLogout){
            Log.i(TAG, "Logout")
            //logout the user
            auth.signOut()
            val logoutIntent = Intent(this, LoginActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        } else if (item.itemId == R.id.miEdit){
            Log.i(TAG, "Show alert dialog to edit status")
            showAlertDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAlertDialog() {
        val editText = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Update your status")
            .setView(editText)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
            Log.i(TAG, "Clicked on positive button!")
            val statusEntered = editText.text.toString()
            if (statusEntered.isBlank()){
                Toast.makeText(this, "Cannot submit empty text", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentUser = auth.currentUser
            if (currentUser == null){
                Toast.makeText(this, "User isn't signed in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update firestore with new status
            db.collection("users").document(currentUser.uid)
                .update("status", statusEntered)
            dialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        val user = hashMapOf(
            "online" to true,
            "date" to Timestamp(Date()),
        )
        db.collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .set(user)

        notificationReceiver = NotificationReceiver()
        this.registerReceiver(notificationReceiver, IntentFilter(MyFirebaseMessagingService.BROADCAST_NET_NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()

        val user = hashMapOf(
            "online" to false,
            "date" to Timestamp(Date()),
        )
        db.collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .set(user)
        notificationReceiver?.let {
            this.unregisterReceiver(it)
        }
    }
}
