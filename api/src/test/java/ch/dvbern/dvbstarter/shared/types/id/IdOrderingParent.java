package ch.dvbern.dvbstarter.shared.types.id;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IdOrderingParent extends AbstractEntity<IdOrderingParent> {

    @Serial
    private static final long serialVersionUID = 1L;


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<IdOrderingChild> children = new ArrayList<>();
}
