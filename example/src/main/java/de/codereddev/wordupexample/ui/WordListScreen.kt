package de.codereddev.wordupexample.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.codereddev.wordup.database.Word
import de.codereddev.wordupexample.ui.theme.WordUpExampleTheme

@Composable
fun WordListScreen(viewModel: WordListViewModel) {
    val context = LocalContext.current

    WordListScreenContent(
        words = viewModel.wordList.collectAsStateWithLifecycle().value,
        onWordClick = { word -> viewModel.onWordClick(context, word) },
        onMenuItemSelected = { word, action -> viewModel.onMenuItemSelected(context, word, action) }
    )
}

@Composable
private fun WordListScreenContent(
    words: List<Word>,
    onWordClick: (Word) -> Unit,
    onMenuItemSelected: (Word, WordAction) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        ) {
            items(words, key = { it.id }) { word ->
                WordItem(
                    word = word,
                    onClick = { onWordClick(word) },
                    onMenuItemSelected = { action -> onMenuItemSelected(word, action) }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_9)
@Composable
private fun WordListScreenContentPreview() {
    val words = listOf(
        Word(id = 1, name = "Hello", path = ""),
        Word(id = 2, name = "World", path = ""),
        Word(id = 3, name = "Foo", path = ""),
        Word(id = 4, name = "Bar", path = ""),
        Word(id = 5, name = "Baz", path = ""),
        Word(id = 6, name = "Qux", path = "")
    )
    WordUpExampleTheme {
        WordListScreenContent(words = words, onWordClick = {}, onMenuItemSelected = { _, _ -> })
    }
}