package ch.dvbern.oss.hemed.esc.api.features.document360.json;

import lombok.Data;

/**
 * Response from Document360 Identity {@code /jwt/generateCode}.
 *
 * <p>{@code code} is a one-time SSO ticket appended to the knowledge-base login URL.
 * It is not a JWT and must not be persisted.</p>
 */
@Data
public class Document360CodeResponseJson {
    private String code;
    private String message;
    private Boolean success;
}
