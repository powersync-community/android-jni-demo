package com.powersync.demo.android.kotlinapp.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.powersync.demo.android.kotlinapp.powersync.TodoItem

@Composable
internal fun EditDialog(
    item: TodoItem,
    onCloseClicked: () -> Unit,
    onTextChanged: (String) -> Unit,
    onDoneChanged: (Boolean) -> Unit,
) {
    EditDialog(
        onCloseRequest = onCloseClicked,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = item.description,
                modifier = Modifier.weight(1F).fillMaxWidth().sizeIn(minHeight = 192.dp),
                label = { Text("Todo text") },
                onValueChange = onTextChanged,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                Text(text = "Completed", Modifier.padding(15.dp))
                Checkbox(
                    checked = item.completed,
                    onCheckedChange = onDoneChanged,
                )
            }
        }
    }
}

@Composable
private fun EditDialog(
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onCloseRequest,
    ) {
        Card(elevation = CardDefaults.elevatedCardElevation()) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .height(IntrinsicSize.Min)
            ) {
                ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                    Text(text = "Edit todo")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1F)) {
                    content()
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCloseRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = "Done")
                }
            }
        }
    }
}
