import {EntityAktivStatus, OrganisationJson, SchuleOptionJson} from '@dvb/data-access-backend';
import {Nullish} from '@dvb/util-shared';
import {getFilteredSchulenOptions} from './schulen-options.store';
import {BackendEntityId} from "@dvb/data-access-types";

const INAKTIV_SUFFIX = ' (inaktiv)';

function schule(
  id: string,
  aktivStatus: EntityAktivStatus,
  organisationId?: string,
  bezeichnung = `Schule ${id}`,
): SchuleOptionJson {
  return {
    id: id as BackendEntityId,
    organisationId: organisationId as BackendEntityId,
    bezeichnung,
    aktivStatus
  };
}

function organisation(id: string): OrganisationJson {
  return {id} as OrganisationJson;
}

const aktivOrg1 = schule('1', EntityAktivStatus.AKTIV, 'org-1');
const aktivOrg2 = schule('2', EntityAktivStatus.AKTIV, 'org-2');
const inaktivOrg1 = schule('3', EntityAktivStatus.INAKTIV, 'org-1');
const inaktivOrg2 = schule('4', EntityAktivStatus.INAKTIV, 'org-2');
const aktivNoOrg = schule('5', EntityAktivStatus.AKTIV);

const allEntities: SchuleOptionJson[] = [aktivOrg1, aktivOrg2, inaktivOrg1, inaktivOrg2, aktivNoOrg];

const inaktivOrg1WithSuffix: SchuleOptionJson = {
  ...inaktivOrg1,
  bezeichnung: inaktivOrg1.bezeichnung + INAKTIV_SUFFIX,
};
const inaktivOrg2WithSuffix: SchuleOptionJson = {
  ...inaktivOrg2,
  bezeichnung: inaktivOrg2.bezeichnung + INAKTIV_SUFFIX,
};

describe('getFilteredSchulenOptions', () => {
  it.each([
    {
      description: 'returns only AKTIV entities when onlyActive=true',
      entities: allEntities,
      currentlySelectedOption: null as SchuleOptionJson | Nullish,
      onlyActive: true,
      onlyFromMyOrganisation: false,
      zentraleOrganisationEnabled: true,
      currentOrganisation: null as OrganisationJson | Nullish,
      hasFachrolleSupport: false,
      expected: [aktivOrg1, aktivOrg2, aktivNoOrg],
    },
    {
      description: 'appends inaktiv-suffix to INAKTIV entities when onlyActive=false',
      entities: allEntities,
      currentlySelectedOption: null,
      onlyActive: false,
      onlyFromMyOrganisation: false,
      zentraleOrganisationEnabled: true,
      currentOrganisation: null,
      hasFachrolleSupport: false,
      expected: [aktivOrg1, aktivOrg2, inaktivOrg1WithSuffix, inaktivOrg2WithSuffix, aktivNoOrg],
    },
    {
      description: 'filters by currentOrganisation when onlyFromMyOrganisation=true and zentraleOrganisation disabled',
      entities: allEntities,
      currentlySelectedOption: null,
      onlyActive: true,
      onlyFromMyOrganisation: true,
      zentraleOrganisationEnabled: false,
      currentOrganisation: organisation('org-1'),
      hasFachrolleSupport: false,
      expected: [aktivOrg1],
    },
    {
      description: 'ignores organisation filter when zentraleOrganisationEnabled=true',
      entities: allEntities,
      currentlySelectedOption: null,
      onlyActive: true,
      onlyFromMyOrganisation: true,
      zentraleOrganisationEnabled: true,
      currentOrganisation: organisation('org-1'),
      hasFachrolleSupport: false,
      expected: [aktivOrg1, aktivOrg2, aktivNoOrg],
    },
    {
      description: 'returns empty list when no organisation and no support role',
      entities: allEntities,
      currentlySelectedOption: null,
      onlyActive: true,
      onlyFromMyOrganisation: true,
      zentraleOrganisationEnabled: false,
      currentOrganisation: null,
      hasFachrolleSupport: false,
      expected: [],
    },
    {
      description: 'returns unfiltered (by organisation) when no organisation but has support role',
      entities: allEntities,
      currentlySelectedOption: null,
      onlyActive: true,
      onlyFromMyOrganisation: true,
      zentraleOrganisationEnabled: false,
      currentOrganisation: null,
      hasFachrolleSupport: true,
      expected: [aktivOrg1, aktivOrg2, aktivNoOrg],
    },
    {
      description: 'appends currentlySelectedOption when not contained in filtered result',
      entities: allEntities,
      currentlySelectedOption: inaktivOrg1,
      onlyActive: true,
      onlyFromMyOrganisation: false,
      zentraleOrganisationEnabled: true,
      currentOrganisation: null,
      hasFachrolleSupport: false,
      expected: [aktivOrg1, aktivOrg2, aktivNoOrg, inaktivOrg1],
    },
    {
      description: 'does not duplicate currentlySelectedOption when already contained',
      entities: allEntities,
      currentlySelectedOption: aktivOrg1,
      onlyActive: true,
      onlyFromMyOrganisation: false,
      zentraleOrganisationEnabled: true,
      currentOrganisation: null,
      hasFachrolleSupport: false,
      expected: [aktivOrg1, aktivOrg2, aktivNoOrg],
    },
    {
      description: 'returns empty list (without selected option) when permission missing',
      entities: allEntities,
      currentlySelectedOption: aktivOrg1,
      onlyActive: true,
      onlyFromMyOrganisation: true,
      zentraleOrganisationEnabled: false,
      currentOrganisation: null,
      hasFachrolleSupport: false,
      expected: [],
    },
    {
      description: 'combines organisation filter and appends selected option from foreign org',
      entities: allEntities,
      currentlySelectedOption: aktivOrg2,
      onlyActive: true,
      onlyFromMyOrganisation: true,
      zentraleOrganisationEnabled: false,
      currentOrganisation: organisation('org-1'),
      hasFachrolleSupport: false,
      expected: [aktivOrg1, aktivOrg2],
    },
    {
      description: 'returns empty list for empty entities input',
      entities: [] as SchuleOptionJson[],
      currentlySelectedOption: null,
      onlyActive: true,
      onlyFromMyOrganisation: false,
      zentraleOrganisationEnabled: true,
      currentOrganisation: null,
      hasFachrolleSupport: false,
      expected: [],
    },
  ])(
    '$description',
    ({
       entities,
       currentlySelectedOption,
       onlyActive,
       onlyFromMyOrganisation,
       zentraleOrganisationEnabled,
       currentOrganisation,
       hasFachrolleSupport,
       expected,
     }) => {
      const result = getFilteredSchulenOptions(
        entities,
        currentlySelectedOption,
        onlyActive,
        onlyFromMyOrganisation,
        zentraleOrganisationEnabled,
        currentOrganisation,
        hasFachrolleSupport,
        INAKTIV_SUFFIX,
      );

      expect(result).toEqual(expected);
    },
  );
});
