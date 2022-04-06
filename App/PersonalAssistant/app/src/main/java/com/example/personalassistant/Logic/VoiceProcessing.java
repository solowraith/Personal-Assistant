package com.example.personalassistant.Logic;

import static android.widget.Toast.makeText;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.personalassistant.Activities.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Scanner;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class VoiceProcessing extends Activity implements RecognitionListener {
    private static final String USR_INPUT = "User Commands";
    private static final String FF_SPEECH = "Freeform Speech";
    private static final String KWS_SEARCH = "Passive Speech";
    private static final String DEFAULT_KEYPHRASE = "listen";
    private final Context REFERENCE;
    public boolean toggle; //Determines whether command mode(false) or FFSearch(true)
    private String chosenKeyPhrase;
    private boolean passiveSpeech = false;
    private SpeechRecognizer recognizer;

    public VoiceProcessing(MainActivity activity) {
        toggle = false;
        REFERENCE = activity.getApplicationContext();
        new SetupTask(activity).execute();
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            startListening(true);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        if (passiveSpeech && hypothesis.getHypstr().equals(chosenKeyPhrase)) {
            makeText(REFERENCE, hypothesis.getHypstr(), Toast.LENGTH_LONG).show();
            System.out.println(hypothesis.getHypstr());
            startListening(false);
        }
    }

    /*
    Provided with cmuPocketSphinx, is called when the recognizer detects an end to the user's input
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            writeFileExternalStorage(hypothesis.getHypstr());

            //Displays the user's input back to them
            makeText(REFERENCE, hypothesis.getHypstr(), Toast.LENGTH_LONG).show();
            System.out.println(hypothesis.getHypstr());
            toTextParser(hypothesis.getHypstr());
        } else
            Log.e("VoiceProcessing", "User input was interpreted as null");
    }

    @Override
    public void onError(Exception e) {
        makeText(getApplicationContext(), "Failed to use Voice Recognition", Toast.LENGTH_SHORT).show();
        e.printStackTrace();
    }

    @Override
    public void onTimeout() {
    }

    /*
    Writes the interpreted user's command from onResult to storage for later use by the TextParser,
    also provides transparency to the user for how their input is used
     */
    private void writeFileExternalStorage(String textToWrite) {
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
            if (voiceLog.createNewFile() || voiceLog.isFile()) {
                //Writing to file that stores a log of all commands issued to the assistant
                outputStream = new FileOutputStream(voiceLog, true);
                outputStream.write(textToWrite.getBytes());
                outputStream.write("\n".getBytes());
                outputStream.flush();
                outputStream.close();
            } else
                makeText(REFERENCE, "Couldn't write to voiceLog.txt", Toast.LENGTH_SHORT).show();

            if (lastCommand.createNewFile() || lastCommand.isFile()) {
                //Writing to file only the previous command issued to the assistant
                outputStream = new FileOutputStream(lastCommand, false);
                outputStream.write(textToWrite.getBytes());
                outputStream.flush();
                outputStream.close();
            } else
                makeText(REFERENCE, "Couldn't write to prevCommand.txt", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {
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
        recognizer.addKeyphraseSearch(KWS_SEARCH, chosenKeyPhrase);
    }

    public void startListening(boolean passiveListen) {
        //Ensures the recognizer is in a stopped/idle state before attempting to start
        recognizer.stop();

        passiveSpeech = passiveListen;

        if (passiveListen)
            recognizer.startListening(KWS_SEARCH);
        else if (!toggle)
            recognizer.startListening(USR_INPUT);
        else
            recognizer.startListening(FF_SPEECH);
    }

    public void toTextParser(String userCommand) {
        TextParser tp = new TextParser(userCommand, REFERENCE, toggle);
        MainActivity.textParser.setValue(tp);
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
                String keyPhrase = "";
                try {
                    FileInputStream input = new FileInputStream(REFERENCE.getExternalFilesDir(null) + "/activationPhrase.txt");

                    keyPhrase = new Scanner(input).nextLine();
                    Log.e("VoiceProcessing", "Read Phrase:" + keyPhrase + ":");

                } catch (FileNotFoundException e) {
                    Log.e("VoiceProcessing", "Could not read activationPhrase.txt");
                    e.printStackTrace();
                }

                if (keyPhrase == null || keyPhrase.equals("") || keyPhrase.equals(" "))
                    chosenKeyPhrase = DEFAULT_KEYPHRASE;
                else
                    chosenKeyPhrase = keyPhrase;

                setupRecognizer(assetDir);

            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Log.e("VoiceProcessing", "cmuSphinx failed to initialize recognizer module");
                makeText(REFERENCE, "Failed to init recognizer ", Toast.LENGTH_LONG).show();
                result.printStackTrace();
            }
            //Alerts the mainActivity that the recognizer has finished loading
            MainActivity.recogInit.setValue(true);
            Log.i("VoiceProcessing", "Activation phrase set to:" + chosenKeyPhrase);
            makeText(REFERENCE, "Recognizer ready to receive input", Toast.LENGTH_SHORT).show();
        }
    }
}
