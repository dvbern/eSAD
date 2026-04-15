package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * FachRolle is the Customer Facing Name for a Role. The idea is to assign at least one FachRolle
 * to each user that will be interacting with ESC. Each FachRolle has a set of BerechtigungsRollen which are the
 * actual Roles that Quarkus will work with. So in Summary FachRolle is a way to group BerechtigungsRollen together
 * in a way that makes sense to the customer.
 * In case a user has multiple FachRollen, the user will have all the BerechtigungsRollen of all the FachRollen.
 */
@AllArgsConstructor
public enum FachRolle {

    EMPFANG,
    MPA,
    ORTHOPISTIN,
    SCHULAERZTIN,
    LAUSBEAUFTRAGTE,
    PFP,
    SCHOOL_NURSE,
    SUPERUSER,
    ADMIN,
    SUPPORT,
    SYSTEM_ADMIN;

    public static List<FachRolle> fachrollenHilfsperson() {
        return List.of(FachRolle.MPA, FachRolle.PFP, FachRolle.SCHOOL_NURSE);
    }

    @NonNull
    public Set<Berechtigung> getBerechtigungen(@NonNull TenantEnum tenant) {
        if (tenant == TenantEnum.BS) {
            return FachRolleBS.valueOf(this.name()).getBerechtigungen();
        }
        if (tenant == TenantEnum.WIN) {
            return FachRolleWIN.valueOf(this.name()).getBerechtigungen();
        }
        if (tenant == TenantEnum.BE) {
            return FachRolleBE.valueOf(this.name()).getBerechtigungen();
        }
        return EnumSet.noneOf(Berechtigung.class);
    }

    public boolean hasBerechtigung(@NonNull Berechtigung berechtigung, @NonNull TenantEnum tenant) {
        return getBerechtigungen(tenant).contains(berechtigung);
    }

    @NonNull
    public static Set<Berechtigung> collectAllBerechtigungen(
        @NonNull TenantEnum tenant,
        @NonNull FachRolle... fachRolle) {
        return Arrays.stream(fachRolle)
            .flatMap(f -> f.getBerechtigungen(tenant).stream())
            .collect(Collectors.toSet());
    }

    @NonNull
    public static List<FachRolle> getFachrollenWithBerechtigung(@NonNull TenantEnum tenant) {
        return Arrays.stream(FachRolle.values())
            .filter(fachRolle -> !fachRolle.getBerechtigungen(tenant).isEmpty())
            .toList();
    }

    @NonNull
    public static List<FachRolle> getSystemRollen() {
        return List.of(SUPPORT, SYSTEM_ADMIN);
    }

    @NonNull
    public static List<FachRolle> getOrganisationRollen() {
        return Arrays.stream(FachRolle.values())
            .filter(fr -> !getSystemRollen().contains(fr)).toList();
    }
}
