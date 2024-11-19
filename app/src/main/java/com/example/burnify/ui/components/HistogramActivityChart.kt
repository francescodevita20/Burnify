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
import androidx.compose.ui.graphics.Color as ComposeColor
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun HistogramActivityChart(classes: List<String>) {
    // Conta le occorrenze di ciascuna classe
    val classOccurrences = classes.groupingBy { it }.eachCount()

    // Ordina le classi in ordine alfabetico o come desiderato
    val sortedClasses = classOccurrences.keys.sorted()

    // Crea una lista di BarEntry a partire dalle occorrenze delle classi
    val barEntries = sortedClasses.mapIndexed { index, className ->
        BarEntry(index.toFloat(), classOccurrences[className]?.toFloat() ?: 0f)
    }

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

                // Imposta la granulosità dell'asse X
                xAxis.setGranularity(1f)

                // Formatter per l'asse X: Mostra i nomi delle classi
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        // Restituisce il nome della classe per la posizione dell'asse X
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
                axisLeft.axisMaximum = (classOccurrences.values.maxOrNull()?.toFloat() ?: 0f) * 1.1f  // Imposta un margine per l'asse Y
                axisLeft.setGranularityEnabled(true)  // Abilita la granularità per evitare intervalli troppo piccoli
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
