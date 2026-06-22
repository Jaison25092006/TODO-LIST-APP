package com.example.taskmasterpro.ui.screens.analytics

import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.taskmasterpro.ui.components.GlassyCard
import com.example.taskmasterpro.ui.components.PremiumBackground
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Retrieve active theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    PremiumBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Productivity Analytics", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary metrics row
                    val totalTasks = state.completedCount + state.pendingCount
                    val completionRate = if (totalTasks > 0) {
                        (state.completedCount.toFloat() / totalTasks * 100).toInt()
                    } else {
                        0
                    }
                    val mostProductiveWeekDay = state.weeklyProductivity.maxByOrNull { it.value }?.key ?: "None"
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryMiniCard(
                            title = "Completion",
                            value = "$completionRate%",
                            color = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMiniCard(
                            title = "Week Completed",
                            value = "${state.weeklyProductivity.values.sum()} Tasks",
                            color = secondaryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryMiniCard(
                            title = "Best Day",
                            value = mostProductiveWeekDay,
                            color = tertiaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMiniCard(
                            title = "Total Active",
                            value = "$totalTasks Tasks",
                            color = onSurfaceColor,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Completion Rate Pie Chart Card
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Completion Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (totalTasks == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks logged yet", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            AndroidView(
                                factory = { context ->
                                    PieChart(context).apply {
                                        description.isEnabled = false
                                        isDrawHoleEnabled = true
                                        setHoleColor(android.graphics.Color.TRANSPARENT)
                                        setTransparentCircleAlpha(0)
                                        holeRadius = 65f
                                        legend.isEnabled = true
                                        setUsePercentValues(true)
                                    }
                                },
                                update = { chart ->
                                    val entries = listOf(
                                        PieEntry(state.completedCount.toFloat(), "Completed"),
                                        PieEntry(state.pendingCount.toFloat(), "Pending")
                                    )
                                    val dataSet = PieDataSet(entries, "").apply {
                                        colors = listOf(
                                            primaryColor.toArgb(),
                                            outlineVariantColor.toArgb()
                                        )
                                        valueTextSize = 13f
                                        valueTextColor = Color.White.toArgb()
                                        valueFormatter = PercentFormatter(chart)
                                    }
                                    
                                    chart.legend.apply {
                                        textColor = onSurfaceColor.toArgb()
                                        textSize = 11f
                                    }
                                    
                                    chart.centerText = "Rate"
                                    chart.setCenterTextColor(onSurfaceColor.toArgb())
                                    chart.setCenterTextSize(15f)
                                    
                                    val hasDataChanged = chart.data?.dataSet?.entryCount != entries.size
                                    chart.data = PieData(dataSet)
                                    if (hasDataChanged || chart.data == null) {
                                        chart.animateY(900)
                                    }
                                    chart.invalidate()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }

                    // Weekly Productivity Bar Chart Card
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Weekly Productivity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AndroidView(
                            factory = { context ->
                                BarChart(context).apply {
                                    description.isEnabled = false
                                    legend.isEnabled = false
                                    setDrawGridBackground(false)
                                    setDrawBarShadow(false)
                                    xAxis.apply {
                                        position = XAxis.XAxisPosition.BOTTOM
                                        granularity = 1f
                                        setDrawGridLines(false)
                                    }
                                    axisLeft.apply {
                                        setDrawGridLines(false)
                                        axisMinimum = 0f
                                    }
                                    axisRight.isEnabled = false
                                }
                            },
                            update = { chart ->
                                val entries = state.weeklyProductivity.entries.mapIndexed { index, entry ->
                                    BarEntry(index.toFloat(), entry.value.toFloat())
                                }
                                val dataSet = BarDataSet(entries, "").apply {
                                    color = secondaryColor.toArgb()
                                    valueTextSize = 11f
                                    valueFormatter = object : ValueFormatter() {
                                        override fun getFormattedValue(value: Float): String {
                                            return value.toInt().toString()
                                        }
                                    }
                                }
                                
                                val labelColor = onSurfaceVariantColor.toArgb()
                                chart.xAxis.apply {
                                    valueFormatter = IndexAxisValueFormatter(state.weeklyProductivity.keys.toList())
                                    textColor = labelColor
                                    textSize = 10f
                                }
                                chart.axisLeft.apply {
                                    textColor = labelColor
                                    textSize = 10f
                                }
                                dataSet.valueTextColor = labelColor

                                val currentEntriesCount = chart.data?.entryCount ?: 0
                                chart.data = BarData(dataSet)
                                if (currentEntriesCount != entries.size) {
                                    chart.animateY(800)
                                }
                                chart.invalidate()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }

                    // Monthly Productivity Line Chart Card
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Monthly Productivity (Last 30 Days)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AndroidView(
                            factory = { context ->
                                LineChart(context).apply {
                                    description.isEnabled = false
                                    legend.isEnabled = false
                                    setDrawGridBackground(false)
                                    xAxis.apply {
                                        position = XAxis.XAxisPosition.BOTTOM
                                        setDrawGridLines(false)
                                    }
                                    axisLeft.apply {
                                        setDrawGridLines(true)
                                        axisMinimum = 0f
                                    }
                                    axisRight.isEnabled = false
                                }
                            },
                            update = { chart ->
                                val entries = state.monthlyProductivity.entries.mapIndexed { index, entry ->
                                    Entry(index.toFloat(), entry.value.toFloat())
                                }
                                val dataSet = LineDataSet(entries, "").apply {
                                    color = primaryColor.toArgb()
                                    setCircleColor(primaryColor.toArgb())
                                    lineWidth = 3f
                                    circleRadius = 4f
                                    setDrawCircleHole(false)
                                    valueTextSize = 10f
                                    mode = LineDataSet.Mode.CUBIC_BEZIER
                                    
                                    // Gradient fill
                                    setDrawFilled(true)
                                    fillDrawable = GradientDrawable(
                                        GradientDrawable.Orientation.TOP_BOTTOM,
                                        intArrayOf(
                                            primaryColor.copy(alpha = 0.35f).toArgb(),
                                            Color.Transparent.toArgb()
                                        )
                                    )
                                    valueFormatter = object : ValueFormatter() {
                                        override fun getFormattedValue(value: Float): String {
                                            return value.toInt().toString()
                                        }
                                    }
                                }

                                val labelColor = onSurfaceVariantColor.toArgb()
                                chart.xAxis.apply {
                                    valueFormatter = IndexAxisValueFormatter(state.monthlyProductivity.keys.map { it.toString() })
                                    textColor = labelColor
                                    textSize = 9f
                                }
                                chart.axisLeft.apply {
                                    textColor = labelColor
                                    textSize = 10f
                                    gridColor = outlineVariantColor.toArgb()
                                }
                                dataSet.valueTextColor = labelColor

                                val currentEntriesCount = chart.data?.entryCount ?: 0
                                chart.data = LineData(dataSet)
                                if (currentEntriesCount != entries.size) {
                                    chart.animateX(900)
                                }
                                chart.invalidate()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }

                    // Category Distribution Pie Chart Card
                    GlassyCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Category Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (state.categoryCounts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks categorized yet", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            AndroidView(
                                factory = { context ->
                                    PieChart(context).apply {
                                        description.isEnabled = false
                                        isDrawHoleEnabled = true
                                        setHoleColor(android.graphics.Color.TRANSPARENT)
                                        setTransparentCircleAlpha(0)
                                        holeRadius = 65f
                                        legend.isEnabled = true
                                    }
                                },
                                update = { chart ->
                                    val entries = state.categoryCounts.entries.map { entry ->
                                        PieEntry(entry.value.toFloat(), entry.key)
                                    }
                                    val dataSet = PieDataSet(entries, "").apply {
                                        colors = listOf(
                                            primaryColor.toArgb(),
                                            secondaryColor.toArgb(),
                                            tertiaryColor.toArgb(),
                                            errorColor.toArgb(),
                                            outlineVariantColor.toArgb()
                                        )
                                        valueTextSize = 12f
                                        valueTextColor = Color.White.toArgb()
                                    }
                                    
                                    chart.legend.apply {
                                        textColor = onSurfaceColor.toArgb()
                                        textSize = 11f
                                    }
                                    chart.centerText = "Categories"
                                    chart.setCenterTextColor(onSurfaceColor.toArgb())
                                    chart.setCenterTextSize(15f)
                                    
                                    val currentEntriesCount = chart.data?.dataSet?.entryCount ?: 0
                                    chart.data = PieData(dataSet)
                                    if (currentEntriesCount != entries.size) {
                                        chart.animateY(800)
                                    }
                                    chart.invalidate()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassyCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

