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
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditRefuelFragment : Fragment(R.layout.add_edit_refuel_fragment) {

    private val viewModel: AddEditRefuelViewModel by viewModels()
    private var _binding: AddEditRefuelFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = AddEditRefuelFragmentBinding.bind(view)

        // Setting spinner adapter
        val spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.type_of_fuel,
            R.layout.spinner_item_list
        )

        binding.apply {
            // Setting date and time pickers to EditTexts
            setPickersForDateAndTime()

            // Setting adapter for spinner
            spinnerTypeOfFuel.setAdapter(spinnerAdapter)

            // Setting fields with data
            setFieldsWithData()

            setListenersToFieldsAndButton()

            setObservers()

            setProperTextForButton()
        }

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
                }.exhaustive
            }
        }
    }

    private fun AddEditRefuelFragmentBinding.setProperTextForButton() {
        if (viewModel.mileage.toString().isEmpty()) {
            btnAddNewRefueled.text = getString(R.string.add_refuel_button_text)
        } else {
            btnAddNewRefueled.text = getString(R.string.edit_refuel_button_text)
        }
    }

    private fun AddEditRefuelFragmentBinding.setObservers() {
        viewModel.lastMileage.observe(viewLifecycleOwner) {
            tvLastValueOfMileage.text = getString(R.string.last_value_of_mileage, it?.addSpace() ?: ZERO_KILOMETERS_STRING)
        }
    }

    private fun AddEditRefuelFragmentBinding.setListenersToFieldsAndButton() {
        etMileage.addTextChangedListener {
            viewModel.mileage = it.toString()
        }

        etPricePerLiter.addTextChangedListener {
            viewModel.price = it.toString()
        }

        etAmountOfFuel.addTextChangedListener {
            viewModel.amountOfFuel = it.toString()
        }

        etCost.addTextChangedListener {
            viewModel.cost = it.toString()
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

    private fun AddEditRefuelFragmentBinding.setPickersForDateAndTime() {
        etDate.transformIntoDatePicker(requireContext(), DATE_FORMAT)
        etTime.transformIntoTimePicker(requireContext(), TIME_FORMAT)
    }

    private fun AddEditRefuelFragmentBinding.setFieldsWithData() {
        etMileage.setText(viewModel.mileage.toString())
        etTime.setText(viewModel.time)
        etDate.setText(viewModel.date)
        etCost.setText(StringUtils.trimTrailingZero(viewModel.cost.toString()))
        etAmountOfFuel.setText(StringUtils.trimTrailingZero(viewModel.amountOfFuel.toString()))
        etPricePerLiter.setText(StringUtils.trimTrailingZero(viewModel.price.toString()))
        etComments.setText(viewModel.comments)
        spinnerTypeOfFuel.setText(viewModel.fuelType, false)
        switchFullRefueled.isChecked = viewModel.fullRefueled
        switchFullRefueled.jumpDrawablesToCurrentState()
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
