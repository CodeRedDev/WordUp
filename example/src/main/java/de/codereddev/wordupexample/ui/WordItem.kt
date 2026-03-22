package de.codereddev.wordupexample.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.codereddev.wordup.database.Word
import de.codereddev.wordupexample.R
import de.codereddev.wordupexample.ui.theme.WordUpExampleTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WordItem(
    word: Word,
    onClick: () -> Unit,
    onMenuItemSelected: (WordAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dropdownVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(onClick = onClick, onLongClick = { dropdownVisible = true })
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_button_blue),
                contentDescription = word.name,
                modifier = Modifier.size(64.dp),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = word.name,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }

        DropdownMenu(
            expanded = dropdownVisible,
            onDismissRequest = { dropdownVisible = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_set_ringtone)) },
                onClick = {
                    dropdownVisible = false
                    onMenuItemSelected(WordAction.SET_RINGTONE)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_set_notification)) },
                onClick = {
                    dropdownVisible = false
                    onMenuItemSelected(WordAction.SET_NOTIFICATION)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_set_alarm)) },
                onClick = {
                    dropdownVisible = false
                    onMenuItemSelected(WordAction.SET_ALARM)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_share)) },
                onClick = {
                    dropdownVisible = false
                    onMenuItemSelected(WordAction.SHARE)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_save)) },
                onClick = {
                    dropdownVisible = false
                    onMenuItemSelected(WordAction.SAVE)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WordItemPreview() {
    WordUpExampleTheme {
        WordItem(
            word = Word(id = 1, name = "Hello", path = ""),
            onClick = {},
            onMenuItemSelected = { _ -> }
        )
    }
}