package de.griefed.serverPackCreator;

import net.fabricmc.installer.util.LauncherMeta;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Server {
    private static final Logger appLogger = LogManager.getLogger("Server");
    private static final Logger errorLogger = LogManager.getLogger("ServerError");

    // Delete clientside mods from serverpack
    public static void deleteClientMods(String modpackDir, List<String> clientMods) throws IOException {
        appLogger.info("Deleting client-side mods from serverpack: ");
        File serverMods = new File(modpackDir + "/server_pack/mods");
            for (File f : Objects.requireNonNull(serverMods.listFiles())) {
                for (int i = 0; i < clientMods.toArray().length; i++) {
                    if (f.getName().startsWith(clientMods.get(i))) {
                        boolean isDeleted = f.delete();
                        if (isDeleted) {
                            appLogger.info("    " + f.getName());
                        } else {
                            errorLogger.error("Could not delete: " + f.getName());
                        }
                    }
                }
            }
    }
    // Create zip archive of serverpack.
    public static void zipBuilder(String modpackDir, String modLoader, String minecraftVersion) {
        // With help from https://stackoverflow.com/questions/1091788/how-to-create-a-zip-file-in-java
        appLogger.info("Creating zip archive of serverpack...");
        appLogger.info("NOTE: The minecraft_server.jar will not be included in the zip-archive.");
        appLogger.info("Mojang strictly prohibits the distribution of their software through third parties.");
        appLogger.info("Tell your users to execute the download scripts to get the Minecraft server jar.");
        final Path sourceDir = Paths.get(modpackDir + "/server_pack");
        try {
            if (modLoader.equals("Fabric")) {
                // Generate download script if modloader is Fabric, shell
                String downloadMinecraftServer = (new URL(LauncherMeta.getLauncherMeta().getVersion(minecraftVersion).getVersionMeta().downloads.get("server").url)).toString();
                String shFabric = "#!/bin/bash\n#Download the Minecraft_server.jar for your modpack\n\nwget -O server.jar " + downloadMinecraftServer;
                Path pathSh = Paths.get(modpackDir + "/server_pack/download_minecraft_server_fabric.sh");
                byte[] strToBytesSh = shFabric.getBytes();
                Files.write(pathSh, strToBytesSh);
                String readSh = Files.readAllLines(pathSh).get(0);
                // Generate download script if modloader is Fabric, batch
                String batFabric = "powershell -Command \"(New-Object Net.WebClient).DownloadFile('" + downloadMinecraftServer + "', 'server.jar')\"";
                Path pathBat = Paths.get(modpackDir + "/server_pack/download_minecraft_server_fabric.bat");
                byte[] strToBytesBat = batFabric.getBytes();
                Files.write(pathBat, strToBytesBat);
                String readBat = Files.readAllLines(pathBat).get(0);
            } else if (modLoader.equals("Forge")) {
                // Generate download script if modloader is Forge, shell
                String downloadMinecraftServer = (new URL(LauncherMeta.getLauncherMeta().getVersion(minecraftVersion).getVersionMeta().downloads.get("server").url)).toString();
                String shForge = "#!/bin/bash\n# Download the Minecraft_server.jar for your modpack\n\nwget -O minecraft_server.1.16.5.jar " + downloadMinecraftServer;
                Path pathSh = Paths.get(modpackDir + "/server_pack/download_minecraft_server_forge.sh");
                byte[] strToBytesSh = shForge.getBytes();
                Files.write(pathSh, strToBytesSh);
                String readSh = Files.readAllLines(pathSh).get(0);
                // Generate download script if modloader is Forge, batch
                String batForge = "powershell -Command \"(New-Object Net.WebClient).DownloadFile('" + downloadMinecraftServer + "', 'minecraft_server.1.16.5.jar')\"";
                Path pathBat = Paths.get(modpackDir + "/server_pack/download_minecraft_server_forge.bat");
                byte[] strToBytesBat = batForge.getBytes();
                Files.write(pathBat, strToBytesBat);
                String readBat = Files.readAllLines(pathBat).get(0);
            }
        } catch (IOException ex) {
            errorLogger.error("Error during download-script generation for Minceraft server.", ex);
        }
        String zipFileName = sourceDir.toString().concat(".zip");
        try {
            final ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException ex) {
                        errorLogger.error("There was an error during zip creation", ex);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException ex) {
            errorLogger.error("There was an error during zip creation", ex);
        }
        // With help from https://stackoverflow.com/questions/5244963/delete-files-from-a-zip-archive-without-decompressing-in-java-or-maybe-python
        // and https://bugs.openjdk.java.net/browse/JDK-8186227
        if (modLoader.equals("Forge")) {
            Map<String, String> zip_properties = new HashMap<>();
            zip_properties.put("create", "false");
            zip_properties.put("encoding", "UTF-8");
            Path serverpackZip = Paths.get(modpackDir + "/server_pack.zip");
            URI zipUri = URI.create("jar:" + serverpackZip.toUri());
            try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, zip_properties)) {
                Path pathInZipfile = zipfs.getPath("minecraft_server.1.16.5.jar");
                appLogger.info("Deleting minecraft_server.jar from server_pack.zip.");
                Files.delete(pathInZipfile);
                appLogger.info("File successfully deleted");
            } catch (IOException ex) {
                errorLogger.error("Error deleting minecraft-server.jar from archive.", ex);
            }
        } else if (modLoader.equals("Fabric")) {
            Map<String, String> zip_properties = new HashMap<>();
            zip_properties.put("create", "false");
            zip_properties.put("encoding", "UTF-8");
            Path serverpackZip = Paths.get(modpackDir + "/server_pack.zip");
            URI zipUri = URI.create("jar:" + serverpackZip.toUri());
            try (FileSystem zipfs = FileSystems.newFileSystem(zipUri, zip_properties)) {
                Path pathInZipfile = zipfs.getPath("server.jar");
                appLogger.info("Deleting minecraft_server.jar from server_pack.zip.");
                Files.delete(pathInZipfile);
                appLogger.info("File successfully deleted");
            } catch (IOException ex) {
                errorLogger.error("Error deleting minecraft-server.jar from archive.", ex);
            }
        } else {
            errorLogger.error("Specified invalid modloader: " + modLoader);
        }
    }

    public static void installServer(String modLoader, String modpackDir, String minecraftVersion, String modLoaderVersion, String javaPath) {
        //File serverDir = new File(modpackDir + "/server_pack");
        if (modLoader.equals("Fabric")) {
            try {
                appLogger.info("################################################################");
                appLogger.info("#                Starting Fabric installation                  #");
                appLogger.info("################################################################");
                // Download fabric-installer.xml
                URL downloadFabricXml = new URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/maven-metadata.xml");
                ReadableByteChannel downloadFabricXmlReadableByteChannel = Channels.newChannel(downloadFabricXml.openStream());
                FileOutputStream downloadFabricXmlFileOutputStream = new FileOutputStream(modpackDir + "/server_pack/fabric-installer.xml");
                FileChannel downloadFabricXmlFileChannel = downloadFabricXmlFileOutputStream.getChannel();
                downloadFabricXmlFileOutputStream.getChannel().transferFrom(downloadFabricXmlReadableByteChannel, 0, Long.MAX_VALUE);
                downloadFabricXmlFileOutputStream.flush();
                downloadFabricXmlFileOutputStream.close();
                // Parse fabric-installer.xml to get newest release version
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                Document fabricXml = builder.parse(new File(modpackDir + "/server_pack/fabric-installer.xml"));
                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath xpath = xPathFactory.newXPath();
                String fabricLauncherVersion = (String) xpath.evaluate("/metadata/versioning/release", fabricXml,  XPathConstants.STRING);
                // Download newest release version of fabric-installer.jar
                URL downloadFabric = new URL("https://maven.fabricmc.net/net/fabricmc/fabric-installer/"+ fabricLauncherVersion + "/fabric-installer-" + fabricLauncherVersion + ".jar");
                ReadableByteChannel readableByteChannel = Channels.newChannel(downloadFabric.openStream());
                FileOutputStream downloadFabricFileOutputStream = new FileOutputStream(modpackDir + "/server_pack/fabric-installer.jar");
                FileChannel downloadFabricFileChannel = downloadFabricFileOutputStream.getChannel();
                downloadFabricFileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                downloadFabricFileOutputStream.flush();
                downloadFabricFileOutputStream.close();
                // Install fabric server
                File fabricXML = new File(modpackDir + "/server_pack/fabric-installer.xml");
                File fabricInstaller = new File(modpackDir + "/server_pack/fabric-installer.jar");
                if (fabricInstaller.exists()) {
                    ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-jar", "fabric-installer.jar", "server", "-mcversion " + minecraftVersion, "-loader " + modLoaderVersion, "-downloadMinecraft").directory(new File(modpackDir + "/server_pack"));
                    processBuilder.redirectErrorStream(true);
                    Process p = processBuilder.start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while (true) {
                        line = r.readLine();
                        if (line == null) { break; }
                        appLogger.info(line);
                    }
                }
                // Delete no longer needed XML and installer files
                boolean isXmlDeleted = fabricXML.delete();
                if (isXmlDeleted) {
                    appLogger.info("Deleted " + fabricXML.getName());
                } else {
                    errorLogger.error("Could not delete " + fabricXML.getName());
                }
                boolean isInstallerDeleted = fabricInstaller.delete();
                if (isInstallerDeleted) {
                    appLogger.info("Deleted " + fabricInstaller.getName());
                } else {
                    errorLogger.error("Could not delete " + fabricInstaller.getName());
                }
                appLogger.info("################################################################");
                appLogger.info("#         Fabric installation complete. Returning to SPC.      #");
                appLogger.info("################################################################");
            } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException ex) {
                errorLogger.error("An error occurred during Fabric installation.", ex);
            }
        } else if (modLoader.equals("Forge")) {
            appLogger.info("Forge installation not yet implemented.");
            try {
                appLogger.info("################################################################");
                appLogger.info("#                Starting Forge installation                   #");
                appLogger.info("################################################################");
                // Download Forge installer as specified in config
                appLogger.info("Downloading https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + minecraftVersion + "-" + modLoaderVersion + "/forge-" + minecraftVersion + "-" + modLoaderVersion + "-installer.jar");
                URL downloadForge = new URL("https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + minecraftVersion + "-" + modLoaderVersion + "/forge-" + minecraftVersion + "-" + modLoaderVersion + "-installer.jar");
                ReadableByteChannel readableByteChannel = Channels.newChannel(downloadForge.openStream());
                FileOutputStream downloadForgeFileOutputStream = new FileOutputStream(modpackDir + "/server_pack/forge-installer.jar");
                FileChannel downloadForgeFileChannel = downloadForgeFileOutputStream.getChannel();
                downloadForgeFileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                downloadForgeFileOutputStream.flush();
                downloadForgeFileOutputStream.close();
                // Install Forge server
                File forgeInstaller = new File(modpackDir + "/server_pack/forge-installer.jar");
                if (forgeInstaller.exists()) {
                    ProcessBuilder processBuilder = new ProcessBuilder(javaPath, "-jar", "forge-installer.jar", "--installServer").directory(new File(modpackDir + "/server_pack"));
                    processBuilder.redirectErrorStream(true);
                    Process p = processBuilder.start();
                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while (true) {
                        line = r.readLine();
                        if (line == null) { break; }
                        appLogger.info(line);
                    }
                }
                // Rename Forge server jar to work with start script
                Files.copy(Paths.get(modpackDir + "/server_pack/forge-" + minecraftVersion + "-" + modLoaderVersion + ".jar"), Paths.get(modpackDir + "/server_pack/forge.jar"), REPLACE_EXISTING);
                boolean isOldJarDeleted = (new File(modpackDir + "/server_pack/forge-" + minecraftVersion + "-" + modLoaderVersion + ".jar")).delete();
                if ((isOldJarDeleted) && (new File(modpackDir + "/server_pack/forge.jar").exists())) {
                    appLogger.info("Renamed forge.jar and deleted old one.");
                } else {
                    errorLogger.error("There was an error during renaming or deletion of the forge server jar.");
                }
                // Delete no longer needed Forge installer
                boolean isInstallerDeleted = forgeInstaller.delete();
                if (isInstallerDeleted) {
                    appLogger.info("Deleted " + forgeInstaller.getName());
                } else {
                    errorLogger.error("Could not delete " + forgeInstaller.getName());
                }
                appLogger.info("################################################################");
                appLogger.info("#        Forge installation complete. Returning to SPC.        #");
                appLogger.info("################################################################");
            } catch (IOException ex) {
                errorLogger.error("An error occurred during Forge installation.", ex);
            }

        } else {
            errorLogger.error("Specified invalid modloader: " + modLoader);
        }
    }
}
