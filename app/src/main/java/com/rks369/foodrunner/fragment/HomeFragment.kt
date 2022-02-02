package com.rks369.foodrunner.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rks369.foodrunner.adapter.HomeRecyclerAdapter
import com.rks369.foodrunner.R
import com.rks369.foodrunner.model.Restaurant
import com.rks369.foodrunner.util.ConnectionManager
import org.json.JSONException
import java.util.*
import kotlin.collections.HashMap

class HomeFragment : Fragment() {

    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerHome: RecyclerView
    private lateinit var recyclerAdapter: HomeRecyclerAdapter
    private lateinit var progressLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar

    val restaurantList = arrayListOf<Restaurant>()
    private var ratingComparator = Comparator<Restaurant> { res1, res2 ->

        if (res1.RestaurantRating.compareTo(res2.RestaurantRating, true) == 0) {
            // sort according to name if rating is same
            res1.RestaurantName.compareTo(res2.RestaurantName, true)
        } else {
            res1.RestaurantRating.compareTo(res2.RestaurantRating, true)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)

        recyclerHome = view.findViewById(R.id.homeRecyclerView)
        layoutManager = LinearLayoutManager(activity)



        progressLayout = view.findViewById(R.id.progressLayout)

        progressBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObject = object : JsonObjectRequest(Method.GET, url, null, Response.Listener {
                print("response 2 is : $it")
                try {
                    progressLayout.visibility = View.GONE
                    val obj=it.getJSONObject("data")
                    val success = obj.getBoolean("success")
                    if (success) {

                        val data = obj.getJSONArray("data")
                        for (i in 0 until data.length()) {
                            val restaurantJsonObject = data.getJSONObject(i)
                            val restaurantObject = Restaurant(
                                restaurantJsonObject.getString("id"),
                                restaurantJsonObject.getString("name"),
                                restaurantJsonObject.getString("rating"),
                                restaurantJsonObject.getString("cost_for_one"),
                                restaurantJsonObject.getString("image_url")
                            )
                            restaurantList.add(restaurantObject)

                            recyclerAdapter =
                                HomeRecyclerAdapter(activity as Context, restaurantList)

                            recyclerHome.adapter = recyclerAdapter
                            recyclerHome.layoutManager = layoutManager

                        }

                    } else {
                        Toast.makeText(
                            activity as Context,
                            "Some Error Occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        activity as Context,
                        "Some unexpected error occurred!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }, Response.ErrorListener {

                //Here we will handle the errors
                if (activity != null) {
                    Toast.makeText(
                        activity as Context,
                        "Volley error occurred!",
                        Toast.LENGTH_SHORT
                    ).show()
                    print("response is : $it")
                }

            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }

            }
            queue.add(jsonObject)

        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }

            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if (id == R.id.action_sort) {
            Collections.sort(restaurantList, ratingComparator)
            restaurantList.reverse()
        }

        recyclerAdapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }


}


