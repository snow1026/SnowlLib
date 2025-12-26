package io.github.snow1026.snowlib.util;

import org.bukkit.Bukkit;

/**
 * 마인크래프트 서버의 버전을 확인하고 비교하기 위한 유틸리티 클래스입니다.
 * <p>
 * Bukkit의 버전 문자열(예: "1.20.1")을 내부 식별자(Enum)로 매핑하여,
 * 플러그인이 실행 중인 서버의 버전을 쉽게 식별하고 버전별 분기 처리를 할 수 있도록 돕습니다.
 * </p>
 */
public final class VersionUtil {

    /**
     * 지원되는 마인크래프트 버전 매핑 열거형입니다.
     * <p>
     * 각 상수는 내부 버전 식별자(예: v1_20_R1)를 나타내며,
     * 해당 버전에 포함되는 실제 마인크래프트 버전 문자열(예: "1.20", "1.20.1")을 포함합니다.
     * </p>
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
        v1_21_R7("1.21.11");

        /** 해당 매핑 버전에 속하는 마인크래프트 버전 문자열 목록 */
        private final String[] bukkitVersions;

        /**
         * 버전 매핑을 생성합니다.
         *
         * @param bukkitVersions 이 버전에 해당하는 마인크래프트 버전 문자열들 (예: "1.20.1")
         */
        MappingsVersion(String... bukkitVersions) {
            this.bukkitVersions = bukkitVersions;
        }

        /**
         * Bukkit 버전 문자열로부터 매핑된 Version Enum을 찾습니다.
         *
         * @param bukkitVersion Bukkit.getBukkitVersion() 등에서 얻은 버전 문자열 (예: "1.20.1-R0.1-SNAPSHOT")
         * @return 해당하는 MappingsVersion 상수
         * @throws IllegalArgumentException 입력된 버전 문자열이 null인 경우
         * @throws RuntimeException 지원하지 않는 버전이거나 알 수 없는 버전인 경우
         */
        public static MappingsVersion fromBukkitVersion(String bukkitVersion) {
            if (bukkitVersion == null) {
                throw new IllegalArgumentException("Bukkit version is null");
            }

            // "1.20.1-R0.1-SNAPSHOT" 같은 형식에서 앞부분("1.20.1")만 추출
            String clean = bukkitVersion.split("-")[0];
            for (MappingsVersion ver : values()) {
                for (String bVer : ver.bukkitVersions) {
                    if (bVer.equals(clean)) {
                        return ver;
                    }
                }
            }
            throw new RuntimeException("Unsupported Bukkit version: " + bukkitVersion);
        }
    }

    /** 현재 서버에서 실행 중인 감지된 버전 */
    private static final MappingsVersion currentVersion;

    // 정적 초기화 블록: 서버 실행 시 현재 버전을 감지하여 캐싱합니다.
    static {
        String bukkitVersion = Bukkit.getBukkitVersion();
        currentVersion = MappingsVersion.fromBukkitVersion(bukkitVersion);
    }

    /**
     * 현재 서버 버전이 지정된 버전보다 높거나 같은지 확인합니다.
     *
     * @param version 기준 버전
     * @return 현재 버전이 기준 버전 이상이면 true, 아니면 false
     */
    public static boolean isAtLeast(MappingsVersion version) {
        return currentVersion.ordinal() >= version.ordinal();
    }

    /**
     * 현재 서버의 매핑된 버전을 가져옵니다.
     *
     * @return 현재 서버 버전 (MappingsVersion)
     */
    public static MappingsVersion getNmsVersion() {
        return currentVersion;
    }
}
