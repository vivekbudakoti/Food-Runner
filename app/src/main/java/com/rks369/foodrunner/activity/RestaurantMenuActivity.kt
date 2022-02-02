
package com.rks369.foodrunner.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.rks369.foodrunner.R
import com.rks369.foodrunner.adapter.HomeRecyclerAdapter
import com.rks369.foodrunner.adapter.RestaurantMenuAdapter
import com.rks369.foodrunner.database.OrderEntity
import com.rks369.foodrunner.database.RestaurantDatabase
import com.rks369.foodrunner.model.RestaurantMenu
import com.rks369.foodrunner.util.ConnectionManager
import com.rks369.foodrunner.util.SessionManager

class RestaurantMenuActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    lateinit var imgFav: ImageButton
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var coordinateLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var btnGoToCart: Button

    private val dishInfoList = arrayListOf<RestaurantMenu>()
    private val orderList = arrayListOf<RestaurantMenu>()

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerAdapter: RestaurantMenuAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var restaurantName: String
    lateinit var sessionManager: SessionManager

    var restaurantId: String = "100"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        recyclerView = findViewById(R.id.recyclerView)

        //setHasOptionsMenu(true)

        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility= View.VISIBLE
        btnGoToCart = findViewById(R.id.btnGoToCart)
        coordinateLayout = findViewById(R.id.coordinateLayout)
        toolbar = findViewById(R.id.toolbar)
        frameLayout = findViewById(R.id.frameLayout)
        imgFav = findViewById(R.id.imgFav)

        layoutManager = LinearLayoutManager(this@RestaurantMenuActivity)
        btnGoToCart.setOnClickListener {
            startActivity(Intent(this@RestaurantMenuActivity, CartActivity::class.java) )}


        if (intent != null) {
            restaurantId = intent.getStringExtra("res_id").toString()
            val listOfFav = HomeRecyclerAdapter.GetFavAsyncTask(this).execute().get()

            if (listOfFav.isNotEmpty() && listOfFav.contains(restaurantId)) {

                imgFav.setBackgroundResource(R.drawable.ic_fiill_favourite)
            } else {
                imgFav.setBackgroundResource(R.drawable.ic_favourite)
            }


        } else {
            finish()
            Toast.makeText(
                this,
                "Some unexpected Error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }
        if (restaurantId == "100") {
            finish()
            Toast.makeText(
                this,
                "Some unexpected Error occurred!",
                Toast.LENGTH_SHORT
            ).show()
        }

        val queue= Volley.newRequestQueue(this@RestaurantMenuActivity)
        val url= "http://13.235.250.119/v2/restaurants/fetch_result/$restaurantId"

        if(ConnectionManager().checkConnectivity(this@RestaurantMenuActivity)){

            val jsonObjectRequest=object :JsonObjectRequest(Method.GET,url,null,Response.Listener {
                print("Result is $it")
                try {
                    val obj2 = it.getJSONObject("data")
                    val success = obj2.getBoolean("success")
                    if (success) {
                        orderList.clear()
                        val data = obj2.getJSONArray("data")
                        progressLayout.visibility = View.GONE
                        for (i in 0 until data.length()) {
                            val dishJsonObject = data.getJSONObject(i)
                            val dishObject = RestaurantMenu(

                                dishJsonObject.getString("id"),
                                dishJsonObject.getString("name"),
                                dishJsonObject.getString("cost_for_one"),
                                dishJsonObject.getString("restaurant_id")
                            )

                            dishInfoList.add(dishObject)
                            recyclerAdapter = RestaurantMenuAdapter(this@RestaurantMenuActivity, dishInfoList)

                            recyclerView.adapter = recyclerAdapter
                            recyclerView.layoutManager = layoutManager
                        }
                    } else {
                        Toast.makeText(
                            this@RestaurantMenuActivity,
                            "Some Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RestaurantMenuActivity, "Some Exception Occurred $e", Toast.LENGTH_SHORT).show()
                }
            },Response.ErrorListener {
                Toast.makeText(this@RestaurantMenuActivity, "Volley error", Toast.LENGTH_SHORT).show()
            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "9bf534118365f1"
                    return headers
                }

            }
            queue.add(jsonObjectRequest)

        }else {
            val dialog = AlertDialog.Builder(this@RestaurantMenuActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }

            dialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(this@RestaurantMenuActivity)
            }
            dialog.create()
            dialog.show()
        }

    }
    class CartItems(context: Context, private val restaurantId:String, private val foodItems:String, val mode:Int):
        AsyncTask<Void, Void, Boolean>(){
        val db= Room.databaseBuilder(context, RestaurantDatabase::class.java,"restaurants-db").build()
        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {
                1 -> {
                    db.orderDao().insertOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }

                2 -> {
                    db.orderDao().deleteOrder(OrderEntity(restaurantId, foodItems))
                    db.close()
                    return true
                }
                3->{

                    val order : OrderEntity =db.orderDao().checkById(restaurantId.toString(),foodItems.toString())
                    db.close()
                    return order !=null
                }
            }
            return false
        }
    }



}