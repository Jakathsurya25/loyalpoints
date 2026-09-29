# LoyaltyPoints — Tier-Based Retail Loyalty Points Engine

Spring Boot **4.1.1** · **Java 26** · Maven · Spring Web · Spring Data JPA · Validation · MySQL · Swagger UI

> Spring Boot 4.1.x officially supports Java 17 up to and including Java 26. `java.version` is set to **26** in `pom.xml`.
> Lombok is intentionally not used (plain Java avoids annotation-processor problems on new JDKs).

## 1. Prerequisites
| Tool | Version |
|---|---|
| JDK | 26 (Oracle OpenJDK 26.0.1) |
| IntelliJ IDEA | 2026.1.4 |
| MySQL | **8.0.16 or newer** (older versions ignore CHECK constraints) |
| Maven | not required to install: use the included `mvnw.cmd` (or IntelliJ's bundled Maven) |

## 2. Database
The app creates the `loyalty_db` database automatically (`createDatabaseIfNotExist=true`), then runs
`schema.sql` (tables, foreign keys, UNIQUE and CHECK constraints) and `data.sql` (seeds the tiers).

Just make sure MySQL is running and set your credentials. Either edit `src/main/resources/application.properties`
or set environment variables (PowerShell):
```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
```
Default values if nothing is set: user `root`, password `root`.

Optional check in MySQL Workbench:
```sql
SELECT VERSION();          -- must be 8.0.16+
```

## 3. Open in IntelliJ IDEA 2026.1.4
1. **File → Open…** → select `pom.xml` (or the project folder) → **Open as Project**.
2. **File → Project Structure → Project**: SDK = **26**, Language level = **26**.
3. **Settings → Build, Execution, Deployment → Build Tools → Maven → Importing**: *JDK for importer* = **Use Project JDK**. (Runner → JRE = *Use Project JDK*.)
4. Wait for Maven to finish importing, then run `LoyaltyPointsApplication` (green ▶ next to `main`).

## 4. Run from the terminal (Windows)
Make sure `JAVA_HOME` points to JDK 26, then in the project folder:
```powershell
.\mvnw.cmd spring-boot:run
```
The first run downloads Maven 3.9.11 automatically. To build a jar:
```powershell
.\mvnw.cmd clean package
java -jar target\loyalty-points-1.0.0.jar
```

## 5. Try it
* Swagger UI: http://localhost:8080/swagger-ui.html
* IntelliJ: open `requests.http` and click ▶ beside each request (13 ready-made scenarios, run in order).

## 6. API
| Method | URL | Purpose | Success |
|---|---|---|---|
| GET | `/api/tiers` | list tiers | 200 |
| GET / POST / PUT / DELETE | `/api/tiers[/{id}]` | manage tiers | 200 / 201 / 200 / 204 |
| POST | `/api/customers` | register customer (lowest tier) | 201 |
| GET | `/api/customers`, `/api/customers/{id}` | list / current tier, balances, next tier | 200 |
| PUT | `/api/customers/{id}` | update profile | 200 |
| GET | `/api/customers/{id}/points-history?page=0&size=10` | points history, newest first | 200 |
| POST | `/api/purchases` | purchase: discount, redemption, points, tier upgrade | 201 |
| GET | `/api/purchases/customer/{customerId}` | purchase history | 200 |

Error codes: **400** validation / bad JSON · **404** not found · **409** duplicate or DB constraint · **422** business rule broken (e.g. redeeming more points than the balance) · **500** unexpected (no stack trace shown).

## 7. Business rules (all enforced in the SERVICE layer, backed by DB constraints)
1. **Points per purchase** = `floor(amount actually paid × loyalty.points-per-rupee)`. Configurable in `application.properties`.
2. **Tier discount** of the customer's *current* tier is applied automatically on every purchase.
3. **Redemption**: `pointsToRedeem × loyalty.rupees-per-point` is deducted after the tier discount. Rejected with **422** if points > available balance, or if the redemption value exceeds the payable amount. Validation happens *before* anything is saved.
4. **Auto-upgrade**: after points are awarded, if `lifetimePoints` reaches a higher tier's threshold the customer is upgraded immediately (can jump several tiers).
5. **Never downgrade**: the tier is driven by `lifetimePoints`, which only increases (redeeming reduces only `availablePoints`), and code only ever moves a customer to a higher tier.
6. **Atomic & safe**: `@Transactional` purchase flow; customer row locked (`SELECT … FOR UPDATE`) so concurrent purchases cannot double-spend points; DB CHECK constraints are the second line of defence.

Seed tiers: SILVER 0 pts / 2 % · GOLD 1000 pts / 5 % · PLATINUM 5000 pts / 10 %.

Worked example (Silver customer, 100 points, buys Rs.1000 and redeems 100 points, 0.50 Rs per point):
tier discount 20.00 → 980.00 → redemption 50.00 → **pay 930.00** → earns **930** points.

## 8. Project structure
```
src/main/java/com/loyalty
  LoyaltyPointsApplication.java
  config/       LoyaltyProperties (loyalty.* settings)
  controller/   Tier / Customer / Purchase REST controllers
  service/      TierService, CustomerService, PurchaseService (business rules, @Transactional)
  repository/   Spring Data JPA repositories
  entity/       Tier, Customer, Purchase, PointsLedger, LedgerType
  dto/          request/response records with validation
  exception/    custom exceptions + GlobalExceptionHandler (@RestControllerAdvice)
src/main/resources
  application.properties, schema.sql, data.sql
docs/LoyaltyPoints_Guide.md   diagrams (class, ER, layered, sequence), test scenarios, viva notes
```

## 9. Troubleshooting
| Problem | Fix |
|---|---|
| `release version 26 not supported` / `invalid source release` | IntelliJ or Maven is not using JDK 26. Check Project Structure SDK and Maven *JDK for importer*; check `JAVA_HOME`. |
| `Access denied for user 'root'` | Wrong MySQL password. Set `DB_PASSWORD` or edit `application.properties`. |
| `Communications link failure` | MySQL is not running or not on port 3306. |
| CHECK constraint does not reject bad data | MySQL older than 8.0.16. Upgrade MySQL. |
| Maven download fails in `mvnw.cmd` | Check internet/proxy, or use IntelliJ's bundled Maven (right-click `pom.xml` → Maven → Reload). |
| Port 8080 already in use | Set `server.port=8081` in `application.properties`. |
| Want the official wrapper files | Run `mvn wrapper:wrapper` (or IntelliJ Maven tool window) to regenerate them. |
