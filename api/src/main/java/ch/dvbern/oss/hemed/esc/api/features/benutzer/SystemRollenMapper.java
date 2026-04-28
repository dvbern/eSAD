package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.SystemRollenJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemRollenMapper {

	@Nullable
	public static SystemRollenJson toJson(
		@Nullable SystemRollen systemRollen,
		@NonNull TenantEnum tenant
	) {
		if (systemRollen == null) {
			return null;
		}
		List<Berechtigung> berechtigungen = systemRollen.getBerechtigungen(tenant).stream().toList();
		return new SystemRollenJson(
			EnumSetUtil.toList(systemRollen.getFachRollen()),
			berechtigungen
		);
	}

	@Nullable
	public static SystemRollen toEntity(@NonNull Benutzer benutzer, @Nullable SystemRollenJson systemRollen) {
		if (systemRollen == null || systemRollen.getFachRollen().isEmpty()) {
			return null;
		}
		SystemRollen result = benutzer.getSystemRollen();
		if (result == null) {
			result = new SystemRollen();
			result.setBenutzer(benutzer);
		}
		result.setFachRollen(EnumSetUtil.toEnumSet(systemRollen.getFachRollen(), FachRolle.class));
		return result;
	}
}
