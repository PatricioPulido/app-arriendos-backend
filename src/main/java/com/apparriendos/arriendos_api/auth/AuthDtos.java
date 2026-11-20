package com.apparriendos.arriendos_api.auth;

import com.apparriendos.arriendos_api.model.Rol;
import com.apparriendos.arriendos_api.model.TipoCuenta;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record RegisterRequest(
            @NotBlank(message = "El nombre es obligatorio") @Size(max = 100) String nombre,
            @NotBlank(message = "El apellido es obligatorio") @Size(max = 100) String apellido,
            @NotBlank @Email(message = "Email inválido") String email,
            @Size(max = 30) String telefono,
            @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres") String password,
            /** HUESPED (por defecto) o ANFITRION. */
            TipoCuenta tipoCuenta) {}

    public record UsuarioDto(Long idUsuario, String email, Rol rol, Long idArrendatario) {}

    /** Respuesta de login/registro/refresh. El refresh token NO va aqui: va en una cookie HttpOnly. */
    public record AuthResponse(String accessToken, String tokenType, long expiresIn, UsuarioDto usuario) {}

    /** Resultado interno: lo que se entrega al cliente mas el refresh token para la cookie. */
    public record Tokens(AuthResponse body, String refreshToken) {}
}
