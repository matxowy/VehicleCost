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
import com.matxowy.vehiclecost.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditVehicleFragment : Fragment(R.layout.add_edit_vehicle_fragment) {

    private val viewModel: AddEditVehicleViewModel by viewModels()
    private var _binding: AddEditVehicleFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddEditVehicleFragmentBinding.bind(view)

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
                            requestKey = ADD_EDIT_VEHICLE_REQUEST,
                            result = bundleOf(ADD_EDIT_VEHICLE_RESULT to event.result)
                        )
                        val action = AddEditVehicleFragmentDirections.actionAddEditVehicleFragmentToStatisticsFragment()
                        findNavController().navigate(action)
                    }
                    is AddEditVehicleViewModel.AddEditVehicleEvent.ShowInvalidDataMessage -> {
                        hideKeyboard()
                        Snackbar.make(requireView(), R.string.required_fields_cannot_be_empty_text, Snackbar.LENGTH_LONG).show()
                    }
                    AddEditVehicleViewModel.AddEditVehicleEvent.ShowAddErrorMessage -> {
                        hideKeyboard()
                        Snackbar.make(requireView(), R.string.insert_new_vehicle_error_message, Snackbar.LENGTH_LONG).show()
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
        const val ADD_EDIT_VEHICLE_REQUEST = "add_edit_vehicle_request"
        const val ADD_EDIT_VEHICLE_RESULT = "add_edit_vehicle_result"
    }
}