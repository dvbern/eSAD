package ch.dvbern.oss.hemed.esc.api.features.search;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.EdokidIdTestGenerator;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerTestGenerator;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidIdGeneratorService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizService;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.features.search.types.SearchColumns;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageRequestJson;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NotizBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungInternBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.UntersuchungImpfungenMassnahmenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SearchApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FilterCombinationSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSearchListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KrankheitSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PageRequestJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PageResultJsonSearchSummaryJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchColumnsSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchJsonFiltersInnerSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchJsonSortsInnerSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchSummaryJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SearchSummaryListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SortDirectionSpec;
import com.google.common.collect.MoreCollectors;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.features.search.types.SearchColumns.DATE_TIME_FORMAT_PATTERN;
import static ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus.isInaktiv;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class SearchResourceITest extends AbstractITest {

    private static final int MAX_PAGE_SIZE = PageRequestJson.MAX_PAGE_SIZE;

    private final SearchApiSpec api = SearchApiSpec.search(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final KindService kindService;
    private final DossierService dossierService;
    private final NotizService notizService;
    private final BenutzerService benutzerService;
    private final LabelService labelService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
    private final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService;
    private final UntersuchungImpfungInternService untersuchungImpfungInternService;
    private final UntersuchungImpfungenMassnahmenService untersuchungImpfungenMassnahmenService;
    private final AnlassService anlassService;
    private final EinladungService einladungService;
    private final OrganisationService organisationService;

    // Without this, the test has to be adapted every year on 01. August
    private final LocalDate currentDate = this.getAppClock().localDateTime().now().toLocalDate();
    private final int currentEintrittsjahr = EdokidIdGeneratorService.calculateEintrittsJahr(currentDate);

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllKinderSummary() {
        // ARRANGE
        Kind kind = KindBuilder.create(
            builder -> builder
                .withID(ID.random(Kind.class))
                .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                .withName("Reber", "Martin"));
        kindService.persistWithoutDuplicateCheck(kind);
        // ACT
        SearchSummaryListJsonSpec result = api
            .searchResourceFindAllKinderSummary()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SearchSummaryListJsonSpec.class);
        // ASSERT
        assertThat(result.getKinderList())
            .isNotEmpty()
            .anyMatch(kindJsonSpec -> {
                    return kind.getId().getId().equals(kindJsonSpec.getId())
                        && "Reber".equals(kindJsonSpec.getName())
                        && "Martin".equals(kindJsonSpec.getVorname());
                }
            );
    }

    @Nested
    class QuickSearchTest {

        private Kind kind1;
        private Kind kind2;
        private Kind kind3;
        private Kind kind4;
        private Kind kind5;

        @BeforeEach
        void setUp() {

            // ARRANGE
            UUID uuid = UUID.randomUUID();
            kind1 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withGeburtsdatum(LocalDate.of(2020, 1, 1))
                    .withEsadId(EdokidIdTestGenerator.getNextEdokidId("482").toFormattedString())
                    .withName("Reber_" + uuid, "Martin_" + uuid));
            kindService.persistWithoutDuplicateCheck(kind1);
            kind2 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withGeburtsdatum(LocalDate.of(2021, 12, 9))
                    .withEsadId(EdokidIdTestGenerator.getNextEdokidId("489").toFormattedString())
                    .withName("Gereber_" + uuid, "Adrian_" + uuid));
            kindService.persistWithoutDuplicateCheck(kind2);

            kind3 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withGeburtsdatum(LocalDate.of(2021, 3, 2))
                    .withEsadId(EdokidIdTestGenerator.getNextEdokidId("489").toFormattedString())
                    .withName("Yorke_" + uuid, "Jane_" + uuid));
            kindService.persistWithoutDuplicateCheck(kind3);

            kind4 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withGeburtsdatum(LocalDate.of(2021, 3, 2))
                    .withEsadId(EdokidIdTestGenerator.getNextEdokidId("489").toFormattedString())
                    .withName("Hueber", "LUNA"));
            kindService.persistWithoutDuplicateCheck(kind4);
            // This kind should not be found as it is inactive
            kind5 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withGeburtsdatum(LocalDate.of(2020, 2, 1))
                    .withKindAktivStatus(EntityAktivStatus.INAKTIV)
                    .withEsadId(EdokidIdTestGenerator.getNextEdokidId("482").toFormattedString())
                    .withName("Reber_" + uuid, "Tina_" + uuid));
            kindService.persistWithoutDuplicateCheck(kind5);

        }

        @Test
        @TestAsAdminBS
        void findKinderUsingQuickSearchWithName() {

            // ACT
            var result = api
                .searchResourceSearchKinder()
                .searchTermQuery("eber")
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(KindSearchListJsonSpec.class);

            // ASSERT
            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind1.getId().getId()))
                .hasSize(1);

            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind2.getId().getId()))
                .hasSize(1);

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind3.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind5.getId().getId()))
                .isEmpty();

        }

        @Test
        @TestAsAdminBS
        void findKinderUsingQuickSearchWithEsadId() {

            // ACT
            var result = api
                .searchResourceSearchKinder()
                .searchTermQuery(this.kind1.getEsadId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(KindSearchListJsonSpec.class);

            // ASSERT
            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind1.getId().getId()))
                .hasSize(1);

            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind2.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind3.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind5.getId().getId()))
                .isEmpty();

        }

        @Test
        @TestAsAdminBS
        void findKinderUsingGeburtsdatum() {

            // ACT
            var result = api
                .searchResourceSearchKinder()
                .searchTermQuery(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(kind2.getGeburtsdatum()))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(KindSearchListJsonSpec.class);

            // ASSERT
            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind1.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind2.getId().getId()))
                .hasSize(1);

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind3.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind5.getId().getId()))
                .isEmpty();

        }

        @Test
        @TestAsAdminBS
        void findKinderUsingExactName() {

            // ACT
            var result = api
                .searchResourceSearchKinder()
                .searchTermQuery(kind4.getNameVorname())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(KindSearchListJsonSpec.class);

            // ASSERT
            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind1.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .isNotEmpty()
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind2.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind3.getId().getId()))
                .isEmpty();

            assertThat(result.getKinderList())
                .filteredOn(kindSearchJsonSpec -> kindSearchJsonSpec.getId().equals(kind4.getId().getId()))
                .isNotEmpty()
                .hasSize(1);
        }
    }

    @Nested
    class KinderPaginationITest {

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_honor_filter() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            insertKnownTestKinder(uuid);

            // ACT
            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, "%all",
                FilterCombinationSpec.OR);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 2);

            assertThat(result.getTotalItemCount())
                .as("totalItemCount should reflect total number of Kinder in DB")
                .isNotNull()
                .isInstanceOf(Integer.class)
                .satisfies(total -> assertThat(total).isGreaterThanOrEqualTo(4));

            assertThat(result.getData())
                .isNotEmpty()
                .allMatch(k -> Strings.CI.contains(k.getName(), "all"));

            assertThat(result.getData())
                .isNotEmpty()
                .filteredOn(k -> k.getName().contains(uuid.toString() + "ralley"))
                .hasSize(1);

            assertThat(result.getData())
                .isNotEmpty()
                .filteredOn(k -> k.getName().contains(uuid.toString() + "ALLCAPS"))
                .hasSize(1);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_accept_maxPageSize_at_limit() {
            // ACT + ASSERT
            api
                .searchResourceFindKinderPaginated()
                .body(searchSpecWithMaxPageSize(PageRequestJson.MAX_PAGE_SIZE))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_reject_maxPageSize_above_limit() {
            // ACT + ASSERT
            api
                .searchResourceFindKinderPaginated()
                .body(searchSpecWithMaxPageSize(PageRequestJson.MAX_PAGE_SIZE + 1))
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.BAD_REQUEST.getStatusCode());
        }

        private SearchJsonSpec searchSpecWithMaxPageSize(int maxPageSize) {
            return new SearchJsonSpec(
                new PageRequestJsonSpec(1L, maxPageSize, false),
                Collections.emptyList(),
                Collections.emptyList()
            );
        }

        @Test
        @TestAsAdminBS
        void findAllKinderPaginated_should_return_filteredKinderCount() {
            // ARRANGE
            Kind kindA = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(
                        SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Reber", "Martin"));
            kindService.persistWithoutDuplicateCheck(kindA);

            Kind kindB = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(
                        SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Ruebli", "Anna"));
            kindService.persistWithoutDuplicateCheck(kindB);

            // ACT
            var unfilteredSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                Collections.emptyList()
            );

            var unfiltered = api.searchResourceFindKinderPaginated()
                .body(unfilteredSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            var nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, "Reber", FilterCombinationSpec.AND
            );

            var filteredSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilter)
            );

            var filtered = api.searchResourceFindKinderPaginated()
                .body(filteredSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(unfiltered)
                .isNotNull()
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalItemCount)
                .as("totalItemCount (unfiltered) should be >= 2")
                .satisfies(total -> assertThat(total).isInstanceOf(Integer.class))
                .matches(total -> (total) >= 2);

            Integer totalUnfiltered = unfiltered.getTotalItemCount();
            Integer filteredCount = filtered.getTotalFilteredCount();
            Integer totalFiltered = filtered.getTotalItemCount();

            assertThat(totalFiltered)
                .as("totalItemCount is global/unchanged by filters")
                .isEqualTo(totalUnfiltered);

            assertThat(filteredCount)
                .as("filteredCount should be <= total")
                .isLessThanOrEqualTo(totalUnfiltered)
                .isLessThan(totalUnfiltered);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_work_for_umlauts_filter() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            insertKnownTestKinder(uuid);

            // ACT
            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, uuid.toString() + "Bürgenme",
                FilterCombinationSpec.OR);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData())
                .isNotEmpty()
                .filteredOn(k -> k.getName().contains(uuid.toString() + "Bürgenmeier"))
                .hasSize(1);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_combine_searchterms() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            insertKnownTestKinder(uuid);

            // ACT
            String name1ToFind = uuid.toString() + "Bürgenmeier";
            String name2ToFind = uuid.toString() + "Klassenkind";

            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, name1ToFind + "  " + name2ToFind,
                FilterCombinationSpec.OR);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 2);

            assertThat(result.getData())
                .isNotEmpty()
                .filteredOn(k -> k.getName().contains(uuid.toString() + "Bürgenmeier"))
                .hasSize(1);
            assertThat(result.getData())
                .isNotEmpty()
                .filteredOn(k -> k.getName().contains(name2ToFind))
                .hasSize(1);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_honor_name_search_prefix_behavior() {
            // ARRANGE
            var uuid = UUID.randomUUID();

            // Create additional test data with specific names for testing the prefix behavior
            Kind kindStartsWithRa = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Raber_" + uuid, "Martin_" + uuid));
            kindService.persistWithoutDuplicateCheck(kindStartsWithRa);

            Kind kindContainsRa = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Graber_" + uuid, "Franz_" + uuid));
            kindService.persistWithoutDuplicateCheck(kindContainsRa);

            // TEST CASE 1: Search without % prefix - should only match names starting with "Ra"
            SearchJsonFiltersInnerSpec nameFilterWithoutPercent = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, "Ra",
                FilterCombinationSpec.SINGLE);
            SearchJsonSpec searchSpecWithoutPercent = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilterWithoutPercent)
            );

            PageResultJsonSearchSummaryJsonSpec resultWithoutPercent = api
                .searchResourceFindKinderPaginated()
                .body(searchSpecWithoutPercent)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT: Should only find names starting with "Ra"
            assertThat(resultWithoutPercent.getData())
                .isNotEmpty()
                .allMatch(k -> k.getName().startsWith("Ra"));

            assertThat(resultWithoutPercent.getData())
                .filteredOn(k -> k.getId().equals(kindStartsWithRa.getId().getId()))
                .hasSize(1);

            assertThat(resultWithoutPercent.getData())
                .filteredOn(k -> k.getId().equals(kindContainsRa.getId().getId()))
                .isEmpty();

            // ACT TEST CASE 2: Search with % prefix - should match names containing "ra" anywhere
            SearchJsonFiltersInnerSpec nameFilterWithPercent = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, "%ra",
                FilterCombinationSpec.SINGLE);
            SearchJsonSpec searchSpecWithPercent = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilterWithPercent)
            );

            PageResultJsonSearchSummaryJsonSpec resultWithPercent = api
                .searchResourceFindKinderPaginated()
                .body(searchSpecWithPercent)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT: Should find names containing "ra" anywhere (case insensitive)
            assertThat(resultWithPercent.getData())
                .isNotEmpty()
                .allMatch(k -> k.getName().toLowerCase().contains("ra"));

            // Should find both test kinds
            assertThat(resultWithPercent.getData())
                .filteredOn(k -> k.getId().equals(kindStartsWithRa.getId().getId()))
                .hasSize(1);

            // Should also find the one with "ra" in the middle of the name
            assertThat(resultWithPercent.getData())
                .filteredOn(k -> k.getId().equals(kindContainsRa.getId().getId())) // ralley
                .hasSize(1);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_honor_vorname_search_prefix_behavior() {
            // ARRANGE
            var uuid = UUID.randomUUID();

            // Create test data with specific first names for testing the prefix behavior
            Kind kindStartsWithMa = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Mueller_" + uuid, "Martin_" + uuid));
            kindService.persistWithoutDuplicateCheck(kindStartsWithMa);

            Kind kindContainsMa = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Weber_" + uuid, "Thomas_" + uuid));
            kindService.persistWithoutDuplicateCheck(kindContainsMa);

            // ACT TEST CASE 1: Search without % prefix - should only match first names starting with "Ma"
            SearchJsonFiltersInnerSpec vornameFilterWithoutPercent = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.VORNAME, "Ma",
                FilterCombinationSpec.SINGLE);
            SearchJsonSpec searchSpecWithoutPercent = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(vornameFilterWithoutPercent)
            );

            PageResultJsonSearchSummaryJsonSpec resultWithoutPercent = api
                .searchResourceFindKinderPaginated()
                .body(searchSpecWithoutPercent)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT: Should only find first names starting with "Ma"
            assertThat(resultWithoutPercent.getData())
                .isNotEmpty()
                .allMatch(k -> k.getVorname().startsWith("Ma"));

            assertThat(resultWithoutPercent.getData())
                .filteredOn(k -> k.getId().equals(kindStartsWithMa.getId().getId()))
                .hasSize(1);

            assertThat(resultWithoutPercent.getData())
                .filteredOn(k -> k.getId().equals(kindContainsMa.getId().getId()))
                .isEmpty();

            // TEST CASE 2: Search with % prefix - should match first names containing "ma" anywhere
            SearchJsonFiltersInnerSpec vornameFilterWithPercent = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.VORNAME, "%ma",
                FilterCombinationSpec.SINGLE);
            SearchJsonSpec searchSpecWithPercent = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(vornameFilterWithPercent)
            );

            PageResultJsonSearchSummaryJsonSpec resultWithPercent = api
                .searchResourceFindKinderPaginated()
                .body(searchSpecWithPercent)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT: Should find first names containing "ma" anywhere (case insensitive)
            assertThat(resultWithPercent.getData())
                .isNotEmpty()
                .anyMatch(k -> k.getVorname().toLowerCase().contains("ma"));

            // Should find the test kind with "Martin"
            assertThat(resultWithPercent.getData())
                .filteredOn(k -> k.getId().equals(kindStartsWithMa.getId().getId()))
                .hasSize(1);

            // Should also find the one with "Thomas" (contains "ma")
            assertThat(resultWithPercent.getData())
                .filteredOn(k -> k.getId().equals(kindContainsMa.getId().getId()))
                .hasSize(1);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_klassenstufe() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            List<Kind> kinds = insertKnownTestKinder(uuid);
            var primarklssenKinderIds = kinds.stream()
                .filter(k -> k.getKlasse() != null
                    && k.getKlasse().getKlassenstufe() == Klassenstufe.PRIMAR_KLASSE_4)
                .map(Kind::getId)
                .map(k -> k.getUUID().toString())
                .toList();

            // ACT
            String klassenstufeSearchTerm = Klassenstufe.PRIMAR_KLASSE_4.toString();

            SearchJsonFiltersInnerSpec klassenstufeFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.KLASSENSTUFE, klassenstufeSearchTerm,
                FilterCombinationSpec.SINGLE);

            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, uuid.toString(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(klassenstufeFilter, nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId).containsAll(primarklssenKinderIds);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_klasse() {
            // ARRANGE
            String uuid = UUID.randomUUID().toString();
            String klassenname = "1C " + uuid;
            Klasse myKlasse = fixtureService.createKlasseWithName(Klassenstufe.PRIMAR_KLASSE_1, klassenname);
            Kind klassenkind = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withKlasse(myKlasse)
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName("Klassenkind" + uuid, "Kurt" + uuid));
            kindService.persistWithoutDuplicateCheck(klassenkind);

            // ACT
            SearchJsonFiltersInnerSpec klassenstufeFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.KLASSENAME, klassenname,
                FilterCombinationSpec.SINGLE);

            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(klassenstufeFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId).contains(klassenkind.getId().getId());
        }

        @Test
        @TestAsAdminBS
        @SuppressWarnings("java:S5841")
        void findPaginatedKinder_should_filter_esadIdByPrefixIgnoringWhitespace() {
            // ARRANGE
            String uuid = UUID.randomUUID().toString();
            String esadId = EdokidIdTestGenerator.getNextEdokidId("482").toFormattedString();
            Kind esadKind = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withEsadId(esadId)
                    .withName("Esadkind" + uuid, "Karin" + uuid));
            esadKind = kindService.persistWithoutDuplicateCheck(esadKind);

            // ACT
            // the id is entered without the whitespace it is stored with, the filter is sent the way the
            // Kinder Liste sends it
            String esadIdWithoutWhitespace = esadId.replace(" ", "");
            PageResultJsonSearchSummaryJsonSpec result = searchKinderByEsadId(esadIdWithoutWhitespace);

            // ASSERT
            assertThat(result.getData())
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(esadKind.getId().getId());

            // ACT
            // a part out of the middle of the id must not match anymore, only ids starting with the search term
            String middleOfEsadId = esadId.substring(3);
            PageResultJsonSearchSummaryJsonSpec resultForMiddlePart = searchKinderByEsadId(middleOfEsadId);

            // ASSERT
            assertThat(resultForMiddlePart.getData())
                .extracting(SearchSummaryJsonSpec::getId)
                .doesNotContain(esadKind.getId().getId());
        }

        @NonNull
        private PageResultJsonSearchSummaryJsonSpec searchKinderByEsadId(@NonNull String searchTerm) {
            SearchJsonFiltersInnerSpec esadIdFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ESAD_ID, searchTerm,
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(esadIdFilter)
            );

            return api
                .searchResourceFindKinderPaginated()
                .body(searchSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_schulname() {
            // ARRANGE
            List<Kind> kinds = insertSchulTestKinder();
            var resultKids = kinds.stream()
                .filter(k -> k.getSchule() != null && k.getSchule().getSchulhaus().equals("Klein"))
                .map(Kind::getId)
                .map(k -> k.getUUID().toString())
                .toList();

            // ACT
            String klassenstufeSearchTerm = "Klein";

            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.SCHULNAME, klassenstufeSearchTerm,
                FilterCombinationSpec.OR);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId).hasSameElementsAs(resultKids);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_schulid() {
            // ARRANGE
            Schule schule1 = fixtureService.createSchule("Test 1");
            Schule schule2 = fixtureService.createSchule("Test 2");

            Kind kind1 = fixtureService.createKind(schule1);
            Kind kind2 = fixtureService.createKind(schule2);

            // ACT
            String schuleIdSearchTerm = Objects.requireNonNull(kind1.getSchule()).getId().getId();

            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.SCHULE_ID, schuleIdSearchTerm,
                FilterCombinationSpec.OR);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count == 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .hasSameElementsAs(List.of(kind1.getId().getId()));
            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .noneMatch(id -> id.equals(kind2.getId().getId()));
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_notiz_status_and_benutzer() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
            benutzerService.persist(benutzer);
            insertKnownTestKinderWithNotizen(uuid, benutzer);

            // ACT
            SearchJsonFiltersInnerSpec statusFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NOTIZ_STATUS, NotizStatusSpec.ABGESCHLOSSEN.toString(),
                FilterCombinationSpec.AND);
            SearchJsonFiltersInnerSpec benutzerFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NOTIZ_VERANTWORTLICHER_ID, benutzer.getId().getId(),
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(statusFilter, benutzerFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count == 2);
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_notiz_labels() {
            // ARRANGE
            var uuid = UUID.randomUUID();

            Label label1 = LabelBuilder.defaultLabel();
            Label label2 = LabelBuilder.create(labelBuilder -> labelBuilder.withID(ID.random(Label.class))
                .withName("Spezifisches Label"));
            labelService.persist(label1);
            labelService.persist(label2);

            ArrayList<Label> labels = new ArrayList<>();
            labels.add(label1);
            labels.add(label2);

            var kinder = insertKnownTestKinderWithNotizenAndLabels(uuid, labels);

            // ACT
            SearchJsonFiltersInnerSpec zeitraumLabels = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NOTIZ_LABEL,
                labels.stream().map(l -> l.getId().getId()).collect(Collectors.joining(" ")),
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(zeitraumLabels)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count == 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(kinder.getLast().getId().getId());

        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_sort() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            var createdKinder = insertManyForSorttest(20, uuid);
            Assertions.assertThat(createdKinder).hasSize(20);

            // ACT

            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, "Multitest_" + uuid.toString(),
                FilterCombinationSpec.OR);
            SearchJsonFiltersInnerSpec vornameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.VORNAME, "Marco_",
                FilterCombinationSpec.OR);
            SearchJsonFiltersInnerSpec geburtsdatum = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.GEBURTSDATUM, "2020-02-02",
                FilterCombinationSpec.OR);

            SearchJsonSortsInnerSpec nameSort =
                new SearchJsonSortsInnerSpec(SearchColumnsSpec.NAME, SortDirectionSpec.DESC);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, 10, true),
                List.of(nameSort),
                List.of(nameFilter, vornameFilter, geburtsdatum)
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
                .returns(10, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 20);

            assertThat(result.getData())
                .isNotEmpty()
                .isSortedAccordingTo((k1, k2) -> k1.getName().compareTo(k2.getName()) * -1)
                .hasSize(10);

        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_erweiterte_filter_mpa() {

            UUID testrunUUID = UUID.randomUUID();

            // ARRANGE
            Organisation organisation = organisationService.getZentraleOrganisation();
            Benutzer mpaSchule = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.MPA))));
            Benutzer mpaDossier = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.MPA))));

            Schule schule = schuleService.persist(SchuleBuilder.create(builder -> builder
                .withZustaendigMPA(mpaSchule)));
            Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
            Kind kind1 =
                kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)
                    .withName("kindWithDossierLevelMPA " + testrunUUID.toString(), "Karl")));
            Kind kind2 =
                kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)
                    .withName("kindWithMPAThroughSchule " + testrunUUID.toString(), "Susanne")));

            var dossierWithMPA = DossierBuilder.create(builder -> builder
                .withZustaendigMPA(mpaDossier)
                .forKind(kind1)
            );
            dossierService.persist(dossierWithMPA);

            var dossierWithMPAThroughSchule = DossierBuilder.create(builder -> builder
                .forKind(kind2)
            );
            dossierService.persist(dossierWithMPAThroughSchule);

            Kind kind3 = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withName(
                testrunUUID.toString()
                    + " kindWithoutAssignment", "Klaus")));
            var dossiwerWithoutMPA = DossierBuilder.create(builder -> builder
                .forKind(kind3)
            );
            dossierService.persist(dossiwerWithoutMPA);

            // ACT search MPA through Dossier
            String mpaIdSearchTerm = mpaDossier.getId().getId();

            SearchJsonFiltersInnerSpec filterTerm = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_MPA_ID, mpaIdSearchTerm,
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(filterTerm)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT that Dossier with given mpa was found
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(dossierWithMPA.getKind().getId().getId())
                .doesNotContainAnyElementsOf(List.of(
                    dossiwerWithoutMPA.getKind().getId().getId(),
                    dossierWithMPAThroughSchule.getKind().getId().getId())
                );

            // ACT search MPA through Schule
            String mpaThroughSchuleSearchIdTerm = mpaSchule.getId().getId();

            SearchJsonFiltersInnerSpec mpaTrhoughSchuleFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_MPA_ID, mpaThroughSchuleSearchIdTerm,
                FilterCombinationSpec.AND);
            SearchJsonSpec searchForMpa = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(mpaTrhoughSchuleFilter)
            );

            PageResultJsonSearchSummaryJsonSpec schuleMpaResult = api
                .searchResourceFindKinderPaginated()
                .body(searchForMpa)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT that Dossier with given mpa was found
            assertThat(schuleMpaResult)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(schuleMpaResult.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(dossierWithMPAThroughSchule.getKind().getId().getId())
                .doesNotContainAnyElementsOf(List.of(
                    dossiwerWithoutMPA.getKind().getId().getId(),
                    dossierWithMPA.getKind().getId().getId())
                );

            // ACT search MPA not_assigned
            String notAssignedTerm = SearchColumns.NOT_ASSIGNED;

            SearchJsonFiltersInnerSpec mpaUnassignedFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_MPA_ID, notAssignedTerm,
                FilterCombinationSpec.AND);
            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, testrunUUID.toString(),
                FilterCombinationSpec.AND
            );
            SearchJsonSpec mpaUnassignedSearch = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(mpaUnassignedFilter, nameFilter)
                // add name filter to make sure we only match kinder of this testrun
            );

            PageResultJsonSearchSummaryJsonSpec mpaUnassignedResult = api
                .searchResourceFindKinderPaginated()
                .body(mpaUnassignedSearch)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT that Dossier with given mpa was found
            assertThat(mpaUnassignedResult)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(mpaUnassignedResult.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(dossiwerWithoutMPA.getKind().getId().getId())
                .doesNotContainAnyElementsOf(List.of(
                    dossierWithMPAThroughSchule.getKind().getId().getId(),
                    dossierWithMPA.getKind().getId().getId())
                );
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_erweiterte_filter_arzt() {

            UUID testrunUUID = UUID.randomUUID();

            // ARRANGE
            Organisation organisation = organisationService.getZentraleOrganisation();

            Benutzer arztSchule = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.SCHULAERZTIN))));
            Benutzer arztDossier = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.SCHULAERZTIN))));

            Schule schule = schuleService.persist(SchuleBuilder.create(builder -> builder
                .withZustaendigArzt(arztSchule)));
            Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
            Kind kind1 =
                kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)
                    .withName("kindWithDossierLevelArzt " + testrunUUID.toString(), "Armin")));
            Kind kind2 =
                kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)
                    .withName("kindWithArztThroughSchule " + testrunUUID.toString(), "Susanne")));

            var dossierWithArzt = DossierBuilder.create(builder -> builder
                .withZustaendigArzt(arztDossier)
                .forKind(kind1)
            );
            dossierService.persist(dossierWithArzt);

            var dossierWithArztThroughSchule = DossierBuilder.create(builder -> builder
                .forKind(kind2)
            );
            dossierService.persist(dossierWithArztThroughSchule);

            Kind kind3 = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withName(
                testrunUUID.toString()
                    + " kindWithoutAssignment", "Klaus")));
            var dossiwerWithoutArzt = DossierBuilder.create(builder -> builder
                .forKind(kind3)
            );
            dossierService.persist(dossiwerWithoutArzt);

            // ACT search Arzt through Dossier
            String arztIdSearchTerm = arztDossier.getId().getId();

            SearchJsonFiltersInnerSpec filterTerm = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_ARZT_ID, arztIdSearchTerm,
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(filterTerm)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchSpec)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT that Dossier with given arzt was found
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(dossierWithArzt.getKind().getId().getId())
                .doesNotContainAnyElementsOf(List.of(
                    dossiwerWithoutArzt.getKind().getId().getId(),
                    dossierWithArztThroughSchule.getKind().getId().getId())
                );

            // ACT search MPA through Schule
            String arztThroughSchuleSearchIdTerm = arztSchule.getId().getId();

            SearchJsonFiltersInnerSpec arztTrhoughSchuleFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_ARZT_ID, arztThroughSchuleSearchIdTerm,
                FilterCombinationSpec.AND);
            SearchJsonSpec searchForMpa = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(arztTrhoughSchuleFilter)
            );

            PageResultJsonSearchSummaryJsonSpec schuleArztResult = api
                .searchResourceFindKinderPaginated()
                .body(searchForMpa)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT that Dossier with given arzt was found
            assertThat(schuleArztResult)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(schuleArztResult.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(dossierWithArztThroughSchule.getKind().getId().getId())
                .doesNotContainAnyElementsOf(List.of(
                    dossiwerWithoutArzt.getKind().getId().getId(),
                    dossierWithArzt.getKind().getId().getId())
                );

            // ACT search MPA not_assigned
            String notAssignedTerm = SearchColumns.NOT_ASSIGNED;

            SearchJsonFiltersInnerSpec arztUnassignedFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_ARZT_ID, notAssignedTerm,
                FilterCombinationSpec.AND);
            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, testrunUUID.toString(),
                FilterCombinationSpec.AND
            );
            SearchJsonSpec arztUnassignedSearch = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(arztUnassignedFilter, nameFilter)
                // add name filter to make sure we only find kidner of this testrun
            );

            PageResultJsonSearchSummaryJsonSpec mpaUnassignedResult = api
                .searchResourceFindKinderPaginated()
                .body(arztUnassignedSearch)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT that Dossier with given mpa was found
            assertThat(mpaUnassignedResult)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(mpaUnassignedResult.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(dossiwerWithoutArzt.getKind().getId().getId())
                .doesNotContainAnyElementsOf(List.of(
                    dossierWithArztThroughSchule.getKind().getId().getId(),
                    dossierWithArzt.getKind().getId().getId())
                );

        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_erweiterte_filter_immunitaeten() {
            // ARRANGE
            var dossierVarizellen = DossierBuilder.create(builder -> builder
                .withImmunitaetVarizellen(true)
                .withImmunitaetMasern(false)
                .forKind(fixtureService.createKind())
            );
            dossierService.persist(dossierVarizellen);

            var dossierMasern = DossierBuilder.create(builder -> builder
                .withImmunitaetVarizellen(false)
                .withImmunitaetMasern(true)
                .forKind(fixtureService.createKind())
            );
            dossierService.persist(dossierMasern);

            var dossierBoth = DossierBuilder.create(builder -> builder
                .withImmunitaetVarizellen(true)
                .withImmunitaetMasern(true)
                .forKind(fixtureService.createKind())
            );
            dossierService.persist(dossierBoth);

            var dossierNone = DossierBuilder.create(builder -> builder
                .withImmunitaetVarizellen(false)
                .withImmunitaetMasern(false)
                .forKind(fixtureService.createKind())
            );
            dossierService.persist(dossierNone);

            List<Kind> allKinder = List.of(
                dossierBoth.getKind(),
                dossierVarizellen.getKind(),
                dossierMasern.getKind(),
                dossierNone.getKind());
            List<String> allKinderIds = allKinder.stream().map(Kind::getId).map(ID::getId).toList();
            List<Kind> masernImmunKinder = List.of(dossierMasern.getKind(), dossierBoth.getKind());
            List<String> masernImmunKinderIds = masernImmunKinder.stream().map(Kind::getId).map(ID::getId).toList();
            List<Kind> varizellenImmunKinder = List.of(dossierVarizellen.getKind(), dossierBoth.getKind());
            List<String> varizellenImmunKinderIds =
                varizellenImmunKinder.stream().map(Kind::getId).map(ID::getId).toList();

            // ACT Masern
            String masernImmunSearchTerm = "true";
            SearchJsonFiltersInnerSpec masernFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_IMMUNITAET_MASERN, masernImmunSearchTerm,
                FilterCombinationSpec.AND);

            SearchJsonSpec masernSearch = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(masernFilter)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(masernSearch)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT Masern
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(masernImmunKinderIds.toArray(new String[0]))
                .doesNotContainAnyElementsOf(allKinderIds.stream()
                    .filter(id -> !masernImmunKinderIds.contains(id))
                    .toList());

            // ACT Varizellen

            SearchJsonFiltersInnerSpec varizellenFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_IMMUNITAET_VARIEZELLEN, "true",
                FilterCombinationSpec.AND);

            SearchJsonSpec varizellenSearch = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(varizellenFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultVarizellen = api
                .searchResourceFindKinderPaginated()
                .body(varizellenSearch)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT Varizellen
            assertThat(resultVarizellen)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(resultVarizellen.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(varizellenImmunKinderIds.toArray(new String[0]))
                .doesNotContainAnyElementsOf(allKinderIds.stream()
                    .filter(id -> !varizellenImmunKinderIds.contains(id))
                    .toList());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_erweiterte_filter_inaktivstatus_inaktive() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            List<Kind> knownKinder = insertKnownTestKinder(uuid);
            Kind inaktivesKind =
                knownKinder.stream().filter(k -> isInaktiv(k.getKindAktivStatus())).collect(
                    MoreCollectors.onlyElement());

            // ACT
            SearchJsonFiltersInnerSpec inaktivFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.INAKTIV, "true",
                FilterCombinationSpec.AND);
            // add name filter to make sure we only find kinder from this run as they have the uuid in the name
            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, uuid.toString(),
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(inaktivFilter, nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .contains(inaktivesKind.getId().getId())
                .doesNotContainAnyElementsOf(knownKinder.stream()
                    .map(k -> k.getId().getId())
                    .filter(id -> !id.equals(inaktivesKind.getId().getId()))
                    .toList()
                );
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_erweiterte_filter_inaktivstatus_aktive() {
            // ARRANGE
            var uuid = UUID.randomUUID();
            List<Kind> knownKinder = insertKnownTestKinder(uuid);
            Kind inaktivesKind =
                knownKinder.stream().filter(k -> isInaktiv(k.getKindAktivStatus())).collect(
                    MoreCollectors.onlyElement());

            // ACT
            SearchJsonFiltersInnerSpec inaktivFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.INAKTIV, "false",
                FilterCombinationSpec.AND);
            // add name filter to make sure we only find kinder from this run as they have the uuid in the name
            SearchJsonFiltersInnerSpec nameFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.NAME, uuid.toString(),
                FilterCombinationSpec.AND);
            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(inaktivFilter, nameFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(result.getData()).extracting(SearchSummaryJsonSpec::getId)
                .doesNotContainAnyElementsOf(List.of(inaktivesKind.getId().getId()))
                .containsAll(knownKinder.stream()
                    .map(k -> k.getId().getId())
                    .filter(id -> !id.equals(inaktivesKind.getId().getId()))
                    .toList()
                );

        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_impfungen() {
            // ARRANGE

            Dossier dossierWithoutImpfungen = fixtureService.createDossier();
            Kind kindWithoutImpfung = dossierWithoutImpfungen.getKind();

            Krankheit[] krankheiten = { Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN };
            Dossier dossierWithInterneImpfung = insertDossierWithInterneImpfung(
                LocalDate.of(2023, 6, 20),
                krankheiten).getLeft();
            Kind kindWithMasernImpfung = dossierWithInterneImpfung.getKind();

            Dossier dossierWithHPVInterneImfung = insertDossierWithInterneImpfung(
                LocalDate.of(2023, 6, 21),
                Krankheit.HPV).getLeft();
            Kind kindWithHpvImpfung = dossierWithHPVInterneImfung.getKind();

            // ACT search by krankheit
            SearchJsonFiltersInnerSpec krankheitFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.IMPFUNG_KRANKHEIT, KrankheitSpec.MASERN.toString(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(krankheitFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that the Kind with Masernimpfung is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kindWithMasernImpfung.getId().getId())
                .doesNotContain(kindWithoutImpfung.getId().getId())
                .doesNotContain(kindWithHpvImpfung.getId().getId());

            // ACT search by date
            SearchJsonFiltersInnerSpec dateImpfungFilterVon = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.IMPFUNG_ZEITRAUM_VON,
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN).format(LocalDate.of(2023, 6, 20)),
                FilterCombinationSpec.AND);
            SearchJsonFiltersInnerSpec dateImpfungFilterBis = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.IMPFUNG_ZEITRAUM_BIS,
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN).format(LocalDate.of(2023, 6, 20)),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByDatum = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(dateImpfungFilterVon, dateImpfungFilterBis)
            );

            PageResultJsonSearchSummaryJsonSpec resultByDatum = api
                .searchResourceFindKinderPaginated()
                .body(searchByDatum)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(resultByDatum)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that the Kind with Masernimpfung is returned
            assertThat(resultByDatum.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kindWithMasernImpfung.getId().getId())
                .doesNotContain(kindWithoutImpfung.getId().getId())
                .doesNotContain(kindWithHpvImpfung.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_zeitraum() {
            // ARRANGE
            LocalDate date1 = LocalDate.of(2023, 6, 20);
            LocalDate date2 = LocalDate.of(2023, 7, 21);

            // Create kind with anlass on date1
            Dossier dossier1 = fixtureService.createDossier();
            Kind kind1 = dossier1.getKind();
            Anlass anlass1 = fixtureService.createAnlass(date1);
            fixtureService.createEinladung(anlass1, dossier1);

            // Create kind with anlass on date2
            Dossier dossier2 = fixtureService.createDossier();
            Kind kind2 = dossier2.getKind();
            Anlass anlass2 = fixtureService.createAnlass(date2);
            fixtureService.createEinladung(anlass2, dossier2);

            // Create kind with no anlass
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT search by date range (from date1 to date1)
            SearchJsonFiltersInnerSpec dateAnlassFilterVon = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_ZEITRAUM_VON,
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN).format(date1),
                FilterCombinationSpec.AND);
            SearchJsonFiltersInnerSpec dateAnlassFilterBis = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_ZEITRAUM_BIS,
                DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN).format(date1),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByDatum = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(dateAnlassFilterVon, dateAnlassFilterBis)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchByDatum)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that only kind1 with anlass on date1 is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_schuljahr() {
            // ARRANGE
            int schuljahr2023 = 2023;


            // Create kind with anlass in schuljahr 2023
            Dossier dossier1 = fixtureService.createDossier();
            Kind kind1 = dossier1.getKind();
            Anlass anlass1 = fixtureService.createAnlass(LocalDate.of(2023, 9, 1));
            fixtureService.createEinladung(anlass1, dossier1);

            // Create kind with anlass in schuljahr 2024
            Dossier dossier2 = fixtureService.createDossier();
            Kind kind2 = dossier2.getKind();
            Anlass anlass2 = fixtureService.createAnlass(LocalDate.of(2024, 9, 1));
            fixtureService.createEinladung(anlass2, dossier2);

            // Create kind with no anlass
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT search by schuljahr 2023
            SearchJsonFiltersInnerSpec schuljahrFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_SCHULJAHR,
                String.valueOf(schuljahr2023),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchBySchuljahr = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(schuljahrFilter)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchBySchuljahr)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that only kind1 with anlass in schuljahr 2023 is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_status() {
            // ARRANGE
            // Create kind with anlass in status AUFGEBOTEN
            Dossier dossier1 = fixtureService.createDossier();
            Kind kind1 = dossier1.getKind();
            Anlass anlass1 = fixtureService.createAnlass(AnlassStatus.AUFGEBOTEN);
            fixtureService.createEinladung(anlass1, dossier1);

            // Create kind with anlass in status BEREIT
            Dossier dossier2 = fixtureService.createDossier();
            Kind kind2 = dossier2.getKind();
            Anlass anlass2 = fixtureService.createAnlass(AnlassStatus.BEREIT);
            fixtureService.createEinladung(anlass2, dossier2);

            // Create kind with no anlass
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT search by status AUFGEBOTEN
            SearchJsonFiltersInnerSpec statusFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_STATUS,
                AnlassStatus.AUFGEBOTEN.toString(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByStatus = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(statusFilter)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchByStatus)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that only kind1 with anlass in status AUFGEBOTEN is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_mpa_arzt_weitere_person() {
            // ARRANGE
            // Create benutzer
            Organisation organisation = organisationService.getZentraleOrganisation();
            Benutzer arzt1 = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.SCHULAERZTIN))));
            Benutzer arzt2 = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.SCHULAERZTIN))));
            Benutzer mpa1 = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.MPA))));
            Benutzer mpa2 = benutzerService.persist(BenutzerBuilder.create(
                builder -> builder.withOrganisationRollen(organisation, List.of(
                    FachRolle.MPA))));

            Benutzer weiterePerson1 = fixtureService.createBenutzerFuerZentraleOrganisation();
            Benutzer weiterePerson2 = fixtureService.createBenutzerFuerZentraleOrganisation();

            // Create kind with anlass with mpa1, arzt1, weiterePerson1
            Dossier dossier1 = fixtureService.createDossier();
            Kind kind1 = dossier1.getKind();
            Anlass anlass1 = anlassService.persist(AnlassBuilder.create(builder -> builder
                .withTitle("Test Anlass 1")
                .withDatumSetSchuljahr(LocalDate.of(2023, 5, 5))
                .withMPA(mpa1)
                .withArzt(arzt1)
                .withWeiterePerson(weiterePerson1)
                .withAnlassConfig(fixtureService.createAnlassConfigAllOptional())));
            fixtureService.createEinladung(anlass1, dossier1);

            // Create kind with anlass with mpa2, arzt2, weiterePerson2
            Dossier dossier2 = fixtureService.createDossier();
            Kind kind2 = dossier2.getKind();
            Anlass anlass2 = anlassService.persist(AnlassBuilder.create(builder -> builder
                .withTitle("Test Anlass 2")
                .withDatumSetSchuljahr(LocalDate.of(2023, 5, 5))
                .withMPA(mpa2)
                .withArzt(arzt2)
                .withWeiterePerson(weiterePerson2)
                .withAnlassConfig(fixtureService.createAnlassConfigAllOptional())));
            fixtureService.createEinladung(anlass2, dossier2);

            // Create kind with no anlass
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT search by MPA
            SearchJsonFiltersInnerSpec mpaFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_MPA_ID,
                mpa1.getId().getId(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByMpa = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(mpaFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultMpa = api
                .searchResourceFindKinderPaginated()
                .body(searchByMpa)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT MPA filter
            assertThat(resultMpa.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());

            // ACT search by Arzt
            SearchJsonFiltersInnerSpec arztFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_ARZT_ID,
                arzt1.getId().getId(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByArzt = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(arztFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultArzt = api
                .searchResourceFindKinderPaginated()
                .body(searchByArzt)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT Arzt filter
            assertThat(resultArzt.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());

            // ACT search by Weitere Person
            SearchJsonFiltersInnerSpec weiterePersonFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_WEITERE_PERSONEN,
                weiterePerson1.getId().getId(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByWeiterePerson = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(weiterePersonFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultWeiterePerson = api
                .searchResourceFindKinderPaginated()
                .body(searchByWeiterePerson)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT Weitere Person filter
            assertThat(resultWeiterePerson.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_teilnahme_status() {
            // ARRANGE
            // Create kind with anlass and einladung with status TEILNAHME_BESTAETIGT
            Dossier dossier1 = fixtureService.createDossier();
            Kind kind1 = dossier1.getKind();
            Anlass anlass1 = fixtureService.createAnlass();
            einladungService.persist(EinladungBuilder.create(builder -> builder
                .withAnlass(anlass1)
                .withDossier(dossier1)
                .withStatus(EinladungStatus.TEILNAHME_BESTAETIGT)));

            // Create kind with anlass and einladung with status TEILNAHME_ABGESAGT
            Dossier dossier2 = fixtureService.createDossier();
            Kind kind2 = dossier2.getKind();
            Anlass anlass2 = fixtureService.createAnlass();
            einladungService.persist(EinladungBuilder.create(builder -> builder
                .withAnlass(anlass2)
                .withDossier(dossier2)
                .withStatus(EinladungStatus.TEILNAHME_ABGESAGT)));

            // Create kind with no anlass
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT search by teilnahme status TEILNAHME_BESTAETIGT
            SearchJsonFiltersInnerSpec teilnahmeStatusFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_TEILNAHME_STATUS,
                EinladungStatus.TEILNAHME_BESTAETIGT.toString(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByTeilnahmeStatus = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(teilnahmeStatusFilter)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchByTeilnahmeStatus)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(result)
                .returns(1, PageResultJsonSearchSummaryJsonSpec::getPage)
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that only kind1 with einladung status TEILNAHME_BESTAETIGT is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_impfungen_charge() {
            // ARRANGE

            Dossier dossierWithoutImpfungen = fixtureService.createDossier();
            Kind kindWithoutImpfung = dossierWithoutImpfungen.getKind();

            Krankheit[] krankheiten = { Krankheit.MASERN, Krankheit.MUMPS, Krankheit.ROETELN };
            Pair<Dossier, UntersuchungImpfungIntern> testdateMasern = insertDossierWithInterneImpfung(
                LocalDate.of(2023, 6, 20),
                krankheiten);
            Kind kindWithMasernImpfung = testdateMasern.getLeft().getKind();
            UntersuchungImpfungIntern masernImpfung = testdateMasern.getRight();
            ImpfstoffCharge charge = masernImpfung.getCharge();
            Assertions.assertThat(charge).isNotNull();

            Pair<Dossier, UntersuchungImpfungIntern> testdatenHpv = insertDossierWithInterneImpfung(
                LocalDate.of(2023, 6, 21),
                Krankheit.HPV);
            Kind kindWithHpvImpfung = testdatenHpv.getLeft().getKind();

            // ACT search by krankheit
            SearchJsonFiltersInnerSpec chargeFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.IMPFUNG_CHARGE, charge.getId().getId(),
                FilterCombinationSpec.AND);

            SearchJsonSpec searchSpec = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(chargeFilter)
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
                .returns(MAX_PAGE_SIZE, PageResultJsonSearchSummaryJsonSpec::getMaxPageSize)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that the Kind with Masernimpfung is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kindWithMasernImpfung.getId().getId())
                .doesNotContain(kindWithoutImpfung.getId().getId())
                .doesNotContain(kindWithHpvImpfung.getId().getId());

        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_impfung_massnahme_vormerkung_folgetermin() {
            // ARRANGE
            // Create kind with Anlass and Untersuchung with vormerkenFuerGruppenFolgetermin = true
            Untersuchung untersuchung = fixtureService.createUntersuchungWithImpfungen();
            Kind kind1 = untersuchung.getEinladungRequired().getDossier().getKind();

            UntersuchungImpfungenMassnahmen impfMassnahmenWithVormerkung =
                UntersuchungImpfungenMassnahmenBuilder.create(
                    b ->
                        b.forUntersuchungImpfungen(Objects.requireNonNull(untersuchung.getUntersuchungImpfungen()))
                            .withVormerkenFuerGruppenFolgetermin(true));
            untersuchungImpfungenMassnahmenService.persist(impfMassnahmenWithVormerkung);

            // Create kind with anlass and untersuchung with vormerkenFuerGruppenFolgetermin = false
            Untersuchung untersuchungNoFolgetermin = fixtureService.createUntersuchungWithImpfungen();
            Kind kind2 = untersuchungNoFolgetermin.getEinladungRequired().getDossier().getKind();

            UntersuchungImpfungenMassnahmen impfMassnahmenNoVormerkung =
                UntersuchungImpfungenMassnahmenBuilder.create(
                    b ->
                        b.forUntersuchungImpfungen(Objects.requireNonNull(untersuchungNoFolgetermin.getUntersuchungImpfungen()))
                            .withVormerkenFuerGruppenFolgetermin(false));
            untersuchungImpfungenMassnahmenService.persist(impfMassnahmenNoVormerkung);

            // Create kind with no anlass
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT - search for children with vormerkenFuerGruppenFolgetermin = true
            SearchJsonFiltersInnerSpec vormerkungFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_IMPFUNG_MASSNAHME_VORMERKUNG_FOLGETERMIN,
                "true",
                FilterCombinationSpec.SINGLE);

            SearchJsonSpec searchByVormerkung = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(vormerkungFilter)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchByVormerkung)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(result)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that only kind1 with vormerkenFuerGruppenFolgetermin = true is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());

            // ACT - search for children with vormerkenFuerGruppenFolgetermin = false
            SearchJsonFiltersInnerSpec noVormerkungFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_IMPFUNG_MASSNAHME_VORMERKUNG_FOLGETERMIN,
                "false",
                FilterCombinationSpec.SINGLE);

            SearchJsonSpec searchByNoVormerkung = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(noVormerkungFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultFalse = api
                .searchResourceFindKinderPaginated()
                .body(searchByNoVormerkung)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT for false case
            assertThat(resultFalse.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind2.getId().getId())
                .doesNotContain(kind1.getId().getId())
                .doesNotContain(kind3.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_anlass_not_assigned() {
            // ARRANGE
            // Create kind with anlass assignment
            Dossier dossierWithAnlass = fixtureService.createDossier();
            Kind kindWithAnlass = dossierWithAnlass.getKind();
            Anlass anlass = fixtureService.createAnlass();
            fixtureService.createEinladung(anlass, dossierWithAnlass);

            // Create kind without anlass assignment
            Dossier dossierWithoutAnlass = fixtureService.createDossier();
            Kind kindWithoutAnlass = dossierWithoutAnlass.getKind();

            // Create another kind without anlass assignment
            Dossier dossierWithoutAnlass2 = fixtureService.createDossier();
            Kind kindWithoutAnlass2 = dossierWithoutAnlass2.getKind();

            // ACT - search for children with NO anlass assignment (noAnlassAssigned = true)
            SearchJsonFiltersInnerSpec noAnlassFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_NOT_ASSIGNED,
                "true",
                FilterCombinationSpec.AND);

            SearchJsonSpec searchNoAnlass = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(noAnlassFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultNoAnlass = api
                .searchResourceFindKinderPaginated()
                .body(searchNoAnlass)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT - should return children without anlass assignment
            assertThat(resultNoAnlass)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 2);

            assertThat(resultNoAnlass.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kindWithoutAnlass.getId().getId(), kindWithoutAnlass2.getId().getId())
                .doesNotContain(kindWithAnlass.getId().getId());

            // ACT - search for children WITH anlass assignment (noAnlassAssigned = false)
            SearchJsonFiltersInnerSpec hasAnlassFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.ANLASS_NOT_ASSIGNED,
                "false",
                FilterCombinationSpec.AND);

            SearchJsonSpec searchHasAnlass = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(hasAnlassFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultHasAnlass = api
                .searchResourceFindKinderPaginated()
                .body(searchHasAnlass)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT - should return children with anlass assignment
            assertThat(resultHasAnlass)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            assertThat(resultHasAnlass.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kindWithAnlass.getId().getId())
                .doesNotContain(kindWithoutAnlass.getId().getId())
                .doesNotContain(kindWithoutAnlass2.getId().getId());
        }

        @Test
        @TestAsAdminBS
        void findPaginatedKinder_should_filter_dossier_nachkontrolle() {
            // ARRANGE
            // Create kind with anlass and dossier with nachkontrolle = true
            Dossier dossier1 = fixtureService.createDossier();
            fixtureService.createNachkontrolle(dossier1, true);
            Kind kind1 = dossier1.getKind();

            // Create kind with anlass and dossier with nachkontrolle = false
            Dossier dossier2 = fixtureService.createDossier();
            fixtureService.createNachkontrolle(dossier2, false);
            Kind kind2 = dossier2.getKind();

            // Create kind with no nachkontrolle
            Dossier dossier3 = fixtureService.createDossier();
            Kind kind3 = dossier3.getKind();

            // ACT - search for children with nachkontrolle = true
            SearchJsonFiltersInnerSpec nachkontrolleFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_NACHKONTROLLE,
                "true",
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByNachkontrolle = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(nachkontrolleFilter)
            );

            PageResultJsonSearchSummaryJsonSpec result = api
                .searchResourceFindKinderPaginated()
                .body(searchByNachkontrolle)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT
            assertThat(result)
                .extracting(PageResultJsonSearchSummaryJsonSpec::getTotalFilteredCount)
                .matches(count -> count >= 1);

            // Verify that only kind1 with nachkontrolle = true is returned
            assertThat(result.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind1.getId().getId())
                .doesNotContain(kind2.getId().getId())
                .doesNotContain(kind3.getId().getId());

            // ACT - search for children with nachkontrolle = false
            SearchJsonFiltersInnerSpec noNachkontrolleFilter = new SearchJsonFiltersInnerSpec(
                SearchColumnsSpec.DOSSIER_NACHKONTROLLE,
                "false",
                FilterCombinationSpec.AND);

            SearchJsonSpec searchByNoNachkontrolle = new SearchJsonSpec(
                new PageRequestJsonSpec(1L, MAX_PAGE_SIZE, true),
                Collections.emptyList(),
                List.of(noNachkontrolleFilter)
            );

            PageResultJsonSearchSummaryJsonSpec resultFalse = api
                .searchResourceFindKinderPaginated()
                .body(searchByNoNachkontrolle)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(PageResultJsonSearchSummaryJsonSpec.class);

            // ASSERT for false case
            assertThat(resultFalse.getData())
                .isNotEmpty()
                .extracting(SearchSummaryJsonSpec::getId)
                .contains(kind2.getId().getId())
                .contains(kind3.getId().getId())
                .doesNotContain(kind1.getId().getId());
        }

        private Pair<Dossier, UntersuchungImpfungIntern> insertDossierWithInterneImpfung(
            LocalDate datumImpfung,
            Krankheit... krankheiten) {
            Dossier dossier = fixtureService.createDossier();
            Anlass previousAnlass = fixtureService.createAnlass(LocalDate.of(2021, 1, 1));
            Einladung previousEinladung = fixtureService.createEinladung(previousAnlass, dossier);
            Untersuchung previousUntersuchung = fixtureService.createUntersuchungWithImpfungen(previousEinladung);
            Impfstoff impfstoff = fixtureService.createImpfstoff(krankheiten);
            Assertions.assertThat(previousUntersuchung.getUntersuchungImpfungen()).isNotNull();

            Optional<UntersuchungImpfstatusKrankheit> krankheit =
                untersuchungImpfstatusKrankheitService.findByUntersuchungAndKrankheit(
                    previousUntersuchung.getUntersuchungImpfungen()
                        .getId(), Krankheit.MASERN);
            krankheit.ifPresent(untersuchungImpfstatusKrankheit -> untersuchungImpfstatusKrankheit.setAnzahlImpfungen(2));

            untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                    previousUntersuchung.getUntersuchungImpfungen())
                .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
                .withImpfStatus(ImpfStatus.DURCHGEFUEHRT)
                .withImpfstoff(impfstoff)
                .withDatumImpfung(LocalDate.of(2023, 1, 27))));

            UntersuchungImpfungIntern interneImpfung =
                untersuchungImpfungInternService.persist(UntersuchungImpfungInternBuilder.create(builder -> builder.forUntersuchungImpfungen(
                        previousUntersuchung.getUntersuchungImpfungen())
                    .withUserErfasst(fixtureService.createBenutzerFuerZentraleOrganisation())
                    .withImpfstoff(impfstoff)
                    .withCharge(fixtureService.createImpfstoffCharge(
                        impfstoff,
                        "testimpfstoffcharge" + impfstoff.getId()))
                    .withDatumImpfung(datumImpfung)));

            Anlass newAnlass = fixtureService.createAnlass(LocalDate.of(2024, 1, 1));
            Einladung newEinladung = fixtureService.createEinladung(newAnlass, dossier);
            fixtureService.createUntersuchungWithImpfungen(newEinladung);
            return Pair.of(dossier, interneImpfung);
        }

        @NonNull
        private List<Kind> insertSchulTestKinder() {
            Schule schule1 = fixtureService.createSchule("KleinMittel (Gross)");
            Schule schule2 = fixtureService.createSchule("Klein (Gross)");
            Schule schule3 = fixtureService.createSchule("KleinMittel");
            Schule schule4 = fixtureService.createSchule("Klein");

            List<Kind> kinder = new ArrayList<>();
            kinder.add(fixtureService.createKind(schule1));
            kinder.add(fixtureService.createKind(schule2));
            kinder.add(fixtureService.createKind(schule3));
            kinder.add(fixtureService.createKind(schule4));

            return kinder;
        }

        @NonNull
        private List<Kind> insertKnownTestKinder(@NonNull UUID uuid) {
            List<Kind> kinder = new ArrayList<>();
            Kind kind = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "ALLCAPS", uuid.toString() + "ALVIN"));
            kindService.persistWithoutDuplicateCheck(kind);
            kinder.add(kind);

            Kind kind2 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "ralley", uuid.toString() + "Vinie"));
            kindService.persistWithoutDuplicateCheck(kind2);
            kinder.add(kind2);

            Kind kind3 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "Bürgenmeier", uuid.toString() + "Sibilla"));
            kindService.persistWithoutDuplicateCheck(kind3);
            kinder.add(kind3);

            Klasse klasse = fixtureService.createKlasse(Klassenstufe.PRIMAR_KLASSE_4);

            Kind kind4 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withKlasse(klasse)
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "Klassenkind", uuid.toString() + "Kurt"));
            kindService.persistWithoutDuplicateCheck(kind4);
            kinder.add(kind4);
            // inaktives kinde
            Kind kind5 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                    .withGeburtsdatum(LocalDate.of(2020, 5, 23))
                    .withKindAktivStatus(EntityAktivStatus.INAKTIV)
                    .withEsadId(EdokidIdTestGenerator.getNextEdokidId("462").toFormattedString())
                    .withName(uuid + "Inaktiv", uuid + "Irmin"));
            kindService.persistWithoutDuplicateCheck(kind5);
            kinder.add(kind5);
            return kinder;
        }

        @NonNull
        private List<Kind> insertKnownTestKinderWithNotizen(@NonNull UUID uuid, @NonNull Benutzer benutzer) {
            List<Kind> kinder = new ArrayList<>();

            Kind kind = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE
                        .getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "ALLCAPS", "ALVIN" + uuid.toString()));
            kindService.persistWithoutDuplicateCheck(kind);
            Dossier dossier = DossierBuilder.create(
                dossierBuilder -> dossierBuilder.withID(ID.random(Dossier.class)).forKind(kind)
            );
            dossierService.persist(dossier);
            Benutzer benutzerRandom =
                BenutzerBuilder.create(benutzerBuilder -> benutzerBuilder.withID(ID.random(Benutzer.class)));
            benutzerService.persist(benutzerRandom);
            Notiz notiz =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier)
                    .withStatus(NotizStatus.AKTIV)
                    .withTimestampErstellt(OffsetDateTime.of(2021, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withVerantwortlicher(benutzerRandom));
            notizService.persist(notiz);

            kinder.add(kind);

            Kind kind2 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE
                        .getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "ralley", uuid.toString() + "Vinie"));
            kindService.persistWithoutDuplicateCheck(kind2);
            Dossier dossier2 = DossierBuilder.create(
                dossierBuilder -> dossierBuilder.withID(ID.random(Dossier.class)).forKind(kind2)
            );
            dossierService.persist(dossier2);
            Notiz notiz2 =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier2)
                    .withStatus(NotizStatus.ABGESCHLOSSEN)
                    .withTimestampErstellt(OffsetDateTime.of(2021, 2, 12, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withVerantwortlicher(benutzer));
            notizService.persist(notiz2);
            kinder.add(kind2);

            Kind kind3 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE
                        .getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "Bürgenmeier", uuid.toString() + "Sibilla"));
            kindService.persistWithoutDuplicateCheck(kind3);
            Dossier dossier3 = DossierBuilder.create(
                dossierBuilder -> dossierBuilder.withID(ID.random(Dossier.class)).forKind(kind3)
            );
            dossierService.persist(dossier3);
            Notiz notiz3 =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier3)
                    .withStatus(NotizStatus.ABGESCHLOSSEN)
                    .withTimestampErstellt(OffsetDateTime.of(2021, 2, 15, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withVerantwortlicher(benutzerRandom));
            notizService.persist(notiz3);
            kinder.add(kind3);

            Klasse klasse = fixtureService.createKlasse(Klassenstufe.PRIMAR_KLASSE_1);

            Kind kind4 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withKlasse(klasse)
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE
                        .getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "Klassenkind", uuid.toString() + "Kurt"));
            kindService.persistWithoutDuplicateCheck(kind4);
            Dossier dossier4 = DossierBuilder.create(
                dossierBuilder -> dossierBuilder.withID(ID.random(Dossier.class)).forKind(kind4)
            );
            dossierService.persist(dossier4);
            Notiz notiz4 =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier4)
                    .withStatus(NotizStatus.ABGESCHLOSSEN)
                    .withTimestampErstellt(OffsetDateTime.of(2022, 10, 12, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withVerantwortlicher(benutzer));
            notizService.persist(notiz4);
            kinder.add(kind4);
            return kinder;
        }

        @NonNull
        private List<Kind> insertKnownTestKinderWithNotizenAndLabels(@NonNull UUID uuid, @NonNull List<Label> labels) {
            List<Kind> kinder = new ArrayList<>();

            Benutzer benutzerRandom =
                BenutzerBuilder.create(benutzerBuilder -> benutzerBuilder.withID(ID.random(Benutzer.class)));
            benutzerService.persist(benutzerRandom);

            Kind kind = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE
                        .getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "ALLCAPS", uuid.toString() + "ALVIN"));
            kindService.persistWithoutDuplicateCheck(kind);
            Dossier dossier = DossierBuilder.create(
                dossierBuilder -> dossierBuilder.withID(ID.random(Dossier.class)).forKind(kind)
            );
            dossierService.persist(dossier);
            Notiz notiz =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier)
                    .withStatus(NotizStatus.AKTIV)
                    .withLabel(labels.getFirst())
                    .withTimestampErstellt(OffsetDateTime.of(2021, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withVerantwortlicher(benutzerRandom));
            notizService.persist(notiz);

            kinder.add(kind);

            Kind kind2 = KindBuilder.create(
                builder -> builder
                    .withID(ID.random(Kind.class))
                    .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE
                        .getNextSozialversicherungsnummer())
                    .withName(uuid.toString() + "ralley", uuid.toString() + "Vinie"));
            kindService.persistWithoutDuplicateCheck(kind2);
            Dossier dossier2 = DossierBuilder.create(
                dossierBuilder -> dossierBuilder.withID(ID.random(Dossier.class)).forKind(kind2)
            );
            dossierService.persist(dossier2);
            Notiz notiz2 =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier2)
                    .withStatus(NotizStatus.ABGESCHLOSSEN)
                    .withTimestampErstellt(OffsetDateTime.of(2021, 2, 12, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withLabel(labels.getFirst())
                    .withVerantwortlicher(benutzerRandom));
            notizService.persist(notiz2);
            Notiz notiz3 =
                NotizBuilder.create(builder -> builder
                    .withID(ID.random(Notiz.class))
                    .withDossier(dossier2)
                    .withStatus(NotizStatus.ABGESCHLOSSEN)
                    .withTimestampErstellt(OffsetDateTime.of(2021, 2, 12, 12, 0, 0, 0, ZoneOffset.UTC))
                    .withLabel(labels.getLast())
                    .withVerantwortlicher(benutzerRandom));
            notizService.persist(notiz3);
            kinder.add(kind2);

            return kinder;
        }

        @NonNull
        private List<Kind> insertManyForSorttest(int countToInsert, @NonNull UUID uuid) {
            List<Kind> result = new ArrayList<>();
            for (int i = 0; i < countToInsert; i++) {
                int index = i;
                Kind kind = KindBuilder.create(
                    builder -> builder
                        .withID(ID.random(Kind.class))
                        .withGeburtsdatum(LocalDate.of(2020, 2, 2))
                        .withSozialversicherungsNummer(SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer())
                        .withName(
                            "Multitest_" + uuid.toString() + '_' + index,
                            "Marco_" + uuid.toString() + '_' + index));
                kindService.persistWithoutDuplicateCheck(kind);
                result.add(kind);
            }
            return result;
        }

    }
}
