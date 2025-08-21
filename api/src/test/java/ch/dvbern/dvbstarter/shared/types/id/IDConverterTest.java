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

package ch.dvbern.dvbstarter.shared.types.id;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.Serial;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IDConverterTest {
    private ObjectMapper mapper;

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class JsonWithIdList {
        private List<ID<IdConverterTestEntity>> idList;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class NestedIdListJson {
        private List<List<ID<IdConverterTestEntity>>> nestedIDList;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IdConverterTestEntity extends AbstractEntity<IdConverterTestEntity> {
        @Serial
        private static final long serialVersionUID = -5701269897668016540L;
        private ID<IdConverterTestEntity> id;
    }


    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        IDConverter.registerJackson(module);
        mapper.registerModule(module);
    }

    @Test
    void testConvertEmpty() throws JsonProcessingException {
        String emptyJson = "{ \"idList\" : []}";

        JsonWithIdList jsonWithIdList = mapper.readValue(emptyJson, JsonWithIdList.class);
        Assertions.assertThat(jsonWithIdList).isNotNull().hasFieldOrPropertyWithValue("idList", List.of());

        String serializedJson = mapper.writeValueAsString(jsonWithIdList);
        Assertions.assertThat(serializedJson).isEqualTo("{\"idList\":[]}");

    }

    @Test
    void testConvertSingle() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        String single = "{\"idList\":[\"" + uuid + "\"]}";

        JsonWithIdList jsonWithIdList = mapper.readValue(single, JsonWithIdList.class);
        Assertions.assertThat(jsonWithIdList)
            .isNotNull()
            .hasFieldOrPropertyWithValue("idList", List.of(ID.of(uuid, IdConverterTestEntity.class)));

        String serializedJson = mapper.writeValueAsString(jsonWithIdList);
        Assertions.assertThat(serializedJson).isEqualTo("{\"idList\":[\"" + uuid + "\"]}");
    }

    @Test
    void testConvertMultiple() throws JsonProcessingException {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        String jsonWithMultipleIds = createJsonWithMultipleIds(uuid1, uuid2);

        JsonWithIdList jsonWithIdList = mapper.readValue(jsonWithMultipleIds, JsonWithIdList.class);
        Assertions.assertThat(jsonWithIdList).isNotNull();
        Assertions.assertThat(jsonWithIdList.getIdList())
            .contains(ID.of(uuid1, IdConverterTestEntity.class), ID.of(uuid2, IdConverterTestEntity.class));

        String serializedJson = mapper.writeValueAsString(jsonWithIdList);
        Assertions.assertThat(serializedJson)
            .isEqualTo("{\"idList\":[\"" + uuid1 + "\",\"" + uuid2 + "\"]}");

    }

    private String createJsonWithMultipleIds(UUID... uuids) {

        StringBuilder sb = new StringBuilder("{\"idList\":[");
        for (int i = 0; i < uuids.length; i++) {
            sb.append('"').append(uuids[i]).append('"');
            if (i < uuids.length - 1) {
                sb.append(',');
            }
        }
        sb.append("]}");
        return sb.toString();

    }

    @Nested
    class NestedIdListTest {
        @Test
        void testConvertEmpty() throws JsonProcessingException {
            String emptyJson = "{ \"nestedIDList\" : []}";

            NestedIdListJson nestedIdListJson = mapper.readValue(emptyJson, NestedIdListJson.class);
            Assertions.assertThat(nestedIdListJson).isNotNull().hasFieldOrPropertyWithValue("nestedIDList", List.of());
        }

        @Test
        void testConvertSingle() throws JsonProcessingException {
            UUID uuid = UUID.randomUUID();
            String single = "{\"nestedIDList\":[[\"" + uuid + "\"]]}";

            NestedIdListJson nestedIdListJson = mapper.readValue(single, NestedIdListJson.class);
            Assertions.assertThat(nestedIdListJson)
                .isNotNull()
                .hasFieldOrPropertyWithValue("nestedIDList", List.of(List.of(ID.of(uuid, IdConverterTestEntity.class))));
        }

        @Test
        void testConvertMultiple() throws JsonProcessingException {
            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            UUID uuid3 = UUID.randomUUID();

            ID<IdConverterTestEntity> id1 = ID.of(uuid1, IdConverterTestEntity.class);
            ID<IdConverterTestEntity> id2 = ID.of(uuid2, IdConverterTestEntity.class);
            ID<IdConverterTestEntity> id3 = ID.of(uuid3, IdConverterTestEntity.class);
            String jsonWithMultipleIds = createJsonWithMultipleNestedIds(uuid1, uuid2, uuid3);

            NestedIdListJson nestedIdListJson = mapper.readValue(jsonWithMultipleIds, NestedIdListJson.class);
            Assertions.assertThat(nestedIdListJson).isNotNull();
            Assertions.assertThat(nestedIdListJson.getNestedIDList())
                .contains(List.of(id1, id2, id3));

            String serializedJson = mapper.writeValueAsString(nestedIdListJson);
            Assertions.assertThat(serializedJson)
                .isEqualTo("{\"nestedIDList\":[[\"" + uuid1 + "\",\"" + uuid2 + "\",\"" + uuid3 + "\"]]}");

        }

        private String createJsonWithMultipleNestedIds(UUID... uuids) {

            StringBuilder sb = new StringBuilder("{\"nestedIDList\":[[");
            for (int i = 0; i < uuids.length; i++) {
                sb.append('"').append(uuids[i]).append('"');
                if (i < uuids.length - 1) {
                    sb.append(',');
                }
            }
            sb.append("]]}");
            return sb.toString();

        }
    }

}
