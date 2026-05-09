@import fr.ikisource.biblion.book.domain.Book
@param Book book

<tr>
    <td>${book.title()}</td>
    <td>${book.author() != null ? book.author() : ""}</td>
    <td><small>${book.isbn() != null ? book.isbn().value() : "—"}</small></td>
    <td>
        <button class="secondary outline"
                hx-delete="/books/${book.id().value()}"
                hx-confirm="Êtes-vous sûr de vouloir supprimer « ${book.title()} » ?"
                hx-target="closest tr"
                hx-swap="outerHTML">
            Supprimer
        </button>
    </td>
</tr>
