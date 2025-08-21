/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.fileio.s3;

import ch.dvbern.oss.hemed.esc.api.shared.auth.HibernateTenantResolver;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@ApplicationScoped
@Slf4j
public class S3RequestBuilderProducer {

    private final ConfigService configService;
    private final HibernateTenantResolver tenantResolver;


    @Inject
    public S3RequestBuilderProducer(
        @NonNull ConfigService configService,
        @PersistenceUnitExtension @NonNull HibernateTenantResolver tenantResolver
    ) {
        this.configService = configService;
        this.tenantResolver = tenantResolver;
    }

    @Produces
    @RequestScoped
    public S3RequestBuilder produceS3RequestBuilder() {
        String tenantId = tenantResolver.resolveTenantId();
        TenantEnum tenant = TenantEnum.valueOf(tenantId);
        Optional<String> s3EncryptionKeyBase64 = configService.getS3EncryptionKey(tenant);
        if (s3EncryptionKeyBase64.isPresent()) {
            return new S3EncryptedRequestBuilderImpl( s3EncryptionKeyBase64.get());
        }
        return new S3RequestBuilderImpl();
    }
}
