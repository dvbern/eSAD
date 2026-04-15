package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_organisation_verantwortlicherarzt", columnList = "verantwortlicherArzt_id, id"),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Organisation extends AbstractEntity<Organisation> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 810592335992415696L;

    public static final ID<Organisation> ZENTRALE_ORGANISATION_ID =
        ID.parse("08831a31-a62b-4c81-adfc-3f3bcb3a1fee", Organisation.class);


    @NonNull
    @OneToOne(optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_organisation_benutzer"), nullable = false)
    private Benutzer verantwortlicherArzt;

    @NonNull
    @Valid
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<OrganisationRollen> mitglieder = new HashSet<>();


    public void addMitglied(@NonNull OrganisationRollen mitglied) {
        if (this.getMitglieder().isEmpty()) {
            this.setMitglieder(Set.of(mitglied));
        } else {
            this.getMitglieder().add(mitglied);
        }
    }
}
