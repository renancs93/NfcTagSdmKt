package br.edu.ifsp.nfctagsdmkt

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val nfcWorker = NfcWorker()
    lateinit var adaptadorNfc: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gravarBt.setOnClickListener {
            if (escritaEt.text.toString().isNotEmpty()) {
                it.isEnabled = false
                escritaEt.isEnabled = false
            }
        }

        adaptadorNfc = NfcAdapter.getDefaultAdapter(this)

    }

    override fun onResume() {
        super.onResume()

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            0
        )

        val ndefIf = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "text/plain")
        val techIf = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val tagIf = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ifArray = arrayOf(ndefIf, techIf, tagIf)

        val listaTecsArray = arrayOf(arrayOf<String>(
            NfcA::class.java.name,
            Ndef::class.java.name,
            MifareClassic::class.java.name
        ))

        adaptadorNfc.enableForegroundDispatch(
            this,
            pendingIntent,
            ifArray,
            listaTecsArray
        )

    }

    override fun onPause() {
        super.onPause()

        adaptadorNfc.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val infoTag: String = nfcWorker.lerGravarTag(intent, !gravarBt.isEnabled, escritaEt.text.toString())

        leituraTv.text = infoTag

        if (!gravarBt.isEnabled){
            gravarBt.isEnabled = true
            escritaEt.setText("")
            escritaEt.isEnabled = true
        }

    }


}
