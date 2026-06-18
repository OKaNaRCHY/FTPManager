package com.example.ftpmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.net.Uri

class UriAdapter(
    private val files: List<Pair<String, Uri>>,
    private val onClick: (String, Uri) -> Unit,
    private val onLongClick: (String, Uri) -> Unit
) : RecyclerView.Adapter<UriAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, uri) = files[position]
        holder.textView.text = name
        holder.itemView.setOnClickListener { onClick(name, uri) }
        holder.itemView.setOnLongClickListener {
            onLongClick(name, uri)
            true
        }
    }

    override fun getItemCount() = files.size
}
