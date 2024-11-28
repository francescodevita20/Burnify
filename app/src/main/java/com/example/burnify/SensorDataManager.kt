import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.MagnetometerMeasurements
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object SensorDataManager {
    var accelerometerIsFilled: Boolean = false
    var gyroscopeIsFilled: Boolean = false
    var magnetometerIsFilled: Boolean = false

    var outputTensor: MutableList<MutableList<Float>> = createZeroMatrix().toMutableList()

    // Assumiamo che AccelerometerMeasurements contenga una lista di AccelerometerSample
    fun setAccelerometerMeasurements(accelerometerMeasurements: AccelerometerMeasurements) {
        val samples = accelerometerMeasurements.getSamples()

        if (samples.size >= 500) {
            for (i in 0 until 500) {
                // Accedi direttamente alle righe e colonne mutabili e modifica i valori
                outputTensor[i][0] = samples[i][0] // X
                outputTensor[i][1] = samples[i][1] // Y
                outputTensor[i][2] = samples[i][2] // Z
            }
            println("accelerometerService is Stopped")
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData()
                resetStateFlags()
                println("Service States are Resumed")
                outputTensor = createZeroMatrix()
            }
        } else {
            println("Dati insufficienti per riempire l'outputTensor")
        }
    }

    fun setGyroscopeMeasurements(gyroscopeMeasurements: GyroscopeMeasurements) {
        val samples = gyroscopeMeasurements.getSamples() // Recupera i campioni del giroscopio

        if (samples.size >= 500) {
            for (i in 0 until 500) {
                // Aggiungi i valori del giroscopio nelle posizioni 3, 4, 5 (X, Y, Z)
                outputTensor[i][3] = samples[i][0] // Assegna l'asse X del giroscopio
                outputTensor[i][4] = samples[i][1] // Assegna l'asse Y del giroscopio
                outputTensor[i][5] = samples[i][2] // Assegna l'asse Z del giroscopio
            }
            gyroscopeIsFilled = true
            println("gyroscopeService is Stopped")
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData()
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service States are Resumed")
            }
        } else {
            println("Dati giroscopio insufficienti per riempire l'outputTensor")
        }
    }

    fun setMagnetometerMeasurements(magnetometerMeasurements: MagnetometerMeasurements) {
        val samples = magnetometerMeasurements.getSamples() // Recupera i campioni del giroscopio

        if (samples.size >= 500) {
            for (i in 0 until 500) {
                // Aggiungi i valori del giroscopio nelle posizioni 3, 4, 5 (X, Y, Z)
                outputTensor[i][6] = samples[i][0] // Assegna l'asse X del giroscopio
                outputTensor[i][7] = samples[i][1] // Assegna l'asse Y del giroscopio
                outputTensor[i][8] = samples[i][2] // Assegna l'asse Z del giroscopio
            }
            magnetometerIsFilled = true
            println("magnetometerService is Stopped")
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData()
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service States are Resumed")
            }
        } else {
            println("Dati giroscopio insufficienti per riempire l'outputTensor")
        }
    }


    fun outputTensorIsFull(): Boolean {
        if (accelerometerIsFilled == true && gyroscopeIsFilled == true && magnetometerIsFilled == true) {
            return true
        } else {
            return false
        }
    }


    fun createZeroMatrix(): MutableList<MutableList<Float>> {
        val rows = 500
        val columns = 9
        return MutableList(rows) {
            MutableList(columns) { 0.0f }
        }
    }

    fun makePostRequestWithSensorData() {
        // Converti i dati in formato JSON
        val jsonData = convertOutputTensorToJson()

        // Definisci il tipo di media per il corpo della richiesta
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = RequestBody.create(mediaType, jsonData)

        // Costruisci la richiesta POST
        val request = Request.Builder()
            .url("http://10.0.2.2:8000/predict/")
            .post(body)
            .build()

        // Esegui la richiesta
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("HttpRequests" + "Errore POST: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    println("HttpRequests" + "Risposta POST: ${response.body?.string()}")
                } else {
                    println("HttpRequests" + "Errore POST: ${response.code}")
                }
            }
        })
    }

    // Funzione per convertire l'outputTensor in JSON
    fun convertOutputTensorToJson(): String {
        // Creo un JSONArray che conterrà tutte le righe della matrice
        val jsonArray = JSONArray()

        // Supponiamo che outputTensor sia una variabile che contiene i dati della matrice 500x9
        for (i in 0 until 500) {
            // Crea un JSONArray per la riga i-esima
            val row = JSONArray()

            // Aggiungi i valori della riga (esempio: dalla matrice outputTensor)
            for (j in 0 until 9) {
                row.put(outputTensor[i][j])  // Recupera il valore da outputTensor
            }

            // Aggiungi la riga al JSONArray
            jsonArray.put(row)
        }

        // Costruisci l'oggetto JSON completo con il campo "data"
        val jsonObject = JSONObject()
        jsonObject.put("data", jsonArray)
        println(jsonObject.toString())
        return jsonObject.toString()  // Restituisci il JSON come stringa
    }


    // Funzione per resettare i flag di stato
    fun resetStateFlags() {
        accelerometerIsFilled = false
        gyroscopeIsFilled = false
        magnetometerIsFilled = false
    }



}




