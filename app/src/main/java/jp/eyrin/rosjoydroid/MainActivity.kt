package jp.eyrin.rosjoydroid

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import jp.eyrin.rosjoydroid.ui.theme.ROSJoyDroidTheme
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max

class MainActivity : GamepadActivity() {
    private lateinit var publishJoyTimer: Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getPreferences(Context.MODE_PRIVATE)

        setContent {
            var domainId by remember { prefs.mutableStateOf("domainId", 0) }
            var ns by remember { prefs.mutableStateOf("ns", "") }
            var period by remember { prefs.mutableStateOf("period", 20L) }
            var deadZone by remember { prefs.mutableStateOf("deadZone", 0.05f) }

            LifecycleResumeEffect(domainId, ns, period) {
                startPublishJoy(domainId, ns, period)
                onPauseOrDispose {
                    stopPublishJoy()
                }
            }

            LaunchedEffect(deadZone) {
                super.deadZone = deadZone
            }

            ROSJoyDroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            space = 10.dp,
                            alignment = Alignment.CenterVertically,
                        )
                    ) {
                        Text(
                            text = "ROSJoyDroid",
                            fontSize = 32.sp,
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(400.dp),
                            value = ns,
                            onValueChange = {
                                ns = it
                            },
                            label = { Text("Namespace") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                        )
                        Row(
                            modifier = Modifier.width(400.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = domainId.toString(),
                                onValueChange = {
                                    domainId = (it.toIntOrNull() ?: 0).coerceIn(0, 101)
                                },
                                label = { Text("Domain ID") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = period.toString(),
                                onValueChange = {
                                    period = max(it.toLongOrNull() ?: 20, 1)
                                },
                                label = { Text("Period (ms)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                modifier = Modifier.weight(1f),
                                value = deadZone.toString(),
                                onValueChange = {
                                    deadZone = (it.toFloatOrNull() ?: 0.05f).coerceIn(0f, 1f)
                                },
                                label = { Text("Dead zone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startPublishJoy(domainId: Int, ns: String, period: Long) {
        stopPublishJoy()
        createJoyPublisher(domainId, ns)
        publishJoyTimer = Timer().also {
            it.schedule(object : TimerTask() {
                override fun run() {
                    publishJoy(axes, buttons)
                }
            }, 0, period)
        }
    }

    private fun stopPublishJoy() {
        runCatching { publishJoyTimer.cancel() }
        destroyJoyPublisher()
    }

    private external fun createJoyPublisher(domainId: Int, ns: String)
    private external fun destroyJoyPublisher()
    private external fun publishJoy(axes: FloatArray, buttons: IntArray)

    companion object {
        init {
            System.loadLibrary("rosjoydroid")
        }
    }
}
