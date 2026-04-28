package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.io.Serial;
import java.util.EnumSet;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.util.EnumSetUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table
public class SystemRollen extends AbstractEntity<SystemRollen>
    implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 1541040056371728827L;

    @NotNull
    @NonNull
    @OneToOne(mappedBy = "systemRollen", optional = false)
    private Benutzer benutzer;

    @Nullable
    @Size(max = DbConst.DB_ENUM_LIST_LENGTH)
    @Column(nullable = true, length = DbConst.DB_ENUM_LIST_LENGTH)
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String fachRollen;


    public void setFachRollen(@NonNull EnumSet<FachRolle> fachRollen) {
        this.fachRollen = EnumSetUtil.toString(fachRollen);
    }

    @NonNull
    public EnumSet<FachRolle> getFachRollen() {
        return EnumSetUtil.fromString(FachRolle.class, fachRollen);
    }

    @NonNull
    public Set<Berechtigung> getBerechtigungen(@NonNull TenantEnum tenant) {
        return FachRolle.collectAllBerechtigungen(
            tenant, getFachRollen().toArray(new FachRolle[0]));
    }
}
