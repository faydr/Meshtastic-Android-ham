package com.geeksville.mesh.ui.components.config

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import com.geeksville.mesh.ConfigProtos.Config.BluetoothConfig
import com.geeksville.mesh.copy
import com.geeksville.mesh.ui.components.DropDownPreference
import com.geeksville.mesh.ui.components.EditTextPreference
import com.geeksville.mesh.ui.components.PreferenceCategory
import com.geeksville.mesh.ui.components.PreferenceFooter
import com.geeksville.mesh.ui.components.SwitchPreference

@Composable
fun BluetoothConfigItemList(
    bluetoothConfig: BluetoothConfig,
    enabled: Boolean,
    focusManager: FocusManager,
    onSaveClicked: (BluetoothConfig) -> Unit,
) {
    var bluetoothInput by remember(bluetoothConfig) { mutableStateOf(bluetoothConfig) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item { PreferenceCategory(text = "Bluetooth Config") }

        item {
            SwitchPreference(title = "Bluetooth enabled",
                checked = bluetoothInput.enabled,
                enabled = enabled,
                onCheckedChange = { bluetoothInput = bluetoothInput.copy { this.enabled = it } })
        }
        item { Divider() }

        item {
            DropDownPreference(title = "Pairing mode",
                enabled = enabled,
                items = BluetoothConfig.PairingMode.values()
                    .filter { it != BluetoothConfig.PairingMode.UNRECOGNIZED }
                    .map { it to it.name },
                selectedItem = bluetoothInput.mode,
                onItemSelected = { bluetoothInput = bluetoothInput.copy { mode = it } })
        }
        item { Divider() }

        item {
            EditTextPreference(title = "Fixed PIN",
                value = bluetoothInput.fixedPin,
                enabled = enabled,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                onValueChanged = {
                    if (it.toString().length == 6) // ensure 6 digits
                        bluetoothInput = bluetoothInput.copy { fixedPin = it }
                })
        }

        item {
            PreferenceFooter(
                enabled = bluetoothInput != bluetoothConfig,
                onCancelClicked = {
                    focusManager.clearFocus()
                    bluetoothInput = bluetoothConfig
                },
                onSaveClicked = { onSaveClicked(bluetoothInput) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothConfigPreview(){
    BluetoothConfigItemList(
        bluetoothConfig = BluetoothConfig.getDefaultInstance(),
        enabled = true,
        focusManager = LocalFocusManager.current,
        onSaveClicked = { },
    )
}
