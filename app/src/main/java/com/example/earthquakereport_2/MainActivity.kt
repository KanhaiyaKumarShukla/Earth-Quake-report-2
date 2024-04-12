package com.example.earthquakereport_2

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val logTag=MainActivity::class.java.simpleName
    // this is a empty view which will so "no data found" message if data is not found.
    private lateinit var mEmptyTextView: TextView
    // loading indicator indicating data is loading...
    private lateinit var loadingIndicator:View

    private val USGS_URL="https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=3&limit=50"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mEmptyTextView=findViewById(R.id.empty_tv)
        loadingIndicator=findViewById<View>(R.id.loading_indicator)

        // check if internet is available
        if(isInternetAvailable(this)) {
            lifecycleScope.launch(Dispatchers.Main) {
                val result: ArrayList<EarthQuake>? = fetchData()
                //delay(5000)
                updateUri(result)
            }
        }else{
            // if internet is not available then don't show loading indicator, directly so "no internet found" message.
            loadingIndicator.visibility=View.GONE
            mEmptyTextView.text="No Internet Found"
            return
        }
    }

    private fun isInternetAvailable(context: Context):Boolean{
        // ConnectivityManager is used checking state of network connectivity
        // getSystemService(name: String): object :- This method is a part of the Context class. It retrieves a system-level service by name. This method allows you to obtain various system services provided by the Android system,
        //        such as the ConnectivityManager, LocationManager, LayoutInflater, and many others.
        //        name: A string representing the name of the system service you want to retrieve. This can be one of the predefined service names available as constants in the Context class, such as Context.CONNECTIVITY_SERVICE for the connectivity manager.
        //CONNECTIVITY_SERVICE :- It is a string constant of context class. Use with getSystemService(java.lang.String) to retrieve a ConnectivityManager for handling management of network connections.
        val connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //For older version of Android, get activeNetworkInfo() was used, after android version 7, we use getNetworkCapabilities() to check network connection. so we will check android version.
        //You can use Build.VERSION.SDK_INT in your Android code to perform runtime checks and execute code based on the version of the Android platform that your app is running on.
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            // getActiveNetwork() returns a Network object. it will return null if there is no default network or is blocked.
            val network=connectivityManager.activeNetwork
            if(network!=null){
                // getNetworkCapabilities(network): NetworkCapabilities :-Get the NetworkCapabilities for the given Network, or null.This method returns null if the network is unknown or if the |network| argument is null.
                // NetworkCapabilities contains many capabilities of active network that represent what modern networks can and can't do.
                // NetworkCapabilities represent various attributes and capabilities of a network connection in Android. These attributes help determine the capabilities and characteristics of the network,
                // which can be useful for making decisions about network usage within an application. Indicates whether the network has the capability to access the internet. This is often used to check if the device is connected to a network.
                val capabilities=connectivityManager.getNetworkCapabilities(network)
                if(capabilities!=null) {
                     // hasCapability (capability: Int):Boolean :- Tests for the presence of a capability on this instance. what ever we will pass attribute which is a constant and is a capability of network. it will check for that capability.
                    // NET_CAPABILITY_INTERNET : It is a constant of NetworkCapabilities class which indicates that this network should be able to reach the internet.
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                }
            }
            return false
        }else{
            // for older version:
            //getActiveNetworkInfo () :-Returns details about the currently active default data network. When connected, this network is the default route for outgoing connections.
            //This may return null when there is no default network.
            val networkInfo=connectivityManager.activeNetworkInfo
            // isConnected() :- Returns true if network connected else false
            return networkInfo?.isConnected?:false
        }
    }
    private suspend fun fetchData(): ArrayList<EarthQuake>?{
        return withContext(Dispatchers.IO){
            try {
                val url=createURL()
                val jsonResponse=makeHttpRequest(url)
                extractJsonData(jsonResponse)
            }catch (e:IOException){
                e.printStackTrace()
                null
            }
        }
    }

    private fun extractJsonData(jsonResponse: String?): ArrayList<EarthQuake>? {
        if (jsonResponse.isNullOrEmpty()) return null
        return try {
            val arrayOfEarthQuakes = ArrayList<EarthQuake>()
            val jsonObj = JSONObject(jsonResponse)
            val jsonArray = jsonObj.getJSONArray("features")
            for (i in 0 until jsonArray.length()) {
                val jsonObj1 = jsonArray.getJSONObject(i)
                val jsonObj2 = jsonObj1.getJSONObject("properties")
                val jsonMag = jsonObj2.optDouble("mag", 0.0) // Using optDouble to handle null values
                val jsonPlace = jsonObj2.optString("place", "")
                val jsonDate = jsonObj2.optLong("time", 0)
                val url = jsonObj2.optString("url", "")
                val currEarthQuake = EarthQuake(jsonMag, jsonPlace, jsonDate, url)
                arrayOfEarthQuakes.add(currEarthQuake)
            }
            arrayOfEarthQuakes
        } catch (e: JSONException) {
            Log.e(logTag, "JSON exception found!", e)
            null
        }
    }


    private suspend fun makeHttpRequest(url: URL?): String?{
         return try {
             withContext(Dispatchers.IO) {
                 val urlConnection = url?.openConnection() as HttpURLConnection
                 urlConnection.requestMethod = "GET"
                 urlConnection.connectTimeout = 15000
                 urlConnection.readTimeout = 10000
                 urlConnection.connect()
                 val inputStream = urlConnection.inputStream
                 val jsonResponse = readFromStream(inputStream)

                 jsonResponse
             }
         }catch (e:Exception){
             e.printStackTrace()
             Log.e(logTag, "network connection error", e)
             null
         }
    }


    private fun readFromStream(inputStream: InputStream):String{
        val output=StringBuilder()
        val reader=BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
        var line=reader.readLine()
        while(line!=null) {
            output.append(line)
            line = reader.readLine()
        }
        return output.toString()
    }


    private fun createURL(): URL? {
        return try {
            URL(USGS_URL)
        }catch (e:Exception){
            Log.e(logTag, "null url exception handled!", e)
            null
        }
    }

    private fun updateUri(result: ArrayList<EarthQuake>?) {


        loadingIndicator.visibility=View.GONE
        val listView=findViewById<ListView>(R.id.list)
        if(result.isNullOrEmpty()){
            listView.visibility= View.GONE
            mEmptyTextView.text="No Data Found !"
            mEmptyTextView.visibility=View.VISIBLE
            return
        }
        val adapter=custom_adaptor(this ,result)
        listView.adapter=adapter
        listView.setOnItemClickListener{ _, _, position, _->
            val currItem=adapter.getItem(position)
            val url= Uri.parse(currItem?.getUrl())
            val intent=Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        }

    }


}