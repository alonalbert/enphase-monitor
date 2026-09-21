package com.alonalbert.enphase.monitor.ui.battery

import android.content.Context
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.TabStopSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.buildSpannedString
import com.alonalbert.enphase.monitor.R
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import com.alonalbert.enphase.monitor.enphase.util.kw
import com.alonalbert.enphase.monitor.enphase.util.rangeOfChunk
import com.alonalbert.enphase.monitor.ui.energy.DecimalValueFormatter
import com.alonalbert.enphase.monitor.ui.energy.SampleData
import com.alonalbert.enphase.monitor.ui.energy.rememberMarker
import com.alonalbert.enphase.monitor.ui.energy.timeOfDayAxisValueFormatter
import com.alonalbert.enphase.monitor.ui.theme.colorOf
import com.alonalbert.enphase.monitor.ui.theme.toInt
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis.HorizontalLabelPosition.Inside
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineStroke
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker.ValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import kotlinx.coroutines.runBlocking

@Composable
fun BatteryLevelChart(
  batteryLevels: List<Int>,
  batteryCapacity: Double,
  modifier: Modifier = Modifier,
  reserveConfig: ReserveConfig? = null,
) {
  val reserves = when (reserveConfig == null) {
    true -> List(96) { 0 }
    false -> ReserveCalculator.calculateDailyReserves(
      reserveConfig.idleLoad,
      batteryCapacity,
      reserveConfig.minReserve,
      reserveConfig.chargeStart,
      reserveConfig.chargeEnd,
    )
  }
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(batteryLevels) {
    modelProducer.runTransaction(batteryLevels, reserves)
  }
  BatteryLevelChart(modelProducer, batteryCapacity, modifier)
}

@Composable
private fun BatteryLevelChart(
  modelProducer: CartesianChartModelProducer,
  batteryCapacity: Double,
  modifier: Modifier = Modifier,
) {
  val fill = Fill(colorResource(R.color.battery_chart))
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberLineCartesianLayer(
          lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
              fill = LineCartesianLayer.LineFill.single(fill),
              areaFill = LineCartesianLayer.AreaFill.single(fill)
            ),
            LineCartesianLayer.Line(
              fill = LineCartesianLayer.LineFill.single(Fill(colorResource(R.color.battery_reserve_start))),
              stroke = LineStroke.Continuous(1.dp)
            ),
            LineCartesianLayer.Line(
              fill = LineCartesianLayer.LineFill.single(Fill(colorResource(R.color.battery_reserve_end))),
              stroke = LineStroke.Continuous(1.dp)
            ),
          ),
          rangeProvider = remember {
            CartesianLayerRangeProvider.fixed(
              minX = 0.0,
              maxX = 95.0,
              minY = 0.0,
              maxY = 100.0
            )
          },
          pointSpacing = 2.2.dp,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,
            valueFormatter = DecimalValueFormatter,
            horizontalLabelPosition = Inside,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(style = TextStyle(fontSize = 10.sp)),
            valueFormatter = timeOfDayAxisValueFormatter(4),
            guideline = null,
            itemPlacer = remember {
              HorizontalAxis.ItemPlacer.aligned(
                spacing = { 12 },
                offset = { 0 },
                shiftExtremeLines = false,
                addExtremeLabelPadding = true
              )
            },
          ),
        marker = rememberMarker(BatteryMarkerValueFormatter(LocalContext.current, batteryCapacity), lineCount = 3),
        layerPadding = { CartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier,
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

private suspend fun CartesianChartModelProducer.runTransaction(
  batteryLevels: List<Int>,
  reserves: List<Int>,
) {
  runTransaction {
    lineModel {
      if (batteryLevels.isNotEmpty()) {
        series(batteryLevels)
      }
      val size = batteryLevels.size
      val start = reserves.take(size)
      series(start)
      val end = reserves.drop(size)
      if (end.isNotEmpty()) {
        series(List(end.size) { size - 1 + it }, end)
      }
    }
  }
}

private class BatteryMarkerValueFormatter(
  private val androidContext: Context,
  private val batteryCapacity: Double,
) : ValueFormatter {
  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>
  ): CharSequence {
    with(androidContext) {
      val color = colorOf(R.color.battery).toInt()
      return buildSpannedString {
        targets.filterIsInstance<LineCartesianLayerMarkerTarget>().forEach { target ->
          val points = target.points
          val percent = points[0].entry.y.toInt()
          val charge = percent * batteryCapacity / 100
          append("${rangeOfChunk(target.x.toInt())}\n")
          append("Charge:\t${charge.kw}\n", ForegroundColorSpan(color), SPAN_EXCLUSIVE_EXCLUSIVE)
          append("Percent:\t$percent%\n", ForegroundColorSpan(color), SPAN_EXCLUSIVE_EXCLUSIVE)
          setSpan(TabStopSpan.Standard(100), 0, length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }
  }
}

@Composable
@Preview
private fun Preview() {
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    val modelProducer = CartesianChartModelProducer()
    runBlocking {
      modelProducer.runTransaction(
        SampleData.dayData.battery.filterNotNull().subList(0, 50),
        ReserveCalculator.calculateDailyReserves(0.8, 20.16, 20, 9, 14)
      )
    }
    BatteryLevelChart(modelProducer, 20.16)
  }
}
