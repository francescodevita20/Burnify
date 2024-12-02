import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.burnify.processor.CaloriesDataProcessor
import com.example.burnify.ui.components.HistogramActivityChart
import com.example.burnify.util.getSharedPreferences
import com.example.burnify.viewmodel.PredictedActivityViewModel

@Composable
fun TodayScreen(context: Context, viewModel: PredictedActivityViewModel) {
    var classes by remember { mutableStateOf<List<String>>(emptyList()) }
    var caloriesData by remember { mutableStateOf<List<ActivityData>>(emptyList()) }

    val weight = (getSharedPreferences(context, "userdata")?.get("weight")).toString().toInt()

    val predictedActivityData by viewModel.predictedActivityData.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPredictedActivityData()
    }

    LaunchedEffect(predictedActivityData) {
        classes = predictedActivityData?.map { it.label.toString() } ?: emptyList()
        caloriesData = predictedActivityData?.map {
            ActivityData(
                hour = it.processedAt.toInt(),
                activityType = it.label.toString(),
                caloriesBurned = CaloriesDataProcessor.processMeasurements(
                    weight, it.label.toString(), 0.00555556f
                )
            )
        } ?: emptyList()
    }

    // Calcolo del totale delle calorie bruciate usando la funzione compatta
    val totalCaloriesBurned = caloriesData.map { it.caloriesBurned }.sumOfFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Titolo della schermata
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            text = "Today's content",
            style = MaterialTheme.typography.titleLarge
        )

        // Card per l'istogramma delle attività
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Activity Histogram",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                HistogramActivityChart(classes = classes)
            }
        }

        // Card per il grafico delle calorie bruciate
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Calories Burned Chart",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                CaloriesBurnedChart(activityData = caloriesData)

                // Sezione per mostrare il totale delle calorie bruciate
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Total Calories Burned: ${"%.2f".format(totalCaloriesBurned)} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}


// Funzione d'estensione per sommare una lista di Float
fun List<Float>.sumOfFloat(): Float = fold(0f) { acc, value -> acc + value }
