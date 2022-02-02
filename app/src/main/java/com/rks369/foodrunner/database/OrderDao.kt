package com.rks369.foodrunner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderDao{

    @Insert
    fun insertOrder(orderEntity: OrderEntity)

    @Delete
    fun deleteOrder(orderEntity: OrderEntity)

    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<OrderEntity>

    @Query("DELETE FROM orders WHERE res_Id = :resId")
    fun deleteOrders(resId: String)

    @Query("SELECT * from orders WHERE res_id=:resId And foodId=:foodId")
    fun checkById(resId: String,foodId:String):OrderEntity
}