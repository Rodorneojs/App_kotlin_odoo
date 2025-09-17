package com.kumbra.crm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kumbra.crm.ui.theme.ContactsViewModel
import com.kumbra.crm.ui.theme.LeadsViewModel

class MainActivity : ComponentActivity() {
    private val leadsVm: LeadsViewModel by viewModels()
    private val contactsVm: ContactsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(leadsVm, contactsVm) }
    }
}

@Composable
private fun App(leadsVm: LeadsViewModel, contactsVm: ContactsViewModel) {
    val leadsState by leadsVm.state.collectAsState(initial = LeadsViewModel.State.Idle)
    val contactsState by contactsVm.state.collectAsState(initial = ContactsViewModel.State.Idle)
    var tab by remember { mutableStateOf(0) }

    MaterialTheme {
        Surface(Modifier.fillMaxSize().padding(16.dp)) {
            Column {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Leads") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Contactos") })
                }
                Spacer(Modifier.height(12.dp))

                if (tab == 0) {
                    Button(onClick = { leadsVm.load() }) { Text("Cargar leads") }
                    Spacer(Modifier.height(12.dp))
                    when (val s = leadsState) {
                        is LeadsViewModel.State.Idle  -> Text("Listo para consultar")
                        is LeadsViewModel.State.Error -> Text("Error: ${s.msg}")
                        is LeadsViewModel.State.Data  -> LazyColumn {
                            items(s.leads) { l ->
                                Text(l.name, style = MaterialTheme.typography.titleMedium)
                                Text("${l.contact_name ?: "-"} · ${l.email ?: "-"} · ${(l.mobile ?: l.phone) ?: "-"}")
                                Divider(Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                } else {
                    Button(onClick = { contactsVm.load() }) { Text("Cargar contactos") }
                    Spacer(Modifier.height(12.dp))
                    when (val s = contactsState) {
                        is ContactsViewModel.State.Idle  -> Text("Listo para consultar")
                        is ContactsViewModel.State.Error -> Text("Error: ${s.msg}")
                        is ContactsViewModel.State.Data  -> LazyColumn {
                            items(s.contacts) { c ->
                                Text(c.name, style = MaterialTheme.typography.titleMedium)
                                Text("${c.email ?: "-"} · ${(c.mobile ?: c.phone) ?: "-"}")
                                Divider(Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
