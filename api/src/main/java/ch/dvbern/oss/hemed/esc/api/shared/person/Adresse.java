package ch.dvbern.oss.hemed.esc.api.shared.person;

import java.io.Serial;
import java.io.Serializable;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;

@Embeddable
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Adresse implements Serializable {

    @Serial
    private static final long serialVersionUID = -3712635907225593561L;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String strasseUndNr;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String adresseZusatz;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    @ToString.Include
    private String plz;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String ort;

    @Nullable
    @Column(nullable = true, length = DbConst.DB_ENUM_LENGTH)
    @Enumerated(EnumType.STRING)
    private Land land;

    @Nullable
    public Pair<String, Optional<String>> getStrasseUndNrAsParts() {
        if (strasseUndNr == null) {
            return null;
        }

        String trimmed = strasseUndNr.trim();

        if (trimmed.isEmpty()) {
            return null;
        }
        // Find the last occurrence of whitespace
        int lastSpaceIndex = -1;
        for (int i = trimmed.length() - 1; i >= 0; i--) {
            if (Character.isWhitespace(trimmed.charAt(i))) {
                lastSpaceIndex = i;
                break;
            }
        }
        // If no whitespace found, return as is
        if (lastSpaceIndex == -1) {
            return Pair.of(trimmed, Optional.empty());
        }
        // Split into two parts
        String streetPart = trimmed.substring(0, lastSpaceIndex).trim();
        String numberPart = trimmed.substring(lastSpaceIndex + 1).trim();
        return Pair.of(streetPart, Optional.of(numberPart));

    }

    @Nullable
    public String getStrasseUndNrNonBreakingSpaceSeparated() {
        Pair<String, Optional<String>> pair = getStrasseUndNrAsParts();
        if (pair == null) {
            return null;
        }
        // concat with non-breaking-space
        Optional<String> nrOpt = pair.getRight();
        return nrOpt.map(nr -> pair.getLeft() + '\u00A0' + nr).orElse(pair.getLeft());

    }


}
