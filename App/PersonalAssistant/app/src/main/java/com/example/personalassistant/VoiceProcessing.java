package com.example.personalassistant;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static android.widget.Toast.makeText;

public class VoiceProcessing extends Activity implements RecognitionListener
{
    private static final String KEYPHRASE = "listen";
    protected static final String KWS_SEARCH = "wakeup";
    protected static final String USR_INPUT = "active listening";
    private Context reference;

    protected SpeechRecognizer recognizer;

    public VoiceProcessing(MainActivity activity)
    {
        reference = activity.getApplicationContext();
        new SetupTask(activity).execute();
    }

    private class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;

        SetupTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.caption_text))
                        .setText("Failed to init recognizer " + result);
            }
            startListening();
        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech()
    {
        recognizer.stop();

        if (recognizer != null)
        {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {}

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            makeText(reference, text, Toast.LENGTH_SHORT).show();

            writeFileExternalStorage(text);
        }
    }

    @Override
    public void onError(Exception e)
    {
        makeText(getApplicationContext(), "Failed to use Voice Recognition", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeout(){}

    public void writeFileExternalStorage(String textToWrite) {
        //Checking the availability state of the External Storage.
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {

            //If it isn't mounted - we can't write into it.
            return;
        }

        //Create a new file that points to the root directory, with the given name:
        File file = new File(reference.getExternalFilesDir(null), "output.txt");

        //This point and below is responsible for the write operation
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            outputStream = new FileOutputStream(file, true);

            outputStream.write(textToWrite.getBytes());
            outputStream.write("\n".getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException
    {
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .getRecognizer();

        recognizer.addListener(this);

       //recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
        File searchGram = new File(assetsDir, "custom.gram");
        recognizer.addGrammarSearch(USR_INPUT, searchGram);
    }

    protected void startListening() {
        recognizer.stop();

        recognizer.startListening(USR_INPUT);
    }
}
