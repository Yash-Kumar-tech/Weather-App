package com.example.weatherapp

import android.content.Context
import android.graphics.Typeface
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomAdapter : BaseAdapter {
    lateinit var context : Context
    lateinit var headings : Array<String>
    lateinit var sub_headings : Array<String>
    lateinit var inflater : LayoutInflater

    constructor(applicationContext: Context?, prov_headings: Array<String>, prov_sub_headings: Array<String>) {
        context = applicationContext!!
        headings = prov_headings
        sub_headings = prov_sub_headings
        inflater = LayoutInflater.from(applicationContext)
    }

    override fun getCount() : Int {
        return headings.size
    }
    override fun getItem(i : Int) : Object? {
        return null
    }
    override fun getItemId(i : Int) : Long {
        return 0
    }
    override fun getView(i : Int, view : View?, viewGroup : ViewGroup) : View? {
        var view = view
        view = inflater.inflate(R.layout.grid_view_item_layout, null)
        var heading = view?.findViewById<TextView>(R.id.heading)
        var sub_heading = view?.findViewById<TextView>(R.id.sub_heading)
        heading?.text = headings[i]
        sub_heading?.text = sub_headings[i]
        if(headings[i] == "City Name") {
            sub_heading?.textSize = 56.0f
            sub_heading?.setTypeface(null, Typeface.NORMAL)
            heading?.visibility = View.GONE
        } else if(headings[i] == "Temperature") {
            sub_heading?.textSize = 48.0f
            heading?.visibility = View.GONE
        } else if(headings[i] == "Weather Condition") {
            sub_heading?.textSize = 24.0f
            heading?.visibility = View.GONE
        } else if(headings[i] == "Date Day Max/Min") {
            sub_heading?.textSize = 18.0f
            heading?.visibility = View.GONE
        }
        return view
    }

}