package ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.UntersuchungAbschluss;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBemerkungFragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.UntersuchungSignatur;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.UntersuchungVorabklaerungen;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
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
        @Index(name = "ix_untersuchung_untersuchungkinddaten", columnList = "untersuchungkinddaten_id, id"),
        @Index(name = "ix_untersuchung_untersuchungdurchfuehrung", columnList = "untersuchungdurchfuehrung_id, id"),
        @Index(name = "ix_untersuchung_untersuchungvorabklaerungen", columnList = "untersuchungvorabklaerungen_id, id"),
        @Index(name = "ix_untersuchung_untersuchungtriage", columnList = "untersuchungtriage_id, id"),
        @Index(name = "ix_untersuchung_untersuchungimpfungen", columnList = "untersuchungimpfungen_id, id"),
        @Index(name = "ix_untersuchung_untersuchungsehenbeurteilung", columnList = "untersuchungsehenbeurteilung_id, id"),
        @Index(name = "ix_untersuchung_untersuchungsehenbemerkungfragebogen", columnList = "untersuchungsehenbemerkungfragebogen_id, id"),
        @Index(name = "ix_untersuchung_untersuchunghoerenaudiogramm", columnList = "untersuchunghoerenaudiogramm_id, id"),
        @Index(name = "ix_untersuchung_untersuchunghoerenotoskopie", columnList = "untersuchunghoerenotoskopie_id, id"),
        @Index(name = "ix_untersuchung_untersuchungmassnahmen", columnList = "untersuchungmassnahmen_id, id"),
        @Index(name = "ix_untersuchung_untersuchunggroessegewicht", columnList = "untersuchunggroessegewicht_id, id"),
        @Index(name = "ix_untersuchung_untersuchunglaeusekontrolle", columnList = "untersuchunglaeusekontrolle_id, id"),
        @Index(name = "ix_untersuchung_untersuchungarztbeurteilung", columnList = "untersuchungarztbeurteilung_id, id"),
        @Index(name = "ix_untersuchung_untersuchungarztbemerkung", columnList = "untersuchungarztbemerkung_id, id"),
        @Index(name = "ix_untersuchung_untersuchungsprache", columnList = "untersuchungsprache_id, id"),
        @Index(name = "ix_untersuchung_untersuchungbewegungsapparat", columnList = "untersuchungbewegungsapparat_id, id"),
        @Index(name = "ix_untersuchung_untersuchungschulrelevantebeintraechtigung", columnList = "untersuchungschulrelevantebeeintraechtigung_id, id"),
        @Index(name = "ix_untersuchung_untersuchungabschluss", columnList = "untersuchungabschluss_id, id"),
        @Index(name = "ix_untersuchung_untersuchungsignatur", columnList = "untersuchungsignatur_id, id")
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Untersuchung extends AbstractEntity<Untersuchung> {

    @Serial
    private static final long serialVersionUID = 7816034792129549030L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchung")
    private Einladung einladung;

    /**
     * This stores data related to the person that is untersucht
     */
    @Nullable // Die Untersuchungsdaten werden beim Abschluss des Anlasses aus dem Kind einmalig abgefüllt
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungkinddaten"), nullable = true)
    private UntersuchungKindDaten untersuchungKindDaten;

    /**
     * This stores the 'meta' data related to the untersuchung
     */
    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungdurchfuehrung"), nullable = true)
    private UntersuchungDurchfuehrung untersuchungDurchfuehrung;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungvorabklaerungen"), nullable = true)
    private UntersuchungVorabklaerungen untersuchungVorabklaerungen;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungtriage"), nullable = true)
    private UntersuchungTriage untersuchungTriage;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungimpfungen"), nullable = true)
    private UntersuchungImpfungen untersuchungImpfungen;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungsehenbeurteilung"), nullable = true)
    private UntersuchungSehenBeurteilung untersuchungSehenBeurteilung;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungsehenbemerkungfragebogen"), nullable = true)
    private UntersuchungSehenBemerkungFragebogen untersuchungSehenBemerkungFragebogen;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchunghoerenaudiogramm"), nullable = true)
    private UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchunghoerenotoskopie"), nullable = true)
    private UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungmassnahmen"), nullable = true)
    private UntersuchungMassnahmen untersuchungMassnahmen;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchunggroessegewicht"), nullable = true)
    private UntersuchungGroesseGewicht untersuchungGroesseGewicht;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchunglaeusekontrolle"), nullable = true)
    private UntersuchungLaeusekontrolle untersuchungLaeusekontrolle;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungarztbeurteilung"), nullable = true)
    private UntersuchungArztBeurteilung untersuchungArztBeurteilung;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungarztbemerkung"), nullable = true)
    private UntersuchungArztBemerkung untersuchungArztBemerkung;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungsprache"), nullable = true)
    private UntersuchungSprache untersuchungSprache;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungbewegungsapparat"), nullable = true)
    private UntersuchungBewegungsapparat untersuchungBewegungsapparat;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungschulrelevantebeeintraechtigung"), nullable = true)
    private UntersuchungSchulrelevanteBeeintraechtigung untersuchungSchulrelevanteBeeintraechtigung;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungabschluss"), nullable = true)
    private UntersuchungAbschluss untersuchungAbschluss;

    @Nullable
    @OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchung_untersuchungsignatur"), nullable = true)
    private UntersuchungSignatur untersuchungSignatur;


    @NonNull
    public static Untersuchung forEinladung(@NonNull Einladung einladung) {
        Untersuchung untersuchung = new Untersuchung();
        untersuchung.setEinladung(einladung);
        einladung.setUntersuchung(untersuchung);
        return untersuchung;
    }
}
