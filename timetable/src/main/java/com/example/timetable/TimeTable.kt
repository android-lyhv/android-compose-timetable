package com.example.timetable

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.timetable.model.Stage
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.roundToInt

object TimeTable {
    val TIME_LINE_WIDTH = 50.dp
    val TIME_SPAN_SIZE = 20.dp
    const val MIN_SCALE = 1f
    const val MAX_SCALE = 3f

    enum class FormatType constructor(val value: String) {
        TYPE_SHORT_HOUR("HH:mm")
    }

    fun getSpanCount(startTime: String, endTime: String): Int {
        val timeFormatter = DateTimeFormatter.ofPattern(FormatType.TYPE_SHORT_HOUR.value)
        val rangeTime = LocalTime.parse(endTime, timeFormatter).toSecondOfDay() - LocalTime.parse(
            startTime,
            timeFormatter
        ).toSecondOfDay()
        return rangeTime.div(60 * 10)
    }
}

@Composable
fun MainTable() {
    val offset = remember { mutableStateOf(Offset.Zero) }
    val matrixTransform by remember {
        mutableStateOf(Matrix())
    }
    val rect = RectF(
        0f,
        0f,
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() },
        with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    offset.value = offset.value.plus(Offset(pan.x, pan.y))
                    matrixTransform.apply {
                        postScale(zoom, zoom, centroid.x, centroid.y)
                        postTranslate(pan.x, pan.y)
                        limitScale(
                            TimeTable.MIN_SCALE,
                            TimeTable.MAX_SCALE,
                            centroid.x,
                            centroid.y
                        )
                        bound(rect, rect)
                    }
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .zIndex(3f)
                    .fillMaxWidth()
                    .height(TimeTable.TIME_LINE_WIDTH)
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxHeight()
                        .zIndex(2f)
                        .width(50.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_time_table),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )

                }

                StageContain(
                    matrixTransform,
                    Stage.default
                )
            }
            Divider(
                color = Color.LightGray,
                modifier = Modifier.zIndex(2f)
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(TimeTable.TIME_LINE_WIDTH)
                        .zIndex(2f)
                        .fillMaxHeight()
                        .background(Color.White)
                ) {
                    TimeLineDivider(
                        modifier = Modifier
                            .width(TimeTable.TIME_LINE_WIDTH),
                        offset.value,
                        matrixTransform
                    )
                }

                TableContain(
                    matrixTransform,
                    stages = Stage.default
                )
            }
        }
    }
}

@Composable
fun TableContain(matrix: Matrix, stages: List<Stage>) {
    if (stages.isEmpty()) return
    val scale = GestureHelper.getScale(matrix)
    val stageWidth =
        (LocalConfiguration.current.screenWidthDp.dp.minus(TimeTable.TIME_LINE_WIDTH)).div(
            stages.size
        )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        stages.forEachIndexed { index, stage ->
            val offsetX = with(LocalDensity.current) { stageWidth.times(index).toPx() }
            val stageOffset = Offset(
                offsetX,
                0f
            )
            val newOffset = matrix.mapOffset(stageOffset)
            val height = with(LocalDensity.current) {
                TimeTable.TIME_SPAN_SIZE.toPx().times(24 * 6).times(scale)
            }
            Canvas(modifier = Modifier.width(stageWidth.times(scale))) {
                drawRect(
                    color = Color(stage.bgColor),
                    topLeft = newOffset,
                    size = Size(size.width, height)
                )
            }
            stage.events.forEach {
                val eventOffsetStartY = TimeTable.getSpanCount(
                    "00:00",
                    it.startTime
                ) * with(LocalDensity.current) { (TimeTable.TIME_SPAN_SIZE).toPx() }
                val columnOffset = matrix.mapOffset(Offset(stageOffset.x, eventOffsetStartY))
                val eventHeight = TimeTable.getSpanCount(
                    it.startTime,
                    it.endTime
                ) * with(LocalDensity.current) {
                    (TimeTable.TIME_SPAN_SIZE).toPx().times(scale)
                }
                val context = LocalContext.current
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                columnOffset.x.roundToInt(),
                                columnOffset.y.roundToInt()
                            )
                        }
                        .height(with(LocalDensity.current) { eventHeight.toDp() })
                        .width(stageWidth.times(scale))
                        .border(
                            BorderStroke(
                                1.dp,
                                Color(stage.eventColor)
                            ), shape = RoundedCornerShape(4.dp)
                        )
                        .fillMaxSize()
                        .clickable {
                            Toast
                                .makeText(context, it.name, Toast.LENGTH_SHORT)
                                .show()
                        },
                ) {
                    Text(
                        text = it.startTime,
                        color = Color(stage.eventColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(2.dp),
                        fontFamily = FontFamily(Font(R.font.sf_pro_text_thin))
                    )
                    Text(
                        text = it.name,
                        color = Color(stage.eventColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center),
                        fontFamily = FontFamily(Font(R.font.sf_pro_text_regular))
                    )
                    Text(
                        text = it.endTime,
                        color = Color(stage.eventColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(2.dp),
                        fontFamily = FontFamily(Font(R.font.sf_pro_text_thin))
                    )
                }

            }
        }
    }
}

@Composable
fun StageContain(matrix: Matrix, stages: List<Stage>) {
    if (stages.isEmpty()) return
    val stageWidth =
        (LocalConfiguration.current.screenWidthDp.dp.minus(TimeTable.TIME_LINE_WIDTH)).div(
            stages.size
        )
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        stages.forEachIndexed { index, stage ->
            val newOffset = matrix.mapOffset(
                Offset(
                    with(LocalDensity.current) { stageWidth.times(index).toPx() },
                    0f
                ),
            )
            Box(
                modifier = Modifier
                    .offset { IntOffset(newOffset.x.roundToInt(), 0) }
                    .fillMaxHeight()
                    .width(stageWidth * GestureHelper.getScale(matrix)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stage.name,
                    color = Color(stage.eventColor),
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily(Font(R.font.sf_pro_text_bold)),
                )
            }

        }

    }
}

@Composable
fun TimeLineDivider(modifier: Modifier, pan: Offset, matrix: Matrix) {
    val timeLineWidth = with(LocalDensity.current) { (TimeTable.TIME_LINE_WIDTH).toPx() }
    val houseSpace = with(LocalDensity.current) { (TimeTable.TIME_SPAN_SIZE * 6).toPx() }
    val minuteSpan = with(LocalDensity.current) { TimeTable.TIME_SPAN_SIZE.toPx() }
    val houseDividerStrokeWidth = with(LocalDensity.current) { 2.dp.toPx() }
    val minuteDividerStrokeWidth = with(LocalDensity.current) { 1.dp.toPx() }
    val minuteDividerHeight = minuteSpan / 2
    val houseDividerHeight = 1.5f * minuteDividerHeight
    val textSize = with(LocalDensity.current) { 12.sp.toPx() }
    val houseTextOffset =
        timeLineWidth - houseDividerHeight - with(LocalDensity.current) { 2.sp.toPx() }
    val houseDividerOffset = timeLineWidth - houseDividerHeight
    val minuteDividerOffset = timeLineWidth - minuteDividerHeight
    val paint = Paint()
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = textSize
    paint.color = android.graphics.Color.BLACK
    Canvas(modifier = modifier) {
        (0..23).forEach { house ->
            drawIntoCanvas {
                val offset =
                    matrix.mapOffset(Offset(houseTextOffset, houseSpace * house))
                it.nativeCanvas.drawText(
                    "$house:00",
                    houseTextOffset,
                    offset.y - (paint.descent() + paint.ascent()) / 2,
                    paint
                )
            }
            val offsetLineStart =
                matrix.mapOffset(Offset(houseDividerOffset, houseSpace * house))
            val offsetLineEnd = matrix.mapOffset(Offset(timeLineWidth, houseSpace * house))
            drawLine(
                color = Color.Gray,
                strokeWidth = houseDividerStrokeWidth,
                start = Offset(houseDividerOffset, offsetLineStart.y),
                end = Offset(timeLineWidth, offsetLineEnd.y),
            )
            val startSpace = houseSpace * house + minuteSpan
            (0..4).forEach {
                val offsetY = startSpace + it * minuteSpan
                drawLine(
                    color = Color.Gray,
                    strokeWidth = minuteDividerStrokeWidth,
                    start = Offset(
                        minuteDividerOffset,
                        matrix.mapOffset(Offset(minuteDividerOffset, offsetY)).y
                    ),
                    end = Offset(
                        timeLineWidth,
                        matrix.mapOffset(Offset(timeLineWidth, offsetY)).y
                    )
                )
            }

        }
    }
}