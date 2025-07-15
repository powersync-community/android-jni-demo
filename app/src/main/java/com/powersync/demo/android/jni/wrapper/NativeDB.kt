package com.powersync.demo.android.jni.wrapper

import com.powersync.PowerSyncDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class NativeDB(val db: PowerSyncDatabase, val id: String) {

    external fun syncStatusCallback(status: String, id: String)

    fun connect(connector: NativeConnector) {
        runBlocking {
            db.connect(connector)
        }

        val syncStatus = db.currentStatus
        CoroutineScope(Dispatchers.Default).launch {
            syncStatus.asFlow().collectLatest { status ->
                syncStatusCallback(
                    status.toString(),
                    id
                ) // Call the external function with the new status
            }
        }
    }

    fun getAll(query: String, params: List<String>): String {
        val res = runBlocking {
            db.getAll(query, params) { cursor ->
                Json.encodeToString(
                    buildMap {
                        for (column in cursor.columnNames) {
                            put(column.key, cursor.getString(column.value))
                        }
                    }
                )
            }
        }

        return Json.encodeToString(res)
    }

    fun execute(query: String, params: List<String>) {
        runBlocking {
            db.execute(query, params)
        }

        return
    }
}
