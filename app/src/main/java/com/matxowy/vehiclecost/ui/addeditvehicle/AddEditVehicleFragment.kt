package com.matxowy.vehiclecost.ui.addeditvehicle

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.AddEditVehicleFragmentBinding
import com.matxowy.vehiclecost.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditVehicleFragment : Fragment(R.layout.add_edit_vehicle_fragment) {

    private val viewModel: AddEditVehicleViewModel by viewModels()
    private lateinit var binding: AddEditVehicleFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddEditVehicleFragmentBinding.bind(view)

        setListeners()
        handleAddEditVehicleEvents()
    }

    private fun setListeners() {
        binding.apply {
            etVehicleName.addTextChangedListener {
                viewModel.vehicleName = it.toString()
            }

            etMileage.addTextChangedListener {
                viewModel.vehicleMileage = it.toString()
            }

            btnAddVehicle.setOnClickListener {
                viewModel.onAddVehicleButtonClick()
            }
        }
    }

    private fun handleAddEditVehicleEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditVehicleEvent.collect { event ->
                when (event) {
                    is AddEditVehicleViewModel.AddEditVehicleEvent.NavigateToStatisticsWithResult -> {
                        setFragmentResult(
                            "add_edit_vehicle_request",
                            bundleOf("add_edit_vehicle_result" to event.result)
                        )
                        val action = AddEditVehicleFragmentDirections.actionAddEditVehicleFragmentToStatisticsFragment()
                        findNavController().navigate(action)
                    }
                    is AddEditVehicleViewModel.AddEditVehicleEvent.ShowInvalidDataMessage -> {
                        Snackbar.make(requireView(), R.string.required_fields_cannot_be_empty_text, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }
}