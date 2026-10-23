# ESC E2E tests

## Prerequisites

Start the local ESC stack and make sure the API is running with the `dev` profile. Database reset is enabled in that profile by default through `%dev.app.allow-db-reset=true`. If the API is not using the `dev` profile, set `APP_ALLOW_DB_RESET=true` for the API process (or configure `app.allow-db-reset=true`).

## Configure users

For the local stack, let the provisioning script create the Keycloak users and write their credentials to `src/util/e2e.env`:

```bash
cd frontend
node apps/esc-app-e2e/scripts/provision-local-users.mjs --tenants bs,be,win --fix-realms
```

Select the tenant when running the tests:

```bash
E2E_TENANT=bs npm run playwright:esc-e2e
E2E_TENANT=be npm run playwright:esc-e2e
E2E_TENANT=win npm run playwright:esc-e2e
```

To configure users manually, copy `src/util/.env-template` to `src/util/.env` and fill in the user/password pairs. The available credentials are `ESC_ADMIN`, `ESC_EMPFANG`, `ESC_MPA`, `ESC_ORTHOPISTIN`, `ESC_SCHULAERZTIN`, `ESC_SUPERMPA`, and `ESC_SUPPORT`; each uses matching `_USER` and `_PASSWORD` variables. For multiple tenants, prefix variables with `BS_`, `BE_`, or `WIN_`, for example:

```dotenv
BE_ESC_SUPPORT_USER=escsupport@example.test
BE_ESC_SUPPORT_PASSWORD=secret
BE_BASE_URL=https://local-esc-be.localhost:6443/
```

Shell variables take precedence over `e2e.env`, which takes precedence over `.env`. `E2E_TENANT` defaults to `bs`; only `bs` also accepts unprefixed variables.

## Run the tests

From `frontend`:

```bash
npm run playwright:esc-e2e
```

Useful variants:

```bash
# One Playwright project
npm run playwright:esc-e2e -- --project=untersuchung

# One spec
npm run playwright:esc-e2e -- src/e2e/untersuchung/untersuchung.e2e.spec.ts

# Interactive UI
npm run playwright-ui:esc-e2e

# Keep the existing database
E2E_RESET_DB=false npm run playwright:esc-e2e

# Open the HTML report
npm run playwright-report
```

The reset setup uses the `ESC_SUPPORT` credentials to call `DELETE /api/db`. `E2E_RESET_DB=false` skips the reset; it does not enable the API permission. The reset endpoint currently resets tenant `bs`.
