package com.aslanovaslan.kotlinmessenger.recycleritem

import android.content.Context
import com.aslanovaslan.kotlinmessenger.R
import com.aslanovaslan.kotlinmessenger.glide.GlideApp
import com.aslanovaslan.kotlinmessenger.internal.StorageUtil
import com.aslanovaslan.kotlinmessenger.model.User
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.newmessage_user_row.view.*

class UserItem(var user: User, val context: Context) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
           itemView.textViewNewMessageUsername.text = user.username
            GlideApp.with(context).load(user.profilePicturePath?.let {
                StorageUtil.pathToReference(
                    it
                )
            }).centerCrop()
                .placeholder(R.drawable.ic_fire_emoji)
                .into(itemView.imageViewNewMessageProfile)
            if (!user.userBio.isNullOrEmpty() && !user.userBio.isNullOrBlank()) {
                itemView.textView_bio.text = user.userBio
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.newmessage_user_row
    }

}