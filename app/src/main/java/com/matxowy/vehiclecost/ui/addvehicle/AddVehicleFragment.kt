package com.matxowy.vehiclecost.ui.addvehicle

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
import com.matxowy.vehiclecost.databinding.AddVehicleFragmentBinding
import com.matxowy.vehiclecost.util.exhaustive
import com.matxowy.vehiclecost.util.hideKeyboard
import com.matxowy.vehiclecost.util.toIntOrDefaultValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddVehicleFragment : Fragment(R.layout.add_vehicle_fragment) {

    private val viewModel: AddVehicleViewModel by viewModels()
    private var _binding: AddVehicleFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddVehicleFragmentBinding.bind(view)

        setListeners()
        handleAddVehicleEvents()
    }

    private fun setListeners() {
        binding.apply {
            etVehicleName.addTextChangedListener {
                viewModel.vehicleName = it.toString()
            }

            etMileage.addTextChangedListener {
                viewModel.vehicleMileage = it.toString().toIntOrDefaultValue()
            }

            btnAddVehicle.setOnClickListener {
                viewModel.onAddVehicleButtonClick()
            }
        }
    }

    private fun handleAddVehicleEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addVehicleEvent.collect { event ->
                when (event) {
                    is AddVehicleViewModel.AddVehicleEvent.NavigateToStatisticsWithResult -> {
                        setFragmentResult(
                            requestKey = ADD_VEHICLE_REQUEST,
                            result = bundleOf(ADD_VEHICLE_RESULT to event.result)
                        )
                        val action = AddVehicleFragmentDirections.actionAddVehicleFragmentToStatisticsFragment()
                        findNavController().navigate(action)
                    }
                    is AddVehicleViewModel.AddVehicleEvent.ShowInvalidDataMessage -> {
                        hideKeyboard()
                        Snackbar.make(requireView(), R.string.required_fields_cannot_be_empty_text, Snackbar.LENGTH_LONG).show()
                    }
                    is AddVehicleViewModel.AddVehicleEvent.ShowAddErrorMessage -> {
                        hideKeyboard()
                        Snackbar.make(requireView(), R.string.insert_edit_vehicle_error_message, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ADD_VEHICLE_REQUEST = "add_vehicle_request"
        const val ADD_VEHICLE_RESULT = "add_vehicle_result"
    }
}