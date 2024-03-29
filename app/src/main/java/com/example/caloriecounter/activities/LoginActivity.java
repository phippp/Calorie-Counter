package com.example.caloriecounter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.InputValidation;
import com.example.caloriecounter.model.LoginFormState;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private EditText username;
    private EditText password;
    private Button submitBtn;
    private TextView registerLink;

    private MutableLiveData<LoginFormState> formState;
    private InputValidation inputValidation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //get preference and set theme before view is set
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = pref.getBoolean("dark_theme",true);
        if(!dark_theme) {
            setTheme(R.style.CustomLight);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        checkPreferences();
        initViews();
        initObjects();
        initListeners();
    }

    private void checkPreferences() {
        //if the user has logged in without logging out then auto log in
        SharedPreferences loggedInUser = getSharedPreferences("LoggedInUser",MODE_PRIVATE);
        String sharedUsername = loggedInUser.getString("username",null);
        String sharedPassword = loggedInUser.getString("password", null);
        if(sharedPassword != null && sharedUsername != null){
            Intent intent = new Intent(LoginActivity.this,MyApp.class);
            startActivity(intent);
            finish();
        }
    }

    private void initViews() {
        username = findViewById(R.id.username_login);
        usernameLayout = findViewById(R.id.username_layout);

        password = findViewById(R.id.password_login);
        passwordLayout = findViewById(R.id.password_layout);

        submitBtn = findViewById(R.id.login_button);

        registerLink = findViewById(R.id.register_link);
    }

    private void initObjects() {
        inputValidation = new InputValidation(LoginActivity.this);
        formState = new MutableLiveData<>();
    }

    private void initListeners() {
        submitBtn.setOnClickListener(this);
        registerLink.setOnClickListener(this);

        formState.observe(this, loginFormState -> {
            if(loginFormState == null) {
                return;
            }
            submitBtn.setEnabled(loginFormState.isDataValid());
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean valid = true;
                if(!inputValidation.isUsernameValid(username.getText().toString())){
                    formState.setValue(new LoginFormState(R.string.invalid_username,null));
                    usernameLayout.setError(getString(R.string.error_message_username));
                    valid = false;
                } else {
                    usernameLayout.setErrorEnabled(false);
                }
                if(!inputValidation.isPasswordValid(password.getText().toString())){
                    formState.setValue(new LoginFormState(null, R.string.invalid_password));
                    passwordLayout.setError(getString(R.string.error_message_password));
                    valid = false;
                } else {
                    passwordLayout.setErrorEnabled(false);
                }
                if(valid){
                    formState.setValue(new LoginFormState(true));
                }
            }
        };

        password.addTextChangedListener(afterTextChangedListener);
        username.addTextChangedListener(afterTextChangedListener);
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.register_link){
            //move to register page
            Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
            startActivity(intent);
        } else if(v.getId() == R.id.login_button){
            //check login
            verifyFromSQL();
        }
    }

    private void verifyFromSQL() {
        boolean valid = false;
        //checks if user exists
        Cursor c = getContentResolver().query(
                DataProvider.URI_USER,
                null,
                DataProvider.COLUMN_USER_PASSWORD + " = ?" + " AND " + DataProvider.COLUMN_USER_USERNAME + " = ?",
                new String[]{password.getText().toString(),username.getText().toString()},
                null
        );
        if(c.getCount() > 0){
            valid = true;
        }
        c.close();
        if(valid){
            //create shared preferences to allow quick login
            SharedPreferences loggedInUser = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
            SharedPreferences.Editor editor = loggedInUser.edit();
            editor.putString("username",username.getText().toString());
            editor.putString("password",password.getText().toString());
            editor.apply();
            //start mainActivity
            Intent intent = new Intent(LoginActivity.this,MyApp.class);
            startActivity(intent);
            finish();
        } else {
            Snackbar.make(findViewById(R.id.container), getString(R.string.error_invalid_email_password), Snackbar.LENGTH_LONG).show();
        }
    }
}