package com.matxowy.vehiclecost.ui.history.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.databinding.ItemRepairBinding

class RepairAdapter : ListAdapter<Repair, RepairAdapter.RepairViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairViewHolder {
        val binding = ItemRepairBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RepairViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class RepairViewHolder(private val binding: ItemRepairBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(repair: Repair) {
            binding.apply {
                tvCost.text = "${repair.cost} zł"
                tvDate.text = repair.date
                tvMileage.text = "${repair.mileage} km"
                tvRepairTitle.text = repair.title
            }
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Repair>() {
        override fun areItemsTheSame(oldItem: Repair, newItem: Repair) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Repair, newItem: Repair) =
            oldItem == newItem
    }

}