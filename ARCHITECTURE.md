# Architecture technique — Biblion

Biblion est une application Java de catalogue de livres au niveau **édition**
(ligne par ISBN, et non par œuvre ou exemplaire physique). Le projet sert de
terrain d'essai pour une stack légère et une architecture hexagonale stricte.

## Stack technique

| Catégorie | Choix | Version |
|---|---|---|
| Langage | Java | 17 |
| Build | Gradle (Groovy DSL) | 8.x via wrapper |
| Serveur HTTP | Javalin | 6.3.0 |
| Templating | JTE | 3.1.16 |
| Base de données | PostgreSQL | 12+ |
| Accès BD | jOOQ | 3.19.15 |
| Migrations | Flyway | 10.20.1 |
| Pool de connexions | HikariCP | 6.2.1 |
| Logs | SLF4J Simple | 2.0.16 |
| Frontend (à venir) | htmx + JTE server-rendered | — |

Choix structurants :
- **Pas de Spring** ni de JPA/Hibernate (volonté de rester léger).
- **JTE** plutôt que Thymeleaf (envie d'essayer).
- **Flyway + SQL brut** plutôt que Liquibase (Postgres-only, accès direct
  à JSONB / tableaux / CTE / fonctions fenêtrées si besoin).
- **jOOQ** prévu en mode codegen (pas encore activé : les adapters utilisent
  pour l'instant `DSL.field(...)` string-based).

## Architecture applicative

**Hexagonale** + **découpage par feature** + vocabulaire **API / SPI**.

### Conventions

- Une **feature** = un sous-package racine (`book/`, `user/`, `loan/`...).
- Chaque feature suit le même découpage interne :

```
fr.ikisource.biblion.<feature>/
├── domain/                  # entités et value objects (records purs)
│   ├── api/                 # ports primaires : ce que le domaine EXPOSE
│   │                        # (interfaces de use cases consommées par
│   │                        # les controllers, CLI, listeners…)
│   └── spi/                 # ports secondaires : ce que le domaine REQUIERT
│                            # (interfaces implémentées par l'infra :
│                            # repositories, clients externes…)
├── application/             # implémentations des ports API (use cases)
└── infrastructure/          # adapters :
                             #  - consomment l'API (REST controllers…)
                             #  - implémentent la SPI (jOOQ repositories…)
```

Mémo direction de dépendance :
- **API** = ce que je **donne** à mes clients (controllers).
- **SPI** = ce que je **demande** à mes fournisseurs (repos, services externes).

### Couches et règles

| Couche | Rôle | Peut dépendre de |
|---|---|---|
| `domain` | Records, ports API/SPI, règles métier pures | rien (pas même Javalin/jOOQ) |
| `application` | Use cases | `domain` |
| `infrastructure` | REST, persistance, intégrations | `domain` (+ libs techniques) |

L'`infrastructure` ne doit **jamais** être référencée depuis `domain` ou
`application`. Un new use case ne fait que dépendre des ports SPI, jamais
de `BookJooqRepository` directement.

### État actuel — feature `book`

```
book/
├── domain/
│   ├── Book.java                       # record (BookId, title, author)
│   ├── BookId.java                     # record (long value)
│   ├── api/
│   │   ├── GetBookById.java            # use case : récupérer par id
│   │   └── ListBooks.java              # use case : lister
│   └── spi/
│       └── BookRepository.java         # findById, findAll
├── application/
│   ├── GetBookByIdUseCase.java
│   └── ListBooksUseCase.java
└── infrastructure/
    ├── BookJooqRepository.java         # adapter SPI (jOOQ)
    └── BookController.java              # adapter API (REST)
```

### Endpoints exposés

| Méthode | Path | Description |
|---|---|---|
| `GET` | `/` | Page d'accueil JTE (liste de titres — câblage temporaire jOOQ inline) |
| `GET` | `/ping` | Health check |
| `GET` | `/books` | Liste JSON triée par titre |
| `GET` | `/books/{id}` | Un livre (404 si inconnu, 400 si id invalide) |

## Templates JTE — mode de compilation

JTE peut fonctionner selon deux modes, qui dictent à quel moment les
templates sont compilés en classes Java.

| Mode | Compilation | Usage |
|---|---|---|
| **DirectoryCodeResolver** *(actuel)* | runtime | dev — feedback instantané sans redémarrage |
| **Précompilé** *(non activé)* | au build (Gradle) | prod — performance, JAR auto-suffisant |

### Mode actuel : runtime

Configuré dans `Application.java` :

```java
TemplateEngine engine = TemplateEngine.create(
        new DirectoryCodeResolver(Path.of("src/main/jte")),
        ContentType.Html
);
```

À chaque modification d'un `.jte`, JTE :

1. génère une classe Java équivalente (`JteindexGenerated.java`, etc.) ;
2. la compile à la volée ;
3. cache le résultat dans le dossier `jte-classes/` à la racine du projet.

C'est ce qui permet de modifier un template **sans redémarrer le serveur** —
la prochaine requête utilise la nouvelle version. Le dossier `jte-classes/`
est entièrement régénéré et n'est donc **pas versionné** (ignoré par
`.gitignore`).

### Bascule vers précompilé

Quand on voudra packager pour déployer (JAR via `./gradlew shadowJar`),
on activera le plugin Gradle `gg.jte.gradle` qui compile les templates au
build. Les `.jte` ne seront plus lus en runtime, et `jte-classes/` ne
sera plus créé. Ce chantier reste à faire le jour où la cible n'est plus
seulement la machine de dev.

## Mise en place de l'environnement

### Prérequis

- **JDK 17** (utilisé par la `toolchain` Gradle)
- **PostgreSQL 12+** (cluster local ou container Docker — voir ci-dessous)
- **Git**

### Base de données

L'application attend une base `biblion` accessible avec un utilisateur
`biblion` / mot de passe `biblion`. Trois variables d'environnement permettent
de surcharger ces défauts :

| Variable | Défaut |
|---|---|
| `BIBLION_DB_URL` | `jdbc:postgresql://localhost:5432/biblion` |
| `BIBLION_DB_USER` | `biblion` |
| `BIBLION_DB_PASSWORD` | `biblion` |

#### Création du user et de la base

Avec un cluster Postgres système (Debian/Ubuntu, port 5432) :

```bash
sudo -u postgres psql <<EOF
CREATE USER biblion WITH PASSWORD 'biblion';
CREATE DATABASE biblion OWNER biblion;
EOF
```

Vérification :

```bash
PGPASSWORD=biblion psql -h localhost -U biblion -d biblion -c '\dt'
# → "Aucune relation trouvée." (Flyway créera la table au premier run)
```

#### Cas du conflit avec un container Docker Postgres

Si le port 5432 est déjà occupé par un container Docker Postgres d'un autre
projet, deux options :

**Option A** — arrêter le container et utiliser le cluster système :

```bash
docker ps --format '{{.Names}}' | grep -i postgres   # repérer le container
docker stop <nom>
sudo pg_ctlcluster 12 main start                     # démarrer le cluster
```

**Option B** — faire cohabiter les deux en passant le cluster système sur un
port différent (ex. 5433) via `/etc/postgresql/<version>/main/postgresql.conf`,
puis exporter `BIBLION_DB_URL=jdbc:postgresql://localhost:5433/biblion`.

#### Vérifier l'état de Postgres

Sur Debian/Ubuntu, `systemctl status postgresql` peut afficher `inactive` même
quand un cluster tourne (c'est un service wrapper). Utiliser :

```bash
pg_lsclusters
# Ver Cluster Port Status Owner   Data directory               …
# 12  main    5432 online postgres /var/lib/postgresql/12/main  …
```

### Configuration IDE (IntelliJ)

Si Gradle se plaint qu'il ne trouve pas de JDK 17 (`Cannot find a Java
installation … matching: {languageVersion=17}`) :

- `Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JVM`
  → choisir un JDK 17.
- Vérifier aussi `File → Project Structure → Project SDK`.

Alternative en ligne de commande : ajouter à la racine un `gradle.properties`

```properties
org.gradle.java.installations.paths=/chemin/vers/jdk-17
```

### Lancement

```bash
./gradlew run
```

Au démarrage :
1. Hikari ouvre un pool de connexions.
2. Flyway exécute les migrations dans `src/main/resources/db/migration/`
   (V1 crée la table `book` et insère 3 livres de seed).
3. Javalin écoute sur `0.0.0.0:8080`.

Tester :

```bash
curl http://localhost:8080/books
curl http://localhost:8080/books/1
```

Depuis un autre appareil du même réseau Wi-Fi : remplacer `localhost` par
l'IP locale de la machine (`hostname -I` ou `ip -4 addr`).

## Schéma de base

Migration `V1__create_book.sql` :

```sql
CREATE TABLE book (
    id          BIGSERIAL PRIMARY KEY,
    title       TEXT NOT NULL,
    author      TEXT,
    year        INTEGER,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

Note : les colonnes `year` et `created_at` ne sont pas encore exposées dans
le record `Book` du domaine. À ajouter quand on enrichira la modélisation
(notamment `isbn` qui devrait devenir l'identifiant fonctionnel — voir
ci-dessous).

## Modélisation à venir

L'identifiant **technique** est `BookId` (wrapper de `long`, géré par
Postgres en `BIGSERIAL`). L'identifiant **fonctionnel** d'une édition est
l'**ISBN** ; il sera ajouté comme value object `Isbn` (avec validation
checksum ISBN-10 / ISBN-13) et contrainte `UNIQUE NOT NULL` côté base.

Le domaine raisonnera sur les deux : `BookId` pour la performance des FK
et joins, `Isbn` comme clé naturelle visible côté métier.

## Étapes suivies pendant la mise en place

Récapitulatif chronologique pour reproduire le projet à partir de zéro.

1. **Init du repo** — `git init`, `.gitignore` adapté à Gradle / IDE, premier
   commit minimal.
2. **Wrapper Gradle et build** — `gradle wrapper`, rédaction du `build.gradle`
   (plugin `application` + `shadow`, dépendances Javalin / JTE / Flyway /
   jOOQ / Hikari / SLF4J), `settings.gradle` avec `rootProject.name`.
3. **Group et package racine** — `group = 'fr.ikisource'`, package racine
   `fr.ikisource.biblion`, `mainClass = 'fr.ikisource.biblion.Application'`.
4. **Bootstrap Javalin** — `Application.java` : DataSource Hikari, exécution
   Flyway, `DSLContext` jOOQ, `TemplateEngine` JTE, démarrage sur le port
   choisi (8080 par défaut).
5. **Première migration** — `V1__create_book.sql` (table + données de seed)
   et template `index.jte` listant les titres.
6. **Postgres** — création du cluster (ou résolution du conflit avec un
   container Docker), création du user et de la base `biblion`
   (cf. section *Base de données*).
7. **JDK 17** — vérifier que l'IDE et `./gradlew` utilisent bien la même
   JVM 17 (cf. *Configuration IDE*).
8. **Architecture hexagonale, feature `book`** — création du squelette
   `domain/{api,spi}`, `application/`, `infrastructure/` :
    - `domain/Book` (record), `domain/BookId` (value object).
    - SPI : `domain/spi/BookRepository` (port).
    - Adapter SPI : `infrastructure/BookJooqRepository` (jOOQ).
    - API : `domain/api/GetBookById` et `domain/api/ListBooks`.
    - Use cases : `application/GetBookByIdUseCase`,
      `application/ListBooksUseCase`.
    - Adapter API : `infrastructure/BookController`
      (`GET /books`, `GET /books/{id}`, DTO interne `BookResponse`).
9. **Câblage** — instanciation manuelle des adapters et use cases dans
   `Application#main` (pas de DI framework).
10. **Vérification end-to-end** — `./gradlew run` puis `curl /books/1`.

## Conventions de commit

Découpage suivi pour ce projet :

- `chore:` — outillage, build, configuration.
- `feat:` — nouvelle fonctionnalité métier.
- `feat(<feature>):` — changement scopé à une feature (`feat(book): …`).
- `fix:` — correction de bug.

Un commit par responsabilité cohérente : éviter de mélanger un changement
d'infrastructure (build, port, deps) avec une feature métier dans le même
commit.
