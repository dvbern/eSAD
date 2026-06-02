package ch.dvbern.dvbstarter.shared.types.id;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;
import java.util.UUID;

@Schema(
    type = SchemaType.STRING,
    format = "entity-id-with-class",
    pattern = "^[0-9a-fA-F\\-]{36}-(anonym|einladung)$",
    implementation = String.class,
    examples = "11111111-1111-1111-1111-111111111111-anonym"
)
public class HasUntersuchungIDWrapper {

    private static final Map<String, Class<? extends AbstractEntity<?>>> SHORTNAME_TO_CLASS = Map.of(
        "anonym", AnonymerEintrag.class,
        "einladung", Einladung.class
    );

    @Schema(hidden = true)
    @NonNull
    private final UUID uuid;

    @Schema(hidden = true)
    @NonNull
    private final Class<? extends AbstractEntity<?>> entityClass;

    private HasUntersuchungIDWrapper(@NonNull UUID uuid, @NonNull Class<? extends AbstractEntity<?>> entityClass) {
        this.uuid = uuid;
        this.entityClass = entityClass;
    }

    @NonNull
    public ID<Einladung> toEinladungId() {
        return toIdOfClass(Einladung.class);
    }

    @NonNull
    public ID<AnonymerEintrag> toAnonymerEintragId() {
        return toIdOfClass(AnonymerEintrag.class);
    }

    public boolean isAnonymerEintrag() {
        return entityClass.equals(AnonymerEintrag.class);
    }

    public <T extends AbstractEntity<T>> boolean isOfClass(@NonNull Class<T> requestedClass) {
        return entityClass.equals(requestedClass);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private <T extends AbstractEntity<T>> ID<T> toIdOfClass(@NonNull Class<T> requestedClass) {
        if (!isOfClass(requestedClass)) {
            throw new IllegalStateException(
                "Requested class missmatch - actual:"
                    + entityClass.getName()
                    + " - requested: "
                    + requestedClass.getName()
            );
        }

        return ID.of(uuid, (Class<T>) entityClass);
    }

    @NonNull
    public static HasUntersuchungIDWrapper fromString(@NonNull String value) {
        // Format: uuid-shortname (e.g. 724778e1-77ed-4b0d-95c6-0b0a95e099b6-anonym)
        // A UUID has exactly 5 groups separated by 4 hyphens: 8-4-4-4-12
        // Any additional hyphen-separated suffix after the UUID is the shortname

        if (value.length() <= 36) {
            throw new IllegalArgumentException(
                "Missing shortname suffix. Expected format: <uuid>-<shortname>. Known shortnames: "
                    + SHORTNAME_TO_CLASS.keySet()
            );
        }

        String uuidPart = value.substring(0, 36);
        String shortname = value.substring(37); // skip the '-' separator

        UUID uuid = UUID.fromString(uuidPart);
        Class<? extends AbstractEntity<?>> entityClass = SHORTNAME_TO_CLASS.get(shortname);

        if (entityClass == null) {
            throw new IllegalArgumentException(
                "Unknown shortname '" + shortname + "'. Known shortnames: " + SHORTNAME_TO_CLASS.keySet()
            );
        }

        return new HasUntersuchungIDWrapper(uuid, entityClass);
    }

    public static HasUntersuchungIDWrapper fromEinladungId(@NonNull ID<Einladung> einladungID) {
        return new HasUntersuchungIDWrapper(einladungID.getUUID(),  Einladung.class);
    }

    public static HasUntersuchungIDWrapper fromAnonymerEintragId(@NonNull ID<AnonymerEintrag> anonymerEintragID) {
        return new HasUntersuchungIDWrapper(anonymerEintragID.getUUID(), AnonymerEintrag.class);
    }

    public static HasUntersuchungIDWrapper fromEinladungUuid(@NonNull String uuid) {
        return fromUuidString(uuid, Einladung.class);
    }

    public static HasUntersuchungIDWrapper fromAnonymerEintragUuid(@NonNull String uuid ) {
        return fromUuidString(uuid, AnonymerEintrag.class);
    }

    private static HasUntersuchungIDWrapper fromUuidString(@NonNull String uuid, @NonNull Class<? extends AbstractEntity<?>> entityClass) {
        return new HasUntersuchungIDWrapper(UUID.fromString(uuid), entityClass);
    }

    @NonNull
    public String toExternalForm() {
        String shortname = SHORTNAME_TO_CLASS.entrySet().stream()
            .filter(entry -> entry.getValue().equals(entityClass))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No shortname registered for entity class: " + entityClass.getName()
            ));

        return uuid.toString() + "-" + shortname;
    }
}
