package com.example.wheretime

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wheretime.data.Entry

sealed class HistoryListItem {
    data class DateHeader(val label: String) : HistoryListItem()
    data class EntryRow(val entry: Entry) : HistoryListItem()
}

class HistoryAdapter(
    private val items: List<HistoryListItem>,
    private val onDeleteClick: (Entry) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ENTRY = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view as TextView
    }

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val entryText: TextView = view.findViewById(R.id.entryText)
        val deleteButton: android.widget.Button = view.findViewById(R.id.deleteButton)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryListItem.DateHeader -> VIEW_TYPE_HEADER
            is HistoryListItem.EntryRow -> VIEW_TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_HEADER) {
            HeaderViewHolder(inflater.inflate(R.layout.item_history_header, parent, false))
        } else {
            EntryViewHolder(inflater.inflate(R.layout.item_history_entry, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryListItem.DateHeader -> {
                (holder as HeaderViewHolder).label.text = item.label
            }
            is HistoryListItem.EntryRow -> {
                val entryHolder = holder as EntryViewHolder
                val entry = item.entry
                entryHolder.entryText.text =
                    "${entry.subcategory.displayName} - ${entry.hours}h ${entry.minutes}m"
                entryHolder.deleteButton.setOnClickListener {
                    onDeleteClick(entry)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}