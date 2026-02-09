package es.iesquevedo.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.iesquevedo.modelo.Pelicula;
import es.iesquevedo.util.LocalDateAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import es.iesquevedo.dao.PeliculaDao;

public class JsonPeliculaDao implements PeliculaDao {
    private final File file;
    private final Gson gson;
    private List<Pelicula> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public JsonPeliculaDao(String filePath) {
        this.file = new File(filePath);
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
        load();
        long maxId = store.stream().map(Pelicula::getId).filter(id -> id != null).mapToLong(Long::longValue).max().orElse(0L);
        idGenerator.set(Math.max(1, maxId + 1));
    }

    private void load() {
        try {
            if (!file.exists()) return;
            Type listType = new TypeToken<List<Pelicula>>() {}.getType();
            try (FileReader fr = new FileReader(file)) {
                List<Pelicula> list = gson.fromJson(fr, listType);
                if (list != null) store = list;
            }
        } catch (Exception e) {
            try {
                File backup = new File(file.getAbsolutePath() + ".corrupt.bak");
                Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                // ignore
            }
            store = new ArrayList<>();
            try { persist(); } catch (Exception ex) { /* ignore */ }
        }
    }

    private void persist() {
        try (FileWriter fw = new FileWriter(file)) {
            gson.toJson(store, fw);
        } catch (Exception e) {
            throw new RuntimeException("Error guardando peliculas en " + file.getAbsolutePath(), e);
        }
    }

    public Pelicula save(Pelicula pelicula) {
        if (pelicula.getId() == null) {
            pelicula.setId(idGenerator.getAndIncrement());
        } else {
            store = store.stream().filter(p -> !p.getId().equals(pelicula.getId())).collect(Collectors.toList());
        }
        store.add(pelicula);
        persist();
        return pelicula;
    }

    public Optional<Pelicula> findById(Long id) {
        return store.stream().filter(p -> p.getId() != null && p.getId().equals(id)).findFirst();
    }

    public List<Pelicula> findByTitulo(String titulo) {
        return store.stream().filter(p -> p.getTitulo() != null && p.getTitulo().equals(titulo)).collect(Collectors.toList());
    }

    public Optional<Pelicula> findAvailableByTitulo(String titulo) {
        return store.stream().filter(p -> p.getTitulo() != null && p.getTitulo().equals(titulo) && p.isDisponible()).findFirst();
    }

    public List<Pelicula> findAll() {
        return new ArrayList<>(store);
    }

    public boolean deleteById(Long id) {
        int before = store.size();
        store = store.stream().filter(p -> p.getId() == null || !p.getId().equals(id)).collect(Collectors.toList());
        boolean changed = store.size() != before;
        if (changed) persist();
        return changed;
    }
}
