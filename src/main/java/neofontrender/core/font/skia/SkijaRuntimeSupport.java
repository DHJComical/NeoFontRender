package neofontrender.core.font.skia;

import java.util.Locale;

/**
 * Detects whether the current runtime can use the embedded Skija artifacts.
 */
public final class SkijaRuntimeSupport {

    private static final int MIN_JAVA_VERSION = 8;

    private SkijaRuntimeSupport() {
    }

    public static Compatibility checkCompatibility() {
        int javaVersion = detectJavaMajorVersion();
        if (javaVersion < MIN_JAVA_VERSION) {
            return Compatibility.unsupported(javaVersion, null,
                    "Java " + javaVersion + " is below the minimum supported runtime for the embedded Skija package (" + MIN_JAVA_VERSION + "+)");
        }

        String classifier = detectEmbeddedClassifier();
        if (classifier == null) {
            return Compatibility.unsupported(javaVersion, null,
                    "No embedded Skija native is packaged for platform " + normalizedOsName() + "/" + normalizedOsArch());
        }

        if (!hasSkijaApi()) {
            return Compatibility.unsupported(javaVersion, classifier,
                    "Skija runtime mod is not installed (use the full package or add neofontrender-skia)");
        }

        String nativeResource = nativeResourcePath();
        if (nativeResource == null || SkijaRuntimeSupport.class.getResource(nativeResource) == null) {
            return Compatibility.unsupported(javaVersion, classifier,
                    "Skija runtime is missing the native library for " + normalizedOsName() + "/" + normalizedOsArch());
        }

        return Compatibility.supported(javaVersion, classifier);
    }

    private static boolean hasSkijaApi() {
        try {
            // Do not initialize Skija here: this capability check runs while the settings screen is
            // open and loading the native backend belongs to FontManager's guarded reload path.
            Class.forName("io.github.humbleui.skija.FontMgr", false, SkijaRuntimeSupport.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private static String nativeResourcePath() {
        String os = normalizedOsName();
        String arch = normalizedOsArch();
        String platformArch = isArm64(arch) ? "arm64" : isX64(arch) ? "x64" : null;
        if (platformArch == null) {
            return null;
        }
        if (os.contains("win")) {
            return "/io/github/humbleui/skija/windows/" + platformArch + "/skija.dll";
        }
        if (os.contains("mac") || os.contains("darwin")) {
            return "/io/github/humbleui/skija/macos/" + platformArch + "/libskija.dylib";
        }
        if (os.contains("linux")) {
            return "/io/github/humbleui/skija/linux/" + platformArch + "/libskija.so";
        }
        return null;
    }

    private static int detectJavaMajorVersion() {
        String specVersion = System.getProperty("java.specification.version", "8").trim();
        if (specVersion.startsWith("1.")) {
            specVersion = specVersion.substring(2);
        }
        int dot = specVersion.indexOf('.');
        if (dot >= 0) {
            specVersion = specVersion.substring(0, dot);
        }
        try {
            return Integer.parseInt(specVersion);
        } catch (NumberFormatException ignored) {
            return MIN_JAVA_VERSION;
        }
    }

    private static String detectEmbeddedClassifier() {
        String os = normalizedOsName();
        String arch = normalizedOsArch();

        if (os.contains("win")) {
            if (isX64(arch)) {
                return "skija-windows-x64";
            }
            return null;
        }
        if (os.contains("mac") || os.contains("darwin")) {
            if (isArm64(arch)) {
                return "skija-macos-arm64";
            }
            if (isX64(arch)) {
                return "skija-macos-x64";
            }
            return null;
        }
        if (os.contains("linux")) {
            if (isArm64(arch)) {
                return "skija-linux-arm64";
            }
            if (isX64(arch)) {
                return "skija-linux-x64";
            }
            return null;
        }
        return null;
    }

    private static boolean isX64(String arch) {
        return "x86_64".equals(arch) || "amd64".equals(arch) || "x64".equals(arch);
    }

    private static boolean isArm64(String arch) {
        return "aarch64".equals(arch) || "arm64".equals(arch);
    }

    private static String normalizedOsName() {
        return System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
    }

    private static String normalizedOsArch() {
        return System.getProperty("os.arch", "unknown").toLowerCase(Locale.ROOT);
    }

    public static final class Compatibility {
        private final boolean supported;
        private final int javaVersion;
        private final String classifier;
        private final String message;

        private Compatibility(boolean supported, int javaVersion, String classifier, String message) {
            this.supported = supported;
            this.javaVersion = javaVersion;
            this.classifier = classifier;
            this.message = message;
        }

        public static Compatibility supported(int javaVersion, String classifier) {
            return new Compatibility(true, javaVersion, classifier,
                    "Java " + javaVersion + ", native " + classifier);
        }

        public static Compatibility unsupported(int javaVersion, String classifier, String message) {
            return new Compatibility(false, javaVersion, classifier, message);
        }

        public boolean isSupported() {
            return supported;
        }

        public int getJavaVersion() {
            return javaVersion;
        }

        public String getClassifier() {
            return classifier;
        }

        public String getMessage() {
            return message;
        }
    }
}
