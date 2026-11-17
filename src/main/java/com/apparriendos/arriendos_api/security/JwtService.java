package com.apparriendos.arriendos_api.security;

import com.apparriendos.arriendos_api.model.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ARRENDATARIO = "arrendatarioId";

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public JwtService(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    /** Token de acceso corto. El subject es el id del usuario. */
    public String createAccessToken(Usuario usuario) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(props.accessTtl()))
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .claim(CLAIM_ROLES, List.of(usuario.getRol().name()));
        if (usuario.getIdArrendatario() != null) {
            claims.claim(CLAIM_ARRENDATARIO, usuario.getIdArrendatario());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    public long accessTtlSeconds() {
        return props.accessTtl().toSeconds();
    }
}
