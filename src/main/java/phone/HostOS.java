package phone;

/**
 * Platform detection utility for identifying the underlying operating system.
 * Provides static methods to check whether the application is running on a
 * Windows, Linux, or other operating system environment.
 *
 * This class implements a lightweight approach to OS detection by analyzing
 * the system property "os.name" at class initialization time.
 */
// AI assisted in understanding how to determine the host operating system.
public class HostOS {
    // System properties are checked once at class loading for efficiency
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_WINDOWS = OS_NAME.contains("win");
    private static final boolean IS_LINUX = OS_NAME.contains("nux");

    /**
     * Checks if the application is running on a Windows platform.
     * Windows detection is performed by searching for the substring "win"
     * in the lowercase system property "os.name".
     *
     * @return {@code true} when running on any Windows version, {@code false} otherwise
     */
    public static boolean isWindows() {
        return IS_WINDOWS;
    }

    /**
     * Checks if the application is running on a Linux-based platform.
     * Linux detection is performed by searching for the substring "nux"
     * in the lowercase system property "os.name", which matches Linux,
     * GNU/Linux, and similar distributions.
     *
     * @return {@code true} when running on a Linux-based system, {@code false} otherwise
     */
    public static boolean isLinux() {
        return IS_LINUX;
    }
}