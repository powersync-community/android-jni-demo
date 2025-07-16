package com.powersync.demo.android.jni

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.powersync.demo.android.jni.ui.theme.PowerSyncJNIDemoApplicationTheme
import com.powersync.demo.android.jni.wrapper.NativeBridge
import com.powersync.demo.android.jni.wrapper.NativeDB
import com.powersync.demo.android.jni.wrapper.NativeDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    var didStartDemo by remember { mutableStateOf(false) }
    var demoTodoItemState by NativeBridge.todoItems
    val context = LocalContext.current
    val scope = rememberCoroutineScope { NativeDispatcher }

    fun startDemo() {
        didStartDemo = true
        scope.launch {
            NativeBridge.start(context)
        }
    }

    fun addItem(item: String) {
        scope.launch {
            NativeBridge.createList(item)
        }
    }

    PowerSyncJNIDemoApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = ::startDemo,
                    enabled = !didStartDemo,
                ) {
                    Text("Click to start native demo")
                }

                if (didStartDemo) {
                    Text(demoTodoItemState ?: "Demo started, see logcat for outputs")

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        var text by remember { mutableStateOf(TextFieldValue()) }

                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.weight(weight = 1F),
                            label = { Text("Add list via C++") }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            enabled = text.text.isNotBlank(),
                            onClick = {
                                addItem(text.text)
                                text = TextFieldValue()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}
