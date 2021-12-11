package com.matxowy.vehiclecost.ui.addeditrefuel

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.AddEditRefuelFragmentBinding
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.StringUtils
import com.matxowy.vehiclecost.util.transformIntoDatePicker
import com.matxowy.vehiclecost.util.transformIntoTimePicker
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

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
            etDate.setText(LocalDateConverter.dateToString(LocalDate.now()))
            etDate.transformIntoDatePicker(requireContext(), "yyyy-MM-dd")
            etTime.setText(LocalDateConverter.timeToString(LocalDateTime.now()))
            etTime.transformIntoTimePicker(requireContext(), "HH:mm")
            spinnerTypeOfFuel.setAdapter(spinnerAdapter)

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

}