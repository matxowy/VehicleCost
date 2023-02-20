package com.matxowy.vehiclecost.ui.history.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.databinding.ItemRefuelBinding
import com.matxowy.vehiclecost.util.addSpace
import com.matxowy.vehiclecost.util.decimalFormat

class RefuelAdapter(private val listener: OnRefuelItemClickListener) :
    ListAdapter<Refuel, RefuelAdapter.RefuelViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RefuelViewHolder {
        val binding = ItemRefuelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RefuelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RefuelViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class RefuelViewHolder(private val binding: ItemRefuelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val refuel = getItem(position)
                        listener.onRefuelItemClick(refuel)
                    }
                }
            }
        }

        fun bind(refuel: Refuel) {
            binding.apply {
                tvAmountOfFuel.text = "${refuel.amountOfFuel.decimalFormat()} l"
                tvDate.text = refuel.date
                tvMileage.text = "${refuel.mileage.addSpace()} km"
                tvPrice.text = "${refuel.price.decimalFormat()} zł"
            }
        }

    }

    interface OnRefuelItemClickListener {
        fun onRefuelItemClick(refuel: Refuel)
    }

    class DiffCallback : DiffUtil.ItemCallback<Refuel>() {
        override fun areItemsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Refuel, newItem: Refuel) =
            oldItem == newItem
    }

}