package com.example.csschallenge.ui.orderEvents

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventShelf
import com.example.csschallenge.ui.theme.CSSChallengeTheme
import com.example.csschallenge.R

@Composable
fun OrderEventsScreen(
    viewModel: OrderEventsViewModel,
    onOrderClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onOrderClick = onOrderClick,
        onRefreshClicked = { viewModel.refreshData() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    uiState: OrderEventsUiState,
    onRefreshClicked: () -> Unit,
    onOrderClick: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.order_events_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onRefreshClicked,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        OrderEventsContent(
            uiState = uiState,
            onOrderClick = onOrderClick,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun OrderEventsContent(
    uiState: OrderEventsUiState,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {
        is OrderEventsUiState.Loading -> {
            OrderEventsLoadingScreen()
        }

        is OrderEventsUiState.Empty -> {
            OrderEventsEmptyScreen()
        }

        is OrderEventsUiState.Error -> {
            OrderEventsErrorScreen()
        }

        is OrderEventsUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OrderStatisticsSection(statistics = uiState.uiModel.statistics)

                ShelfViewSection(
                    shelfItems = uiState.uiModel.shelfItems,
                    onOrderClick = onOrderClick
                )
            }
        }
    }
}

@Composable
private fun OrderStatisticsSection(
    statistics: OrderStatistics,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.order_statistics_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                title = stringResource(R.string.statistic_orders_delivered),
                value = statistics.ordersDelivered.toString(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            StatisticCard(
                title = stringResource(R.string.statistic_orders_trashed),
                value = statistics.ordersTrashed.toString(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )

            StatisticCard(
                title = stringResource(R.string.statistic_total_sales),
                value = stringResource(
                    R.string.currency_format,
                    statistics.formattedTotalSales,
                ),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )

            StatisticCard(
                title = stringResource(R.string.statistic_total_waste),
                value = stringResource(
                    R.string.currency_format,
                    statistics.formattedTotalWaste,
                ),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StatisticCard(
                title = stringResource(R.string.statistic_total_revenue),
                value = stringResource(
                    R.string.currency_format,
                    statistics.formattedTotalRevenue,
                ),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.widthIn(min = 120.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = contentColor
            )
        }
    }
}

@Composable
private fun ShelfViewSection(
    shelfItems: Map<OrderEventShelf, List<DomainOrderEvent>>,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.order_shelves_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OrderEventShelf.entries.forEachIndexed { index, shelf ->
            val items = shelfItems[shelf] ?: emptyList()

            ShelfCard(
                shelf = shelf,
                items = items,
                onOrderClick = onOrderClick
            )

            // Add spacing between shelf cards, but not after the last one
            if (index < OrderEventShelf.entries.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShelfCard(
    shelf: OrderEventShelf,
    items: List<DomainOrderEvent>,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shelfColor = when (shelf) {
        OrderEventShelf.HOT -> MaterialTheme.colorScheme.errorContainer
        OrderEventShelf.COLD -> MaterialTheme.colorScheme.primaryContainer
        OrderEventShelf.FROZEN -> MaterialTheme.colorScheme.surfaceVariant
        OrderEventShelf.OVERFLOW -> MaterialTheme.colorScheme.tertiaryContainer
        OrderEventShelf.NONE -> MaterialTheme.colorScheme.surfaceVariant
    }

    val onShelfColor = when (shelf) {
        OrderEventShelf.HOT -> MaterialTheme.colorScheme.onErrorContainer
        OrderEventShelf.COLD -> MaterialTheme.colorScheme.onPrimaryContainer
        OrderEventShelf.FROZEN -> MaterialTheme.colorScheme.onSurfaceVariant
        OrderEventShelf.OVERFLOW -> MaterialTheme.colorScheme.onTertiaryContainer
        OrderEventShelf.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = shelfColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shelf.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onShelfColor
                )

                Text(
                    text = stringResource(R.string.shelf_items_count, items.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = onShelfColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (items.isEmpty()) {
                // Empty state
                Text(
                    text = stringResource(R.string.shelf_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = onShelfColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp), // Limit height to prevent taking too much space
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(
                        items = items,
                        key = { it.id }
                    ) { item ->
                        OrderItemCard(
                            item = item,
                            onOrderClick = onOrderClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderItemCard(
    item: DomainOrderEvent,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { onOrderClick(item.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.item,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.order_number_format, item.id),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = item.state.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OrderEventsEmptyScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Kitchen,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.kitchen_closed_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.kitchen_closed_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OrderEventsErrorScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.kitchen_error_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.kitchen_error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OrderEventsLoadingScreen(
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
            Text(stringResource(R.string.loading_order_events))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsContentPreview_Success() {
    CSSChallengeTheme {
        OrderEventsContent(
            uiState = OrderEventsUiState.Success(
                uiModel = OrderEventsUiModel.preview()
            ),
            onOrderClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsContentPreview_Loading() {
    CSSChallengeTheme {
        OrderEventsContent(
            uiState = OrderEventsUiState.Loading,
            onOrderClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsContentPreview_Empty() {
    CSSChallengeTheme {
        OrderEventsContent(
            uiState = OrderEventsUiState.Empty,
            onOrderClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderEventsContentPreview_Error() {
    CSSChallengeTheme {
        OrderEventsContent(
            uiState = OrderEventsUiState.Error,
            onOrderClick = {}
        )
    }
}