package com.matxowy.vehiclecost.ui.history.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.databinding.ItemRefuelBinding

class RefuelAdapter : ListAdapter<Refuel, RefuelAdapter.RefuelViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefuelViewHolder {
        val binding = ItemRefuelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RefuelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RefuelViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class RefuelViewHolder(private val binding: ItemRefuelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(refuel: Refuel) {
            binding.apply {
                tvAmountOfFuel.text = "${refuel.amountOfFuel.toString()} l"
                tvDate.text = refuel.date
                tvMileage.text = "${refuel.mileage.toString()} km"
                tvPrice.text = "${refuel.price.toString()} zł"
            }
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<Refuel>() {
        override fun areItemsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem == newItem
    }

}