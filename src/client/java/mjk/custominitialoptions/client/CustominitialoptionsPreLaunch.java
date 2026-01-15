package mjk.custominitialoptions.client;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustominitialoptionsPreLaunch implements PreLaunchEntrypoint {

    private static final String TEMPLATE_RESOURCE = "/custominitialoptions/options.txt";
    private static final String MARKER_RELATIVE_PATH = "config/custominitialoptions.applied";

    @Override
    public void onPreLaunch() {
        try {
            // MinecraftClient 뜨기 전이므로 client.runDirectory 못 씀
            // 대신 FabricLoader의 gameDir 사용 (너 로그의 runDir이랑 보통 동일하게 잡힘)
            Path runDir = FabricLoader.getInstance().getGameDir();
            Path optionsPath = runDir.resolve("options.txt");
            Path markerPath = runDir.resolve(MARKER_RELATIVE_PATH);

            System.out.println("[CustomInitialOptions] (preLaunch) runDir=" + runDir);

            if (Files.exists(markerPath)) {
                System.out.println("[CustomInitialOptions] (preLaunch) marker exists, skipping.");
                return;
            }

            String template = readResourceText(TEMPLATE_RESOURCE);

            // options.txt를 "게임이 읽기 전에" 만들어 둠
            Files.writeString(optionsPath, template, StandardCharsets.UTF_8);

            Files.createDirectories(markerPath.getParent());
            Files.writeString(markerPath, "applied", StandardCharsets.UTF_8);

            System.out.println("[CustomInitialOptions] (preLaunch) options.txt created (first run only).");

        } catch (Exception e) {
            System.err.println("[CustomInitialOptions] (preLaunch) Failed");
            e.printStackTrace();
        }
    }

    private static String readResourceText(String resourcePath) throws IOException {
        try (InputStream in = CustominitialoptionsPreLaunch.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
