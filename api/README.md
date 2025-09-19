# esc-api

Module: api

Kunde: hemed

Project: esc

Local Dev-Hostname: local-esc.localhost

## Getting Started

### Installation (quick-start)

1. Add **hostname** entries to `/etc/hosts` (windows: `c:\Windows\System32\drivers\etc\hosts`) in the form: \
   `127.0.0.1 local-esc-bs.localhost` and `127.0.0.1 local-esc-win.localhost`  
2. **Generate a new keypair** for HTTPS communication: `scripts/certs/generate-keypair.sh` (windows: `scripts\certs\generate-keypair.ps1`)
3. **Import certificate** to the correct Java (e.g. Windows as Admin user: 
```
cd C:\Users\bran\java\IdeaProjects\edokid\esc\developer-local-settings\config
C:\Users\bran\.jdks\temurin-21.0.4\bin/keytool -importcert -file certs/local-esc-bs.localhost.pem -cacerts -keypass changeit -storepass changeit -noprompt -alias esc-local-bs
```
   )
3. **Copy**  `.env-template` to `.env` and adjust secrets/config
4. **Copy** `developer-local-settings-template` to `developer-local-settings` and adjust secrets/config (do not overwrite the generated certs)
5. Search everywhere for **CHANGEME** and replace it with the correct values (usually: choose your personal credentials, don't change the templates though ;-))
6. **Keycloak**: In order for the login to work correctly you will need a Keycloak Server and an OAuth Proxy. There is a docker-compose file in the root of the project that should start a keycloak, auto import a realm and start an oauth proxy. Start the stack with **docker-compose up**
6. Move to `esc/api`, then generate sources and **build** the application `./mvnw clean package`
7. You can now **start** the application `./mvnw compile quarkus:dev`
8. Search for **FIXME** in the code and make your choices

> **_NOTE:_**  Quarkus ships with a Dev UI, which is available in dev mode at https://localhost:8443/q/dev/ or https://local-esc-bs.localhost:8443/q/dev 

## Contributing Guidelines

Please read [CONTRIBUTING.md](../CONTRIBUTING.md) for the process for submitting pull requests to us.

## Code of Conduct

One healthy social atmosphere is very important to us, wherefore we rate our Code of Conduct high.
For details check the file [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md)

## Authors

* **DV Bern AG** - *Initial work* - [dvbern](https://github.com/dvbern)

See also the list of [contributors](https://github.com/dvbern/(RepoName)/contributors)
who participated in this project.

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE](../LICENSE) file for details.

## Related guides

- Hibernate ORM ([guide](https://quarkus.io/guides/hibernate-orm)): Define your persistent model with Hibernate ORM and Jakarta Persistence.
- Liquibase ([guide](https://quarkus.io/guides/liquibase)): Handle your database schema migrations.
- Flyway ([guide](https://quarkus.io/guides/flyway)): Handle your database schema migrations in an oldschool way.
- Hibernate Validator ([guide](https://quarkus.io/guides/validation)): Validate object properties (field, getter) and method parameters for your beans (REST, CDI, Jakarta Persistence).
- SmallRye OpenAPI ([guide](https://quarkus.io/guides/openapi-swaggerui)): Document your REST APIs with OpenAPI - comes with Swagger UI.
- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Logging GELF ([guide](https://quarkus.io/guides/centralized-log-management)): Log using the Graylog Extended Log Format and centralize your logs in ELK or EFK.
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health.
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC.
