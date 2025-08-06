package co.ec.amazonfiyattakip.helper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.helper.helpers.LogHelper
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.pow

fun predictNextPrices(
    prices: List<PriceInfo>,
    days: List<Int> = listOf(15,30,45)
): Pair<List<Pair<Long, Double>>, AnnotatedString> {

    val predicter=predictFunction(prices)
    val lastDay = prices.maxOf { it.date }
    // return
    return Pair(days
        .map { day -> predicter.second(day) }
        .mapIndexed { index, it ->
            Pair(lastDay + days[index] * 86400, it * 100)
        }, predicter.first
    )

}

fun predictFunction(
    prices: List<PriceInfo>,
): Pair<AnnotatedString, (Int) -> Double> {

    val x = prices.map { (it.date) }
    val y = prices.map { (it.price / 100).toDouble() }
    val lastDay = x.maxOf { it }

    // Ortalama hesapla
    val xMean = x.average()
    val yMean = y.average()

    // Beta1 ve Beta0 hesapla (y = Beta0 + Beta1 * x)
    val numerator = x.zip(y).sumOf { (xi, yi) -> (xi - xMean) * (yi - yMean) }
    val denominator = x.sumOf { (it - xMean).pow(2) }
    val beta1 = numerator / denominator
    val beta0 = yMean - beta1 * xMean
    val beta0Text = String.format(Locale.getDefault(), "%.2f", beta0)
    val beta1Text = String.format(Locale.getDefault(), "%.6f", beta1.absoluteValue)
    val boldStyle = SpanStyle(
        fontWeight = FontWeight.SemiBold
    )
    val functionText=buildAnnotatedString {
        withStyle(boldStyle) {
            append(beta0Text)
        }
        append(if(beta1<0) " - " else " + ")
        withStyle(boldStyle) {
            append(beta1Text)
        }
        append(" * (${lastDay} + ")
        withStyle(boldStyle) {
            append("day")
        }
        append(" * 86400) ")
    }

    // return
    return Pair(functionText,{ day:Int -> beta0 + beta1 * (lastDay + day * 86400) })

}


// Extension property to get the luminance of a Color
val Color.luminance: Float
    get() {
        // Linearize the RGB components first (if they aren't already)
        // For standard sRGB colors from Compose, you usually don't need
        // a full sRGB to linear conversion for a quick luminance check.
        // Direct component access usually works for a basic check.
        val r = red
        val g = green
        val b = blue

        // ITU-R BT.709 coefficients for luminance calculation
        // These are standard weights for human perception of brightness.
        return (0.2126f * r + 0.7152f * g + 0.0722f * b)
    }

// Extension function to check if a Color is light
fun Color.isLight(): Boolean {
    // You can adjust the threshold (0.5f) if needed
    // A lower threshold means more colors are considered "light".
    // 0.5f is a common starting point.
    return this.luminance > 0.5f
}
