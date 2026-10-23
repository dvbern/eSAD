package ch.dvbern.dvbstarter.infrastructure.db;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.interceptor.Interceptor;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.ManagedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Initializes all generated QueryDSL query classes ({@code Q*}) serially on a single thread
 * before the first request is served.
 * <p>
 * The static {@code Q*} instances reference each other through their constructors
 * (e.g. {@code QUntersuchung -> QUntersuchungImpfungen -> QUntersuchung}).
 * When two request threads trigger the class initialization of such a cycle from different ends
 * at the same time, the JVM ends up in a class initialization deadlock (JLS §12.4.2) that never
 * resolves: every thread that touches one of the involved classes afterwards hangs forever.
 * <p>
 * Initializing every query class once on startup makes the cycle harmless, because an already
 * initialized class no longer takes the initialization lock.
 * This also runs after every dev mode hot reload, where the classes are loaded again.
 *
 * @see <a href="https://github.com/querydsl/querydsl/issues/2334">querydsl/querydsl#2334</a>
 */
@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class QueryDslWarmup {

    static final String QUERY_CLASS_PREFIX = "Q";

    private final EntityManagerFactory entityManagerFactory;

    private final Set<String> initializedQueryClasses = new TreeSet<>();

    @SuppressWarnings("java:S1172" /* false positive */)
    void onStart(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE) StartupEvent ignored) {
        warmup();
    }

    /**
     * Initializes the query class of every JPA managed type (entities, mapped superclasses, embeddables).
     * Managed types without a generated query class are skipped.
     */
    public void warmup() {
        long start = System.nanoTime();

        Set<ManagedType<?>> managedTypes = entityManagerFactory.getMetamodel()
            .getManagedTypes();

        for (ManagedType<?> managedType : managedTypes) {
            Class<?> javaType = managedType.getJavaType();
            if (javaType == null) {
                continue;
            }
            initializeQueryClass(javaType);
        }

        long durationMillis = (System.nanoTime() - start) / 1_000_000;
        LOG.info(
            "Initialized {} QueryDSL query classes for {} managed types in {} ms",
            initializedQueryClasses.size(),
            managedTypes.size(),
            durationMillis
        );
    }

    /**
     * @return the fully qualified names of all query classes initialized by {@link #warmup()}
     */
    @NonNull
    public Set<String> getInitializedQueryClasses() {
        return Collections.unmodifiableSet(initializedQueryClasses);
    }

    private void initializeQueryClass(@NonNull Class<?> javaType) {
        String queryClassName = queryClassName(javaType);
        Class<?> queryClass = loadInitialized(queryClassName, javaType.getClassLoader());
        if (queryClass == null) {
            LOG.debug("No QueryDSL query class {} found for managed type {}", queryClassName, javaType.getName());
            return;
        }
        initializedQueryClasses.add(queryClass.getName());
    }

    @Nullable
    private static Class<?> loadInitialized(@NonNull String className, @Nullable ClassLoader classLoader) {
        try {
            // initialize = true runs the static initializer, which creates the static Q instance
            // and, recursively on this very thread, everything it references.
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    /**
     * Derives the name of the generated QueryDSL query class the same way querydsl-apt does:
     * same package, simple name prefixed with {@value #QUERY_CLASS_PREFIX},
     * nested classes joined with {@code _}.
     */
    @NonNull
    static String queryClassName(@NonNull Class<?> javaType) {
        String packageName = javaType.getPackageName();
        String nestedName = javaType.getName()
            .substring(packageName.isEmpty() ? 0 : packageName.length() + 1)
            .replace('$', '_');

        return packageName.isEmpty()
            ? QUERY_CLASS_PREFIX + nestedName
            : packageName + '.' + QUERY_CLASS_PREFIX + nestedName;
    }
}
