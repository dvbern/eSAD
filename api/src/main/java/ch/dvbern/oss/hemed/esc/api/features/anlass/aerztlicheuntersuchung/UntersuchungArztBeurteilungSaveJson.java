package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
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
    private List<Sprachfaehigkeit> sprachfaehigkeit;

    @Nullable
    @Schema(required = false)
    private String sprachfaehigkeitBemerkung;

    @Nullable
    @Schema(required = false)
    private Motorik motorik;

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
    private String einbeinstandBemerkung;

    @Nullable
    @Schema(required = false)
    private Einbeinhuepfen einbeinhuepfen;

    @Nullable
    @Schema(required = false)
    private String einbeinhuepfenBemerkung;

    @Nullable
    @Schema(required = false)
    private List<Fingeropposition> fingeropposition;

    @Nullable
    @Schema(required = false)
    private String fingeroppositionBemerkung;

    @Schema(required = true)
    private long version;
}
