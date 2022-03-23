package com.example.personalassistant.Logic;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import android.os.Environment;
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

public class VoiceProcessing extends Activity implements RecognitionListener
{
    public boolean toggle; //Determines whether command mode(false) or FFSearch(true)

    private static final String USR_INPUT = "User Commands";
    private static final String FF_SPEECH = "Freeform Speech";

    private final Context REFERENCE;

    private boolean isDone;
    private SpeechRecognizer recognizer;

    public VoiceProcessing(MainActivity activity)
    {
        isDone = false;
        toggle = false;
        REFERENCE = activity.getApplicationContext();
        new SetupTask(activity).execute();
    }

    /*
    Class provided with cmuPocketSphinx, initializes the recognizer on a separate thread
     */
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
            if (result != null)
            {
                makeText(REFERENCE, "Failed to init recognizer ", Toast.LENGTH_LONG).show();
                result.printStackTrace();
            }
            //Alerts the mainActivity that the recognizer has finished loading
            MainActivity.recogInit.setValue(true);
            makeText(REFERENCE, "Recognizer ready to receive input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBeginningOfSpeech() {}

    @Override
    public void onEndOfSpeech()
    {
        recognizer.stop();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {}

    /*
    Provided with cmuPocketSphinx, is called when the recognizer detects an end to the user's input
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null)
            writeFileExternalStorage(hypothesis.getHypstr());

        //Displays the user's input back to them
        makeText(REFERENCE, hypothesis.getHypstr(), Toast.LENGTH_LONG).show();
        System.out.println(hypothesis.getHypstr());
    }

    @Override
    public void onError(Exception e)
    {
        makeText(getApplicationContext(), "Failed to use Voice Recognition", Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }

    @Override
    public void onTimeout() {}

    /*
    Writes the interpreted user's command from onResult to storage for later use by the TextParser,
    also provides transparency to the user for how their input is used
     */
    public void writeFileExternalStorage(String textToWrite)
    {
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
            if(voiceLog.createNewFile() || voiceLog.isFile())
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

            if(lastCommand.createNewFile() || lastCommand.isFile())
            {
                //Writing to file only the previous command issued to the assistant
                outputStream = new FileOutputStream(lastCommand, false);
                outputStream.write(textToWrite.getBytes());
                outputStream.flush();
                outputStream.close();
            }
            else
                makeText(REFERENCE, "Couldn't write to prevCommand.txt", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        isDone = true;
    }

    private void setupRecognizer(File assetsDir) throws IOException
    {
        //Initializes the recognizer and sets required filepaths
        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .getRecognizer();

        recognizer.addListener(this);

        File searchGram = new File(assetsDir, "custom.gram");
        File languageModel = new File(assetsDir, "en-70k-0.2-pruned.lm");

        recognizer.addGrammarSearch(USR_INPUT, searchGram);
        recognizer.addNgramSearch(FF_SPEECH, languageModel);
        //recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);
    }

    public void startListening() {
        //Ensures the recognizer is in a stopped/idle state before attempting to start
        recognizer.stop();

        if(!toggle)
            recognizer.startListening(USR_INPUT);
        else
            recognizer.startListening(FF_SPEECH);
    }

    public boolean getStatus()
    {
        return isDone;
    }

    public void resetStatus()
    {
        isDone = false;
    }
}
