package co.ec.amazonfiyattakip

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import co.ec.amazonfiyattakip.service.AmznRequest
import co.ec.helper.helpers.SettingsHelper

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var sharedSettings = SettingsHelper.get()

        // Check if the activity was started by a share intent
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                val regex = "(https?://[\\w-]+(\\.[\\w-]+)+(/[\\w-./?%&=]*)?)".toRegex()
                val url = regex.find(sharedText)?.value
                if (url != null) {
                    AmznRequest.getRealUrl(url, { real ->
                        sharedSettings.putString("sharedUrl", real)
                        redirect()
                    }, {
                        sharedSettings.putString("sharedUrl", "")
                        redirect()
                    })
                } else {
                    sharedSettings.putString("sharedUrl", "")

                    redirect()
                }

            } else {
                redirect()
            }
        }
    }

    private fun redirect() {

        //redirect to main
        val redirectIntent = Intent(this, MainActivity::class.java)
        redirectIntent.putExtra("destination", "add")
        startActivity(redirectIntent)
    }
}

