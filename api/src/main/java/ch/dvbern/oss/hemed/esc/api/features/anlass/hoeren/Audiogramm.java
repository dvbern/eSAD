package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.envers.Audited;

@Entity
@Table
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Audiogramm extends AbstractTenantEntity<Audiogramm> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 5285023727443911252L;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz250;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz500;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz1000;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz2000;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz4000;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz6000;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Dezibel dbHz8000;

}
