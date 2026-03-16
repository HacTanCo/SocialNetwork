let page = 0;

function loadMore() {

    page++;

    fetch(`/home?page=${page}`)
        .then(res => res.text())
        .then(html => {

            const parser = new DOMParser();
            const doc = parser.parseFromString(html, "text/html");

            const newPosts = doc.querySelectorAll(".post");

            const feed = document.querySelector("#feed");

            newPosts.forEach(post => feed.appendChild(post));

        });
}