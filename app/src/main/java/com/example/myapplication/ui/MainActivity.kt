package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        val isPermissionGranted = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!isPermissionGranted) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10)
        }

        setContent {
            Box(Modifier.fillMaxSize()) {
                var position1 by remember { mutableStateOf(0f) }
                var position2 by remember { mutableStateOf(0f) }

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                ) {
                    Text(text = "Value : $position1")
                    VerticalSlider(
                        value = position1,
                        valueRange = 0f..100f,
                        onValueChange = { position1 = it },
                        thumbIcon = painterResource(id = R.drawable.zoom_thumb),
                        inactiveTrackColor = Color.Black,
                        activeTrackColor = Color.Black,
                        modifier = Modifier.width(24.dp).height(244.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                ) {
                    VerticalSlider(
                        value = position2,
                        onValueChange = { position2 = it },
                        thumbIcon = painterResource(id = R.drawable.exposure),
                        inactiveTrackColor = Color.Black,
                        activeTrackColor = Color.Black,
                        modifier = Modifier.width(24.dp).height(244.dp)
                    )
                    Text(text = "Value : $position2")
                }
            }
        }
    }
}