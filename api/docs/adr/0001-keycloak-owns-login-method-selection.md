---
status: accepted
---

# Keycloak owns login method selection; the public app only offers shortcut buttons

Users of a tenant can sign in with several identity providers (BeLogin, HIN, AGOV, DV Bern
Entra, Winterthur ADFS, and username/password on DEV/UAT). The tenant realm's Keycloak login
page makes the choice. This covers deep links and session expiry with no SPA code. The public
app (`/publicapp/`) stays a branded info page. Each of its buttons sends `kc_idp_hint`, so a
click skips the Keycloak selection page. We chose this because the SPA-owned alternative
needed a login gate, an ingress swap, a hint-replay store and a bounce counter just to
rebuild what Keycloak already does on the server.

See [api/docs/login-flow.md](../login-flow.md) for the flow diagrams.

## Considered Options

- **SPA-owned flow (rejected).** The landing page would be the only selector. Keycloak hides
  every IdP (`hideOnLogin: true`). A login gate in `appInitializer` would redirect anonymous
  deep links to `/publicapp/?rd=…`. The SPA would store the last-used hint in `localStorage`
  and replay it on 401, reconciled with an `identity_provider` claim mapper. This made
  "login offer editable without a deploy" possible. The costs:
  - the public ingress has to own the host, which makes the main-app shell publicly
    downloadable;
  - gate↔landing ping-pong has to be bounded with a `sessionStorage` counter;
  - every authenticated boot pays one extra blocking round trip;
  - a whole class of routing bugs appears (route ordering, public shell exposure, bounce
    loops).
- **Keycloak-owned selection, info page with hint buttons (chosen, "Option 1 hybrid").**
  Deep links, session expiry and the anonymous front door are handled by oauth2-proxy and
  the realm configuration. The SPA only restarts the OAuth flow with a relative `rd` and
  **no** hint.

## Consequences

- **Changing the login offer means deploying Keycloak.** The esc-keycloak gitops repo
  decides which brokers exist and whether a realm has a `defaultProvider`. We dropped the
  requirement that the offer be configurable at runtime without a deploy.
- **There are two sources of truth, and we accept it.** The brokers enabled in the realm
  decide what Keycloak shows. `TenantSetting.loginMethods` in the DB, editable by Support,
  decides which buttons the info page shows. The two can drift, but drift stays harmless:
  a button with an unknown or non-allow-listed hint lands on the Keycloak selection page,
  never on a dead end. Two things keep drift in check:
  - *Detection:* a curl smoke check (`/oauth2/start?rd=/&kc_idp_hint=<alias>`) run per
    environment.
  - *Prevention:* enable in this order: broker first, then the proxy allow-list, then the
    Support UI button.
- **Each alias must match in three places, byte for byte:** `LoginMethodIdpHintResolver`,
  the esc-realm broker alias, and the oauth2-proxy `kc_idp_hint` allow-list. If one
  differs, the hint is silently dropped.
- **The `PASSWORD` button needs a hint that is present but empty (`kc_idp_hint=`).**
  Keycloak's `identity-provider-redirector` treats an empty hint as "skip" and shows the
  username/password form. If the parameter is missing, Keycloak falls back to
  `defaultProvider` instead. The frontend therefore has to check whether the hint is
  *present* (`!== null`), not whether it is truthy. The proxy allow-list needs
  `value: ""`.
- **Realms behave differently.**
  - Bern has no `defaultProvider`, so its login page shows its broker buttons.
  - Basel and Winterthur keep `defaultProvider`, so login takes zero clicks.
  - The downstream hemed realm sets `hideOnLogin: true` and must never be interactive,
    because every esc broker passes its downstream hint along.

## Revisit

We may reconsider this decision later. The likely successor is the deferred end state
from the original plan: Caddy in front of oauth2-proxy, checking the session with
`forward_auth` and sending anonymous visitors to `/publicapp/`. This gives one branded front
door, one source of truth for the login offer that can be changed without a deploy, and no SPA
login gate. It still needs the ingress change and hint replay on re-auth. See "Alternative:
Caddy in front of oauth2-proxy" in [login-flow.md](../login-flow.md).
