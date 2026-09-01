package dev.yakekusolsu.growthtools.api;

/** Version of the experimental binary API, independent from the plugin version. */
public record ApiVersion(int major, int minor) implements Comparable<ApiVersion> {
    public static final ApiVersion CURRENT = new ApiVersion(1, 0);

    public ApiVersion {
        if (major < 0 || minor < 0) throw new IllegalArgumentException("version must be non-negative");
    }

    @Override public int compareTo(ApiVersion other) {
        int majorComparison = Integer.compare(major, other.major);
        return majorComparison != 0 ? majorComparison : Integer.compare(minor, other.minor);
    }

    @Override public String toString() { return major + "." + minor; }
}
