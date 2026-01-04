package io.github.snow1026.snowlib.util;

import io.github.snow1026.snowlib.SnowLibrary;
import org.bukkit.Bukkit;
import java.util.logging.Level;

/**
 * 마인크래프트 서버의 버전을 확인하고 비교하기 위한 유틸리티 클래스입니다.
 * <p>
 * 서버 버전이 리스트에 없을 경우(신규 버전 등)에도 플러그인이 중단되지 않도록
 * 유연한 예외 처리 및 최신 버전 폴백(Fallback) 로직이 포함되어 있습니다.
 * </p>
 */
public final class VersionUtil {

    private VersionUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * 지원되는 마인크래프트 버전 매핑 열거형입니다.
     */
    public enum MappingsVersion {
        v1_17_R1("1.17", "1.17.1"),
        v1_18_R1("1.18", "1.18.1"),
        v1_18_R2("1.18.2"),
        v1_19_R1("1.19", "1.19.1", "1.19.2"),
        v1_19_R2("1.19.3"),
        v1_19_R3("1.19.4"),
        v1_20_R1("1.20", "1.20.1"),
        v1_20_R2("1.20.2"),
        v1_20_R3("1.20.3", "1.20.4"),
        v1_20_R4("1.20.5", "1.20.6"),
        v1_21_R1("1.21", "1.21.1"),
        v1_21_R2("1.21.2", "1.21.3"),
        v1_21_R3("1.21.4"),
        v1_21_R4("1.21.5"),
        v1_21_R5("1.21.6", "1.21.7", "1.21.8"),
        v1_21_R6("1.21.9", "1.21.10"),
        v1_21_R7("1.21.11"),
        /** 알 수 없거나 지원 예정인 최신 버전을 위한 플레이스홀더 */
        UNKNOWN("Unknown");

        private final String[] bukkitVersions;

        MappingsVersion(String... bukkitVersions) {
            this.bukkitVersions = bukkitVersions;
        }

        /**
         * Bukkit 버전 문자열로부터 매핑된 Version Enum을 찾습니다.
         *
         * @param bukkitVersion Bukkit.getBukkitVersion() 문자열
         * @return 해당하는 MappingsVersion (찾지 못하면 UNKNOWN 반환)
         */
        public static MappingsVersion fromBukkitVersion(String bukkitVersion) {
            if (bukkitVersion == null || bukkitVersion.isEmpty()) {
                return UNKNOWN;
            }

            try {
                // "1.20.1-R0.1-SNAPSHOT" -> "1.20.1"
                String clean = bukkitVersion.split("-")[0];

                for (MappingsVersion ver : values()) {
                    if (ver == UNKNOWN) continue;
                    for (String bVer : ver.bukkitVersions) {
                        if (bVer.equals(clean)) return ver;
                    }
                }
            } catch (Exception ignored) {}

            return UNKNOWN;
        }
    }

    private static final MappingsVersion currentVersion;

    static {
        String versionString = Bukkit.getBukkitVersion();
        MappingsVersion detected = MappingsVersion.fromBukkitVersion(versionString);

        // [중요] UNKNOWN일 경우(즉, 코드에 등록되지 않은 신규 버전일 경우)
        // 플러그인이 죽지 않도록 현재 코드상 가장 최신 버전을 기본값으로 사용합니다.
        if (detected == MappingsVersion.UNKNOWN) {
            MappingsVersion[] values = MappingsVersion.values();
            // UNKNOWN 제외 가장 마지막(최신) 상수 선택
            currentVersion = values.length > 1 ? values[values.length - 2] : MappingsVersion.UNKNOWN;

            SnowLibrary.snowlibrary().getLogger().log(Level.WARNING, "[SnowLib] 알 수 없는 서버 버전 감지: {0}", versionString);
            SnowLibrary.snowlibrary().getLogger().log(Level.WARNING, "[SnowLib] 최신 매핑({0})을 사용하여 호환성 모드로 실행을 시도합니다.", currentVersion.name());
        } else {
            currentVersion = detected;
        }
    }

    /**
     * 현재 서버의 매핑 버전을 가져옵니다.
     */
    public static MappingsVersion getNmsVersion() {
        return currentVersion;
    }

    /**
     * 현재 버전이 지정된 버전 이상인지 확인합니다.
     */
    public static boolean isAtLeast(MappingsVersion version) {
        if (currentVersion == MappingsVersion.UNKNOWN || version == MappingsVersion.UNKNOWN) return true;
        return currentVersion.ordinal() >= version.ordinal();
    }

    /**
     * 현재 버전이 지정된 버전 이하인지 확인합니다.
     */
    public static boolean isAtMost(MappingsVersion version) {
        if (currentVersion == MappingsVersion.UNKNOWN || version == MappingsVersion.UNKNOWN) return false;
        return currentVersion.ordinal() <= version.ordinal();
    }
}
