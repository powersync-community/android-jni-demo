package com.powersync.demo.android.kotlinapp.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powersync.PowerSyncDatabase
import com.powersync.db.getString
import com.powersync.demo.android.kotlinapp.powersync.LISTS_TABLE
import com.powersync.demo.android.kotlinapp.powersync.ListItem
import com.powersync.demo.android.kotlinapp.powersync.TODOS_TABLE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListsViewModel(
    private val db: PowerSyncDatabase,
) : ViewModel() {
    private val _selectedListId = MutableStateFlow<String?>(null)
    val selectedListId: StateFlow<String?> = _selectedListId

    private val _inputText = MutableStateFlow<String>("")
    val inputText: StateFlow<String> = _inputText

    fun watchItems(): Flow<List<ListItem>> =
        db.watch(
            """
            SELECT
                *
            FROM
                $LISTS_TABLE
            LEFT JOIN $TODOS_TABLE
            ON  $LISTS_TABLE.id = $TODOS_TABLE.list_id
            GROUP BY $LISTS_TABLE.id
        """,
        ) { cursor ->
            ListItem(
                id = cursor.getString("id"),
                createdAt = cursor.getString("created_at"),
                name = cursor.getString("name"),
                ownerId = cursor.getString("owner_id"),
            )
        }

    fun onItemDeleteClicked(item: ListItem) {
        viewModelScope.launch {
            db.writeTransaction { tx ->
                tx.execute("DELETE FROM $LISTS_TABLE WHERE id = ?", listOf(item.id))
                tx.execute("DELETE FROM $TODOS_TABLE WHERE list_id = ?", listOf(item.id))
            }
        }
    }

    fun onAddItemClicked() {
        if (_inputText.value.isBlank()) return

        viewModelScope.launch {
            db.writeTransaction { tx ->
                tx.execute(
                    "INSERT INTO $LISTS_TABLE (id, created_at, name, owner_id) VALUES (uuid(), datetime(), ?, uuid())",
                    listOf(_inputText.value),
                )
            }
            _inputText.value = ""
        }
    }

    fun onItemClicked(item: ListItem) {
        _selectedListId.value = item.id
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }
}
