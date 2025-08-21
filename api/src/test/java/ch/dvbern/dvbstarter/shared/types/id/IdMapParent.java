package ch.dvbern.dvbstarter.shared.types.id;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class IdMapParent extends AbstractEntity<IdMapParent> {

    @Serial
    private static final long serialVersionUID = 1L;


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey
    @OrderBy("orderIndex ASC")
    private Map<ID<IdMapChild>, IdMapChild> children = new HashMap<>();
}
