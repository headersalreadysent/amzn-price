package co.ec.amazonfiyattakip.service

import co.ec.helper.AppSharedSettings
import co.ec.helper.Async
import co.ec.helper.utils.unix
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object AmznRequest {

    var client: OkHttpClient = OkHttpClient()

    val sharedSettings = AppSharedSettings.get()

    init {
        obtainCookieJar()
    }

    private fun getCookies(): String {
        val cookieJar = sharedSettings.getString("cookieJar") ?: "[]"
        val cookieList = Json.decodeFromString<List<Triple<String, String, Int>>>(cookieJar)
        val cookieString = StringBuilder()
        val now = unix()
        cookieList.filter {
            if (it.second != "") {
                if (it.third < now) {
                    cookieString.append("${it.first}=${it.second};")
                    return@filter true
                }
            } else {
                return@filter false
            }
            return@filter true
        }
        return cookieString.toString()
    }

    private fun recordCookies(cookies: List<String>) {
        val yearLater = unix() + 86400 * 365
        val oldJar = sharedSettings.getString("cookieJar") ?: "[]"
        var cookieList =
            Json.decodeFromString<List<Triple<String, String, Int>>>(oldJar).toMutableList()

        val cookieMap = cookies.forEach {
            val parts = it.split(";")
            val expires = parts.find { it.startsWith("Expires=") }
            var expireUnix = yearLater
            if (expires != null) {
                // Create a SimpleDateFormat instance
                val format = SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH)
                format.timeZone = TimeZone.getTimeZone("GMT")
                val date: Date? = format.parse(expires.split("=")[1])
                expireUnix = date?.time?.div(1000) ?: yearLater
            }
            val value = parts[0].split("=")
            cookieList = cookieList.filter { it.first != value[0] }.toMutableList()
            cookieList.add(Triple(value[0], value[1], expireUnix.toInt()))
        }
        val cookieJar = Json.encodeToString(cookieList)
        sharedSettings.putString("cookieJar", cookieJar)
    }


    private fun obtainCookieJar() {
        Async.run({
            //look old cookies
            var request = Request.Builder()
                .url("https://www.amazon.com.tr/")
            request = generateHeaders(request)
            var response = client.newCall(request.build())
                .execute()

            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val cookie = response.headers("set-cookie").toList()
            return@run cookie
        }, {
            recordCookies(it)
        })

    }


    fun request(
        url: String,
        then: (res: String?) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        Async.run({
            var request = Request.Builder()
                .url(url)
            request = generateHeaders(request)

            client.newCall(request.build())
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }
                    recordCookies(response.headers("set-cookie"))
                    return@run response.body?.string()
                }
        }, {
            then(it)
        }, err)


    }

    private fun generateHeaders(
        req: Request.Builder,
    ): Request.Builder {
        val request = req.header(
            "accept",
            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
        )
            .header("accept-language", "tr-TR,en-US,en;q=0.9,de;q=0.8,tr;q=0.7")
            .header("cache-control", "no-cache")
            .header("device-memory", "8")
            .header("downlink", "4.1")
            .header("dpr", "0.90625")
            .header("ect", "4g")
            .header("pragma", "no-cache")
            .header("priority", "u=0,i")
            .header("referer", "https://www.google.com/")
            .header("rtt", "50")
            .header("sec-ch-device-memory", "8")
            .header("sec-ch-dpr", "0.90625")
            .header("cookie", getCookies())
            .header(
                "sec-ch-ua",
                "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\""
            )
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Linux\"")
            .header("sec-ch-viewport-width", "2120")
            .header("sec-fetch-dest", "document")
            .header("sec-fetch-mode", "navigate")
            .header("sec-fetch-site", "same-origin")
            .header("sec-fetch-user", "?1")
            .header("upgrade-insecure-requests", "1")
            .header(
                "user-agent",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
            )
            .header("viewport-width", "2120")



        return request

    }

    fun getRealUrl(
        url: String,
        then: (res: String) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        Async.run({
            val client = OkHttpClient.Builder()
                .followRedirects(false)
                .build()

            val request = Request.Builder()
                .url(url)
                .build()
            client
                .newCall(request)
                .execute().use { response: Response ->
                    if (response.isRedirect) {
                        val redirectUrl = response.header("Location") ?: ""
                        return@run redirectUrl
                    } else {
                        throw IOException("No redirect $response")
                    }
                }
        }, then, err)

    }

}