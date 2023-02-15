package com.matxowy.vehiclecost.ui.addeditrepair

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
import com.matxowy.vehiclecost.databinding.AddEditRepairFragmentBinding
import com.matxowy.vehiclecost.util.*
import com.matxowy.vehiclecost.util.constants.Formats.DATE_FORMAT
import com.matxowy.vehiclecost.util.constants.Formats.TIME_FORMAT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditRepairFragment : Fragment(R.layout.add_edit_repair_fragment) {

    private val viewModel: AddEditRepairViewModel by viewModels()
    private var _binding: AddEditRepairFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddEditRepairFragmentBinding.bind(view)

        setPickersForDateAndTime()
        setFieldsWithData()
        setListenersToFieldsAndButton()
        setProperTextForButton()
        handleAddEditRepairEvents()
    }

    private fun handleAddEditRepairEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditRepairEvent.collect { event ->
                when (event) {
                    is AddEditRepairViewModel.AddEditRepairEvent.NavigateToHistoryWithResult -> {
                        setFragmentResult(
                            requestKey = ADD_EDIT_REPAIR_REQUEST,
                            result = bundleOf(ADD_EDIT_REPAIR_RESULT to event.result)
                        )
                        val action = AddEditRepairFragmentDirections.actionAddEditRepairFragmentToHistoryFragment()
                        findNavController().navigate(action)
                    }
                    is AddEditRepairViewModel.AddEditRepairEvent.ShowFieldsCannotBeEmptyMessage -> {
                        Snackbar.make(requireView(), getString(R.string.required_fields_cannot_be_empty_text), Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditRepairViewModel.AddEditRepairEvent.ShowDefaultErrorMessage -> {
                        Snackbar.make(requireView(), getString(R.string.default_error_message), Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    private fun setProperTextForButton() {
        binding.apply {
            if (viewModel.mileage.hasDefaultValue()) {
                btnAddNewRepair.text = getString(R.string.add_repair_button_text)
            } else {
                btnAddNewRepair.text = getString(R.string.edit_repair_button_text)
            }
        }
    }

    private fun setListenersToFieldsAndButton() {
        binding.apply {
            etMileage.addTextChangedListener {
                viewModel.mileage = it.toString().toIntOrDefaultValue()
            }

            etComments.addTextChangedListener {
                viewModel.comments = it.toString()
            }

            etTime.addTextChangedListener {
                viewModel.time = it.toString()
            }

            etDate.addTextChangedListener {
                viewModel.date = it.toString()
            }

            etCost.addTextChangedListener {
                viewModel.cost = it.toString().toDoubleOrDefaultValue()
            }

            etTitle.addTextChangedListener {
                viewModel.title = it.toString()
            }

            btnAddNewRepair.setOnClickListener {
                viewModel.onSaveRepairClick()
            }
        }
    }

    private fun setPickersForDateAndTime() {
        binding.apply {
            etDate.transformIntoDatePicker(requireContext(), DATE_FORMAT)
            etTime.transformIntoTimePicker(requireContext(), TIME_FORMAT)
        }
    }

    private fun setFieldsWithData() {
        binding.apply {
            etTitle.setText(viewModel.title)
            etCost.setText(viewModel.cost.returnValueForField())
            etDate.setText(viewModel.date)
            etMileage.setText(viewModel.mileage.returnValueForField())
            etTime.setText(viewModel.time)
            etComments.setText(viewModel.comments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ADD_EDIT_REPAIR_REQUEST = "add_edit_repair_request"
        const val ADD_EDIT_REPAIR_RESULT = "add_edit_repair_result"
    }
}