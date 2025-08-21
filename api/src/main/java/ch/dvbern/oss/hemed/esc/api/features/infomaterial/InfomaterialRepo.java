package ch.dvbern.oss.hemed.esc.api.features.infomaterial;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguageProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class InfomaterialRepo extends BaseRepo<Infomaterial> {
    private final AppLanguageProvider appLanguageProvider;

    @NonNull
    public List<Infomaterial> findAll() {
        boolean languageIsGerman = appLanguageProvider.current() == AppLanguages.DE;
        return db.selectFrom(QInfomaterial.infomaterial)
            .orderBy(languageIsGerman ?
                QInfomaterial.infomaterial.name.nameDe.asc() :
                QInfomaterial.infomaterial.name.nameFr.asc())
            .fetch();
    }

    @NonNull
    public List<Infomaterial> findAllActive() {
        boolean languageIsGerman = appLanguageProvider.current() == AppLanguages.DE;
        return db.selectFrom(QInfomaterial.infomaterial).orderBy(
                languageIsGerman ?
                    QInfomaterial.infomaterial.name.nameDe.asc() :
                    QInfomaterial.infomaterial.name.nameFr.asc())
            .where(QInfomaterial.infomaterial.aktiv.isTrue())
            .fetch();
    }
}
