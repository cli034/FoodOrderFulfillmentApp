package com.example.csschallenge.viewmodel

import app.cash.turbine.test
import com.example.csschallenge.domain.model.DomainOrderEvent
import com.example.csschallenge.domain.model.OrderEventShelf
import com.example.csschallenge.domain.model.OrderEventState
import com.example.csschallenge.domain.usecase.ResetDataUseCase
import com.example.csschallenge.domain.usecase.RetrieveOrderEventsUseCase
import com.example.csschallenge.testing.MainDispatcherRule
import com.example.csschallenge.testing.respositories.TestOrderRepoImpl
import com.example.csschallenge.ui.orderEvents.OrderEventsUiState
import com.example.csschallenge.ui.orderEvents.OrderEventsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OrderEventsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var orderRepo: TestOrderRepoImpl
    private lateinit var resetDataUseCase: ResetDataUseCase
    private lateinit var retrieveOrderEventsUseCase: RetrieveOrderEventsUseCase

    @Before
    fun setup() {
        orderRepo = TestOrderRepoImpl()
        resetDataUseCase = ResetDataUseCase(orderRepo = orderRepo)
        retrieveOrderEventsUseCase = RetrieveOrderEventsUseCase(orderRepo = orderRepo)
    }

    private fun initializeViewModel(): OrderEventsViewModel {
        return OrderEventsViewModel(
            retrieveOrderEventsUseCase = retrieveOrderEventsUseCase,
            resetDataUseCase = resetDataUseCase
        )
    }

    @Test
    fun `should emit Empty state when order events list is empty`() = runTest {
        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsUiState.Loading, awaitItem())

            // Set empty data - this should trigger Error state
            orderRepo.setOrderEvents(emptyList())

            // Should emit Error state
            assertIs<OrderEventsUiState.Empty>(awaitItem())
        }
    }

    @Test
    fun `should emit Success state with correct data when order events are received`() = runTest {
        // Given
        val orderEvents = listOf(
            createTestOrderEvent(
                id = "1",
                state = OrderEventState.COOKING,
                price = 1250,
                item = "Pizza",
                shelf = OrderEventShelf.HOT
            ),
            createTestOrderEvent(
                id = "2",
                state = OrderEventState.DELIVERED,
                price = 850,
                item = "Salad",
                shelf = OrderEventShelf.COLD
            )
        )

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsUiState.Loading, awaitItem())

            // Set test data, this triggers the flow
            orderRepo.setOrderEvents(orderEvents)

            // Should emit Success state
            val successState = awaitItem() as OrderEventsUiState.Success

            // Verify the data
            val hotShelfItems = successState.uiModel.shelfItems[OrderEventShelf.HOT]
            val coldShelfItems = successState.uiModel.shelfItems[OrderEventShelf.COLD]

            assertNotNull(hotShelfItems)
            assertNotNull(coldShelfItems)
            assertEquals(1, hotShelfItems.size)
            assertEquals(1, coldShelfItems.size)
            assertEquals("Pizza", hotShelfItems.first().item)
            assertEquals("Salad", coldShelfItems.first().item)
        }
    }

    @Test
    fun `should group orders by shelf correctly`() = runTest {
        // Given
        val orderEvents = listOf(
            createTestOrderEvent(id = "1", shelf = OrderEventShelf.HOT),
            createTestOrderEvent(id = "2", shelf = OrderEventShelf.HOT),
            createTestOrderEvent(id = "3", shelf = OrderEventShelf.COLD)
        )

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsUiState.Loading, awaitItem())

            // Set test data
            orderRepo.setOrderEvents(orderEvents)

            // Should emit Success state
            val successState = awaitItem() as OrderEventsUiState.Success
            val shelfItems = successState.uiModel.shelfItems

            assertEquals(2, shelfItems[OrderEventShelf.HOT]?.size)
            assertEquals(1, shelfItems[OrderEventShelf.COLD]?.size)
        }
    }

    @Test
    fun `should calculate statistics correctly for delivered and trashed orders`() = runTest {
        // Given
        val orderEvents = listOf(
            createTestOrderEvent(
                id = "1",
                state = OrderEventState.DELIVERED,
                price = 1000
            ), // $10.00
            createTestOrderEvent(
                id = "2",
                state = OrderEventState.DELIVERED,
                price = 1500
            ), // $15.00
            createTestOrderEvent(
                id = "3",
                state = OrderEventState.TRASHED,
                price = 800
            ), // $8.00
            createTestOrderEvent(
                id = "4",
                state = OrderEventState.COOKING,
                price = 1200
            ), // Should not count since it is still cooking
        )

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsUiState.Loading, awaitItem())

            // Set test data
            orderRepo.setOrderEvents(orderEvents)

            // Should emit Success state
            val successState = awaitItem() as OrderEventsUiState.Success
            val statistics = successState.uiModel.statistics

            assertEquals(2, statistics.ordersDelivered)
            assertEquals(1, statistics.ordersTrashed)
            assertEquals(1000 + 1500, statistics.totalSales)
            assertEquals(800, statistics.totalWaste)
            assertEquals(1700, statistics.totalRevenue) // $25.00 - $8.00
        }
    }

    @Test
    fun `should handle empty statistics correctly`() = runTest {
        // Given, only orders that don't count towards order statistics
        val orderEvents = listOf(
            createTestOrderEvent(id = "1", state = OrderEventState.COOKING),
            createTestOrderEvent(id = "2", state = OrderEventState.WAITING)
        )

        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsUiState.Loading, awaitItem())

            // Set test data
            orderRepo.setOrderEvents(orderEvents)

            // Should emit Success state
            val successState = awaitItem() as OrderEventsUiState.Success
            val statistics = successState.uiModel.statistics

            assertEquals(0, statistics.ordersDelivered)
            assertEquals(0, statistics.ordersTrashed)
            assertEquals(0, statistics.totalSales)
            assertEquals(0, statistics.totalWaste)
            assertEquals(0, statistics.totalRevenue)
        }
    }

    @Test
    fun `should handle state transitions when data changes`() = runTest {
        val viewModel = initializeViewModel()

        viewModel.uiState.test {
            // Initially should be Loading
            assertEquals(OrderEventsUiState.Loading, awaitItem())

            //set data, should get Success
            orderRepo.setOrderEvents(listOf(createTestOrderEvent(id = "1")))
            assertTrue(awaitItem() is OrderEventsUiState.Success)

            // clear data, should get Empty
            orderRepo.setOrderEvents(emptyList())
            assertTrue(awaitItem() is OrderEventsUiState.Empty)

            // add data again, should get Success
            orderRepo.setOrderEvents(listOf(createTestOrderEvent(id = "2")))
            assertTrue(awaitItem() is OrderEventsUiState.Success)
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