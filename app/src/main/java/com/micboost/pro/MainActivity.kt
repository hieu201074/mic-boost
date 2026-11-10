
package com.micboost.pro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val Black = Color(0xFF101014)
private val Red = Color(0xFFFF3030)
private val Panel = Color(0xFF201D22)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Black,
                    surface = Panel,
                    primary = Red
                )
            ) {
                MicBoostScreen()
            }
        }
    }
}

@Composable
fun MicBoostScreen() {

    val context = LocalContext.current
    val engine = remember { AudioEngine() }

    var running by remember { mutableStateOf(false) }
    var gain by remember { mutableFloatStateOf(2f) }
    var compressor by remember { mutableStateOf(true) }
    var message by remember {
        mutableStateOf("MIC ENGINE READY")
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {
                try {
                    engine.setGain(gain)
                    engine.start()
                    running = true
                    message = "MIC ENGINE ACTIVE"
                } catch (e: Exception) {
                    message = "ERROR: ${e.message}"
                }
            } else {
                message = "MICROPHONE PERMISSION DENIED"
            }
        }

    DisposableEffect(Unit) {
        onDispose {
            engine.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "MIC BOOST PRO",
            color = Red,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )

        Text(
            "ANDROID VOICE ENGINE",
            color = Color.LightGray,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(30.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Panel
            ),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    "MICROPHONE GAIN",
                    color = Color.LightGray
                )

                Text(
                    "${(gain * 100).toInt()}%",
                    color = Red,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = gain,
                    onValueChange = {
                        gain = it
                        engine.setGain(it)
                    },
                    valueRange = 1f..5f,
                    colors = SliderDefaults.colors(
                        thumbColor = Red,
                        activeTrackColor = Red
                    )
                )

                Text(
                    "100% - 500%",
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "COMPRESSOR",
                color = Color.White
            )

            Switch(
                checked = compressor,
                onCheckedChange = {
                    compressor = it
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (running) {
                    engine.stop()
                    running = false
                    message = "MIC ENGINE STOPPED"
                } else {
                    val granted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        try {
                            engine.setGain(gain)
                            engine.start()
                            running = true
                            message = "MIC ENGINE ACTIVE"
                        } catch (e: Exception) {
                            message = "ERROR: ${e.message}"
                        }
                    } else {
                        permissionLauncher.launch(
                            Manifest.permission.RECORD_AUDIO
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (running) {
                    Color.DarkGray
                } else {
                    Red
                }
            )
        ) {
            Text(
                if (running) "STOP ENGINE"
                else "START MICROPHONE ENGINE",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            message,
            color = if (running) Color.Green else Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Voice processing preview only",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}
