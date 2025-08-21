package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Important, synced to frontend enum Path!!!
 * If you change a *value* here, make sure to check the equivalent enum Path in the frontend!
 */
@Getter
public enum Path {
	ANLASS("anlass"),
	ANLASS_UNTERSUCHUNG("untersuchung"),
	ANLASS_UNTERSUCHUNG_EINLADUNG("einladung"),
	ANLASS_UNTERSUCHUNG_EINLADUNG_IDS_SEGMENT("einladungIds"),
	ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN("impfungen");

	@NonNull
	private final String value;

	Path(@NonNull String value) {
        this.value = value;
	}
}
