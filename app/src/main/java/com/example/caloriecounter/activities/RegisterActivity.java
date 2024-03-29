package com.example.caloriecounter.activities;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.example.caloriecounter.R;
import com.example.caloriecounter.data.DataProvider;
import com.example.caloriecounter.model.InputValidation;
import com.example.caloriecounter.model.RegisterFormState;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout usernameLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout confirmPasswordLayout;
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
    private EditText email;
    private Button submitBtn;

    private MutableLiveData<RegisterFormState> formState;
    private InputValidation inputValidation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //get theme settings
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_theme = pref.getBoolean("dark_theme",true);
        if(!dark_theme) {
            setTheme(R.style.CustomLight);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        initViews();
        initObjects();
        initListeners();
    }

    private void initViews() {
        username = findViewById(R.id.username_register);
        password = findViewById(R.id.password_register);
        confirmPassword = findViewById(R.id.password_confirmation_register);
        email = findViewById(R.id.email_register);

        usernameLayout = findViewById(R.id.register_username_layout);
        passwordLayout = findViewById(R.id.register_password1_layout);
        confirmPasswordLayout = findViewById(R.id.register_password2_layout);
        emailLayout = findViewById(R.id.register_email_layout);

        submitBtn = findViewById(R.id.register_button);

    }

    private void initObjects() {
        inputValidation = new InputValidation(RegisterActivity.this);
        formState = new MutableLiveData<>();
    }

    private void initListeners() {
        submitBtn.setOnClickListener(this);

        formState.observe(this, registerFormState -> {
            if(registerFormState == null) {
                return;
            }
            submitBtn.setEnabled(registerFormState.isDataValid());
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
                //username
                if(!inputValidation.isUsernameValid(username.getText().toString())){
                    formState.setValue(new RegisterFormState(R.string.invalid_username,null,null));
                    usernameLayout.setError(getString(R.string.error_message_username));
                    valid = false;
                } else {
                    usernameLayout.setErrorEnabled(false);
                }
                //password
                if(!inputValidation.isPasswordValid(password.getText().toString())){
                    formState.setValue(new RegisterFormState(null, R.string.invalid_password, null));
                    passwordLayout.setError(getString(R.string.error_message_password));
                    valid = false;
                } else {
                    passwordLayout.setErrorEnabled(false);
                }
                //passwords match
                if(!inputValidation.doPasswordsMatch(password.getText().toString(),confirmPassword.getText().toString())){
                    formState.setValue(new RegisterFormState(null, R.string.invalid_password, null));
                    confirmPasswordLayout.setError(getString(R.string.error_message_confirm_password));
                    valid = false;
                } else {
                    confirmPasswordLayout.setErrorEnabled(false);
                }
                //email
                if(!inputValidation.isEmailValid(email.getText().toString())){
                    formState.setValue(new RegisterFormState(null, null, R.string.invalid_email));
                    emailLayout.setError(getString(R.string.error_message_email));
                    valid = false;
                } else {
                    emailLayout.setErrorEnabled(false);
                }
                if(valid){
                    formState.setValue(new RegisterFormState(true));
                }

            }
        };

        password.addTextChangedListener(afterTextChangedListener);
        confirmPassword.addTextChangedListener(afterTextChangedListener);
        email.addTextChangedListener(afterTextChangedListener);
        username.addTextChangedListener(afterTextChangedListener);
    }

    @Override
    public void onClick(View v) {
        insertIntoSQL();
    }

    private void insertIntoSQL() {

        boolean valid = true;

        Cursor c = getContentResolver().query(
                DataProvider.URI_USER,
                null,
                DataProvider.COLUMN_USER_EMAIL + " = ?",
                new String[]{email.getText().toString()},
                null
        );
        if(c.getCount() > 0){
            valid = false;
        }
        c.close();

        if(valid){

            ContentValues values = new ContentValues();
            values.put(DataProvider.COLUMN_USER_PASSWORD,password.getText().toString());
            values.put(DataProvider.COLUMN_USER_EMAIL,email.getText().toString());
            values.put(DataProvider.COLUMN_USER_USERNAME,username.getText().toString());

            getContentResolver().insert(DataProvider.URI_USER,values);

            Snackbar.make(findViewById(R.id.container), getString(R.string.account_made), Snackbar.LENGTH_LONG).show();

            finish();
        } else {
            Snackbar.make(findViewById(R.id.container), getString(R.string.error_email_exists), Snackbar.LENGTH_LONG).show();
        }
    }
}
