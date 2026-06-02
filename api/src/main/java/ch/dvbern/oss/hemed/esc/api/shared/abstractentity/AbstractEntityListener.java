package ch.dvbern.oss.hemed.esc.api.shared.abstractentity;

import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.StringNormalizerUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@AllArgsConstructor(access = AccessLevel.PACKAGE)
// required for JPA :(
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class AbstractEntityListener {

    @Inject
    AppClock clock;

    @Inject
    CurrentUserService currentUserService;

    @Inject
    ConfigService configService;

    @PrePersist
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void prePersist(@NonNull AbstractEntity<?> entity) {
        var now = clock.offsetDateTime().now();

        entity.setTimestampErstellt(now);
        entity.setTimestampMutiert(now);
        entity.setUserErstellt(currentUserService.currentUserRef().getId().getId());
        entity.setUserMutiert(currentUserService.currentUserRef().getId().getId());

        ensureNormalization(entity);
        ensureHashValueUpdate(entity);
    }

    @PreUpdate
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void preUpdate(@NonNull AbstractEntity<?> entity) {
        var now = clock.offsetDateTime().now();

        entity.setTimestampMutiert(now);
        entity.setUserMutiert(currentUserService.currentUserRef().getId().getId());

        ensureOptimisticLocking(entity);
        ensureNormalization(entity);
        ensureHashValueUpdate(entity);
    }

    private void ensureNormalization(@NonNull AbstractEntity<?> entity) {
        if (entity instanceof Kind kind) {
            kind.setNormalizedName(StringNormalizerUtil.normalizeAndStripDiarcritics(kind.getName()));
            kind.setNormalizedVorname(StringNormalizerUtil.normalizeAndStripDiarcritics(kind.getVorname()));
        }
    }

    private void ensureHashValueUpdate(@NonNull AbstractEntity<?> entity) {
        if (entity instanceof Kind kind) {
            kind.setHashValue(HashValueGeneratorUtil.generateHashValueForKindValues(
                kind.getName(),
                kind.getVorname(),
                kind.getGeschlecht(),
                kind.getGeburtsdatum()));
        }
    }

    private void ensureOptimisticLocking(@NonNull AbstractEntity<?> entity) {
        if (configService.isFeatureOptimisticLockingDisabled()) {
            return;
        }
        if (entity instanceof EntityWithOptimisticLocking) {
            if (entity.isOptimisticLockingCheckDisabled()) {
                return;
            }
            if (entity.getVersionFromFrontend() == null) {
                throw new AppFailureException(AppFailureMessage.internalError(
                    "Version from frontend is null for Entity "
                        + entity.getClass().getSimpleName()
                        + ". Use "
                        + BaseRepo.class.getName()
                        + ".persistWithOptimisticLocking for update"));
            }
            if (!Objects.equals(entity.getVersion(), entity.getVersionFromFrontend())) {
                throw new AppFailureException(AppFailureMessage.changedByOtherUser(entity));
            }
        }
    }
}
