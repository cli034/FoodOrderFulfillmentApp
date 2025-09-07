package com.example.csschallenge.viewmodel

import app.cash.turbine.test
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventShelf
import com.example.csschallenge.domain.model.OrderEventState
import com.example.csschallenge.domain.usecase.RetrieveOrderHistoryUseCase
import com.example.csschallenge.testing.MainDispatcherRule
import com.example.csschallenge.testing.respositories.TestOrderRepoImpl
import com.example.csschallenge.ui.orderEvents.details.ChangeType
import com.example.csschallenge.ui.orderEvents.details.OrderEventsDetailUiState
import com.example.csschallenge.ui.orderEvents.details.OrderEventsDetailViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderEventsDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var orderRepo: TestOrderRepoImpl
    private lateinit var retrieveOrderHistoryUseCase: RetrieveOrderHistoryUseCase

    @Before
    fun setup() {
        orderRepo = TestOrderRepoImpl()
        retrieveOrderHistoryUseCase = RetrieveOrderHistoryUseCase(orderRepo = orderRepo)
    }

    private fun initializeViewModel(): OrderEventsDetailViewModel {
        return OrderEventsDetailViewModel(
            retrieveOrderHistoryUseCase = retrieveOrderHistoryUseCase
        )
    }

    @Test
    fun `should emit NotFound state when order history is empty`() = runTest {
        // Given
        val orderId = "9999"
        orderRepo.setOrderEvents(emptyList())

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsDetailUiState.Loading, awaitItem())

            // Load order detail with non-existent order - should get NotFound
            viewModel.loadOrderDetail(orderId)
            assertTrue(awaitItem() is OrderEventsDetailUiState.NotFound)
        }
    }

    @Test
    fun `should emit Success state with correct data when order history exists`() = runTest {
        // Given
        val orderId = "123"
        val orderEvents = listOf(
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.CREATED,
                shelf = OrderEventShelf.NONE,
                timestamp = 1000L,
            ),
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.COOKING,
                shelf = OrderEventShelf.HOT,
                timestamp = 2000L,
            ),
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.DELIVERED,
                shelf = OrderEventShelf.HOT,
                timestamp = 3000L,
            )
        )
        orderRepo.setOrderEvents(orderEvents)

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsDetailUiState.Loading, awaitItem())

            // Load order detail - should get Success with filtered data
            viewModel.loadOrderDetail(orderId)

            // Should emit Success state
            val successState = awaitItem() as OrderEventsDetailUiState.Success
            val uiModel = successState.uiModel

            // Verify current order is the last event
            assertEquals(OrderEventState.DELIVERED, uiModel.currentOrder.state)
            assertEquals(OrderEventShelf.HOT, uiModel.currentOrder.shelf)

            // Verify complete order history
            assertEquals(3, uiModel.orderHistory.size)
            assertEquals(orderId, uiModel.orderHistory.first().id)

            // Verify changelog generation
            assertEquals(3, uiModel.changelog.size)

            // Most recent first (reversed order)
            assertEquals(
                ChangeType.STATE_CHANGED,
                uiModel.changelog[0].changeType
            ) // COOKING -> DELIVERED
            assertEquals(
                ChangeType.BOTH_CHANGED,
                uiModel.changelog[1].changeType
            )  // CREATED -> COOKING + NONE -> HOT
            assertEquals(ChangeType.ORDER_CREATED, uiModel.changelog[2].changeType) // Order created
        }
    }

    @Test
    fun `should filter order history by orderId correctly`() = runTest {
        // Given
        val targetOrderId = "123"
        val otherOrderId = "456"
        val orderEvents = listOf(
            createTestOrderEvent(id = targetOrderId, state = OrderEventState.CREATED),
            createTestOrderEvent(id = otherOrderId, state = OrderEventState.CREATED),
            createTestOrderEvent(id = targetOrderId, state = OrderEventState.COOKING),
            createTestOrderEvent(id = otherOrderId, state = OrderEventState.DELIVERED),
            createTestOrderEvent(id = targetOrderId, state = OrderEventState.DELIVERED)
        )
        orderRepo.setOrderEvents(orderEvents)

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsDetailUiState.Loading, awaitItem())

            // Load specific order detail
            viewModel.loadOrderDetail(targetOrderId)

            // Should emit Success state with filtered history
            val successState = awaitItem() as OrderEventsDetailUiState.Success
            val uiModel = successState.uiModel

            // Should only contain events for target order
            assertEquals(3, uiModel.orderHistory.size)
            assertTrue(uiModel.orderHistory.all { it.id == targetOrderId })

            // Verify states are correct for this order
            assertEquals(OrderEventState.CREATED, uiModel.orderHistory[0].state)
            assertEquals(OrderEventState.COOKING, uiModel.orderHistory[1].state)
            assertEquals(OrderEventState.DELIVERED, uiModel.orderHistory[2].state)
        }
    }

    @Test
    fun `should generate correct changelog for order lifecycle`() = runTest {
        // Given, complete order lifecycle with various changes
        val orderId = "123"
        val orderEvents = listOf(
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.CREATED,
                shelf = OrderEventShelf.NONE,
                timestamp = 1000L
            ),
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.COOKING,
                shelf = OrderEventShelf.HOT,
                timestamp = 2000L
            ),
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.COOKING,
                shelf = OrderEventShelf.COLD, // Only shelf changed
                timestamp = 3000L
            ),
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.WAITING,
                shelf = OrderEventShelf.COLD, // Only state changed
                timestamp = 4000L
            ),
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.DELIVERED,
                shelf = OrderEventShelf.HOT, // Both changed
                timestamp = 5000L
            )
        )
        orderRepo.setOrderEvents(orderEvents)

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsDetailUiState.Loading, awaitItem())

            viewModel.loadOrderDetail(orderId)

            val successState = awaitItem() as OrderEventsDetailUiState.Success
            val changelog = successState.uiModel.changelog

            // Should have 5 changelog entries (most recent first)
            assertEquals(5, changelog.size)

            // Verify changelog order and types (most recent first due to reverse)
            assertEquals(
                ChangeType.BOTH_CHANGED,
                changelog[0].changeType
            )   // WAITING->DELIVERED + COLD->HOT
            assertEquals(OrderEventState.DELIVERED, changelog[0].state)
            assertEquals(OrderEventShelf.HOT, changelog[0].shelf)

            assertEquals(ChangeType.STATE_CHANGED, changelog[1].changeType)  // COOKING->WAITING
            assertEquals(OrderEventState.WAITING, changelog[1].state)
            assertEquals(OrderEventShelf.COLD, changelog[1].shelf)

            assertEquals(ChangeType.SHELF_CHANGED, changelog[2].changeType)  // HOT->COLD
            assertEquals(OrderEventState.COOKING, changelog[2].state)
            assertEquals(OrderEventShelf.COLD, changelog[2].shelf)

            assertEquals(
                ChangeType.BOTH_CHANGED,
                changelog[3].changeType
            )   // CREATED->COOKING + NONE->HOT
            assertEquals(OrderEventState.COOKING, changelog[3].state)
            assertEquals(OrderEventShelf.HOT, changelog[3].shelf)

            assertEquals(ChangeType.ORDER_CREATED, changelog[4].changeType)  // Order created
            assertEquals(OrderEventState.CREATED, changelog[4].state)
            assertEquals(OrderEventShelf.NONE, changelog[4].shelf)
        }
    }

    @Test
    fun `should handle order with no changes correctly`() = runTest {
        // Given, single order event with no subsequent changes
        val orderId = "123"
        val orderEvents = listOf(
            createTestOrderEvent(
                id = orderId,
                state = OrderEventState.CREATED,
                shelf = OrderEventShelf.NONE,
                timestamp = 1000L
            )
        )
        orderRepo.setOrderEvents(orderEvents)

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsDetailUiState.Loading, awaitItem())

            viewModel.loadOrderDetail(orderId)

            val successState = awaitItem() as OrderEventsDetailUiState.Success
            val uiModel = successState.uiModel

            // Should have single order in history
            assertEquals(1, uiModel.orderHistory.size)
            assertEquals(orderId, uiModel.currentOrder.id)

            // Should have only one changelog entry for order creation
            assertEquals(1, uiModel.changelog.size)
            assertEquals(ChangeType.ORDER_CREATED, uiModel.changelog[0].changeType)
        }
    }

    @Test
    fun `should handle state transitions when order data changes`() = runTest {
        // Given - initially empty repository
        val orderId = "123"
        orderRepo.setOrderEvents(emptyList())
        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsDetailUiState.Loading, awaitItem())

            // Load non-existent order - should get NotFound
            viewModel.loadOrderDetail(orderId)
            assertTrue(awaitItem() is OrderEventsDetailUiState.NotFound)

            // Now add order events and load again
            orderRepo.setOrderEvents(
                listOf(
                    createTestOrderEvent(id = orderId, state = OrderEventState.CREATED)
                )
            )

            // Load same order again - should now get Success
            viewModel.loadOrderDetail(orderId)
            val successState = awaitItem() as OrderEventsDetailUiState.Success
            assertEquals(orderId, successState.uiModel.currentOrder.id)
        }
    }

    private fun createTestOrderEvent(
        id: String = "test-id",
        state: OrderEventState = OrderEventState.COOKING,
        price: Int = 1000,
        item: String = "Test Item",
        customer: String = "Test Customer",
        shelf: OrderEventShelf = OrderEventShelf.HOT,
        timestamp: Long = System.currentTimeMillis(),
        destination: String = "Test Address"
    ): DomainOrderEvent {
        return DomainOrderEvent(
            id = id,
            state = state,
            price = price,
            item = item,
            customer = customer,
            shelf = shelf,
            timestamp = timestamp,
            destination = destination
        )
    }
}