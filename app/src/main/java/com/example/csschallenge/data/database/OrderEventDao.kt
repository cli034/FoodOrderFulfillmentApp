package com.example.csschallenge.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderEvents(events: List<OrderEventEntity>)

    @Query("SELECT * FROM order_events WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getOrderHistoryFlow(orderId: String): Flow<List<OrderEventEntity>>

    @Query(
        """
        SELECT * FROM order_events e1 
        WHERE e1.timestamp = (
            SELECT MAX(e2.timestamp) 
            FROM order_events e2 
            WHERE e2.orderId = e1.orderId
        )
    """
    )
    fun getLatestOrderEventsFlow(): Flow<List<OrderEventEntity>>

    @Query("DELETE FROM order_events")
    suspend fun clearAllOrderEvents()
}