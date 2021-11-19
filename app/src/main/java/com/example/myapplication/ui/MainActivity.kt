package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
                var position by remember { mutableStateOf(0f) }

                VerticalSlider(value = position, onValueChange = {
                    position = it
                }, modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(24.dp)
                    .height(244.dp)
                    .padding(end = 12.dp)
                )

                VerticalSlider(value = position, onValueChange = {
                    position = it
                }, modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(24.dp)
                    .height(244.dp)
                    .padding(start = 12.dp)
                )
            }
        }
    }
}