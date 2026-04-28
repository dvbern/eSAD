package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class BerechtigungTest {

    @Test
    void getRolleName_should_equal_Rolle() {
        Arrays.stream(Berechtigung.values()).forEach(
            rolle -> Assertions.assertThat(rolle.name()).as("Assigned BerechtigungName should equal Berechtigung.name() to avoid confusion")
                .isEqualTo(rolle.getBerechtigungName())
        );
    }

    @Test
    @Disabled("Only used to print all BerechtigungNames for FachRolle")
    void print_BerechtigungName_forFachrolle() {

        Arrays.stream(FachRolle.values()).forEach(
            fachRolle -> {
                System.out.println(String.format("================ %s ================", fachRolle.name()));
                FachRolleUtil.getBerechtigungen(TenantEnum.BS, fachRolle).forEach(
                    rolle -> {
                        Assertions.assertThat(rolle.getBerechtigungName()).isNotNull();
                        System.out.println(rolle.getBerechtigungName());
                    }
                );
            }
        );
    }
}
