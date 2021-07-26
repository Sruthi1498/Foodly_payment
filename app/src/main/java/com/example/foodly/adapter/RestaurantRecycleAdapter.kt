package com.example.foodly.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodly.R
import com.example.foodly.activity.RestaurantMenuActivity
import com.example.foodly.database.RestaurantDatabase
import com.example.foodly.database.RestaurantEntity
import com.example.foodly.model.Restaurant
import com.muddzdev.styleabletoast.StyleableToast
import com.squareup.picasso.Picasso

class RestaurantRecycleAdapter(val context: Context, private var itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<RestaurantRecycleAdapter.RestaurantViewHolder>() {
    class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val txtFav: TextView = view.findViewById(R.id.favTextView)
        val txtRestaurantName: TextView = view.findViewById(R.id.txtFoodName)
        val txtRestaurantRating: TextView = view.findViewById(R.id.txtFoodRating)
        val txtRestaurantPrice: TextView = view.findViewById(R.id.txtFoodPrice)
        val restaurantImage: ImageView = view.findViewById(R.id.foodImageView)
        val rlContent: RelativeLayout = view.findViewById(R.id.rlContent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_dashboard, parent, false)

        return RestaurantViewHolder(view)

    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {

        val restaurant = itemList[position]
        val restaurantEntity = RestaurantEntity(restaurant.restaurantId, restaurant.restaurantName)

        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtRestaurantRating.text = restaurant.restaurantRating
        holder.txtRestaurantPrice.text = "${restaurant.restaurantPrice}/Person"

        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.restaurant_image)
            .into(holder.restaurantImage)

        holder.rlContent.setOnClickListener {
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantId", restaurant.restaurantId)
            intent.putExtra("restaurantName", restaurant.restaurantName)
            intent.putExtra("restaurantImage",restaurant.restaurantImage.toString())
            intent.putExtra("restaurantPrice", restaurant.restaurantPrice.toString())
            intent.putExtra("restaurantRating", restaurant.restaurantRating.toString())
            context.startActivity(intent)
        }
        //TODO AsyncTask is depcrecated, should be avoided and This is not how it should be used
        val checkFav = DBAsyncTask(context, restaurantEntity, 1).execute()
        val isFav = checkFav.get()

        if (isFav) {
            holder.txtFav.tag = "liked"
            holder.txtFav.background = context.resources.getDrawable(R.drawable.ic_fav_fill)
        } else {
            holder.txtFav.tag = "unliked"
            holder.txtFav.background = context.resources.getDrawable(R.drawable.ic_fav_outline)
        }

        holder.txtFav.setOnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val result = DBAsyncTask(context, restaurantEntity, 2).execute().get()
                if (result) {
                    StyleableToast.Builder(context).text("${restaurant.restaurantName} Added To Fav")
                        .textColor(Color.WHITE)
                        .iconStart(R.drawable.ic_favorite)
                        .length(100)
                        .backgroundColor(Color.RED)
                        .show()
                    holder.txtFav.tag = "liked"
                    holder.txtFav.background = context.resources.getDrawable(R.drawable.ic_fav_fill)
                } else {
                    Toast.makeText(
                        context,
                        "Some Unknown Error Occured Please Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                val result = DBAsyncTask(context, restaurantEntity, 3).execute().get()
                if (result) {
                    StyleableToast.Builder(context).text("${restaurant.restaurantName} Removed From Fav")
                        .textColor(Color.WHITE)
                        .iconStart(R.drawable.ic_favorite)
                        .length(100)
                        .backgroundColor(Color.RED)
                        .show()
                    holder.txtFav.tag = "unliked"
                    holder.txtFav.background =
                        context.resources.getDrawable(R.drawable.ic_fav_outline)
                } else {
                    Toast.makeText(
                        context,
                        "Some Unknown Error Occured Please Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}

class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
    AsyncTask<Void, Void, Boolean>() {

    //TODO Database instance should be created once for the whole application and the reference should be used everywhere
    val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

    override fun doInBackground(vararg params: Void?): Boolean {

        when (mode) {
//TODO the mode should be declared as a constant with a meaningful name denoting what it stands for, so that the code can be understood better
            1 -> {
                val restaurant: RestaurantEntity =
                    db.restaurantDao().getAllRestaurant(restaurantEntity.restaurant_Id)
                db.close()
                //TODO this will always return true as restaurant is non-nullable
                return restaurant != null

            }

            2 -> {
                db.restaurantDao().insertRestaurant(restaurantEntity)
                db.close()
                return true

            }

            3 -> {
                db.restaurantDao().deleteRestaurant(restaurantEntity)
                db.close()
                return true

            }

        }

        return false
    }

}