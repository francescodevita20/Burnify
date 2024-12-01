package com.example.burnify.ui.components

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color as ComposeColor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HistogramActivityChart(classes: List<String>) {

    LaunchedEffect(classes) {
println("Classi passate:"+ classes.toString())
    }

    if (classes.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Activity Chart is loading...",
                style = TextStyle(
                    color = ComposeColor.Black,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    } else {
        // Codice esistente per il grafico a barre
        // Predefinisci le 12 classi (se alcune non sono presenti, metti 0 occorrenze)
        val allClasses = (1..12).map { it.toString() }

        // Conta le occorrenze delle classi dalla lista passata
        val classOccurrences = allClasses.associateWith { className ->
            // Conta quante volte ogni classe appare nella lista 'classes'
            classes.count { it == className }
        }

        // Log di debug per verificare le occorrenze


        // Ordina le classi da 1 a 12
        val sortedClasses = allClasses

        // Crea una lista di BarEntry a partire dalle occorrenze delle classi
        val barEntries = sortedClasses.mapIndexed { index, className ->
            BarEntry(index.toFloat(), classOccurrences[className]?.toFloat() ?: 0f)
        }

        // Calcola il valore massimo per l'asse Y
        val maxOccurrences = classOccurrences.values.maxOrNull()?.toFloat() ?: 0f

        // Calcola i valori di Y (0, max/2, max)
        val yValues = listOf(0f, maxOccurrences / 2, maxOccurrences)

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Grafico
            AndroidView(factory = { context ->
                BarChart(context).apply {
                    val dataSet = BarDataSet(barEntries, "Class Occurrences").apply {
                        // Imposta un colore uniforme per tutte le barre (es. grigio chiaro)
                        setColor(Color.parseColor("#B0BEC5"))  // Colore grigio tenue
                    }

                    data = BarData(dataSet)

                    // Disabilita la descrizione per rimuovere la scritta "description label"
                    description.isEnabled = false

                    // Formatter personalizzato per l'asse Y: Visualizza solo numeri interi
                    axisLeft.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString()  // Converte il valore in intero
                        }
                    }

                    // Imposta i valori dell'asse Y
                    axisLeft.setGranularity(1f)
                    axisLeft.axisMinimum = 0f  // Il valore minimo è 0
                    axisLeft.axisMaximum = maxOccurrences * 1.1f // Imposta un margine per l'asse Y
                    axisLeft.setGranularityEnabled(true)  // Abilita la granularità per evitare intervalli troppo piccoli

                    // Aggiungi i valori 0, max/2, max
                    axisLeft.setLabelCount(3, true)
                    axisLeft.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (value) {
                                0f -> "0"
                                maxOccurrences / 2 -> "${(maxOccurrences / 2).toInt()}"
                                maxOccurrences -> "${maxOccurrences.toInt()}"
                                else -> ""
                            }
                        }
                    }

                    // Imposta la granulosità dell'asse X
                    xAxis.setGranularity(1f)

                    // Formatter per l'asse X: Mostra i numeri da 1 a 12
                    xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            // Restituisce l'indice per il numero dell'asse X
                            return sortedClasses.getOrElse(value.toInt()) { "" }
                        }
                    }

                    // Rimuovi i numeri a destra (asse Y destro)
                    axisRight.isEnabled = false

                    // Disabilita la leggenda sotto il grafico
                    legend.isEnabled = false

                    // Rimuovi i counter sopra le barre
                    setDrawMarkers(false)

                    // Configura l'asse X per evitare sovrapposizioni
                    xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM)

                    // Imposta l'asse Y per evitare valori duplicati
                    axisLeft.axisMinimum = 0f  // Il valore minimo è 0
                    axisLeft.axisMaximum = (maxOccurrences * 1.1f).toFloat() // Imposta un margine per l'asse Y
                }
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp))

            // Titolo sotto il grafico
            Text(
                text = "Activity History",
                style = TextStyle(
                    color = ComposeColor.Black,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

