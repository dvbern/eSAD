/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.net.URI;
import java.net.URL;
import java.util.EnumSet;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.commons.i18nl10n.I18nMessage;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.mail.service.MailDAO;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationRollenBuilder;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
class FachrollenMailServiceTest {

    private FachrollenMailService service;
    private MailDAO mailDAO;
    private ConfigService configService;
    private TenantSettingsService tenantSettingsService;
    private TL tl;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        tl = mock(TL.class);
        configService = mock(ConfigService.class);
        mailDAO = mock(MailDAO.class);
        tenantSettingsService = mock(TenantSettingsService.class);

        service = new FachrollenMailService(tl, configService, mailDAO, tenantSettingsService);

        URL url = URI.create("http://localhost").toURL();
        when(configService.getPublicUrl()).thenReturn(url);

        TenantSetting tenantSetting = mock(TenantSetting.class);
        when(tenantSettingsService.getTenantSetting()).thenReturn(tenantSetting);
        when(tenantSetting.getSupportEmail()).thenReturn("support@test.com");

        Uni<Void> successUni = Uni.createFrom().nullItem();
        doReturn(successUni).when(mailDAO).sendEmail(
            anyString(),
            anyString(),
            anyString()
        );
        when(tl.translate("benutzer-organisation-delegieren.mail.subject"))
            .thenReturn("Einladung Organisation");
        when(tl.translate(
            eq("benutzer-organisation-delegieren.mail.content"),
            eq("nameArzt"),
            anyString(),
            eq("eMailBenutzer"),
            anyString(),
            eq("linkeSAD"),
            eq("http://localhost"),
            eq("eMailsupport"),
            eq("support@test.com")
        )).thenReturn("Mail content");
    }

    @Test
    void testSendEinladungOrganisationMail_SendForAnyRole() {
        // ARRANGE
        Benutzer empfangBenutzer = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.create(builder -> builder
            .withVerantwortlicherArzt(BenutzerBuilder.defaultBenutzer()));

        empfangBenutzer.addOrganisation(OrganisationRollenBuilder.create(b -> b
            .withBenutzer(empfangBenutzer)
            .withOrganisation(organisation)
            .withFachrollen(EnumSet.of(FachRolle.EMPFANG))));

        when(tl.translate(any(I18nMessage.class))).thenReturn("translated");

        // ACT
        service.sendEinladungOrganisationMail(empfangBenutzer, organisation);

        // ASSERT
        verify(mailDAO).sendEmail(any(), any(), any());
    }

    @Test
    void testSendEinladungOrganisationMail_SendForMultipleRoles() {
        // ARRANGE
        Benutzer multiRoleBenutzer = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.create(builder -> builder
            .withVerantwortlicherArzt(BenutzerBuilder.defaultBenutzer()));

        multiRoleBenutzer.addOrganisation(OrganisationRollenBuilder.create(b -> b
            .withBenutzer(multiRoleBenutzer)
            .withOrganisation(organisation)
            .withFachrollen(EnumSet.of(FachRolle.EMPFANG, FachRolle.MPA))));

        when(tl.translate(any(I18nMessage.class))).thenReturn("translated");

        // ACT
        service.sendEinladungOrganisationMail(multiRoleBenutzer, organisation);

        // ASSERT
        verify(mailDAO).sendEmail(any(), any(), any());
    }

    @Test
    void testSendEinladungOrganisationMail_SendEvenIfRolesAreForDifferentOrganisation() {
        // ARRANGE
        Benutzer otherOrgBenutzer = BenutzerBuilder.defaultBenutzer();
        Organisation currentOrganisation = OrganisationBuilder.create(builder -> builder
            .withVerantwortlicherArzt(BenutzerBuilder.defaultBenutzer()));

        Organisation differentOrganisation = OrganisationBuilder.create(builder -> builder
            .withVerantwortlicherArzt(BenutzerBuilder.defaultBenutzer()));

        otherOrgBenutzer.addOrganisation(OrganisationRollenBuilder.create(b -> b
            .withBenutzer(otherOrgBenutzer)
            .withOrganisation(differentOrganisation)
            .withFachrollen(EnumSet.of(FachRolle.EMPFANG))));

        when(tl.translate(any(I18nMessage.class))).thenReturn("translated");

        // ACT
        service.sendEinladungOrganisationMail(otherOrgBenutzer, currentOrganisation);

        // ASSERT
        verify(mailDAO).sendEmail(any(), any(), any());
    }

    @Test
    void testSendEinladungOrganisationMail_NoMailIfVerantwortlicherArzt() {
        // ARRANGE
        Benutzer arzt = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.create(builder -> builder
            .withVerantwortlicherArzt(arzt));

        // ACT
        service.sendEinladungOrganisationMail(arzt, organisation);

        // ASSERT
        verify(mailDAO, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
