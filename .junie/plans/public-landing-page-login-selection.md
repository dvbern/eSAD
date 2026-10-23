# Public Landing Page with Login Method Selection — Option 1 Hybrid

> **Decision record.** Originally planned as a full SPA-owned login flow (landing page with
> hint replay, login gate, ingress swap). After reviewing the committed Steps 1–4 the
> architecture was pivoted to **"Option 1 hybrid"**:
>
> - The **load-bearing method selection happens on the Keycloak login page of the tenant
>   realm** (server-side, works for deep links and session expiry with no SPA involvement).
> - The public app stays as the **branded info page with per-IdP buttons** (customer
>   requirement), sending `kc_idp_hint` so a click skips the Keycloak selection page.
> - The **"no-deploy" runtime-configurability requirement was dropped**: changing the login
>   offer is a gitops change (esc-keycloak deploy), the DB `loginMethods` list remains as a
>   *button list source* for the info page and is Support-editable, with the documented
>   dual-truth discipline below.
> - Consequences: **no login gate, no ingress swap, no bounce counter, no hint replay
>   store, no identity_provider claim mapper** (former Steps 6–9 are deleted; deep links
>   and expiry are solved structurally by Keycloak).

## Requirements

1. `/publicapp/` explains the IdPs (per-tenant texts) and renders one button per
   `LoginMethod` configured for the tenant, in `LoginOptionJson.order`, with translated
   labels. A click starts `/oauth2/start?rd=<sanitised rd>&kc_idp_hint=<idpHint>`
   (`kc_idp_hint=` explicitly empty for `PASSWORD`).
2. An already-authenticated visitor of `/publicapp/` is forwarded to `MAIN_APP_ROOT` (`/`).
3. Deep links and session expiry are handled **by Keycloak**: the tenant realm's login page
   offers exactly its enabled brokers (be: no `defaultProvider`; bs/win keep
   `defaultProvider` → zero-click). The 401-interceptor restarts the flow with a relative
   `rd` and **no** hint — the selection page appears.
4. Support can read/write `loginMethods` (non-empty enforced server- and client-side);
   this list drives the info-page buttons only, not the Keycloak offer.
5. `PASSWORD` is seeded together with `DVBERN_HEMED_ENTRA` for all tenants (DEV/UAT convenience).
   On tenants with a single effective login method Keycloak keeps `defaultProvider` and
   redirects straight to the hemed realm, so the seeded `PASSWORD` button is never reached
   there; Support removes it from `loginMethods` where it must not be offered.

## Implemented (Steps 1–4, adjusted for the pivot)

- `LoginMethod` enum (`PASSWORD, AGOV_ZH, BE_LOGIN, HIN, DVBERN_HEMED_ENTRA, WINTERTHUR_ADFS`);
  `TenantSetting.loginMethods` column (einladungStatusValues pattern);
  `LoginOptionJson(method, idpHint, order)`; `TenantSettingsResource` fills `loginOptions`
  via `LoginMethodIdpHintResolver` and rejects empty `PUT`s; Support-UI multi-select with
  required-at-least-one validator; Liquibase `db/base/0085-tenant-settings-login-methods`
  seeds `PASSWORD,DVBERN_HEMED_ENTRA` for **all** tenants (no db/be changeset — BeLogin/HIN are
  Support-enabled after Step 5).
- `LoginMethodIdpHintResolver` keeps the stage-independent aliases as constants
  (`dvbern-hemed-entra-client`, `hin-idp`, `agov`, `WinterthurADFS`) and derives only the
  stage-dependent BeLogin alias from the single stage source `app.deployment.environment`
  (`belogin-replica` / `belogin-test` / `belogin`) — no extra `kc-env` property. Every
  alias can additionally be overridden per environment via `esc.login-method.<NAME>.alias`
  (`ConfigService#getLoginMethodAlias`), which is the escape hatch if an esc broker is
  renamed before this plan is finished.
- `OAuth2ProxyService`: `startOAuthFlow(rd?, params?)` preserving empty values,
  three-state `checkAuthentication()` (`no-store`), `toRelativeUrl()` as the only `rd`
  producer, `signIn` slash fix.
- Publicinfo page: buttons + sanitised `rd` threading + authenticated forwarding.
  **No auto-start, no `select=1`, no back/forward guard, no persisted hint** — the page is
  a deliberate entry point, not the load-bearing selector.
- Interceptor: on 401 + confirmed unauthenticated → `startOAuthFlow(toRelativeUrl(…))`
  with no params (Keycloak offers the methods; `rd` returns the user to their page).

## Remaining steps

### Step 5 — esc-keycloak: brokers, realm config, login theme

- One broker per tenant-relevant external IdP, cloned from `-hemed-entra-client`, each
  hardwiring its downstream `kc_idp_hint` towards the hemed realm: be → `belogin`
  (`belogin-replica`/`belogin-test`/`belogin` per stage) + `hin-idp`; win → `adfs-win`;
  bs unchanged.
- be realm: **remove `defaultProvider`** so the login page renders the broker buttons;
  PROD-be disables the username/password subflow (no local users) — DEV/UAT keep it.
- bs/win realms: keep `defaultProvider` (entra) → zero-click unchanged.
- hemed realm: `hideOnLogin: true` on its IdPs as guard rails (it must never be
  interactive — every esc broker hardwires the downstream hint).
- One **per-realm login theme** (texts, help link, footer); alias values must match the
  resolver byte-for-byte (triple-match check resolver ↔ esc broker ↔ allow-list).

### Step 6 — deployment-config: allow-list for the info-page buttons

- Extend `providers[].loginURLParameters[kc_idp_hint].allow` for **be** (dev/uat/prod;
  win if it wants buttons) with `belogin*`/`hin-idp` aliases and the current
  `dvbern-hemed-entra-client` alias (the old `vacme-<stage>-hemed-entra-client` values in
  the deployed `values-*.yaml` are stale). bs needs nothing. `value: ""` for the empty
  PASSWORD hint works — verified locally, the proxy forwards `kc_idp_hint=` unchanged.
- Two pitfalls found while doing this locally (both apply to the deployed values files):
  the alpha loader is **strict**, an unknown key aborts the start, and
  `injectRequestHeaders[].values[]` takes `claim`/`prefix` **directly** — the
  `claimSource:` wrapper currently in `environments/{uat,prod}/values-*.yaml` fails with
  `json: unknown field "claimSource"`. `${ENV_VAR}` inside the alpha config *is* expanded
  by oauth2-proxy itself, so `clientSecret: "${OAUTH2_PROXY_CLIENT_SECRET}"` is fine.
- Local counterpart (done): **all three** proxies in `developer-local-settings-template`
  run the alpha config as their **only** config file
  (`config/oauthproxy-<be|bs|win>/oauth2-proxy-alpha.yaml`); the legacy
  `oauth2-proxy.cfg` files were deleted and the options the alpha schema does not model
  (cookies, skips, `redirect_url`, email domains, logout whitelist) are now CLI flags in
  `docker-compose.yml` — the local equivalent of `extraArgs` in the deployed values
  files. Regex skips with a `$` anchor need `$$` there (docker compose interpolation).
  Each allow-list carries the LOCAL resolver defaults (incl. `WinterthurADFS`, first for
  win) plus the alias the local realm import creates (`REALM_IDP_VACME_ALIAS`, default
  `vacme-dev`); the lists are deliberately identical so a hint never disappears just
  because you switched the local tenant you test against.
- Add the curl smoke check (`/oauth2/start?rd=/&kc_idp_hint=<alias>` → Location carries
  the alias; bogus alias → absent) and run it per environment after every change. This is
  the dual-truth guard: a button whose alias is not allow-listed / not a broker alias
  degrades to the Keycloak selection page (harmless) — the smoke check detects it.

### Step 7 — enablement (per environment, Support UI)

- After Step 5 + 6 are verified on an environment: Support adds `HIN`, `BE_LOGIN` to the
  be tenant's `loginMethods`. Optionally removes `DVBERN_HEMED_ENTRA` for be once Bern's own
  methods are confirmed. Order matters: brokers/allow-list first, buttons second.

## Dual-truth discipline (accepted risk)

Two sources describe the login offer: **enabled brokers in the esc realm** (truth for
deep links/expiry — Keycloak renders them) and **`loginMethods` in the DB** (truth for
the info-page buttons). Drift is tolerated but must be harmless: a DB-listed method whose
broker/allow-list entry is missing degrades to the Keycloak selection page, never a dead
end (unknown hint → redirector falls through → login page with its buttons). The Step 6
smoke check is the detection mechanism; Step 7's ordering is the prevention mechanism.

## What was removed by the pivot (for the record)

`LoginMethodStore` (pending/promote/confirmed hint replay), landing-page auto-start rules
and their guards, `identity_provider` protocol mapper + claim reconciliation, the login
gate + bounce counter, the public-ingress/ingress-swap runbooks (former Step 8), the
`forward_auth` decision doc (former Step 9), the `app.deployment.kc-env` property, and
the `db/be` seed changeset. Deep links, session expiry and the anonymous front door are
all handled server-side by proxy + Keycloak realm config — the entire Step-8 risk class
(route ordering, public shell exposure, gate bounce) does not exist in this architecture.
