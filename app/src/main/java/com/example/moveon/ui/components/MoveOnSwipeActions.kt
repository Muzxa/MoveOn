package com.example.moveon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveOnSwipeActionBox(
    modifier: Modifier = Modifier,
    onSwipeStart: () -> Unit,
    onSwipeEnd: () -> Unit,
    startBackgroundColor: Color,
    endBackgroundColor: Color,
    idleBackgroundColor: Color,
    backgroundShape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    startContent: @Composable RowScope.() -> Unit,
    endContent: @Composable RowScope.() -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipeStart()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipeEnd()
                    false
                }

                SwipeToDismissBoxValue.Settled -> true
            }
        }
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            val backgroundColor = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> startBackgroundColor
                SwipeToDismissBoxValue.EndToStart -> endBackgroundColor
                SwipeToDismissBoxValue.Settled -> idleBackgroundColor
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, backgroundShape)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        startContent()
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        endContent()
                    }
                }
            }
        }
    ) {
        content()
    }
}
