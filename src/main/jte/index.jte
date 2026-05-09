@import fr.ikisource.biblion.book.domain.Book
@import java.util.List
@param List<Book> books

@template.layout(title = "Biblion", content = @`
    <hgroup>
        <h1>Biblion</h1>
        <p>Votre bibliothèque personnelle.</p>
    </hgroup>

    <article>
        <h2>Ajouter un livre</h2>
        <form hx-post="/books"
              hx-target="#book-list"
              hx-swap="afterbegin"
              hx-on::after-request="
                  if (event.target !== this) return;
                  var f = document.getElementById('form-feedback');
                  if (event.detail.successful) { this.reset(); f.textContent = ''; document.getElementById('book-preview').innerHTML = ''; }
                  else { f.textContent = event.detail.xhr.responseText || 'Erreur'; }
              ">
            <label for="isbn">ISBN <small>(obligatoire)</small></label>
            <input type="text" id="isbn" name="isbn" required placeholder="9782070408504"
                   hx-get="/books/lookup"
                   hx-trigger="change, keyup changed delay:500ms"
                   hx-target="#book-preview"
                   hx-swap="innerHTML">
            <div id="book-preview"></div>
            <details>
                <summary>Saisie manuelle (si l'ISBN n'est pas reconnu)</summary>
                <label for="title">Titre</label>
                <input type="text" id="title" name="title">
                <label for="author">Auteur</label>
                <input type="text" id="author" name="author">
            </details>
            <button type="submit">Ajouter</button>
        </form>
        <p id="form-feedback" role="alert"></p>
    </article>

    <h2>Catalogue (${books.size()})</h2>

    <table class="striped">
        <thead>
            <tr>
                <th scope="col">Titre</th>
                <th scope="col">Auteur</th>
                <th scope="col">ISBN</th>
                <th scope="col"></th>
            </tr>
        </thead>
        <tbody id="book-list">
            @for(Book book : books)
                @template.book.row(book = book)
            @endfor
        </tbody>
    </table>
`)
