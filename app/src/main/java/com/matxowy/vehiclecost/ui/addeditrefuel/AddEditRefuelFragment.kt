package com.matxowy.vehiclecost.ui.addeditrefuel

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.AddEditRefuelFragmentBinding
import com.matxowy.vehiclecost.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditRefuelFragment : Fragment(R.layout.add_edit_refuel_fragment) {

    private val viewModel: AddEditRefuelViewModel by viewModels()
    private lateinit var binding: AddEditRefuelFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddEditRefuelFragmentBinding.bind(view)

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

            // Refactor in future
            if (viewModel.mileage == "") {
                btnAddNewRefueled.text = "DODAJ TANKOWANIE"
            } else {
                btnAddNewRefueled.text = "EDYTUJ TANKOWANIE"
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditRefuelEvent.collect { event ->
                when (event) {
                    is AddEditRefuelViewModel.AddEditRefuelEvent.NavigateToHistoryWithResult -> {
                        setFragmentResult(
                            "add_edit_refuel_request",
                            bundleOf("add_edit_refuel_result" to event.result)
                        )
                        val action = AddEditRefuelFragmentDirections.actionAddEditRefuelFragmentToHistoryFragment()
                        findNavController().navigate(action)
                    }
                    is AddEditRefuelViewModel.AddEditRefuelEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
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
    }

    // not needed
    /*private fun AddEditRefuelFragmentBinding.clearFocusOnEditTexts() {
        etComments.clearFocus()
        etTime.clearFocus()
        etDate.clearFocus()
        etCost.clearFocus()
        etAmountOfFuel.clearFocus()
        etPricePerLiter.clearFocus()
        etMileage.clearFocus()
    }*/

    private fun AddEditRefuelFragmentBinding.setPickersForDateAndTime() {
        etDate.transformIntoDatePicker(requireContext(), "yyyy-MM-dd")
        etTime.transformIntoTimePicker(requireContext(), "HH:mm")
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

}