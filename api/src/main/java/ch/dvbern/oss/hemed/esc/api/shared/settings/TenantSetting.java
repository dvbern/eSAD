package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.io.Serial;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.types.StringListConverter;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeatures;
import ch.dvbern.oss.hemed.esc.api.shared.types.AgbResponsability;
import ch.dvbern.oss.hemed.esc.api.shared.types.LandingPageOption;
import ch.dvbern.oss.hemed.esc.api.shared.types.LoginMethod;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantSetting extends AbstractEntity<TenantSetting> {

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

    @NotNull
    @Column(nullable = false)
    private int folgeTermineMaxSlots;

    @NotNull
    @NonNull
    @Column(nullable = false)
    @Convert(converter = StringListConverter.class)
    private List<String> customerAdminEmails;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private String supportEmail;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private AgbResponsability agbResponsability;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private LandingPageOption defaultLandingPage;

    @Valid
    @NotNull
    @NonNull
    @Embedded
    private TenantSettingFeatures tenantSettingFeatures;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String einladungStatusValues;

    @NotNull
    @NonNull
    @Column(nullable = false)
    private LocalDate gueltigAbWachstumskurvePch;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String loginMethods;

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

    @NonNull
    public EnumSet<EinladungStatus> getEinladungStatusValues() {
        return EnumSetUtil.fromString(EinladungStatus.class, einladungStatusValues);
    }

    public void setEinladungStatusValues(@NonNull Collection<EinladungStatus> einladungStatusValues) {
        this.einladungStatusValues = EnumSetUtil.toString(einladungStatusValues);
    }

    @NonNull
    public EnumSet<LoginMethod> getLoginMethods() {
        return EnumSetUtil.fromString(LoginMethod.class, loginMethods);
    }

    public void setLoginMethods(@NonNull Collection<LoginMethod> loginMethods) {
        this.loginMethods = EnumSetUtil.toString(loginMethods);
    }

}
