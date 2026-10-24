package com.nalr.appcrudusers.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public final class ErrorUtils {
    private static final Gson GSON = new Gson();

    private ErrorUtils() {
    }

    public static String apiMessage(Throwable error) {
        if (error instanceof HttpException) {
            HttpException httpError = (HttpException) error;
            Response<?> response = httpError.response();
            return errorMessage(response == null ? null : response.errorBody(), httpError.code());
        }
        return error instanceof IOException
                ? "Sin conexión. Comprueba internet e inténtalo de nuevo."
                : error.getMessage() == null ? "No se pudo completar la solicitud" : error.getMessage();
    }

    public static String apiMessage(Response<?> response) {
        return errorMessage(response.errorBody(), response.code());
    }

    private static String errorMessage(ResponseBody errorBody, int code) {
        if (errorBody != null) {
            try {
                Map<?, ?> body = GSON.fromJson(errorBody.string(), Map.class);
                Object message = body == null ? null : body.get("message");
                if (message != null && !message.toString().trim().isEmpty()) return message.toString();
            } catch (Exception ignored) {
            }
        }
        if (code == 401) return "Sesión expirada o credenciales inválidas.";
        if (code == 403) return "No tienes permisos para esta acción.";
        if (code == 404) return "Usuario no encontrado.";
        if (code == 409) return "Ya existe un usuario con ese dato.";
        return "Error del servidor (" + code + ").";
    }
}
