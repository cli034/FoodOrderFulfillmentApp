package com.example.csschallenge.ui.orderEvents.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.csschallenge.R
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.ui.theme.CSSChallengeTheme
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderEventsDetailScreen(
    orderId: String,
    viewModel: OrderEventsDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    Content(
        uiState = uiState,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    uiState: OrderEventsDetailUiState,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.order_detail_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        OrderEventsDetailContent(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun OrderEventsDetailContent(
    uiState: OrderEventsDetailUiState,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is OrderEventsDetailUiState.Loading -> {
            OrderEventsDetailLoadingScreen()
        }

        is OrderEventsDetailUiState.NotFound -> {
            OrderEventsDetailNotFoundScreen()
        }

        is OrderEventsDetailUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OrderDetailHeaderSection(order = uiState.uiModel.currentOrder)

                OrderChangelogSection(changelog = uiState.uiModel.changelog)
            }
        }
    }
}

@Composable
private fun OrderDetailHeaderSection(
    order: DomainOrderEvent,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = order.item,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            OrderDetailRow(
                label = stringResource(R.string.order_detail_id),
                value = order.id
            )

            OrderDetailRow(
                label = stringResource(R.string.order_detail_customer),
                value = order.customer
            )

            OrderDetailRow(
                label = stringResource(R.string.order_detail_price),
                value = stringResource(
                    R.string.currency_format,
                    order.formattedPrice,
                )
            )

            OrderDetailRow(
                label = stringResource(R.string.order_detail_state),
                value = order.state.name
            )

            OrderDetailRow(
                label = stringResource(R.string.order_detail_shelf),
                value = order.shelf.name
            )

            if (order.destination.isNotEmpty()) {
                OrderDetailRow(
                    label = stringResource(R.string.order_detail_destination),
                    value = order.destination
                )
            }

            OrderDetailRow(
                label = stringResource(R.string.order_detail_last_updated),
                value = order.formattedTimestamp,
            )
        }
    }
}

@Composable
private fun OrderDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun OrderChangelogSection(
    changelog: List<OrderChangelogEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.order_detail_changelog_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (changelog.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.order_detail_no_changelog),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            changelog.forEach { entry ->
                ChangelogEntryCard(entry = entry)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ChangelogEntryCard(
    entry: OrderChangelogEntry,
    modifier: Modifier = Modifier
) {
    val cardColor = when (entry.changeType) {
        ChangeType.ORDER_CREATED -> MaterialTheme.colorScheme.primaryContainer
        ChangeType.STATE_CHANGED -> MaterialTheme.colorScheme.tertiaryContainer
        ChangeType.SHELF_CHANGED -> MaterialTheme.colorScheme.secondaryContainer
        ChangeType.BOTH_CHANGED -> MaterialTheme.colorScheme.errorContainer
    }

    val onCardColor = when (entry.changeType) {
        ChangeType.ORDER_CREATED -> MaterialTheme.colorScheme.onPrimaryContainer
        ChangeType.STATE_CHANGED -> MaterialTheme.colorScheme.onTertiaryContainer
        ChangeType.SHELF_CHANGED -> MaterialTheme.colorScheme.onSecondaryContainer
        ChangeType.BOTH_CHANGED -> MaterialTheme.colorScheme.onErrorContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                val descriptionText = when (entry.changeType) {
                    ChangeType.ORDER_CREATED -> stringResource(entry.descriptionResId)
                    ChangeType.STATE_CHANGED -> stringResource(
                        entry.descriptionResId,
                        entry.state.name,
                    )
                    ChangeType.SHELF_CHANGED -> stringResource(
                        entry.descriptionResId,
                        entry.shelf.name,
                    )
                    ChangeType.BOTH_CHANGED -> stringResource(
                        entry.descriptionResId,
                        entry.state.name,
                        entry.shelf.name,
                    )
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = onCardColor,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = entry.formattedTimestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = onCardColor
                )
            }
        }
    }
}

@Composable
private fun OrderEventsDetailLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.order_detail_loading))
        }
    }
}

@Composable
private fun OrderEventsDetailNotFoundScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.order_detail_not_found_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.order_detail_not_found_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsDetailContentPreview_Success() {
    CSSChallengeTheme {
        OrderEventsDetailContent(
            uiState = OrderEventsDetailUiState.Success(
                uiModel = OrderEventsDetailUiModel.preview()
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsDetailContentPreview_Loading() {
    CSSChallengeTheme {
        OrderEventsDetailContent(
            uiState = OrderEventsDetailUiState.Loading
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsDetailContentPreview_NotFound() {
    CSSChallengeTheme {
        OrderEventsDetailContent(
            uiState = OrderEventsDetailUiState.NotFound
        )
    }
}