package ch.dvbern.oss.hemed.esc.api.shared.plz;

import java.io.Serial;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table(indexes = @Index(name = "ix_plzdata_plz", columnList = "plz"))
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PLZData extends AbstractEntity<PLZData> {

    @Serial
    private static final long serialVersionUID = -3297584486691688796L;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String plz;

    @NotEmpty
    @NonNull
    @Column(nullable = false, length = DbConst.DB_DEFAULT_MAX_LENGTH)
    @Size(max = DbConst.DB_DEFAULT_MAX_LENGTH)
    private String ortsBezeichnung;

    @NotEmpty
    @NonNull
    @Column(nullable = false, length = 2)
    @Size(max = 2)
    private String kanton;

    @NotNull
    @NonNull
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private String plzTyp;
}
