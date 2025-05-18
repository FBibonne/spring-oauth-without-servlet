package poc.java.oauth.authorizationservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.function.Consumer;

record NativeAppOAuth2AuthorizationRequestResolver(
        Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer) {

    private static final Logger log = LoggerFactory.getLogger(NativeAppOAuth2AuthorizationRequestResolver.class);

    private static final Consumer<OAuth2AuthorizationRequest.Builder> DEFAULT_PKCE_APPLIER = OAuth2AuthorizationRequestCustomizers
            .withPkce();
    private static final StringKeyGenerator DEFAULT_SECURE_KEY_GENERATOR = new Base64StringKeyGenerator(
            Base64.getUrlEncoder().withoutPadding(), 96);
    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(
            Base64.getUrlEncoder());

    public NativeAppOAuth2AuthorizationRequestResolver {
        if (authorizationRequestCustomizer == null) {
            authorizationRequestCustomizer = _ -> {
            };
        }
    }

    public NativeAppOAuth2AuthorizationRequestResolver() {
        this(null);
    }


    public OAuth2AuthorizationRequest resolve(ClientRegistration clientRegistration) {
        // org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver#resolve(HttpServletRequest, java.lang.String, java.lang.String)

        log.debug("Building OAuth2AuthorizationRequest from client registration");
        OAuth2AuthorizationRequest.Builder builder = getBuilder(clientRegistration);

        String redirectUriStr = expandRedirectUri(clientRegistration);

        // @formatter:off
        builder.clientId(clientRegistration.getClientId())
                .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
                .redirectUri(redirectUriStr)
                .scopes(clientRegistration.getScopes())
                .state(DEFAULT_STATE_GENERATOR.generateKey());
        // @formatter:on

        this.authorizationRequestCustomizer.accept(builder);

        return builder.build();
    }

    private String expandRedirectUri(ClientRegistration clientRegistration) {
        // In a native app context, redirect uri is not intended to have placeholders : only encode illegal characters
        return UriComponentsBuilder.fromUriString(clientRegistration.getRedirectUri())
                .toUriString();
    }


    //org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver.getBuilder
    private OAuth2AuthorizationRequest.Builder getBuilder(ClientRegistration clientRegistration) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(clientRegistration.getAuthorizationGrantType())) {
            // @formatter:off
            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .attributes(attrs ->
                            attrs.put(OAuth2ParameterNames.REGISTRATION_ID, clientRegistration.getRegistrationId()));
            // @formatter:on
            if (!CollectionUtils.isEmpty(clientRegistration.getScopes())
                    && clientRegistration.getScopes().contains(OidcScopes.OPENID)) {
                // Section 3.1.2.1 Authentication Request -
                // https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest scope
                // REQUIRED. OpenID Connect requests MUST contain the "openid" scope
                // value.
                applyNonce(builder);
            }
            if (ClientAuthenticationMethod.NONE.equals(clientRegistration.getClientAuthenticationMethod())) {
                DEFAULT_PKCE_APPLIER.accept(builder);
            }
            return builder;
        }
        throw new IllegalArgumentException(
                "Invalid Authorization Grant Type (" + clientRegistration.getAuthorizationGrantType().getValue()
                        + ") for Client Registration with Id: " + clientRegistration.getRegistrationId());
    }

    /**
     * org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver#applyNonce(org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.Builder)
     * <p>
     * Creates nonce and its hash for use in OpenID Connect 1.0 Authentication Requests.
     *
     * @param builder where the {@link OidcParameterNames#NONCE} and hash is stored for
     *                the authentication request
     * @see <a target="_blank" href=
     * "https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest">3.1.2.1.
     * Authentication Request</a>
     * @since 5.2
     */
    private static void applyNonce(OAuth2AuthorizationRequest.Builder builder) {
        try {
            String nonce = DEFAULT_SECURE_KEY_GENERATOR.generateKey();
            String nonceHash = createHash(nonce);
            builder.attributes(attrs -> attrs.put(OidcParameterNames.NONCE, nonce));
            builder.additionalParameters(params -> params.put(OidcParameterNames.NONCE, nonceHash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver.createHash
    private static String createHash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

}
