package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JsonWebTokenClaims {
    public static final String SESSION_ID_CLAIM = "sid";
    public static final String PREFERRED_USERNAME = "preferred_username";
    public static final String FAMILIY_NAME = "family_name";
    public static final String GIVEN_NAME = "given_name";
    public static final String EMAIL = "email";
    public static final String MOBILE_NUMMER = "MobileNummer";
    public static final String TITLE_CLAIM = "ESC-TITLE";
    public static final String PHONE_CLAIM = "ESC-PHONE";
    public static final String TENANT_CLAIM = "tenant";
    // client roles are nested in the token like this: 'resource_access.esc.roles'
    public static final String CLIENT_ROLE_CLAIM = "resource_access";
    public static final String CLIENT_ROLE_CLIENT_NAME_CLAIM = "esc";
    public static final String CLIENT_ROLE_ROLES_CLAIM = "roles";
    public static final String HIN_EAN_NO_MEDICAL = "X-HIN-EAN-NO-MEDICAL";
}
