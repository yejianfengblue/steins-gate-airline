package com.yejianfengblue.sga.apigateway.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class OidcController {

    @GetMapping("/oidc-info")
    public ResponseEntity<String> oidcInfo(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient oAuth2AuthorizedClient,
                                          @AuthenticationPrincipal OidcUser oidcUser,
                                          OAuth2AuthenticationToken oAuth2AuthenticationToken) {

        // OAuth2AuthorizedClient.principalName
        log.info("oAuth2AuthorizedClient.getPrincipalName() = {}", oAuth2AuthorizedClient.getPrincipalName());
        // OAuth2AuthorizedClient.accessToken
        OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
        Assert.isTrue(accessToken.getTokenType().equals(OAuth2AccessToken.TokenType.BEARER), "Access token type should be BEARER");
        log.info("accessToken = {}", accessToken.getTokenValue());
        log.info("accessToken.getIssuedAt() = {}", accessToken.getIssuedAt());
        log.info("accessToken.getExpiresAt() = {}", accessToken.getExpiresAt());
        log.info("accessToken.getScopes() = {}", accessToken.getScopes());
        // OAuth2AuthorizedClient.refreshToken
        OAuth2RefreshToken refreshToken = oAuth2AuthorizedClient.getRefreshToken();
        log.info("refreshToken = {}", refreshToken.getTokenValue());
        log.info("refreshToken.getIssuedAt() = {}", refreshToken.getIssuedAt());
        Assert.isNull(refreshToken.getExpiresAt(), "refreshToken should have no expiresAt");


        // OidcUser
        log.info("oAuth2User.getName() = {}", oidcUser.getName());
        log.info("oAuth2User.getAttributes() = {}", oidcUser.getAttributes());
        log.info("oAuth2User.getAuthorities() = {}", oidcUser.getAuthorities());

        // OAuth2AuthenticationToken
        Assert.isTrue(oAuth2AuthenticationToken.getPrincipal() == oidcUser, "Authenticated Principal should be an OidcUser");
        Assert.isTrue(oAuth2AuthenticationToken.getCredentials().equals(""), "Credentials are never exposed (by the Provider) for an OAuth2 User");
        Assert.isTrue(oAuth2AuthenticationToken.getName().equals(oAuth2AuthorizedClient.getPrincipalName()), "Principal name should be same");
        Assert.isTrue(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId().equals("okta"), "Authorized client registration ID should be okta");
        log.info("oAuth2AuthenticationToken.getAuthorities() = {}", oAuth2AuthenticationToken.getAuthorities());
//        Assert.isInstanceOf(WebAuthenticationDetails.class, oAuth2AuthenticationToken.getDetails(), "Authentication additional details should be a WebAuthenticationDetails");
        log.info("oAuth2AuthenticationToken.getDetails() = {}", oAuth2AuthenticationToken.getDetails());
        Assert.isTrue(oAuth2AuthenticationToken.isAuthenticated(), "isAuthenticated should be true");

        return ResponseEntity.ok(oidcUser.toString());
    }
}
