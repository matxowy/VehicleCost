package com.matxowy.vehiclecost.ui.calculator



import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.text.bold
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.CalculatorFragmentBinding
import com.matxowy.vehiclecost.internal.SelectedTab
import com.matxowy.vehiclecost.util.StringUtils
import com.matxowy.vehiclecost.util.roundTo
import kotlinx.coroutines.flow.collect

class CalculatorFragment : Fragment() {

    private var _binding: CalculatorFragmentBinding? = null
    private val binding
        get() = _binding!!

    companion object {
        fun newInstance() = CalculatorFragment()
    }

    private lateinit var viewModel: CalculatorViewModel
    

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CalculatorFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CalculatorViewModel::class.java)


        binding.rgTypes.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_fuel_consumption -> {
                    hideSoftKeyboard()
                    setValuesInEditTexts()
                    makeVisibleFuelConsumptionView()
                }
                R.id.rb_costs -> {
                    hideSoftKeyboard()
                    setValuesInEditTexts()
                    makeVisibleCostsView()
                }
                R.id.rb_range -> {
                    hideSoftKeyboard()
                    setValuesInEditTexts()
                    makeVisibleRangeView()
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
                        showResultForCostsTab(event.requiredAmountOfFuel, event.costForTravel, event.costPerPerson)
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
            if(viewModel.refueled != null) etRefueledFromFuelConsumptionTab.setText(
                StringUtils.trimTrailingZero((viewModel.refueled).toString()))
            if(viewModel.paid != null) etPaidFromRangeTab.setText(
                StringUtils.trimTrailingZero((viewModel.paid).toString()))
            if(viewModel.numberOfPeople != null) etNumberOfPeopleFromCostsTab.setText(
                StringUtils.trimTrailingZero((viewModel.numberOfPeople).toString()))
            if(viewModel.kmTraveled != null) etKmTraveledFromFuelConsumptionTab.setText(
                StringUtils.trimTrailingZero((viewModel.kmTraveled).toString()))
            if(viewModel.kmTraveled != null) etKmTraveledFromCostsTab.setText(
                StringUtils.trimTrailingZero((viewModel.kmTraveled).toString()))
            if(viewModel.fuelPrice != null) etFuelPriceFromRangeTab.setText(
                StringUtils.trimTrailingZero((viewModel.fuelPrice).toString()))
            if(viewModel.fuelPrice != null) etFuelPriceFromFuelConsumptionTab.setText(
                StringUtils.trimTrailingZero((viewModel.fuelPrice).toString()))
            if(viewModel.fuelPrice != null) etFuelPriceFromCostsTab.setText(
                StringUtils.trimTrailingZero((viewModel.fuelPrice).toString()))
            if(viewModel.avgFuelConsumption != null) etAvgFuelConsumptionFromRangeTab.setText(
                StringUtils.trimTrailingZero((viewModel.avgFuelConsumption).toString()))
            if(viewModel.avgFuelConsumption != null) etAvgFuelConsumptionFromCostsTab.setText(
                StringUtils.trimTrailingZero((viewModel.avgFuelConsumption).toString()))
        }
    }

    private fun hideSoftKeyboard() {
        val view = activity?.currentFocus
        view?.let { v ->
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun showResultForRangeTab(amountOfFilledWithFuel: Double, rangeInKm: Double) {
        val filledText = SpannableStringBuilder()
            .append("Zatankowano: ")
            .bold { append(amountOfFilledWithFuel.roundTo(2).toString()) }
            .bold { append(" litrów") }

        val expectedRangeText = SpannableStringBuilder()
            .append("Przewidywany zasięg wynosi: ")
            .bold { append(rangeInKm.roundTo(2).toString()) }
            .bold { append(" km") }

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
            .append("Wymagana ilość paliwa: ")
            .bold { append(requiredAmountOfFuel.roundTo(2).toString()) }
            .bold { append(" litrów") }

        val travelCostText = SpannableStringBuilder()
            .append("Koszt podróży wyniesie: ")
            .bold { append(costForTravel.roundTo(2).toString()) }
            .bold { append("zł") }

        val travelCostPerPersonText = SpannableStringBuilder()
            .append("Na osobę: ")
            .bold { append(costPerPerson.roundTo(2).toString()) }
            .bold { append("zł") }

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
            .append("Srednie spalanie wynosi: ")
            .bold { append(avgConsumption.roundTo(2).toString()) }
            .bold{ append(" litrów") }

        val costFor100kmText = SpannableStringBuilder()
            .append("Cena za przejechanie 100km wynosi: ")
            .bold { append(price.roundTo(2).toString()) }
            .bold { append("zł") }

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
                viewModel.refueled == null -> {
                    tvAvgFuelConsumption.text = "Brak danych o zatankowanym paliwie"
                    tvAvgFuelConsumption.visibility = View.VISIBLE
                    tvCostOf100km.visibility = View.GONE
                }
                viewModel.kmTraveled == null -> {
                    tvAvgFuelConsumption.text = "Brak danych o przejechanych kilometrach"
                    tvAvgFuelConsumption.visibility = View.VISIBLE
                    tvCostOf100km.visibility = View.GONE
                }
                viewModel.fuelPrice == null -> {
                    tvAvgFuelConsumption.text = "Brak danych o cenie za litr paliwa"
                    tvAvgFuelConsumption.visibility = View.VISIBLE
                    tvCostOf100km.visibility = View.GONE
                }
            }
        }
    }

    private fun showPreciseInfoAboutMissingDataInCostsTab() {
        binding.apply {
            when {
                viewModel.avgFuelConsumption == null -> {
                    tvRequiredAmountOfFuel.text = "Brak danych o średnim spalaniu"
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
                viewModel.kmTraveled == null -> {
                    tvRequiredAmountOfFuel.text = "Brak danych o przejechanych kilometrach"
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
                viewModel.fuelPrice == null -> {
                    tvRequiredAmountOfFuel.text = "Brak danych o cenie za litr paliwa"
                    tvRequiredAmountOfFuel.visibility = View.VISIBLE
                    tvTravelCost.visibility = View.GONE
                    tvTravelCostPerPerson.visibility = View.GONE
                }
                viewModel.numberOfPeople == null -> {
                    tvRequiredAmountOfFuel.text = "Brak danych o liczbie osób"
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
                viewModel.avgFuelConsumption == null -> {
                    tvFilledWithFuel.text = "Brak danych o średnim spalaniu"
                    tvFilledWithFuel.visibility = View.VISIBLE
                    tvExpectedRange.visibility = View.GONE
                }
                viewModel.paid == null -> {
                    tvFilledWithFuel.text = "Brak danych o zapłaconej kwocie"
                    tvFilledWithFuel.visibility = View.VISIBLE
                    tvExpectedRange.visibility = View.GONE
                }
                viewModel.fuelPrice == null -> {
                    tvFilledWithFuel.text = "Brak danych o cenie za litr paliwa"
                    tvFilledWithFuel.visibility = View.VISIBLE
                    tvExpectedRange.visibility = View.GONE
                }
            }
        }
    }

    private fun addObservers() {
        viewModel.apply {
            currentRefueled.observe(viewLifecycleOwner, Observer {
                viewModel.doCalculation()
            })

            currentKmTraveled.observe(viewLifecycleOwner, Observer {
                viewModel.doCalculation()
            })

            currentFuelPrice.observe(viewLifecycleOwner, Observer {
                viewModel.doCalculation()
            })

            currentAvgFuelConsumption.observe(viewLifecycleOwner, Observer {
                viewModel.doCalculation()
            })

            currentNumberOfPeople.observe(viewLifecycleOwner, Observer {
                viewModel.doCalculation()
            })

            currentPaid.observe(viewLifecycleOwner, Observer {
                viewModel.doCalculation()
            })
        }
    }

    private fun addTextChangeListenersToRangeTab() {
        binding.apply {
            etAvgFuelConsumptionFromRangeTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.avgFuelConsumption = p0.toString().toDouble()
                        else viewModel.avgFuelConsumption = null
                    }
                    viewModel.currentAvgFuelConsumption.value = viewModel.avgFuelConsumption
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etPaidFromRangeTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.paid = p0.toString().toDouble()
                        else viewModel.paid = null
                    }
                    viewModel.currentPaid.value = viewModel.paid
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etFuelPriceFromRangeTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.fuelPrice = p0.toString().toDouble()
                        else viewModel.fuelPrice = null
                    }
                    viewModel.currentFuelPrice.value = viewModel.fuelPrice
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })
        }
    }

    private fun addTextChangeListenersToCostsTab() {
        binding.apply {
            etAvgFuelConsumptionFromCostsTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.avgFuelConsumption = p0.toString().toDouble()
                        else viewModel.avgFuelConsumption = null
                    }
                    viewModel.currentAvgFuelConsumption.value = viewModel.avgFuelConsumption
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etKmTraveledFromCostsTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.kmTraveled = p0.toString().toDouble()
                        else viewModel.kmTraveled = null
                    }
                    viewModel.currentKmTraveled.value = viewModel.kmTraveled
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etFuelPriceFromCostsTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.fuelPrice = p0.toString().toDouble()
                        else viewModel.fuelPrice = null
                    }
                    viewModel.currentFuelPrice.value = viewModel.fuelPrice
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etNumberOfPeopleFromCostsTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.numberOfPeople = p0.toString().toInt()
                        else viewModel.numberOfPeople = null
                    }
                    viewModel.currentNumberOfPeople.value = viewModel.numberOfPeople
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })
        }
    }

    private fun addTextChangeListenersToConsumptionTab() {
        binding.apply {
            etRefueledFromFuelConsumptionTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.refueled = p0.toString().toDouble()
                        else viewModel.refueled = null
                    }
                    viewModel.currentRefueled.value = viewModel.refueled
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etKmTraveledFromFuelConsumptionTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.kmTraveled = p0.toString().toDouble()
                        else viewModel.kmTraveled = null
                    }
                    viewModel.currentKmTraveled.value = viewModel.kmTraveled
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })

            etFuelPriceFromFuelConsumptionTab.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0 != null) {
                        if (p0.isNotEmpty()) viewModel.fuelPrice = p0.toString().toDouble()
                        else viewModel.fuelPrice = null
                    }
                    viewModel.currentFuelPrice.value = viewModel.fuelPrice
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(p0: Editable?) {}
            })
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
}