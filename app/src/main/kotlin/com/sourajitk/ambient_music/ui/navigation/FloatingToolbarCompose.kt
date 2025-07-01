package com.sourajitk.ambient_music.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Helper data class for the action buttons
data class QuickActions(val icon: ImageVector, val label: String, val onAction: () -> Unit)

@Composable
fun FloatingToolbarCompose(
  modifier: Modifier = Modifier,
  onFabClick: () -> Unit,
  items: List<QuickActions>,
) {
  var isMenuExpanded by remember { mutableStateOf(false) }
  val rotationAngle by animateFloatAsState(targetValue = if (isMenuExpanded) 45f else 0f)
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.End,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    AnimatedVisibility(
      visible = isMenuExpanded,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
      Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        items.forEach { item -> SmallFabRow(item = item) }
      }
    }
    FloatingActionButton(
      onClick = {
        isMenuExpanded = !isMenuExpanded
        onFabClick()
      },
      containerColor = MaterialTheme.colorScheme.primary,
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Toggle menu",
        modifier = Modifier.rotate(rotationAngle),
      )
    }
  }
}

@Composable
private fun SmallFabRow(item: QuickActions) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    if (item.label.isNotBlank()) {
      Text(text = item.label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
    }
    SmallFloatingActionButton(
      onClick = item.onAction,
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
      Icon(imageVector = item.icon, contentDescription = item.label)
    }
  }
}
