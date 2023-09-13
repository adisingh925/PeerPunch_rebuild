package app.adreal.android.peerpunch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.adreal.android.peerpunch.R
import app.adreal.android.peerpunch.model.Data

class ChatAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messageList = ArrayList<Data>()

    private val senderView = 1

    private val receiverView = 2
    private inner class ViewHolder1(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var senderTextView: TextView = itemView.findViewById(R.id.senderMessage)

        fun bind(position: Int) {
            senderTextView.text = messageList[position].message
        }
    }

    private inner class ViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val receiverTextView: TextView = itemView.findViewById(R.id.receiverMessage)

        fun bind(position: Int) {
            receiverTextView.text = messageList[position].message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> return ViewHolder1(
                LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false)
            )
            2 -> return ViewHolder2(
                LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false)
            )
        }

        return ViewHolder2(LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (messageList[position].isReceived) {
            0 -> senderView
            1 -> receiverView
            else -> senderView
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (messageList[position].isReceived) {
            0 -> (holder as ViewHolder1).bind(position)
            1 -> (holder as ViewHolder2).bind(position)
        }
    }

    fun setData(data: List<Data>) {
        this.messageList = data as ArrayList<Data>
        notifyItemInserted(messageList.size)
    }
}