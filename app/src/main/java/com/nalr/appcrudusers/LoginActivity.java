package com.nalr.appcrudusers;

import com.nalr.appcrudusers.api.ApiClient;
import com.nalr.appcrudusers.model.LoginRequest;
import com.nalr.appcrudusers.model.LoginResponse;
import com.nalr.appcrudusers.utils.ErrorUtils;
import com.nalr.appcrudusers.utils.SessionManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SessionManager.token(this) != null) {
            openUsers();
            return;
        }

        setContentView(R.layout.activity_login);
        EditText email = findViewById(R.id.etEmailLogin);
        EditText password = findViewById(R.id.etPasswordLogin);
        Button submit = findViewById(R.id.btnLoginSubmit);

        submit.setOnClickListener(view -> {
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString();
            if (mail.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Escribe tu correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            submit.setEnabled(false);
            ApiClient.getService().login(new LoginRequest(mail, pass)).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().accessToken != null) {
                        SessionManager.save(LoginActivity.this, response.body().accessToken);
                        openUsers();
                    } else {
                        Toast.makeText(LoginActivity.this, ErrorUtils.apiMessage(response), Toast.LENGTH_LONG).show();
                        submit.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable error) {
                    Toast.makeText(LoginActivity.this, ErrorUtils.apiMessage(error), Toast.LENGTH_LONG).show();
                    submit.setEnabled(true);
                }
            });
        });

        findViewById(R.id.btnGoToRegister).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class).putExtra("publicRegistration", true)));
    }

    private void openUsers() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
