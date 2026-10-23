package ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
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
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataArztJson {

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @Nullable
    @Schema(required = false)
    Set<Einverstaendniserklaerung> einverstaendniserklaerung;

    @Nullable
    @Schema(required = false)
    Verhaltensfragebogen verhaltensfragebogen;

    @Nullable
    @Schema(required = false)
    Verhalten verhalten;

    @Nullable
    @Schema(required = false)
    Blutdruck blutdruck;

    @Nullable
    @Schema(required = false)
    Integer blutdruckSystolisch;

    @Nullable
    @Schema(required = false)
    Integer blutdruckDiastolisch;

    @Nullable
    @Schema(required = false)
    Herzauskultation herzauskultation;

    @Nullable
    @Schema(required = false)
    Haut haut;

    @Nullable
    @Schema(required = false)
    Set<MundAndZaehne> mundAndZaehne;

    @Nullable
    @Schema(required = false)
    Sprachfaehigkeit sprachfaehigkeitSingleValue;

    @Nullable
    @Schema(required = false)
    Set<Sprachfaehigkeit> sprachfaehigkeitMultiValue;

    @Nullable
    @Schema(required = false)
    BigDecimal sprachfaehigkeitPunkte;

    @Nullable
    @Schema(required = false)
    Motorik motorik;

    @Nullable
    @Schema(required = false)
    BigDecimal motorikPunkte;

    @Nullable
    @Schema(required = false)
    Kognition kognition;

    @Nullable
    @Schema(required = false)
    BigDecimal kognitionPunkte;

    @Nullable
    @Schema(required = false)
    Set<Bewegungsapparat> bewegungsapparat;

    @Nullable
    @Schema(required = false)
    Einbeinstand einbeinstand;

    @Nullable
    @Schema(required = false)
    Einbeinstand einbeinstandRechts;

    @Nullable
    @Schema(required = false)
    Einbeinstand einbeinstandLinks;

    @Nullable
    @Schema(required = false)
    Einbeinhuepfen einbeinhuepfen;

    @Nullable
    @Schema(required = false)
    Einbeinhuepfen einbeinhuepfenRechts;

    @Nullable
    @Schema(required = false)
    Einbeinhuepfen einbeinhuepfenLinks;

    @Nullable
    @Schema(required = false)
    Set<Fingeropposition> fingeropposition;

    @Nullable
    @Schema(required = false)
    Hampelmann hampelmann;

    @Nullable
    @Schema(required = false)
    BallFangen ballFangen;

    @Nullable
    @Schema(required = false)
    BallWerfen ballWerfen;

    @Nullable
    @Schema(required = false)
    NameSchreiben nameSchreiben;

    @Nullable
    @Schema(required = false)
    FigurNachmalen figurNachmalen;

    @Nullable
    @Schema(required = false)
    Stifthaltung stifthaltung;

    @Nullable
    @Schema(required = false)
    Aussprache aussprache;

    @Nullable
    @Schema(required = false)
    Lesefit lesefit;

    @Nullable
    @Schema(required = false)
    BigDecimal lesefitPunkte;

    @Nullable
    @Schema(required = false)
    ZahlenNachsprechen zahlenNachsprechen4;

    @Nullable
    @Schema(required = false)
    ZahlenNachsprechen zahlenNachsprechen5;

    @Nullable
    @Schema(required = false)
    Zeichnen zeichnenKreis;

    @Nullable
    @Schema(required = false)
    Zeichnen zeichnenViereck;

    @Nullable
    @Schema(required = false)
    Zeichnen zeichnenDreieck;

    @Nullable
    @Schema(required = false)
    ZeichnenMensch zeichnenMensch;

    @Nullable
    @Schema(required = false)
    BigDecimal zeichnenMenschPunkte;
}
