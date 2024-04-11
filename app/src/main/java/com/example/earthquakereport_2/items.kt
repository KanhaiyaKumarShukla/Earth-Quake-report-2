package com.example.earthquakereport_2

class items(
    private val magnitude:Double,
    private val location:String,
    private val date_and_time:Long,
    private val url:String
) {
    fun getMagnitude():Double{
        return magnitude
    }
    fun getLocation():String{
        return location
    }
    fun getDateAndTime():Long{
        return date_and_time
    }
    fun getUrl():String{
        return url
    }
}