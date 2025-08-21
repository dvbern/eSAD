import {
  FilterCombination,
  SearchColumns,
  SearchJsonFiltersInner,
  SearchJsonSortsInner,
  SearchSummaryJson,
  SortDirection,
} from '@dvb/data-access-backend';
import {FilterDTO, SortDTO} from '@dvb/util-angular';
import {isFilledIn, isNotNullish, isNullish, Leaves} from '@dvb/util-shared';
import {ErweiterteFilterJson} from '../kinder-list/kinder-filter/kinder-filter.form';

export function mapSortToSortJson(sort: SortDTO<SearchSummaryJson> | undefined): SearchJsonSortsInner[] {
  if (isNotNullish(sort) && isNotNullish(sort.colSorts)) {
    return sort.colSorts
      .filter(colSort => colSort.sortDirection !== 'NONE') // no need to handle unsorted columns
      .map(colSort => ({
        columnName: mapColumnNameToSearchColumn(colSort.colName),
        direction: mapSortDirection(colSort.sortDirection),
      }));
  }
  return [];
}

function mapSortDirection(sortDirection: 'ASC' | 'DESC' | 'NONE' | undefined): SortDirection {
  switch (sortDirection) {
    case 'ASC':
      return SortDirection.ASC;
    case 'DESC':
      return SortDirection.DESC;
    default:
      throw new Error(`sort direction '${sortDirection}' is not yet mapped in enum`);
  }
}

export function mapFilterToFilterJson(filterDTO: FilterDTO<SearchSummaryJson> | undefined): SearchJsonFiltersInner[] {
  if (isNotNullish(filterDTO) && isNotNullish(filterDTO.filters)) {
    return filterDTO.filters.map(filter => ({
      columnName: mapColumnNameToSearchColumn(filter.filterName),
      filterValue: filter.filterValue,
      filterCombinationMethod: filter.filterCombinationMethod,
    }));
  }
  return [];
}

export function mapErweiterteFilterToFilterJson(erweiterteFilter: ErweiterteFilterJson | undefined): SearchJsonFiltersInner[] {
  if (isNullish(erweiterteFilter)) {
    return [];
  }

  const filters: SearchJsonFiltersInner[] = [];
  if (erweiterteFilter.anlassPendent) {
    filters.push({
      filterValue: erweiterteFilter.anlassPendent ? 'true' : 'false',
      filterCombinationMethod: FilterCombination.AND,
      columnName: SearchColumns.ANLASS_PENDENT,
    })
  }
  if (isFilledIn(erweiterteFilter.zeitraumVon)) {
    filters.push({
      filterValue: erweiterteFilter.zeitraumVon,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.NOTIZ_ZEITRAUM_VON,
    })
  }
  if (isFilledIn(erweiterteFilter.zeitraumBis)) {
    filters.push({
      filterValue: erweiterteFilter.zeitraumBis,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.NOTIZ_ZEITRAUM_BIS,
    })
  }
  if (isNotNullish(erweiterteFilter.schuljahr)) {
    filters.push({
      filterValue: erweiterteFilter.schuljahr.toString(),
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.NOTIZ_SCHULJAHR,
    })
  }
  if (isNotNullish(erweiterteFilter.verantwortlicherId)) {
    filters.push({
      filterValue: erweiterteFilter.verantwortlicherId,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.NOTIZ_VERANTWORTLICHER_ID,
    })
  }
  if (erweiterteFilter.labels.length > 0) {
    filters.push({
      filterValue: erweiterteFilter.labels.map(label => label.id).join(' '),
      filterCombinationMethod: FilterCombination.AND,
      columnName: SearchColumns.NOTIZ_LABEL,
    })
  }
  if (isNotNullish(erweiterteFilter.status)) {
    filters.push({
      filterValue: erweiterteFilter.status,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.NOTIZ_STATUS,
    })
  }
  if (isNotNullish(erweiterteFilter.inaktiv)) {
    filters.push({
        filterValue: erweiterteFilter.inaktiv ? 'true' : 'false',
        filterCombinationMethod: FilterCombination.SINGLE,
        columnName: SearchColumns.INAKTIV,
      },
    )
  }
  if (isNotNullish(erweiterteFilter.mpaId)) {
    filters.push({
      filterValue: erweiterteFilter.mpaId,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.DOSSIER_MPA_ID,
    })
  }
  if (isNotNullish(erweiterteFilter.arztId)) {
    filters.push({
      filterValue: erweiterteFilter.arztId,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.DOSSIER_ARZT_ID,
    })
  }
  if (isNotNullish(erweiterteFilter.immunitaetVariezellen)) {
    filters.push({
      filterValue: erweiterteFilter.immunitaetVariezellen ? 'true' : 'false',
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.DOSSIER_IMMUNITAET_VARIEZELLEN,
    })
  }
  if (isNotNullish(erweiterteFilter.immunitaetMasern)) {
    filters.push({
      filterValue: erweiterteFilter.immunitaetMasern ? 'true' : 'false',
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.DOSSIER_IMMUNITAET_MASERN,
    })
  }

  if (isNotNullish(erweiterteFilter.krankheit)) {
    filters.push({
      filterValue: erweiterteFilter.krankheit,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.IMPFUNG_KRANKHEIT,
    })
  }

  if (isFilledIn(erweiterteFilter.impfdatumVon)) {
    filters.push({
      filterValue: erweiterteFilter.impfdatumVon,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.IMPFUNG_ZEITRAUM_VON,
    })
  }
  if (isFilledIn(erweiterteFilter.impfdatumBis)) {
    filters.push({
      filterValue: erweiterteFilter.impfdatumBis,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.IMPFUNG_ZEITRAUM_BIS,
    })
  }

  if (isNotNullish(erweiterteFilter.impfungCharge)) {
    filters.push({
      filterValue: erweiterteFilter.impfungCharge,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.IMPFUNG_CHARGE,
    })
  }

  // Anlass related filters
  if (isFilledIn(erweiterteFilter.anlassZeitraumVon)) {
    filters.push({
      filterValue: erweiterteFilter.anlassZeitraumVon,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_ZEITRAUM_VON,
    })
  }
  if (isFilledIn(erweiterteFilter.anlassZeitraumBis)) {
    filters.push({
      filterValue: erweiterteFilter.anlassZeitraumBis,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_ZEITRAUM_BIS,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassSchuljahr)) {
    filters.push({
      filterValue: erweiterteFilter.anlassSchuljahr.toString(),
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_SCHULJAHR,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassStatus)) {
    filters.push({
      filterValue: erweiterteFilter.anlassStatus,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_STATUS,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassArt)) {
    filters.push({
      filterValue: erweiterteFilter.anlassArt,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_ART,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassTyp)) {
    filters.push({
      filterValue: erweiterteFilter.anlassTyp,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_TYP,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassMpaId)) {
    filters.push({
      filterValue: erweiterteFilter.anlassMpaId,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_MPA_ID,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassArztId)) {
    filters.push({
      filterValue: erweiterteFilter.anlassArztId,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_ARZT_ID,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassWeiterePerson)) {
    filters.push({
      filterValue: erweiterteFilter.anlassWeiterePerson,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_WEITERE_PERSONEN,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassTeilnahmeStatus)) {
    filters.push({
      filterValue: erweiterteFilter.anlassTeilnahmeStatus,
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_TEILNAHME_STATUS,
    })
  }

  if (isNotNullish(erweiterteFilter.anlassImpfungMassnahmeVormerkungFolgetermin)) {
    filters.push({
      filterValue: erweiterteFilter.anlassImpfungMassnahmeVormerkungFolgetermin ? 'true' : 'false',
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_IMPFUNG_MASSNAHME_VORMERKUNG_FOLGETERMIN,
    })

  }
  if (isNotNullish(erweiterteFilter.dossierNachkontrolle)) {
    filters.push({
      filterValue: erweiterteFilter.dossierNachkontrolle
        ? 'true'
        : 'false',
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.DOSSIER_NACHKONTROLLE,
    })
  }
  if (isNotNullish(erweiterteFilter.anlassNotAssigned)) {
    filters.push({
      filterValue: erweiterteFilter.anlassNotAssigned ? 'true' : 'false',
      filterCombinationMethod: FilterCombination.SINGLE,
      columnName: SearchColumns.ANLASS_NOT_ASSIGNED,
    })
  }

  return filters;
}

export function mapColumnNameToSearchColumn(colName: Leaves<SearchSummaryJson> | undefined): SearchColumns {
  switch (colName) {
    case 'name':
      return SearchColumns.NAME;
    case 'vorname':
      return SearchColumns.VORNAME;
    case 'geschlecht':
      return SearchColumns.GESCHLECHT;
    case 'geburtsdatum':
      return SearchColumns.GEBURTSDATUM;
    case 'schule.bezeichnung':
      return SearchColumns.SCHULNAME;
    case 'schule.id':
      return SearchColumns.SCHULE_ID;
    case 'klasse.bezeichnung':
      return SearchColumns.KLASSENAME;
    case 'klassenStufe':
      return SearchColumns.KLASSENSTUFE;
    case 'esadId':
      return SearchColumns.ESAD_ID;

    default:
      throw new Error(`column name '${colName}' is not yet mapped in enum`);
  }
}
