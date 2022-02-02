package com.rks369.foodrunner.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.rks369.foodrunner.R
import com.rks369.foodrunner.activity.RestaurantMenuActivity
import com.rks369.foodrunner.database.RestaurantDatabase
import com.rks369.foodrunner.database.RestaurantEntity
import com.rks369.foodrunner.model.Restaurant
import com.squareup.picasso.Picasso


class HomeRecyclerAdapter(val context:Context, private val itemList:ArrayList<Restaurant>): RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_home_single_row,parent,false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val restaurant=itemList[position]
        holder.txtRestaurant.text=restaurant.RestaurantName
        holder.txtPrice.text= restaurant.FoodPrice
        holder.txtRestaurantRatings.text=restaurant.RestaurantRating
        holder.rlContent.setOnClickListener {
            val intent=Intent(context,RestaurantMenuActivity::class.java)
            intent.putExtra("res_id",restaurant.RestaurantId)
            context.startActivity(intent)

        }
        Picasso.get().load(restaurant.RestaurantImage).error(R.mipmap.ic_launcher).into(holder.imgRestaurant)
        val listOfFav = GetFavAsyncTask(context).execute().get()

        if (listOfFav.isNotEmpty() && listOfFav.contains(restaurant.RestaurantId.toString())) {
            holder.imgFav.setBackgroundResource(R.drawable.ic_fiill_favourite)
        } else {
            holder.imgFav.setBackgroundResource(R.drawable.ic_favourite)
        }

        holder.imgFav.setOnClickListener {
            val restaurantEntity = RestaurantEntity(
                restaurant.RestaurantId.toInt(),
                restaurant.RestaurantName,
                restaurant.RestaurantRating,
                restaurant.FoodPrice,
                restaurant.RestaurantImage
            )
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async = DBAsyncTask(context, restaurantEntity, 2).execute()
                val data = async.get()
                if (data) {
                    Toast.makeText(context,"Added to favourites", Toast.LENGTH_SHORT).show()
                    holder.imgFav.setBackgroundResource(R.drawable.ic_fiill_favourite)
                }
            } else {
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val data = async.get()

                if (data) {
                    Toast.makeText(context,"Removed favourites",Toast.LENGTH_SHORT).show()
                    holder.imgFav.setBackgroundResource(R.drawable.ic_favourite)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val txtRestaurant:TextView= view.findViewById(R.id.txtRestaurantName)
        val txtPrice:TextView=view.findViewById(R.id.txtPrice)
        val imgRestaurant:ImageView=view.findViewById(R.id.imgRestaurant)
        val txtRestaurantRatings:TextView=view.findViewById(R.id.txtRestaurantRating)
        val imgFav:ImageView=view.findViewById(R.id.imgFavourite)
        val llContent:LinearLayout=view.findViewById(R.id.llContent)
        val rlContent:RelativeLayout=view.findViewById(R.id.rlContent)

    }

    class GetFavAsyncTask(context: Context) : AsyncTask<Void, Void, List<String>>() {
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db")
            .build()

        override fun doInBackground(vararg params: Void?): List<String> {
            val list = db.restaurantDao().getAllRestaurants()
            val listOfIds = arrayListOf<String>()
            for (i in list) {
                listOfIds.add(i.RestaurantId.toString())
            }
            return listOfIds
        }
    }

    class DBAsyncTask(val context: Context,val restaurantEntity: RestaurantEntity,val mode:Int) :
        AsyncTask<Void, Void, Boolean>(){


/*
       Mode 1 -> Check DB if the restaurant is favourite or not
       Mode 2 -> Save the restaurant into DB as favourite
       Mode 3 -> Remove the favourite restaurant
       * */

        val db= Room.databaseBuilder(context, RestaurantDatabase::class.java,"restaurants-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode){
                1-> {
                    // Check DB if the restaurant is favourite or not
                    val restaurant :RestaurantEntity=db.restaurantDao().getRestaurantById(restaurantEntity.RestaurantId.toString())
                    db.close()
                    return restaurant!=null

                }
                2-> {
                    //Save the restaurant into DB as favourite
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3-> {
                    //Remove From Favourites
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false

        }
    }
}