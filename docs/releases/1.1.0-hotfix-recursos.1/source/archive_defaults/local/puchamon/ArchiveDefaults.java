package local.puchamon;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

/** Seeds client assets from the official archive already verified by the updater. */
public final class ArchiveDefaults implements PreLaunchEntrypoint {
    private static final Set<String> RETIRED_PACKS = Set.of(
            "file/CobbleMotion RP.zip", "file/EeveelutionsReimagined RP.zip", "file/GlitchDex RP.zip",
            "file/LackingMons RP.zip", "file/MysticMons RP.zip", "file/TDmon RP.zip");
    private static final Pattern PACK_SELECTION = Pattern.compile(
            "(?m)^(\\uFEFF?)(resourcePacks|incompatibleResourcePacks):([^\\r\\n]*)");
    private static final Pattern OLD_QUEST_SHORTCUT = Pattern.compile(
            "(?m)^(\\uFEFF?key_key\\.ftbquests\\.gui\\.player_prefs:)key\\.keyboard\\.p(?=\\r?$)");

    private record Asset(String hash, String previous, Path target, String entry) {}

    @Override
    public void onPreLaunch() {
        try {
            install(FabricLoader.getInstance().getGameDir());
        } catch (Exception failure) {
            throw new IllegalStateException("Puchamon no pudo preparar los recursos oficiales de Cobbleverse.", failure);
        }
    }

    public static void main(String[] args) throws Exception {
        install(Path.of(args[0]));
    }

    static void install(Path directory) throws Exception {
        Path game = directory.toRealPath();
        migrateClientSettings(game, "options.txt");
        migrateClientSettings(game, "config/defaultoptions/options.txt");
        migrateClientSettings(game, "config/defaultoptions/keybindings.txt");
        try (InputStream spec = ArchiveDefaults.class.getResourceAsStream("/puchamon-archive-defaults.tsv");
             var lines = new BufferedReader(new InputStreamReader(spec, StandardCharsets.UTF_8))) {
            Path archive = checked(game, "puchamon/downloads/" + lines.readLine() + ".mrpack");
            if (!Files.isRegularFile(archive)) {
                // Freesm's normal entry point downloads the archive before Fabric starts.
                // Direct starts from a fully installed instance can omit that cache.
                return;
            }
            List<Asset> pending = new ArrayList<>();
            for (String line; (line = lines.readLine()) != null;) {
                String[] fields = line.split("\t", -1);
                var target = checked(game, fields[2]);
                if (!Files.exists(target) || (!fields[1].isEmpty() && fields[1].equals(hash(target)))) {
                    pending.add(new Asset(fields[0], fields[1], target, fields[3]));
                }
            }
            if (pending.isEmpty()) return;
            try (var zip = new ZipFile(archive.toFile())) {
                for (Asset asset : pending) {
                    var entry = zip.getEntry(asset.entry());
                    if (entry == null || entry.isDirectory() || entry.getSize() > 512L * 1024 * 1024) {
                        throw new IOException("Recurso oficial ausente: " + asset.entry());
                    }
                    Files.createDirectories(asset.target().getParent());
                    Path staged = Files.createTempFile(asset.target().getParent(), ".puchamon-", ".asset");
                    try {
                        try (var input = zip.getInputStream(entry)) {
                            Files.copy(input, staged, StandardCopyOption.REPLACE_EXISTING);
                        }
                        if (!hash(staged).equals(asset.hash())) {
                            throw new IOException("Recurso oficial con hash incorrecto: " + asset.entry());
                        }
                        if (Files.exists(asset.target())) {
                            if (asset.previous().equals(hash(asset.target()))) {
                                Files.move(staged, asset.target(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        } else {
                            Files.move(staged, asset.target());
                        }
                    } finally {
                        Files.deleteIfExists(staged);
                    }
                }
            }
        }
    }

    private static void migrateClientSettings(Path game, String relative) throws IOException {
        Path options = checked(game, relative);
        if (!Files.isRegularFile(options)) return;
        String original = Files.readString(options);
        String migrated = original.contains("key_key.lumymon.access_pc:key.keyboard.p")
                ? OLD_QUEST_SHORTCUT.matcher(original).replaceAll("$1key.keyboard.unknown")
                : original;
        Matcher lines = PACK_SELECTION.matcher(migrated);
        StringBuilder result = new StringBuilder();
        while (lines.find()) {
            var selection = parsePacks(lines.group(3));
            if (selection == null || !selection.asList().removeIf(pack ->
                    pack instanceof JsonPrimitive id && id.isString() && RETIRED_PACKS.contains(id.getAsString()))) {
                continue;
            }
            if (lines.group(2).equals("resourcePacks")) {
                insertDefault(selection, "musicnotification:vanillabackport", "highlight:highlight_extended");
                if (Files.isRegularFile(checked(game, "resourcepacks/Better Leaves.zip"))) {
                    insertDefault(selection, "file/Better Leaves.zip", "continuity:default");
                }
            }
            lines.appendReplacement(result, Matcher.quoteReplacement(lines.group(1) + lines.group(2) + ":" + selection));
        }
        lines.appendTail(result);
        if (original.contentEquals(result)) return;
        Path temporary = Files.createTempFile(options.getParent(), ".puchamon-options-", ".tmp");
        try {
            Files.writeString(temporary, result);
            try {
                Files.move(temporary, options, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, options, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void insertDefault(JsonArray selection, String name, String before) {
        var pack = new JsonPrimitive(name);
        var packs = selection.asList();
        if (packs.contains(pack)) return;
        int anchor = packs.indexOf(new JsonPrimitive(before));
        if (anchor >= 0) packs.add(anchor, pack);
    }

    private static JsonArray parsePacks(String value) {
        try {
            var parsed = JsonParser.parseString(value);
            return parsed.isJsonArray() ? parsed.getAsJsonArray() : null;
        } catch (JsonParseException invalid) {
            return null;
        }
    }

    private static Path checked(Path game, String relative) throws IOException {
        Path path = game.resolve(relative).normalize();
        if (!path.startsWith(game) || path.equals(game)) throw new IOException("Ruta inválida");
        for (Path current = path; !current.equals(game); current = current.getParent()) {
            if (Files.exists(current) && !current.toRealPath().startsWith(game)) {
                throw new IOException("Enlace de recursos fuera de la instancia");
            }
        }
        return path;
    }

    private static String hash(Path path) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[65536];
            for (int n; (n = input.read(buffer)) >= 0;) digest.update(buffer, 0, n);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
