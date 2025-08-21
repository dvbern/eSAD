package ch.dvbern.oss.hemed.esc.api.shared.person;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdresseMapper {

    @Nullable
    public static AdresseJson toJson(@Nullable Adresse adresse) {
        if (adresse == null) {
            return null;
        }
        return new AdresseJson(
            adresse.getStrasseUndNr(),
            adresse.getAdresseZusatz(),
            adresse.getPlz(),
            adresse.getOrt(),
            adresse.getLand());
    }

    public static @Valid @Nullable Adresse toEntity(@Nullable @Valid AdresseJson adresse) {
        if (adresse == null) {
            return null;
        }
        return new Adresse(
            adresse.getStrasseUndNr(),
            adresse.getAdresseZusatz(),
            adresse.getPlz(),
            adresse.getOrt(),
            adresse.getLand());
    }
}
