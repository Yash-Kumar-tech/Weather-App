package com.example.weatherapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.app.AlertDialog
import android.content.DialogInterface
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var longitude : Double = 0.0
    var latitude : Double = 0.0
    var headings = arrayOf("City Name", "Temperature", "Weather Condition", "Date Day Max/Min")
    var info = arrayOf("Null", "0.00C", "Unknown", "X MON DAY MAX/MIN")

    private lateinit var mySwipeRefresh: SwipeRefreshLayout
    private lateinit var mToolbar : MaterialToolbar
    private lateinit var gridView : GridView
    private lateinit var weatherBrief : String

    val SDF = SimpleDateFormat("dd MMM E")
    val date = SDF.format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Assign all views
        assignViews()
        setSupportActionBar(mToolbar)

        //Ask for location permission
        requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), 0)

        refreshWeather()

        mySwipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

    }

    private fun refreshWeather() {
        getLocation()
    }

    private fun getWeather(city : String) {
        var customAdapter: CustomAdapter
        val sideThread = Thread {
            try {
                info = setWeather(doInBackground(city))!!
                customAdapter = CustomAdapter(applicationContext, headings, info)

            } catch (e: Exception) {
                Log.d("Error", e.toString())
            }
        }
        sideThread.start()
        while(sideThread.isAlive) {}
        customAdapter = CustomAdapter(applicationContext, headings, info)
        gridView.adapter = customAdapter
        setBackground()
        mySwipeRefresh.isRefreshing = false
    }

    private fun setBackground() {
        val SDF1 = SimpleDateFormat("HH")
        val time = SDF1.format(Date())
        val bgImage = findViewById<ImageView>(R.id.bgImageView)
        if(time.toString().toInt() <= 4 || time.toString().toInt() >= 20) {
            bgImage.setImageResource(R.drawable.night_clear_background)
        } else if(time.toString().toInt() in 11..15) {
            bgImage.setImageResource(R.drawable.noon_clear_background)
            if(weatherBrief == "Clouds") {
                bgImage.setImageResource(R.drawable.clouds_background)
            }
        } else if(time.toString().toInt() in 3 .. 6){
            bgImage.setImageResource(R.drawable.sunrise_background)
            if(weatherBrief == "Clouds") {
                bgImage.setImageResource(R.drawable.clouds_background)
            }
        } else {
            bgImage.setImageResource(R.drawable.sunset_background)
        }
        if(weatherBrief == "Rain") {
            bgImage.setImageResource(R.drawable.rain_background)
        }
    }

    private fun setWeather(result: String?) : Array<String>? {
        try {
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val weather = jsonObj.getJSONArray("weather")
            val temp = main.getString("temp") + "°C"
            val address = jsonObj.getString("name")
            val maxTemp = main.getString("temp_max")
            val minTemp = main.getString("temp_min")
            val last_val = "$date $maxTemp °C/$minTemp °C"
            weatherBrief = weather.getJSONObject(0).getString("main")
            return arrayOf(address, temp, weather.getJSONObject(0).getString("description").replaceFirstChar { it.titlecase(Locale.getDefault()) }, last_val)
        } catch(e : java.lang.Exception) {
            Log.d("Result", e.toString())
            return null
        }
    }

    private fun doInBackground(city : String): String? {
        val request = HttpRequest()
        if(city != "") {
            return request.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=" + getString(R.string.API_key))
        }
        return request.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + getString(R.string.API_key))
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_toolbar_menu, menu)
        mToolbar.title = ""
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.refresh -> {
                mySwipeRefresh.isRefreshing = true
                refreshWeather()
                return true
            }
            R.id.cities -> {
                mySwipeRefresh.isRefreshing = true
                val alert: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
                alert.setTitle("Cities")
                alert.setMessage("Enter name of city:")
                val input = EditText(this)
                alert.setView(input)
                alert.setPositiveButton(
                    "Ok"
                ) { _, _ ->
                    // Do something with value!
                    val city = input.text.toString().trim().replace(' ','+')
                    setWeatherCity(city)
                }

                alert.setNegativeButton("Cancel"
                ) { dialog, _ ->
                    // Canceled.
                    mySwipeRefresh.isRefreshing = false
                    dialog.cancel()
                }

                alert.show()
                return true
            }
            R.id.settings -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setWeatherCity(city: String) {
        getWeather(city)
    }

    fun assignViews() {
        mySwipeRefresh = findViewById(R.id.swipeToRefresh)
        mToolbar = findViewById(R.id.materialToolbar)
        gridView = findViewById(R.id.gridView)
    }

    fun getLocation() {
        var locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        var locationListener = LocationListener { p0 ->
            latitude = p0!!.latitude
            longitude = p0!!.longitude
            getWeather("")
            //Toast.makeText(applicationContext, "Latitude: $latitude ; Longitude: $longitude", Toast.LENGTH_SHORT).show()
        }
        try {
            locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } catch (ex:SecurityException) {
            Toast.makeText(applicationContext, ex.toString(), Toast.LENGTH_SHORT).show()
        }
        if(latitude == 0.0 && longitude == 0.0) {
            //Toast.makeText(applicationContext, "Could not get location. Try again later", Toast.LENGTH_SHORT).show()
            mySwipeRefresh.isRefreshing = false
            return
        }
    }
}