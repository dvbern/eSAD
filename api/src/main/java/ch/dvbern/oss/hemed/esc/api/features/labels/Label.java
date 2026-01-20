package ch.dvbern.oss.hemed.esc.api.features.labels;

import java.io.Serial;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.DbConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import ch.dvbern.oss.hemed.esc.api.shared.mandant.TranslatedString;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.envers.Audited;

@Entity
@Table(
    indexes = {
        @Index(name = "ix_label_name", columnList = "nameDe, id"),
        @Index(name = "ix_label_name", columnList = "nameFr, id"),
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uc_label_nameDe_labelTyp", columnNames = { "nameDe", "labelTyp" }),
        @UniqueConstraint(name = "uc_label_nameFr_labelTyp", columnNames = { "nameFr", "labelTyp" }),
    }
)
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Label extends AbstractEntity<Label> implements EntityWithOptimisticLocking {

    @Serial
    private static final long serialVersionUID = 8005202370526715032L;

    // this id constant is also used in LabelStore
    public static final ID<Label>
        ID_LABEL_IMPFAUSWEIS = ID.of(UUID.fromString("350f2746-7db4-4dfa-ae28-1b3b5812434a"), Label.class);
    public static final ID<Label>
        ID_LABEL_FRAGEBOGEN = ID.of(UUID.fromString("f8514824-9694-408e-bdb6-d72e376d7833"), Label.class);
    public static final ID<Label>
        ID_LABEL_DOPPELKONTROLLE_IMPFENTSCHEID =
        ID.of(UUID.fromString("5df501a0-b7c5-45b6-a531-82d44541cdef"), Label.class);
    public static final ID<Label>
        ID_LABEL_DISPENSATIONSGESUCH = ID.of(UUID.fromString("345d4d49-ef8d-4236-94dc-3e35f4bdf76c"), Label.class);
    public static final ID<Label>
        ID_LABEL_ABMELDUNGSGRUND = ID.of(UUID.fromString("8a322ffa-55e8-40d9-8a75-8969a892febe"), Label.class);
    public static final ID<Label>
        ID_LABEL_GRUPPEN_FOLGETERMIN = ID.of(UUID.fromString("5d6870a9-50f5-466b-aee6-8810bfcf3f0d"), Label.class);
    public static final ID<Label> ID_LABEL_NACHZUEGLER =
        ID.of(UUID.fromString("4db33316-7779-4c2e-851a-139e18458e7c"), Label.class);
    public static final ID<Label> ID_LABEL_ANMELDUNG_BESTAETIGUNG =
        ID.of(UUID.fromString("e84f6525-0f70-4440-bb62-11fd25fea5b1"), Label.class);
    public static final ID<Label> ID_LABEL_DATENVERLUST_NOTIZ =
        ID.of(UUID.fromString("9a1c9a97-b7dc-4e3d-bb22-8e8250d51768"), Label.class);

    @NonNull
    @NotNull
    @Valid
    @Embedded
    private TranslatedString name;

    @NotNull
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = DbConst.DB_ENUM_LENGTH)
    private LabelTyp labelTyp;

    // Whether this label is automatically set by the system,
    // This is not mutually exclusive with the "disabled" property.
    @NotNull
    @Column(nullable = false)
    private boolean autoLabel = false;

    // Whether this label can be selected manually
    @NotNull
    @Column(nullable = false)
    private boolean disabled = false;

    // Whether the label is generally still in use
    @NotNull
    @Column(nullable = false)
    private boolean aktiv = true;
}
