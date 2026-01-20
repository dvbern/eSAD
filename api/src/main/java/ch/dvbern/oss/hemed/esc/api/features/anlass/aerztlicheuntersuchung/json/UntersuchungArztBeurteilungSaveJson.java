package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json;

import java.math.BigDecimal;
import java.util.List;

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
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class UntersuchungArztBeurteilungSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    private List<Einverstaendniserklaerung> einverstaendniserklaerung;

    @Nullable
    @Schema(required = false)
    private Verhaltensfragebogen verhaltensfragebogen;

    @Nullable
    @Schema(required = false)
    private Verhalten verhalten;

    @Nullable
    @Schema(required = false)
    private Blutdruck blutdruck;

    @Nullable
    @Schema(required = false)
    private Integer blutdruckSystolisch;

    @Nullable
    @Schema(required = false)
    private Integer blutdruckDiastolisch;

    @Nullable
    @Schema(required = false)
    private Herzauskultation herzauskultation;

    @Nullable
    @Schema(required = false)
    private String herzauskultationBemerkung;

    @Nullable
    @Schema(required = false)
    private Haut haut;

    @Nullable
    @Schema(required = false)
    private String hautBemerkung;

    @Nullable
    @Schema(required = false)
    private List<MundAndZaehne> mundAndZaehne;

    @Nullable
    @Schema(required = false)
    private String mundAndZaehneBemerkung;

    @Nullable
    @Schema(required = false)
    private Sprachfaehigkeit sprachfaehigkeitSingleValue;

    @Nullable
    @Schema(required = false)
    private List<Sprachfaehigkeit> sprachfaehigkeitMultiValue;

    @Nullable
    @Schema(required = false)
    private String sprachfaehigkeitBemerkung;

    @Nullable
    @Schema(required = false)
    private BigDecimal sprachfaehigkeitPunkte;

    @Nullable
    @Schema(required = false)
    private Motorik motorik;

    @Nullable
    @Schema(required = false)
    private BigDecimal motorikPunkte;

    @Nullable
    @Schema(required = false)
    private Kognition kognition;

    @Nullable
    @Schema(required = false)
    private BigDecimal kognitionPunkte;

    @Nullable
    @Schema(required = false)
    private List<Bewegungsapparat> bewegungsapparat;

    @Nullable
    @Schema(required = false)
    private String bewegungsapparatBemerkung;

    @Nullable
    @Schema(required = false)
    private Einbeinstand einbeinstand;

    @Nullable
    @Schema(required = false)
    private Einbeinstand einbeinstandRechts;

    @Nullable
    @Schema(required = false)
    private Einbeinstand einbeinstandLinks;

    @Nullable
    @Schema(required = false)
    private String einbeinstandBemerkung;

    @Nullable
    @Schema(required = false)
    private Einbeinhuepfen einbeinhuepfen;

    @Nullable
    @Schema(required = false)
    private Einbeinhuepfen einbeinhuepfenRechts;

    @Nullable
    @Schema(required = false)
    private Einbeinhuepfen einbeinhuepfenLinks;

    @Nullable
    @Schema(required = false)
    private String einbeinhuepfenBemerkung;

    @Nullable
    @Schema(required = false)
    private List<Fingeropposition> fingeropposition;

    @Nullable
    @Schema(required = false)
    private String fingeroppositionBemerkung;

    @Nullable
    @Schema(required = false)
    private Hampelmann hampelmann;

    @Nullable
    @Schema(required = false)
    private BallFangen ballFangen;

    @Nullable
    @Schema(required = false)
    private BallWerfen ballWerfen;

    @Nullable
    @Schema(required = false)
    private NameSchreiben nameSchreiben;

    @Nullable
    @Schema(required = false)
    private FigurNachmalen figurNachmalen;

    @Nullable
    @Schema(required = false)
    private Stifthaltung stifthaltung;

    @Nullable
    @Schema(required = false)
    private Aussprache aussprache;

    @Nullable
    @Schema(required = false)
    private Lesefit lesefit;

    @Nullable
    @Schema(required = false)
    private BigDecimal lesefitPunkte;

    @Nullable
    @Schema(required = false)
    private ZahlenNachsprechen zahlenNachsprechen4;

    @Nullable
    @Schema(required = false)
    private ZahlenNachsprechen zahlenNachsprechen5;

    @Nullable
    @Schema(required = false)
    private Zeichnen zeichnenKreis;

    @Nullable
    @Schema(required = false)
    private Zeichnen zeichnenViereck;

    @Nullable
    @Schema(required = false)
    private Zeichnen zeichnenDreieck;

    @Nullable
    @Schema(required = false)
    private ZeichnenMensch zeichnenMensch;

    @Nullable
    @Schema(required = false)
    private BigDecimal zeichnenMenschPunkte;

    @Schema(required = true)
    private long version;
}
