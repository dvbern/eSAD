package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
@Slf4j
public final class DuplikatMapper {

    private final DuplikatService duplikatService;

    @NonNull
    public static DuplikatJson toJson(@NonNull Duplikat duplikat) {
        return new DuplikatJson(
            duplikat.getId(),
            EntityInfoJson.fromEntity(duplikat),
            toKindJson(ID.of(duplikat.getIdKind1(), Kind.class), duplikat.getKind1()),
            toKindJson(ID.of(duplikat.getIdKind2(), Kind.class), duplikat.getKind2()),
            duplikat.getDuplikatStatus()
        );
    }

    @NonNull
    public static DuplikatKindJson toKindJson(@NonNull ID<Kind> id, @Nullable Kind duplikat) {
        if (duplikat == null) {
            return new DuplikatKindJson(id, null, null, null, null, null, null, null, false, null, null);
        }

        return new DuplikatKindJson(
            id,
            duplikat.getName(),
            duplikat.getVorname(),
            duplikat.getGeschlecht(),
            duplikat.getGeburtsdatum(),
            duplikat.getTimestampErstellt().toLocalDateTime(),
            duplikat.getKindAktivStatus(),
            duplikat.getExternalImportInfo() != null ? duplikat.getExternalImportInfo().getTimestampLatestImport() : null,
            duplikat.getExternalImportInfo() != null,
            duplikat.getSchule() != null ? duplikat.getSchule().getSchulhaus() : null,
            duplikat.getKlasse() != null ? duplikat.getKlasse().getBezeichnung() : null
        );
    }

    @NonNull
    public List<DuplikatJson> findAll() {
        return duplikatService.findAll().stream()
            .map(DuplikatMapper::toJson)
            .toList();
    }

    @NonNull
    public DuplikatJson markAsFalsePositive(@NonNull ID<Duplikat> duplikatId) {
        Duplikat duplikat = duplikatService.ensureDuplikatAndMarkAsFalsePositive(duplikatId);
        return DuplikatMapper.toJson(duplikat);
    }

    @NonNull
    public DuplikatJson merge(@NonNull ID<Duplikat> duplikatId, @NonNull ID<Kind> basisKindId) {
        Duplikat duplikat = duplikatService.merge(duplikatId, basisKindId);
        return DuplikatMapper.toJson(duplikat);
    }
}
