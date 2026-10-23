package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSignaturMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungSignaturService untersuchungSignaturService;
    private final TenantContext tenantContext;

    @Nullable
    public static UntersuchungSignaturJson toJson(@Nullable UntersuchungSignatur signatur, @NonNull TenantEnum tenant) {
        if (signatur == null) {
            return null;
        }
        return new UntersuchungSignaturJson(
            signatur.getId(),
            EntityInfoJson.fromEntity(signatur),
            new UntersuchungSignaturSaveJson(
                BenutzerMapper.toOptionJson(signatur.getPerson(), tenant),
                signatur.getTimestampVisiert(),
                signatur.getVersion()
            )
        );
    }

    @Nullable
    public UntersuchungSignaturJson getByEinladung(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchung.getUntersuchungSignatur(), tenantContext.get());
    }

    @NonNull
    public UntersuchungSignaturJson signieren(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchungSignaturService.signieren(untersuchung), tenantContext.get());
    }

    @NonNull
    public UntersuchungSignaturJson resetSignatur(
        @NonNull HasUntersuchungIDWrapper wrappedId,
        @NonNull Consumer<Untersuchung> hasAuthorizationUntersuchung
    ) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(wrappedId);
        hasAuthorizationUntersuchung.accept(untersuchung);
        return toJson(untersuchungSignaturService.resetSignatur(untersuchung), tenantContext.get());
    }
}
