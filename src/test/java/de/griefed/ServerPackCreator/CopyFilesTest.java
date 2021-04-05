package de.griefed.ServerPackCreator;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class CopyFilesTest {
    @Mock
    Logger appLogger;
    @InjectMocks
    CopyFiles copyFiles;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    static void testCleanupEnvironment() throws IOException {
        String modpackDir = "./src/test/resources/cleanup_tests";
        Files.copy(Paths.get("./src/test/resources/testresources/server_pack.zip"), Paths.get("./src/test/resources/cleanup_tests/server_pack.zip"), REPLACE_EXISTING);
        Files.createDirectories(Paths.get(String.format("%s/server_pack",modpackDir)));
        CopyFiles.cleanupEnvironment(modpackDir);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testCopyStartScriptsFabric() throws IOException {
        String modpackDir = "./src/test/resources/fabric_tests";
        String modLoader = "Fabric";
        FilesSetup.filesSetup();
        CopyFiles.copyStartScripts(modpackDir, modLoader, true);
        Assertions.assertTrue(new File(String.format("%s/server_pack/start-fabric.bat",modpackDir)).exists());
        Assertions.assertTrue(new File(String.format("%s/server_pack/start-fabric.sh",modpackDir)).exists());
        new File(String.format("%s/server_pack/start-fabric.bat",modpackDir)).delete();
        new File(String.format("%s/server_pack/start-fabric.sh",modpackDir)).delete();
        String delete = "./server_files";
        if (new File(delete).isDirectory()) {
            Path pathToBeDeleted = Paths.get(delete);
            Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        new File("./serverpackcreator.conf").delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testCopyStartScriptsForge() throws IOException {
        String modpackDir = "./src/test/resources/forge_tests";
        String modLoader = "Forge";
        FilesSetup.filesSetup();
        CopyFiles.copyStartScripts(modpackDir, modLoader, true);
        Assertions.assertTrue(new File(String.format("%s/server_pack/start-forge.bat",modpackDir)).exists());
        Assertions.assertTrue(new File(String.format("%s/server_pack/start-forge.sh",modpackDir)).exists());
        new File(String.format("%s/server_pack/start-forge.bat",modpackDir)).delete();
        new File(String.format("%s/server_pack/start-forge.sh",modpackDir)).delete();
        String delete = "./server_files";
        if (new File(delete).isDirectory()) {
            Path pathToBeDeleted = Paths.get(delete);
            Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        new File("./serverpackcreator.conf").delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testCopyFiles() throws IOException {
        String modpackDir = "./src/test/resources/forge_tests";
        List<String> copyDirs = Arrays.asList("config","mods","scripts","seeds","defaultconfigs");
        CopyFiles.copyFiles(modpackDir, copyDirs);
        Assertions.assertTrue(new File(String.format("%s/server_pack/config",modpackDir)).isDirectory());
        Assertions.assertTrue(new File(String.format("%s/server_pack/mods",modpackDir)).isDirectory());
        Assertions.assertTrue(new File(String.format("%s/server_pack/scripts",modpackDir)).isDirectory());
        Assertions.assertTrue(new File(String.format("%s/server_pack/seeds",modpackDir)).isDirectory());
        Assertions.assertTrue(new File(String.format("%s/server_pack/defaultconfigs",modpackDir)).isDirectory());
        Assertions.assertTrue(new File(String.format("%s/server_pack/config/testfile.txt",modpackDir)).exists());
        Assertions.assertTrue(new File(String.format("%s/server_pack/defaultconfigs/testfile.txt",modpackDir)).exists());
        Assertions.assertTrue(new File(String.format("%s/server_pack/mods/testmod.jar",modpackDir)).exists());
        Assertions.assertTrue(new File(String.format("%s/server_pack/scripts/testscript.zs",modpackDir)).exists());
        Assertions.assertTrue(new File(String.format("%s/server_pack/seeds/testjson.json",modpackDir)).exists());
        for (String s : copyDirs) {
            String deleteMe = (String.format("%s/server_pack/%s",modpackDir,s));
            if (new File(deleteMe).isDirectory()) {
                    Path pathToBeDeleted = Paths.get(deleteMe);
                    Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testCopyIcon() throws IOException {
        FilesSetup.filesSetup();
        String modpackDir = "./src/test/resources/forge_tests";
        CopyFiles.copyIcon(modpackDir);
        Assertions.assertTrue(new File(String.format("%s/server_pack/server-icon.png",modpackDir)).exists());
        new File(String.format("%s/server_pack/server-icon.png",modpackDir)).delete();
        String delete = "./server_files";
        if (new File(delete).isDirectory()) {
            Path pathToBeDeleted = Paths.get(delete);
            Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        new File("./serverpackcreator.conf").delete();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void testCopyProperties() throws IOException {
        FilesSetup.filesSetup();
        String modpackDir = "./src/test/resources/forge_tests";
        CopyFiles.copyProperties(modpackDir);
        Assertions.assertTrue(new File(String.format("%s/server_pack/server.properties",modpackDir)).exists());
        new File(String.format("%s/server_pack/server.properties",modpackDir)).delete();
        String delete = "./server_files";
        if (new File(delete).isDirectory()) {
            Path pathToBeDeleted = Paths.get(delete);
            Files.walk(pathToBeDeleted).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        new File("./serverpackcreator.conf").delete();
    }
}