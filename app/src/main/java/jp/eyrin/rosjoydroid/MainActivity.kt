package jp.eyrin.rosjoydroid

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import jp.eyrin.rosjoydroid.ui.theme.ROSJoyDroidTheme
import java.util.Timer
import java.util.TimerTask
import kotlin.math.max

class MainActivity : GamepadActivity() {
    private lateinit var publishJoyTimer: Timer

    @OptIn(ExperimentalMaterial3Api::class)
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
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ), title = {
                                Text("ROSJoyDroid")
                            })
                        },
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    label = { Text("Domain ID") },
                                    value = domainId.toString(),
                                    onValueChange = {
                                        domainId = (it.toIntOrNull() ?: 0).coerceIn(0, 101)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    label = { Text("Namespace") },
                                    value = ns,
                                    onValueChange = {
                                        ns = it
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    label = { Text("Period(ms)") },
                                    value = period.toString(),
                                    onValueChange = {
                                        period = max(it.toLongOrNull() ?: 20, 1)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    label = { Text("Dead zone") },
                                    value = deadZone.toString(),
                                    onValueChange = {
                                        deadZone = (it.toFloatOrNull() ?: 0.05f).coerceIn(0f, 1f)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                )
                            }
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
                    Log.d("lr", axes.joinToString(","))
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
