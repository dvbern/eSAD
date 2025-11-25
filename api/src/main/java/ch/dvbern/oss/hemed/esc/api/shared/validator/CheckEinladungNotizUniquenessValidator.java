package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckEinladungNotizUniquenessValidator extends AbstractValidator<CheckEinladungNotizUniqueness, Einladung> {

    @Override
    public boolean isEntityValid(@NonNull Einladung einladung) {
        List<Notiz> notizen = einladung.getNotizen();
        if (notizen.isEmpty()) {
            return true;
        }

        return notizen.stream()
            .filter(notiz -> notiz.getTyp().isUnique())
            .allMatch(notiz -> notizen.stream().filter(n -> n.getTyp() == notiz.getTyp()).count() == 1);
    }
}
