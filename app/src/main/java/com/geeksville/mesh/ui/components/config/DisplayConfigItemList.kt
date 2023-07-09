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
import com.geeksville.mesh.ConfigProtos.Config.DisplayConfig
import com.geeksville.mesh.copy
import com.geeksville.mesh.ui.components.DropDownPreference
import com.geeksville.mesh.ui.components.EditTextPreference
import com.geeksville.mesh.ui.components.PreferenceCategory
import com.geeksville.mesh.ui.components.PreferenceFooter
import com.geeksville.mesh.ui.components.SwitchPreference

@Composable
fun DisplayConfigItemList(
    displayConfig: DisplayConfig,
    enabled: Boolean,
    focusManager: FocusManager,
    onSaveClicked: (DisplayConfig) -> Unit,
) {
    var displayInput by remember(displayConfig) { mutableStateOf(displayConfig) }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item { PreferenceCategory(text = "Display Config") }

        item {
            EditTextPreference(title = "Screen timeout (seconds)",
                value = displayInput.screenOnSecs,
                enabled = enabled,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                onValueChanged = { displayInput = displayInput.copy { screenOnSecs = it } })
        }

        item {
            DropDownPreference(title = "GPS coordinates format",
                enabled = enabled,
                items = DisplayConfig.GpsCoordinateFormat.values()
                    .filter { it != DisplayConfig.GpsCoordinateFormat.UNRECOGNIZED }
                    .map { it to it.name },
                selectedItem = displayInput.gpsFormat,
                onItemSelected = { displayInput = displayInput.copy { gpsFormat = it } })
        }
        item { Divider() }

        item {
            EditTextPreference(title = "Auto screen carousel (seconds)",
                value = displayInput.autoScreenCarouselSecs,
                enabled = enabled,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                onValueChanged = {
                    displayInput = displayInput.copy { autoScreenCarouselSecs = it }
                })
        }

        item {
            SwitchPreference(title = "Compass north top",
                checked = displayInput.compassNorthTop,
                enabled = enabled,
                onCheckedChange = { displayInput = displayInput.copy { compassNorthTop = it } })
        }
        item { Divider() }

        item {
            SwitchPreference(title = "Flip screen",
                checked = displayInput.flipScreen,
                enabled = enabled,
                onCheckedChange = { displayInput = displayInput.copy { flipScreen = it } })
        }
        item { Divider() }

        item {
            DropDownPreference(title = "Display units",
                enabled = enabled,
                items = DisplayConfig.DisplayUnits.values()
                    .filter { it != DisplayConfig.DisplayUnits.UNRECOGNIZED }
                    .map { it to it.name },
                selectedItem = displayInput.units,
                onItemSelected = { displayInput = displayInput.copy { units = it } })
        }
        item { Divider() }

        item {
            DropDownPreference(title = "Override OLED auto-detect",
                enabled = enabled,
                items = DisplayConfig.OledType.values()
                    .filter { it != DisplayConfig.OledType.UNRECOGNIZED }
                    .map { it to it.name },
                selectedItem = displayInput.oled,
                onItemSelected = { displayInput = displayInput.copy { oled = it } })
        }
        item { Divider() }

        item {
            DropDownPreference(title = "Display mode",
                enabled = enabled,
                items = DisplayConfig.DisplayMode.values()
                    .filter { it != DisplayConfig.DisplayMode.UNRECOGNIZED }
                    .map { it to it.name },
                selectedItem = displayInput.displaymode,
                onItemSelected = { displayInput = displayInput.copy { displaymode = it } })
        }
        item { Divider() }

        item {
            SwitchPreference(title = "Heading bold",
                checked = displayInput.headingBold,
                enabled = enabled,
                onCheckedChange = { displayInput = displayInput.copy { headingBold = it } })
        }
        item { Divider() }

        item {
            SwitchPreference(title = "Wake screen on tap or motion",
                checked = displayInput.wakeOnTapOrMotion,
                enabled = enabled,
                onCheckedChange = { displayInput = displayInput.copy { wakeOnTapOrMotion = it } })
        }
        item { Divider() }

        item {
            PreferenceFooter(
                enabled = displayInput != displayConfig,
                onCancelClicked = {
                    focusManager.clearFocus()
                    displayInput = displayConfig
                },
                onSaveClicked = { onSaveClicked(displayInput) }
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DisplayConfigPreview(){
    DisplayConfigItemList(
        displayConfig = DisplayConfig.getDefaultInstance(),
        enabled = true,
        focusManager = LocalFocusManager.current,
        onSaveClicked = { },
    )
}
