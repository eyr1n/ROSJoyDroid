package jp.eyrin.rosjoydroid

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

open class GamepadActivity : ComponentActivity() {
    private val _axes = MutableStateFlow(floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f))
    private val _buttons = MutableStateFlow(IntArray(GamepadButton.entries.size))
    var deadZone = 0f

    val axes get() = _axes.value
    val buttons get() = _buttons.value

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.source and (InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK) != 0) {
            GamepadButton.fromKeyCode(keyCode)?.let {
                updateGamepadButton(it, 1)
                true
            } ?: super.onKeyDown(keyCode, event)
        } else super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.source and (InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK) != 0) {
            GamepadButton.fromKeyCode(keyCode)?.let {
                updateGamepadButton(it, 0)
                true
            } ?: super.onKeyDown(keyCode, event)
        } else super.onKeyDown(keyCode, event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return if (event.source and (InputDevice.SOURCE_GAMEPAD or InputDevice.SOURCE_JOYSTICK) != 0 && event.action == MotionEvent.ACTION_MOVE) {
            this._axes.value = floatArrayOf(
                truncateAxis(event.getAxisValue(MotionEvent.AXIS_X)),
                truncateAxis(event.getAxisValue(MotionEvent.AXIS_Y)),
                truncateAxis(event.getAxisValue(MotionEvent.AXIS_Z)),
                truncateAxis(event.getAxisValue(MotionEvent.AXIS_RZ)),
                truncateAxis(event.getAxisValue(MotionEvent.AXIS_BRAKE)),
                truncateAxis(event.getAxisValue(MotionEvent.AXIS_GAS)),
            )

            val xAxis = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            val yAxis = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
            when (xAxis) {
                -1.0f -> updateGamepadButton(GamepadButton.DPAD_LEFT, 1)
                1.0f -> updateGamepadButton(GamepadButton.DPAD_RIGHT, 1)
                else -> {
                    updateGamepadButton(GamepadButton.DPAD_LEFT, 0)
                    updateGamepadButton(GamepadButton.DPAD_RIGHT, 0)
                }
            }
            when (yAxis) {
                -1.0f -> updateGamepadButton(GamepadButton.DPAD_UP, 1)
                1.0f -> updateGamepadButton(GamepadButton.DPAD_DOWN, 1)
                else -> {
                    updateGamepadButton(GamepadButton.DPAD_UP, 0)
                    updateGamepadButton(GamepadButton.DPAD_DOWN, 0)
                }
            }

            true
        } else {
            super.onGenericMotionEvent(event)
        }
    }

    private fun updateGamepadButton(key: GamepadButton, state: Int) {
        _buttons.update {
            it.clone().apply {
                this[key.ordinal] = state
            }
        }
    }

    private fun truncateAxis(value: Float): Float {
        return if (value in -deadZone..deadZone) 0f else value
    }
}

enum class GamepadButton {
    A, B, X, Y, BACK, GUIDE, START, LEFTSTICK, RIGHTSTICK, LEFTSHOULDER, RIGHTSHOULDER, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, MISC1, PADDLE1, PADDLE2, PADDLE3, PADDLE4, TOUCHPAD;

    companion object {
        fun fromKeyCode(keyCode: Int) = when (keyCode) {
            KeyEvent.KEYCODE_BUTTON_A -> A
            KeyEvent.KEYCODE_BUTTON_B -> B
            KeyEvent.KEYCODE_BUTTON_X -> X
            KeyEvent.KEYCODE_BUTTON_Y -> Y
            KeyEvent.KEYCODE_BUTTON_SELECT -> BACK
            KeyEvent.KEYCODE_BUTTON_MODE -> GUIDE
            KeyEvent.KEYCODE_BUTTON_START -> START
            KeyEvent.KEYCODE_BUTTON_THUMBL -> LEFTSTICK
            KeyEvent.KEYCODE_BUTTON_THUMBR -> RIGHTSTICK
            KeyEvent.KEYCODE_BUTTON_L1 -> LEFTSHOULDER
            KeyEvent.KEYCODE_BUTTON_R1 -> RIGHTSHOULDER
            KeyEvent.KEYCODE_DPAD_UP -> DPAD_UP
            KeyEvent.KEYCODE_DPAD_DOWN -> DPAD_DOWN
            KeyEvent.KEYCODE_DPAD_LEFT -> DPAD_LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> DPAD_RIGHT
            else -> null
        }
    }
}
