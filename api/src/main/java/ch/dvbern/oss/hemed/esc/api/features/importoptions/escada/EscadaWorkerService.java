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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileValidationService;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Service
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
@Transactional(TxType.NOT_SUPPORTED)
public class EscadaWorkerService {

    private final EscadaSyncEventService escadaSyncEventService;
    private final FileValidationService fileValidationService;
    private final EventBus eventBus;

    public void processEscadaCsvUploadUsingEventBus(@NonNull FileUpload fileUpload) {
        // check the filetype
        fileValidationService.requireFiletypeIsAllowed(fileUpload, FileTyp.ESCADA_SYNC);
        // store the sync data in a file
        EscadaSyncEvent escadaSyncEvent = escadaSyncEventService.saveEscadaSyncData(fileUpload);
        LOG.info("ESCADA: sending event for processing ");
        eventBus.requestAndForget(EscadaService.ESCADA_SYNC_EVENT_ADDRESS, escadaSyncEvent);
    }
}
