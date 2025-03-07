package co.ec.amazonfiyattakip.helper

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

fun predictNextPrices(
    prices: List<PriceInfo>,
    days: List<Int> = listOf(7, 14, 21, 28)
): Pair<List<Pair<Long, Double>>, AnnotatedString> {

    val firstQuery = prices.minOf { it.date }
    val x = prices.map { (it.date - firstQuery) }
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
    val beta1Text = String.format(Locale.getDefault(), "%.6f", beta1)
    val boldStyle = SpanStyle(
        fontWeight = FontWeight.SemiBold
    )
    val functionText=buildAnnotatedString {
        withStyle(boldStyle) {
            append(beta0Text)
        }
        append(" + ")
        withStyle(boldStyle) {
            append(beta1Text)
        }
        append(" * (${lastDay + firstQuery} + ")
        withStyle(boldStyle) {
            append("day")
        }
        append(" * 86400) ")
    }

    // return
    return Pair(days
        .map { day -> beta0 + beta1 * (lastDay + day * 86400) }
        .mapIndexed { index, it ->
            Pair(lastDay + firstQuery + days[index] * 86400, it * 100)
        }, functionText
    )

}