package com.matxowy.vehiclecost.ui.history

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelFragment.Companion.ADD_EDIT_REFUEL_REQUEST
import com.matxowy.vehiclecost.ui.addeditrefuel.AddEditRefuelFragment.Companion.ADD_EDIT_REFUEL_RESULT
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairFragment.Companion.ADD_EDIT_REPAIR_REQUEST
import com.matxowy.vehiclecost.ui.addeditrepair.AddEditRepairFragment.Companion.ADD_EDIT_REPAIR_RESULT
import com.matxowy.vehiclecost.ui.history.adapters.RefuelAdapter
import com.matxowy.vehiclecost.ui.history.adapters.RepairAdapter
import com.matxowy.vehiclecost.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.history_fragment),
    RepairAdapter.OnRepairItemClickListener, RefuelAdapter.OnRefuelItemClickListener {

    private var _binding: HistoryFragmentBinding? = null
    private val binding get() = _binding!!

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

        // Disable back arrow in header for avoid returning to adding/editing screen by this
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        _binding = HistoryFragmentBinding.bind(view)

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
        setFabListeners()

        setFragmentResultListeners()

        // Navigation between screens
        handleRefuelAndRepairEvents()
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(ADD_EDIT_REPAIR_REQUEST) { _, bundle ->
            val result = bundle.getInt(ADD_EDIT_REPAIR_RESULT)
            viewModel.onAddEditRepairResult(result)
        }

        setFragmentResultListener(ADD_EDIT_REFUEL_REQUEST) { _, bundle ->
            val result = bundle.getInt(ADD_EDIT_REFUEL_RESULT)
            viewModel.onAddEditRefuelResult(result)
        }
    }

    private fun setFabListeners() {
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
    }

    private fun handleRefuelAndRepairEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.refuelAndRepairEvent.collect { event ->
                when (event) {
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToAddRefuelScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRefuelFragment(
                            null,
                            getString(R.string.title_new_refuel)
                        )
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToAddRepairScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRepairFragment(
                            null,
                            getString(R.string.title_new_repair)
                        )
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToEditRefuelScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRefuelFragment(
                            event.refuel,
                            getString(R.string.title_edit_refuel)
                        )
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.NavigateToEditRepairScreen -> {
                        val action = HistoryFragmentDirections.actionHistoryFragmentToAddEditRepairFragment(
                            event.repair,
                            getString(R.string.title_edit_repair)
                        )
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowUndoDeleteRefuelMessage -> {
                        Snackbar.make(requireView(), getString(R.string.removed_text), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.restore_text)) {
                                viewModel.onUndoDeleteRefuelClick(event.refuel)
                            }.show()
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowUndoDeleteRepairMessage -> {
                        Snackbar.make(requireView(), getString(R.string.removed_text), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.restore_text)) {
                                viewModel.onUndoDeleteRepairClick(event.repair)
                            }.show()
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowRefuelSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.refueled_added_message), Snackbar.LENGTH_SHORT).show()
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowRepairSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.repair_added_message), Snackbar.LENGTH_SHORT).show()
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowRefuelEditedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.refueled_updated_message), Snackbar.LENGTH_SHORT).show()
                    }
                    is HistoryViewModel.RefuelAndRepairEvent.ShowRepairEditedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.repair_updated_message), Snackbar.LENGTH_SHORT).show()
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
                    binding.apply {
                        recyclerViewRepair.visibility = View.GONE
                        recyclerViewRefuel.visibility = View.VISIBLE
                    }
                }
                R.id.rb_repair_history -> {
                    binding.apply {
                        recyclerViewRepair.visibility = View.VISIBLE
                        recyclerViewRefuel.visibility = View.GONE
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}