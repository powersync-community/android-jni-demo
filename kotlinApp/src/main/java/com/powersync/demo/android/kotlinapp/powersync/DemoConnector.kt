package com.powersync.demo.android.kotlinapp.powersync

import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonPrimitive

class DemoConnector(private val client: HttpClient): PowerSyncBackendConnector() {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun fetchCredentials(): PowerSyncCredentials? {
        @Serializable
        @JsonIgnoreUnknownKeys()
        class TokenResponse(val token: String)

        val response: TokenResponse = client.get("http://10.0.2.2:6060/api/auth/token").body()
        return PowerSyncCredentials(
            endpoint = "http://10.0.2.2:8080",
            token = response.token,
        )
    }

    override suspend fun uploadData(database: PowerSyncDatabase) {
        @Serializable
        class SerializedOplogEntry(
            val table: String,
            val op: String,
            val id: String,
            val data: Map<String, JsonElement?>?
        )

        @Serializable
        class UploadRequest(val batch: List<SerializedOplogEntry>)

        val dbBatch = database.getCrudBatch() ?: return
        val request = UploadRequest(buildList {
            for (op in dbBatch.crud) {
                add(SerializedOplogEntry(
                    table = op.table,
                    op = op.op.toJson(),
                    id = op.id,
                    data = op.opData?.mapValues { (key, value) ->
                        when (key) {
                            // Cast to boolean
                            "completed" -> JsonPrimitive(value == "1")
                            else -> JsonPrimitive(value)
                        }
                    }
                ))
            }
        })

        client.post("http://10.0.2.2:6061/api/data") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        dbBatch.complete(null)
    }
}
