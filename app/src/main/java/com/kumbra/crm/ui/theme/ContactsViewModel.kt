package com.kumbra.crm.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kumbra.crm.data.Contact
import com.kumbra.crm.data.ContactQuery
import com.kumbra.crm.net.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {
    sealed interface State { data object Idle: State; data class Data(val contacts: List<Contact>): State; data class Error(val msg:String): State }
    private val _state = MutableStateFlow<State>(State.Idle)
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try { _state.value = State.Data(Api.postContacts(ContactQuery(limit = 50))) }
            catch (e: Exception) { _state.value = State.Error(e.message ?: "Error") }
        }
    }
}
