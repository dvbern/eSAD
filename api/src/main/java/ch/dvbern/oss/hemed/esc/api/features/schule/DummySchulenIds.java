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

package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class DummySchulenIds {

    // *** All Tenants
    public static final ID<Schule> DUMMY_SCHULE_ID =
        ID.of(UUID.fromString("0defe1d1-b4f3-4568-afe1-d1b4f34568ba"), Schule.class); // FALLBACK DUMMYSCHULE

    // *** Tenant BS
    // Allgemeine Gewerbeschule Basel
    public static final ID<Schule> ALLGEMEINE_GEWERBESCHULE_BASEL =
        ID.of(UUID.fromString("d6bff17c-a9e2-45ff-bff1-7ca9e265ffc5"), Schule.class);
    // Berufsfachschule Basel
    public static final ID<Schule> BERUFSFACHSCHULE_BASEL =
        ID.of(UUID.fromString("1727c8ac-5e00-4455-a7c8-ac5e00f4551f"), Schule.class);
    // "Fachmaturitätsschule"
    public static final ID<Schule> FACHMATURITAETSSCHULE =
        ID.of(UUID.fromString("5ff2707c-303d-4618-b270-7c303da618b0"), Schule.class);
    // Gymnasium Bäumlihof
    public static final ID<Schule> GYM_BHOF =
        ID.of(UUID.fromString("6dde6527-e296-495c-9e65-27e296e95c4d"), Schule.class);
    // Gymnasium Kirschgarten
    public static final ID<Schule> GYM_KIRSCHGARTEN_MFB =
        ID.of(UUID.fromString("3a688b01-7fd2-41ec-a88b-017fd281ec21"), Schule.class);
    // Gymnasium Kirschgarten
    public static final ID<Schule> GYM_KIRSCHGARTEN_PASSERELLE =
        ID.of(UUID.fromString("78c2faf9-bc6d-4fcc-82fa-f9bc6dffccff"), Schule.class);
    // Gymnasium Kirschgarten
    public static final ID<Schule> GYM_KIRSCHGARTEN =
        ID.of(UUID.fromString("d947ae8d-6ba2-427f-87ae-8d6ba2727f1f"), Schule.class);
    // Gymnasium Leonhard
    public static final ID<Schule> GYM_LEONHARD =
        ID.of(UUID.fromString("005750c6-c373-4774-9750-c6c373d774f1"), Schule.class);
    // Gymnasium am Münsterplatz
    public static final ID<Schule> GYM_MUENSTERPLATZ =
        ID.of(UUID.fromString("4f622ad8-b25f-4f6d-a22a-d8b25f5f6dc4"), Schule.class);
    // Schule für Gestaltung
    public static final ID<Schule> SCHULE_FUER_GESTALTUNG =
        ID.of(UUID.fromString("727db5a4-ec80-4fc3-bdb5-a4ec805fc33f"), Schule.class);
    // Wirtschaftsgymnasium und Wirtschaftsmittelschule
    public static final ID<Schule> WIRTSCHAFTSMITTLERSCHULE =
        ID.of(UUID.fromString("1e7d5454-0a4d-4ef1-bd54-540a4d3ef138"), Schule.class);
    // Zentrum für Brückenangebote
    public static final ID<Schule> ZENTRUM_BRUECKENANGEBOT =
        ID.of(UUID.fromString("b0a067bf-a9b0-4e33-a067-bfa9b0fe333b"), Schule.class);

    private final Map<String, ID<Schule>> DUMMY_SCHULE_NAME_TO_ID = new HashMap<>();

    // this is a static block that is executed when the class is loaded, the schulen are cosntent
    static {
        DUMMY_SCHULE_NAME_TO_ID.put("Allgemeine Gewerbeschule Basel", ALLGEMEINE_GEWERBESCHULE_BASEL);
        DUMMY_SCHULE_NAME_TO_ID.put("Berufsfachschule Basel", BERUFSFACHSCHULE_BASEL);
        DUMMY_SCHULE_NAME_TO_ID.put("Fachmaturitätsschule", FACHMATURITAETSSCHULE);
        DUMMY_SCHULE_NAME_TO_ID.put("Gymnasium Bäumlihof", GYM_BHOF);
        DUMMY_SCHULE_NAME_TO_ID.put("Gymnasium Kirschgarten / MfB", GYM_KIRSCHGARTEN_MFB);
        DUMMY_SCHULE_NAME_TO_ID.put("Gymnasium Kirschgarten / Passerelle", GYM_KIRSCHGARTEN_PASSERELLE);
        DUMMY_SCHULE_NAME_TO_ID.put("Gymnasium Kirschgarten", GYM_KIRSCHGARTEN);
        DUMMY_SCHULE_NAME_TO_ID.put("Gymnasium Leonhard", GYM_LEONHARD);
        DUMMY_SCHULE_NAME_TO_ID.put("Gymnasium am Münsterplatz", GYM_MUENSTERPLATZ);
        DUMMY_SCHULE_NAME_TO_ID.put("Schule für Gestaltung", SCHULE_FUER_GESTALTUNG);
        DUMMY_SCHULE_NAME_TO_ID.put("Wirtschaftsgymnasium und Wirtschaftsmittelschule", WIRTSCHAFTSMITTLERSCHULE);
        DUMMY_SCHULE_NAME_TO_ID.put("Zentrum für Brückenangebote", ZENTRUM_BRUECKENANGEBOT);
    }

    @Nullable
    public static ID<Schule> getDummySchuleForUnassignedEscadaKlassen(@NonNull String schulkreisBezeichnung) {
        return DUMMY_SCHULE_NAME_TO_ID.get(schulkreisBezeichnung);
    }

    public static boolean isDummySchule(@NonNull ID<Schule> schuleID) {
        // the Schulen in the map are the 'actual' dummy schulen from escada, the DUMMY_SCHULE_ID is our fallback
        return DUMMY_SCHULE_NAME_TO_ID.containsValue(schuleID) || schuleID.equals(DUMMY_SCHULE_ID) ;
    }

}
