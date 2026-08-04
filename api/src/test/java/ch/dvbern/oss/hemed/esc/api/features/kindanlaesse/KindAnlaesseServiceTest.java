package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.util.List;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.KindAnlassSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.EinladungBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class KindAnlaesseServiceTest {

    private final KindAnlaesseRepo repo = Mockito.mock(KindAnlaesseRepo.class, Mockito.CALLS_REAL_METHODS);
    private final EinladungService einladungService = Mockito.mock(EinladungService.class);
    private final AnlassService anlassService = Mockito.mock(AnlassService.class);
    private final KindAnlaesseService service = new KindAnlaesseService(repo, einladungService, anlassService);
    private final AuthorizerService authorizerService = Mockito.mock(AuthorizerService.class);
    // translates any AnlassTyp key to "KLASSE_4" so expected titles are deterministic
    private final TL tl = message -> "KLASSE_4";

    private final KindAnlaesseMapper mapper = new KindAnlaesseMapper(
        service,
        Mockito.mock(UntersuchungImpfstatusKrankheitService.class),
        Mockito.mock(UntersuchungImpfungInternService.class), tl);

    private final FixedClockProvider clockProvider = FixedClockProvider.now();
    private final AppClock appClock = new AppClock(clockProvider);

    Einladung einladung1 = EinladungBuilder.defaultEinladung(appClock.offsetDateTime().now().toLocalDate());
    Einladung einladung2 = EinladungBuilder.defaultEinladung(appClock.offsetDateTime().now().toLocalDate());

    @BeforeEach
    void setUp() {
        Mockito.reset(repo);
    }

    @Test
    void loadAndMapKindAnlaesse() {

        // ARRANGE
        Mockito.doAnswer(invocation -> List.of(einladung1, einladung2))
            .when(repo).findAllEinladungForKind(ArgumentMatchers.any());
        Mockito.doAnswer(invocation -> true)
            .when(authorizerService)
            .hasReadAuthorizationAnlass(ArgumentMatchers.any(Anlass.class), ArgumentMatchers.any());

        // ACT
        List<KindAnlassSummaryJson> mappedList = mapper.findAllForKind(ID.random(Kind.class), anlass -> true);

        // ASSERT
        assertThat(mappedList).hasSize(2);
        KindAnlassSummaryJson json1 = mappedList.getFirst();
        assertThat(json1.getAnlassId()).isEqualTo(einladung1.getAnlass().getId());
        assertThat(json1.getTitel()).isEqualTo(einladung1.getAnlass().getFullAnlassTitel(tl));
        assertThat(json1.getEinladungId()).isEqualTo(einladung1.getId());
    }

    @ParameterizedTest
    @CsvSource({
        "Mein Anlass, 2025/26 KLASSE_4 Mein Anlass",
        "          , 2025/26 KLASSE_4",
        "'   ',      2025/26 KLASSE_4",
    })
    void toKindAnlassSummaryJson_returns_expected_titel(String titel, String expectedTitel) {
        // ARRANGE
        Anlass anlass = AnlassBuilder.create(b -> b.withTitle(titel).withSchuljahr(2025));
        Einladung einladung = EinladungBuilder.create(b -> b.withAnlass(anlass));
        Mockito.doAnswer(invocation -> List.of(einladung))
            .when(repo).findAllEinladungForKind(ArgumentMatchers.any());

        // ACT
        List<KindAnlassSummaryJson> result = mapper.findAllForKind(ID.random(Kind.class), e -> true);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitel()).isEqualTo(expectedTitel);
    }
}
