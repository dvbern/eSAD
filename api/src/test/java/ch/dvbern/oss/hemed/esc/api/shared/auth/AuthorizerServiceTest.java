package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsEmpfangBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsLausbeauftragteWIN;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpaBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsPfpWIN;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
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
    @TestAsSupportBS
    void checkWriteAuthorizationAnlass_anlass_offen_SUPPORT() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsSupportBS
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
    @TestAsMpaBS
    void checkWriteAuthorizationAnlass_anlass_offen_MPA() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsMpaBS
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_MPA() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()));
    }

    @Test
    @TestAsSupportBS
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
    @TestAsSupportBS
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
    @TestAsEmpfangBS
    void checkAuthorizationNotiz_Fragebogen_EMPFANG() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz(Set.of(Label.ID_LABEL_FRAGEBOGEN));
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireReadAuthorizationNotiz(notiz.getId()));
        assertThrows(Exception.class, () -> authorizerService.requireWriteAuthorizationNotiz(notiz.getId()));
    }

    @Test
    @TestAsLausbeauftragteWIN
    void checkAuthorizationAnlass_Impftermin_Lausbeauftragte() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.IMPFTERMIN);
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireReadAuthorizationAnlass(anlass.getId()));
    }

    @Test
    @TestAsLausbeauftragteWIN
    void checkAuthorizationAnlass_Lauskontrolle_Lausbeauftragte() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassTyp.LAEUSEKONTROLLE);
        // ACT / ASSERT
        assertThatCode(() -> authorizerService.requireReadAuthorizationAnlass(anlass.getId()))
            .doesNotThrowAnyException();
    }

    @Test
    @TestAsPfpWIN
    void checkWriteAuthorizationAnlass_anlass_abgeschlossen_PFP() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.ABGESCHLOSSEN);
        // ACT / ASSERT
        assertThrows(Exception.class, () -> authorizerService.requireWriteAuthorizationAnlass(anlass.getId()));
    }
}
