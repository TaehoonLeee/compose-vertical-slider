package com.example.myapplication.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

val Context.dataStore by preferencesDataStore("test")

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.act_main)

		val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
		val isPermissionGranted = REQUIRED_PERMISSIONS.all {
			ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
		}
		if (!isPermissionGranted) {
			ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10)
		}

		setContent {

		}
	}
}

@Composable
fun MyDialog(setOnButtonClick: (@Composable () -> Unit) -> Unit) {
	Dialog(onDismissRequest = {}) {
		Box {
			Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
				Text(text = "Dialog")
				OutlinedButton(onClick = {
					setOnButtonClick {

					}
				}) {
					Text(text = "test")
				}
			}
		}
	}
}

@Composable
fun ComposeDialogDemo() {
	// State to manage if the alert dialog is showing or not.
	// Default is false (not showing)
	val (showDialog, setShowDialog) =  remember { mutableStateOf(false) }
	Column(
		// Make the column fill the whole screen space (width and height).
		modifier = Modifier.fillMaxSize(),
		// Place all children at center horizontally.
		horizontalAlignment = Alignment.CenterHorizontally,
		// Place all children at center vertically.
		verticalArrangement = Arrangement.Center
	) {
		Button(
			onClick = {
				setShowDialog(true)
			}) {
			Text("Show Dialog")
		}
		// Create alert dialog, pass the showDialog state to this Composable
		DialogDemo(showDialog, setShowDialog)
	}
}

@Composable
fun DialogDemo(showDialog: Boolean, setShowDialog: (Boolean) -> Unit) {
	if (showDialog) {
		AlertDialog(
			onDismissRequest = {
			},
			title = {
				Text("Title")
			},
			confirmButton = {
				Button(
					onClick = {
						// Change the state to close the dialog
						setShowDialog(false)
					},
				) {
					Text("Confirm")
				}
			},
			dismissButton = {
				Button(
					onClick = {
						// Change the state to close the dialog
						setShowDialog(false)
					},
				) {
					Text("Dismiss")
				}
			},
			text = {
				Text("This is a text on the dialog")
			},
		)
	}
}

@Composable
fun LockScreenOrientation() {
	val context = LocalContext.current

	fun Context.findActivity(): Activity? = when (this) {
		is Activity -> this
		is ContextWrapper -> baseContext.findActivity()
		else -> null
	}

	var orientation by remember {
		mutableStateOf(Configuration.ORIENTATION_PORTRAIT)
	}
	val configuration = LocalConfiguration.current
	LaunchedEffect(key1 = configuration, block = {
		snapshotFlow { configuration.orientation }
			.collect { orientation = it; println(orientation) }
	})

	DisposableEffect(key1 = Unit, effect = {
		val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
		val originalOrientation = activity.requestedOrientation
		activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		onDispose {
			activity.requestedOrientation = originalOrientation
		}
	})
}