package com.powersync.demo.android.jni.wrapper

import com.powersync.PowerSyncDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class NativeDB(val db: PowerSyncDatabase, val id: String) {

    external fun syncStatusCallback(status: String, id: String)
    external fun tablesChangedCallback(id: String)

    fun connect(connector: NativeConnector) {
        runBlocking {
            db.connect(connector)
        }

        val syncStatus = db.currentStatus
        CoroutineScope(NativeDispatcher).apply {
            launch {
                syncStatus.asFlow().collectLatest { status ->
                    syncStatusCallback(
                        status.toString(),
                        id
                    ) // Call the external function with the new status
                }
            }

            launch {
                db.onChange(setOf("lists", "todos"))
                    .collect {
                        tablesChangedCallback(id)
                }
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

    @OptIn(DelicateCoroutinesApi::class)
    fun execute(query: String, params: List<String>) {
        GlobalScope.launch {
            try {
                db.execute(query, params)
                println("did insert")
            } catch (e: Exception) {
                println("huh")
                throw e
            }
        }

        return
    }
}
