package com.cop4331.oneshot;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cop4331.networking.AccountManager;

public class NewGameActivity extends Activity {

    private EditText mPrompt    = null;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newgame_layout);

        Spinner spinner = (Spinner) findViewById(R.id.gameDurationSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gameduration_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        ((Button)findViewById(R.id.createGameButton)).setOnClickListener(mSignupListener);

        mPrompt = ((EditText)findViewById(R.id.promptText));
    }

    private final Button.OnClickListener mSignupListener = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            AccountManager manager = AccountManager.getInstance();
            String prompt   = mPrompt.getText().toString();
        }
    };

}

