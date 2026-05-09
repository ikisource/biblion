# biblion

L'application Biblion permet de gérer sa bibliothèque de livres.
On peut ajouter un livre en fournissant le n° ISBN ou via une recherche qui va aller récupérer le livre dans une base
de données tierce.

## À propos de l'ISBN

Deux versions du standard ISBN coexistent — Biblion accepte les deux.

### Origine

- **ISBN-10** : standard original (1970 → 2006), 10 chiffres.
- **ISBN-13** : standard actuel depuis le **1ᵉʳ janvier 2007**, 13 chiffres.
  Imposé par l'industrie pour fusionner avec le système EAN-13 (codes-barres
  produits) — un livre est aussi un produit scanné en caisse.

### Structure

```
ISBN-10 :    2 - 07 - 040850 - 4
             │   │      │       │
             │   │      │       └─ chiffre de contrôle (mod 11, peut être 'X')
             │   │      └────────  numéro de titre
             │   └─────────────── préfixe éditeur
             └─────────────────── préfixe linguistique / régional (2 = francophone)

ISBN-13 :  978 - 2 - 07 - 040850 - 4
           │
           └─ préfixe EAN (978 = "Bookland" ; 979 depuis ~2010, quand 978 sature)
```

Les quatre blocs internes sont identiques entre ISBN-10 et ISBN-13 — seuls
changent le **préfixe EAN** (978/979) et le **chiffre de contrôle**.

### Chiffre de contrôle

Le calcul est différent dans les deux versions :

| | Modulo | Poids | Cas particulier |
|---|---|---|---|
| **ISBN-10** | 11 | `10·d₁ + 9·d₂ + … + 1·d₁₀` | dernier chiffre peut être `X` (= 10) |
| **ISBN-13** | 10 | poids alternés `1, 3, 1, 3, …` | toujours un chiffre |

Conséquence : on ne peut **pas** déduire l'ISBN-13 d'un ISBN-10 par simple
ajout du préfixe `978` — il faut recalculer le chiffre de contrôle.

### Conversion ISBN-10 → ISBN-13

```
ISBN-10                : 2-07-040850-4
1. retirer le checksum : 2-07-040850
2. préfixer 978        : 978-2-07-040850
3. recalculer checksum : 978-2-07-040850-4
```

Tous les ISBN-10 ont un équivalent ISBN-13. L'inverse n'est vrai que pour
les ISBN-13 commençant par `978` — les `979` n'ont pas de pendant ISBN-10.

### En pratique

- **Livres publiés avant 2007** : peuvent n'avoir qu'un ISBN-10 (rare
  aujourd'hui, la plupart sont ré-encodés en ISBN-13 dans les catalogues).
- **Livres publiés depuis 2007** : ISBN-13 obligatoire, ISBN-10 optionnel.
- **Google Books API** : renvoie les deux formats quand ils existent
  (champ `industryIdentifiers`).

La forme canonique moderne est l'**ISBN-13** ; c'est elle qu'il vaut mieux
stocker quand on a le choix.
