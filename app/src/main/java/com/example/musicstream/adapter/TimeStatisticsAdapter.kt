package com.example.musicstream.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.GroupedUsageStatisticsModel
import com.example.musicstream.R
import com.example.musicstream.models.UsageStatisticsModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeStatisticsAdapter(private var groupedStatisticsList: List<GroupedUsageStatisticsModel>) :
    RecyclerView.Adapter<TimeStatisticsAdapter.StatisticsViewHolder>() {

    class StatisticsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_statistics, parent, false)
        return StatisticsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticsViewHolder, position: Int) {
        val statistic = groupedStatisticsList[position]
        holder.dateTextView.text = statistic.date
        holder.timeTextView.text = "${statistic.totalUsageTime / 1000} giây"
    }

    override fun getItemCount(): Int = groupedStatisticsList.size

    fun submitList(newList: List<UsageStatisticsModel>) {
        groupedStatisticsList = newList
            .groupBy { model ->
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(model.timestamp))
            }
            .map { (date, models) ->
                GroupedUsageStatisticsModel(
                    date = date,
                    totalUsageTime = models.sumOf { it.usageTime }
                )
            }

        notifyDataSetChanged()
    }
}
