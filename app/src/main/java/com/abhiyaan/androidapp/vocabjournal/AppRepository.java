package com.abhiyaan.androidapp.vocabjournal;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.abhiyaan.androidapp.vocabjournal.db.AppDatabase;
import com.abhiyaan.androidapp.vocabjournal.db.Sentence;
import com.abhiyaan.androidapp.vocabjournal.db.Word;
import com.abhiyaan.androidapp.vocabjournal.db.WordWithSentences;

import java.util.List;

import static com.abhiyaan.androidapp.vocabjournal.AppWebService.BAD_WORD;

/**
 * Created by Binaya Bhattarai on 3/27/2018.
 */

public class AppRepository {

    private AppWebService appWebService;
    private AppDatabase appDatabase;

    public AppRepository(Application application){
        appDatabase = AppDatabase.getInMemoryDatabase(application);
        appWebService = new AppWebService();
    }

    public LiveData<List<Word>> getWordsSortByRecent(){
        return appDatabase.wordDao().getWordsRecent();
    }

    public LiveData<List<Word>> getWordsSortByAlpha(){
        return appDatabase.wordDao().getWordsAlphabetical();
    }

    public LiveData<List<Word>> getWordsSortByAlphaDesc(){
        return appDatabase.wordDao().getWordsAlphabeticalDesc();
    }

    public LiveData<WordWithSentences> getWord(String wordTitle){
        if (!refreshWord(wordTitle))
            wordTitle = BAD_WORD;
        return appDatabase.wordDao().getWordWithSentences(wordTitle);
    }

    private boolean refreshWord(String wordTitle){
        String log;
        Word wordEntryInDb = appDatabase.wordDao().getWordSync(wordTitle);
        boolean returnBoolean = true;

        if (wordEntryInDb == null){
            log = "from the Web";
            wordEntryInDb = appWebService.getDefinition(wordTitle);

            //bad http response; return BAD_WORD definition from local db
            if (wordEntryInDb.getTitle().equals(BAD_WORD)) {
                returnBoolean = false;
            }
            else {
                //TODO optimize BAD_WORD retrieval
                //BAD_WORD is being added to db every time
                //add BAD_WORD entry to db one time upon creation and get rid of this else block
                appDatabase.wordDao().insert(wordEntryInDb);
            }
        }
        else{
            log = "from local DB";
        }
        Log.i("fetching Word Entry:", log);
        return returnBoolean;
    }

    public void createSentence(Sentence sentence) {
        appDatabase.sentenceDao().insert(sentence);
    }
}
