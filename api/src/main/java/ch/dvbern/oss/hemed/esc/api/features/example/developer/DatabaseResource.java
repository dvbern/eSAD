package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DBInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/db")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_DEVELOPER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class DatabaseResource {

    private final ConfigService configService;
    private final DatabaseService databaseService;


    @DELETE
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void resetDatabase() {
        configService.validateNotProdAndDbResetFeatureEnabled();
        databaseService.resetAndReinitDatabasePreservingHibernateSequenceValue();
    }

    @POST
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void initDatabase() {
        configService.validateNotProdAndDbResetFeatureEnabled();
        databaseService.initDatabase();
    }

    @Path("dbcheck")
    @GET
    @RolesAllowed(BerechtigungName.SUPPORT)
    public DBInfoJson returnDbInfo(){
        return new DBInfoJson(databaseService.getDBVersion(), databaseService.getDBInfo());
    }
}
