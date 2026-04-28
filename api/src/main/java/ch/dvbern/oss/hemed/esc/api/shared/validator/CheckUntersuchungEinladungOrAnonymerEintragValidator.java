package ch.dvbern.oss.hemed.esc.api.shared.validator;

import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CheckUntersuchungEinladungOrAnonymerEintragValidator extends AbstractValidator<CheckUntersuchungEinladungOrAnonymerEintrag, Untersuchung> {

	@Override
	public boolean isEntityValid(@NonNull Untersuchung untersuchung) {
		boolean hasEinladung = untersuchung.getEinladungOptional().isPresent();
		boolean hasAnonymerEintrag = untersuchung.getAnonymerEintrag() != null;
		return hasEinladung != hasAnonymerEintrag;
	}
}
