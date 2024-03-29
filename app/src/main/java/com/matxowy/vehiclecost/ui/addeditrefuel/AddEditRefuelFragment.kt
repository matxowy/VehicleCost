package com.matxowy.vehiclecost.ui.addeditrefuel

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.AddEditRefuelFragmentBinding
import com.matxowy.vehiclecost.util.*
import com.matxowy.vehiclecost.util.constants.Formats.DATE_FORMAT
import com.matxowy.vehiclecost.util.constants.Formats.TIME_FORMAT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditRefuelFragment : Fragment(R.layout.add_edit_refuel_fragment) {

    private val viewModel: AddEditRefuelViewModel by viewModels()
    private var _binding: AddEditRefuelFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddEditRefuelFragmentBinding.bind(view)

        setAdapterForSpinner()
        setPickersForDateAndTime()
        setFieldsWithData()
        setListenersToFieldsAndButton()
        setObservers()
        setProperTextForButton()
        handleAddEditRefuelEvents()
    }

    private fun handleAddEditRefuelEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditRefuelEvent.collect { event ->
                when (event) {
                    is AddEditRefuelViewModel.AddEditRefuelEvent.NavigateToHistoryWithResult -> {
                        setFragmentResult(
                            requestKey = ADD_EDIT_REFUEL_REQUEST,
                            result = bundleOf(ADD_EDIT_REFUEL_RESULT to event.result)
                        )
                        val action = AddEditRefuelFragmentDirections.actionAddEditRefuelFragmentToHistoryFragment()
                        findNavController().navigate(action)
                    }
                    is AddEditRefuelViewModel.AddEditRefuelEvent.ShowFieldsCannotBeEmptyMessage -> {
                        Snackbar.make(requireView(), getString(R.string.required_fields_cannot_be_empty_text), Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditRefuelViewModel.AddEditRefuelEvent.ShowMileageCannotBeLessThanPreviousMessage -> {
                        Snackbar.make(requireView(), getString(R.string.mileage_cannot_be_less_than_previous), Snackbar.LENGTH_LONG).show()
                    }
                    is AddEditRefuelViewModel.AddEditRefuelEvent.ShowDefaultErrorMessage -> {
                        Snackbar.make(requireView(), getString(R.string.default_error_message), Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
        }
    }

    private fun setAdapterForSpinner() {
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.type_of_fuel,
            R.layout.spinner_item_list
        )
        binding.spinnerTypeOfFuel.setAdapter(spinnerAdapter)
    }

    private fun setProperTextForButton() {
        binding.apply {
            if (viewModel.mileage.hasDefaultValue()) {
                btnAddNewRefueled.text = getString(R.string.add_refuel_button_text)
            } else {
                btnAddNewRefueled.text = getString(R.string.edit_refuel_button_text)
            }
        }
    }

    private fun setObservers() {
        binding.apply {
            viewModel.lastMileage.observe(viewLifecycleOwner) {
                tvLastValueOfMileage.text = getString(R.string.last_value_of_mileage, it?.addSpace() ?: ZERO_KILOMETERS_STRING)
            }
        }
    }

    private fun setListenersToFieldsAndButton() {
        binding.apply {
            etMileage.addTextChangedListener {
                viewModel.mileage = it.toString().toIntOrDefaultValue()
            }

            etPricePerLiter.addTextChangedListener {
                viewModel.price = it.toString().toDoubleOrDefaultValue()
            }

            etAmountOfFuel.addTextChangedListener {
                viewModel.amountOfFuel = it.toString().toDoubleOrDefaultValue()
            }

            etCost.addTextChangedListener {
                viewModel.cost = it.toString().toDoubleOrDefaultValue()
            }

            etDate.addTextChangedListener {
                viewModel.date = it.toString()
            }

            etTime.addTextChangedListener {
                viewModel.time = it.toString()
            }

            etComments.addTextChangedListener {
                viewModel.comments = it.toString()
            }

            switchFullRefueled.setOnCheckedChangeListener { _, isChecked ->
                viewModel.fullRefueled = isChecked
            }

            btnAddNewRefueled.setOnClickListener {
                viewModel.onSaveRefueledClick()
            }

            spinnerTypeOfFuel.doOnTextChanged { text, _, _, _ ->
                viewModel.fuelType = text.toString()
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
            etMileage.setText(viewModel.mileage.returnValueForField())
            etTime.setText(viewModel.time)
            etDate.setText(viewModel.date)
            etCost.setText(viewModel.cost.returnValueForField())
            etAmountOfFuel.setText(viewModel.amountOfFuel.returnValueForField())
            etPricePerLiter.setText(viewModel.price.returnValueForField())
            etComments.setText(viewModel.comments)
            spinnerTypeOfFuel.setText(viewModel.fuelType, false)
            switchFullRefueled.isChecked = viewModel.fullRefueled
            switchFullRefueled.jumpDrawablesToCurrentState()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ZERO_KILOMETERS_STRING = "0"
        const val ADD_EDIT_REFUEL_REQUEST = "add_edit_refuel_request"
        const val ADD_EDIT_REFUEL_RESULT = "add_edit_refuel_result"
    }
}
