package com.example.earthquakereport_2

class EarthQuake(
    private val magnitude:Double,
    private val location:String,
    private val timeInMillisecond:Long,
    private val url:String
) {
    fun getMagnitude():Double{
        return magnitude
    }
    fun getLocation():String{
        return location
    }
    fun getDateAndTime():Long{
        return timeInMillisecond
    }
    fun getUrl():String{
        return url
    }
}