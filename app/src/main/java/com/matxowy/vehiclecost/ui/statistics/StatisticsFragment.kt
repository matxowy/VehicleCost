package com.matxowy.vehiclecost.ui.statistics

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.StatisticsFragmentBinding

class StatisticsFragment : Fragment(R.layout.statistics_fragment) {

    companion object {
        fun newInstance() = StatisticsFragment()
    }

    private lateinit var viewModel: StatisticsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = StatisticsFragmentBinding.bind(view)
    }

}