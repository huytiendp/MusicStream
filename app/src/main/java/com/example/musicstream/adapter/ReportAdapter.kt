package com.example.musicstream.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicstream.R
import com.example.musicstream.models.ReportModel

class ReportAdapter(private var reports: List<ReportModel>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songTitle: TextView = itemView.findViewById(R.id.song_title)
        val reason: TextView = itemView.findViewById(R.id.report_reason)
        val email: TextView = itemView.findViewById(R.id.user_email)
        val reportedAt: TextView = itemView.findViewById(R.id.reported_at)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.songTitle.text = "Bài hát: ${report.songTitle}"
        holder.reason.text = "Lý do: ${report.reason}"
        holder.email.text = "Email: ${report.email}"
        holder.reportedAt.text = "Thời gian: ${report.reportedAt}"
    }

    override fun getItemCount() = reports.size

    fun updateData(newReports: List<ReportModel>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
