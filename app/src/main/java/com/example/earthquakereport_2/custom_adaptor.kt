package com.example.earthquakereport_2

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class custom_adaptor(context: Context, earthQuake:ArrayList<EarthQuake>): ArrayAdapter<EarthQuake>(context, 0, earthQuake) {
    private val TEXT_SEPARATOR="of"
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
         var listItemView=convertView
         if(listItemView==null){
             listItemView=LayoutInflater.from(context).inflate(R.layout.earth_quake_list_view_items, parent, false)
         }
         val currItem=getItem(position)
         val magnitude_tv=listItemView!!.findViewById<TextView>(R.id.magnitude_tv)
         val local_loc_tv=listItemView.findViewById<TextView>(R.id.location_primary_tv)
         val local_offset_tv=listItemView.findViewById<TextView>(R.id.location_off_set_tv)
         val date=listItemView.findViewById<TextView>(R.id.date_tv)
         val time=listItemView.findViewById<TextView>(R.id.time_tv)


         magnitude_tv.text=formatMagnitude(currItem!!.getMagnitude())

         val magnitudeCircle= magnitude_tv.background as GradientDrawable
         magnitudeCircle.setColor(getMagnitudeColor(currItem.getMagnitude()))

         val location=currItem.getLocation()
         val separatedLoc=splitLocation(location)
         local_loc_tv.text=separatedLoc[0]
         local_offset_tv.text=separatedLoc[1]
         date.text=formateDate(currItem.getDateAndTime())
         time.text=formateTime(currItem.getDateAndTime())
         return listItemView
    }

    private fun getMagnitudeColor(magnitude: Double): Int {
        val colorId=when(magnitude.toInt()){
            0, 1->R.color.magnitude1
            2 -> R.color.magnitude2
            3 -> R.color.magnitude3
            4 -> R.color.magnitude4
            5 -> R.color.magnitude5
            6 -> R.color.magnitude6
            7 -> R.color.magnitude7
            8 -> R.color.magnitude8
            9 -> R.color.magnitude9
            else -> R.color.magnitude10plus
        }
        return ContextCompat.getColor(context, colorId)
    }

    private fun formateTime(dateAndTime: Long): String {
        return SimpleDateFormat("h:mm a").format(dateAndTime)
    }

    private fun formateDate(dateAndTime: Long): String {
        return SimpleDateFormat("LLL dd, yyyy").format(dateAndTime)
    }

    private fun splitLocation(location: String): Array<String> {
        val arrayOfLocation= arrayOf("near the", location)
        if(location.contains(TEXT_SEPARATOR)){
            val splitedLocation=location.split(TEXT_SEPARATOR)
            arrayOfLocation[0]=splitedLocation[0]+TEXT_SEPARATOR
            arrayOfLocation[1]=splitedLocation[1]
        }
        return arrayOfLocation
    }

    private fun formatMagnitude(magnitude: Double): String {
         return DecimalFormat("0.0").format(magnitude)
    }
}

