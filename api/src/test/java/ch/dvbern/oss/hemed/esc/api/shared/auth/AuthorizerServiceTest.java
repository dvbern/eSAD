package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfang;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpa;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class AuthorizerServiceTest {

    private final AuthorizerService authorizerService;
    private final FixtureService fixtureService;

    @Test
    @TestAsSupport
    void checkWriteAuthorizationAnlass_anlass_offen_SUPPORT() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsSupport
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_SUPPORT() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsAdminBS
    void checkWriteAuthorization_anlass_abgeschlossen_ADMIN() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsMpa
    void checkWriteAuthorizationAnlass_anlass_offen_MPA() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsMpa
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_MPA() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()));
    }

    @Test
    @TestAsSupport
    void checkAuthorizationNotiz_Fragebogen_SUPPORT() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_FRAGEBOGEN));
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationNotiz(notiz.getId()))
            .doesNotThrowAnyException();
        assertThatCode(() -> authorizerService.requireWriteAuthorizationNotiz(notiz.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsSupport
    void checkAuthorizationNotiz_Dispensationsgesuch_SUPPORT() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_DISPENSATIONSGESUCH));
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationNotiz(notiz.getId()))
            .doesNotThrowAnyException();
        assertThatCode(() -> authorizerService.requireWriteAuthorizationNotiz(notiz.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsEmpfang
    void checkAuthorizationNotiz_Fragebogen_EMPFANG() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_FRAGEBOGEN));
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireReadAuthorizationNotiz(notiz.getId()));
        assertThrows(Exception.class, () -> authorizerService.requireWriteAuthorizationNotiz(notiz.getId()));
    }

    @Test
    @TestAsEmpfang
    void checkAuthorizationNotiz_Dispensationsgesuch_EMPFANG() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_DISPENSATIONSGESUCH));
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationNotiz(notiz.getId()))
            .doesNotThrowAnyException();
        assertThatCode(() -> authorizerService.requireWriteAuthorizationNotiz(notiz.getId()))
            .doesNotThrowAnyException();
    }
}
