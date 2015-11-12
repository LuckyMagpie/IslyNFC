package project.com.isly.helpers;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.v1tor.nuno.projectoiii.controllers.NFCControl;
import com.v1tor.nuno.projectoiii.interfaces.INFCEvents;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by Diego on 11/1/2015.
 */
public class NFCFragment extends Fragment implements INFCEvents {

    public NFCControl NFCControl;
    public INFCEvents INFCEvents;

    public NFCFragment() {

        INFCEvents = this;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NFCControl = new NFCControl(getActivity().getApplicationContext(), INFCEvents);

        NFCControl.startBroadCastListening();

        if (!NFCControl.hasNfc()) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    "NFC is not available, this application may not work correctly on this device.",
                    Toast.LENGTH_LONG).show();
        } else {

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent newIntent = new Intent("org.opennfc.intent.action.NFC_INTENT");
        newIntent.putExtra("NewIntent", data);
        getActivity().getApplicationContext().sendBroadcast(newIntent);
    }

    @Override
    public void onResume() {
        super.onResume();

        NFCControl.enableForegroundDispatch(getActivity(), this.getClass());

    }

    @Override
    public void onPause() {

        super.onPause();

        NFCControl.disableForegroundDispatch(getActivity());

    }

    public void onDestroy() {
        super.onDestroy();

        NFCControl.stopBroadCastListening();
    }

    @Override
    public void onTagDiscovered(Context context, Intent intent) {
        //invocado quando um tag e descoberta
    }

    @Override
    public NdefMessage onCreateNfcMessage(NfcEvent event) {
        //invocado para obter mensagem que se pretende escrever na tag
        return null;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

    }

    @Override
    public NdefMessage onWriteTag(Context context, Intent intent) {
        return null;
    }

    @Override
    public void onWriteTagSuccess(Tag tag, NdefMessage msg) {
        //invocado quando o conteudo foi escrito com sucesso na tag
    }

    @Override
    public void onWriteTagError(Tag tag, NdefMessage msg, int writeResult) {
        //invocado quando ocorre um erro ao escrever na tag
    }

    @Override
    public NdefMessage getNFCMessageToWrite() {
        return null;
    }

    public NdefRecord Serialize(String string) {

        final byte[] language = Locale.ENGLISH.getLanguage().getBytes(
                Charset.forName("US-ASCII"));
        final byte[] text = string.getBytes(Charset.forName("UTF-8"));

        final int languageSize = language.length;
        final int textLength = text.length;

        final byte[] payload = new byte[1 + languageSize + textLength];

        payload[0] = (byte) (languageSize & 0x1F);

        System.arraycopy(language, 0, payload, 1, languageSize);
        System.arraycopy(text, 0, payload, 1 + languageSize, textLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,
                new byte[0], payload);
    }

    public String getReceivedPayloadString(Intent intent) {
        String result = null;
        Parcelable[] rawMsgs = intent
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = null;
        try {
            msg = (NdefMessage) rawMsgs[0];
            // record 1 is the AAR, if present
            byte[] payload = msg.getRecords()[0].getPayload();
            int languageSize = payload[0] & 0xff;
            int textSize = payload.length - 1 - languageSize;
            byte[] textPayload = new byte[textSize];
            System.arraycopy(payload, languageSize + 1, textPayload, 0,
                    textSize);
            result = new String(textPayload);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
