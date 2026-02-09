package es.iesquevedo.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.iesquevedo.modelo.Alquiler;
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
import es.iesquevedo.dao.AlquilerDao;

public class JsonAlquilerDao implements AlquilerDao {
    private final File file;
    private final Gson gson;
    private List<Alquiler> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public JsonAlquilerDao(String filePath) {
        this.file = new File(filePath);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        load();
        long maxId = store.stream().map(Alquiler::getId).filter(id -> id != null).mapToLong(Long::longValue).max().orElse(0L);
        idGenerator.set(Math.max(1, maxId + 1));
    }

    private void load() {
        try {
            if (!file.exists()) return;
            Type listType = new TypeToken<List<Alquiler>>() {}.getType();
            try (FileReader fr = new FileReader(file)) {
                List<Alquiler> list = gson.fromJson(fr, listType);
                if (list != null) store = list;
            }
        } catch (Exception e) {
            // Si hay error al parsear el JSON, hacemos backup y seguimos con lista vacía
            try {
                File backup = new File(file.getAbsolutePath() + ".corrupt.bak");
                Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                // ignorar
            }
            store = new ArrayList<>();
            // intentar persistir fichero limpio para evitar futuros errores
            try {
                persist();
            } catch (Exception ex) {
                // ignorar
            }
        }
    }

    private void persist() {
        try (FileWriter fw = new FileWriter(file)) {
            gson.toJson(store, fw);
        } catch (Exception e) {
            throw new RuntimeException("Error guardando alquileres en " + file.getAbsolutePath(), e);
        }
    }

    public Alquiler save(Alquiler alquiler) {
        if (alquiler.getId() == null) {
            alquiler.setId(idGenerator.getAndIncrement());
        } else {
            // eliminar posible duplicado
            store = store.stream().filter(a -> !a.getId().equals(alquiler.getId())).collect(Collectors.toList());
        }
        store.add(alquiler);
        persist();
        return alquiler;
    }

    public Optional<Alquiler> findById(Long id) {
        return store.stream().filter(a -> a.getId() != null && a.getId().equals(id)).findFirst();
    }

    public List<Alquiler> findAll() {
        return new ArrayList<>(store);
    }

    public List<Alquiler> findBySocio(String dni) {
        return store.stream().filter(a -> a.getSocio() != null && a.getSocio().getDni().equals(dni)).collect(Collectors.toList());
    }

    public List<Alquiler> findActiveByPelicula(String titulo) {
        return store.stream().filter(a -> a.getPelicula() != null && a.getPelicula().getTitulo().equals(titulo) && a.getFechaDevolucion() == null).collect(Collectors.toList());
    }

    public boolean deleteById(Long id) {
        int before = store.size();
        store = store.stream().filter(a -> a.getId() == null || !a.getId().equals(id)).collect(Collectors.toList());
        boolean changed = store.size() != before;
        if (changed) persist();
        return changed;
    }
}
