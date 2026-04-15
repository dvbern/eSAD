package ch.dvbern.dvbstarter.shared.types.id;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IdMapChild extends AbstractEntity<IdMapChild> {

    @Serial
    private static final long serialVersionUID = 1L;


    @Column(nullable = false)
    private int orderIndex;

    @ManyToOne(cascade = CascadeType.ALL)
    private IdMapParent parent;

}
