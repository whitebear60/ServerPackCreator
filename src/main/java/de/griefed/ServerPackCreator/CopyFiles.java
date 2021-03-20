package de.griefed.ServerPackCreator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class CopyFiles {
    private static final Logger appLogger = LogManager.getLogger("ApplicationLogger");
    /** Deletes files from previous runs of ServerPackCreator.
     * @param modpackDir String. The directory in where to check for files from previous runs.
     */
    static void cleanupEnvironment(String modpackDir) {
        if (new File(modpackDir + "/server_pack").exists()) {
            appLogger.info("Found old server_pack. Cleaning up...");
            Path serverPack = Paths.get(modpackDir + "/server_pack");
            try {
                Files.walkFileTree(serverPack,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult postVisitDirectory(
                                    Path dir, IOException exc) throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                            @Override
                            public FileVisitResult visitFile(
                                    Path file, BasicFileAttributes attrs)
                                    throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException ex) {
                appLogger.error("Error deleting file from " + modpackDir + "/server_pack");
            } finally {
                appLogger.info("Cleanup of previous server_pack completed.");
            }
        }
        if (new File(modpackDir + "/server_pack.zip").exists()) {
            appLogger.info("Found old server_pack.zip. Cleaning up...");
            boolean isZipDeleted = new File(modpackDir + "/server_pack.zip").delete();
            if (isZipDeleted) {
                appLogger.info("Old server_pack.zip deleted.");
            } else {
                appLogger.error("Error deleting old zip archive.");
            }
        }
    }
    /** Copies start scripts for Forge modloader into the server_pack folder.
     * @param modpackDir String. Files will be copied into subfolder server_pack. Checks for valid modpackDir are in ConfigCheck.
     * @param modLoader String. Determines whether start scripts for Forge or Fabric are copied to modpackDir. Checks for valid modLoader are in ConfigCheck.
     * @param includeStartScripts Boolean. Whether to include start scripts in server_pack. Boolean.
     */
    static void copyStartScripts(String modpackDir, String modLoader, boolean includeStartScripts) {
        if (modLoader.equalsIgnoreCase("Forge") && includeStartScripts) {
            appLogger.info("Copying Forge start scripts...");
            try {
                Files.copy(Paths.get("./server_files/" + Reference.forgeWindowsFile), Paths.get(modpackDir + "/server_pack/" + Reference.forgeWindowsFile), REPLACE_EXISTING);
                Files.copy(Paths.get("./server_files/" + Reference.forgeLinuxFile), Paths.get(modpackDir + "/server_pack/" + Reference.forgeLinuxFile), REPLACE_EXISTING);
            } catch (IOException ex) {
                appLogger.error("An error occurred while copying files: ", ex);
            }
        } else if (modLoader.equalsIgnoreCase("Fabric") && includeStartScripts) {
            appLogger.info("Copying Fabric start scripts...");
            try {
                Files.copy(Paths.get("./server_files/" + Reference.fabricWindowsFile), Paths.get(modpackDir + "/server_pack/" + Reference.fabricWindowsFile), REPLACE_EXISTING);
                Files.copy(Paths.get("./server_files/" + Reference.fabricLinuxFile), Paths.get(modpackDir + "/server_pack/" + Reference.fabricLinuxFile), REPLACE_EXISTING);
            } catch (IOException ex) {
                appLogger.error("An error occurred while copying files: ", ex);
            }
        } else {
            appLogger.info("Specified invalid modloader. Must be either Forge or Fabric.");
        }
    }
    /** Copies all specified folders and their files to the modpackDir.
     * @param modpackDir String. /server_pack. Directory where all directories listed in copyDirs will be copied into.
     * @param copyDirs String List. The folders and files within to copy.
     * @throws IOException Only print stacktrace if it does not start with java.nio.file.DirectoryNotEmptyException.
     */
    static void copyFiles(String modpackDir, List<String> copyDirs) throws IOException {
        String serverPath = modpackDir + "/server_pack";
        Files.createDirectories(Paths.get(serverPath));
        for (int i = 0; i<copyDirs.toArray().length; i++) {
            String clientDir = modpackDir + "/" + copyDirs.get(i);
            String serverDir = serverPath + "/" + copyDirs.get(i);
            appLogger.info("Setting up " + serverDir + " files.");
            if (copyDirs.get(i).startsWith("saves/")) {
                String savesDir = serverPath + "/" + copyDirs.get(i).substring(6);
                try {
                    Stream<Path> files = Files.walk(Paths.get(clientDir));
                    files.forEach(file -> {
                        try {
                            Files.copy(file, Paths.get(savesDir).resolve(Paths.get(clientDir).relativize(file)), REPLACE_EXISTING);
                            appLogger.debug("Copying: " + file.toAbsolutePath().toString());
                        } catch (IOException ex) {
                            if (!ex.toString().startsWith("java.nio.file.DirectoryNotEmptyException")) {
                                appLogger.error("An error occurred during copy operation.", ex);
                            }
                        }
                    });
                } catch (IOException ex) {
                    appLogger.error("An error occurred copying the specified world.", ex);
                }
            } else {
                try {
                    Stream<Path> files = Files.walk(Paths.get(clientDir));
                    files.forEach(file -> {
                        try {
                            Files.copy(file, Paths.get(serverDir).resolve(Paths.get(clientDir).relativize(file)), REPLACE_EXISTING);
                            appLogger.debug("Copying: " + file.toAbsolutePath().toString());
                        } catch (IOException ex) {
                            if (!ex.toString().startsWith("java.nio.file.DirectoryNotEmptyException")) {
                                appLogger.error("An error occurred copying files to the serverpack.", ex);
                            }
                        }
                    });
                    files.close();
                } catch (IOException ex) {
                    appLogger.error("An error occurred during the copy-procedure to the serverpack.", ex);
                }
            }
        }
    }
    /** Copies the server-icon.png into server_pack.
     * @param modpackDir String. /server_pack. Directory where the server-icon.png will be copied to.
     */
    static void copyIcon(String modpackDir) {
        appLogger.info("Copying server-icon.png...");
        try {
            Files.copy(Paths.get("./server_files/" + Reference.iconFile), Paths.get(modpackDir + "/server_pack/" + Reference.iconFile), REPLACE_EXISTING);
        } catch (IOException ex) {
            appLogger.error("An error occurred trying to copy the server icon.", ex);
        }
    }
    /** Copies the server.properties into server_pack.
     * @param modpackDir String. /server_pack. Directory where the server.properties. will be copied to.
     */
    static void copyProperties(String modpackDir) {
        appLogger.info("Copying server.properties...");
        try {
            Files.copy(Paths.get("./server_files/" + Reference.propertiesFile), Paths.get(modpackDir + "/server_pack/" + Reference.propertiesFile), REPLACE_EXISTING);
        } catch (IOException ex) {
            appLogger.error("An error occurred trying to copy the server.properties-file.", ex);
        }
    }
}