package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import java.io.Serial;
import java.util.EnumSet;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.InformationEltern;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungImpfungenMassnahmenPresentIfReq;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
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
        @Index(name = "ix_untersuchungimpfungenmassnahmen_aufgabe_informationeltern",
            columnList = "aufgabeinformationeltern_id, id"),
        @Index(name = "ix_untersuchungimpfungenmassnahmen_aufgabe_informationhausarzt",
            columnList = "aufgabeinformationhausarzt_id, id"),
        @Index(name = "ix_untersuchungimpfungenmassnahmen_aufgabe_rueckfragehausarzt",
            columnList = "aufgaberueckfragehausarzt_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungImpfungenMassnahmenPresentIfReq
public class UntersuchungImpfungenMassnahmen extends AbstractEntity<UntersuchungImpfungenMassnahmen>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 7512251037224910832L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungImpfungenMassnahmen")
    private UntersuchungImpfungen untersuchungImpfungen;

    @NotNull
    @Column(nullable = false)
    private boolean keineMassnahmen = false;

    @NotNull
    @Column(nullable = false)
    private boolean vormerkenFuerGruppenFolgetermin = false;

    @NotNull
    @Column(nullable = false)
    private boolean impfberatungMpaDurchgefuehrt = false;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String impfberatungMpaDurchgefuehrtBemerkungen;

    @NotNull
    @Column(nullable = false)
    private boolean infomaterialAbgegeben = false;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private InformationEltern informationEltern;

    @NotNull
    @Column(nullable = false)
    private boolean informationHausarzt = false;

    @NotNull
    @Column(nullable = false)
    private boolean rueckfrageHausarzt = false;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String empfehlung;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String achtung;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bemerkungen;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungenmassnahmen_aufgabe_informationeltern"),
        nullable = true,
        updatable = true)
    private Aufgabe aufgabeInformationEltern;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungenmassnahmen_aufgabe_informationhausarzt"),
        nullable = true,
        updatable = true)
    private Aufgabe aufgabeInformationHausarzt;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungimpfungenmassnahmen_aufgabe_rueckfragehausarzt"),
        nullable = true,
        updatable = true)
    private Aufgabe aufgabeRueckfrageHausarzt;


    // Empfehlung
    public void setEmpfehlung(@Nullable EnumSet<ImpfungenMassnahmenEmpfehlung> empfehlungList) {
        this.empfehlung = EnumSetUtil.toString(empfehlungList);
    }

    @NonNull
    public EnumSet<ImpfungenMassnahmenEmpfehlung> getEmpfehlung() {
        return EnumSetUtil.fromString(ImpfungenMassnahmenEmpfehlung.class, empfehlung);
    }
}
