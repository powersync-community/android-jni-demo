package com.powersync.demo.android.kotlinapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.powersync.demo.android.kotlinapp.powersync.TodoItem
import com.powersync.demo.android.kotlinapp.state.NavController
import com.powersync.demo.android.kotlinapp.state.Screen
import com.powersync.demo.android.kotlinapp.ui.components.Input
import com.powersync.demo.android.kotlinapp.ui.components.WifiIcon
import com.powersync.sync.SyncStatusData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodosScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    items: List<TodoItem>,
    inputText: String,
    syncStatus: SyncStatusData,
    onItemClicked: (item: TodoItem) -> Unit,
    onItemDoneChanged: (item: TodoItem, isDone: Boolean) -> Unit,
    onItemDeleteClicked: (item: TodoItem) -> Unit,
    onAddItemClicked: () -> Unit,
    onInputTextChanged: (value: String) -> Unit,
) {
    Column(modifier) {
        TopAppBar(
            title = {
                Text(
                    "Todo List",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(end = 36.dp),
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate(Screen.Home) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                }
            },
            actions = {
                WifiIcon(syncStatus)
                Spacer(modifier = Modifier.width(16.dp))
            },
        )

        Input(
            text = inputText,
            onAddClicked = onAddItemClicked,
            onTextChanged = onInputTextChanged,
            screen = Screen.Todos,
        )

        Box(Modifier.weight(1F)) {
            TodoList(
                items = items,
                onItemClicked = onItemClicked,
                onItemDoneChanged = onItemDoneChanged,
                onItemDeleteClicked = onItemDeleteClicked,
            )
        }
    }
}

@Composable
private fun TodoList(
    items: List<TodoItem>,
    onItemClicked: (id: TodoItem) -> Unit,
    onItemDoneChanged: (item: TodoItem, isDone: Boolean) -> Unit,
    onItemDeleteClicked: (item: TodoItem) -> Unit,
) {
    Box {
        val listState = rememberLazyListState()

        LazyColumn(state = listState) {
            items(items) { item ->
                TodoItem(
                    item = item,
                    onClicked = { onItemClicked(item) },
                    onDoneChanged = { onItemDoneChanged(item, it) },
                    onDeleteClicked = { onItemDeleteClicked(item) }
                )

                HorizontalDivider()
            }
        }
    }
}

@Composable
internal fun TodoItem(
    item: TodoItem,
    onClicked: () -> Unit,
    onDoneChanged: (Boolean) -> Unit,
    onDeleteClicked: () -> Unit
) {
    Row(modifier = Modifier.clickable(onClick = onClicked)) {
        Spacer(modifier = Modifier.width(8.dp))

        Checkbox(
            checked = item.completed,
            modifier = Modifier.align(Alignment.CenterVertically),
            onCheckedChange = onDoneChanged,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = AnnotatedString(item.description),
            modifier = Modifier.weight(1F).align(Alignment.CenterVertically),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onDeleteClicked) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
    }
}
