package com.theseuntaylor.stackboxes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theseuntaylor.stackboxes.ui.theme.StackBoxesTheme
import com.theseuntaylor.stackboxes.ui.theme.activeColor
import com.theseuntaylor.stackboxes.ui.theme.inactiveColor

class MainActivity : ComponentActivity() {

    val data = listOf("1", "2", "3", "4", "5" , "6", "7", "8", "9")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StackBoxesTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
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
    modifier: Modifier = Modifier
) {
    val isActive = true

    LazyVerticalGrid(
        columns = GridCells.Fixed(3)
    ) {
        items(data) { item ->
            Card(
                modifier = modifier.padding(4.dp)
                    .height(50.dp)
                    .border(
                        width = 1.dp,
                        color = if (isActive) activeColor else inactiveColor,
                        shape = RectangleShape
                        ),
                backgroundColor = Color.LightGray
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StackBoxesTheme {
        Greeting("Android")
    }
}