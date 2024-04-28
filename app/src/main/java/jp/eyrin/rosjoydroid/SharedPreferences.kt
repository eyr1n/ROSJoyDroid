package jp.eyrin.rosjoydroid

import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

inline fun <reified T> SharedPreferences.mutableStateOf(
    key: String, defValue: T
): MutableState<T> {
    val state = mutableStateOf(
        when (T::class) {
            String::class -> getString(key, defValue as String) as T
            Int::class -> getInt(key, defValue as Int) as T
            Long::class -> getLong(key, defValue as Long) as T
            Float::class -> getFloat(key, defValue as Float) as T
            Boolean::class -> getBoolean(key, defValue as Boolean) as T
            else -> throw IllegalArgumentException("\"${T::class.simpleName}\" is not supported.")
        }
    )

    return object : MutableState<T> by state {
        override var value: T
            get() = state.value
            set(value) {
                state.value = value
                edit {
                    when (T::class) {
                        String::class -> putString(key, value as String)
                        Int::class -> putInt(key, value as Int)
                        Long::class -> putLong(key, value as Long)
                        Float::class -> putFloat(key, value as Float)
                        Boolean::class -> putBoolean(key, value as Boolean)
                    }
                }
            }
    }
}
