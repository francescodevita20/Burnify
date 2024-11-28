
import okhttp3.*
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.random.Random

class HttpRequests {

    private val client = OkHttpClient()

    // Funzione per eseguire una richiesta GET
    fun makeGetRequest() {
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/helloworld")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("HttpRequests"+ "Errore GET: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("HttpRequests"+ "Risposta GET: ${response.body?.string()}")
                } else {
                    println("HttpRequests"+ "Errore GET: ${response.code}")
                }
            }
        })
    }

    // Funzione per eseguire una richiesta POST
    fun makePostRequest() {
        val jsonData = generateRandomDataForPrediction()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonData)

        val request = Request.Builder()
            .url("http://10.0.2.2:8000/predict/")
            .post(body)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("HttpRequests"+ "Errore POST: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("HttpRequests"+ "Risposta POST: ${response.body?.string()}")
                } else {
                    println("HttpRequests"+ "Errore POST: ${response.code}")
                }
            }
        })
    }


    fun generateRandomDataForPrediction(): String {
        val random = Random.Default  // Usa l'implementazione predefinita di Random

        // Crea una lista di 500 righe, ognuna contenente 9 valori float
        val dataArray = JSONArray()
        for (i in 0 until 500) {
            val row = JSONArray()
            for (j in 0 until 9) {
                row.put(random.nextFloat())  // Valori casuali tra 0 e 1
            }
            dataArray.put(row)
        }

        // Crea il JSON finale
        val jsonObject = JSONObject()
        jsonObject.put("data", dataArray)

        return jsonObject.toString()  // Restituisce il JSON come stringa
    }

}
