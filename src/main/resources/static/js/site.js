// Busca, filtros e paginação funcionam por GET, inclusive sem JavaScript.
const year = document.querySelector('#year');
if (year) year.textContent = String(new Date().getFullYear());
