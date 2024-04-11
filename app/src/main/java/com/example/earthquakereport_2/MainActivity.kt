package com.example.earthquakereport_2

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ListView
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
    private val LOG_TAG=MainActivity::class.java.simpleName
    val USGS_URL="https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=06"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lifecycleScope.launch(Dispatchers.Main) {
            val result: ArrayList<items>? =fetchData();
            updateUri(result);
        }
    }

    private suspend fun fetchData(): ArrayList<items>?{
         val arrayAns=ArrayList<items>()
         if(USGS_URL==null)return arrayAns
        return withContext(Dispatchers.IO){
            try {
                val url=createURL(USGS_URL)
                val jsonResponse=makeHttpRequest(url)
                extractJsonData(jsonResponse)
            }catch (e:IOException){
                e.printStackTrace()
                arrayAns
            }
        }
    }

    private fun extractJsonData(jsonResponse: String?): ArrayList<items>? {
        if(TextUtils.isEmpty(jsonResponse))return null
        return try{
            val arrayData=ArrayList<items>()
            val jsonObj= JSONObject(jsonResponse)
            // retreve JSONArray to get data
            val jsonArray=jsonObj.getJSONArray("features")
            for(i in 0 until jsonArray.length()){
                val jsonObj1=jsonArray.getJSONObject(i)
                val jsonObj2=jsonObj1.getJSONObject("properties")
                val jsonMag=jsonObj2.getDouble("mag")
                val jsonPlace=jsonObj2.getString("place")
                val jsonDate=jsonObj2.getLong("time")
                val url=jsonObj2.getString("url")
                // here we are making object of "earthQuake" to push it in earthQuake arraylist
                val currEarthQuake=items(jsonMag, jsonPlace, jsonDate, url)
                // we are pushing the above made instance
                arrayData.add(currEarthQuake)
            }


            arrayData
        }catch (e:JSONException){
            Log.e(LOG_TAG, "json exception found! ", e)
            null
        }
    }

    private fun makeHttpRequest(url: URL?): String?{
         return try {
             val jsonResponse:String
             val urlConnection=url?.openConnection() as HttpURLConnection
             urlConnection.requestMethod="GET"
             urlConnection.connectTimeout=15000
             urlConnection.readTimeout=10000
             urlConnection.connect()
             val inputStream=urlConnection.inputStream
             jsonResponse=readFromStream(inputStream)

             jsonResponse
         }catch (e:Exception){
             e.printStackTrace()
             Log.e(LOG_TAG, "network connection error", e)
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


    private fun createURL(usgsUrl: String): URL? {
        return try {
            URL(usgsUrl)
        }catch (e:Exception){
            Log.e(LOG_TAG, "null url exception handled", e)
            null
        }
    }

    private fun updateUri(result: ArrayList<items>?) {
        if(result==null)return ;
        val list_view=findViewById<ListView>(R.id.list)
        val adapter=custom_adaptor(this ,result)
        list_view.adapter=adapter
        list_view.setOnItemClickListener{parent, view, position, id->
            val currItem=adapter.getItem(position)
            val url= Uri.parse(currItem?.getUrl())
            val intent=Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        }

    }


}