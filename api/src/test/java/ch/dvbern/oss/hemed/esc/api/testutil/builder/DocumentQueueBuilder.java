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

package ch.dvbern.oss.hemed.esc.api.testutil.builder;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMapper;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueStatus;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueDisplayJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.mapper.DocumentQueueJsonSpecMapper;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueDisplayJsonSpec;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DocumentQueueBuilder {

    private final DocumentQueue documentQueue;

    private DocumentQueueBuilder() {
        this.documentQueue = defaultDocumentQueue();
        TestUtil.setEntityInfo(documentQueue);
    }

    @NonNull
    public static DocumentQueue defaultDocumentQueue() {
        return new DocumentQueue(
            FileTyp.PLAYGROUND,
            "Beispiel Eintrag",
            DocumentQueueStatus.NEW,
            0,
            null,
            null,
            null,
            null,
            null,
            null,
            BenutzerBuilder.defaultBenutzer(),
            AppLanguages.DE
        );
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withFileTyp(@NonNull FileTyp fileTyp) {
        this.documentQueue.setType(fileTyp);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withStatus(@NonNull DocumentQueueStatus status) {
        this.documentQueue.setStatus(status);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withParameter(@NonNull String parameter) {
        this.documentQueue.setParameters(parameter);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withType(@NonNull FileTyp type) {
        this.documentQueue.setType(type);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withBenutzer(@NonNull Benutzer benutzer) {
        this.documentQueue.setBenutzer(benutzer);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withTimestampResult(@NonNull OffsetDateTime dateTime) {
        this.documentQueue.setTimestampResult(dateTime);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withID(@NonNull ID<DocumentQueue> id) {
        this.documentQueue.setId(id);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withResult(@NonNull FileData fileData) {
        this.documentQueue.setResult(fileData);
        return this;
    }

    @NonNull
    @CanIgnoreReturnValue
    public DocumentQueueBuilder withDisplayName(@NonNull String displayName) {
        this.documentQueue.setDisplayName(displayName);
        return this;
    }

    @NonNull
    public static DocumentQueue create(@NonNull Consumer<DocumentQueueBuilder> block) {
        DocumentQueueBuilder builder = new DocumentQueueBuilder();
        block.accept(builder);
        return builder.documentQueue;
    }

    @NonNull
    public static DocumentQueueDisplayJson createJax(@NonNull Consumer<DocumentQueueBuilder> block) {
        DocumentQueue entity = create(block);
        return DocumentQueueMapper.toJson(entity);
    }

    @NonNull
    public static DocumentQueueDisplayJsonSpec createJaxSpec(@NonNull Consumer<DocumentQueueBuilder> block) {
        DocumentQueueDisplayJson json = createJax(block);
        return DocumentQueueJsonSpecMapper.INSTANCE.documentQueueJsonToDocumentQueueJsonSpec(json);
    }
}
