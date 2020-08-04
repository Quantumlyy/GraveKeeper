package com.quantumlytangled.gravekeeper.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import com.quantumlytangled.gravekeeper.core.Registration;

public class NBTFile {

  public static void write(@Nonnull final String fileName, @Nonnull final NBTTagCompound tagCompound) {
    try {
      final File file = new File(fileName);
      if (!file.exists()) {
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
      }

      final FileOutputStream fileoutputstream = new FileOutputStream(file);

      CompressedStreamTools.writeCompressed(tagCompound, fileoutputstream);

      fileoutputstream.close();
    } catch (final Exception exception) {
      exception.printStackTrace(Registration.printStreamError);
    }
  }

  @Nullable
  public static NBTTagCompound read(@Nonnull final String fileName) {
    try {
      final File file = new File(fileName);
      if (!file.exists()) {
        return null;
      }

      final FileInputStream fileinputstream = new FileInputStream(file);
      final NBTTagCompound tagCompound = CompressedStreamTools.readCompressed(fileinputstream);

      fileinputstream.close();

      return tagCompound;
    } catch (final Exception exception) {
      exception.printStackTrace(Registration.printStreamError);
    }

    return null;
  }

}
