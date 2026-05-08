@import java.util.List
@param String title
@param List<String> books

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
    <style>
        body { font-family: system-ui, sans-serif; max-width: 720px; margin: 2rem auto; padding: 0 1rem; }
        h1 { color: #2c3e50; }
        ul { line-height: 1.8; }
    </style>
</head>
<body>
    <h1>${title}</h1>
    <p>Bienvenue dans Biblion — votre bibliothèque personnelle.</p>

    <h2>Livres (${books.size()})</h2>
    @if(books.isEmpty())
        <p><em>Aucun livre pour le moment.</em></p>
    @else
        <ul>
        @for(String book : books)
            <li>${book}</li>
        @endfor
        </ul>
    @endif

    <button hx-get="/ping" hx-swap="outerHTML">Tester htmx</button>
</body>
</html>
