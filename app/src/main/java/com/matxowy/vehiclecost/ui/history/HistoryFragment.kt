package com.matxowy.vehiclecost.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.HistoryFragmentBinding
import com.matxowy.vehiclecost.ui.history.adapters.RefuelAdapter
import com.matxowy.vehiclecost.ui.history.adapters.RepairAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.history_fragment.*

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.history_fragment) {

    private lateinit var binding: HistoryFragmentBinding

    //Animations
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }

    private var clicked = false

    private val viewModel: HistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = HistoryFragmentBinding.bind(view)

        //Radio group operation
        onRadioButtonCheckedChanged()

        //RecyclerView operations
        val refuelAdapter = RefuelAdapter()
        val repairAdapter = RepairAdapter()

        setAdapters(refuelAdapter, repairAdapter)
        setObservers(refuelAdapter, repairAdapter)


        //FAB operations
        binding.apply {
            fabAdd.setOnClickListener {
                onAddButtonClicked()
            }

            fabAddRefuel.setOnClickListener {
                Toast.makeText(context, "Add refuel clicked", Toast.LENGTH_SHORT).show()
            }

            fabAddRepair.setOnClickListener {
                Toast.makeText(context, "Add repair clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Functions managing RecyclerView
    private fun setObservers(
        refuelAdapter: RefuelAdapter,
        repairAdapter: RepairAdapter
    ) {
        viewModel.refuels.observe(viewLifecycleOwner) {
            binding.groupLoading.visibility = View.GONE
            refuelAdapter.submitList(it)
        }

        viewModel.repairs.observe(viewLifecycleOwner) {
            binding.groupLoading.visibility = View.GONE
            repairAdapter.submitList(it)
        }
    }

    private fun setAdapters(
        refuelAdapter: RefuelAdapter,
        repairAdapter: RepairAdapter
    ) {

        binding.apply {
            recyclerViewRefuel.apply {
                adapter = refuelAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            recyclerViewRepair.apply {
                adapter = repairAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }

    }

    // Function managing RadioButtons
    private fun onRadioButtonCheckedChanged() {
        binding.rgHistory.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_fuel_history -> {
                    recyclerView_repair.visibility = View.GONE
                    recyclerView_refuel.visibility = View.VISIBLE
                }
                R.id.rb_repair_history -> {
                    recyclerView_repair.visibility = View.VISIBLE
                    recyclerView_refuel.visibility = View.GONE
                }
            }
        }
    }

    // Functions managing animations of fab
    private fun onAddButtonClicked() {
        setVisibility()
        setAnimation()
        setClickable()
        clicked = !clicked
    }

    private fun setVisibility() {
        if (!clicked) {
            binding.apply {
                fabAddRepair.visibility = View.VISIBLE
                fabAddRefuel.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                fabAddRepair.visibility = View.GONE
                fabAddRefuel.visibility = View.GONE
            }
        }
    }

    private fun setAnimation() {
        if (!clicked) {
            binding.apply {
                fabAddRepair.startAnimation(fromBottom)
                fabAddRefuel.startAnimation(fromBottom)
                fabAdd.startAnimation(rotateOpen)
            }
        } else {
            binding.apply {
                fabAddRepair.startAnimation(toBottom)
                fabAddRefuel.startAnimation(toBottom)
                fabAdd.startAnimation(rotateClose)
            }
        }
    }

    private fun setClickable() {
        if (!clicked) {
            binding.apply {
                fabAddRefuel.isClickable = true
                fabAddRepair.isClickable = true
            }
        } else {
            binding.apply {
                fabAddRefuel.isClickable = false
                fabAddRepair.isClickable = false
            }
        }
    }

}