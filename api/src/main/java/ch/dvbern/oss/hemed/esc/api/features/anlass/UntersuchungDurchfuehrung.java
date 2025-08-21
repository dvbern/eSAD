package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Nachkontrolle;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungDurchfuehrungUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungDurchfuehrungPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        @Index(name = "ix_untersuchungdurchfuehrung_benutzer_arzt", columnList = "arzt_id, id"),
        @Index(name = "ix_untersuchungdurchfuehrung_benutzer_mpa", columnList = "mpa_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungDurchfuehrungPresentIfReq
public class UntersuchungDurchfuehrung extends AbstractTenantEntity<UntersuchungDurchfuehrung> {

    @Serial
    private static final long serialVersionUID = 4719335518255035134L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungDurchfuehrung")
    private Untersuchung untersuchung;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungdurchfuehrung_benutzer_arzt"), nullable = true)
    private Benutzer arzt;

    @Nullable
    @ManyToOne(optional = true)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_untersuchungdurchfuehrung_benutzer_mpa"), nullable = true)
    private Benutzer mpa;

    @Column(nullable = false)
    private boolean nachkontrolle = false;

    @Column(nullable = false)
    private boolean nachzuegler = false;

    @Column(nullable = false)
    private boolean neueintritt = false;

    @NonNull
    public static UntersuchungDurchfuehrung defaultsForNextUntersuchung(
        @NonNull Untersuchung currentUntersuchung,
        @Nullable Einladung urspruenglicheEinladungForNachzuegler,
        @NonNull Nachkontrolle nachkontrolle
    ) {
        UntersuchungDurchfuehrung durchfuehrung = new UntersuchungDurchfuehrung();
        durchfuehrung.setUntersuchung(currentUntersuchung);
        durchfuehrung.setNachzuegler(urspruenglicheEinladungForNachzuegler != null);
        durchfuehrung.setNachkontrolle(nachkontrolle.getAnlassFlags().hasAnyNachkontrolle());
        durchfuehrung.setArzt(currentUntersuchung.getEinladung().getAnlass().getArzt());
        durchfuehrung.setMpa(currentUntersuchung.getEinladung().getAnlass().getMpa());
        return durchfuehrung;
    }

    @NonNull
    public static UntersuchungDurchfuehrung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungDurchfuehrung durchfuehrung = new UntersuchungDurchfuehrung();
        durchfuehrung.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungDurchfuehrung(durchfuehrung);
        return durchfuehrung;
    }

    @NonNull
    public static UntersuchungDurchfuehrung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung,
        @Nullable Benutzer defaultArzt,
        @Nullable Benutzer defaultMpa
    ) {
        UntersuchungDurchfuehrung durchfuehrung = untersuchung.getUntersuchungDurchfuehrung();
        if (durchfuehrung == null) {
            durchfuehrung = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();
        durchfuehrung.setArzt(UntersuchungDurchfuehrungUtil.getArzt(
            config, durchfuehrung, defaultArzt));
        durchfuehrung.setMpa(UntersuchungDurchfuehrungUtil.getMpa(
            config, durchfuehrung, defaultMpa));
        return durchfuehrung;
    }
}
