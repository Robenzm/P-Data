package info.androidhive.sqlite.view;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import info.androidhive.sqlite.R;
import info.androidhive.sqlite.database.DatabaseHelper;
import info.androidhive.sqlite.database.model.Note;
import info.androidhive.sqlite.utils.MyDividerItemDecoration;
import info.androidhive.sqlite.utils.RecyclerTouchListener;

public class MainActivity extends AppCompatActivity {
    private NotesAdapter mAdapter;
    private final List<Note> notesList = new ArrayList<>();
    private TextView noNotesView;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);

        db = new DatabaseHelper(this);

        notesList.addAll(db.getAllNotes());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showNoteDialog(false, null, -1));

        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    @SuppressLint("NotifyDataSetChanged")
    private void createNote( String name, String sex, String age, String contact,String diagnosis,String hospital,String treatment) {
        // inserting note in db and getting
        // newly inserted note id
        long id = db.insertNote(hospital + "\n"+name + "\n" + sex + "-" + age + "-" + contact  +"\n" + diagnosis +"\n" + treatment);

        // get the newly inserted note from db
        Note n = db.getNote(id);

        if (n != null) {
            // adding new note to array list at 0 position
            notesList.add(0, n);

            // refreshing the list
            mAdapter.notifyDataSetChanged();

            toggleEmptyNotes();
        }
    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateNote(String note, int position) {
        Note n = notesList.get(position);
        // updating note text
        n.setNote(note);

        // updating note in db
        db.updateNote(n);

        // refreshing the list
        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteNote(int position) {
        // deleting the note from db
        db.deleteNote(notesList.get(position));

        // removing the note from the list
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence[] colors = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, (dialog, which) -> {
            if (which == 0) {
                showNoteDialog(true, notesList.get(position), position);
            } else {
                deleteNote(position);
            }
        });
        builder.show();
    }


    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog alertDialogBuilderUserInput = new MaterialAlertDialogBuilder(MainActivity.this).create();
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        final EditText name = view.findViewById(R.id.name);
        final EditText age = view.findViewById(R.id.age);
        final EditText sex = view.findViewById(R.id.sex);
        final EditText contact = view.findViewById(R.id.conact);
        final EditText hospital = view.findViewById(R.id.hospital);
        final EditText diagnosis = view.findViewById(R.id.diagnosis);
        final EditText treatment = view.findViewById(R.id.treatment);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false);
        alertDialogBuilderUserInput.setButton(BUTTON_POSITIVE
                , (shouldUpdate ? "update" : "save"), (dialogBox, id) -> {

                });
        alertDialogBuilderUserInput.setButton(BUTTON_NEGATIVE, "cancel",
                (dialogBox, id) -> dialogBox.cancel());
        alertDialogBuilderUserInput.show();

        alertDialogBuilderUserInput.getButton(BUTTON_POSITIVE).setOnClickListener(v -> {
            // Show toast message when no text is entered
            if (TextUtils.isEmpty(inputNote.getText().toString())) {
                Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                alertDialogBuilderUserInput.dismiss();
            }

            // check if user updating note
            if (shouldUpdate && note != null) {
                // update note by it's id
                updateNote(inputNote.getText().toString(), position);
            } else {
                // create new note
                createNote( name.getEditableText().toString(), sex.getEditableText().toString(), age.getEditableText().toString(), contact.getEditableText().toString(), diagnosis.getEditableText().toString(), hospital.getEditableText().toString(), treatment.getEditableText().toString());
            }
        });
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (db.getNotesCount() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }
}
