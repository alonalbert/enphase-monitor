package com.alonalbert.enphase.monitor.ui.energy

import android.content.Context
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.TabStopSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
import com.alonalbert.enphase.monitor.db.DayTotals
import com.alonalbert.enphase.monitor.enphase.util.dayOfMonth
import com.alonalbert.enphase.monitor.repository.MonthData
import com.alonalbert.enphase.monitor.ui.theme.colorOf
import com.alonalbert.enphase.monitor.util.appendEnergyValue
import com.alonalbert.enphase.monitor.util.seriesOrEmpty
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerPadding
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer.LineStroke
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker.ValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import kotlinx.coroutines.runBlocking
import java.time.YearMonth

@Composable
fun MonthChart(
  monthData: MonthData,
  showProduction: Boolean,
  showConsumption: Boolean,
  showStorage: Boolean,
  showGrid: Boolean,
  modifier: Modifier = Modifier,
) {
  val modelProducer = remember { CartesianChartModelProducer() }
  LaunchedEffect(monthData, showProduction, showConsumption, showStorage, showGrid) {
    modelProducer.runTransaction(monthData.days, showProduction, showConsumption, showStorage, showGrid)
  }
  MonthChart(monthData.month, modelProducer, modifier)
}

@Composable
private fun MonthChart(
  month: YearMonth,
  modelProducer: CartesianChartModelProducer,
  modifier: Modifier = Modifier,
) {
  CartesianChartHost(
    chart =
      rememberCartesianChart(
        rememberColumnCartesianLayer(
          columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
              rememberLineComponent(fill = Fill(colorResource(R.color.solar))),
              rememberLineComponent(fill = Fill(colorResource(R.color.grid))),
              rememberLineComponent(fill = Fill(colorResource(R.color.battery))),
              rememberLineComponent(fill = Fill(colorResource(R.color.consumption))),
              rememberLineComponent(fill = Fill(colorResource(R.color.battery))),
            ),
          columnCollectionSpacing = 0.5.dp,
          mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
        ),
        rememberLineCartesianLayer(
          lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
              fill = LineCartesianLayer.LineFill.single(Fill(Color.Gray)),
              stroke = LineStroke.Continuous(1.dp)
            )
          ),
          pointSpacing = 0.5.dp,
        ),
        startAxis =
          VerticalAxis.rememberStart(
            guideline = null,
            valueFormatter = DecimalValueFormatter,
          ),
        bottomAxis =
          HorizontalAxis.rememberBottom(
            label = rememberAxisLabelComponent(style = TextStyle(fontSize = 10.sp)),
            valueFormatter = CartesianValueFormatter { _, x, _ -> "${(x + 1).toInt()}" },
            guideline = null,
            itemPlacer = remember {
              HorizontalAxis.ItemPlacer.aligned(
                spacing = { 2 },
                offset = { 0 },
                shiftExtremeLines = false,
                addExtremeLabelPadding = true
              )
            },
          ),
        marker = rememberMarker(MonthMarkerValueFormatter(LocalContext.current, month), lineCount = 7),
        layerPadding = { CartesianLayerPadding(scalableStart = 0.dp, scalableEnd = 0.dp) },
      ),
    modelProducer = modelProducer,
    modifier = modifier.height(300.dp),
    zoomState = rememberVicoZoomState(zoomEnabled = false),
  )
}

private suspend fun CartesianChartModelProducer.runTransaction(
  days: List<DayTotals>,
  showProduction: Boolean,
  showConsumption: Boolean,
  showStorage: Boolean,
  showGrid: Boolean,
) {
  runTransaction {
    columnModel {
      days.seriesOrEmpty(showProduction) { it.production + it.exportProduction }
      days.seriesOrEmpty(showGrid) { it.import - it.export }
      days.seriesOrEmpty(showStorage) { it.discharge }
      days.seriesOrEmpty(showConsumption) { -it.consumption }
      days.seriesOrEmpty(showStorage) { -it.charge }
    }
    lineModel {
      series(List(days.size) { 0 })
    }
  }
}

private class MonthMarkerValueFormatter(
  private val androidContext: Context,
  private val month: YearMonth,
) : ValueFormatter {
  override fun format(
    context: CartesianDrawingContext,
    targets: List<CartesianMarker.Target>
  ): CharSequence {
    with(androidContext) {
      val solarColor = colorOf(R.color.solar)
      val gridColor = colorOf(R.color.grid)
      val storageColor = colorOf(R.color.battery)
      val consumptionColor = colorOf(R.color.consumption)
      return buildSpannedString {
        targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>().forEach { target ->
          val columns = target.columns
          val production = columns[0].entry.y
          val import = columns[1].entry.y
          val discharge = columns[2].entry.y
          val consumption = -columns[3].entry.y
          val charge = -columns[4].entry.y

          val dayOfMonth = target.x.toInt() + 1
          append("${month.atDay(dayOfMonth).dayOfMonth()}\n")
          appendEnergyValue("Produced", production, solarColor)
          appendEnergyValue("Imported", import, gridColor)
          appendEnergyValue("Discharged", discharge, storageColor)
          appendEnergyValue("Consumed", consumption, consumptionColor)
          appendEnergyValue("Charged", charge, storageColor)

          setSpan(TabStopSpan.Standard(100), 0, length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }
  }
}

@Composable
@Preview(widthDp = 400)
private fun Preview() {
  Box(
    modifier = Modifier
      .background(Color.White)
      .padding(16.dp)
  ) {
    val modelProducer = CartesianChartModelProducer()
    runBlocking {
      modelProducer.runTransaction(
        SampleData.days,
        showProduction = true,
        showConsumption = true,
        showStorage = true,
        showGrid = true,
      )
    }
    MonthChart(YearMonth.now(), modelProducer)
  }
}
