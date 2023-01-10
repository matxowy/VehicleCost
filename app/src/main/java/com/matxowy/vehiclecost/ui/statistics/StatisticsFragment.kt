package com.matxowy.vehiclecost.ui.statistics

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.StatisticsFragmentBinding
import com.matxowy.vehiclecost.util.decimalFormat
import com.matxowy.vehiclecost.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.statistics_fragment) {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var binding: StatisticsFragmentBinding

    //Animations
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }

    private var clicked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = StatisticsFragmentBinding.bind(view)

        addObservers()
        setListeners()
        handleStatisticsEvents()
        setFragmentResultListeners()
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener("add_edit_vehicle_request") { _, bundle ->
            val result = bundle.getInt("add_edit_vehicle_result")
            viewModel.onAddEditVehicleResult(result)
        }
    }

    private fun handleStatisticsEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.statisticsEvent.collect { event ->
                when (event) {
                    is StatisticsViewModel.StatisticsEvent.NavigateToAddRefuelScreen -> {
                        val action = StatisticsFragmentDirections.actionStatisticsFragmentToAddEditRefuelFragment(
                            refuel = null,
                            title = getString(R.string.title_new_refuel)
                        )
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is StatisticsViewModel.StatisticsEvent.NavigateToAddRepairScreen -> {
                        val action = StatisticsFragmentDirections.actionStatisticsFragmentToAddEditRepairFragment(
                            repair = null,
                            title = getString(R.string.title_new_repair)
                        )
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is StatisticsViewModel.StatisticsEvent.NavigateToAddEditVehicleScreen -> {
                        val action = StatisticsFragmentDirections.actionStatisticsFragmentToAddEditVehicleFragment(
                            title = getString(R.string.title_new_vehicle)
                        )
                        findNavController().navigate(action)
                    }
                    is StatisticsViewModel.StatisticsEvent.ShowVehicleEditedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.vehicle_added_message), Snackbar.LENGTH_LONG).show()
                    }
                    is StatisticsViewModel.StatisticsEvent.ShowVehicleSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.vehicle_updated_message), Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    private fun setListeners() {
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

            spinnerVehicle.setOnItemClickListener { _, _, position, _ ->
                if (position == 0) {
                    viewModel.onAddNewVehicleClick()
                } else {
                    viewModel.saveSelectedVehicleAndRefreshStatistics(position)
                }
            }
        }
    }

    private fun addObservers() {
        viewModel.sumOfFuelAmount.observe(viewLifecycleOwner) {
            binding.tvRefueledStat.text = "${it?.decimalFormat() ?: 0} l"
        }

        viewModel.sumCostsOfRefuels.observe(viewLifecycleOwner) {
            binding.tvFuelCostsStat.text = "${it?.decimalFormat() ?: 0} zł"
        }

        viewModel.lastPriceOfFuel.observe(viewLifecycleOwner) {
            binding.tvLastFuelPriceStat.text = "${it?.decimalFormat() ?: 0} zł"
        }

        viewModel.sumCostOfRepair.observe(viewLifecycleOwner) {
            binding.tvSumCostRepairStat.text = "${it?.decimalFormat() ?: 0} zł"
        }

        viewModel.maxCostOfRepair.observe(viewLifecycleOwner) {
            binding.tvGreatestCostRepairStat.text = "${it?.decimalFormat() ?: 0} zł"
        }

        viewModel.lastCostOfRepair.observe(viewLifecycleOwner) {
            binding.tvLatestCostRepairStat.text = "${it?.decimalFormat() ?: 0} zł"
        }

        viewModel.vehiclesNames.observe(viewLifecycleOwner) { listOfVehiclesNames ->
            val listOfVehiclesNamesWithAddingOption = viewModel.getListOfVehiclesNamesWithAddingOptionOnFirstPosition(
                listOfVehiclesNames = listOfVehiclesNames,
                addNewVehicleText = getString(R.string.add_new_vehicle_text)
            )
            setSpinner(requireContext(), listOfVehiclesNamesWithAddingOption)
            setSpinnerOnCurrentSelectedVehicle(listOfVehiclesNamesWithAddingOption)
        }
    }

    private fun setSpinner(context: Context, listOfVehiclesNames: List<String>) {
        val spinnerAdapter = ArrayAdapter(context, R.layout.spinner_item_list, listOfVehiclesNames)
        binding.spinnerVehicle.setAdapter(spinnerAdapter)
    }

    private fun setSpinnerOnCurrentSelectedVehicle(listOfVehiclesNames: List<String>) {
        val selectedVehiclePosition = viewModel.getSelectedVehicleId()
        try {
            binding.spinnerVehicle.setText(listOfVehiclesNames[selectedVehiclePosition], false)
        } catch (e: Exception) {
            Log.i("Spinner error: ", e.toString())
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