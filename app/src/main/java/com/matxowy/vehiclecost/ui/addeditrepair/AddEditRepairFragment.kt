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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditRepairFragment : Fragment(R.layout.add_edit_repair_fragment) {

    private val viewModel: AddEditRepairViewModel by viewModels()
    private lateinit var binding: AddEditRepairFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddEditRepairFragmentBinding.bind(view)

        binding.apply {
            // Setting date and time pickers to EditTexts
            setPickersForDateAndTime()

            // Setting fields with data
            setFieldsWithData()

            setListenersToFieldsAndButton()

            viewModel.lastMileage.observe(viewLifecycleOwner) {
                tvLastValueOfMileage.text = "Ostatnia wartość: ${it.addSpace()} km"
            }

            // Refactor in future
            if (viewModel.mileage == "") {
                btnAddNewRepair.text = "DODAJ NAPRAWĘ"
            } else {
                btnAddNewRepair.text = "EDYTUJ NAPRAWĘ"
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditRepairEvent.collect { event ->
                when (event) {
                    is AddEditRepairViewModel.AddEditRepairEvent.NavigateToHistoryWithResult -> {
                        setFragmentResult(
                            "add_edit_repair_request",
                            bundleOf("add_edit_repair_result" to event.result)
                        )
                        val action = AddEditRepairFragmentDirections.actionAddEditRepairFragmentToHistoryFragment()
                        findNavController().navigate(action)
                    }
                    is AddEditRepairViewModel.AddEditRepairEvent.ShowInvalidDataMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                }.exhaustive
            }
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
        etDate.transformIntoDatePicker(requireContext(), "yyyy-MM-dd")
        etTime.transformIntoTimePicker(requireContext(), "HH:mm")
    }

    private fun AddEditRepairFragmentBinding.setFieldsWithData() {
        etTitle.setText(viewModel.title)
        etCost.setText(StringUtils.trimTrailingZero(viewModel.cost.toString()))
        etDate.setText(viewModel.date)
        etMileage.setText(viewModel.mileage.toString())
        etTime.setText(viewModel.time)
        etComments.setText(viewModel.comments)
    }

}