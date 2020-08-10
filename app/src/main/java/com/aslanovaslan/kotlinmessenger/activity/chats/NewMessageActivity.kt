package com.aslanovaslan.kotlinmessenger.activity.chats


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.internal.ProgressFragment
import com.aslanovaslan.kotlinmessenger.internal.internet.CheckNetworkState
import com.aslanovaslan.kotlinmessenger.model.User
import com.aslanovaslan.kotlinmessenger.recycleritem.UserItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessageActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Select User"
     
        if (CheckNetworkState(this).isOnline()){
            fetchUserDatabase()
            createProgressBar()
        } else{
            Snackbar.make(newMessageContainer,"Internet yoxdu ", Snackbar.LENGTH_LONG).show()
        }
    



        Log.d(TAG, "onCreate: ${recyclerViewNewMessage.childCount}")
    }

    private fun fetchUserDatabase() {

        val databaseRef = Firebase.database.reference
        databaseRef.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}")
                cancelProgressBar()

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val groupAdapter = GroupAdapter<GroupieViewHolder>()

                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        Log.d(TAG, "onDataChange: $it")
                        val singleUser = it.getValue(User::class.java)
                        if (singleUser != null && FirebaseAuth.getInstance().currentUser!!.uid != singleUser.userId) {
                            groupAdapter.add(
                                UserItem(
                                    singleUser,
                                    this@NewMessageActivity
                                )
                            )
                            cancelProgressBar()
                        }
                    }
                    groupAdapter.setOnItemClickListener { item, _ ->
                        val userItem = item as UserItem
                        val intent = Intent(this@NewMessageActivity, ChatLog::class.java)
                        intent.putExtra(USER_DATA, userItem.user)
                        startActivity(intent)
                        finish()
                    }
                    recyclerViewNewMessage.apply {
                        adapter = groupAdapter
                        setItemViewCacheSize(100)
                        setHasFixedSize(true)
                        addItemDecoration(
                            DividerItemDecoration(
                                this@NewMessageActivity,
                                DividerItemDecoration.VERTICAL
                            )

                        )
                    }
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

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        super.onPanelClosed(featureId, menu)
        cancelProgressBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelProgressBar()
    }
    override fun onPause() {
        super.onPause()
        cancelProgressBar()
    }
    override fun onBackPressed() {
        cancelProgressBar()
        super.onBackPressed()
      
    }
    companion object {
        private const val TAG = "NewMessageActivity"
        const val USER_DATA = "USER_DATA"
    }
}


