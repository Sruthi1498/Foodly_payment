package com.example.foodly.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.example.foodly.R
import com.example.foodly.adapter.RestaurantRecycleAdapter
import com.example.foodly.model.Restaurant
import com.example.foodly.util.ConnectionManager
import com.google.android.material.chip.Chip
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeFragment : Fragment() {
    lateinit var recyclerDashboard: RecyclerView

    lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var recyclerAdapter: RestaurantRecycleAdapter

    lateinit var homeProgress: RelativeLayout

    lateinit var searchBar: EditText

    lateinit var byName: Chip

    lateinit var byNameDesc: Chip

    lateinit var byRating: Chip

    lateinit var byPrice: Chip

    lateinit var byPriceDesc: Chip

    lateinit var imageSlider: ImageSlider

    val restaurantInfoList = arrayListOf<Restaurant>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        recyclerDashboard = view.findViewById(R.id.recyclerDashboard)
        layoutManager = LinearLayoutManager(activity)
        homeProgress = view.findViewById(R.id.progressHomeLayout)
        searchBar = view.findViewById(R.id.main_search_bar)
        imageSlider = view.findViewById(R.id.imageSlider)
        byRating = view.findViewById(R.id.byRating)
        byPrice = view.findViewById(R.id.byPrice)
        byName = view.findViewById(R.id.byName)
        byNameDesc = view.findViewById(R.id.byNameDesc)
        byPriceDesc = view.findViewById(R.id.byPriceDesc)

        val queue = Volley.newRequestQueue(activity as Context)
        val slideModel : ArrayList<SlideModel> = ArrayList()



        slideModel.add(SlideModel(R.drawable.slider1,""))

        slideModel.add(SlideModel(R.drawable.slider2,""))

        slideModel.add(SlideModel(R.drawable.slider3,""))

        slideModel.add(SlideModel(R.drawable.slider4,""))

        slideModel.add(SlideModel(R.drawable.slider1,""))

        imageSlider.setImageList(slideModel)



        val ratingComp = Comparator<Restaurant> { res1, res2 ->

            if (res1.restaurantRating.compareTo(res2.restaurantRating, true) == 0) {
                res1.restaurantName.compareTo(res2.restaurantRating, true)

            } else {

                res1.restaurantRating.compareTo(res2.restaurantRating, true)

            }



        }


        val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
        if (ConnectionManager().checkConnectivity(activity as Context)) {
            homeProgress.visibility = View.INVISIBLE
            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                    try {
                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")

                        if (success) {
                            val data = response.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = Restaurant(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )

                                restaurantInfoList.add(restaurantObject)
                                recyclerAdapter = RestaurantRecycleAdapter(
                                    activity as Context,
                                    restaurantInfoList
                                )
                                Collections.sort(restaurantInfoList, ratingComp)
                                restaurantInfoList.reverse()
                                recyclerAdapter.notifyDataSetChanged()

                                recyclerDashboard.adapter = recyclerAdapter
                                recyclerDashboard.layoutManager = layoutManager

                            }
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Some Error",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }, Response.ErrorListener {
                    if (activity != null) {
                        Toast.makeText(activity as Context, "Volley Error", Toast.LENGTH_SHORT)
                            .show()
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "9bf534118365f1"
                        return headers
                    }

                }
            queue.add(jsonObjectRequest)
        } else {
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("ERROR")
            dialog.setMessage("Internet Connection  Not Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }

            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val listData = arrayListOf<Restaurant>()
                for (i in 0 until restaurantInfoList.size) {
                    if (restaurantInfoList[i].restaurantName.toLowerCase()
                            .contains(searchBar.text.toString().toLowerCase()) ||
                        restaurantInfoList[i].restaurantPrice.contains(searchBar.text.toString()) ||
                        restaurantInfoList[i].restaurantRating.contains(searchBar.text.toString())
                    ) {
                        listData.add(restaurantInfoList[i])
                    }
                }
                recyclerDashboard.adapter = RestaurantRecycleAdapter(activity as Context, listData)
                (recyclerDashboard.adapter as RestaurantRecycleAdapter).notifyDataSetChanged()
            }
        }
        )
        val costComparator = Comparator<Restaurant> { rest1, rest2 ->
            rest1.restaurantPrice.compareTo(rest2.restaurantPrice, true)
        }
        val ratingCompName = Comparator<Restaurant> { item1, item2 ->
            if (item1.restaurantName.compareTo(item2.restaurantName, true) == 0) {
                item1.restaurantName.compareTo(item2.restaurantName, true)
            } else{
                item1.restaurantName.compareTo(item2.restaurantName, true)
            }
        }
        byPrice.setOnClickListener {
            Collections.sort(restaurantInfoList, costComparator)
            restaurantInfoList.reverse()
            recyclerAdapter.notifyDataSetChanged()
        }
        byPriceDesc.setOnClickListener {
            Collections.sort(restaurantInfoList, costComparator)
            recyclerAdapter.notifyDataSetChanged()

        }
        byRating.setOnClickListener {
            Collections.sort(restaurantInfoList, ratingComp)
            restaurantInfoList.reverse()
            recyclerAdapter.notifyDataSetChanged()

        }
        byName.setOnClickListener {
            Collections.sort(restaurantInfoList, ratingCompName)
            recyclerAdapter.notifyDataSetChanged()

        }
        byNameDesc.setOnClickListener {
            Collections.sort(restaurantInfoList, ratingCompName)
            restaurantInfoList.reverse()
            recyclerAdapter.notifyDataSetChanged()
        }
        return view
    }
}


