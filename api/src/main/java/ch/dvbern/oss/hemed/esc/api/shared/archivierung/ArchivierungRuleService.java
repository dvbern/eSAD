package ch.dvbern.oss.hemed.esc.api.shared.archivierung;

import java.util.List;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This interface defines the contract for determining which Dossiers need to be archived
 */
public interface ArchivierungRuleService {

    @NonNull
    List<ID<Dossier>> findDossiersToArchive(int batchsize);

    @NonNull
    List<ID<Dossier>> findDossiersToDelete(int monthsBeforeDeletion, int batchsize);
}
