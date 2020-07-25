package com.aslanovaslan.kotlinmessenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.internal.ProgressFragment
import com.aslanovaslan.kotlinmessenger.model.ChatMessage
import com.aslanovaslan.kotlinmessenger.model.User
import com.aslanovaslan.kotlinmessenger.recycleritem.LatestMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlin.math.log

class LatestMessageActivity : AppCompatActivity() {
    var groupAdapter = GroupAdapter<GroupieViewHolder>()
    val latestMessageMap = HashMap<String, ChatMessage>()
    private lateinit var progressBar: ProgressFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)
        isUserLoginOrNull()
        fetchCurrentUser()
        fetchLastMessageFireBase()
        createProgressBar()
        recyclerViewLatestMessage.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(this@LatestMessageActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@LatestMessageActivity,
                    DividerItemDecoration.VERTICAL
                )
            )

            groupAdapter.setOnItemClickListener {item, view ->
                val intent=Intent(this@LatestMessageActivity,ChatLog::class.java)
                val row=item as LatestMessageItem
                intent.putExtra(NewMessageActivity.USER_DATA,row.partnerUserItem)
                startActivity(intent)
                Log.d(TAG, "onCreate: $item")
            }
        }
    }

    private fun fetchLastMessageFireBase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val lastMessage = Firebase.database.getReference("latest-messages").child(currentUserId)
        lastMessage.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val messageItem = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = messageItem
                refreshRecyclerViewMessages()
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val messageItem = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessageMap[snapshot.key!!] = messageItem
                refreshRecyclerViewMessages()
                cancelProgressBar()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

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

    private fun refreshRecyclerViewMessages() {
        groupAdapter.clear()
        latestMessageMap.values.forEach {
            groupAdapter.add(
                LatestMessageItem(
                    it
                )
            )
        }
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().getReference("users").child(uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                CURRENT_USER = snapshot.getValue(User::class.java)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK))
                startActivity(intent)
                finish()
            }
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun isUserLoginOrNull() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK))
            startActivity(intent)
        }
    }

    companion object {
        var CURRENT_USER: User? = null
        private const val TAG = "LatestMessageActivity"
    }
}