package ch.dvbern.oss.hemed.esc.api.features.arzt;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.json.EntityInfoJson;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.print.ArztExportService;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseMapper;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

import static ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil.DATE_FORMATTER_FOR_FILENAME;

@JaxMapper
@RequiredArgsConstructor
public class ArztMapper {

    private final ArztService arztService;
    private final AppClock appClock;
    private final TL tl;

    @NonNull
    public static ArztJson toJson(@NonNull Arzt arzt) {
        return new ArztJson(
            arzt.getId(), EntityInfoJson.fromEntity(arzt), new ArztSaveJson(
            arzt.getAnrede(),
            arzt.getFachrichtung(),
            arzt.getOrganisation(),
            arzt.getVorname(),
            arzt.getName(),
            AdresseMapper.toJson(arzt.getAdresse()),
            arzt.getTelefon(),
            arzt.getEmail(),
            arzt.getBemerkungen(),
            arzt.getHpvTeilnahme()));
    }

    @NonNull
    private Arzt toEntityForCreate(@NonNull ArztSaveJson arztSaveJson) {
        return new Arzt(
            arztSaveJson.getAnrede(),
            arztSaveJson.getFachrichtung(),
            arztSaveJson.getOrganisation(),
            arztSaveJson.getVorname(),
            arztSaveJson.getName(),
            AdresseMapper.toEntity(arztSaveJson.getAdresse()),
            arztSaveJson.getTelefon(),
            arztSaveJson.getEmail(),
            arztSaveJson.getBemerkungen(),
            arztSaveJson.getHpvTeilnahme());
    }

    @NonNull
    private Arzt toEntity(@NonNull Arzt existingArzt, @NonNull ArztSaveJson saveJson) {
        existingArzt.setAnrede(saveJson.getAnrede());
        existingArzt.setFachrichtung(saveJson.getFachrichtung());
        existingArzt.setOrganisation(saveJson.getOrganisation());
        existingArzt.setVorname(saveJson.getVorname());
        existingArzt.setName(saveJson.getName());
        existingArzt.setAdresse(AdresseMapper.toEntity(saveJson.getAdresse()));
        existingArzt.setTelefon(saveJson.getTelefon());
        existingArzt.setEmail(saveJson.getEmail());
        existingArzt.setBemerkungen(saveJson.getBemerkungen());
        existingArzt.setHpvTeilnahme(saveJson.getHpvTeilnahme());
        return existingArzt;
    }

    @NonNull
    public List<ArztJson> findAll() {
        return arztService.findAll().stream().map(ArztMapper::toJson).toList();
    }

    @NonNull
    public ArztJson getById(@NonNull ID<Arzt> arztID) {
        return ArztMapper.toJson(arztService.getById(arztID));
    }

    @NonNull
    public PrintResultDTO  exportArztList(@NonNull List<ID<Arzt>> arztIDs) {
        List<Arzt> aerzte = arztService.findByIds(arztIDs);
        String fileTimeStamp = appClock.localDateTime().now().toLocalDate().format(DATE_FORMATTER_FOR_FILENAME);
        return  ArztExportService.generatAearzteExport(aerzte, fileTimeStamp, tl);
    }

    @NonNull
    public ArztJson update(@NonNull ID<Arzt> arztId, @NonNull ArztSaveJson arztJson) {
        Arzt existingArzt = arztService.getById(arztId);
        Arzt entity = toEntity(existingArzt, arztJson);
        return toJson(arztService.persist(entity));
    }

    @NonNull
    public ArztJson create(@NonNull ArztSaveJson arztSaveJson) {
        Arzt arzt = toEntityForCreate(arztSaveJson);
        return toJson(arztService.persist(arzt));
    }

    public void remove(@NonNull ID<Arzt> arztId) {
        arztService.remove(arztId);
    }
}
