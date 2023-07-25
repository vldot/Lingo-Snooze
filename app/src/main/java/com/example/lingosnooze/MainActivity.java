package com.example.lingosnooze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import io.appwrite.Client;
import io.appwrite.models.DocumentList;
import io.appwrite.services.Account;
import io.appwrite.services.Databases;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import io.appwrite.exceptions.AppwriteException;
import io.appwrite.models.Document;
import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import android.util.Log;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Databases databases;

    private TextInputEditText wordEditText;
    private TextInputEditText meaningEditText;
    private TextInputEditText exampleEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Client client = new Client(getApplicationContext())
                .setEndpoint("https://cloud.appwrite.io/v1")
                .setProject("9867534210")
                .setSelfSigned(true);

        client.addHeader("X-Appwrite-API-Key", "==SECRET API KEY==");

        Account account = new Account(client);

        databases = new Databases(client);

        Button nextButton = findViewById(R.id.button);
        nextButton.setOnClickListener(v -> {

            Toast.makeText(MainActivity.this, "Getting a new word..", Toast.LENGTH_SHORT).show();
            try {
                fetchRandomDocument();
            } catch (AppwriteException e) {
                throw new RuntimeException(e);
            }
        });

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> {
            EditText wordEditText = findViewById(R.id.word_edit_text);
            EditText meaningEditText = findViewById(R.id.meaning_edit_text);
            EditText exampleEditText = findViewById(R.id.example_edit_text);

            String word = wordEditText.getText().toString();
            String meaning = meaningEditText.getText().toString();
            String example = exampleEditText.getText().toString();

            Toast.makeText(MainActivity.this, "Adding a new Word..", Toast.LENGTH_SHORT).show();
            try {
                addWordToDatabase(word, meaning, example);
            } catch (AppwriteException e) {
                throw new RuntimeException(e);
            }
        });

        // find the FloatingActionButton by its ID
        FloatingActionButton fab = findViewById(R.id.fab);

        // OnClickListener for the FloatingActionButton
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // PopupMenu object with the menu resource and the anchor view
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());

                // OnMenuItemClickListener for the PopupMenu object
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        if (id == R.id.profile) {
                            // open profile activity
                            return true;
                        } else if (id == R.id.settings) {
                            // open settings activity
                            return true;
                        } else if (id == R.id.favorites) {
                            // logout from the app
                            return true;
                        } else {
                            return false;
                        }
                    }
                });

                // show the PopupMenu object
                popup.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile) {
            // open profile activity
            return true;
        } else if (id == R.id.settings) {
            // open settings activity
            return true;
        } else if (id == R.id.favorites) {
            // logout from the app
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearTextFields() {
        // Clear the contents of the text fields here
        wordEditText.setText("");
        meaningEditText.setText("");
        exampleEditText.setText("");
    }

    private void fetchRandomDocument() throws AppwriteException {
        String databaseId = "1427538690";
        String collectionId = "14275386901";

        Log.d("FETCH_RANDOM_DOCUMENT", "Fetching random document from collection: " + collectionId);

        databases.listDocuments(
                databaseId,
                collectionId,
                new Continuation<DocumentList>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object o) {
                        try {
                            if (o instanceof Result.Failure) {
                                Result.Failure failure = (Result.Failure) o;
                                throw failure.exception;
                            } else {
                                DocumentList documentList = (DocumentList) o;
                                List<Document> documents = documentList.getDocuments();

                                if (documents.size() > 0) {
                                    // Select a random document from the list
                                    int randomIndex = new Random().nextInt(documents.size());
                                    Document document = documents.get(randomIndex);

                                    // Extract word, meaning, and example from the document
                                    String word = Objects.requireNonNull(document.getData().get("word")).toString();
                                    String meaning = Objects.requireNonNull(document.getData().get("meaning")).toString();
                                    String example = Objects.requireNonNull(document.getData().get("example")).toString();

                                    TextView textViewWord = findViewById(R.id.textViewWord);
                                    TextView textViewMeaning = findViewById(R.id.textViewMeaning);
                                    TextView textViewExample = findViewById(R.id.textViewExample);

                                    textViewWord.setText(word);
                                    textViewMeaning.setText(meaning);
                                    textViewExample.setText(example);
                                }
                            }
                        } catch (Throwable th) {
                            Log.e("ERROR", th.toString());
                        }
                    }
                }
        );
    }

    private void addWordToDatabase(String word, String meaning, String example) throws AppwriteException {
        try {
            JSONObject data = new JSONObject();
            data.put("word", word);
            data.put("meaning", meaning);
            data.put("example", example);

            String collectionId = "14275386901";
            databases.createDocument(
                    "1427538690",
                    collectionId,
                    "unique()",
                    data.toString(), // Convert the data JSON object to a string
                    null,
                    new Continuation<Document>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(Object o) {
                            try {
                                if (o instanceof Result.Failure) {
                                    Result.Failure failure = (Result.Failure) o;
                                    throw failure.exception;
                                } else {
                                    Document createdDocument = (Document) o;
                                    Log.d("ADD_WORD_SUCCESS", "Word added successfully!");
                                    clearTextFields();
                                }
                            } catch (Throwable th) {
                                Log.e("ERROR", th.toString());
                            }
                        }
                    }
            );
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }
    }
}
