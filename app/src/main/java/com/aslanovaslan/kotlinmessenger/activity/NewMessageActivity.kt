package com.aslanovaslan.kotlinmessenger.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.ProgressFragment
import com.aslanovaslan.kotlinmessenger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.newmessage_user_row.*

class NewMessageActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Select User"
        fetchUserFiresafe()
        createProgressBar()



        Log.d(TAG, "onCreate: ${recyclerViewNewMessage.childCount}")
    }

    private fun fetchUserFiresafe() {

        val databaseRef = Firebase.database.reference
        databaseRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}")
                cancelProgressBar()

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        Log.d(TAG, "onDataChange: $it")
                        val singleUser = it.getValue(User::class.java)
                        if (singleUser != null && FirebaseAuth.getInstance().currentUser!!.uid != singleUser.userId) {
                            adapter.add(
                                UserItem(singleUser, this@NewMessageActivity)
                            )
                            cancelProgressBar()
                        }
                    }
                    adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem
                        val intent = Intent(this@NewMessageActivity, ChatLog::class.java)
                        intent.putExtra(USER_DATA, userItem.user)
                        startActivity(intent)
                        finish()
                    }
                    recyclerViewNewMessage.adapter = adapter
                }
            }

        })
    }

    private fun createProgressBar() {
        progressBar = ProgressFragment()
        progressBar.show(supportFragmentManager, "mainDialog")
        progressBar.isCancelable = false
    }

    private fun cancelProgressBar() {
        progressBar.dismiss()
    }

    companion object {
        private const val TAG = "NewMessageActivity"
        const val USER_DATA = "USER_DATA"
    }
}

//val user: User
class UserItem(var user: User, val context: Context) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            textViewNewMessageUsername.text = user.username
            GlideApp.with(context).load(user.profilePicturePath).centerCrop()
                .placeholder(R.drawable.ic_fire_emoji)
                .into(imageViewNewMessageProfile)
            if (!user.userBio.isNullOrEmpty() && !user.userBio.isNullOrBlank()) {
                textView_bio.text = user.userBio
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.newmessage_user_row
    }

}
