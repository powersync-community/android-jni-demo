package com.powersync.demo.android.jni.wrapper

import com.powersync.PowerSyncDatabase
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.connectors.PowerSyncCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class NativeConnector(val identifier: String) : PowerSyncBackendConnector() {
    external fun fetchCredentialsC(): PowerSyncCredentials
    external fun uploadDataC(id: String, op: String, table: String, data: String?): String?

    override suspend fun fetchCredentials(): PowerSyncCredentials {
        return withContext(Dispatchers.IO) {
            fetchCredentialsC()
        }
    }

    override suspend fun uploadData(database: PowerSyncDatabase) {
        withContext(Dispatchers.IO) {
            var checkpoint: String? = null
            database.getCrudBatch()?.let { batch ->
                batch.crud.forEach { item ->
                    val jsonData = item.opData?.let { data -> Json.encodeToString(data) }
                    checkpoint = uploadDataC(item.id, item.op.toString(), item.table, jsonData)
                }
                batch.complete(checkpoint)
            }
        }
    }
}
