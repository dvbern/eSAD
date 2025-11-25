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

package ch.dvbern.oss.hemed.esc.api.features.search;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.infrastructure.db.SmartJPAQuery;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.QAnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.QImpfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.QImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QNachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.notizen.QNotiz;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.features.search.json.KindSearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.QKindSearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.QSearchSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.search.types.KinderQuickSearchTerm;
import ch.dvbern.oss.hemed.esc.api.features.search.types.SearchColumns;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterFieldJson;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageRequestJson;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import com.google.common.collect.Streams;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class SearchRepo {

    private final Db db;
    private static final BooleanExpression[] EMPTY_BOOLEAN_EXPRESSION = new BooleanExpression[0];

    @NonNull
    public List<SearchSummaryJson> findAllSummary() {
        return getKindSummaryJsonQuery().orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc()).fetch();
    }

    @NonNull
    public List<KindSearchJson> quickSearchKinder(@NonNull KinderQuickSearchTerm searchTerm, int maxResults) {
        BooleanExpression geburtsdatumPredicate = createQuickSearchGebDatumPredicates(searchTerm.getDateSearchTerms());
        BooleanExpression stringMatchPredicates =
            createQuickSearchNameVornamePredicates(searchTerm.getNameSearchTerms());
        BooleanExpression esadIdMatchPredicates =
            createQuickSearchEsadIdPredicates(searchTerm.getEdokidIdSearchTerms());
        var predicate = Expressions.anyOf(geburtsdatumPredicate, stringMatchPredicates, esadIdMatchPredicates);

        return db.select(new QKindSearchJson(
                QKind.kind.id,
                QKind.kind.esadId,
                QKind.kind.sozialversicherungsNummer,
                QKind.kind.name,
                QKind.kind.vorname,
                QKind.kind.geschlecht,
                QKind.kind.geburtsdatum))
            .from(QKind.kind)
            .where(predicate.and(QKind.kind.kindAktivStatus.ne(EntityAktivStatus.INAKTIV)))

            .limit(maxResults)
            .orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc())
            .fetch();
    }

    @NonNull
    private static BooleanExpression createQuickSearchEsadIdPredicates(@NonNull List<String> esadIdSearchTerms) {
        List<BooleanExpression> esadIdPredicates = new ArrayList<>();
        for (String nameSearchTerm : esadIdSearchTerms) {
            // if the search term starts with a letter, we can change the pattern to match from beginning only
            if (startsWithLetter(nameSearchTerm)) {
                esadIdPredicates.add(QKind.kind.esadId.like(nameSearchTerm + '%'));
            } else {
                BooleanExpression esadIdMatch = QKind.kind.esadId.contains(nameSearchTerm);
                esadIdPredicates.add(esadIdMatch);
            }
        }
        return Expressions.anyOf(esadIdPredicates.toArray(EMPTY_BOOLEAN_EXPRESSION));
    }

    private static boolean startsWithLetter(@NonNull String nameSearchTerm) {
        if (!nameSearchTerm.isEmpty()) {
            char firstChar = nameSearchTerm.charAt(0);
            return Character.isAlphabetic(firstChar);
        }
        return false;
    }

    @NonNull
    private static BooleanExpression createQuickSearchNameVornamePredicates(@NonNull List<String> stringSearchTerms) {
        List<BooleanExpression> stringPredicates = new ArrayList<>();
        for (String nameSearchTerm : stringSearchTerms) {
            BooleanExpression stringMatch = QKind.kind.name.containsIgnoreCase(nameSearchTerm)
                .or(QKind.kind.vorname.containsIgnoreCase(nameSearchTerm));
            stringPredicates.add(stringMatch);
        }
        return Expressions.allOf(stringPredicates.toArray(EMPTY_BOOLEAN_EXPRESSION));
    }

    @NonNull
    private static BooleanExpression createQuickSearchGebDatumPredicates(@NonNull List<LocalDate> getDatumSearchTerms) {
        List<BooleanExpression> datePredicates = new ArrayList<>();
        for (LocalDate dateSearchTerm : getDatumSearchTerms) {
            BooleanExpression dateMatch = QKind.kind.geburtsdatum.eq(dateSearchTerm);
            datePredicates.add(dateMatch);
        }
        return Expressions.anyOf(datePredicates.toArray(EMPTY_BOOLEAN_EXPRESSION));
    }

    @NonNull
    public PageResultJson<SearchSummaryJson> findKinderPaginated(@NonNull SearchJson kindSearchJson) {
        SmartJPAQuery<SearchSummaryJson> query = createKindSearchAndSortQuery(kindSearchJson);

        PageRequestJson pageRequest = kindSearchJson.getPaginationRequest();
        SmartJPAQuery<SearchSummaryJson> limitedQuery =
            query.offset(pageRequest.calculateOffset()).limit(pageRequest.getMaxPageSize());

        int totalItemCount = pageRequest.isRequestTotal() ? runTotalCountQuery() : -1 ;

        int totalFilteredItemCount = kindSearchJson.getPaginationRequest().isRequestTotal() ?
            runTotalFilteredItemCountQuery(kindSearchJson) :
            -1;

        return new PageResultJson<>(
            (int) pageRequest.getPageNr(),
            pageRequest.getMaxPageSize(),
            totalFilteredItemCount,
            totalItemCount,
            limitedQuery.fetch());
    }

    @NonNull
    private SmartJPAQuery<SearchSummaryJson> createKindSearchAndSortQuery(@NonNull SearchJson kindSearchJson) {
        SmartJPAQuery<SearchSummaryJson> baseQuery = getKindSummaryJsonQuery();

        SmartJPAQuery<SearchSummaryJson>
            filteredQuery = baseQuery.where(createKindSearchWhere(kindSearchJson));

        // add sorting
        OrderSpecifier<?>[] orderSpecifiers = kindSearchJson.getSorts()
            .stream()
            .map(sort -> sort.getColumnName().createOrderTerm(sort.getDirection()))
            .toArray(OrderSpecifier[]::new);

        // it is important to always sort by id as well to ensure we always get the same unique order in case we have
        // multiple rows
        // that otherwise have the same values
        OrderSpecifier<?> idOrderTerm = new OrderSpecifier<>(Order.ASC, QKind.kind.id);
        OrderSpecifier<?>[] uniqueOrder =
            Streams.concat(Arrays.stream(orderSpecifiers), Stream.of(idOrderTerm)).toArray(OrderSpecifier[]::new);

        return filteredQuery.orderBy(uniqueOrder);
    }

    private int runTotalFilteredItemCountQuery(@NonNull SearchJson searchJson) {
        SmartJPAQuery<Long> baseQuery = db.select(QKind.kind.id.countDistinct())
            .from(QKind.kind)
            .leftJoin(QKind.kind.schule, QSchule.schule)
            .leftJoin(QKind.kind.klasse, QKlasse.klasse)
            .leftJoin(QDossier.dossier)
            .on(QDossier.dossier.kind.eq(QKind.kind))
            .leftJoin(QEinladung.einladung)
            .on(QEinladung.einladung.dossier.eq(QDossier.dossier))
            .leftJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .leftJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .leftJoin(QEinladung.einladung.untersuchung, QUntersuchung.untersuchung)
            .leftJoin(QUntersuchung.untersuchung.untersuchungImpfungen, QUntersuchungImpfungen.untersuchungImpfungen)
            .leftJoin(
                QUntersuchungImpfungen.untersuchungImpfungen.untersuchungImpfungenMassnahmen,
                QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen)
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
            .on(QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen.eq(QUntersuchungImpfungen.untersuchungImpfungen))
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern.impfstoff, QImpfstoff.impfstoff)
            .leftJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.charge,
                QImpfstoffCharge.impfstoffCharge)
            .leftJoin(QNotiz.notiz)
            .on(QNotiz.notiz.dossier.eq(QDossier.dossier))
            .leftJoin(QNachkontrolle.nachkontrolle)
            .on(QNachkontrolle.nachkontrolle.dossier.eq(QDossier.dossier));

        SmartJPAQuery<Long> query = baseQuery.where(createKindSearchWhere(searchJson));
        Optional<Long> countOpt = query.fetchOne();
        return countOpt.map(Long::intValue).orElse(0);
    }

    private int runTotalCountQuery() {
        SmartJPAQuery<Long> baseQuery = db.select(QKind.kind.id.count())
            .from(QKind.kind);

        Optional<Long> countOpt = baseQuery.fetchOne();
        return countOpt.map(Long::intValue).orElse(0);
    }

    @NonNull
    private static Predicate[] createKindSearchWhere(
        @NonNull SearchJson kindSearchJson
    ) {
        boolean sucheNachPendentenAnlaessen = SearchPredicateUtils.isSucheForPendenteAnlaesse(kindSearchJson);
        Predicate[] filterTerms = null;
        if (sucheNachPendentenAnlaessen) {
            JPQLSubQuery<Integer> existsSubquery = createSubqueryForKinderMissingAnlass(kindSearchJson);

            // Negate the whole spezial filter expression
            BooleanExpression negatedSpezialFilters = existsSubquery.exists().not();

            // Get other filters (not the special ones)
            List<Predicate> otherFilterTerms = SearchPredicateUtils.getPredicatesNormalsuche(kindSearchJson);

            // Combine negated spezial filters with other filters using AND
            List<Predicate> finalFilterTerms = new ArrayList<>();
            finalFilterTerms.add(negatedSpezialFilters);
            finalFilterTerms.addAll(otherFilterTerms);

            filterTerms = finalFilterTerms.toArray(Predicate[]::new);

        } else { // default search
            filterTerms = kindSearchJson.getFilters()
                .stream()
                .map(filter -> filter.getColumnName().createFilterTerm(filter))
                .toArray(Predicate[]::new);
        }
        return filterTerms;
    }

    @NonNull
    private static JPQLSubQuery<Integer> createSubqueryForKinderMissingAnlass(@NonNull SearchJson kindSearchJson) {
        // Create the EXISTS subquery with the combined special filters; define new alias where needed
        QEinladung innerEinladung = new QEinladung("innerEinladung");
        QDossier innerDossier = new QDossier("innerDossier");
        QAnlass innerAnlass = new QAnlass("innerAnlass");
        QAnlassConfig innerAnlassConfig = new QAnlassConfig("innerAnlassConfig");

        // All special filters for the "Pendente Anlaesse" search must be handeled here
        SearchPredicateUtils.getSearchColumnsForAnlassPendentSuche(kindSearchJson).forEach(searchColumns -> {
            if (searchColumns != SearchColumns.ANLASS_PENDENT
                && searchColumns != SearchColumns.ANLASS_TYP
                && searchColumns != SearchColumns.ANLASS_ZEITRAUM_BIS
                && searchColumns != SearchColumns.ANLASS_ZEITRAUM_VON
            ) {
                throw new IllegalArgumentException("Invalid search column for Pendente Anlaesse: " + searchColumns);
            }
        });

        Optional<FilterFieldJson<SearchColumns>> anlassTypFilterOpt =
            SearchPredicateUtils.getFilterIfPresent(kindSearchJson, SearchColumns.ANLASS_TYP);
        Predicate anlassTypPredicate = anlassTypFilterOpt
            .map(filter -> innerAnlassConfig.anlassTyp.eq(AnlassTyp.valueOf(filter.getFilterValue())))
            .orElse(Expressions.TRUE);

        Optional<FilterFieldJson<SearchColumns>> anlassDatumBisFilterOpt =
            SearchPredicateUtils.getFilterIfPresent(kindSearchJson, SearchColumns.ANLASS_ZEITRAUM_BIS);
        Predicate anlassDatumBisPredicate = anlassDatumBisFilterOpt
            .map(filter -> innerAnlass.datum.loe(SearchColumns.toLocalDate(filter)))
            .orElse(Expressions.TRUE);

        Optional<FilterFieldJson<SearchColumns>> anlassDatumVonFilterOpt =
            SearchPredicateUtils.getFilterIfPresent(kindSearchJson, SearchColumns.ANLASS_ZEITRAUM_VON);
        Predicate anlassDatumVonPredicate = anlassDatumVonFilterOpt
            .map(filter -> innerAnlass.datum.goe(SearchColumns.toLocalDate(filter)))
            .orElse(Expressions.TRUE);

        return JPAExpressions.selectOne()
            .from(innerEinladung)
            .innerJoin(innerEinladung.dossier, innerDossier)
            .innerJoin(innerEinladung.anlass, innerAnlass)
            .innerJoin(innerAnlass.anlassConfig, innerAnlassConfig)
            .where(innerDossier.kind.id.eq(QKind.kind.id) // math kind from main query
                .and(anlassTypPredicate)
                .and(anlassDatumBisPredicate)
                .and(anlassDatumVonPredicate));
    }

    @NonNull
    private SmartJPAQuery<SearchSummaryJson> getKindSummaryJsonQuery() {
        return db.selectDistinct(new QSearchSummaryJson(
                QKind.kind.id,
                QKind.kind.esadId,
                QKind.kind.sozialversicherungsNummer,
                QKind.kind.name,
                QKind.kind.vorname,
                QKind.kind.geschlecht,
                QKind.kind.geburtsdatum,
                QKind.kind.kindAktivStatus,
                QSchule.schule.id,
                QSchule.schule.schulhaus,
                QSchule.schule.schuleAktivStatus,
                QKlasse.klasse.id,
                QKlasse.klasse.bezeichnung,
                QKlasse.klasse.klasseAktivStatus,
                QKlasse.klasse.klassenstufe))
            .from(QKind.kind)
            .leftJoin(QKind.kind.schule, QSchule.schule)
            .leftJoin(QKind.kind.klasse, QKlasse.klasse)
            .leftJoin(QDossier.dossier).on(QDossier.dossier.kind.eq(QKind.kind))
            .leftJoin(QEinladung.einladung).on(QEinladung.einladung.dossier.eq(QDossier.dossier))
            .leftJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .leftJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .leftJoin(QEinladung.einladung.untersuchung, QUntersuchung.untersuchung)
            .leftJoin(QUntersuchung.untersuchung.untersuchungImpfungen, QUntersuchungImpfungen.untersuchungImpfungen)
            .leftJoin(
                QUntersuchungImpfungen.untersuchungImpfungen.untersuchungImpfungenMassnahmen,
                QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen)
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern)
            .on(QUntersuchungImpfungIntern.untersuchungImpfungIntern.untersuchungImpfungen.eq(QUntersuchungImpfungen.untersuchungImpfungen))
            .leftJoin(QUntersuchungImpfungIntern.untersuchungImpfungIntern.impfstoff, QImpfstoff.impfstoff)
            .leftJoin(
                QUntersuchungImpfungIntern.untersuchungImpfungIntern.charge,
                QImpfstoffCharge.impfstoffCharge)
            .leftJoin(QNotiz.notiz).on(QNotiz.notiz.dossier.eq(QDossier.dossier))
            .leftJoin(QNachkontrolle.nachkontrolle)
            .on(QNachkontrolle.nachkontrolle.dossier.eq(QDossier.dossier));
    }
}
