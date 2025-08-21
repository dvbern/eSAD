package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.infomaterial.Infomaterial;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
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
@Table(
    indexes = {
        @Index(name = "ix_untersuchungmassnahmen_aufgabe_informationeltern", columnList = "aufgabeinformationeltern_id, id"),
        @Index(name = "ix_untersuchungmassnahmen_aufgabe_ueberweisunghausarzt", columnList = "aufgabeueberweisunghausarzt_id, id"),
        @Index(name = "ix_untersuchungmassnahmen_aufgabe_ueberweisungaugenarzt", columnList = "aufgabeueberweisungaugenarzt_id, id"),
        @Index(name = "ix_untersuchungmassnahmen_aufgabe_nachkontrolle", columnList = "aufgabenachkontrolle_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UntersuchungMassnahmen extends AbstractTenantEntity<UntersuchungMassnahmen> {

    @Serial
    private static final long serialVersionUID = 7512251037224910832L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungMassnahmen")
    private Untersuchung untersuchung;

    @NotNull
    @Column(nullable = false)
    private boolean keineMassnahmen = false;

    @NotNull
    @Column(nullable = false)
    private boolean beratungMPA = false;

    @NotNull
    @Column(nullable = false)
    private boolean beratungSchulAerztin = false;

    @NotNull
    @Column(nullable = false)
    private boolean anderes = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String anderesBemerkung;

    @NotNull
    @Column(nullable = false)
    private boolean bereitsBehandeltSehen = false;

    @NotNull
    @Column(nullable = false)
    private boolean bereitsBehandeltHoeren = false;

    @NotNull
    @Column(nullable = false)
    private boolean bereitsBehandeltGroesseGewicht = false;

    @NotNull
    @Column(nullable = false)
    private boolean bereitsBehandeltSprache = false;

    @NotNull
    @Column(nullable = false)
    private boolean bereitsBehandeltMotorik = false;

    @NotNull
    @Column(nullable = false)
    private boolean bereitsBehandeltAnderes = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String bereitsBehandeltAnderesBemerkung;

    @NotNull
    @NonNull
    @ManyToMany
    @JoinTable(name = "UntersuchungMassnahmen_Infomaterial",
        joinColumns = @JoinColumn(
            name = "untersuchungmassnahmen_id",
            foreignKey = @ForeignKey(name = "fk_untersuchungmassnahmen_infomaterial_untersuchungmassnahmen")),
        inverseJoinColumns = @JoinColumn(
            name = "informaterial_id",
            foreignKey = @ForeignKey(name = "fk_untersuchungmassnahmen_infomaterial_infomaterial")),
        indexes = {
            @Index(name = "ix_untersuchungmassnahmen_infomaterial_untersuchungmassnahmen",
                columnList = "untersuchungmassnahmen_id, informaterial_id"),
            @Index(name = "ix_untersuchungmassnahmen_infomaterial_infomaterial",
                columnList = "informaterial_id, untersuchungMassnahmen_id"),
        }
    )
    private Set<Infomaterial> abgegebenesInfomaterial = new HashSet<>();

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private InformationEltern informationEltern;

    @NotNull
    @Column(nullable = false)
    private boolean ueberweisungHausarzt = false;

    @NotNull
    @Column(nullable = false)
    private boolean ueberweisungAugenarzt = false;

    @Valid
    @Embedded
    @NonNull
    @NotNull
    private AnlassFlags nachkontrolle = new AnlassFlags();

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH)
    private String bemerkungen;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungmassnahmen_aufgabe_informationeltern"), nullable = true, updatable = true)
    private Aufgabe aufgabeInformationEltern;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungmassnahmen_aufgabe_ueberweisunghausarzt"), nullable = true, updatable = true)
    private Aufgabe aufgabeUeberweisungHausarzt;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungmassnahmen_aufgabe_ueberweisungaugenarzt"), nullable = true, updatable = true)
    private Aufgabe aufgabeUeberweisungAugenarzt;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungmassnahmen_aufgabe_nachkontrolle"), nullable = true, updatable = true)
    private Aufgabe aufgabeNachkontrolle;

    @NonNull
    public static UntersuchungMassnahmen forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungMassnahmen massnahmen = new UntersuchungMassnahmen();
        massnahmen.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungMassnahmen(massnahmen);
        return massnahmen;
    }

    @NonNull
    public static UntersuchungMassnahmen forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungMassnahmen massnahmen = untersuchung.getUntersuchungMassnahmen();
        if (massnahmen == null) {
            massnahmen = forUntersuchung(untersuchung);
        }
        return massnahmen;
    }
}
