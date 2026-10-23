#!/usr/bin/env node
/*
 * Provisions the e2e users in the LOCAL Keycloak (docker-compose, :8989) and writes src/util/e2e.env.
 *
 *   node apps/esc-app-e2e/scripts/provision-local-users.mjs [--tenants bs,be,win] [--fix-realms]
 *
 * Why this exists: the committed .env-template points at the shared DV Bern Keycloak, whose users log in
 * through the vacme IdP. Locally there is no such IdP, the imported realms carry no users, and the BE realm
 * has no client for the local OAuth2Proxy - so the suite could only run as one hand-made admin, for BS only.
 *
 * What a user needs depends on the tenant, because the tenants manage roles differently (tenantsetting):
 * - BS syncs Fachrollen from the token into its zentrale Organisation, so the Keycloak client roles ARE the
 *   roles. New users are fine.
 * - WIN does the same as BS, with its own Fachrollen (see its user list).
 * - BE ignores Keycloak roles and has no zentrale Organisation; roles are assigned in the app. So BE uses the
 *   Benutzer the dev seed already gave roles (matched by e-mail), with the names the seed has - the API
 *   overwrites the Benutzer's name from the token on every login.
 *
 * Idempotent: re-running keeps the passwords already in e2e.env, and resets Keycloak to match them.
 * --fix-realms also applies the realm prerequisites a tenant lacks (reported line by line). Without it the
 * script only reports them. Passwords are never printed.
 */
import {randomBytes} from 'node:crypto';
import {existsSync, readFileSync, writeFileSync} from 'node:fs';
import https from 'node:https';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const REPO = path.resolve(HERE, '../../../..');
const KEYCLOAK_ENV = path.join(REPO, 'developer-local-settings/config/keycloak.env');
const E2E_ENV = path.resolve(HERE, '../src/util/e2e.env');
const KEYCLOAK = 'https://localhost:8989';
// The local Keycloak serves a mkcert certificate for its public hostname, not for localhost. Verification is
// switched off for these requests only, and they only ever go to localhost.
const LOCAL_TLS = new https.Agent({rejectUnauthorized: false});

const PROXY_CLIENT = 'esc-oauth2-proxy-local';
const ROLE_SCOPE = 'esc-fachrolle';

/** Keys follow the storage-state names used by the specs; env names follow util/env.util.ts. */
const TENANTS = {
  bs: {
    realm: 'bs',
    tenantClaim: 'BS',
    baseUrl: 'https://local-esc-bs.localhost:4443',
    users: [
      {env: 'ESC_ADMIN', email: 'escadmin@mailbucket.dvbern.ch', firstName: 'Adele', lastName: 'Admin', roles: ['ADMIN']},
      {env: 'ESC_EMPFANG', email: 'escempfang@mailbucket.dvbern.ch', firstName: 'Emil', lastName: 'Empfang', roles: ['EMPFANG']},
      // the Doppelkontrolle spec picks «Monika MPA» as the second visum
      {env: 'ESC_MPA', email: 'escmpa@mailbucket.dvbern.ch', firstName: 'Monika', lastName: 'MPA', roles: ['MPA']},
      {env: 'ESC_ORTHOPISTIN', email: 'escorthopistin@mailbucket.dvbern.ch', firstName: 'Olivia', lastName: 'Orthoptistin', roles: ['ORTHOPISTIN']},
      {env: 'ESC_SCHULAERZTIN', email: 'escschulaerztin@mailbucket.dvbern.ch', firstName: 'Sophie', lastName: 'Schulaerztin', roles: ['SCHULAERZTIN']},
      {env: 'ESC_SUPERMPA', email: 'escsupermpa@mailbucket.dvbern.ch', firstName: 'Susanne', lastName: 'SuperMPA', roles: ['MPA', 'SUPERUSER']},
      {env: 'ESC_SUPPORT', email: 'escsupport@mailbucket.dvbern.ch', firstName: 'Stefan', lastName: 'Support', roles: ['SUPPORT']},
    ],
  },
  be: {
    realm: 'be',
    tenantClaim: 'BE',
    baseUrl: 'https://local-esc-be.localhost:6443',
    // BE assigns roles in the app: these are the dev seed's Benutzer, names as seeded (see file header).
    // No Empfang: in BE that Fachrolle carries no Berechtigung at all, so the user cannot open the app.
    users: [
      {env: 'ESC_MPA', email: 'example-mpa@mailbucket.dvbern.ch', firstName: 'Marta', lastName: 'MPA', roles: ['MPA']},
      {env: 'ESC_SCHULAERZTIN', email: 'example-schularzt@mailbucket.dvbern.ch', firstName: 'Jaques', lastName: 'Schularzt', roles: ['SCHULAERZTIN']},
      {env: 'ESC_SUPPORT', email: 'escsupport-be@mailbucket.dvbern.ch', firstName: 'Sarah', lastName: 'BE Support', roles: ['SUPPORT']},
    ],
  },
  win: {
    realm: 'win',
    tenantClaim: 'WIN',
    baseUrl: 'https://local-esc-win.localhost:5443',
    // WIN syncs roles from Keycloak like BS, but its Fachrollen differ (FachRolleWIN): MPA, Empfang and
    // Orthoptistin carry no Berechtigung there, and the Pflegefachperson (PFP) is the second person at an
    // Untersuchung - so ESC_MPA is a PFP.
    users: [
      {env: 'ESC_ADMIN', email: 'escadmin@mailbucket.dvbern.ch', firstName: 'Adele', lastName: 'Admin', roles: ['ADMIN']},
      {env: 'ESC_MPA', email: 'escpfp@mailbucket.dvbern.ch', firstName: 'Paula', lastName: 'PFP', roles: ['PFP']},
      {env: 'ESC_SCHULAERZTIN', email: 'escschulaerztin@mailbucket.dvbern.ch', firstName: 'Sophie', lastName: 'Schulaerztin', roles: ['SCHULAERZTIN']},
      {env: 'ESC_SUPPORT', email: 'escsupport@mailbucket.dvbern.ch', firstName: 'Stefan', lastName: 'Support', roles: ['SUPPORT']},
    ],
  },
};

function parseArgs(argv) {
  const args = {tenants: Object.keys(TENANTS), fixRealms: false};
  for (let i = 0; i < argv.length; i++) {
    if (argv[i] === '--tenants') {
      args.tenants = argv[++i].split(',').map(t => t.trim()).filter(Boolean);
    } else if (argv[i] === '--fix-realms') {
      args.fixRealms = true;
    } else {
      throw new Error(`unknown argument ${argv[i]}`);
    }
  }
  for (const tenant of args.tenants) {
    if (!TENANTS[tenant]) {
      throw new Error(`unknown tenant ${tenant} (known: ${Object.keys(TENANTS).join(', ')})`);
    }
  }
  return args;
}

function readEnvFile(file) {
  const values = {};
  if (!existsSync(file)) {
    return values;
  }
  for (const raw of readFileSync(file, 'utf8').split('\n')) {
    const line = raw.trim();
    if (line && !line.startsWith('#') && line.includes('=')) {
      const at = line.indexOf('=');
      values[line.slice(0, at)] = line.slice(at + 1);
    }
  }
  return values;
}

/** Minimal JSON-over-HTTPS client: no dependency, and the relaxed TLS stays scoped to these calls. */
function request(method, url, {headers = {}, body} = {}) {
  return new Promise((resolve, reject) => {
    const req = https.request(url, {method, headers, agent: LOCAL_TLS}, res => {
      let text = '';
      res.setEncoding('utf8');
      res.on('data', chunk => text += chunk);
      res.on('end', () => resolve({status: res.statusCode, ok: res.statusCode >= 200 && res.statusCode < 300, text}));
    });
    req.on('error', reject);
    if (body !== undefined) {
      req.write(body);
    }
    req.end();
  });
}

let accessToken;

async function adminToken() {
  const env = readEnvFile(KEYCLOAK_ENV);
  if (!env.KEYCLOAK_ADMIN || !env.KEYCLOAK_ADMIN_PASSWORD) {
    throw new Error(`KEYCLOAK_ADMIN / KEYCLOAK_ADMIN_PASSWORD missing in ${KEYCLOAK_ENV}`);
  }
  const response = await request('POST', `${KEYCLOAK}/realms/master/protocol/openid-connect/token`, {
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: new URLSearchParams({
      grant_type: 'password', client_id: 'admin-cli', username: env.KEYCLOAK_ADMIN, password: env.KEYCLOAK_ADMIN_PASSWORD,
    }).toString(),
  });
  if (!response.ok) {
    throw new Error(`Keycloak admin login failed: ${response.status}`);
  }
  return JSON.parse(response.text).access_token;
}

async function kc(method, realmPath, body) {
  accessToken ??= await adminToken();
  const response = await request(method, `${KEYCLOAK}/admin/realms${realmPath}`, {
    headers: {'Authorization': `Bearer ${accessToken}`, 'Content-Type': 'application/json'},
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) {
    throw new Error(`${method} ${realmPath}: ${response.status} ${response.text.slice(0, 300)}`);
  }
  return response.text ? JSON.parse(response.text) : undefined;
}

const stripIds = representation => {
  const copy = {...representation};
  delete copy.id;
  return copy;
};

async function findClient(realm, clientId) {
  return (await kc('GET', `/${realm}/clients?clientId=${encodeURIComponent(clientId)}`))[0];
}

/**
 * The prerequisites a realm needs before its users can log in through the local OAuth2Proxy. Each check
 * reports what is missing and, with --fix-realms, fixes it by copying the BS realm's working setup.
 */
async function ensureRealm(config, fix) {
  const {realm} = config;
  const requiredRoles = [...new Set(config.users.flatMap(user => user.roles))];
  const pending = [];
  const step = async (description, isMissing, apply) => {
    if (!(await isMissing())) {
      return;
    }
    if (fix) {
      await apply();
      console.log(`  [${realm}] fixed: ${description}`);
    } else {
      pending.push(description);
    }
  };

  // 1. Tenant claim: an unmanaged "tenant" attribute is dropped unless the user profile allows it
  await step('user profile keeps unmanaged attributes (the tenant claim comes from one)',
    async () => (await kc('GET', `/${realm}/users/profile`)).unmanagedAttributePolicy !== 'ADMIN_EDIT',
    async () => {
      const profile = await kc('GET', `/${realm}/users/profile`);
      await kc('PUT', `/${realm}/users/profile`, {...profile, unmanagedAttributePolicy: 'ADMIN_EDIT'});
    });

  // 2. The OAuth2Proxy's client, cloned from BS - including its secret, which all local proxies share
  await step(`client ${PROXY_CLIENT} exists`,
    async () => !(await findClient(realm, PROXY_CLIENT)),
    async () => {
      const source = await findClient('bs', PROXY_CLIENT);
      const origin = new URL(config.baseUrl).origin;
      await kc('POST', `/${realm}/clients`, {
        ...stripIds(source),
        rootUrl: origin,
        baseUrl: '/',
        redirectUris: [`${origin}/oauth2/callback`],
        webOrigins: ['+'],
        protocolMappers: undefined,
        defaultClientScopes: undefined,
        optionalClientScopes: undefined,
      });
    });

  const client = await findClient(realm, PROXY_CLIENT);
  if (client) {
    // 3. All local OAuth2Proxies send the same client secret (api/.env). A realm whose client has another one
    //    accepts the password and then fails the token exchange with "unauthorized_client". BS logins work,
    //    so its client holds the shared secret. The secrets are compared, never printed.
    const secretOf = async (realmName, id) => (await kc('GET', `/${realmName}/clients/${id}/client-secret`)).value;
    const bsClient = await findClient('bs', PROXY_CLIENT);
    await step('client secret is the one the local OAuth2Proxies share (as in BS)',
      async () => realm !== 'bs' && (await secretOf(realm, client.id)) !== (await secretOf('bs', bsClient.id)),
      async () => {
        const current = await kc('GET', `/${realm}/clients/${client.id}`);
        await kc('PUT', `/${realm}/clients/${client.id}`, {...current, secret: await secretOf('bs', bsClient.id)});
      });

    // 4. Client roles: the token's resource_access.esc.roles are mapped from these
    await step(`client roles ${requiredRoles.join(', ')} exist`,
      async () => {
        const existing = (await kc('GET', `/${realm}/clients/${client.id}/roles`)).map(r => r.name);
        return requiredRoles.some(role => !existing.includes(role));
      },
      async () => {
        const existing = (await kc('GET', `/${realm}/clients/${client.id}/roles`)).map(r => r.name);
        for (const role of requiredRoles.filter(r => !existing.includes(r))) {
          await kc('POST', `/${realm}/clients/${client.id}/roles`, {name: role});
        }
      });

    // 5. The scope that puts tenant + roles into the token, cloned from BS and attached as a default scope
    const scopeNamed = async name => (await kc('GET', `/${realm}/client-scopes`)).find(s => s.name === name);
    const roleScopeNames = [ROLE_SCOPE, 'esc-client-scope']; // WIN names the same scope differently
    await step(`client scope carrying tenant + roles is a default scope of ${PROXY_CLIENT}`,
      async () => {
        const defaults = (await kc('GET', `/${realm}/clients/${client.id}/default-client-scopes`)).map(s => s.name);
        return !defaults.some(name => roleScopeNames.includes(name));
      },
      async () => {
        let scope = await scopeNamed(ROLE_SCOPE);
        if (!scope) {
          const bsScope = (await kc('GET', '/bs/client-scopes')).find(s => s.name === ROLE_SCOPE);
          await kc('POST', `/${realm}/client-scopes`, {
            ...stripIds(bsScope),
            protocolMappers: (bsScope.protocolMappers ?? []).map(stripIds),
          });
          scope = await scopeNamed(ROLE_SCOPE);
        }
        await kc('PUT', `/${realm}/clients/${client.id}/default-client-scopes/${scope.id}`);
      });
  }

  // 6. An Identity Provider Redirector with a default provider sends every login to that IdP. Locally the
  //    vacme IdP is unreachable, so the password form never appears. Removing the default keeps the IdP as a
  //    button on the login page, which is how the BS realm is set up.
  const realmRepresentation = await kc('GET', `/${realm}`);
  const executions = await kc('GET', `/${realm}/authentication/flows/${encodeURIComponent(realmRepresentation.browserFlow)}/executions`);
  for (const execution of executions.filter(e => e.providerId === 'identity-provider-redirector' && e.authenticationConfig)) {
    await step(`browser flow does not auto-redirect to an IdP (restore: set defaultProvider on the redirector of flow "${realmRepresentation.browserFlow}")`,
      async () => !!(await kc('GET', `/${realm}/authentication/config/${execution.authenticationConfig}`)).config?.defaultProvider,
      async () => kc('DELETE', `/${realm}/authentication/config/${execution.authenticationConfig}`));
  }

  return pending;
}

async function ensureUser(realm, tenantClaim, user, password) {
  const client = await findClient(realm, PROXY_CLIENT);
  const existing = (await kc('GET', `/${realm}/users?exact=true&username=${encodeURIComponent(user.email)}`))[0];
  const representation = {
    username: user.email,
    email: user.email,
    firstName: user.firstName,
    lastName: user.lastName,
    enabled: true,
    emailVerified: true,
    requiredActions: [],
    attributes: {...(existing?.attributes ?? {}), tenant: [tenantClaim]},
  };
  if (existing) {
    await kc('PUT', `/${realm}/users/${existing.id}`, {...existing, ...representation});
  } else {
    await kc('POST', `/${realm}/users`, representation);
  }
  const {id} = (await kc('GET', `/${realm}/users?exact=true&username=${encodeURIComponent(user.email)}`))[0];
  await kc('PUT', `/${realm}/users/${id}/reset-password`, {type: 'password', value: password, temporary: false});

  // exactly the listed client roles: a leftover role would silently change what the e2e user may do
  const assigned = await kc('GET', `/${realm}/users/${id}/role-mappings/clients/${client.id}`);
  const available = await kc('GET', `/${realm}/clients/${client.id}/roles`);
  const toRemove = assigned.filter(role => !user.roles.includes(role.name));
  const toAdd = available.filter(role => user.roles.includes(role.name) && !assigned.some(a => a.name === role.name));
  if (toRemove.length) {
    await kc('DELETE', `/${realm}/users/${id}/role-mappings/clients/${client.id}`, toRemove);
  }
  if (toAdd.length) {
    await kc('POST', `/${realm}/users/${id}/role-mappings/clients/${client.id}`, toAdd);
  }
  const missing = user.roles.filter(role => !available.some(a => a.name === role));
  if (missing.length) {
    throw new Error(`[${realm}] client ${PROXY_CLIENT} has no role(s) ${missing.join(', ')}`);
  }
}

function writeE2eEnv(values, tenants) {
  const lines = [
    '# GENERATED by apps/esc-app-e2e/scripts/provision-local-users.mjs for the LOCAL docker-compose stack.',
    '# Local-only test users; do not commit (gitignored). Re-run the script to repair Keycloak or add a tenant.',
    '# Select the tenant with E2E_TENANT (default bs). Values here take precedence over .env.',
    '',
  ];
  for (const tenantKey of Object.keys(TENANTS)) {
    const prefix = `${tenantKey.toUpperCase()}_`;
    const keys = Object.keys(values).filter(key => key.startsWith(prefix)).sort();
    if (!keys.length) {
      continue;
    }
    lines.push(`# ${tenantKey}${tenants.includes(tenantKey) ? '' : ' (not re-provisioned in the last run)'}`);
    for (const key of keys) {
      lines.push(`${key}=${values[key]}`);
    }
    lines.push('');
  }
  writeFileSync(E2E_ENV, lines.join('\n'), {mode: 0o600});
}

async function main() {
  const args = parseArgs(process.argv.slice(2));
  const values = readEnvFile(E2E_ENV);
  let blocked = false;

  for (const tenantKey of args.tenants) {
    const config = TENANTS[tenantKey];
    const prefix = `${tenantKey.toUpperCase()}_`;
    console.log(`${tenantKey}: realm ${config.realm}`);

    const pending = await ensureRealm(config, args.fixRealms);
    if (pending.length) {
      blocked = true;
      console.log(`  [${config.realm}] NOT provisioned - missing (re-run with --fix-realms to apply):`);
      pending.forEach(item => console.log(`    - ${item}`));
      continue;
    }

    // forget users this tenant no longer has, so the e2e setup does not try to log them in
    const configured = new Set([`${prefix}BASE_URL`,
      ...config.users.flatMap(user => [`${prefix}${user.env}_USER`, `${prefix}${user.env}_PASSWORD`])]);
    Object.keys(values).filter(key => key.startsWith(prefix) && !configured.has(key)).forEach(key => delete values[key]);

    for (const user of config.users) {
      const passwordKey = `${prefix}${user.env}_PASSWORD`;
      values[passwordKey] ||= randomBytes(18).toString('base64url');
      await ensureUser(config.realm, config.tenantClaim, user, values[passwordKey]);
      values[`${prefix}${user.env}_USER`] = user.email;
      console.log(`  ${user.env.padEnd(18)} ${user.email} [${user.roles.join(', ')}]`);
    }
    values[`${prefix}BASE_URL`] = config.baseUrl;
  }

  writeE2eEnv(values, args.tenants);
  console.log(`wrote ${path.relative(process.cwd(), E2E_ENV)}`);
  if (blocked) {
    process.exitCode = 2;
  }
}

main().catch(error => {
  console.error(`provisioning failed: ${error.message}`);
  process.exit(1);
});
