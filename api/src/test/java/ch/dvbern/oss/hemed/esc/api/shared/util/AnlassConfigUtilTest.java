package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
@Slf4j
class AnlassConfigUtilTest {

    private final AnlassConfigService anlassConfigService;

    @Test
    @TestAsSupportBS
    void createSqlUpdateQuery() {
        List<AnlassConfig> all = anlassConfigService.findAll();
        for (AnlassConfig anlassConfig : all) {
            String s = AnlassConfigUtil.createSqlUpdateQuery(anlassConfig, null, null);
            assertNotNull(s);
        }
    }

    @Test
    @TestAsSupportBS
    void createSqlInsertQuery() {
        List<AnlassConfig> all = anlassConfigService.findAll();
        for (AnlassConfig anlassConfig : all) {
            String s = AnlassConfigUtil.createSqlInsertQuery(anlassConfig, null, null);
            assertNotNull(s);
        }
    }

    @Test
    @TestAsSupportBS
    void createSqlUpdateQueryWithDefault() {
        List<AnlassConfig> all = anlassConfigService.findAll();
        for (AnlassConfig anlassConfig : all) {
            LOG.info(anlassConfig.getAnlassTyp().name());
            String s = AnlassConfigUtil.createSqlUpdateQuery(anlassConfig, RequirementSetting.NOT_VISIBLE, false);
            assertNotNull(s);
        }
    }

    @Test
    @TestAsSupportBS
    void createSqlInsertQueryWithDefault() {
        List<AnlassConfig> all = anlassConfigService.findAll();
        for (AnlassConfig anlassConfig : all) {
            String s = AnlassConfigUtil.createSqlInsertQuery(anlassConfig, RequirementSetting.OPTIONAL, true);
            assertNotNull(s);
        }
    }
}
