package com.theseuntaylor.stackboxes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theseuntaylor.stackboxes.ui.theme.StackBoxesTheme
import com.theseuntaylor.stackboxes.ui.theme.activeColor
import com.theseuntaylor.stackboxes.ui.theme.inactiveColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val data = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackBoxesTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                // A surface container using the 'background' color from the theme
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .systemBarsPadding(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Boxes(data = data, snackbarHostState = snackbarHostState)
                    }
                }
            }
        }
    }
}

@Composable
fun Boxes(
    data: List<String>, modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState
) {
    // Indices of the tapped boxes, in tap order; the top of the stack is the last element.
    // A box is lit exactly when its index is in the stack.
    val boxStack = remember { mutableStateListOf<Int>() }
    val isResetting = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        LazyVerticalGrid(
            state = rememberLazyGridState(), columns = GridCells.Fixed(3)
        ) {
            itemsIndexed(data) { index, item ->
                Box(
                    modifier = modifier
                        .width(50.dp)
                        .clickable {
                            coroutineScope.launch {
                                handleClicksAndState(
                                    stack = boxStack,
                                    capacity = data.size,
                                    index = index,
                                    isResetting = isResetting,
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                        .padding(5.dp)
                        .border(
                            width = 3.dp,
                            color = if (index in boxStack) activeColor else inactiveColor,
                            shape = RectangleShape
                        ), contentAlignment = Alignment.Center) {
                    Text(
                        text = item,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

// On every tap we push the box onto the stack. Once the stack is full we pop it
// back down one box at a time, ignoring taps until it is empty again.
private suspend fun handleClicksAndState(
    stack: SnapshotStateList<Int>,
    capacity: Int,
    index: Int,
    isResetting: MutableState<Boolean>,
    snackbarHostState: SnackbarHostState
) {
    if (isResetting.value || index in stack) return

    stack.add(index)

    if (stack.size == capacity) {
        isResetting.value = true
        try {
            while (stack.isNotEmpty()) {
                delay(500)
                stack.removeAt(stack.lastIndex)
            }
        } finally {
            isResetting.value = false
        }
        snackbarHostState.showSnackbar(
            message = "All boxes have been reset!", duration = SnackbarDuration.Short
        )
    }
}
