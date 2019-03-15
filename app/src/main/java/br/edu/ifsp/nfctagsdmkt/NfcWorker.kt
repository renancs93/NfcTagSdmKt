package br.edu.ifsp.nfctagsdmkt

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Parcelable
import java.nio.charset.Charset

class NfcWorker {

    private lateinit var tag: Tag

    fun lerGravarTag(intent: Intent, gravar: Boolean, payload: String = ""): String {
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

        val listaNdefMsgs: MutableList<NdefMessage> = mutableListOf()

        // Leitura
        var infoTagSb: StringBuffer = StringBuffer()
        infoTagSb.append("Tag ID: ${tag.id}\n")
        infoTagSb.append("Lista de tecnologias: \n")
        tag.techList.withIndex().forEach({
            infoTagSb.append("\t ${it.index} - ${it.value}")
        })

        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED -> {
                val ndefMessages: Array<Parcelable> = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES
                )

                ndefMessages.withIndex().forEach({ msgParcelable ->
                    infoTagSb.append("Mensagem: ${msgParcelable.index}\n")

                    val ndefMessage: NdefMessage = msgParcelable.value as NdefMessage
                    ndefMessage.records.withIndex().forEach({ registro ->
                        infoTagSb.append("\t\t Registro: ${registro.index}\n")
                        infoTagSb.append("\t\t TNF: ${registro.value.tnf}\n")
                        infoTagSb.append("\t\t Tipo payload: ${String(registro.value.type)}\n")
                        infoTagSb.append("\t\t Payload puro: ${String(registro.value.payload)}\n")
                    })
                    listaNdefMsgs.add(ndefMessage)
                })
            }
            NfcAdapter.ACTION_TECH_DISCOVERED -> infoTagSb.append(
                "Tipo de payload desconhecido ou não foi tratado pela Activity"
            )
            NfcAdapter.ACTION_TAG_DISCOVERED -> infoTagSb.append(
                "Tecnologia não suportada pela Activity"
            )
        }

        // Escrita
        if (gravar) {
            gravarTag(listaNdefMsgs, payload)
        }

        return infoTagSb.toString()
    }

    private fun gravarTag(listaNdefMsgs: MutableList<NdefMessage>, payload: String) {

        // lista geral de NDEF record
        val listaNdefRecs: MutableList<NdefRecord> = mutableListOf()

        // Adicionar todos os recs na lista geral e recs ndef
        listaNdefMsgs.forEach { listaNdefRecs.addAll(it.records) }

        // Criar um novo registro
        val charset = Charset.forName("UTF-8")
        val novoNdefRec = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            ByteArray(0),
            payload.toByteArray(charset)
        )

        listaNdefRecs.add(novoNdefRec)

        val novaNdefMsg = NdefMessage(listaNdefRecs.toTypedArray())
        listaNdefMsgs.add(novaNdefMsg)

        listaNdefMsgs.forEach { ndefMsg ->
            if (NdefFormatable.get(tag) != null) {
                NdefFormatable.get(tag).use { nf ->
                    nf.connect()
                    nf.format(ndefMsg)
                    nf.close()
                }
            } else {
                Ndef.get(tag)?.use { ndef ->
                    ndef.connect()
                    ndef.writeNdefMessage(ndefMsg)
                    ndef.close()
                }
            }
        }

    }


}