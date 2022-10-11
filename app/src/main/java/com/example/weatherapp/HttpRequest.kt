package com.example.weatherapp

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpRequest {
    fun excuteGet(targetURL : String?) : String? {
        val url : URL
        var connection : HttpURLConnection? = null
        try {
            url = URL(targetURL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val IS : InputStream
            val status : Int = connection.responseCode
            IS = if (status != HttpURLConnection.HTTP_OK)
                connection.errorStream
            else
                connection.inputStream
            val rd = BufferedReader(InputStreamReader(IS))
            var line : String?
            val response = StringBuffer()
            while(rd.readLine().also {line = it } != null) {
                line?.let {
                    response.append(line)
                    response.append('\r')
                }
            }
            rd.close()
            return response.toString()
        } catch (e : Exception) {
            Log.d("Result", "Error in HttpRequest: $e")
            return null
        } finally {
            connection?.disconnect()
        }
    }
}
