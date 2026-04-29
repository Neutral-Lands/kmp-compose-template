package com.nouri.presentation.web.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nouri.presentation.shared.NouriTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val SidebarWidth = 220.dp
private val ItemVerticalPadding = 12.dp
private val ItemHorizontalPadding = 16.dp
private val IconSize = 20.dp
private val IconTextGap = 12.dp

@Composable
fun WebSidebar(
    selectedRoute: WebRoute,
    onNavigate: (WebRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .widthIn(min = SidebarWidth, max = SidebarWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = "Nouri",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    horizontal = ItemHorizontalPadding,
                    vertical = ItemVerticalPadding,
                ),
        )

        WebRoute.entries.forEach { route ->
            WebSidebarItem(
                route = route,
                selected = route == selectedRoute,
                onClick = { onNavigate(route) },
            )
        }
    }
}

@Composable
private fun WebSidebarItem(
    route: WebRoute,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    val contentColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .background(containerColor)
                .clickable(onClick = onClick)
                .padding(horizontal = ItemHorizontalPadding, vertical = ItemVerticalPadding),
    ) {
        Icon(
            imageVector = route.icon,
            contentDescription = route.label,
            tint = contentColor,
            modifier = Modifier.size(IconSize),
        )
        Spacer(modifier = Modifier.width(IconTextGap))
        Text(
            text = route.label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
        )
    }
}

@Preview
@Composable
fun WebSidebarPreview() {
    NouriTheme {
        WebSidebar(
            selectedRoute = WebRoute.Dashboard,
            onNavigate = {},
        )
    }
}
