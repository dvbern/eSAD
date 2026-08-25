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

package ch.dvbern.oss.hemed.esc.api.features.search.types;

import ch.dvbern.oss.hemed.esc.api.features.search.SearchRepo;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchJson;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.Util;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.QUntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.QImpfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.QImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QNachkontrolle;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizStatus;
import ch.dvbern.oss.hemed.esc.api.features.notizen.QNotiz;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.QAnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterCombination;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterFieldJson;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.SortDirection;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLSubQuery;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum SearchColumns {

    /**
     * this only serves as a marker column, its logical consequences are handled in SearchRepo
     * {@link SearchRepo#createSubqueryForKinderMissingAnlass(SearchJson)}
     */
    ANLASS_PENDENT(null, true) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {

            return Expressions.TRUE;
        }
    },

    NAME(QKind.kind.name, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return createNameFilterValue(filter, QKind.kind.name);
        }
    },
    VORNAME(QKind.kind.vorname, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return createNameFilterValue(filter, QKind.kind.vorname);
        }
    },
    GEBURTSDATUM(QKind.kind.geburtsdatum, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QKind.kind.geburtsdatum.eq(toLocalDate(filter));
        }
    },
    GESCHLECHT(QKind.kind.geschlecht, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QKind.kind.geschlecht.eq(Geschlecht.valueOf(filter.getFilterValue()));
        }
    },
    INAKTIV(QKind.kind.kindAktivStatus, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            EntityAktivStatus aktivStatusToSearch =
                Boolean.parseBoolean(filter.getFilterValue()) ? EntityAktivStatus.INAKTIV : EntityAktivStatus.AKTIV;
            return QKind.kind.kindAktivStatus.eq(aktivStatusToSearch);
        }
    },

    DOSSIER_MPA_ID(null, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            String value = filter.getFilterValue().trim();
            // special case: NOT_ASSIGNED  means we want to search for dossiers that have no MPA assigned
            if (NOT_ASSIGNED.equals(value)) {
                return QDossier.dossier.zustaendigMPA.id.isNull().and(
                    QSchule.schule.zustaendigMPA.id.isNull()
                );
            }
            UUID uuid = UUID.fromString(value);
            return QDossier.dossier.zustaendigMPA.id.eq(uuid).or(
                QDossier.dossier.zustaendigMPA.id.isNull().and(QSchule.schule.zustaendigMPA.id.eq(uuid))
            );
        }
    },

    DOSSIER_ARZT_ID(null, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            String value = filter.getFilterValue().trim();
            // special case: NOT_ASSIGNED  means we want to search for dossiers that have no MPA assigned
            if (NOT_ASSIGNED.equals(value)) {
                return QDossier.dossier.zustaendigArzt.id.isNull().and(
                    QSchule.schule.zustaendigArzt.id.isNull()
                );
            }
            UUID uuid = UUID.fromString(value);
            return QDossier.dossier.zustaendigArzt.id.eq(uuid).or(
                QDossier.dossier.zustaendigArzt.id.isNull().and(QSchule.schule.zustaendigArzt.id.eq(uuid))
            );
        }
    },

    DOSSIER_IMMUNITAET_VARIEZELLEN(QDossier.dossier.immunitaetVarizellen, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Boolean expectedValue = Boolean.parseBoolean(filter.getFilterValue());
            return QDossier.dossier.immunitaetVarizellen.eq(expectedValue);
        }
    },

    DOSSIER_IMMUNITAET_MASERN(QDossier.dossier.immunitaetMasern, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Boolean expectedValue = Boolean.parseBoolean(filter.getFilterValue());
            return QDossier.dossier.immunitaetMasern.eq(expectedValue);
        }
    },

    DOSSIER_NACHKONTROLLE(null, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            QAnlassFlags anlassFlags = QNachkontrolle.nachkontrolle.anlassFlags;

            Predicate hasNachkontrolle = anlassFlags.isNotNull()
                .and(
                    SearchColumns.combinePredicates(List.of(
                        anlassFlags.nachkontrolleAnderes.eq(true),
                        anlassFlags.nachkontrolleHoeren.eq(true),
                        anlassFlags.nachkontrolleMotorik.eq(true),
                        anlassFlags.nachkontrolleSehen.eq(true),
                        anlassFlags.nachkontrolleSprache.eq(true)
                    ), FilterCombination.OR)
                );

            return Boolean.parseBoolean(filter.getFilterValue()) ? hasNachkontrolle : hasNachkontrolle.not();
        }
    },

    SCHULNAME(QSchule.schule.schulhaus, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> searchTerms = splitSearchTerm(filter.getFilterValue());
            List<Predicate> predicates = searchTerms.stream()
                .map(String::trim)
                .map(searchTerm -> (Predicate) QSchule.schule.schulhaus.eq(searchTerm))
                .toList();
            return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
        }
    },
    SCHULE_ID(QSchule.schule.id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> searchTerms = splitSearchTerm(filter.getFilterValue());
            List<Predicate> predicates = searchTerms.stream()
                .map(String::trim)
                .map(searchTerm -> ID.of(UUID.fromString(searchTerm), Schule.class))
                .map(searchTerm -> (Predicate) QSchule.schule.id.eq(searchTerm.getUUID()))
                .toList();
            return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
        }
    },
    KLASSENAME(QKlasse.klasse.bezeichnung, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return createStringFilterValue(filter, QKlasse.klasse.bezeichnung);
        }
    },
    KLASSENSTUFE(QKlasse.klasse.klassenstufe, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> enumValuesAsString = splitSearchTerm(filter.getFilterValue()); // do not normalize
            List<Predicate> predicates = enumValuesAsString.stream()
                .map(String::trim)
                .map(Klassenstufe::valueOf)
                .map(klassenstufe -> (Predicate) QKlasse.klasse.klassenstufe.eq(klassenstufe))
                .toList();
            return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
        }
    },
    ESAD_ID(QKind.kind.esadId, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return createEsadIdFilterValue(filter, QKind.kind.esadId);
        }
    },
    NOTIZ_ZEITRAUM_VON(QNotiz.notiz.timestampErstellt, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
            OffsetDateTime dateToSearch = OffsetDateTime.of(
                formatter.parse(filter.getFilterValue(), LocalDate::from),
                LocalTime.MIDNIGHT,
                ZoneOffset.UTC);
            return QNotiz.notiz.timestampErstellt.after(dateToSearch);
        }
    },
    NOTIZ_ZEITRAUM_BIS(QNotiz.notiz.timestampErstellt, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
            OffsetDateTime dateToSearch = OffsetDateTime.of(
                formatter.parse(filter.getFilterValue(), LocalDate::from),
                LocalTime.MAX,
                ZoneOffset.UTC);
            return QNotiz.notiz.timestampErstellt.before(dateToSearch);
        }
    },
    NOTIZ_SCHULJAHR(QNotiz.notiz.timestampErstellt, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            int schuljahr = Integer.parseInt(filter.getFilterValue());
            return QNotiz.notiz.timestampErstellt.between(
                OffsetDateTime.of(
                    LocalDate.of(schuljahr, 8, 1),
                    LocalTime.MIDNIGHT,
                    ZoneOffset.UTC),
                OffsetDateTime.of(
                    LocalDate.of(schuljahr + 1, 7, 31),
                    LocalTime.MIDNIGHT,
                    ZoneOffset.UTC));
        }
    },
    NOTIZ_VERANTWORTLICHER_ID(QNotiz.notiz.verantwortlicher.id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QNotiz.notiz.verantwortlicher.id.eq(UUID.fromString(filter.getFilterValue()));
        }
    },
    NOTIZ_LABEL(QNotiz.notiz.labels.any().id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> labelIdsAsString = splitSearchTerm(filter.getFilterValue());
            List<Predicate> predicates = labelIdsAsString
                .stream()
                .map(String::trim)
                .map(labelId -> (Predicate) JPAExpressions.selectOne()
                    .from(QNotiz.notiz)
                    .where(QNotiz.notiz.labels.any().id.eq(UUID.fromString(labelId))
                        .and(QNotiz.notiz.dossier.kind.id.eq(QKind.kind.id)))
                    .exists())
                .toList();

            return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
        }
    },
    NOTIZ_STATUS(QNotiz.notiz.status, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QNotiz.notiz.status.eq(NotizStatus.valueOf(filter.getFilterValue()));
        }
    },

    ANLASS_ZEITRAUM_VON(QAnlass.anlass.datum, true) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.datum.goe(toLocalDate(filter));
        }
    },

    ANLASS_ZEITRAUM_BIS(QAnlass.anlass.datum, true) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.datum.loe(toLocalDate(filter));
        }
    },

    ANLASS_SCHULJAHR(null, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.schuljahr.eq(Integer.parseInt(filter.getFilterValue()));
        }
    },

    ANLASS_STATUS(QAnlass.anlass.anlassStatus, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.anlassStatus.eq(AnlassStatus.valueOf(filter.getFilterValue()));
        }
    },

    ANLASS_ART(QAnlass.anlass.anlassArt, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.anlassArt.eq(AnlassArt.valueOf(filter.getFilterValue()));
        }
    },

    ANLASS_TYP(QAnlass.anlass.anlassConfig.anlassTyp, true) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            String value = filter.getFilterValue();
            return QAnlass.anlass.anlassConfig.anlassTyp.eq(AnlassTyp.valueOf(value));
        }
    },
    ANLASS_NOT_ASSIGNED(null, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            boolean noAnlassAssigned = Boolean.parseBoolean(filter.getFilterValue());
            JPQLSubQuery<Integer> anlssForKindRelation = JPAExpressions.selectOne()
                .from(QEinladung.einladung)
                .where(QEinladung.einladung.dossier.kind.id.eq(QKind.kind.id));
            if (noAnlassAssigned) {
                return anlssForKindRelation.notExists();
            }
            return anlssForKindRelation.exists();
        }

    },

    ANLASS_MPA_ID(QAnlass.anlass.mpa.id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.mpa.id.eq(UUID.fromString(filter.getFilterValue()));
        }
    },

    ANLASS_ARZT_ID(QAnlass.anlass.arzt.id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.arzt.id.eq(UUID.fromString(filter.getFilterValue()));
        }
    },

    ANLASS_WEITERE_PERSONEN(QAnlass.anlass.weiterePerson.id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QAnlass.anlass.weiterePerson.id.eq(UUID.fromString(filter.getFilterValue()));
        }
    },

    ANLASS_TEILNAHME_STATUS(QEinladung.einladung.einladungStatus, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> statusAsString = splitSearchTerm(filter.getFilterValue());
            List<Predicate> predicates = statusAsString.stream()
                .map(String::trim)
                .map(EinladungStatus::valueOf)
                .map(status -> (Predicate) QEinladung.einladung.einladungStatus.eq(status))
                .toList();
            return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
        }
    },

    IMPFUNG_KRANKHEIT(null, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> krankheitenAsString = splitSearchTerm(filter.getFilterValue());
            List<Krankheit> krankheiten = krankheitenAsString.stream()
                .map(String::trim)
                .map(Krankheit::valueOf)
                .toList();
            // only one krankheit is supported
            if (krankheiten.size() > 1) {
                throw new AppFailureException(AppFailureMessage.internalError(
                    "Can only filter for one krankheit but got " + krankheitenAsString));
            }
            Krankheit krankheit = krankheiten.getFirst();
            BooleanExpression predicate =
                QImpfstoff.impfstoff.krankheiten.contains(krankheit.name())
                    .and(QUntersuchungImpfungIntern.untersuchungImpfungIntern.impfStatus.eq(ImpfStatus.DURCHGEFUEHRT));

            return SearchColumns.combinePredicates(List.of(predicate), filter.getFilterCombinationMethod());
        }
    },
    ANLASS_IMPFUNG_MASSNAHME_VORMERKUNG_FOLGETERMIN(QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.vormerkenFuerGruppenFolgetermin, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Boolean expectedValue = Boolean.valueOf(filter.getFilterValue());
            return QUntersuchungImpfungenMassnahmen.untersuchungImpfungenMassnahmen.vormerkenFuerGruppenFolgetermin.eq(
                expectedValue);

        }
    },



    IMPFUNG_CHARGE(QImpfstoffCharge.impfstoffCharge.id, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            Collection<String> chargeIdAsString = splitSearchTerm(filter.getFilterValue());

            List<Predicate> predicates = chargeIdAsString.stream()
                .map(String::trim)
                .map(UUID::fromString)
                .map(chargeUUID -> (Predicate) QImpfstoffCharge.impfstoffCharge.id.eq(chargeUUID))
                .toList();
            return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
        }
    },

    IMPFUNG_ZEITRAUM_VON(QUntersuchungImpfungIntern.untersuchungImpfungIntern.datumImpfung, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QUntersuchungImpfungIntern.untersuchungImpfungIntern.datumImpfung.goe(toLocalDate(filter));
        }
    },

    IMPFUNG_ZEITRAUM_BIS(QUntersuchungImpfungIntern.untersuchungImpfungIntern.datumImpfung, false) {
        @Override
        public Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter) {
            return QUntersuchungImpfungIntern.untersuchungImpfungIntern.datumImpfung.loe(toLocalDate(filter));
        }
    };

    public static final String NOT_ASSIGNED = "NOT_ASSIGNED";
    public static final String DATE_TIME_FORMAT_PATTERN = "yyyy-MM-dd";
    public static final char MULTI_VALUE_FILTER_SEPARATOR = ' ';

    public abstract Predicate createFilterTerm(FilterFieldJson<SearchColumns> filter);

    @Nullable
    private final ComparableExpression<?> columnSortExpression;

    @Getter
    private final boolean canBeUsedInAnlassPendentSuche;

    SearchColumns(@Nullable ComparableExpression<?> columnSortExpression, boolean canBeUsedInAnlassPendentSuche) {
        this.columnSortExpression = columnSortExpression;
        this.canBeUsedInAnlassPendentSuche = canBeUsedInAnlassPendentSuche;
    }

    @SuppressWarnings("java:S1452" /* could not find a way to use the correct generic sicne we are in an enum */)
    public OrderSpecifier<?> createOrderTerm(@NonNull SortDirection direction) {
        Order order = mapSortDirectionToOrderDirection(direction);
        if (columnSortExpression == null) {
            throw new AppFailureException(AppFailureMessage.internalError(
                String.format(
                    "Tried to create order term for column %s that has no column expression for Ordering",
                    this.name())));

        }
        return new OrderSpecifier<>(order, columnSortExpression);
    }

    @NonNull
    private static Predicate combinePredicates(
        @NonNull Collection<Predicate> predicates,
        @NonNull FilterCombination filterCombinationMethod
    ) {
        Validate.isTrue(!predicates.isEmpty(), "Tried to combine empty predicates for Filter");
        return switch (filterCombinationMethod) {
            case AND -> Objects.requireNonNull(ExpressionUtils.allOf(predicates));
            case OR -> Objects.requireNonNull(ExpressionUtils.anyOf(predicates));
            case SINGLE -> {
                if (predicates.size() == 1) {
                    yield predicates.iterator().next();
                }
                throw new AppFailureException(AppFailureMessage.internalError(
                    "Single filter combination method requires exactly one filter value"));
            }
        };
    }

    @NonNull
    static Predicate createStringFilterValue(
        @NonNull FilterFieldJson<SearchColumns> filter,
        @NonNull StringExpression qFilterColumn) {
        List<String> filterValues = prepareStringFilterValues(filter);
        Collection<Predicate> predicates = filterValues.stream()
            .map(filterValue -> (Predicate) qFilterColumn.likeIgnoreCase('%' + filterValue + '%'))
            .toList();
        return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
    }

    /**
     * The esadId is searched as a prefix (and not as a substring), since the beginning of an esadId is what is
     * usually known when searching for a Kind. The whole filter value is treated as a single esadId: whitespace
     * is removed from the search term and the separators are inserted again the way the esadId is stored
     * (see {@link EdokidId#formatPartial}), so that the id can be entered with or without the usual grouping,
     * e.g. {@code 2515067} finds {@code 25 15 067 xx}. In the db the values are stored with separators
     */
    @NonNull
    static Predicate createEsadIdFilterValue(
        @NonNull FilterFieldJson<SearchColumns> filter,
        @NonNull StringExpression qFilterColumn
    ) {
        String searchTerm = EdokidId.formatPartial(filter.getFilterValue());
        if (searchTerm.isEmpty()) {
            return Expressions.TRUE;
        }
        return qFilterColumn.likeIgnoreCase(searchTerm + '%');
    }

    @NonNull
    static Predicate createNameFilterValue(
        @NonNull FilterFieldJson<SearchColumns> filter,
        @NonNull StringExpression qFilterColumn) {
        List<String> filterValues = prepareStringFilterValues(filter);
        Collection<Predicate> predicates = filterValues.stream()
            .map(filterValue -> {
                if (filterValue.startsWith("%")) {
                    // If the search term starts with %, match anywhere in the string (but remove % as to not
                    // duplicate it)
                    String valueToSearchFor = filterValue.substring(1);
                    return (Predicate) qFilterColumn.likeIgnoreCase('%' + valueToSearchFor + '%');
                }
                // Otherwise, only match from the beginning of the string
                return (Predicate) qFilterColumn.likeIgnoreCase(filterValue + '%');

            })
            .toList();
        return SearchColumns.combinePredicates(predicates, filter.getFilterCombinationMethod());
    }

    @NonNull
    static List<String> prepareStringFilterValues(@NonNull FilterFieldJson<SearchColumns> filter) {
        FilterCombination combinationMethod = filter.getFilterCombinationMethod();
        return switch (combinationMethod) {
            case FilterCombination.SINGLE -> List.of(filter.getFilterValue());
            case AND, OR -> splitSearchTerm(filter.getFilterValue())
                .stream()
                .map(SearchColumns::normalizeStringFilterValue)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        };
    }

    @NonNull
    private static Collection<String> splitSearchTerm(@NonNull String filterValue) {
        return Util.tokenizeStringKeepingQuotedParts(filterValue, MULTI_VALUE_FILTER_SEPARATOR, '"');
    }

    @NonNull
    private static String normalizeStringFilterValue(@NonNull String filterValue) {
        return filterValue.toLowerCase();
    }

    @NonNull
    private static Order mapSortDirectionToOrderDirection(@NonNull SortDirection direction) {
        return switch (direction) {
            case ASC -> Order.ASC;
            case DESC -> Order.DESC;
        };
    }

    @NonNull
    public static LocalDate toLocalDate(@NonNull FilterFieldJson<SearchColumns> filter) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_PATTERN);
        return formatter.parse(filter.getFilterValue(), LocalDate::from);
    }
}
