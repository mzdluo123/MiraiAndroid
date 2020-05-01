package io.github.mzdluo123.mirai.android.utils;

import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class DexCompiler {
    private File tempDir, pluginDir;

    public DexCompiler(File fileDir) {
        tempDir = new File(fileDir.getAbsolutePath(), "temp");
        pluginDir = new File(fileDir.getAbsolutePath(), "plugins");
    }

    public File compile(File jarFile) throws CompilationFailedException, IOException {
        String outName = jarFile.getName().substring(0, jarFile.getName().length() - 4) +
                "-android.jar";
        File outFile = new File(tempDir, outName).getAbsoluteFile();
        if (!outFile.exists()) {
            outFile.createNewFile();
        }

        D8Command command = D8Command.builder()
                .addProgramFiles(jarFile.getAbsoluteFile().toPath())
                .setOutput(outFile.toPath(), OutputMode.DexIndexed)
                .build();
        D8.run(command);
        return outFile;
    }

    public void copyResourcesAndMove(File origin, File newFile) throws IOException {
        ZipFile originZip = new ZipFile(origin);
        ZipFile newZip = new ZipFile(newFile);
        ArrayList<File> resources = new ArrayList<>();
        originZip.getFileHeaders().forEach(i -> {
            try {
                originZip.extractFile(i, tempDir.getAbsolutePath());
            } catch (ZipException e) {
                e.printStackTrace();
            }
        });

        for (File file : tempDir.listFiles()) {
            if (file.isFile() && !file.getName().equals(newFile.getName())) {
                resources.add(file);
            }
        }
        newZip.addFiles(resources);
        newFile.renameTo(new File(pluginDir, newFile.getName()));
    }
}
