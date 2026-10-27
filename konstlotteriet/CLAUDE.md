# CLAUDE.md - Projektutvecklingsguide

Detta dokument innehåller viktig information för AI-assistenter (som Claude) som arbetar med detta projekt.

## Projektöversikt

**Projektnamn**: Konstlotteriet
**Typ**: Art Lottery Management System
**Framework**: Quarkus 3.28.3
**Språk**: Java 25
**Databas**: PostgreSQL 15

## Affärsdomän

### Kärnkoncept
Detta är ett system för att hantera årliga konstlotterier för en konstförening. Systemet hanterar:

1. **Medlemmar** - Personer som är med i föreningen och kan delta i lotterier
2. **Konstverk** - Konst som köpts in och ska lottas ut
3. **Utlottningar** - Årliga events där konstverk fördelas till medlemmar
4. **Rankinglistor** - Medlemmars preferenser för vilka konstverk de vill vinna

### Kritiska Affärsregler

⚠️ **VIKTIGT**: Dessa regler måste alltid respekteras vid ändringar:

1. **Varje medlem får en ny lott varje år**
2. **Max 3 lotter per medlem**
3. **Om medlem vann förra året → 1 lott innevarande år**
4. **Endast en vinst per medlem och år**
5. **Efter vinst utgår alla lotter för medlemmen under den utlottningen**

### Implementering av Affärsregler

Reglerna implementeras i `LotteryService.java` via metoden `calculateNumberOfTickets()`:

```java
// src/main/java/se/konstlotteriet/service/LotteryService.java
private int calculateNumberOfTickets(Member member, int lotteryYear) {
    Integer lastWinYear = member.getLastWinYear();

    if (lastWinYear != null && lastWinYear == (lotteryYear - 1)) {
        return 1;  // Vann förra året
    }

    return 3;  // Standard: 3 lotter
}
```

## Projektstruktur

```
src/main/java/se/konstlotteriet/
├── domain/              # JPA Entities - Datamodeller
├── repository/          # Panache Repositories - Databasåtkomst
├── service/             # Business Logic - Affärsregler
├── dto/                 # Data Transfer Objects - API-kontrakt
└── rest/                # REST Endpoints - API
    ├── admin/           # Admin-endpoints (full åtkomst)
    └── member/          # Medlems-endpoints (begränsad åtkomst)
```

### Lagerskiktning

1. **REST Layer** (`rest/`) - HTTP endpoints, validering, felhantering
2. **Service Layer** (`service/`) - Affärslogik, transaktioner
3. **Repository Layer** (`repository/`) - Databasoperationer
4. **Domain Layer** (`domain/`) - Entities och affärsobjekt

## Viktiga Design Decisions

### 1. Soft Delete för Medlemmar
```java
// Medlemmar tas INTE bort ur databasen
// De markeras bara som inaktiva
member.setActive(false);
```

**Motivering**: Vi behöver historisk data för rapporter och statistik.

### 2. Auto-genererat yearId för Konstverk
```java
// Varje konstverk får ett löpnummer per år (1, 2, 3...)
Integer nextYearId = artworkRepository.getNextYearId(artwork.getLotteryYear());
artwork.setYearId(nextYearId);
```

**Motivering**: Enklare referens vid fysisk hantering av konstverk.

### 3. Flyway för Databasmigrationer
- Hibernate sköter INTE schema-generering (`quarkus.hibernate-orm.database.generation=none`)
- Flyway hanterar alla schema-ändringar via SQL-filer
- Migrations körs automatiskt vid start

**Motivering**: Explicit kontroll över databasschema, versionering, och reproducerbarhet.

### 4. Panache Repository Pattern
```java
// Inte Active Record (extends PanacheEntity)
// Utan Repository Pattern (implements PanacheRepository<T>)
public class MemberRepository implements PanacheRepository<Member> { }
```

**Motivering**: Tydlig separation mellan domänmodell och persistence-logik.

## Databasschema

### Entity Relationships

```
Member 1 ──── * LotteryParticipant * ──── 1 Lottery
                      │
                      │ 0..1
                      ▼
                   Artwork

Member 1 ──── * RankingList * ──── 1 Lottery
                      │
                      │
                      ▼
               RankingItem * ──── 1 Artwork
```

### Viktiga Constraints

- `members.email` - UNIQUE
- `lotteries.year` - UNIQUE
- `lottery_participants(lottery_id, member_id)` - UNIQUE
- `ranking_lists(lottery_id, member_id)` - UNIQUE
- `lottery_participants.number_of_tickets` - CHECK (1-3)

## Vanliga Utvecklingsscenarier

### Scenario 1: Lägga till nytt fält till Entity

1. **Uppdatera Entity** (`domain/`)
   ```java
   @Column(name = "new_field")
   private String newField;
   // + getters/setters
   ```

2. **Skapa Flyway migration** (`db/migration/`)
   ```sql
   -- V2__add_new_field.sql
   ALTER TABLE table_name ADD COLUMN new_field VARCHAR(255);
   ```

3. **Uppdatera DTO** (`dto/`)
   ```java
   public String newField;
   // Uppdatera fromEntity() och toEntity()
   ```

4. **Uppdatera Service** (`service/`) om nödvändigt

### Scenario 2: Lägga till ny Endpoint

1. **Skapa endpoint** i relevant Resource (`rest/admin/` eller `rest/member/`)
   ```java
   @GET
   @Path("/new-endpoint")
   public Response newEndpoint() { }
   ```

2. **Implementera logik** i Service-lagret
3. **Dokumentera** i README.md (API-sektion)
4. **Testa** via Swagger UI

### Scenario 3: Lägga till ny Affärsregel

1. **Implementera i Service-lagret** (`service/`)
2. **Dokumentera i CLAUDE.md** (detta dokument)
3. **Uppdatera README.md** om användaren påverkas
4. **Lägg till tester** för att verifiera regeln

## Kodkonventioner

### Namngivning

- **Entities**: Singular, PascalCase (Member, Artwork, Lottery)
- **Tables**: Plural, snake_case (members, artworks, lotteries)
- **Repositories**: EntityNameRepository
- **Services**: EntityNameService
- **Resources**: EntityNameResource
- **DTOs**: EntityNameDTO

### Transaktionshantering

```java
@ApplicationScoped
public class SomeService {

    @Transactional  // Endast på metoder som skriver till DB
    public void createOrUpdate() { }

    // Inga @Transactional på read-only metoder
    public List<Entity> getAll() { }
}
```

### Felhantering

```java
// Service-lager: Kasta IllegalArgumentException med svensk text
if (member == null) {
    throw new IllegalArgumentException("Medlem med ID " + id + " hittades inte");
}

// REST-lager: Konvertera till HTTP responses
try {
    service.doSomething();
} catch (IllegalArgumentException e) {
    return Response.status(Response.Status.BAD_REQUEST)
            .entity(new ErrorResponse(e.getMessage()))
            .build();
}
```

### Validering

```java
// På Entity-nivå
@NotBlank(message = "Förnamn är obligatoriskt")
@Column(name = "first_name", nullable = false)
private String firstName;

// På DTO-nivå vid behov
@Valid MemberDTO memberDTO

// I Service-lager för affärsregler
if (member.getEmail() == null || member.getEmail().isBlank()) {
    throw new IllegalArgumentException("E-post är obligatorisk");
}
```

## Konfiguration

### Miljöspecifika Inställningar

**Development** (default i `application.properties`):
```properties
quarkus.datasource.devservices.enabled=true  # Auto-start PostgreSQL
quarkus.hibernate-orm.log.sql=true           # Logga SQL
quarkus.http.cors.origins=*                  # Öppen CORS
```

**Production** (bör overridas):
```properties
quarkus.datasource.devservices.enabled=false
quarkus.hibernate-orm.log.sql=false
quarkus.http.cors.origins=https://your-frontend.com
```

### Profiler

Quarkus stöder profiler via `%profile.` prefix:

```properties
%dev.quarkus.log.level=DEBUG
%prod.quarkus.log.level=WARN
```

Kör med profil:
```bash
./mvnw quarkus:dev -Dquarkus.profile=prod
```

## Testing

### Teststruktur (TODO)

```
src/test/java/se/konstlotteriet/
├── service/             # Service-lager unit tests
├── rest/                # Integration tests för endpoints
└── repository/          # Repository tests (vid behov)
```

### Test med TestContainers (TODO)

```java
@QuarkusTest
@QuarkusTestResource(PostgresResource.class)
public class MemberResourceTest {
    // Integration tests mot riktig PostgreSQL
}
```

## Säkerhet ✅

### Nuvarande Implementation

Systemet har JWT-baserad autentisering med bcrypt-hashning av lösenord:

1. **Authentication**: JWT-baserad autentisering ✅
   - Lösenord hashas med bcrypt
   - JWT tokens genereras vid lyckad inloggning
   - Tokens innehåller användarinfo (userId, email, role)

2. **Authorization**: Rollbaserad access control ✅
   - `ADMIN` - Full åtkomst till `/api/admin/*` och `/api/auth/register`
   - `MEMBER` - Begränsad åtkomst till `/api/member/*`
   - Public endpoints: `/api/auth/login`

3. **User Management**:
   - Admin kan skapa användarkonton via `/api/auth/register`
   - Användare kan byta lösenord via `/api/auth/change-password`
   - Separata `users` och `members` tabeller (one-to-one relation)

### Användarkonto-skapande

**Via API (Backend):**
```bash
POST /api/auth/register
{
  "email": "member@example.com",
  "password": "password123",
  "role": "MEMBER",
  "memberId": 1
}
```

**Via UI (Frontend):**
- Admin kan skapa medlemmar med inloggningskonton direkt
- Admin kan skapa inloggningskonton för befintliga medlemmar
- Se `konstlotteriet-frontend/src/pages/admin/Members.tsx`

### Framtida Förbättringar

⚠️ **TODO**: Medlems-endpoints använder fortfarande memberId som parameter:

```java
// NUVARANDE - Osäkert (alla kan accessa alla medlemmars data)
@GET
@Path("/{memberId}")
public Response getProfile(@PathParam("memberId") Long memberId) { }

// FRAMTIDA - Säkert (medlemmar kan bara accessa sin egen data)
@GET
public Response getProfile(@Context SecurityContext sec) {
    Long memberId = getUserIdFromToken(sec);
    // Verify member owns this data
}
```

**Nästa steg:**
1. Ta bort memberId från member-endpoints
2. Hämta userId från JWT token automatiskt
3. Validera att medlemmar bara kan accessa sin egen data

## Common Pitfalls & Tips

### ⚠️ Problem: LazyInitializationException

**Orsak**: Försök att accessa lazy-loaded relations utanför transaction.

**Lösning**:
```java
// Antingen JOIN FETCH i query
@Query("SELECT m FROM Member m JOIN FETCH m.lotteryParticipants WHERE m.id = ?1")

// Eller gör access inom @Transactional-metod
@Transactional
public MemberDTO getMemberWithParticipants(Long id) {
    Member member = memberRepository.findById(id);
    member.getLotteryParticipants().size(); // Force load
    return MemberDTO.fromEntity(member);
}
```

### ⚠️ Problem: Flyway migration failure

**Orsak**: Migration-fil med fel eller databasinkonsekvens.

**Lösning**:
```bash
# Development: Droppa databas och börja om
docker stop <postgres-container>
docker rm <postgres-container>
./mvnw quarkus:dev  # Startar ny databas

# Production: Fixa migration och versioning
```

### ⚠️ Problem: CORS errors i frontend

**Lösning**: Uppdatera `application.properties`
```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
```

### 💡 Tip: Live Coding

Quarkus dev mode stöder hot reload:
```bash
./mvnw quarkus:dev
# Gör ändringar i koden
# Spara filen
# Applikationen reloadar automatiskt
```

### 💡 Tip: DevServices

Quarkus startar automatiskt PostgreSQL i Docker:
```properties
quarkus.datasource.devservices.enabled=true
```

Ingen manuell databas-setup behövs för utveckling!

### 💡 Tip: Dev UI

Öppna http://localhost:8080/q/dev för:
- Swagger UI link
- Databas-info
- Config-översikt
- Arc (CDI) info
- Och mer!

## Vanliga Kommandon

```bash
# Utveckling
./mvnw quarkus:dev              # Starta med hot reload
./mvnw quarkus:dev -Ddebug      # Med debugger på port 5005

# Test
./mvnw test                     # Kör alla tester
./mvnw test -Dtest=MemberServiceTest  # Kör specifikt test

# Build
./mvnw clean package            # Bygg JAR
./mvnw clean package -DskipTests  # Bygg utan tester

# Databas
./mvnw flyway:info              # Visa migrations-status
./mvnw flyway:migrate           # Kör migrations manuellt
./mvnw flyway:clean             # ⚠️ VARNING: Droppar hela schemat!

# Native
./mvnw package -Dnative         # Bygg native executable
```

## Dependencies

### Core Dependencies
- `quarkus-hibernate-orm-panache` - ORM med repository pattern
- `quarkus-jdbc-postgresql` - PostgreSQL driver
- `quarkus-flyway` - Databasmigrationer
- `quarkus-rest-jackson` - REST API med JSON
- `quarkus-hibernate-validator` - Bean validation

### Utility Dependencies
- `quarkus-smallrye-openapi` - Swagger/OpenAPI dokumentation
- `quarkus-mailer` - Email (ej konfigurerad än)
- `quarkus-security` - Säkerhet (ej implementerad än)

### Test Dependencies (TODO)
- `quarkus-junit5` - JUnit 5 för tester
- `rest-assured` - REST API testing

## Utbyggnad av Systemet

### Prio 1: Säkerhet ✅ → Förbättringar
1. ~~Implementera JWT-autentisering~~ ✅ KLART
2. ~~Lägg till roller (ADMIN, MEMBER)~~ ✅ KLART
3. ~~Säkra endpoints baserat på roller~~ ✅ KLART
4. Ta bort memberId från URLs (TODO)

### Prio 2: Email-funktionalitet
1. Konfigurera SMTP i `application.properties`
2. Skapa email-templates
3. Implementera EmailService
4. Lägg till endpoints för att skicka rankinglistor

### Prio 3: Lotteringsalgoritm
1. Implementera DrawService
2. Algoritm för att välja vinnare baserat på:
   - Antal lotter per medlem
   - Rankinglistor (prioritet)
   - Slumpmässighet
3. Uppdatera winners och wonArtwork
4. Uppdatera members.lastWinYear

### Prio 4: Rapporter
1. PDF-generering (iText, Apache PDFBox)
2. Excel-export (Apache POI)
3. Statistik-endpoints
4. Admin-dashboard data

### Prio 5: Frontend
1. React/Vue/Angular SPA
2. Admin-dashboard
3. Medlemsportal
4. Mobil-anpassning

## Länkar till Dokumentation

- [Quarkus Guide](https://quarkus.io/guides/)
- [Hibernate ORM](https://hibernate.org/orm/documentation/)
- [Panache Guide](https://quarkus.io/guides/hibernate-orm-panache)
- [Flyway](https://flywaydb.org/documentation/)
- [JAX-RS Spec](https://jakarta.ee/specifications/restful-ws/)

## Ändringshistorik

| Datum | Ändring | Av |
|-------|---------|-----|
| 2025-10-10 | Initial projektstruktur skapad | Claude |
| | 28 filer: entities, repos, services, DTOs, REST | |
| | Flyway migration V1 | |
| | Konfiguration och dokumentation | |
| 2025-10-16 | JWT-autentisering och användarhantering | Claude |
| | User entity och AuthService implementerat | |
| | Frontend stöd för att skapa användarkonton | |
| | Rollbaserad access control (ADMIN/MEMBER) | |

---

**Senast uppdaterad**: 2025-10-16
**Version**: 1.0.0-SNAPSHOT
**Underhålls av**: Konstföreningen
