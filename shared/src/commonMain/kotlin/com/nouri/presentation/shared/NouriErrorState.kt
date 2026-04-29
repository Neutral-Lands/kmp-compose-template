package com.nouri.presentation.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nouri.domain.model.DomainError
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun NouriErrorState(
    error: DomainError,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error.toUserMessage(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun DomainError.toUserMessage(): String =
    when (this) {
        is DomainError.NetworkError -> "No internet connection. Check your network and try again."
        is DomainError.AuthError -> "Your session has expired. Please sign in again."
        is DomainError.NotFoundError -> "The requested resource was not found."
        is DomainError.UnknownError -> "Something went wrong. Please try again."
    }

@Preview
@Composable
fun NouriErrorStatePreview() {
    NouriTheme {
        NouriErrorState(
            error = DomainError.NetworkError(),
            onRetry = {},
        )
    }
}
