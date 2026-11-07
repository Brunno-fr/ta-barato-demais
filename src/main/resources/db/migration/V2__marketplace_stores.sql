INSERT INTO stores(name, slug) VALUES ('Mercado Livre', 'mercado-livre'), ('Shopee', 'shopee')
ON CONFLICT (slug) DO NOTHING;
