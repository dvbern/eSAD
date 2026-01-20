package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Arrays;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FachRolleTest {

    @Test
    void collectAllBerechtigungsrollen_of_one_rolle_should_contain_berechtigungsrollen_of_that_rolle() {
        Set<Berechtigung> combinedRoles = FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.MPA);
        Arrays.stream(Berechtigung.values()).forEach(rolle -> {
            if (FachRolle.MPA.hasBerechtigung(rolle, TenantEnum.BS)) {
                Assertions.assertThat(combinedRoles).contains(rolle);
            } else {
                Assertions.assertThat(combinedRoles).doesNotContain(rolle);
            }
        });
    }

    @Test
    void collectAllBerechtigungsrollen_combination_of_berechtigung_of_fachrollen_works() {

        Set<Berechtigung> combinedRoles = FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.MPA, FachRolle.SUPERUSER);
        Arrays.stream(Berechtigung.values()).forEach(rolle -> {
            if (FachRolle.MPA.hasBerechtigung(rolle, TenantEnum.BS) || FachRolle.SUPERUSER.hasBerechtigung(rolle, TenantEnum.BS)) {
                Assertions.assertThat(combinedRoles).contains(rolle);
            } else {
                Assertions.assertThat(combinedRoles).doesNotContain(rolle);
            }
        });
    }
}
