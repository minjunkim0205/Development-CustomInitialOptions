package mjk.custominitialoptions.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CustominitialoptionsClient implements ClientModInitializer {

    private static final String TEMPLATE_RESOURCE = "/custominitialoptions/options.txt";
    private static final String MARKER_RELATIVE_PATH = "config/custominitialoptions.applied";

    private static boolean didAttempt = false;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(CustominitialoptionsClient::tickOnce);
    }

    private static void tickOnce(MinecraftClient client) {
        if (didAttempt) return;
        if (client == null || client.options == null) return;

        // 로딩 오버레이 중이면 옵션 적용 타이밍이 꼬일 수 있어서 조금 기다림
        if (client.getOverlay() != null) return;

        didAttempt = true;

        try {
            Path runDir = client.runDirectory.toPath();
            Path optionsPath = runDir.resolve("options.txt");
            Path markerPath = runDir.resolve(MARKER_RELATIVE_PATH);

            System.out.println("[CustomInitialOptions] runDir=" + runDir);

            if (Files.exists(markerPath)) {
                System.out.println("[CustomInitialOptions] marker exists, skipping.");
                return;
            }

            // 1) 템플릿 읽기
            String template = readResourceText(TEMPLATE_RESOURCE);

            // 2) options.txt 작성 (첫 실행만)
            Files.writeString(optionsPath, template, StandardCharsets.UTF_8);

            // 3) **중요**: 이미 로드가 끝난 뒤라서, 지금 세션에 반영하려면 강제로 다시 load
            //    (이게 없으면 "파일은 생기는데 옵션은 안 바뀜" 상태가 됨)
            client.options.load();

            // 4) 마커 작성은 "로드까지 끝난 다음"에
            Files.createDirectories(markerPath.getParent());
            Files.writeString(markerPath, "applied", StandardCharsets.UTF_8);

            System.out.println("[CustomInitialOptions] options.txt applied + reloaded (first run only).");

        } catch (Exception e) {
            System.err.println("[CustomInitialOptions] Failed to apply options.txt");
            e.printStackTrace();
        }
    }

    private static String readResourceText(String resourcePath) throws IOException {
        try (InputStream in = CustominitialoptionsClient.class.getResourceAsStream(resourcePath)) {
            if (in == null) throw new IOException("Resource not found: " + resourcePath);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
