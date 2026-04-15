package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.UntersuchungService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerMapper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
public class UntersuchungSignaturMapper {

    private final UntersuchungService untersuchungService;
    private final UntersuchungSignaturService untersuchungSignaturService;

    @Nullable
    public static UntersuchungSignaturJson toJson(@Nullable UntersuchungSignatur signatur) {
        if (signatur == null) {
            return null;
        }
        return new UntersuchungSignaturJson(
            signatur.getId(),
            EntityInfoJson.fromEntity(signatur),
            new UntersuchungSignaturSaveJson(
                BenutzerMapper.toOptionJson(signatur.getPerson()),
                signatur.getTimestampVisiert(),
                signatur.getVersion()
            )
        );
    }

    @Nullable
    public UntersuchungSignaturJson getByEinladung(@NonNull ID<Einladung> einladungId) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungId);
        return toJson(untersuchung.getUntersuchungSignatur());
    }

    @NonNull
    public UntersuchungSignaturJson signieren(@NonNull ID<Einladung> einladungID) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        return toJson(untersuchungSignaturService.signieren(untersuchung));
    }

    @NonNull
    public UntersuchungSignaturJson resetSignatur(@NonNull ID<Einladung> einladungID) {
        Untersuchung untersuchung = untersuchungService.getOrCreateUntersuchung(einladungID);
        return toJson(untersuchungSignaturService.resetSignatur(untersuchung));
    }
}
