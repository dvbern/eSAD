# Testing in QUARKUS using RestAssured

# Erstellung von RestAssuredClient
api/pom.xml:727
-> Erstellt spec Files KindJsonSpec.java

_Beispieltest_
[KindResourceITest.java](/Users/dvbern/IdeaProjects/esc/esc/api/src/test/java/ch/dvbern/oss/hemed/esc/api/features/kinder/KindResourceITest.java)
- findAllKinder zeigen
- Wie wird eingeloggt: @TestAsAdminBS  -> @TestSecurity, @OidcSecurity sind helper von Quarkus

- create zeigen -> Mapper mit MapStruct damit wir den gleichen Builder verwenden können


## Wo hilft uns das Framework
- Login
- Starten der Umsysteme (Quarkus, DB)
- Transaktionsmanagement, in ausnahmefällen testen wir direkt Services mit @TestTransaction damit wir dann ein Rollback haben



## Erstellen von Testdaten

### Insert per Skript / DB Unit
Was sich nicht so bewährt hat war DB Unit (ensprecht quasi einem DB Dump)
- Skripts zum inserten müssen laufend nachgepflegt werden
- Es ist unklar welches die relevanten Daten sind in der Datenbank
- Drop und Insert ist langsam

### Insert mit Builder

Wir erstellen Daten mit Buildern und alle Tests dürfen nur ihre eigenen Daten benutzten
- Builder programmieren für Entity e.G KindBuilder
- Der Builder hat Methoden um aus einem Entity unser Transfer Object zu machen (KindJson)
- Der Builder hat Methoden um per MapStrunct das KindJson in ein KindJsonSpec zu transformieren

Wo nötig machen wir einen Reset 
- DatabaseService#resetAndReinitDatabasePreservingHibernateSequenceValue
- verwendet Liquibase zum resetten
  - Achtung HibernateRevInfo Sequence muss danach gesetzt werden sonst geht Envers kaputt
  - Der Grund ist, dass Hibernate6 die 50 nächsten Revisionsnummern reserviert und dass dann Unique Constraints verletzt werden

Ein kleiner Grundstock an Daten wir mit Liquibase im Testscope inserted (skripte die nur für Tests laufen)


## Asserts so schreiben dass man nicht von Daten abhängig ist
- Nach dem erstellen von 2 Kidnern nicht prüfen dass man nun 2 Kinder hat sondern prüfen dass diese beiden Kinder existieren
- Nach löschen prüfen dass die gelöschten Entities nicht mehr existieren
- Nie die Anzahl von etwas prüfen

