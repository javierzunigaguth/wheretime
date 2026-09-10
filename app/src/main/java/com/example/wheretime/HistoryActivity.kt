package com.example.wheretime

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wheretime.data.AppDatabase
import com.example.wheretime.data.Entry
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyStateText = findViewById(R.id.emptyStateText)

        val repository = AppDatabase.getInstance(applicationContext).entryRepository()

        lifecycleScope.launch {
            repository.getAllEntries().collectLatest { entries ->
                displayEntries(entries, repository)
            }
        }
    }

    private fun displayEntries(
        entries: List<Entry>,
        repository: com.example.wheretime.data.EntryRepository
    ) {
        if (entries.isEmpty()) {
            recyclerView.visibility = RecyclerView.GONE
            emptyStateText.visibility = TextView.VISIBLE
            return
        }

        recyclerView.visibility = RecyclerView.VISIBLE
        emptyStateText.visibility = TextView.GONE

        val grouped = entries.groupBy { it.date }

        val listItems = mutableListOf<HistoryListItem>()
        for ((date, entriesForDate) in grouped) {
            listItems.add(HistoryListItem.DateHeader(dateHeaderLabel(date)))
            entriesForDate.forEach { entry ->
                listItems.add(HistoryListItem.EntryRow(entry))
            }
        }

        recyclerView.adapter = HistoryAdapter(listItems) { entryToDelete ->
            lifecycleScope.launch {
                repository.delete(entryToDelete)
            }
        }
    }

    private fun dateHeaderLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
        }
    }
}