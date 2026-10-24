package com.nalr.appcrudusers;

import com.nalr.appcrudusers.api.ApiClient;
import com.nalr.appcrudusers.model.User;
import com.nalr.appcrudusers.utils.ErrorUtils;
import com.nalr.appcrudusers.utils.SessionManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> formLauncher;

    private String token() {
        return "Bearer " + SessionManager.token(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        formLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) listUsers();
        });

        if (SessionManager.token(this) == null) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_main);
        findViewById(R.id.btnListUsers).setOnClickListener(view -> listUsers());
        findViewById(R.id.btnCreateUser).setOnClickListener(view -> openForm(null));
        findViewById(R.id.btnGetUserById).setOnClickListener(view -> askId(id ->
                ApiClient.getService().user(token(), id).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) showUser(response.body());
                        else showError(response);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable error) {
                        showError(error);
                    }
                })));
        findViewById(R.id.btnUpdateUser).setOnClickListener(view -> askId(id ->
                ApiClient.getService().user(token(), id).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) openForm(response.body());
                        else showError(response);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable error) {
                        showError(error);
                    }
                })));
        findViewById(R.id.btnDeleteUser).setOnClickListener(view -> askId(this::confirmDelete));
        findViewById(R.id.btnLogout).setOnClickListener(view -> {
            SessionManager.clear(this);
            openLogin();
        });
    }

    private void listUsers() {
        ApiClient.getService().users(token()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError(response);
                    return;
                }
                List<User> users = response.body();
                if (users.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No hay usuarios", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<String> labels = new ArrayList<>();
                for (User user : users) labels.add(user.name + " · " + user.email + " · " + user.id);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Usuarios")
                        .setItems(labels.toArray(new String[0]), (dialog, position) -> showUser(users.get(position)))
                        .setNegativeButton("Cerrar", null)
                        .show();
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable error) {
                showError(error);
            }
        });
    }

    private void showUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle(user.name)
                .setMessage("ID: " + user.id + "\nCorreo: " + user.email + "\nRol: " + user.role + "\nEstado: " + user.status)
                .setPositiveButton("Editar", (dialog, which) -> openForm(user))
                .setNeutralButton("Eliminar", (dialog, which) -> confirmDelete(user.id))
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void askId(Consumer<String> action) {
        EditText input = new EditText(this);
        input.setHint("ID del usuario");
        new AlertDialog.Builder(this)
                .setTitle("ID del usuario")
                .setView(input)
                .setPositiveButton("Continuar", (dialog, which) -> {
                    String id = input.getText().toString().trim();
                    if (id.isEmpty()) Toast.makeText(this, "Escribe un ID", Toast.LENGTH_SHORT).show();
                    else action.accept(id);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmDelete(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Eliminar el usuario " + id + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> ApiClient.getService().delete(token(), id).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) Toast.makeText(MainActivity.this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
                        else showError(response);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable error) {
                        showError(error);
                    }
                }))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void openForm(User user) {
        Intent intent = new Intent(this, RegisterActivity.class);
        if (user != null) {
            intent.putExtra("id", user.id);
            intent.putExtra("name", user.name);
            intent.putExtra("email", user.email);
            intent.putExtra("role", user.role);
            intent.putExtra("status", user.status);
        }
        formLauncher.launch(intent);
    }

    private void showError(Response<?> response) {
        Toast.makeText(this, ErrorUtils.apiMessage(response), Toast.LENGTH_LONG).show();
    }

    private void showError(Throwable error) {
        Toast.makeText(this, ErrorUtils.apiMessage(error), Toast.LENGTH_LONG).show();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
