package com.kumbra.crm.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kumbra.crm.data.Lead
import com.kumbra.crm.data.LeadQuery
import com.kumbra.crm.net.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LeadsViewModel : ViewModel() {
    sealed interface State { data object Idle: State; data class Data(val leads: List<Lead>): State; data class Error(val msg:String): State }
    private val _state = MutableStateFlow<State>(State.Idle)
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try { _state.value = State.Data(Api.postLeads(LeadQuery(limit = 50, tag = "Solicitud de Catálogo"))) }
            catch (e: Exception) { _state.value = State.Error(e.message ?: "Error") }
        }
    }
}
