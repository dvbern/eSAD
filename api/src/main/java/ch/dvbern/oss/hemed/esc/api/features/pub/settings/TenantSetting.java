package ch.dvbern.oss.hemed.esc.api.features.pub.settings;

import java.io.Serial;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;
@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uc_tenantsetting_tenant", columnNames = { "tenant" })
}, indexes = {
    @Index(name = "ix_tenantsetting_tenant", columnList = "tenant, id")
})
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantSetting extends AbstractTenantEntity<TenantSetting> {

    @Serial
    private static final long serialVersionUID = -2616852567861298939L;

    @NotNull
    @NonNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppLanguages defaultLanguage;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private String supportedLanguages;


    public void setSupportedLanguages(@NonNull List<AppLanguages> supportedLanguages) {
        this.supportedLanguages = supportedLanguages.stream()
            .map(AppLanguages::name)
            .sorted()
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    @NonNull
    public List<AppLanguages> getSupportedLanguages() {
        return Stream.of(supportedLanguages.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .map(AppLanguages::valueOf)
            .sorted()
            .toList();
    }
}
