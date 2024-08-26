package com.fluxit.architecture.reference.authenticated.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class JwtConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    @Value("${authentication.validRoles:admin,user}")
    private String authenticationValidRoles;

    private Set<String> validRoles;

    @PostConstruct
    private void initConverter() {
        validRoles = Arrays.stream(authenticationValidRoles.split(",")).collect(Collectors.toSet());
    }

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter
            = new JwtGrantedAuthoritiesConverter();

    private final JwtConverterProperties properties;

    public JwtConverter(final JwtConverterProperties theProperties) {
        this.properties = theProperties;
    }

    @Override
    public final AbstractAuthenticationToken convert(final Jwt jwt) {
        Collection<GrantedAuthority> authorities =
                Stream.of(jwtGrantedAuthoritiesConverter.convert(jwt),
                        extractResourceRoles(jwt),
                        extractResourceRealmAccess(jwt))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities,
                getPrincipalClaimName(jwt));
    }

    private Collection<? extends GrantedAuthority> extractResourceRealmAccess(
            final Jwt jwt) {
        if (jwt.getClaim("realm_access") instanceof Map<?, ?> realmAccess
            && realmAccess.get("roles") instanceof Collection<?> roles
        ) {
            return roles.stream()
                    .filter(validRoles::contains)
                    .map(
                    role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                    .toList();
        }

        return Set.of();
    }

    private String getPrincipalClaimName(final Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (properties.getPrincipalAttribute() != null) {
            claimName = properties.getPrincipalAttribute();
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(
            final Jwt jwt) {
        if (jwt.getClaim("resource_access") instanceof Map<?, ?> resAccess
                && resAccess.get(properties.getResourceId())
                instanceof Map<?, ?> resource
                && resource.get("roles") instanceof Collection<?> resourceRoles) {
            return resourceRoles.stream()
                    .filter(validRoles::contains)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }
}
