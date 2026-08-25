package ch.dvbern.oss.hemed.esc.api.features.document360.json;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 * Outbound body for {@code POST https://identity.document360.io/jwt/generateCode}.
 *
 * <p>Field names follow the Document360 JWT SSO contract (camelCase {@code userName}, not JavaBean
 * {@code username}). See
 * <a href="https://docs.document360.com/docs/configuring-the-jwt-sso">Configuring JWT SSO</a>.</p>
 */
@Value
public class Document360CodeRequestJson {
    @JsonProperty("userName")
    String username;
    String firstName;
    String lastName;
    @IgnoreForIdClassTest
    String emailId;
    List<String> readerGroupIds;
    Integer tokenValidity;
}
