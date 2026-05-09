# htmx — notes pour Biblion

## Qu'est-ce que c'est

**htmx** est une lib JavaScript de ~15 ko qui ajoute des **attributs HTML**
permettant à n'importe quel élément :

1. d'émettre une requête HTTP (GET/POST/PUT/PATCH/DELETE),
2. de récupérer du **HTML** en réponse (pas du JSON),
3. de **swapper** ce HTML quelque part dans la page.

Pas de build JS, pas de framework SPA, pas de routeur côté client.
Le serveur reste maître de la logique et du rendu.

```html
<button hx-get="/books/42" hx-target="#detail" hx-swap="innerHTML">
  Voir le livre 42
</button>
<div id="detail"></div>
```

Au clic : GET `/books/42` → le serveur renvoie un fragment HTML →
htmx l'injecte dans `#detail`.

## Philosophie

> *« Server-side rendering, fragments HTML, hypermédia comme moteur d'état. »*

- Pas de duplication de logique entre client et serveur.
- Les use cases du domaine (`ListBooks`, `GetBookById`…) servent à la fois
  l'API JSON et les vues HTML — seul le controller change.
- Tu écris du HTML sémantique. Bonus : ça marche bien avec un CSS classless
  type **Pico CSS**.

Quand l'utiliser : applis CRUD, dashboards, back-offices, sites éditoriaux
interactifs. Quand l'éviter : applis fortement temps réel (collaboratif live,
canvas / jeux), ou quand l'état client est complexe et déconnecté du
serveur.

## Installation

Une seule ligne dans le layout JTE :

```html
<script src="https://unpkg.com/htmx.org@2.0.4"></script>
```

(ou télécharger le fichier dans `src/main/resources/static/` et le servir
en local — Javalin sert `/static` automatiquement si configuré).

## Attributs principaux

| Attribut | Effet | Exemple |
|---|---|---|
| `hx-get` / `hx-post` / `hx-put` / `hx-patch` / `hx-delete` | Émettre une requête | `hx-get="/books"` |
| `hx-target` | Sélecteur CSS où injecter la réponse | `hx-target="#list"` |
| `hx-swap` | Comment injecter (cf. ci-dessous) | `hx-swap="outerHTML"` |
| `hx-trigger` | Quel événement déclenche la requête | `hx-trigger="keyup changed delay:300ms"` |
| `hx-push-url` | Mettre à jour l'URL du navigateur | `hx-push-url="true"` |
| `hx-vals` | Données supplémentaires à envoyer | `hx-vals='{"sort":"title"}'` |
| `hx-include` | Inclure d'autres champs du formulaire | `hx-include="#search"` |
| `hx-confirm` | `confirm()` avant la requête | `hx-confirm="Supprimer ?"` |
| `hx-indicator` | Afficher un loader pendant la requête | `hx-indicator="#spinner"` |
| `hx-boost` | Convertir tous les `<a>` / `<form>` enfants en requêtes htmx (avec push-state) | `hx-boost="true"` |

### Valeurs de `hx-swap`

| Valeur | Effet |
|---|---|
| `innerHTML` *(défaut)* | Remplace le contenu de la cible |
| `outerHTML` | Remplace la cible elle-même |
| `beforebegin` / `afterbegin` / `beforeend` / `afterend` | Insère relativement à la cible |
| `delete` | Supprime la cible |
| `none` | Ne rien injecter (utile pour les actions sans rendu) |

Modificateurs : `swap:300ms`, `settle:300ms`, `scroll:top`, `show:bottom`,
`focus-scroll:true`.

### Valeurs de `hx-trigger`

| Valeur | Effet |
|---|---|
| `click` *(défaut sur la plupart des éléments)* | Au clic |
| `change` | Au changement de la valeur d'un champ |
| `submit` *(défaut sur `<form>`)* | À la soumission |
| `keyup changed delay:300ms` | Au keyup, si valeur changée, après 300 ms |
| `revealed` | Quand l'élément entre dans le viewport (utile pour l'infinite scroll) |
| `every 5s` | Polling toutes les 5 s |
| `load` | Au chargement de la page (lazy load) |

## Patterns courants pour Biblion

### 1. Recherche live

```html
<input
  type="search"
  name="q"
  placeholder="Chercher un livre…"
  hx-get="/books/search"
  hx-trigger="keyup changed delay:300ms"
  hx-target="#book-list"
  hx-swap="innerHTML">

<ul id="book-list">
  @template.book.list(books = books)
</ul>
```

Côté Javalin :

```java
app.get("/books/search", ctx -> {
    String q = ctx.queryParam("q");
    List<Book> books = searchBooks.execute(q);
    ctx.render("book/list-fragment.jte", Map.of("books", books));
});
```

### 2. Charger un détail inline

```html
<a hx-get="/books/42" hx-target="#detail" hx-push-url="true">
  Le Petit Prince
</a>
<aside id="detail"></aside>
```

Le controller renvoie `book/detail.jte` (un fragment, pas la page entière).

### 3. Ajouter un livre — formulaire

```html
<form hx-post="/books" hx-target="#book-list" hx-swap="beforeend"
      hx-on::after-request="this.reset()">
  <input name="title" required>
  <input name="author">
  <button>Ajouter</button>
</form>
```

Le serveur renvoie *uniquement la nouvelle ligne* (`book/row.jte`), htmx
l'ajoute en fin de liste.

### 4. Supprimer une ligne

```html
<tr id="book-42">
  <td>Le Petit Prince</td>
  <td>
    <button
      hx-delete="/books/42"
      hx-target="#book-42"
      hx-swap="outerHTML swap:300ms"
      hx-confirm="Supprimer ce livre ?">
      ×
    </button>
  </td>
</tr>
```

Le serveur renvoie une réponse vide → la ligne disparaît avec une transition.

### 5. Infinite scroll / lazy loading

```html
<div hx-get="/books?page=2"
     hx-trigger="revealed"
     hx-swap="afterend">
  Chargement…
</div>
```

Quand l'élément entre dans le viewport, htmx charge la page 2 et l'insère
juste après — le `<div>` lui-même est remplacé par les nouvelles lignes (en
ajoutant à son tour un déclencheur pour la page 3 dans le HTML retourné).

## Intégration avec Javalin + JTE

### Conventions de templates

```
src/main/jte/
├── layout.jte              # squelette HTML (<head>, <body>, scripts, CSS)
├── book/
│   ├── list.jte            # page entière (utilise layout)
│   ├── list-fragment.jte   # fragment ré-utilisé pour htmx
│   ├── row.jte             # une ligne <tr>
│   └── detail.jte          # panneau de détail
```

Règle simple :
- **Page complète** = template avec layout, rendue à la première requête.
- **Fragment** = template sans layout, rendu pour les requêtes htmx.

### Détecter une requête htmx côté serveur

htmx envoie l'en-tête `HX-Request: true`. Utile pour décider si on rend la
page entière ou juste un fragment :

```java
boolean isHtmx = "true".equals(ctx.header("HX-Request"));
String view = isHtmx ? "book/list-fragment.jte" : "book/list.jte";
```

### En-têtes utiles côté serveur

Le serveur peut piloter htmx via des en-têtes de réponse :

| En-tête | Effet |
|---|---|
| `HX-Redirect: /login` | Redirection côté client |
| `HX-Refresh: true` | Recharger la page complète |
| `HX-Trigger: bookCreated` | Émet un événement JS côté client (utile pour rafraîchir un compteur, fermer un modal…) |
| `HX-Push-Url: /books/42` | Mettre à jour l'URL sans naviguer |

## Bonnes pratiques

- **Garder les controllers minces** : ils traduisent HTTP ↔ use case + choisissent
  le template (page vs fragment). La logique reste dans `application/`.
- **Un fragment = une responsabilité** : `book/row.jte` rend une ligne, pas
  une liste. La liste boucle sur les lignes. Ça facilite la réutilisation
  (création, mise à jour, suppression).
- **DTO de présentation** : si la vue a besoin de plus que le record `Book`
  (status formaté, URL de détail…), créer un `BookView` côté infrastructure,
  ne pas polluer le domaine.
- **CSRF** : à activer dès qu'on aura des POST/PUT/DELETE. Javalin offre
  un `RouteRole` ou un avant-handler pour vérifier un token.
- **Erreurs** : renvoyer un fragment d'erreur (statut 4xx/5xx + HTML) que
  htmx affiche via `hx-target-error` ou un `hx-swap-oob`.

## Pièges courants

- **Renvoyer une page complète à la place d'un fragment** : htmx swap tout,
  layout compris → double `<html>` dans le DOM. Toujours vérifier l'en-tête
  `HX-Request` ou avoir des routes dédiées aux fragments.
- **Cible introuvable** : si `hx-target="#xyz"` ne match rien, htmx ne fait
  rien (silencieusement). Vérifier la console htmx (`htmx.logAll()` en dev).
- **Form avec `hx-post` *et* `action`** : sans `hx-post`, le formulaire est
  soumis classiquement (rechargement complet). Toujours mettre `hx-post`.
- **Boost trop large** : `hx-boost="true"` sur `<body>` casse les liens
  externes / téléchargements si on n'exclut pas (`hx-boost="false"` ciblé).

## Ressources

- Site officiel : <https://htmx.org/>
- Documentation des attributs : <https://htmx.org/reference/>
- Patterns : <https://htmx.org/examples/>
- Livre gratuit *Hypermedia Systems* : <https://hypermedia.systems/>
