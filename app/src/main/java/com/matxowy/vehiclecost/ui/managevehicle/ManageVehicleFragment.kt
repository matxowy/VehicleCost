package com.matxowy.vehiclecost.ui.managevehicle

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.ManageVehiclesFragmentBinding
import com.matxowy.vehiclecost.util.exhaustive
import com.matxowy.vehiclecost.util.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageVehicleFragment : Fragment(R.layout.manage_vehicles_fragment) {

    private val viewModel: ManageVehicleViewModel by viewModels()
    private var _binding: ManageVehiclesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ManageVehiclesFragmentBinding.bind(view)

        setObservers()
        setListeners()
        handleManageVehicleEvents()

    }

    private fun handleManageVehicleEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.manageVehicleEvent.collect { event ->
                when (event) {
                    is ManageVehicleViewModel.ManageVehicleEvents.SetFieldsWithData -> {
                        setFieldsWithData()
                    }
                    is ManageVehicleViewModel.ManageVehicleEvents.ShowDeleteConfirmMessageWithUndoOption -> {
                        hideKeyboard()
                        hideFields()
                        resetSpinnerChoose()
                        Snackbar.make(requireView(), getString(R.string.removed_text), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.restore_text)) {
                                viewModel.onUndoDeleteVehicleClick(event.vehicle)
                            }.show()
                    }
                    ManageVehicleViewModel.ManageVehicleEvents.ShowUpdateConfirmMessage -> {
                        hideKeyboard()
                        hideFields()
                        resetSpinnerChoose()
                        Snackbar.make(requireView(), R.string.vehicle_updated_message, Snackbar.LENGTH_LONG).show()
                    }
                    ManageVehicleViewModel.ManageVehicleEvents.ShowCannotDeleteCurrentSelectedVehicleMessage -> {
                        hideKeyboard()
                        Snackbar.make(requireView(), R.string.delete_current_selected_vehicle_error_message, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    private fun resetSpinnerChoose() {
        binding.apply {
            spinnerVehicle.text.clear()
            spinnerVehicle.clearFocus()
        }
    }

    private fun hideFields() {
        binding.apply {
            llManagingVehicle.visibility = View.GONE
        }
    }

    private fun setObservers() {
        viewModel.vehiclesNames.observe(viewLifecycleOwner) { listOfVehiclesNames ->
            setSpinner(requireContext(), listOfVehiclesNames)
        }
    }

    private fun setListeners() {
        binding.apply {
            etVehicleName.addTextChangedListener {
                viewModel.vehicleName = it.toString()
            }

            etMileage.addTextChangedListener {
                viewModel.vehicleMileage = it.toString()
            }

            spinnerVehicle.setOnItemClickListener { _, _, _, _ ->
                binding.llManagingVehicle.visibility = View.VISIBLE
                viewModel.onSelectVehicle(spinnerVehicle.text.toString())
            }

            btnEditVehicle.setOnClickListener {
                viewModel.onEditVehicleButtonClick(spinnerVehicle.text.toString())
            }

            btnDeleteVehicle.setOnClickListener {
                viewModel.onDeleteVehicleButtonClick(spinnerVehicle.text.toString())
            }
        }
    }

    private fun setFieldsWithData() {
        binding.apply {
            etVehicleName.setText(viewModel.vehicleName)
            etMileage.setText(viewModel.vehicleMileage.toString())
        }
    }

    private fun setSpinner(context: Context, listOfVehiclesNames: List<String>) {
        val spinnerAdapter = ArrayAdapter(context, R.layout.spinner_item_list, listOfVehiclesNames)
        binding.spinnerVehicle.setAdapter(spinnerAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}