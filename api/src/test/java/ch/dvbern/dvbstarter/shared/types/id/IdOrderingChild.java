package ch.dvbern.dvbstarter.shared.types.id;

import java.io.Serial;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IdOrderingChild extends AbstractEntity<IdOrderingChild> {

    @Serial
    private static final long serialVersionUID = 1L;


    @OrderBy
    @Column(nullable = false)
    private int orderIndex;

    @ManyToOne(cascade = CascadeType.ALL)
    private IdOrderingParent parent;

}
