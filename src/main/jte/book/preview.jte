@import fr.ikisource.biblion.book.domain.BookMetadata
@param String state
@param BookMetadata metadata = null

@if("already".equals(state))
    <article>
        <strong>⚠ Cet ISBN est déjà dans votre catalogue.</strong>
    </article>
@elseif("found".equals(state) && metadata != null)
    <article>
        <header>Trouvé sur Google Books</header>
        <div style="display: flex; gap: 1rem; align-items: flex-start;">
            @if(metadata.coverUrl() != null)
                <img src="${metadata.coverUrl()}" alt="Couverture" style="height: 100px; width: auto;">
            @endif
            <div>
                <strong>${metadata.title()}</strong>
                @if(metadata.author() != null)
                    <br>${metadata.author()}
                @endif
            </div>
        </div>
    </article>
@elseif("not-found".equals(state))
    <article>
        <small>ISBN inconnu de Google Books — vous pouvez compléter manuellement ci-dessous.</small>
    </article>
@elseif("invalid".equals(state))
    <article>
        <small>Format d'ISBN invalide.</small>
    </article>
@endif
