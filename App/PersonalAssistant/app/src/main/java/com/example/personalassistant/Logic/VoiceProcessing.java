package com.example.personalassistant.Logic;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

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

import com.example.personalassistant.Activities.MainActivity;
import com.example.personalassistant.R;

public class VoiceProcessing extends Activity implements RecognitionListener
{
    private static final String USR_INPUT = "User Commands";
    private final Context REFERENCE;

    private String hyp = null;
    private SpeechRecognizer recognizer;

    public VoiceProcessing(MainActivity activity)
    {
        REFERENCE = activity.getApplicationContext();
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
            hyp = hypothesis.getHypstr();
            makeText(REFERENCE, hyp, Toast.LENGTH_SHORT).show();

            writeFileExternalStorage(hyp);
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
        File voiceLog = new File(REFERENCE.getExternalFilesDir(null), "voiceLog.txt");
        File lastCommand = new File(REFERENCE.getExternalFilesDir(null), "prevCommand.txt");

        //This point and below is responsible for the write operation
        FileOutputStream outputStream;
        try {
            if(voiceLog.createNewFile())
            {
                //Writing to file that stores a log of all commands issued to the assistant
                outputStream = new FileOutputStream(voiceLog, true);
                outputStream.write(textToWrite.getBytes());
                outputStream.write("\n".getBytes());
                outputStream.flush();
                outputStream.close();
            }
            else
                makeText(REFERENCE, "Couldn't write to voiceLog.txt", Toast.LENGTH_SHORT).show();

            if(lastCommand.createNewFile())
            {
                //Writing to file only the previous command issued to the assistant
                outputStream = new FileOutputStream(lastCommand, false);
                outputStream.write(textToWrite.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            else
                makeText(REFERENCE, "Couldn't write to lastCommand.txt", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
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

    //Returns the hypothesis, can only be called after onResult
    public String getHyp()
    {
        return hyp;
    }

    private void startListening() {
        recognizer.stop();

        recognizer.startListening(USR_INPUT);
    }
}
