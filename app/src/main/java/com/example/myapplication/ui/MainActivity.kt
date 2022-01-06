package com.example.myapplication.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.HandlerThread
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.bumptech.glide.Glide
import com.example.myapplication.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

val Context.dataStore by preferencesDataStore("test")

val testList = listOf(
	ListAnimationTest("Test1", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "1"),
	ListAnimationTest("Test2", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "2"),
	ListAnimationTest("Test3", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "3"),
	ListAnimationTest("Test4", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "4"),
	ListAnimationTest("Test5", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "5")
)

val testList2 = listOf(
	ListAnimationTest("Test1", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "1"),
	ListAnimationTest("Test3", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "3"),
	ListAnimationTest("Test2", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "2"),
	ListAnimationTest("Test4", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "4"),
	ListAnimationTest("Test5", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "5")
)

val testList3 = listOf(
	ListAnimationTest("Test1", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "1"),
	ListAnimationTest("Test3", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "3"),
	ListAnimationTest("Test4", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "4"),
	ListAnimationTest("Test5", "rtmp://asdkfjasdjfnoasdnfaosdnfoakdfsjdnfo", "5")
)

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
			val list = remember { testList.toMutableStateList() }
			fun moveList(from: Int, to: Int) {
				list.add(to, list.removeAt(from))
			}

			val oldList = testList
			val newList = testList3
			val diffCallback =
				object : DiffUtil.Callback() {
					override fun getOldListSize(): Int = oldList.size

					override fun getNewListSize(): Int = newList.size

					override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
						oldList[oldItemPosition] == newList[newItemPosition]

					override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
						oldList[oldItemPosition] == newList[newItemPosition]
				}

			DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(object : ListUpdateCallback {
				override fun onInserted(position: Int, count: Int) {
					println("On Inserted: $position, $count")
				}

				override fun onRemoved(position: Int, count: Int) {
					println("On Removed: $position, $count")
				}

				override fun onMoved(fromPosition: Int, toPosition: Int) {
					println("On Moved: $fromPosition, $toPosition")
				}

				override fun onChanged(position: Int, count: Int, payload: Any?) {
					println("On Changed: $position, $count, $payload")
				}
			})

			Box(modifier = Modifier
				.fillMaxSize()
				.background(Color.Black)
			) {
				ActionLazyColumn(
					list = list,
					onPlaced = ::moveList
				) { idx, item, listActionState ->
					TestLayout(idx = idx, item = item, listActionState = listActionState)
				}
			}
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