package com.powersync.demo.android.jni.wrapper

import android.content.Context
import android.util.Log
import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncDatabase
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.powersync.db.schema.*

@Serializable
data class JsonIndexedColumn(val name: String)

@Serializable
data class JsonIndex(val name: String, val columns: List<JsonIndexedColumn>)

@Serializable
data class JsonColumn(val name: String, val type: String)

@Serializable
data class JsonTable(
    val name: String,
    val columns: List<JsonColumn>,
    val indexes: List<JsonIndex> = emptyList()
)

@Serializable
data class JsonSchema(val tables: List<JsonTable>)

object NativeBridge {
    private var applicationContext: Context? = null

   @JvmStatic
   private external fun init()

   init {
       System.loadLibrary("powersync_jni_demo")
   }

    fun start(ctx: Context) {
        applicationContext = ctx
        init()
        // Avoid leaking the context
        applicationContext = null
    }

   @JvmStatic
   fun createSchemaFromJson(json: String): Schema {
       val parsed = Json.decodeFromString<JsonSchema>(json)
       val tables = parsed.tables.map { table ->
           Table(
               name = table.name,
               columns = table.columns.map { col ->
                   when (col.type) {
                       "text" -> Column.text(col.name)
                       "integer" -> Column.integer(col.name)
                       else -> throw IllegalArgumentException("Unknown column type: ${col.type}")
                   }
               },
               indexes = table.indexes.map { idx ->
                   Index(
                       name = idx.name,
                       columns = idx.columns.map { IndexedColumn(it.name) }
                   )
               }
           )
       }

       return Schema(tables)
   }

    @JvmStatic
    fun createDbWithDefaultDatabaseDriverFactory(schema: Schema): PowerSyncDatabase {
        try {
            val driverFactory = DatabaseDriverFactory(applicationContext!!)
            return PowerSyncDatabase(driverFactory, schema)
        } catch (e: Exception) {
            Log.e("NativeBridge", "creating database failed", e)
            throw e
        }
    }
}
