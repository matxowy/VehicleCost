package com.matxowy.vehiclecost.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.entity.Refuel
import com.matxowy.vehiclecost.data.db.entity.Repair
import com.matxowy.vehiclecost.databinding.HistoryFragmentBinding
import com.matxowy.vehiclecost.ui.history.adapters.RefuelAdapter
import com.matxowy.vehiclecost.ui.history.adapters.RepairAdapter
import com.matxowy.vehiclecost.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.history_fragment.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.history_fragment),
    RepairAdapter.OnRepairItemClickListener, RefuelAdapter.OnRefuelItemClickListener {

    private lateinit var binding: HistoryFragmentBinding

    //Animations
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }

    // Flag to know if fab is clicked or not
    private var clicked = false

    private val viewModel: HistoryViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = HistoryFragmentBinding.bind(view)

        //Radio group operation
        onRadioButtonCheckedChanged()

        //RecyclerView operations
        val refuelAdapter = RefuelAdapter(this)
        val repairAdapter = RepairAdapter(this)

        setAdapters(refuelAdapter, repairAdapter)
        setObservers(refuelAdapter, repairAdapter)

        //Swipe actions in RecyclerView
        addSwipeToDeleteActionForRefuelRecyclerView(refuelAdapter)
        addSwipeToDeleteActionForRepairRecyclerView(repairAdapter)

        //FAB operations
        binding.apply {
            fabAdd.setOnClickListener {
                onAddButtonClicked()
            }

            fabAddRefuel.setOnClickListener {
                viewModel.onAddNewRefuelClick()
            }

            fabAddRepair.setOnClickListener {
                viewModel.onAddNewRepairClick()
            }
        }

        // Navigation between screens
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.refuelAndRepairEvent.collect { event ->
                when(event) {
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToAddRefuelScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRefuelFragment(null, "Nowe tankowanie")
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToAddRepairScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRepairFragment(null, "Nowa naprawa")
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToEditRefuelScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRefuelFragment(event.refuel, "Edycja tankowania")
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToEditRepairScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRepairFragment(event.repair, "Edycja naprawy")
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowUndoDeleteRefuelMessage -> {
                        Snackbar.make(requireView(), "Usunięto", Snackbar.LENGTH_LONG)
                            .setAction("Przywróć") {
                                viewModel.onUndoDeleteRefuelClick(event.refuel)
                            }.show()
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowUndoDeleteRepairMessage -> {
                        Snackbar.make(requireView(), "Usunięto", Snackbar.LENGTH_LONG)
                            .setAction("Przywróć") {
                                viewModel.onUndoDeleteRepairClick(event.repair)
                            }.show()
                    }
                }.exhaustive
            }
        }
    }

    override fun onRefuelItemClick(refuel: Refuel) {
        viewModel.onRefuelItemSelected(refuel)
    }

    override fun onRepairItemClick(repair: Repair) {
        viewModel.onRepairItemSelected(repair)
    }

    private fun addSwipeToDeleteActionForRepairRecyclerView(repairAdapter: RepairAdapter) {
        binding.apply {
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val repair = repairAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onRepairSwiped(repair)
                }
            }).attachToRecyclerView(recyclerViewRepair)
        }
    }

    private fun addSwipeToDeleteActionForRefuelRecyclerView(refuelAdapter: RefuelAdapter) {
        binding.apply {
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val refuel = refuelAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onRefuelSwiped(refuel)
                }
            }).attachToRecyclerView(recyclerViewRefuel)
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