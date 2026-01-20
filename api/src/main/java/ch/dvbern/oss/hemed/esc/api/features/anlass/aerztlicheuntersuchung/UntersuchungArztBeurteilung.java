/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.EnumSet;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Aussprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallFangen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.BallWerfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einverstaendniserklaerung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.FigurNachmalen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Hampelmann;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Kognition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Lesefit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.NameSchreiben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Stifthaltung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhaltensfragebogen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.ZahlenNachsprechen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Zeichnen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.ZeichnenMensch;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.UntersuchungArztUtil;
import ch.dvbern.oss.hemed.esc.api.shared.validator.CheckUntersuchungArztBeurteilungPresentIfReq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@CheckUntersuchungArztBeurteilungPresentIfReq
public class UntersuchungArztBeurteilung extends AbstractEntity<UntersuchungArztBeurteilung>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1446398935944895850L;

    @NotNull
    @NonNull
    @OneToOne(optional = false, mappedBy = "untersuchungArztBeurteilung")
    private Untersuchung untersuchung;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String einverstaendniserklaerung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Verhaltensfragebogen verhaltensfragebogen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Verhalten verhalten;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Blutdruck blutdruck;

    @Nullable
    @Column(nullable = true)
    private Integer blutdruckSystolisch;

    @Nullable
    @Column(nullable = true)
    private Integer blutdruckDiastolisch;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Herzauskultation herzauskultation;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String herzauskultationBemerkung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Haut haut;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String hautBemerkung;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String mundAndZaehne;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String mundAndZaehneBemerkung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Sprachfaehigkeit sprachfaehigkeitSingleValue;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String sprachfaehigkeitMultiValue;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String sprachfaehigkeitBemerkung;

    @Nullable
    @Min(0)
    @Max(99)
    @Column(nullable = true, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal sprachfaehigkeitPunkte;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Motorik motorik;

    @Nullable
    @Min(0)
    @Max(99)
    @Column(nullable = true, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal motorikPunkte;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Kognition kognition;

    @Nullable
    @Min(0)
    @Max(99)
    @Column(nullable = true, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal kognitionPunkte;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String bewegungsapparat;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String bewegungsapparatBemerkung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Einbeinstand einbeinstand;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Einbeinstand einbeinstandRechts;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Einbeinstand einbeinstandLinks;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String einbeinstandBemerkung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Einbeinhuepfen einbeinhuepfen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Einbeinhuepfen einbeinhuepfenRechts;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Einbeinhuepfen einbeinhuepfenLinks;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String einbeinhuepfenBemerkung;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String fingeropposition;

    @Nullable
    @Size(max = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    @Column(nullable = true, length = DbConst.DB_TEXTAREA_MAX_LENGTH_2000)
    private String fingeroppositionBemerkung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Hampelmann hampelmann;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private BallFangen ballFangen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private BallWerfen ballWerfen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private NameSchreiben nameSchreiben;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private FigurNachmalen figurNachmalen;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Stifthaltung stifthaltung;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Aussprache aussprache;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Lesefit lesefit;

    @Nullable
    @Min(0)
    @Max(99)
    @Column(nullable = true, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal lesefitPunkte;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private ZahlenNachsprechen zahlenNachsprechen4;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private ZahlenNachsprechen zahlenNachsprechen5;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Zeichnen zeichnenKreis;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Zeichnen zeichnenViereck;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private Zeichnen zeichnenDreieck;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(length = DbConst.DB_ENUM_LENGTH, nullable = true)
    private ZeichnenMensch zeichnenMensch;

    @Nullable
    @Min(0)
    @Max(99)
    @Column(nullable = true, scale = DbConst.DB_ZAHL_SCALE_1)
    private BigDecimal zeichnenMenschPunkte;

    // Einverstaendniserklaerung
    public void setEinverstaendniserklaerung(@Nullable EnumSet<Einverstaendniserklaerung> einverstaendnisErklaerung) {
        this.einverstaendniserklaerung = EnumSetUtil.toString(einverstaendnisErklaerung);
    }

    @NonNull
    public EnumSet<Einverstaendniserklaerung> getEinverstaendniserklaerung() {
        return EnumSetUtil.fromString(Einverstaendniserklaerung.class, einverstaendniserklaerung);
    }

    // MundAndZaehne
    public void setMundAndZaehne(@Nullable EnumSet<MundAndZaehne> mundAndZaehneList) {
        this.mundAndZaehne = EnumSetUtil.toString(mundAndZaehneList);
    }

    @NonNull
    public EnumSet<MundAndZaehne> getMundAndZaehne() {
        return EnumSetUtil.fromString(MundAndZaehne.class, mundAndZaehne);
    }

    // Sprachfaehigkeit
    public void setSprachfaehigkeitMultiValue(@Nullable EnumSet<Sprachfaehigkeit> sprachfaehigkeitList) {
        this.sprachfaehigkeitMultiValue = EnumSetUtil.toString(sprachfaehigkeitList);
    }

    @NonNull
    public EnumSet<Sprachfaehigkeit> getSprachfaehigkeitMultiValue() {
        return EnumSetUtil.fromString(Sprachfaehigkeit.class, sprachfaehigkeitMultiValue);
    }

    // Bewegungsapparat
    public void setBewegungsapparat(@Nullable EnumSet<Bewegungsapparat> bewegungsapparatList) {
        this.bewegungsapparat = EnumSetUtil.toString(bewegungsapparatList);
    }

    @NonNull
    public EnumSet<Bewegungsapparat> getBewegungsapparat() {
        return EnumSetUtil.fromString(Bewegungsapparat.class, bewegungsapparat);
    }

    // Fingeropposition
    public void setFingeropposition(@Nullable EnumSet<Fingeropposition> fingeroppositionList) {
        this.fingeropposition = EnumSetUtil.toString(fingeroppositionList);
    }

    @NonNull
    public EnumSet<Fingeropposition> getFingeropposition() {
        return EnumSetUtil.fromString(Fingeropposition.class, fingeropposition);
    }

    @NonNull
    public static UntersuchungArztBeurteilung forUntersuchung(@NonNull Untersuchung untersuchung) {
        UntersuchungArztBeurteilung untersuchungArzt = new UntersuchungArztBeurteilung();
        untersuchungArzt.setUntersuchung(untersuchung);
        untersuchung.setUntersuchungArztBeurteilung(untersuchungArzt);
        return untersuchungArzt;
    }

    @NonNull
    public static UntersuchungArztBeurteilung forUntersuchungAbschliessen(
        @NonNull Untersuchung untersuchung
    ) {
        UntersuchungArztBeurteilung arzt = untersuchung.getUntersuchungArztBeurteilung();
        if (arzt == null) {
            arzt = forUntersuchung(untersuchung);
        }
        AnlassConfig config = untersuchung.getEinladung().getAnlass().getAnlassConfig();

        arzt.setBewegungsapparat(UntersuchungArztUtil.getBewegungsapparat(
            config, arzt, Bewegungsapparat.NICHT_GEPRUEFT));
        arzt.setEinbeinstand(UntersuchungArztUtil.getEinbeinstand(
            config, arzt, Einbeinstand.NICHT_GEPRUEFT));
        arzt.setEinbeinhuepfen(UntersuchungArztUtil.getEinbeinhuepfen(
            config, arzt, Einbeinhuepfen.NICHT_GEPRUEFT));
        arzt.setFingeropposition(UntersuchungArztUtil.getFingeropposition(
            config, arzt, Fingeropposition.NICHT_GEPRUEFT));
        arzt.setMotorik(UntersuchungArztUtil.getMotorik(
            config, arzt, Motorik.NICHT_GEPRUEFT));
        arzt.setSprachfaehigkeitMultiValue(UntersuchungArztUtil.getSprachfaehigkeit(
            config, arzt, Sprachfaehigkeit.NICHT_GEPRUEFT));
        arzt.setMundAndZaehne(UntersuchungArztUtil.getMundAndZaehne(
            config, arzt, MundAndZaehne.NICHT_GEPRUEFT));
        arzt.setHaut(UntersuchungArztUtil.getHaut(
            config, arzt, Haut.NICHT_GEPRUEFT));
        arzt.setHerzauskultation(UntersuchungArztUtil.getHerzauskultation(
            config, arzt, Herzauskultation.NICHT_GEPRUEFT));
        arzt.setBlutdruck(UntersuchungArztUtil.getBlutdruck(
            config, arzt, Blutdruck.NICHT_GEPRUEFT));
        arzt.setVerhalten(UntersuchungArztUtil.getVerhalten(
            config, arzt, Verhalten.NICHT_GEPRUEFT));

        return arzt;
    }
}
