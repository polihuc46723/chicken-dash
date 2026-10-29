package com.Kelzuni2.Vorqet.presentation.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.Kelzuni2.Vorqet.R
import com.Kelzuni2.Vorqet.core.ui.SpriteCatalog
import com.Kelzuni2.Vorqet.databinding.ItemSessionBinding
import com.Kelzuni2.Vorqet.domain.model.SessionRecord

class SessionHistoryAdapter : RecyclerView.Adapter<SessionHistoryAdapter.SessionViewHolder>() {

    private val records = mutableListOf<SessionRecord>()

    fun submit(items: List<SessionRecord>) {
        records.clear()
        records.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    class SessionViewHolder(
        private val binding: ItemSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: SessionRecord) {
            val context = binding.root.context
            binding.itemSprite.setImageResource(SpriteCatalog.drawableAt(record.spriteIndex))
            binding.itemTitle.text = record.title
            binding.itemDetail.text = context.getString(
                R.string.history_row_detail,
                record.blooms,
                record.accuracy
            )
            binding.itemScore.text = context.getString(R.string.history_row_score, record.score)
            binding.root.contentDescription = record.title
        }
    }
}
