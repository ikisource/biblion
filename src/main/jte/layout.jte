@import gg.jte.Content
@param String title
@param Content content

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css">
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
    <script>
        document.addEventListener('htmx:beforeSwap', function (evt) {
            if (evt.detail.xhr.status === 204) {
                evt.detail.shouldSwap = true;
            }
        });
    </script>
    <style>
        #form-feedback { color: #c0392b; margin: 0.5rem 0; }
        #form-feedback:empty { display: none; }
        table button { margin: 0; padding: 0.25rem 0.6rem; font-size: 0.85em; width: auto; }
    </style>
</head>
<body>
    <main class="container">
        ${content}
    </main>
</body>
</html>
