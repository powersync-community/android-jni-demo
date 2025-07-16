package com.powersync.demo.android.kotlinapp

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.powersync.DatabaseDriverFactory
import com.powersync.PowerSyncDatabase
import com.powersync.compose.composeState
import com.powersync.connectors.PowerSyncBackendConnector
import com.powersync.demo.android.kotlinapp.powersync.DemoConnector
import com.powersync.demo.android.kotlinapp.powersync.ListItem
import com.powersync.demo.android.kotlinapp.powersync.schema
import com.powersync.demo.android.kotlinapp.state.ListsViewModel
import com.powersync.demo.android.kotlinapp.state.NavController
import com.powersync.demo.android.kotlinapp.state.Screen
import com.powersync.demo.android.kotlinapp.state.Todo
import com.powersync.demo.android.kotlinapp.ui.components.EditDialog
import com.powersync.demo.android.kotlinapp.ui.screens.HomeScreen
import com.powersync.demo.android.kotlinapp.ui.screens.TodosScreen
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.KoinApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
    } }
    single { DemoConnector(get()) } bind PowerSyncBackendConnector::class
    single { PowerSyncDatabase(get(), schema) }
    single { NavController(Screen.Home) }
}

@Composable
fun App(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    fun KoinApplication.withDatabase() {
        modules(module { single {
            DatabaseDriverFactory(context)
        } }, appModule)
    }

    KoinApplication(application = KoinApplication::withDatabase) {
        AppContent(modifier=modifier)
    }
}

@Composable
private fun AppContent(
    modifier: Modifier = Modifier,
    db: PowerSyncDatabase = koinInject(),
    connector: PowerSyncBackendConnector = koinInject(),
) {
    LaunchedEffect(Unit) {
        try {
            db.connect(connector)
            awaitCancellation()
        } finally {
            withContext(NonCancellable) {
                db.disconnect()
            }
        }
    }

    val status by db.currentStatus.composeState()

    val navController = koinInject<NavController>()
    val currentScreen by navController.currentScreen.collectAsState()

    val lists = remember { mutableStateOf(ListsViewModel(db)) }
    val selectedListId by lists.value.selectedListId.collectAsState()
    val items by lists.value.watchItems().collectAsState(initial = emptyList())
    val listsInputText by lists.value.inputText.collectAsState()

    val todos = remember { mutableStateOf(Todo(db)) }
    val todoItems by todos.value.watchItems(selectedListId).collectAsState(initial = emptyList())
    val editingItem by todos.value.editingItem.collectAsState()
    val todosInputText by todos.value.inputText.collectAsState()

    when (currentScreen) {
        is Screen.Home -> {
            val handleOnItemClicked = { item: ListItem ->
                lists.value.onItemClicked(item)
                navController.navigate(Screen.Todos)
            }

            HomeScreen(
                modifier = modifier.background(MaterialTheme.colorScheme.background),
                items = items,
                inputText = listsInputText,
                onItemClicked = handleOnItemClicked,
                onItemDeleteClicked = lists.value::onItemDeleteClicked,
                onAddItemClicked = lists.value::onAddItemClicked,
                onInputTextChanged = lists.value::onInputTextChanged,
                syncStatus = status,
            )
        }
        is Screen.Todos -> {
            val handleOnAddItemClicked = {
                todos.value.onAddItemClicked(selectedListId!!)
            }

            TodosScreen(
                modifier = modifier.background(MaterialTheme.colorScheme.background),
                navController = navController,
                items = todoItems,
                syncStatus = status,
                inputText = todosInputText,
                onItemClicked = todos.value::onItemClicked,
                onItemDoneChanged = todos.value::onItemDoneChanged,
                onItemDeleteClicked = todos.value::onItemDeleteClicked,
                onAddItemClicked = handleOnAddItemClicked,
                onInputTextChanged = todos.value::onInputTextChanged,
            )

            editingItem?.also {
                EditDialog(
                    item = it,
                    onCloseClicked = todos.value::onEditorCloseClicked,
                    onTextChanged = todos.value::onEditorTextChanged,
                    onDoneChanged = todos.value::onEditorDoneChanged,
                )
            }
        }
    }
}
