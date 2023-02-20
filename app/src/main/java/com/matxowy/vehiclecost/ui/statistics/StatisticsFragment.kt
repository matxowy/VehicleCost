package com.matxowy.vehiclecost.ui.statistics

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.data.db.entity.Vehicle
import com.matxowy.vehiclecost.databinding.StatisticsFragmentBinding
import com.matxowy.vehiclecost.ui.addvehicle.AddVehicleFragment.Companion.ADD_VEHICLE_REQUEST
import com.matxowy.vehiclecost.ui.addvehicle.AddVehicleFragment.Companion.ADD_VEHICLE_RESULT
import com.matxowy.vehiclecost.util.decimalFormat
import com.matxowy.vehiclecost.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.statistics_fragment) {

    private val viewModel: StatisticsViewModel by viewModels()
    private var _binding: StatisticsFragmentBinding? = null
    private val binding get() = _binding!!

    //Animations
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }

    private var clicked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = StatisticsFragmentBinding.bind(view)

        addTopMenu()
        addObservers()
        setListeners()
        handleStatisticsEvents()
        setFragmentResultListeners()
    }

    private fun addTopMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.top_menu_statistics_fragment, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_manage_vehicles -> {
                        val action =
                            StatisticsFragmentDirections.actionStatisticsFragmentToManageVehicleFragment(getString(R.string.title_manage_vehicle))
                        findNavController().navigate(action)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setFragmentResultListeners() {
        setFragmentResultListener(ADD_VEHICLE_REQUEST) { _, bundle ->
            val result = bundle.getInt(ADD_VEHICLE_RESULT)
            viewModel.onAddVehicleResult(result)
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
                    is StatisticsViewModel.StatisticsEvent.NavigateToAddVehicleScreen -> {
                        val action = StatisticsFragmentDirections.actionStatisticsFragmentToAddVehicleFragment(
                            title = getString(R.string.title_new_vehicle)
                        )
                        findNavController().navigate(action)
                    }
                    is StatisticsViewModel.StatisticsEvent.ShowVehicleSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), getString(R.string.vehicle_added_message), Snackbar.LENGTH_LONG).show()
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
            spinnerVehicle.setOnClickListener {
                if (clicked) onAddButtonClicked()
            }
        }
    }

    private fun addObservers() {
        viewModel.sumOfFuelAmount.observe(viewLifecycleOwner) {
            binding.tvRefueledStat.text = getString(R.string.numerical_value_with_unit_of_volume, it?.decimalFormat() ?: "0", UNIT_OF_VOLUME)
        }

        viewModel.sumCostsOfRefuels.observe(viewLifecycleOwner) {
            binding.tvFuelCostsStat.text = getString(R.string.numerical_value_with_currency, it?.decimalFormat() ?: "0", CURRENCY)
        }

        viewModel.lastPriceOfFuel.observe(viewLifecycleOwner) {
            binding.tvLastFuelPriceStat.text = getString(R.string.numerical_value_with_currency, it?.decimalFormat() ?: "0", CURRENCY)
        }

        viewModel.sumCostOfRepair.observe(viewLifecycleOwner) {
            binding.tvSumCostRepairStat.text = getString(R.string.numerical_value_with_currency, it?.decimalFormat() ?: "0", CURRENCY)
        }

        viewModel.maxCostOfRepair.observe(viewLifecycleOwner) {
            binding.tvGreatestCostRepairStat.text = getString(R.string.numerical_value_with_currency, it?.decimalFormat() ?: "0", CURRENCY)
        }

        viewModel.lastCostOfRepair.observe(viewLifecycleOwner) {
            binding.tvLatestCostRepairStat.text = getString(R.string.numerical_value_with_currency, it?.decimalFormat() ?: "0", CURRENCY)
        }

        viewModel.vehiclesNames.observe(viewLifecycleOwner) { listOfVehicles ->
            val listOfVehiclesWithAddingOption = viewModel.getListOfVehiclesNamesWithAddingOptionOnFirstPosition(
                listOfVehiclesNames = listOfVehicles,
                addNewVehicleText = getString(R.string.add_new_vehicle_text)
            )
            setSpinner(requireContext(), listOfVehiclesWithAddingOption)
            setSpinnerOnCurrentSelectedVehicle(listOfVehiclesWithAddingOption)
        }
    }

    private fun setSpinner(context: Context, listOfVehicles: List<Vehicle>) {
        val spinnerAdapter = ArrayAdapter(context, R.layout.spinner_item_list, listOfVehicles)
        binding.spinnerVehicle.setAdapter(spinnerAdapter)
        binding.spinnerVehicle.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                viewModel.onAddNewVehicleClick()
            } else {
                viewModel.saveSelectedVehicleAndRefreshStatistics(position, listOfVehicles[position].vehicleId)
            }
        }
    }

    private fun setSpinnerOnCurrentSelectedVehicle(listOfVehicles: List<Vehicle>) {
        val selectedVehiclePosition = viewModel.getSelectedVehiclePosition()
        try {
            binding.spinnerVehicle.setText(listOfVehicles[selectedVehiclePosition].name, false)
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
                clMain.alpha = SEMI_TRANSPARENT
            }
        } else {
            binding.apply {
                fabAddRepair.visibility = View.GONE
                fabAddRefuel.visibility = View.GONE
                clMain.alpha = OPAQUE
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

    companion object {
        const val UNIT_OF_VOLUME = "l"
        const val CURRENCY = "zł"
        const val SEMI_TRANSPARENT = 0.5F
        const val OPAQUE = 1F
    }
}