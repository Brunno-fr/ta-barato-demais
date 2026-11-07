-- Categorias iniciais; preserva nomes, URLs e relações já cadastrados.
WITH suggested(name, slug) AS (VALUES
    ('Eletrônicos', 'eletronicos'),
    ('Informática', 'informatica'),
    ('Eletrodomésticos', 'eletrodomesticos'),
    ('Casa e Decoração', 'casa-e-decoracao'),
    ('Beleza e Cuidados', 'beleza-e-cuidados'),
    ('Moda', 'moda'),
    ('Esportes e Lazer', 'esportes-e-lazer'),
    ('Ferramentas', 'ferramentas'),
    ('Brinquedos', 'brinquedos'),
    ('Automotivo', 'automotivo')
)
INSERT INTO categories(name, slug)
SELECT s.name, s.slug FROM suggested s
WHERE NOT EXISTS (
    SELECT 1 FROM categories c
    WHERE c.slug = s.slug
       OR translate(lower(trim(c.name)), 'áàâãéêíóôõúüç', 'aaaaeeiooouuc')
        = translate(lower(s.name), 'áàâãéêíóôõúüç', 'aaaaeeiooouuc')
)
ON CONFLICT (slug) DO NOTHING;
