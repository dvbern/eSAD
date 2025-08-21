package ch.dvbern.dvbstarter.shared.types.id;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = A.TABLE_NAME)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class A extends AbstractEntity<A> {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String TABLE_NAME = "testing_idtype_a";

    // The bug only occurs on EAGER load, so keep it that way!
    @ManyToOne
    //    @OneToOne
    private B b;
}
