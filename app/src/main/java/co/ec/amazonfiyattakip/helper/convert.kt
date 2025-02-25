package co.ec.amazonfiyattakip.helper

import androidx.compose.ui.graphics.Color
import java.text.NumberFormat
import java.util.Locale
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


fun Int.price(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(this.toFloat() / 100F)
}

fun Any.autoToString(): String {
    val className = this::class.simpleName
    val properties = this::class.memberProperties.joinToString { prop ->
        val value = runCatching { prop.get(this as Nothing) }.getOrNull() ?: "null"
        "${prop.name}=$value"
    }
    return "$className($properties)"
}

fun Color.toHtml(): String {
    return String.format("#%02X%02X%02X",
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}
fun Color.toHtmlWithAlpha(): String {
    return String.format("#%02X%02X%02X%02X",
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}