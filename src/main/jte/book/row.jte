@import fr.ikisource.biblion.book.domain.Book
@param Book book

<tr>
    <td>
        @if(book.coverUrl() != null)
            <img src="${book.coverUrl()}" alt="" style="height: 40px; width: auto;">
        @endif
    </td>
    <td>${book.title()}</td>
    <td>${book.author() != null ? book.author() : ""}</td>
    <td><small>${book.isbn() != null ? book.isbn().value() : "—"}</small></td>
    <td>
        <button class="secondary outline"
                aria-label="Supprimer"
                hx-delete="/books/${book.id().value()}"
                hx-confirm="Êtes-vous sûr de vouloir supprimer « ${book.title()} » ?"
                hx-target="closest tr"
                hx-swap="outerHTML">
            @template.icon.trash()
        </button>
    </td>
</tr>
