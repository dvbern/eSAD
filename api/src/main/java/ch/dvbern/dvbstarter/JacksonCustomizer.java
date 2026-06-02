package ch.dvbern.dvbstarter;

import ch.dvbern.dvbstarter.shared.inputvalidation.StringNormalizerModule;
import ch.dvbern.dvbstarter.shared.math.CalculationConverter;
import ch.dvbern.dvbstarter.shared.math.DbNumberConverter;
import ch.dvbern.dvbstarter.shared.math.FrankenEinerConverter;
import ch.dvbern.dvbstarter.shared.math.FrankenFuenferConverter;
import ch.dvbern.dvbstarter.shared.math.GanzZahlConverter;
import ch.dvbern.dvbstarter.shared.math.ZahlConverter;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileNameConverter;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddressConverter;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapperConverter;
import ch.dvbern.dvbstarter.shared.types.id.IDConverter;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumberConverter;
import ch.dvbern.dvbstarter.shared.types.semver.SemverConverter;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerConverter;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidIdConverter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

/**
 * See <a href="https://quarkus.io/guides/rest-json#jackson">Quarkus Jackson Customization</a>
 */
@Singleton
@SuppressWarnings("unused")
public class JacksonCustomizer implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper mapper) {
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setConstructorDetector(ConstructorDetector.USE_PROPERTIES_BASED);

        SimpleModule module = new SimpleModule("esc.api");

        EmailAddressConverter.registerJackson(module);
        CleanFileNameConverter.registerJackson(module);
        IDConverter.registerJackson(module);
        HasUntersuchungIDWrapperConverter.registerJackson(module);

        CalculationConverter.registerJackson(module);
        DbNumberConverter.registerJackson(module);
        FrankenEinerConverter.registerJackson(module);
        FrankenFuenferConverter.registerJackson(module);
        GanzZahlConverter.registerJackson(module);
        ZahlConverter.registerJackson(module);
        NormalizedPhoneNumberConverter.registerJackson(module);
        SozialversicherungsNummerConverter.registerJackson(module);
        EdokidIdConverter.registerJackson(module);

        SemverConverter.registerJackson(module);

        mapper.registerModule(module);
        mapper.registerModule(new StringNormalizerModule()); // all Strings will be trimmed
    }

}
