package com.rks369.foodrunner.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.rks369.foodrunner.R
import com.rks369.foodrunner.adapter.HomeRecyclerAdapter
import com.rks369.foodrunner.database.RestaurantDatabase
import com.rks369.foodrunner.database.RestaurantEntity
import com.rks369.foodrunner.model.Restaurant

class FavouritesFragment : Fragment() {
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerFavourite: RecyclerView
    private lateinit var recyclerAdapter: HomeRecyclerAdapter
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var noFav:RelativeLayout
    var dbResList= arrayListOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_favourites, container, false)

        recyclerFavourite = view.findViewById(R.id.favouriteRecyclerView)
        layoutManager = LinearLayoutManager(activity)


         noFav=view.findViewById(R.id.noFav)
        noFav.visibility=View.GONE
        progressLayout = view.findViewById(R.id.progressLayout)

        progressBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE

        val resList = RetrieveFavourites(activity as Context).execute().get()

        if(resList.isEmpty()){
           noFav.visibility=View.VISIBLE
            progressLayout.visibility=View.GONE
        }
        else{
            for (i in resList){
            dbResList.add(
                Restaurant(
                    i.RestaurantId.toString(),
                    i.RestaurantName,
                    i.Ratings,
                    i.Cost,
                    i.Image
                )
            )
        }
        }

        if (activity != null) {
            progressLayout.visibility = View.GONE
            recyclerAdapter = HomeRecyclerAdapter(activity as Context, dbResList)
            recyclerFavourite.adapter = recyclerAdapter
            recyclerFavourite.layoutManager = layoutManager
        }
        recyclerAdapter.notifyDataSetChanged()


        return view

    }
    class RetrieveFavourites(val context: Context) : AsyncTask<Void, Void, List<RestaurantEntity>>() {

        override fun doInBackground(vararg p0: Void?): List<RestaurantEntity> {
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurants-db").build()

            return db.restaurantDao().getAllRestaurants()
        }

    }
}