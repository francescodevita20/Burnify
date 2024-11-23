package com.example.burnify


import android.content.Context
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer


class LSTMModel(context: Context,fileName:String) {
    private val interpreter: Interpreter

    init {
        // Carica il modello TensorFlow Lite dalla memoria
        val model = FileUtil.loadMappedFile(context, fileName)
        interpreter = Interpreter(model)
    }

    /**
     * Esegue l'inferenza sul modello con input di 9 float e output di 12 classi.
     * @param input Un array di 9 valori in virgola mobile che rappresentano l'input.
     * @return Un array di 12 valori in virgola mobile che rappresentano le probabilità di ciascuna classe.
     */
    fun predict(input: FloatArray): FloatArray {
        // Verifica che l'input abbia la dimensione corretta
        if (input.size != 9) {
            throw IllegalArgumentException("L'input deve contenere esattamente 9 valori float.")
        }

        // Prepara il tensore di input
        val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 9), DataType.FLOAT32)
        inputTensor.loadArray(input)

        // Prepara il tensore di output
        val outputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 12), DataType.FLOAT32)

        // Esegui l'inferenza
        interpreter.run(inputTensor.buffer, outputTensor.buffer.rewind())

        // Restituisci il risultato come un array di float
        return outputTensor.floatArray
    }
}
