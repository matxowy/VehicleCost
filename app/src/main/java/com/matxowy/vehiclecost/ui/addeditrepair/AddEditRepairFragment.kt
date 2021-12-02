package com.matxowy.vehiclecost.ui.addeditrepair

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.AddEditRepairFragmentBinding
import com.matxowy.vehiclecost.util.LocalDateConverter
import com.matxowy.vehiclecost.util.transformIntoDatePicker
import com.matxowy.vehiclecost.util.transformIntoTimePicker
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@AndroidEntryPoint
class AddEditRepairFragment : Fragment(R.layout.add_edit_repair_fragment) {

    private val viewModel: AddEditRepairViewModel by viewModels()
    private lateinit var binding: AddEditRepairFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = AddEditRepairFragmentBinding.bind(view)

        binding.apply {
            etDate.setText(LocalDateConverter.dateToString(LocalDate.now()))
            etDate.transformIntoDatePicker(requireContext(), "yyyy-MM-dd")
            etTime.setText(LocalDateConverter.timeToString(LocalDateTime.now()))
            etTime.transformIntoTimePicker(requireContext(), "HH:mm")
        }
    }

}