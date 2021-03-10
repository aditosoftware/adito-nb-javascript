package de.adito.aditoweb.nbm.javascript.impl.options.downloader;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;

/**
 * Automatically download NodeJS versions
 *
 * @author w.glanzer, 10.03.2021
 */
public interface INodeJSDownloader
{

  @NotNull
  static INodeJSDownloader getInstance()
  {
    return NodeJSDownloaderImpl.INSTANCE;
  }

  /**
   * Retrieves all available versions
   *
   * @return versions, sorted descending (in correct "versioning" order)
   */
  @NotNull
  List<String> getAvailableVersions() throws IOException;

  /**
   * Downloads a nodejs version to pTarget.
   *
   * @param pVersion Version to download (has to be a version from getAvailableVersions())
   * @param pTarget  Target Folder
   * @return the binary nodejs target
   */
  @NotNull
  File downloadVersion(@NotNull String pVersion, @NotNull File pTarget) throws IOException;

}