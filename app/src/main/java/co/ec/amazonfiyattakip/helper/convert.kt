package co.ec.amazonfiyattakip.helper

import java.text.NumberFormat
import java.util.Locale


fun Int.price(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(this.toFloat() / 100F)
}