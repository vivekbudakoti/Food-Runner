package com.rks369.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val foodId: String,
    @ColumnInfo(name = "res_id")val resId:String

)
