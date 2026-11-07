package com.tabaratodemais.home;

import com.tabaratodemais.catalog.CategoryService;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

/** Ícones locais; categorias personalizadas usam o símbolo da principal ou a grade. */
final class CategoryIcons {
    private static final Map<String, String> ICONS = Map.ofEntries(
        Map.entry("eletronicos", "phone"), Map.entry("informatica", "laptop"),
        Map.entry("eletrodomesticos", "fridge"), Map.entry("casa-e-decoracao", "home"),
        Map.entry("beleza-e-cuidados", "flower"), Map.entry("moda", "shirt"),
        Map.entry("esportes-e-lazer", "dumbbell"), Map.entry("ferramentas", "tool"),
        Map.entry("brinquedos", "game"), Map.entry("automotivo", "car"),
        Map.entry("fones-de-ouvido", "headphones"), Map.entry("celulares-e-smartphones", "phone")
    );

    static String forCategory(CategoryService.Row category) {
        for (String value : new String[]{category.name(), category.slug(), category.parentName()}) {
            if (value == null) continue;
            String key = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).strip()
                .replaceAll("[^a-z0-9]+", "-");
            if (ICONS.containsKey(key)) return "#i-" + ICONS.get(key);
        }
        return "#i-grid";
    }

    private CategoryIcons() {}
}
