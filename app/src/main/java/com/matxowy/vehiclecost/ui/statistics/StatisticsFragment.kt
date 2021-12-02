package com.matxowy.vehiclecost.ui.statistics

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.StatisticsFragmentBinding
import com.matxowy.vehiclecost.util.exhaustive
import kotlinx.coroutines.flow.collect


class StatisticsFragment : Fragment(R.layout.statistics_fragment) {

    private val viewModel: StatisticsViewModel by viewModels()
    private lateinit var binding: StatisticsFragmentBinding

    //Animations
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.to_bottom_anim) }

    private var clicked = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = StatisticsFragmentBinding.bind(view)

        //FAB operations
        binding.apply {
            fabAdd.setOnClickListener {
                onAddButtonClicked()
            }

            fabAddRefuel.setOnClickListener {
                viewModel.onAddNewRefuelClick()
            }

            fabAddRepair.setOnClickListener {
                viewModel.onAddNewRepairClick()
            }
        }

        // Navigation between screens
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addingEvent.collect { event ->
                when(event) {
                    is StatisticsViewModel.AddingEvent.NavigateToAddRefuelScreen -> {
                        val action = StatisticsFragmentDirections.actionStatisticsFragmentToAddEditRefuelFragment(null, "Nowe tankowanie")
                        findNavController().navigate(action)
                        clicked = false
                    }
                    is StatisticsViewModel.AddingEvent.NavigateToAddRepairScreen -> {
                        val action = StatisticsFragmentDirections.actionStatisticsFragmentToAddEditRepairFragment(null, "Nowa naprawa")
                        findNavController().navigate(action)
                        clicked = false
                    }
                }.exhaustive
            }
        }
    }

    // Functions managing animations of fab
    private fun onAddButtonClicked() {
        setVisibility()
        setAnimation()
        setClickable()
        clicked = !clicked
    }

    private fun setVisibility() {
        if (!clicked) {
            binding.apply {
                fabAddRepair.visibility = View.VISIBLE
                fabAddRefuel.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                fabAddRepair.visibility = View.GONE
                fabAddRefuel.visibility = View.GONE
            }
        }
    }

    private fun setAnimation() {
        if (!clicked) {
            binding.apply {
                fabAddRepair.startAnimation(fromBottom)
                fabAddRefuel.startAnimation(fromBottom)
                fabAdd.startAnimation(rotateOpen)
            }
        } else {
            binding.apply {
                fabAddRepair.startAnimation(toBottom)
                fabAddRefuel.startAnimation(toBottom)
                fabAdd.startAnimation(rotateClose)
            }
        }
    }

    private fun setClickable() {
        if (!clicked) {
            binding.apply {
                fabAddRefuel.isClickable = true
                fabAddRepair.isClickable = true
            }
        } else {
            binding.apply {
                fabAddRefuel.isClickable = false
                fabAddRepair.isClickable = false
            }
        }
    }

}