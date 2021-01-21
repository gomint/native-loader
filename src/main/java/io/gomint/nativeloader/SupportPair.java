package io.gomint.nativeloader;

import oshi.PlatformEnum;

import java.util.Objects;

public class SupportPair {

    private final PlatformEnum platform;
    private final String arch;

    public SupportPair(PlatformEnum platform, String arch) {
        this.platform = platform;
        this.arch = arch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupportPair that = (SupportPair) o;
        return platform == that.platform && Objects.equals(arch, that.arch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, arch);
    }

}
