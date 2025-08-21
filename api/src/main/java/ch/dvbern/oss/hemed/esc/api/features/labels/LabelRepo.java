package ch.dvbern.oss.hemed.esc.api.features.labels;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class LabelRepo extends BaseRepo<Label> {
    private final AppLanguageProvider appLanguageProvider;

    @NonNull
    public List<Label> findAll() {
        boolean languageIsGerman = appLanguageProvider.current() == AppLanguages.DE;
        return db.selectFrom(QLabel.label)
            .orderBy(languageIsGerman ?
                QLabel.label.name.nameDe.asc() :
                QLabel.label.name.nameFr.asc())
            .fetch();
    }

    @NonNull
    public List<Label> filterByType(@NonNull LabelTyp type) {
        return db.selectFrom(QLabel.label)
            .where(QLabel.label.labelTyp.eq(type))
            .fetch();
    }
}
