package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.PrincipalJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.TestingResponseJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@JaxMapper
@RequiredArgsConstructor
public class BenutzerMapper {

    private final BenutzerService benutzerService;
    private final AppClock appClock;

    @Nullable
    public static BenutzerOptionJson toOptionJson(@Nullable Benutzer benutzer) {
        if (benutzer == null) {
            return null;
        }
        return new BenutzerOptionJson(
            benutzer.getId(),
            benutzer.getNachname(),
            benutzer.getVorname(),
            benutzer.getVorname() + ' ' + benutzer.getNachname(),
            benutzer.getFachRollen(),
            benutzer.isAktiv()
        );
    }

    @Nullable
    public Benutzer toEntity(@Nullable BenutzerOptionJson benutzerOptionJson) {
        if (benutzerOptionJson == null) {
            return null;
        }

        return this.benutzerService.getById(benutzerOptionJson.getId());
    }

    @NonNull
    private Benutzer toEntity(
        @NonNull Benutzer existingBenutzer,
        @NonNull BenutzerSaveJson saveJson
    ) {
        existingBenutzer.setNachname(saveJson.getName());
        existingBenutzer.setVorname(saveJson.getVorname());
        existingBenutzer.setEmail(saveJson.getEmail());
        existingBenutzer.setTitel(saveJson.getTitel());
        existingBenutzer.setTelefon(saveJson.getTelefon());
        // ensure we only set the timestampManuallyActivated if it's being activated
        if (!existingBenutzer.isAktiv() && saveJson.isAktiv()) {
            existingBenutzer.setTimestampLastManuallyActivated(appClock.offsetDateTime().now());
        }
        existingBenutzer.setAktiv(saveJson.isAktiv());
        existingBenutzer.setFachRollen(saveJson.getFachRollen());

        return existingBenutzer;
    }

    @Nullable
    public static BenutzerJson toJson(@Nullable Benutzer benutzer) {
        if (benutzer == null) {
            return null;
        }
        return new BenutzerJson(
            benutzer.getId(),
            EntityInfoJson.fromEntity(benutzer),
            new BenutzerSaveJson(
                benutzer.getTitel(),
                benutzer.getNachname(),
                benutzer.getVorname(),
                benutzer.getVorname() + ' ' + benutzer.getNachname(),
                benutzer.getEmail().toString(),
                benutzer.getEmail(),
                benutzer.getTelefon(),
                benutzer.isAktiv(),
                benutzer.getFachRollen()
            )
        );
    }

    @NonNull
    public static PrincipalJson toPrincipalJson(@NonNull Benutzer benutzer) {
        return new PrincipalJson(
            toJson(benutzer),
            benutzer.getBerechtigungen().stream().map(BenutzerBerechtigung::getBerechtigung).toList()
        );
    }

    @NonNull
    public TestingResponseJson mappingErrorExample() {
        var id = ID.random(Benutzer.class);
        throw new AppFailureException(AppFailureMessage.entityNotFound(id));
    }

    @NonNull
    public List<BenutzerOptionJson> findAllActiveBenutzerOptions() {
        return this.benutzerService.findAllActiveBenutzer().stream()
            .map(BenutzerMapper::toOptionJson)
            .toList();
    }

    @NonNull
    public List<BenutzerJson> findAllBenutzer() {
        return this.benutzerService.findAllBenutzer().stream()
            .map(BenutzerMapper::toJson)
            .toList();
    }

    @NonNull
    public BenutzerJson update(
        @NonNull ID<Benutzer> benutzerID,
        @NonNull BenutzerSaveJson benutzerJson
    ) {
        Benutzer existingBenutzer = this.benutzerService.getById(benutzerID);
        Benutzer entity = toEntity(existingBenutzer, benutzerJson);
        return toJson(this.benutzerService.persist(entity));
    }
}
