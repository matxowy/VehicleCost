package com.matxowy.vehiclecost.ui.calculator


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.CalculatorFragmentBinding
import com.matxowy.vehiclecost.util.enums.SelectedTab
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_DOUBLE_VALUE
import com.matxowy.vehiclecost.util.constants.DefaultFieldValues.DEFAULT_INT_VALUE
import com.matxowy.vehiclecost.util.hasDefaultValue
import com.matxowy.vehiclecost.util.hideKeyboard
import com.matxowy.vehiclecost.util.roundTo
import com.matxowy.vehiclecost.util.trimTrailingZero
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalculatorFragment : Fragment(R.layout.calculator_fragment) {

    private val viewModel: CalculatorViewModel by navGraphViewModels(R.id.nav_graph) // must be navGraphViewModels to not delete inputs in calculator
    private var _binding: CalculatorFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Disable back arrow in header for avoid returning to adding/editing screen by this
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        _binding = CalculatorFragmentBinding.bind(view)

        setValuesInEditTexts()

        binding.rgTypes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_fuel_consumption -> {
                    hideKeyboard()
                    setValuesInEditTexts()
                    makeVisibleFuelConsumptionView()
                    viewModel.doCalculation()
                }
                R.id.rb_costs -> {
                    hideKeyboard()
                    setValuesInEditTexts()
                    makeVisibleCostsView()
                    viewModel.doCalculation()
                }
                R.id.rb_range -> {
                    hideKeyboard()
                    setValuesInEditTexts()
                    makeVisibleRangeView()
                    viewModel.doCalculation()
                }
            }
        }

        addTextChangeListenersToConsumptionTab()
        addTextChangeListenersToCostsTab()
        addTextChangeListenersToRangeTab()

        addObservers()


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.calculatorEvent.collect { event ->
                when (event) {
                    is CalculatorViewModel.CalculatorEvent.ShowMessageAboutMissingData -> {
                        showMessageAboutMissingData()
                    }
                    is CalculatorViewModel.CalculatorEvent.ShowResultForConsumptionTab -> {
                        showResultForConsumptionTab(event.avgConsumption, event.price)
                    }
                    is CalculatorViewModel.CalculatorEvent.ShowResultForCostsTab -> {
                        showResultForCostsTab(
                            event.requiredAmountOfFuel,
                            event.costForTravel,
                            event.costPerPerson
                        )
                    }
                    is CalculatorViewModel.CalculatorEvent.ShowResultForRangeTab -> {
                        showResultForRangeTab(event.amountOfFilledWithFuel, event.rangeInKm)
                    }
                }
            }
        }
    }

    private fun setValuesInEditTexts() {
        binding.apply {
            if (!viewModel.refueled.hasDefaultValue()) etRefueledFromFuelConsumptionTab.setText(viewModel.refueled.toString().trimTrailingZero())
            if (!viewModel.paid.hasDefaultValue()) etPaidFromRangeTab.setText(viewModel.paid.toString().trimTrailingZero())
            if (!viewModel.numberOfPeople.hasDefaultValue()) etNumberOfPeopleFromCostsTab.setText(viewModel.numberOfPeople.toString())
            if (!viewModel.kmTraveled.hasDefaultValue()) etKmTraveledFromFuelConsumptionTab.setText(viewModel.kmTraveled.toString().trimTrailingZero())
            if (!viewModel.kmTraveled.hasDefaultValue()) etKmTraveledFromCostsTab.setText(viewModel.kmTraveled.toString().trimTrailingZero())
            if (!viewModel.fuelPrice.hasDefaultValue()) etFuelPriceFromRangeTab.setText(viewModel.fuelPrice.toString().trimTrailingZero())
            if (!viewModel.fuelPrice.hasDefaultValue()) etFuelPriceFromFuelConsumptionTab.setText(viewModel.fuelPrice.toString().trimTrailingZero())
            if (!viewModel.fuelPrice.hasDefaultValue()) etFuelPriceFromCostsTab.setText(viewModel.fuelPrice.toString().trimTrailingZero())
            if (!viewModel.avgFuelConsumption.hasDefaultValue()) etAvgFuelConsumptionFromRangeTab.setText(viewModel.avgFuelConsumption.toString().trimTrailingZero())
            if (!viewModel.avgFuelConsumption.hasDefaultValue()) etAvgFuelConsumptionFromCostsTab.setText(viewModel.avgFuelConsumption.toString().trimTrailingZero())
        }
    }

    private fun showResultForRangeTab(amountOfFilledWithFuel: Double, rangeInKm: Double) {
        val filledText = SpannableStringBuilder()
            .append(getString(R.string.refueled_result_text))
            .bold { append(amountOfFilledWithFuel.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.liters_result_text)) }

        val expectedRangeText = SpannableStringBuilder()
            .append(getString(R.string.expected_range_is_result_text))
            .bold { append(rangeInKm.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.km_result_text)) }

        binding.apply {
            tvFilledWithFuel.text = filledText
            tvFilledWithFuel.visibility = View.VISIBLE
            tvExpectedRange.text = expectedRangeText
            tvExpectedRange.visibility = View.VISIBLE
        }
    }

    private fun showResultForCostsTab(
        requiredAmountOfFuel: Double,
        costForTravel: Double,
        costPerPerson: Double
    ) {
        val requiredAmountText = SpannableStringBuilder()
            .append(getString(R.string.required_amount_of_fuel_result_text))
            .bold { append(requiredAmountOfFuel.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.liters_result_text)) }

        val travelCostText = SpannableStringBuilder()
            .append(getString(R.string.cost_of_travel_result_text))
            .bold { append(costForTravel.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.zloty_result_text)) }

        val travelCostPerPersonText = SpannableStringBuilder()
            .append(getString(R.string.per_person_result_text))
            .bold { append(costPerPerson.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.zloty_result_text)) }

        binding.apply {
            tvRequiredAmountOfFuel.text = requiredAmountText
            tvRequiredAmountOfFuel.visibility = View.VISIBLE
            tvTravelCost.text = travelCostText
            tvTravelCost.visibility = View.VISIBLE
            tvTravelCostPerPerson.text = travelCostPerPersonText
            tvTravelCostPerPerson.visibility = View.VISIBLE
        }
    }

    private fun showResultForConsumptionTab(avgConsumption: Double, price: Double) {
        val avgFuelConsumptionText = SpannableStringBuilder()
            .append(getString(R.string.avg_consumtion_result_text))
            .bold { append(avgConsumption.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.liters_result_text)) }

        val costFor100kmText = SpannableStringBuilder()
            .append(getString(R.string.price_per_100km_result_text))
            .bold { append(price.roundTo(DECIMAL_PLACES).toString().trimTrailingZero()) }
            .bold { append(getString(R.string.zloty_result_text)) }

        binding.apply {
            tvAvgFuelConsumption.text = avgFuelConsumptionText
            tvAvgFuelConsumption.visibility = View.VISIBLE
            tvCostOf100km.text = costFor100kmText
            tvCostOf100km.visibility = View.VISIBLE
        }
    }

    private fun showMessageAboutMissingData() {
        binding.apply {
            when {
                llFuelConsumptionView.visibility == View.VISIBLE -> {
                    showPreciseInfoAboutMissingDataInConsumptionTab()
                }
                llCostsView.visibility == View.VISIBLE -> {
                    showPreciseInfoAboutMissingDataInCostsTab()
                }
                llRangeView.visibility == View.VISIBLE -> {
                    showPreciseInfoAboutMissingDataInRangeTab()
                }
            }
        }
    }

    private fun showPreciseInfoAboutMissingDataInConsumptionTab() {
        binding.apply {
            when {
                viewModel.refueled.hasDefaultValue() -> {
                    tvAvgFuelConsumption.text = getString(R.string.message_about_missing_refueled_data)
                    tvAvgFuelConsumption.visibility = View.VISIBLE
                    tvCostOf100km.visibility = View.GONE
                }
                viewModel.kmTraveled.hasDefaultValue() -> {
                    tvAvgFuelConsumption.text = getString(R.string.message_about_missing_km_traveled_data)
                    tvAvgFuelConsumption.visibility = View.VISIBLE
                    tvCostOf100km.visibility = View.GONE
                }
                viewModel.fuelPrice.hasDefaultValue() -> {
                    tvAvgFuelConsumption.text = getString(R.string.message_about_missing_price_per_liter_data)
                    tvAvgFuelConsumption.visibility = View.VISIBLE
                    tvCostOf100km.visibility = View.GONE
                }
            }
        }
    }

    private fun showPreciseInfoAboutMissingDataInCostsTab() {
        binding.apply {
            when {
                viewModel.avgFuelConsumption.hasDefaultValue() -> {
                    tvRequiredAmountOfFuel.text = getString(R.string.message_about_missing_avg_consumption_data)
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
                viewModel.kmTraveled.hasDefaultValue() -> {
                    tvRequiredAmountOfFuel.text = getString(R.string.message_about_missing_km_traveled_data)
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
                viewModel.fuelPrice.hasDefaultValue() -> {
                    tvRequiredAmountOfFuel.text = getString(R.string.message_about_missing_price_per_liter_data)
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
                viewModel.numberOfPeople.hasDefaultValue() -> {
                    tvRequiredAmountOfFuel.text = getString(R.string.message_about_missing_num_of_people_data)
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
            }
        }
    }

    private fun showPreciseInfoAboutMissingDataInRangeTab() {
        binding.apply {
            when {
                viewModel.avgFuelConsumption.hasDefaultValue() -> {
                    tvFilledWithFuel.text = getString(R.string.message_about_missing_avg_consumption_data)
                    tvFilledWithFuel.visibility = View.VISIBLE
                    tvExpectedRange.visibility = View.GONE
                }
                viewModel.paid.hasDefaultValue() -> {
                    tvFilledWithFuel.text = getString(R.string.message_about_missing_paid_data)
                    tvFilledWithFuel.visibility = View.VISIBLE
                    tvExpectedRange.visibility = View.GONE
                }
                viewModel.fuelPrice.hasDefaultValue() -> {
                    tvFilledWithFuel.text = getString(R.string.message_about_missing_price_per_liter_data)
                    tvFilledWithFuel.visibility = View.VISIBLE
                    tvExpectedRange.visibility = View.GONE
                }
            }
        }
    }

    private fun addObservers() {
        viewModel.apply {
            currentRefueled.observe(viewLifecycleOwner, {
                viewModel.doCalculation()
            })

            currentKmTraveled.observe(viewLifecycleOwner, {
                viewModel.doCalculation()
            })

            currentFuelPrice.observe(viewLifecycleOwner, {
                viewModel.doCalculation()
            })

            currentAvgFuelConsumption.observe(viewLifecycleOwner, {
                viewModel.doCalculation()
            })

            currentNumberOfPeople.observe(viewLifecycleOwner, {
                viewModel.doCalculation()
            })

            currentPaid.observe(viewLifecycleOwner, {
                viewModel.doCalculation()
            })
        }
    }

    private fun addTextChangeListenersToRangeTab() {
        binding.apply {
            etAvgFuelConsumptionFromRangeTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.avgFuelConsumption = text.toString().toDouble()
                    else viewModel.avgFuelConsumption = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentAvgFuelConsumption.value = viewModel.avgFuelConsumption.toString().toDoubleOrNull()
            }

            etPaidFromRangeTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.paid = text.toString().toDouble()
                    else viewModel.paid = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentPaid.value = viewModel.paid.toString().toDoubleOrNull()
            }

            etFuelPriceFromRangeTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.fuelPrice = text.toString().toDouble()
                    else viewModel.fuelPrice = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentFuelPrice.value = viewModel.fuelPrice.toString().toDoubleOrNull()
            }
        }
    }

    private fun addTextChangeListenersToCostsTab() {
        binding.apply {
            etAvgFuelConsumptionFromCostsTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.avgFuelConsumption = text.toString().toDouble()
                    else viewModel.avgFuelConsumption = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentAvgFuelConsumption.value = viewModel.avgFuelConsumption.toString().toDoubleOrNull()
            }

            etKmTraveledFromCostsTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.kmTraveled = text.toString().toDouble()
                    else viewModel.kmTraveled = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentKmTraveled.value = viewModel.kmTraveled.toString().toDoubleOrNull()
            }

            etFuelPriceFromCostsTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.fuelPrice = text.toString().toDouble()
                    else viewModel.fuelPrice = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentFuelPrice.value = viewModel.fuelPrice.toString().toDoubleOrNull()
            }

            etNumberOfPeopleFromCostsTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.numberOfPeople = text.toString().toInt()
                    else viewModel.numberOfPeople = DEFAULT_INT_VALUE
                }
                viewModel.currentNumberOfPeople.value = viewModel.numberOfPeople.toString().toIntOrNull()
            }
        }
    }

    private fun addTextChangeListenersToConsumptionTab() {
        binding.apply {
            etRefueledFromFuelConsumptionTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.refueled = text.toString().toDouble()
                    else viewModel.refueled = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentRefueled.value = viewModel.refueled.toString().toDoubleOrNull()
            }

            etKmTraveledFromFuelConsumptionTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.kmTraveled = text.toString().toDouble()
                    else viewModel.kmTraveled = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentKmTraveled.value = viewModel.kmTraveled.toString().toDoubleOrNull()
            }

            etFuelPriceFromFuelConsumptionTab.doAfterTextChanged { text ->
                if (text != null) {
                    if (text.isNotEmpty()) viewModel.fuelPrice = text.toString().toDouble()
                    else viewModel.fuelPrice = DEFAULT_DOUBLE_VALUE
                }
                viewModel.currentFuelPrice.value = viewModel.fuelPrice.toString().toDoubleOrNull()
            }
        }
    }

    private fun makeVisibleRangeView() {
        binding.apply {
            llRangeView.visibility = View.VISIBLE

            llFuelConsumptionView.visibility = View.GONE
            llCostsView.visibility = View.GONE
        }

        viewModel.currentTabSelected = SelectedTab.RANGE
    }

    private fun makeVisibleCostsView() {
        binding.apply {
            llCostsView.visibility = View.VISIBLE

            llFuelConsumptionView.visibility = View.GONE
            llRangeView.visibility = View.GONE
        }

        viewModel.currentTabSelected = SelectedTab.COSTS
    }

    private fun makeVisibleFuelConsumptionView() {
        binding.apply {
            llFuelConsumptionView.visibility = View.VISIBLE

            llRangeView.visibility = View.GONE
            llCostsView.visibility = View.GONE
        }

        viewModel.currentTabSelected = SelectedTab.CONSUMPTION
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DECIMAL_PLACES = 2
    }
}
