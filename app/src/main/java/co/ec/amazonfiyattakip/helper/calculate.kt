package co.ec.amazonfiyattakip.helper

import kotlin.math.pow

fun predictNextPrices(prices: List<Double>, days: List<Int> = listOf(7,14,21,28)): List<Double> {
    val n = prices.size
    val x = (1..n).map { it.toDouble() }
    val y = prices

    // Ortalama hesapla
    val xMean = x.average()
    val yMean = y.average()

    // Beta1 ve Beta0 hesapla (y = Beta0 + Beta1 * x)
    val numerator = x.zip(y).sumOf { (xi, yi) -> (xi - xMean) * (yi - yMean) }
    val denominator = x.sumOf { (it - xMean).pow(2) }
    val beta1 = numerator / denominator
    val beta0 = yMean - beta1 * xMean

    // Sonraki günleri tahmin et
    return days.map { day -> beta0 + beta1 * (n + day) }
}