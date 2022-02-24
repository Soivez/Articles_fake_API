package com.example.fakeapi

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val texts: ArrayList<Element>,
    private val onClickButton: (Element) -> Unit
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    class UserViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val nameView: TextView = root.findViewById(R.id.name)
        private val phoneNumberView: TextView = root.findViewById(R.id.text)
        var deleteButton: Button = root.findViewById(R.id.sms_button)

        fun bind(user: Element, button : Button) {
            nameView.text = user.title
            phoneNumberView.text = user.body
            deleteButton = button
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val holder = UserViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.list_item, parent, false)
        )
        holder.deleteButton.setOnClickListener {
            if (holder.absoluteAdapterPosition != -1) {
                onClickButton(texts[holder.absoluteAdapterPosition])
            }
        }
        return holder
    }

    fun setData(newPosts : ArrayList<Element>) {
        val diffResult = DiffUtil.calculateDiff(
            ProductDiffUtilCallBack(texts, newPosts)
        )
        texts.clear()
        texts.addAll(newPosts)
        diffResult.dispatchUpdatesTo(this)
    }


    fun deleteItem(e : Element) {
        val list = ArrayList(texts)
        if (list.remove(e)) {
            setData(list)
        }
    }

    fun addElement(e : Element) {
        val list = ArrayList(texts)
        list.add(e)
        setData(list)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) = holder.bind(texts[position], holder.deleteButton)
    override fun getItemCount() = texts.size
}