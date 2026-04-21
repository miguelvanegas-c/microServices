package eci.edu.co.monolito.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (!StringUtils.hasText(audience)) {
            // no audience configured -> accept
            return OAuth2TokenValidatorResult.success();
        }
        Object audClaim = token.getClaims().get("aud");
        if (audClaim == null) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Missing audience", null));
        }
        // aud may be a single string or list
        if (audClaim instanceof String) {
            if (audience.equals(audClaim)) {
                return OAuth2TokenValidatorResult.success();
            }
        } else if (audClaim instanceof java.util.Collection) {
            for (Object a : (java.util.Collection<?>) audClaim) {
                if (audience.equals(a)) {
                    return OAuth2TokenValidatorResult.success();
                }
            }
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
    }
}

