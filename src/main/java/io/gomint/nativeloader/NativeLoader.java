package io.gomint.nativeloader;

import oshi.PlatformEnum;
import oshi.SystemInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author geNAZt
 * @version 1.0
 */
public class NativeLoader {

    public static NativeLoader create() {
        return new NativeLoader();
    }

    private static final EnumMap<PlatformEnum, String> LIBRARY_SUFFIXES = new EnumMap<>(PlatformEnum.class) {{
        put(PlatformEnum.LINUX, "so");
        put(PlatformEnum.MACOS, "dylib");
        put(PlatformEnum.WINDOWS, "dll");
    }};

    private final Set<SupportPair> supportMatrix = new HashSet<>();

    private NativeLoader() {

    }

    public NativeLoader supports(PlatformEnum platform, String arch) {
        this.supportMatrix.add(new SupportPair(platform, arch));
        return this;
    }

    public boolean load(String library, ClassLoader classLoader) {
        // Check if this setup is supported
        if (!this.supportMatrix.contains(new SupportPair(SystemInfo.getCurrentPlatformEnum(),
                System.getProperty("os.arch")))) {
            return false;
        }

        // Construct the full library name
        String ending = "." + LIBRARY_SUFFIXES.get(SystemInfo.getCurrentPlatformEnum());
        String fullName = SystemInfo.getCurrentPlatformEnum().name().toLowerCase() + "_" + library + "_" +
                System.getProperty("os.arch");

        // Get the input from either the jar or filesystem and copy it to a temp then load it
        try ( InputStream soFile = getInput( fullName + ending, classLoader ) ) {
            if ( soFile == null ) {
                return false;
            }

            // Else we will create and copy it to a temp file
            File temp = File.createTempFile( fullName, ending );

            // Don't leave cruft on filesystem
            temp.deleteOnExit();

            // Copy over
            try ( OutputStream outputStream = new FileOutputStream( temp ) ) {
                copy( soFile, outputStream );
            }

            // And load the native extension
            System.load( temp.getPath() );
            return true;
        } catch ( IOException ex ) {
            // Can't write to tmp?
        } catch ( UnsatisfiedLinkError ex ) {
            System.out.println( "Could not load native library: " + ex.getMessage() );
        }

        return false;
    }

    private static void copy(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[8192];

        while(true) {
            int r = from.read(buf);
            if (r == -1) {
                return;
            }

            to.write(buf, 0, r);
        }
    }

    private static InputStream getInput( String name, ClassLoader classLoader ) {
        InputStream in = classLoader.getResourceAsStream( name );
        if ( in == null ) {
            try {
                in = new FileInputStream( "./src/main/resources/" + name );
            } catch ( FileNotFoundException e ) {
                // Ignored -.-
            }
        }

        return in;
    }

}
