package ch.dvbern.oss.hemed.esc.api.features.search;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SearchApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FilterCombinationSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PageRequestJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PageResultJsonSearchSummaryJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchColumnsSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchJsonFiltersInnerSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchSummaryJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class SearchResourceSpezialsucheITest extends AbstractITest {

    private final SearchApiSpec api = SearchApiSpec.search(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final KindService kindService;

    private static final String KIND_NAME_UUID = "13c5e319-0ba8-4695-9bb2-81401189c78d";
    private static final ID<Kind>
        KIND_OHNE_ANLASS_ID = ID.of(UUID.fromString("6f37bb78-b914-4371-867f-2794ffd2c984"), Kind.class);
    private static final ID<Kind>
        KIND_OHNE_ANLASS_MATTEO_ID = ID.of(UUID.fromString("619794e6-d185-41e0-8def-13fd767d4c18"), Kind.class);
    private static final ID<Kind>
        KIND_OHNE_ANLASS_ID_KLASSE_8 = ID.of(UUID.fromString("dc65efae-0d27-4f2b-ad1e-1eb2a78b89ee"), Kind.class);
    private static final ID<Kind>
        KIND_WITH_ANLASS_ID_KLASSE_4 = ID.of(UUID.fromString("890205bb-0c8c-4adc-a75e-dc47871d8d99"), Kind.class);
    private static final ID<Kind>
        KIND_WITH_ANLASS_ID_KLASSE_8 = ID.of(UUID.fromString("c8485adb-7a94-482f-a7c9-48c14b46fa5f"), Kind.class);
    private static final ID<Kind>
        KIND_WITH_ANLASS_ID_KLASSE_8_MATT = ID.of(UUID.fromString("789a8bfd-6496-4e3a-81e2-bef521c99988"), Kind.class);


    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        insertKind(KIND_OHNE_ANLASS_ID, null, null, null, null);
        insertKind(KIND_OHNE_ANLASS_MATTEO_ID, "Matteo", null, null, null);
        insertKind(KIND_OHNE_ANLASS_ID_KLASSE_8, null, null, null, Klassenstufe.SEK_I_KLASSE_1);
        insertKind(KIND_WITH_ANLASS_ID_KLASSE_4, null, AnlassTyp.KLASSE_4, LocalDate.of(2021, 1, 5), Klassenstufe.PRIMAR_KLASSE_4);
        insertKind(KIND_WITH_ANLASS_ID_KLASSE_8, null, AnlassTyp.KLASSE_8, LocalDate.of(2021, 2, 5), Klassenstufe.SEK_I_KLASSE_1);
        insertKind(KIND_WITH_ANLASS_ID_KLASSE_8_MATT, "Matt", AnlassTyp.KLASSE_8, LocalDate.of(2022, 2, 1), Klassenstufe.SEK_I_KLASSE_1);
    }

    @AfterEach
    void tearDown() {
        kindService.remove(KIND_OHNE_ANLASS_ID);
        kindService.remove(KIND_OHNE_ANLASS_MATTEO_ID);
        kindService.remove(KIND_OHNE_ANLASS_ID_KLASSE_8);
        kindService.remove(KIND_WITH_ANLASS_ID_KLASSE_4);
        kindService.remove(KIND_WITH_ANLASS_ID_KLASSE_8);
        kindService.remove(KIND_WITH_ANLASS_ID_KLASSE_8_MATT);
    }

    @ParameterizedTest
    @MethodSource("provideSearchParameters")
    @TestAsAdminBS
    void findPaginatedKinder_should_filter_klassenstufe_neu(
        @Nullable Boolean anlassPendentSuche,
        @Nullable String vorname,
        @Nullable Boolean anlassNotAssigned,
        @Nullable LocalDate anlassDatumVon,
        @Nullable LocalDate anlassDatumBis,
        @Nullable AnlassTyp anlassTyp,
        @Nullable Klassenstufe klassenstufe,
        @NonNull List<ID<Kind>> expectedKinderIdsFound
    ) {
        // ARRANGE
        // Anlass Pendent Suche
        List<SearchJsonFiltersInnerSpec> filters = new ArrayList<>();
        if (anlassPendentSuche != null ) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.ANLASS_PENDENT)
                .filterValue(String.valueOf(anlassPendentSuche))
                .filterCombinationMethod(FilterCombinationSpec.AND));
        }
        // Anlass Not Assigned
        if (anlassNotAssigned != null) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.ANLASS_NOT_ASSIGNED)
                .filterValue(String.valueOf(anlassNotAssigned))
                .filterCombinationMethod(FilterCombinationSpec.SINGLE));
        }
        // Anlass Zeitraum
        if (anlassDatumVon != null) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.ANLASS_ZEITRAUM_VON)
                .filterValue(anlassDatumVon.toString())
                .filterCombinationMethod(FilterCombinationSpec.SINGLE));
        }
        if (anlassDatumBis != null) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.ANLASS_ZEITRAUM_BIS)
                .filterValue(anlassDatumBis.toString())
                .filterCombinationMethod(FilterCombinationSpec.SINGLE));
        }
        // Anlass Typ
        if (anlassTyp != null) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.ANLASS_TYP)
                .filterValue(anlassTyp.toString())
                .filterCombinationMethod(FilterCombinationSpec.SINGLE));
        }
        // Klassenstufe
        if (klassenstufe != null) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.KLASSENSTUFE)
                .filterValue(klassenstufe.name())
                .filterCombinationMethod(FilterCombinationSpec.SINGLE));
        }
        // Vorname Filter
        if (vorname != null) {
            filters.add(new SearchJsonFiltersInnerSpec()
                .columnName(SearchColumnsSpec.VORNAME)
                .filterValue(vorname)
                .filterCombinationMethod(FilterCombinationSpec.AND));
        }
         // Der Name-Filter wird IMMER benötigt, um die Suche auf die Kinder dieses Tests zu beschränken.
        SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
            SearchColumnsSpec.NAME, KIND_NAME_UUID,
            FilterCombinationSpec.AND);
        filters.add(nameFilter);

        // ACT
        SearchJsonSpec searchSpec = new SearchJsonSpec(
            new PageRequestJsonSpec(1L, 50, true),
            Collections.emptyList(),
            filters
        );
        PageResultJsonSearchSummaryJsonSpec result = api
            .searchResourceFindKinderPaginated()
            .body(searchSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

        // ASSERT
        assertThat(result)
            .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
            .returns(50, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
            .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalItemCount)
            .matches(count -> count >= expectedKinderIdsFound.size())
        ;
        List<SearchSummaryJsonSpec> actualKinderFound = result.getData();
        assertThat(actualKinderFound)
            .hasSize(expectedKinderIdsFound.size())
            .extracting(SearchSummaryJsonSpec::getId)
            .containsExactlyInAnyOrderElementsOf(
                expectedKinderIdsFound.stream()
                    .map(ID::getId)
                    .toList());
    }

    static Stream<Arguments> provideSearchParameters() {
        return Stream.of(
            // isPendenteAnlassSuche, vorname, anlassNotAssigned, anlassDatumVon, anlassDatumBis, anlassTyp,  klassenstufe, expectedKinderIdsFound

            // Pendente Anlass Suche, AnlassNotAssigned = null
            Arguments.of(
                true, null, null, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                true, "Matt", null, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_MATTEO_ID)),
            Arguments.of(
                true, null, null, null, null, AnlassTyp.KLASSE_4, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, null, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 31), null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, null, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 28), null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, null, LocalDate.of(2022, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31), AnlassTyp.KLASSE_4, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_4, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, null, null, null, null, Klassenstufe.SEK_I_KLASSE_1,
                List.of(KIND_OHNE_ANLASS_ID_KLASSE_8)),
            // Pendente Anlass Suche, AnlassNotAssigned = null
            Arguments.of(
                false, null, null, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_4, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                false, "Matt", null, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_MATTEO_ID, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                false, null, null, null, null, AnlassTyp.KLASSE_4, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4)),
            Arguments.of(
                false, null, null, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 31), null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4)),
            Arguments.of(
                false, null, null, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 28), null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4, KIND_WITH_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                false, null, null, null, null, null, Klassenstufe.SEK_I_KLASSE_1,
                List.of(KIND_OHNE_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),

            // Pendente Anlass Suche, AnlassNotAssigned = TRUE
            Arguments.of(
                true, null, true, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                true, "Matt", true, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_MATTEO_ID)),
            Arguments.of(
                true, null, true, null, null, AnlassTyp.KLASSE_4, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                true, null, true, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 31), null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                true, null, true, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 28), null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                true, null, true, LocalDate.of(2022, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31), AnlassTyp.KLASSE_4, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                true, null, true, null, null, null, Klassenstufe.SEK_I_KLASSE_1,
                List.of(KIND_OHNE_ANLASS_ID_KLASSE_8)),
            // Pendente Anlass Suche, AnlassNotAssigned = TRUE
            Arguments.of(
                false, null, true, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_ID, KIND_OHNE_ANLASS_MATTEO_ID, KIND_OHNE_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                false, "Matt", true, null, null, null, null,
                List.of(KIND_OHNE_ANLASS_MATTEO_ID)),
            Arguments.of(
                false, null, true, null, null, AnlassTyp.KLASSE_4, null,
                List.of()),
            Arguments.of(
                false, null, true, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 31), null, null,
                List.of()),
            Arguments.of(
                false, null, true, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 28), null, null,
                List.of()),
            Arguments.of(
                false, null, true, null, null, null, Klassenstufe.SEK_I_KLASSE_1,
                List.of(KIND_OHNE_ANLASS_ID_KLASSE_8)),

            // Pendente Anlass Suche, AnlassNotAssigned = FALSE
            Arguments.of(
                true, null, false, null, null, null, null,
                List.of()),
            Arguments.of(
                true, "Matt", false, null, null, null, null,
                List.of()),
            Arguments.of(
                true, null, false, null, null, AnlassTyp.KLASSE_4, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, false, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 31), null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, false, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 28), null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, false, LocalDate.of(2022, Month.JANUARY, 1), LocalDate.of(2022, Month.DECEMBER, 31), AnlassTyp.KLASSE_4, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                true, null, false, null, null, null, Klassenstufe.SEK_I_KLASSE_1,
                List.of()),
            // Pendente Anlass Suche, AnlassNotAssigned = null
            Arguments.of(
                false, null, false, null, null, null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4, KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                false, "Matt", false, null, null, null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_8_MATT)),
            Arguments.of(
                false, null, false, null, null, AnlassTyp.KLASSE_4, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4)),
            Arguments.of(
                false, null, false, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.JANUARY, 31), null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4)),
            Arguments.of(
                false, null, false, LocalDate.of(2021, Month.JANUARY, 1), LocalDate.of(2021, Month.FEBRUARY, 28), null, null,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_4, KIND_WITH_ANLASS_ID_KLASSE_8)),
            Arguments.of(
                false, null, false, null, null, null, Klassenstufe.SEK_I_KLASSE_1,
                List.of(KIND_WITH_ANLASS_ID_KLASSE_8, KIND_WITH_ANLASS_ID_KLASSE_8_MATT))
        );
    }

    private void insertKind(
        @NonNull ID<Kind> kindId,
        @Nullable String vorname,
        @Nullable AnlassTyp anlassTyp,
        @Nullable LocalDate anlassDatum,
        @Nullable Klassenstufe klassenstufe
    ) {
        Klasse klasse = klassenstufe != null ? fixtureService.createKlasse(klassenstufe) : null;
        Kind kindWithAnlass = kindService.persistWithoutDuplicateCheck(KindBuilder.create(
            builder -> builder
                .withID(kindId)
                .withName(KIND_NAME_UUID + "_TestKind" , vorname + KIND_NAME_UUID)
                .withKlasse(klasse)));
        Dossier dossier = fixtureService.createDossier(kindWithAnlass);
        if (anlassTyp != null && anlassDatum != null) {
            final Anlass anlass = fixtureService.persistAnlass(
                AnlassBuilder.create(builder -> builder
                    .withDatumSetSchuljahr(anlassDatum)
                    .withAnlassTyp(anlassTyp)));
            Einladung previousEinladung = fixtureService.createEinladung(anlass, dossier);
            fixtureService.createUntersuchungWithImpfungen(previousEinladung);
        }
    }
}
