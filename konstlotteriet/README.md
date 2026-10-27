# Konstlotteriet - Art Lottery Management System

Ett Quarkus-baserat system för att hantera Konstföreningens årliga konstlotterier.

## Innehållsförteckning
- [Om Projektet](#om-projektet)
- [Affärsregler](#affärsregler)
- [Teknisk Stack](#teknisk-stack)
- [Kom Igång](#kom-igång)
- [API Dokumentation](#api-dokumentation)
- [Databasstruktur](#databasstruktur)
- [Utveckling](#utveckling)

## Om Projektet

Konstlotteriet är ett system som hanterar:
- **Medlemmar** - Personer som deltar i lotteriet
- **Konstverk** - Konstverk som ska lottas ut
- **Utlottningar** - Årliga lotteringstillfällen
- **Rankinglistor** - Medlemmars preferenser för konstverk

## Affärsregler

### Lottregler
1. Varje medlem får en ny lott varje år
2. En medlem kan få max tre (3) lotter
3. Om medlem fick en vinst året innan så får den en (1) lott innevarande år
4. Medlem kan bara vinna en vinst per år
5. Efter en vinst så utgår alla lotter för medlemmen under den utlottningen

### Medlemshantering
- Medlemmar markeras som inaktiva istället för att tas bort (soft delete)
- Endast aktiva medlemmar kan delta i utlottningar
- Medlemmar kan se sina egna uppgifter men inte andras

## Teknisk Stack

- **Framework**: Quarkus 3.28.3
- **Språk**: Java 25
- **Databas**: PostgreSQL 15
- **ORM**: Hibernate with Panache
- **Migration**: Flyway
- **API**: JAX-RS (REST)
- **Validering**: Hibernate Validator
- **API Dokumentation**: SmallRye OpenAPI (Swagger)

## Kom Igång

### Förutsättningar
- Java 25 eller senare
- Maven 3.9+
- Docker (för dev services - PostgreSQL startas automatiskt)

### Starta applikationen

```bash
# Kör i utvecklingsläge (med live reload)
./mvnw quarkus:dev
```

Quarkus startar automatiskt PostgreSQL i en Docker-container (dev services).

### Öppna applikationen
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui
- **Quarkus Dev UI**: http://localhost:8080/q/dev

### Alternativ: Manuell PostgreSQL-konfiguration

Om du vill använda en egen PostgreSQL-instans, uppdatera `application.properties`:

```properties
quarkus.datasource.devservices.enabled=false
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/konstlotteriet
quarkus.datasource.username=your_username
quarkus.datasource.password=your_password
```

## API Dokumentation

Fullständig API-dokumentation finns på **Swagger UI**: http://localhost:8080/swagger-ui

### Admin Endpoints

#### Autentisering (`/api/auth`)
- `POST /api/auth/login` - Logga in (öppet för alla)
- `POST /api/auth/register` - Skapa användarkonto (endast admin)
- `POST /api/auth/change-password` - Byt lösenord
- `GET /api/auth/me` - Hämta inloggad användare

**Exempel: Skapa användarkonto för medlem**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "email": "anna@example.com",
    "password": "securepassword123",
    "role": "MEMBER",
    "memberId": 1
  }'
```

**Exempel: Logga in**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "anna@example.com",
    "password": "securepassword123"
  }'
```

#### Medlemmar (`/api/admin/members`)
- `GET /api/admin/members` - Hämta alla medlemmar
- `GET /api/admin/members/active` - Hämta aktiva medlemmar
- `GET /api/admin/members/{id}` - Hämta specifik medlem
- `POST /api/admin/members` - Skapa ny medlem
- `PUT /api/admin/members/{id}` - Uppdatera medlem
- `DELETE /api/admin/members/{id}` - Ta bort medlem (soft delete)
- `POST /api/admin/members/{id}/reactivate` - Återaktivera medlem

**Exempel: Skapa medlem**
```bash
curl -X POST http://localhost:8080/api/admin/members \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Anna",
    "lastName": "Andersson",
    "email": "anna@example.com",
    "phoneNumber": "0701234567",
    "membershipStartYear": 2024,
    "active": true
  }'
```

**Skapa inloggningskonto för medlem:**
Efter att ha skapat en medlem, kan du ge dem inloggning genom att anropa `/api/auth/register` med medlemmens ID.

#### Konstverk (`/api/admin/artworks`)
- `GET /api/admin/artworks` - Hämta alla konstverk
- `GET /api/admin/artworks/{id}` - Hämta specifikt konstverk
- `GET /api/admin/artworks/year/{year}` - Hämta konstverk för ett år
- `GET /api/admin/artworks/year/{year}/available` - Hämta tillgängliga konstverk
- `POST /api/admin/artworks` - Skapa nytt konstverk
- `PUT /api/admin/artworks/{id}` - Uppdatera konstverk
- `DELETE /api/admin/artworks/{id}` - Ta bort konstverk

**Exempel: Skapa konstverk**
```bash
curl -X POST http://localhost:8080/api/admin/artworks \
  -H "Content-Type: application/json" \
  -d '{
    "priceCategory": "A",
    "title": "Sommarlandskap",
    "artist": "Erik Eriksson",
    "technique": "Olja på duk",
    "size": "50x70cm",
    "purchaseYear": 2024,
    "lotteryYear": 2024
  }'
```

#### Utlottningar (`/api/admin/lotteries`)
- `GET /api/admin/lotteries` - Hämta alla utlottningar
- `GET /api/admin/lotteries/{id}` - Hämta specifik utlottning
- `GET /api/admin/lotteries/year/{year}` - Hämta utlottning för år
- `GET /api/admin/lotteries/active` - Hämta aktiv utlottning
- `POST /api/admin/lotteries` - Skapa ny utlottning
- `PUT /api/admin/lotteries/{id}` - Uppdatera utlottning
- `DELETE /api/admin/lotteries/{id}` - Ta bort utlottning
- `POST /api/admin/lotteries/{id}/participants/add-all` - Lägg till alla aktiva medlemmar
- `POST /api/admin/lotteries/{lotteryId}/participants/{memberId}` - Lägg till specifik medlem
- `GET /api/admin/lotteries/{id}/participants` - Hämta deltagare
- `GET /api/admin/lotteries/{id}/winners` - Hämta vinnare
- `POST /api/admin/lotteries/{id}/activate` - Aktivera utlottning
- `POST /api/admin/lotteries/{id}/complete` - Markera utlottning som genomförd

### Member Endpoints

#### Profil
- `GET /api/member/profile/{memberId}` - Hämta egen profil

#### Rankinglistor
- `POST /api/member/rankings/lottery/{lotteryId}/member/{memberId}` - Skapa rankinglista
- `PUT /api/member/rankings/{rankingListId}` - Uppdatera rankinglista
- `POST /api/member/rankings/{rankingListId}/submit` - Skicka in rankinglista
- `GET /api/member/rankings/lottery/{lotteryId}/member/{memberId}` - Hämta rankinglista
- `DELETE /api/member/rankings/{rankingListId}` - Ta bort rankinglista

## Databasstruktur

### Tabeller

#### users
Användarkonton med autentisering (JWT).

**Fält:**
- `id` - Primary key
- `email` - E-postadress (unik, användarnamn)
- `password` - Bcrypt-hashat lösenord
- `role` - Roll (ADMIN eller MEMBER)
- `member_id` - FK till members (för MEMBER-roll)
- `active` - Om kontot är aktivt
- `created_at`, `updated_at` - Tidsstämplar

#### members
Medlemsinformation med soft delete-stöd.

**Fält:**
- `id` - Primary key
- `first_name` - Förnamn
- `last_name` - Efternamn
- `email` - E-postadress (unik)
- `phone_number` - Telefonnummer
- `membership_start_year` - År då medlemskapet började
- `last_win_year` - Senaste år medlemmen vann
- `active` - Om medlemmen är aktiv
- `created_at`, `updated_at` - Tidsstämplar

**Relation till users:**
- En medlem kan ha ett användarkonto (one-to-one via `users.member_id`)
- Ett användarkonto skapas separat efter att medlemmen har registrerats

#### artworks
Konstverk som ska lottas ut.

**Fält:**
- `id` - Primary key
- `year_id` - Sekvensnummer för året (auto-genereras)
- `price_category` - Priskategori
- `title` - Titel
- `artist` - Konstnär
- `technique` - Teknik
- `size` - Storlek
- `purchase_year` - Inköpsår
- `lottery_year` - År för utlottning
- `is_won` - Om konstverket är vunnet
- `created_at`, `updated_at` - Tidsstämplar

#### lotteries
Utlottningar med status.

**Fält:**
- `id` - Primary key
- `year` - År (unikt)
- `lottery_date` - Datum för utlottning
- `status` - Status (PLANNED, ACTIVE, COMPLETED)
- `created_at`, `updated_at` - Tidsstämplar

#### lottery_participants
Kopplar medlemmar till utlottningar med antal lotter baserat på affärsregler.

**Fält:**
- `id` - Primary key
- `lottery_id` - FK till lotteries
- `member_id` - FK till members
- `number_of_tickets` - Antal lotter (1-3, beräknas automatiskt)
- `has_won` - Om deltagaren har vunnit
- `won_artwork_id` - FK till vunnet konstverk
- `created_at` - Tidsstämpel

#### ranking_lists
Medlemmars rankinglistor för utlottningar.

**Fält:**
- `id` - Primary key
- `lottery_id` - FK till lotteries
- `member_id` - FK till members
- `submitted_at` - När listan skickades in
- `created_at`, `updated_at` - Tidsstämplar

#### ranking_items
Individuella rankade konstverk i en rankinglista.

**Fält:**
- `id` - Primary key
- `ranking_list_id` - FK till ranking_lists
- `artwork_id` - FK till artworks
- `rank` - Rangordning (1 = högst prioritet)

## Utveckling

### Projektstruktur

```
src/main/java/se/konstlotteriet/
├── domain/              # JPA Entities (7 klasser)
│   ├── Member.java
│   ├── Artwork.java
│   ├── Lottery.java
│   ├── LotteryParticipant.java
│   ├── RankingList.java
│   ├── RankingItem.java
│   └── LotteryStatus.java
│
├── repository/          # Panache Repositories (6 klasser)
│   ├── MemberRepository.java
│   ├── ArtworkRepository.java
│   ├── LotteryRepository.java
│   ├── LotteryParticipantRepository.java
│   ├── RankingListRepository.java
│   └── RankingItemRepository.java
│
├── service/             # Business Logic (4 klasser)
│   ├── MemberService.java
│   ├── ArtworkService.java
│   ├── LotteryService.java      # Implementerar affärsreglerna
│   └── RankingService.java
│
├── dto/                 # Data Transfer Objects (5 klasser)
│   ├── MemberDTO.java
│   ├── ArtworkDTO.java
│   ├── LotteryDTO.java
│   ├── LotteryParticipantDTO.java
│   └── RankingListDTO.java
│
└── rest/                # REST Endpoints (6 klasser)
    ├── admin/
    │   ├── MemberResource.java
    │   ├── ArtworkResource.java
    │   ├── LotteryResource.java
    │   └── RankingAdminResource.java
    └── member/
        ├── MemberProfileResource.java
        └── RankingResource.java
```

### Implementerade Affärsregler

Affärsreglerna implementeras i `LotteryService.java`:

**Beräkning av antal lotter:**
```java
private int calculateNumberOfTickets(Member member, int lotteryYear) {
    Integer lastWinYear = member.getLastWinYear();

    // Om medlem vann förra året → 1 lott
    if (lastWinYear != null && lastWinYear == (lotteryYear - 1)) {
        return 1;
    }

    // Annars → 3 lotter (max)
    return 3;
}
```

### Köra tester

```bash
./mvnw test
```

### Bygga applikationen

**JAR-fil:**
```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

**Uber JAR:**
```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

**Native executable (kräver GraalVM):**
```bash
./mvnw package -Dnative
./target/konstlotteriet-1.0.0-SNAPSHOT-runner
```

**Native i container:**
```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

## Flyway Migrations

Databasschema hanteras med Flyway. Migrationsfiler finns i:
```
src/main/resources/db/migration/
```

- `V1__create_initial_schema.sql` - Initialt schema med alla tabeller och index

För att lägga till nya ändringar, skapa nya filer med högre versionsnummer:
- `V2__add_new_feature.sql`
- `V3__another_change.sql`

Flyway kör automatiskt alla nya migrationer vid start (`quarkus.flyway.migrate-at-start=true`).

## Konfiguration

Viktiga konfigurationer i `src/main/resources/application.properties`:

### Databas
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/konstlotteriet
quarkus.datasource.username=konstlotteriet
quarkus.datasource.password=konstlotteriet
```

### Hibernate
```properties
quarkus.hibernate-orm.database.generation=none  # Flyway hanterar schema
quarkus.hibernate-orm.log.sql=true              # Logga SQL queries
```

### Flyway
```properties
quarkus.flyway.migrate-at-start=true
quarkus.flyway.baseline-on-migrate=true
```

### CORS
```properties
quarkus.http.cors=true
quarkus.http.cors.origins=*  # Ändra i produktion!
```

### Swagger UI
```properties
quarkus.swagger-ui.always-include=true
quarkus.swagger-ui.path=/swagger-ui
```

## Framtida Förbättringar

### Säkerhet
- [x] Implementera autentisering (JWT med bcrypt lösenordshashning)
- [x] Rollbaserad access control (ADMIN, MEMBER)
- [ ] API-nyckelhantering
- [ ] HTTPS-konfiguration

### Email-funktionalitet
- [ ] Skicka rankinglistor till medlemmar
- [ ] Notifieringar om utlottningsresultat
- [ ] Påminnelser inför utlottning
- [ ] Välkomstmail för nya medlemmar

### Rapporter
- [ ] PDF-generering av lottlistor
- [ ] Excel-export av medlemmar/konstverk
- [ ] Statistik över utlottningar
- [ ] Historik över vinnare

### Lotteringsalgoritm
- [ ] Implementera automatisk dragning
- [ ] Hantera rankinglistor vid dragning
- [ ] Fördelning baserat på preferenser
- [ ] Loggning av lottningsresultat

### Frontend
- [ ] React/Vue/Angular frontend
- [ ] Medlemsportal för egen hantering
- [ ] Admin-dashboard med statistik
- [ ] Mobil-anpassad design

### Övriga förbättringar
- [ ] Audit logging (vem gjorde vad när)
- [ ] Backup-funktionalitet
- [ ] Import/export av data
- [ ] Notifikationssystem
- [ ] Sökfunktionalitet
- [ ] Paginering för stora dataset

## Quarkus Resources

Om du vill lära dig mer om Quarkus:
- **Website**: https://quarkus.io/
- **Guides**: https://quarkus.io/guides/
- **Extensions**: https://quarkus.io/extensions/

### Använda Extensions

- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [REST Jackson](https://quarkus.io/guides/rest#json-serialisation)
- [Hibernate Validator](https://quarkus.io/guides/validation)
- [Flyway](https://quarkus.io/guides/flyway)
- [SmallRye OpenAPI](https://quarkus.io/guides/openapi-swaggerui)
- [JDBC Driver - PostgreSQL](https://quarkus.io/guides/datasource)
- [Mailer](https://quarkus.io/guides/mailer)

## Licens

Detta projekt är skapat för Konstföreningen.

## Support

För frågor eller problem, kontakta projektadministratören.
