package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class DuplikatRepo extends BaseRepo<Duplikat> {

	private static final BooleanExpression[] EMPTY_BOOLEAN_EXPRESSION = new BooleanExpression[0];


    @NonNull
    public List<Duplikat> findAll() {
        return db.selectFrom(QDuplikat.duplikat).fetch();
    }

    @NonNull
    public List<Duplikat> findUnmergedDuplikate() {
        return db.selectFrom(QDuplikat.duplikat)
            .where(QDuplikat.duplikat.duplikatStatus.ne(DuplikatStatus.GEMERGT)).fetch();
    }

	@NonNull
	public List<Kind> searchDuplikate(@NonNull DuplikatSearchTerm searchTerm) {
        BooleanExpression namePredicate = createNamePredicate(searchTerm.getNameSearchTerms());
        BooleanExpression vornamePredicate = createVornamePredicate(searchTerm.getVornameSearchTerms());
		BooleanExpression geburtsdatumPredicate = createGeburtsdatumPredicate(searchTerm.getGeburtsdatumSearchTerm());
        BooleanExpression geschlechtPredicate = createGeschlechtPredicate(searchTerm.getGeschlechtSearchTerm());
		var predicate = Expressions.allOf(namePredicate, vornamePredicate, geburtsdatumPredicate, geschlechtPredicate);

        if (searchTerm.getCurrentKindId() != null) {
            predicate = predicate.and(QKind.kind.id.ne(searchTerm.getCurrentKindId().getUUID()));
        }
		return db.selectFrom(QKind.kind)
                .distinct()
				.where(predicate)
				.fetch();
	}

    @NonNull
    private static BooleanExpression createNamePredicate(@NonNull List<String> stringSearchTerms) {
        List<BooleanExpression> stringPredicates = new ArrayList<>();
        for (String nameSearchTerm : stringSearchTerms) {
            BooleanExpression stringMatch = QKind.kind.normalizedName.contains(nameSearchTerm);
            stringPredicates.add(stringMatch);
        }
        return Expressions.anyOf(stringPredicates.toArray(EMPTY_BOOLEAN_EXPRESSION));
    }

    @NonNull
    private static BooleanExpression createVornamePredicate(@NonNull List<String> stringSearchTerms) {
        List<BooleanExpression> stringPredicates = new ArrayList<>();
        for (String nameSearchTerm : stringSearchTerms) {
            BooleanExpression stringMatch = QKind.kind.normalizedVorname.contains(nameSearchTerm);
            stringPredicates.add(stringMatch);
        }
        return Expressions.anyOf(stringPredicates.toArray(EMPTY_BOOLEAN_EXPRESSION));
    }

	@NonNull
	private static BooleanExpression createGeburtsdatumPredicate(@NonNull LocalDate geburtsdatum) {
        return QKind.kind.geburtsdatum.eq(geburtsdatum);
	}

    @NonNull
    private static BooleanExpression createGeschlechtPredicate(@NonNull Geschlecht geschlecht) {
        // if we are searching for unbekannt or divers we can 'skip' the expression by returning true
        if (geschlecht == Geschlecht.UNBEKANNT || geschlecht == Geschlecht.DIVERS) {
            return Expressions.TRUE;
        }
        // UNBEKANNT and DIVERS are treated as a match for all other Geschlecht values
        Set<Geschlecht> geschlechtToMatch = EnumSet.of(Geschlecht.UNBEKANNT, Geschlecht.DIVERS);
        geschlechtToMatch.add(geschlecht);
        return QKind.kind.geschlecht.in(geschlechtToMatch);
    }

    @NonNull
    public Optional<Duplikat> findByKindIds(@NonNull ID<Kind> idA, @NonNull ID<Kind> idB) {
        return db
            .selectFrom(QDuplikat.duplikat)
            .where(
                // idA and idB can be in any order, so we check both combinations
                QDuplikat.duplikat.idKind1.eq(idA.getUUID()).and(QDuplikat.duplikat.idKind2.eq(idB.getUUID()))
                .or(QDuplikat.duplikat.idKind1.eq(idB.getUUID()).and(QDuplikat.duplikat.idKind2.eq(idA.getUUID())))
            )
            .fetchOne();
    }

    @NonNull
    public List<Duplikat> findByKindId(@NonNull ID<Kind> id) {
        return db
            .selectFrom(QDuplikat.duplikat)
            .where(QDuplikat.duplikat.idKind1.eq(id.getUUID())
                .or(QDuplikat.duplikat.idKind2.eq(id.getUUID())))
            .fetch();
    }
}
