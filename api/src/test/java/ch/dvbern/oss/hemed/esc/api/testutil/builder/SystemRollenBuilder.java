package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.util.EnumSet;
import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollen;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SystemRollenBuilder {
    private SystemRollen systemRollen;

    private SystemRollenBuilder() {
        this.systemRollen = defaultSystemRollen();
        TestUtil.setEntityInfo(systemRollen);
    }

    @NonNull
    public static SystemRollen defaultSystemRollen() {
        return new SystemRollen(
            BenutzerBuilder.defaultBenutzer(),
            null
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public SystemRollenBuilder forSystemRollen(@NonNull SystemRollen existingSystemRollen) {
        this.systemRollen = existingSystemRollen;
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SystemRollenBuilder withBenutzer(@NonNull Benutzer benutzer) {
        this.systemRollen.setBenutzer(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public SystemRollenBuilder withSystemrollen(@NonNull EnumSet<FachRolle> systemrollen) {
        this.systemRollen.setFachRollen(systemrollen);
        return this;
    }

    @NonNull
    public static SystemRollen create(@NonNull Consumer<SystemRollenBuilder> block) {
        SystemRollenBuilder builder = new SystemRollenBuilder();
        block.accept(builder);
        return builder.systemRollen;
    }
}
