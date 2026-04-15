package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.SystemRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemRollenMapper {

    @Nullable
    public static SystemRollenJson toJson(@Nullable SystemRollen systemRollen) {
        if (systemRollen == null) {
            return null;
        }
        return new SystemRollenJson(
            EnumSetUtil.toList(systemRollen.getFachRollen())
        );
    }
}
