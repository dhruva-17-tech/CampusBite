package com.campusbite.config;

import com.campusbite.menu.MenuItem;
import com.campusbite.menu.MenuItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class MenuSeeder implements ApplicationRunner {
    private final MenuItemRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    public MenuSeeder(MenuItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count() > 0) return;
        try (InputStream input = new ClassPathResource("menu-seed.json").getInputStream()) {
            JsonNode items = mapper.readTree(input);
            for (JsonNode item : items) {
                MenuItem saved = repository.save(new MenuItem(
                    item.path("name").asText(),
                    item.path("description").asText(),
                    item.path("category").asText(),
                    item.path("price").asDouble(),
                    item.path("stock").asInt(),
                    item.path("outlet").asText(),
                    item.path("imageUrl").asText(""),
                    item.path("available").asBoolean(true)
                ));
                saved.setEmoji(item.path("emoji").asText("🍽️"));
                repository.save(saved);
            }
        }
    }
}
