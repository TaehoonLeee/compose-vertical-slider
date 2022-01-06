package com.example.myapplication.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Composable
fun CircularIndicator(
	radius: Float = LocalDensity.current.run { 18.dp.toPx() },
	strokeColor: Color = Color.Black,
	strokeWidth: Float = LocalDensity.current.run { 3.dp.toPx() }
) {
	val animateFloat = remember { Animatable(0f) }
	LaunchedEffect(key1 = animateFloat, block = {
		animateFloat.animateTo(30f, tween(3000, easing = LinearEasing))
	})
	val fraction = ((animateFloat.value - 0f) / (30f - 0f)).coerceIn(0f, 1f)
	val indicatorOffset = lerp(0f, 1f, fraction)

	Canvas(modifier = Modifier.fillMaxSize()){
		drawArc(
			color = Color.Black,
			startAngle = 135f,
			sweepAngle = 270f * indicatorOffset,
			useCenter = false,
			topLeft = Offset(size.width / 4, size.height / 4),
			size = Size(radius * 2 ,
				radius * 2),
			style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
	}
}