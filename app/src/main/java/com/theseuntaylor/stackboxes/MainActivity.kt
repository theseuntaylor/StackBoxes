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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
import java.util.Stack

class MainActivity : ComponentActivity() {

    val data = listOf("1", "2", "3", "4", "5" , "6", "7", "8", "9")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackBoxesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Boxes(data = data)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun Boxes(
    data: List<String>,
    modifier: Modifier = Modifier,
) {
    val boxStack = Stack<String>()
    val boxesState = remember { mutableStateListOf(false, false, false,false, false, false,false, false, false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LazyVerticalGrid(
            state = rememberLazyGridState(),
            columns = GridCells.Fixed(3)
        ) {
            items(data) { item ->
                val i = data.indexOf(item)
                Box(
                    modifier = modifier
                        .width(50.dp)
                        .clickable {
                            handleClicksAndState(
                                stack = boxStack,
                                boxesState = boxesState,
                                arraySize = data,
                                item = item,
                                indx = i
                            )

                        }
                        .padding(5.dp)
                        .border(
                            width = 3.dp,
                            color = if (boxesState[i]) activeColor else inactiveColor,
                            shape = RectangleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
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

// takes in stack and a boolean. at every tap, we check if the stack is filled up,
// if it is, we start to pop, else we change the state of the box and finish?
private fun handleClicksAndState(
    stack: Stack<String>,
    boxesState: SnapshotStateList<Boolean>,
    arraySize: List<String>,
    item: String,
    indx: Int,
) {
    // if we have gotten to the max,
    // [1, 2, 3]
    // [true, true, true]

    var index = indx
    if (stack.size < arraySize.size) {
        if (!stack.contains(item)) {
            stack.push(item)
            boxesState[index] = !boxesState[index]
            if (stack.size == arraySize.size) {
                do {
                    val latest = stack.peek()
                    var indexOfLatest = arraySize.indexOf(latest)

                    // so, i want to pop the latest item that was put into the stack, right?
                    // what this means is i can find the latest item at any point in time.
                    // how can i find it? stack.peek()
                    // i get the latest item and then look for it in the list of items i am being sent.
                    // then i want to get the index of that item and tie it to the list of states.

                    stack.pop()
                    boxesState[indexOfLatest] = !boxesState[indexOfLatest]
                } while (stack.size > 0)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StackBoxesTheme {
        Greeting("Android")
    }
}