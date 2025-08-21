package ch.dvbern.oss.hemed.esc.api.features.benutzer;/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
@Service
@RequiredArgsConstructor
public class BerechtigungenenCacheService {


    public static final int ROLE_CACHE_MAX_SIZE = 5000;
    private final BenutzerService benutzerService;
    private final ConfigService configService;

    private LoadingCache<EmailAddress, Optional<Set<Berechtigung>>> benutzerEmailToBerechtigungenenCache;


    @PostConstruct
    void init() {
        // do init in postConstruct so all injected configs are ready to be used

        // Loader function
        CacheLoader<EmailAddress, Optional<Set<Berechtigung>>> loadBerechtigungenForBenutzernameFunction;
        loadBerechtigungenForBenutzernameFunction = new CacheLoader<>() {
            @Override
            @NonNull
            public  Optional<Set<Berechtigung>> load(@Nullable EmailAddress email) {
                if (email != null) {
                    return benutzerService.findBerechtigungenByEmail(email);
                }
                return Optional.empty();
            }
        };

        int timeout  = configService.getBenutzerRollenCacheTimeoutMinutes();
        // define cache settings
        benutzerEmailToBerechtigungenenCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.of(timeout, ChronoUnit.MINUTES))
            .maximumSize(ROLE_CACHE_MAX_SIZE)
            .build(loadBerechtigungenForBenutzernameFunction);

    }

    @NonNull
    public Optional<Set<Berechtigung>> getBerechtigungenForBenutzerByEmail(@NonNull EmailAddress email) {
        return benutzerEmailToBerechtigungenenCache.getUnchecked(email);
    }

    /**
     * flush the cache for the given benutzername. Should be called if the roles of a user are changed
     */
    public void flushCache(@NonNull EmailAddress benutzername) {
        benutzerEmailToBerechtigungenenCache.invalidate(benutzername);
    }

}
