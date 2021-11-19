package com.example.myapplication.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.CancellationException

@Composable
fun VerticalSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onValueChangeFinished: ((Float) -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val thumbRadius = 12.dp
    val onValueChangeState = rememberUpdatedState(newValue = onValueChange)
    BoxWithConstraints(
        modifier
            .requiredSizeIn(thumbRadius * 2, thumbRadius * 2)
            .sliderSemantics(value, onValueChange, valueRange)
            .focusable(true, interactionSource)
    ) {
        val maxPx = constraints.maxHeight.toFloat()
        val minPx = 0f

        val rawOffset = remember { mutableStateOf(value) }
        val draggableState = remember(minPx, maxPx, valueRange) {
            SliderDraggableState {
                rawOffset.value = (rawOffset.value + it).coerceIn(minPx, maxPx)
                onValueChangeState.value.invoke(rawOffset.value)
            }
        }

        val gestureEndAction = rememberUpdatedState<(Float) -> Unit> { velocity: Float ->
            onValueChangeFinished?.invoke(velocity)
        }

        val press = Modifier.sliderPressModifier(
            draggableState, interactionSource, maxPx, rawOffset, gestureEndAction
        )

        val drag = Modifier.draggable(
            orientation = Orientation.Vertical,
            interactionSource = interactionSource,
            onDragStopped = { velocity -> gestureEndAction.value.invoke(velocity) },
            startDragImmediately = draggableState.isDragging,
            state = draggableState
        )

        Box(press.then(drag)
            .widthIn(24.dp)
            .heightIn(244.dp)
        ) {
            val trackStrokeWidth: Float
            val thumbPx: Float
            with(LocalDensity.current) {
                trackStrokeWidth = 2.dp.toPx()
                thumbPx = thumbRadius.toPx()
            }

            val thumbSize = 24.dp
            val center = Modifier.align(Alignment.CenterStart)

            Track(
                modifier = center.fillMaxSize(),
                thumbPx = thumbPx,
                trackStrokeWidth = trackStrokeWidth
            )
            SliderThumb(
                modifier = center,
                interactionSource = interactionSource,
                thumbSize = thumbSize
            )
        }
    }
}

@Composable
fun Track(
    modifier: Modifier,
    thumbPx: Float,
    trackStrokeWidth: Float
) {
    val inactiveTrackColor = Color.Black
    val activeTrackColor = Color.Black

    Canvas(modifier) {
        val sliderBottom = Offset(center.x, thumbPx)
        val sliderTop = Offset(center.x, size.height - thumbPx)
        drawLine(
            inactiveTrackColor,
            sliderBottom,
            sliderTop,
            trackStrokeWidth,
            StrokeCap.Round
        )

        val sliderValueEnd = Offset(
            center.x, sliderBottom.y + (sliderTop.y - sliderBottom.y)
        )
        val sliderValueStart = Offset(
            center.x, sliderBottom.y + (sliderTop.y - sliderBottom.y)
        )
        drawLine(
            activeTrackColor,
            sliderValueStart,
            sliderValueEnd,
            trackStrokeWidth,
            StrokeCap.Round
        )
    }
}

@Composable
fun SliderThumb(
    modifier: Modifier,
    interactionSource: MutableInteractionSource,
    thumbSize: Dp
) {
    Box(modifier) {
        Spacer(
            Modifier
                .size(thumbSize, thumbSize)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = false, radius = 10.dp)
                )
                .hoverable(interactionSource = interactionSource)
                .shadow(0.dp, CircleShape, clip = false)
                .background(Color.Blue, CircleShape)
        )
//        Image(painter = painterResource(id = R.drawable.zoom_thumb), contentDescription = null)
    }
}

private fun Modifier.sliderSemantics(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        setProgress { targetValue ->
            val newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
            if (newValue == coerced) {
                false
            } else {
                onValueChange(newValue)
                true
            }
        }
    }.progressSemantics(value, valueRange)
}

private fun Modifier.sliderPressModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Float,
    rawOffset: State<Float>,
    gestureEndAction: State<(Float) -> Unit>
): Modifier = pointerInput(draggableState, interactionSource, maxPx) {
    detectTapGestures(
        onPress = { pos ->
            draggableState.drag(MutatePriority.UserInput) {
                val to = pos.y
                dragBy(to - rawOffset.value)
            }
            val interaction = PressInteraction.Press(pos)
            interactionSource.emit(interaction)
            val finishInteraction = try {
                val success = tryAwaitRelease()
                gestureEndAction.value.invoke(0f)
                if (success) PressInteraction.Release(interaction)
                else PressInteraction.Cancel(interaction)
            } catch (e: CancellationException) {
                PressInteraction.Cancel(interaction)
            }

            interactionSource.emit(finishInteraction)
        }
    )
}

private class SliderDraggableState(
    val onDelta: (Float) -> Unit
) : DraggableState {

    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float) = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(dragPriority: MutatePriority, block: suspend DragScope.() -> Unit) = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    override fun dispatchRawDelta(delta: Float) = onDelta(delta)
}