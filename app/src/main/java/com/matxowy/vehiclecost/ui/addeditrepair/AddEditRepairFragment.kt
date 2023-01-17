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

        binding.apply {
            // Setting date and time pickers to EditTexts
            setPickersForDateAndTime()

            // Setting fields with data
            setFieldsWithData()

            setListenersToFieldsAndButton()

            setObservers()

            setProperTextForButton()
        }

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
                    AddEditRepairViewModel.AddEditRepairEvent.ShowMileageCannotBeLessThanPreviousMessage -> {
                        Snackbar.make(requireView(), getString(R.string.mileage_cannot_be_less_than_previous), Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    private fun AddEditRepairFragmentBinding.setProperTextForButton() {
        if (viewModel.mileage.toString().isEmpty()) {
            btnAddNewRepair.text = getString(R.string.add_repair_button_text)
        } else {
            btnAddNewRepair.text = getString(R.string.edit_repair_button_text)
        }
    }

    private fun AddEditRepairFragmentBinding.setObservers() {
        viewModel.lastMileage.observe(viewLifecycleOwner) {
            tvLastValueOfMileage.text = getString(R.string.last_value_of_mileage, it?.addSpace() ?: ZERO_KILOMETERS_STRING)
        }
    }

    private fun AddEditRepairFragmentBinding.setListenersToFieldsAndButton() {
        etMileage.addTextChangedListener {
            viewModel.mileage = it.toString()
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
            viewModel.cost = it.toString()
        }

        etTitle.addTextChangedListener {
            viewModel.title = it.toString()
        }

        btnAddNewRepair.setOnClickListener {
            viewModel.onSaveRepairClick()
        }
    }

    private fun AddEditRepairFragmentBinding.setPickersForDateAndTime() {
        etDate.transformIntoDatePicker(requireContext(), DATE_FORMAT)
        etTime.transformIntoTimePicker(requireContext(), TIME_FORMAT)
    }

    private fun AddEditRepairFragmentBinding.setFieldsWithData() {
        etTitle.setText(viewModel.title)
        etCost.setText(StringUtils.trimTrailingZero(viewModel.cost.toString()))
        etDate.setText(viewModel.date)
        etMileage.setText(viewModel.mileage.toString())
        etTime.setText(viewModel.time)
        etComments.setText(viewModel.comments)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ZERO_KILOMETERS_STRING = "0"
        const val ADD_EDIT_REPAIR_REQUEST = "add_edit_repair_request"
        const val ADD_EDIT_REPAIR_RESULT = "add_edit_repair_result"
    }
}