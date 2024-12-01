import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.example.burnify.dao.ActivityPredictionDao
import com.example.burnify.database.ActivityPrediction
import com.example.burnify.database.AppDatabaseProvider
import com.example.burnify.model.AccelerometerMeasurements
import com.example.burnify.model.AccelerometerSample
import com.example.burnify.model.GyroscopeMeasurements
import com.example.burnify.model.MagnetometerMeasurements
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SensorDataManager {
    var accelerometerIsFilled: Boolean = false
    var gyroscopeIsFilled: Boolean = false
    var magnetometerIsFilled: Boolean = false
    var outputTensor: MutableList<MutableList<Float>> = createZeroMatrix().toMutableList()



    // Funzione per impostare il contesto
    fun setAccelerometerMeasurements(accelerometerMeasurements: AccelerometerMeasurements,context: Context) {
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
                makePostRequestWithSensorData(context)
                resetStateFlags()
                println("Service States are Resumed")
                outputTensor = createZeroMatrix()
            }
        } else {
            println("Dati insufficienti per riempire l'outputTensor")
        }
    }

    fun setGyroscopeMeasurements(gyroscopeMeasurements: GyroscopeMeasurements,context: Context) {
        val samples = gyroscopeMeasurements.getSamples()

        if (samples.size >= 500) {
            for (i in 0 until 500) {
                outputTensor[i][3] = samples[i][0] // Assegna l'asse X del giroscopio
                outputTensor[i][4] = samples[i][1] // Assegna l'asse Y del giroscopio
                outputTensor[i][5] = samples[i][2] // Assegna l'asse Z del giroscopio
            }
            gyroscopeIsFilled = true
            println("gyroscopeService is Stopped")
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData(context)
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service States are Resumed")
            }
        } else {
            println("Dati giroscopio insufficienti per riempire l'outputTensor")
        }
    }

    fun setMagnetometerMeasurements(magnetometerMeasurements: MagnetometerMeasurements,context: Context) {
        val samples = magnetometerMeasurements.getSamples()

        if (samples.size >= 500) {
            for (i in 0 until 500) {
                outputTensor[i][6] = samples[i][0] // Assegna l'asse X del magnetometro
                outputTensor[i][7] = samples[i][1] // Assegna l'asse Y del magnetometro
                outputTensor[i][8] = samples[i][2] // Assegna l'asse Z del magnetometro
            }
            magnetometerIsFilled = true
            println("magnetometerService is Stopped")
            if (outputTensorIsFull()) {
                makePostRequestWithSensorData(context)
                resetStateFlags()
                outputTensor = createZeroMatrix()
                println("Service States are Resumed")
            }
        } else {
            println("Dati magnetometro insufficienti per riempire l'outputTensor")
        }
    }

    fun outputTensorIsFull(): Boolean {
        return accelerometerIsFilled && gyroscopeIsFilled && magnetometerIsFilled
    }

    fun createZeroMatrix(): MutableList<MutableList<Float>> {
        val rows = 500
        val columns = 9
        return MutableList(rows) {
            MutableList(columns) { 0.0f }
        }
    }

    fun makePostRequestWithSensorData(context: Context) {
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
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        println("HttpRequests: Risposta POST: $bodyString")
                        try {
                            val jsonObject = JSONObject(bodyString) // Parsing JSON
                            val predictedClass = jsonObject.getInt("predicted_class")
                            println("Predicted class: $predictedClass")
                            GlobalScope.launch {
                                val db = AppDatabaseProvider.getInstance(context) // Usa il singleton
                                val dao = db.activityPredictionDao()
                                insertActivityPredictionToDB(dao, predictedClass)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            println("Errore nel parsing JSON: ${e.message}")
                        }
                    } else {
                        println("Il corpo della risposta è null")
                    }
                } else {
                    println("HttpRequests" + "Errore POST: ${response.code}")
                }
            }
        })
    }

    // Funzione per convertire l'outputTensor in JSON
    fun convertOutputTensorToJson(): String {
        val jsonArray = JSONArray()

        for (i in 0 until 500) {
            val row = JSONArray()
            for (j in 0 until 9) {
                row.put(outputTensor[i][j])
            }
            jsonArray.put(row)
        }

        val jsonObject = JSONObject()
        jsonObject.put("data", jsonArray)
        println(jsonObject.toString())
        return jsonObject.toString()
    }

    fun resetStateFlags() {
        accelerometerIsFilled = false
        gyroscopeIsFilled = false
        magnetometerIsFilled = false
    }

    suspend fun insertActivityPredictionToDB(dao: ActivityPredictionDao, label: Int) {
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val activityPrediction = ActivityPrediction(
            processedAt = currentDateTime, // Aggiungi la data corrente
            label = label // Aggiungi la label passata come input
        )

        dao.insertActivityPrediction(activityPrediction)
    }
}
