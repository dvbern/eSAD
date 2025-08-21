package ch.dvbern.dvbstarter.shared.types.id;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = B.TABLE_NAME)
@Getter
@Setter
@NoArgsConstructor
public class B extends AbstractEntity<B> {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "testing_idtype_b";
}
