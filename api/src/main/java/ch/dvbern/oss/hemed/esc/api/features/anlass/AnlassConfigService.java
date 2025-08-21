package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class AnlassConfigService {

    private final AnlassConfigRepo anlassConfigRepo;


    @NonNull
    public AnlassConfig getByAnlassTypAndStichtag(@NonNull AnlassTyp anlassTyp, @NonNull LocalDate stichtag) {
        return anlassConfigRepo.getByAnlassTypAndStichtag(anlassTyp, stichtag);
    }
}
