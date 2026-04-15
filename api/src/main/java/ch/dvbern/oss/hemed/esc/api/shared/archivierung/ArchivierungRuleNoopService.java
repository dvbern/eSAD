package ch.dvbern.oss.hemed.esc.api.shared.archivierung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import jakarta.enterprise.inject.Typed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
// since we have a producer for ArchivierungRuleService exclude this class from  being directly injected for
// ArchivierungRuleService
@Typed(ArchivierungRuleNoopService.class)
@RequiredArgsConstructor
@Slf4j
public class ArchivierungRuleNoopService implements ArchivierungRuleService {

    @Override
    @NonNull
    public List<ID<Dossier>> findDossiersToArchive(int batchsize) {
        return List.of();
    }

    @Override
    @NonNull
    public List<ID<Dossier>> findDossiersToDelete(int monthsBeforeDeletion, int batchsize) {
        return List.of();
    }
}
