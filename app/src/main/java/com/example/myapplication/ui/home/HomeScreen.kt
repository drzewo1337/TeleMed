package com.example.myapplication.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.domain.model.Measurement
import com.example.myapplication.domain.model.MeasurementType
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.theme.BloodPressureBg
import com.example.myapplication.ui.theme.BloodPressureColor
import com.example.myapplication.ui.theme.BloodSugarBg
import com.example.myapplication.ui.theme.BloodSugarColor
import com.example.myapplication.ui.theme.TemperatureBg
import com.example.myapplication.ui.theme.TemperatureColor
import com.example.myapplication.ui.theme.WeightBg
import com.example.myapplication.ui.theme.WeightColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = LocalDate.now().toString()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Dodaj pomiar",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Wybierz, co chcesz zmierzyć:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MeasurementTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.DeviceThermostat,
                    label = "Temperatura",
                    backgroundColor = TemperatureBg,
                    iconColor = TemperatureColor,
                    lastMeasurement = uiState.lastMeasurements[MeasurementType.TEMPERATURE],
                    onClick = { navController.navigate(Screen.AddMeasurement.createRoute(today)) }
                )
                MeasurementTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Favorite,
                    label = "Ciśnienie",
                    backgroundColor = BloodPressureBg,
                    iconColor = BloodPressureColor,
                    lastMeasurement = uiState.lastMeasurements[MeasurementType.BLOOD_PRESSURE],
                    onClick = { navController.navigate(Screen.AddMeasurement.createRoute(today)) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MeasurementTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.WaterDrop,
                    label = "Cukier",
                    backgroundColor = BloodSugarBg,
                    iconColor = BloodSugarColor,
                    lastMeasurement = uiState.lastMeasurements[MeasurementType.BLOOD_SUGAR],
                    onClick = { navController.navigate(Screen.AddMeasurement.createRoute(today)) }
                )
                MeasurementTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.MonitorWeight,
                    label = "Waga",
                    backgroundColor = WeightBg,
                    iconColor = WeightColor,
                    lastMeasurement = uiState.lastMeasurements[MeasurementType.WEIGHT],
                    onClick = { navController.navigate(Screen.AddMeasurement.createRoute(today)) }
                )
            }
        }
    }
}

@Composable
private fun MeasurementTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    lastMeasurement: Measurement?,
    onClick: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("pl"))

    Card(
        modifier = modifier
            .height(170.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "Dodaj pomiar: $label"
            ) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(52.dp),
                tint = iconColor
            )

            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = iconColor
            )

            if (lastMeasurement != null) {
                Text(
                    text = "Ostatnio: ${lastMeasurement.timestamp.format(dateFormatter)}",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = iconColor.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = "Brak pomiarów",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = iconColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}
