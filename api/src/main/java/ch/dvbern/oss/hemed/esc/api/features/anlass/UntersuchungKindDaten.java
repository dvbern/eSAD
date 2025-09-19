package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;
import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UntersuchungKindDaten extends AbstractTenantEntity<UntersuchungKindDaten>
        implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 202768990618477062L;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String name;

    @NotNull
    @NonNull
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = false)
    private String vorname;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate geburtsdatum;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Land nationalitaet;

    @Nullable
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Column(nullable = true)
    private String schulhaus;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String schulstandort;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String quartier;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String schulkreis;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String schulhausnummer;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    private Klassenstufe klassenstufe;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    private String klassentyp;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String klasse;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_UUID_LENGTH)
    @IgnoreForIdClassTest
    private String klasseId;

    @NonNull
    public static UntersuchungKindDaten ofKind(@NonNull Kind kind) {
        Klasse klasse = kind.getKlasse();
        Schule schule = kind.extractSchule();
        return new UntersuchungKindDaten(
            kind.getName(),
            kind.getVorname(),
            kind.getGeschlecht(),
            kind.getGeburtsdatum(),
            kind.getNationalitaet(),
            schule != null ? schule.getSchulhaus() : null,
            schule != null ? schule.getSchulstandort() : null,
            schule != null && schule.getQuartier() != null ? schule.getQuartier().getQuartierName() : null,
            schule != null && schule.getSchulkreis() != null ? schule.getSchulkreis() : null,
            schule != null && schule.getSchulhausnummer() != null ? schule.getSchulhausnummer() : null,
            klasse != null ? klasse.getKlassenstufe() : null,
            klasse != null ? klasse.getKlasseSchulart() : null,
            klasse != null ? klasse.getBezeichnung() : null,
            klasse != null ? klasse.getId().getId() : null
        );
    }
}
