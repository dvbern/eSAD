 package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

 import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
 import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
 import com.google.common.collect.MoreCollectors;
 import lombok.RequiredArgsConstructor;
 import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class TenantSettingsRepo extends BaseRepo<TenantSetting> {

    @NonNull
    public  TenantSetting getTenantSetting() {
        return db.findAll(QTenantSetting.tenantSetting).stream().collect(MoreCollectors.onlyElement());
    }

}
