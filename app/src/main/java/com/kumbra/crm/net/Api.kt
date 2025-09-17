package com.kumbra.crm.net

import com.kumbra.crm.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException

object Api {
    private const val BASE_URL = "http://10.0.2.2:8069"  // emulador -> localhost
    private const val DB = "datarod"                     // tu DB

    private fun url(path: String) = "$BASE_URL$path?db=$DB"
    private val media = "application/json; charset=utf-8".toMediaType()
    private val json = Json { ignoreUnknownKeys = true }

    private val client by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder().addInterceptor(log).build()
    }

    suspend fun postLeads(q: LeadQuery): List<Lead> = withContext(Dispatchers.IO) {
        val body = json.encodeToString(RpcEnvelope(params = q)).toRequestBody(media)
        val req = Request.Builder().url(url("/api/v1/leads/search")).post(body).build()
        client.newCall(req).execute().use { r ->
            if (!r.isSuccessful) throw IOException("HTTP ${r.code}")
            json.decodeFromString<RpcResponse<LeadList>>(r.body!!.string()).result.items
        }
    }

    suspend fun postContacts(q: ContactQuery): List<Contact> = withContext(Dispatchers.IO) {
        val body = json.encodeToString(RpcEnvelope(params = q)).toRequestBody(media)
        val req = Request.Builder().url(url("/api/v1/contacts/search")).post(body).build()
        client.newCall(req).execute().use { r ->
            if (!r.isSuccessful) throw IOException("HTTP ${r.code}")
            json.decodeFromString<RpcResponse<ContactList>>(r.body!!.string()).result.items
        }
    }
}
