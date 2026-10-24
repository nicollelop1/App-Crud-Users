package com.nalr.appcrudusers;

import com.nalr.appcrudusers.api.ApiClient;
import com.nalr.appcrudusers.model.CreateUser;
import com.nalr.appcrudusers.model.UpdateUser;
import com.nalr.appcrudusers.model.User;
import com.nalr.appcrudusers.utils.ErrorUtils;
import com.nalr.appcrudusers.utils.SessionManager;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText id = findViewById(R.id.etIdRegister);
        EditText name = findViewById(R.id.etNameRegister);
        EditText email = findViewById(R.id.etEmailRegister);
        EditText password = findViewById(R.id.etPasswordRegister);
        EditText role = findViewById(R.id.etRoleRegister);
        EditText status = findViewById(R.id.etStatusRegister);
        TextView title = findViewById(R.id.tvRegisterTitle);
        boolean publicRegistration = getIntent().getBooleanExtra("publicRegistration", false);
        boolean editing = getIntent().hasExtra("name");

        String currentId = getIntent().getStringExtra("id");
        if (currentId != null) id.setText(currentId);

        if (editing) {
            title.setText("Editar usuario");
            name.setText(getIntent().getStringExtra("name"));
            email.setText(getIntent().getStringExtra("email"));
            role.setText(getIntent().getStringExtra("role"));
            status.setText(getIntent().getStringExtra("status"));
            id.setEnabled(false);
            password.setHint("Nueva contraseña (opcional)");
        } else {
            title.setText(publicRegistration ? "Crear cuenta" : "Crear usuario");
            role.setText("MEMBER");
            status.setText("PENDING");
            findViewById(R.id.tvIdLabel).setVisibility(View.GONE);
            id.setVisibility(View.GONE);
        }

        if (publicRegistration) {
            findViewById(R.id.tvIdLabel).setVisibility(View.GONE);
            id.setVisibility(View.GONE);
            findViewById(R.id.tvRoleLabel).setVisibility(View.GONE);
            role.setVisibility(View.GONE);
            findViewById(R.id.tvStatusLabel).setVisibility(View.GONE);
            status.setVisibility(View.GONE);
        }

        findViewById(R.id.btnRegisterSubmit).setOnClickListener(view -> {
            String userId = editing ? id.getText().toString().trim() : UUID.randomUUID().toString();
            String userName = name.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString();
            String userRole = publicRegistration ? "MEMBER" : role.getText().toString().trim().toUpperCase(Locale.ROOT);
            String userStatus = status.getText().toString().trim().toUpperCase(Locale.ROOT);

            boolean valid = userName.length() >= 3
                    && Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()
                    && (editing && userPassword.isEmpty() || userPassword.length() >= 8)
                    && (publicRegistration || Arrays.asList("ADMIN", "MEMBER", "REVIEWER").contains(userRole))
                    && (!editing || Arrays.asList("ACTIVE", "INACTIVE", "PENDING", "BLOCKED").contains(userStatus));

            if (!valid) {
                Toast.makeText(this, "Revisa los datos y la contraseña (mínimo 8 caracteres)", Toast.LENGTH_LONG).show();
                return;
            }

            Button submit = findViewById(R.id.btnRegisterSubmit);
            submit.setEnabled(false);
            CreateUser createRequest = new CreateUser(userId, userName, userEmail, userPassword, userRole);
            Call<User> request;
            if (editing) {
                UpdateUser updateRequest = new UpdateUser(
                        userName,
                        userEmail,
                        userPassword.isEmpty() ? null : userPassword,
                        userRole,
                        userStatus);
                request = ApiClient.getService().update("Bearer " + SessionManager.token(this), userId, updateRequest);
            } else if (publicRegistration) {
                request = ApiClient.getService().create(createRequest);
            } else {
                request = ApiClient.getService().createAuthorized("Bearer " + SessionManager.token(this), createRequest);
            }

            request.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        String message = publicRegistration ? "Cuenta creada. Inicia sesión." : "Usuario guardado";
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, ErrorUtils.apiMessage(response), Toast.LENGTH_LONG).show();
                        submit.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable error) {
                    Toast.makeText(RegisterActivity.this, ErrorUtils.apiMessage(error), Toast.LENGTH_LONG).show();
                    submit.setEnabled(true);
                }
            });
        });

        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());
    }
}
