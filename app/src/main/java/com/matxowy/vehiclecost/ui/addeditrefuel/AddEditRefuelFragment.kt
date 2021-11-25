package com.matxowy.vehiclecost.ui.addeditrefuel

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.AddEditRefuelFragmentBinding
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.transformIntoDatePicker
import com.matxowy.vehiclecost.util.transformIntoTimePicker
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class AddEditRefuelFragment : Fragment(R.layout.add_edit_refuel_fragment) {

    private val viewModel: AddEditRefuelViewModel by viewModels()
    private lateinit var binding: AddEditRefuelFragmentBinding

    companion object {
        fun newInstance() = AddEditRefuelFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddEditRefuelFragmentBinding.bind(view)
        val spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.type_of_fuel, R.layout.spinner_item_list)

        binding.apply {
            etDate.setText(LocalDateConverter.dateToString(LocalDate.now()))
            etDate.transformIntoDatePicker(requireContext(), "yyyy-MM-dd")
            etTime.setText(LocalDateConverter.timeToString(LocalDateTime.now()))
            etTime.transformIntoTimePicker(requireContext(), "HH:mm")
            spinnerTypeOfFuel.setAdapter(spinnerAdapter)
        }
    }

}