/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.net.URL;

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
@ApplicationScoped
public class CheckAllowedLinkValidator implements ConstraintValidator<CheckAllowedLink, URL> {

    private final ConfigService configService;

    @Override
    public void initialize(CheckAllowedLink constraintAnnotation) {
        // no-op
    }

    @Override
    public boolean isValid(@Nullable URL url, @Nullable ConstraintValidatorContext constraintValidatorContext) {
        URL publicUrl = configService.getPublicUrl();
        if (url != null) {
            return url.toString().startsWith(publicUrl.toString());
        }
        return true;
    }
}
