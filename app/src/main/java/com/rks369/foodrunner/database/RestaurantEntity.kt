package com.rks369.foodrunner.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Restaurants")
data class RestaurantEntity(
    @PrimaryKey val RestaurantId:Int,
    @ColumnInfo(name = "Restaurant name") val RestaurantName : String,
    @ColumnInfo(name = "Ratings")val Ratings:String,
    @ColumnInfo(name = "Cost") val Cost :String,
    @ColumnInfo(name = "Image") val Image :String


)
