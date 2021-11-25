package com.matxowy.vehiclecost.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {
    private val addEventChannel = Channel<AddingEvent>()
    val addingEvent = addEventChannel.receiveAsFlow()

    fun onAddNewRefuelClick() = viewModelScope.launch {
        addEventChannel.send(AddingEvent.NavigateToAddRefuelScreen)
    }

    sealed class AddingEvent {
        object NavigateToAddRefuelScreen : AddingEvent()
        object NavigateToAddRepairScreen : AddingEvent()
    }
}