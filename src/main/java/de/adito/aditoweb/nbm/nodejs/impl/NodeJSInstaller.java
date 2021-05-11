package de.adito.aditoweb.nbm.nodejs.impl;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import de.adito.aditoweb.nbm.nodejs.impl.ls.TypeScriptLanguageServerProvider;
import de.adito.aditoweb.nbm.nodejs.impl.options.NodeJSOptions;
import de.adito.aditoweb.nbm.nodejs.impl.options.downloader.INodeJSDownloader;
import org.jetbrains.annotations.NotNull;
import org.netbeans.api.progress.*;
import org.openide.util.*;
import org.openide.windows.OnShowing;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

/**
 * @author w.glanzer, 10.05.2021
 */
@OnShowing // to show progress, must not be too early..
public class NodeJSInstaller implements Runnable
{

  private static final String _INSTALLER_INTEGRITYCHECK_FILE = ".installer_integrity";
  private static final Logger _LOGGER = Logger.getLogger(NodeJSInstaller.class.getName());

  @Override
  public void run()
  {
    _downloadLibraries();
  }

  /**
   * Downloads all necessary libraries asynchronously
   */
  private void _downloadLibraries()
  {
    // only show if ready, so the progresshandle will show up
    SwingUtilities.invokeLater(() -> RequestProcessor.getDefault().submit(() -> {
      try (ProgressHandle handle = ProgressHandleFactory.createSystemHandle(NbBundle.getMessage(NodeJSInstaller.class, "LBL_Progress_DownloadLibraries"), null))
      {
        // handle progress
        handle.start();
        handle.switchToIndeterminate();

        // download and / or install
        downloadBundledNodeJS(handle);
        downloadOrUpdateBundledTypeScript(handle);
      }
      catch (Exception e)
      {
        _LOGGER.log(Level.WARNING, "", e);
      }
    }));
  }

  /**
   * Downloads the latest nodejs version, if no version is specified
   *
   * @param pHandle Progress
   */
  protected void downloadBundledNodeJS(@NotNull ProgressHandle pHandle) throws IOException
  {
    // do not download or update anything, if the nodejs container folder already exists and integrity is ok
    File target = BundledNodeJS.getInstance().getBundledNodeJSContainer();
    String version = BundledNodeJS.getInstance().getBundledVersion();
    if (_isIntegrityOK(target, version))
      return;

    // download
    pHandle.progress(NbBundle.getMessage(NodeJSInstaller.class, "LBL_Progress_Download_Execute", version));
    INodeJSDownloader downloader = INodeJSDownloader.getInstance();
    File binFile = downloader.downloadVersion(version, target.getParentFile());
    File nodeVersionContainer = downloader.findInstallationFromNodeExecutable(binFile);

    // rename to target
    if (nodeVersionContainer != null)
      if (nodeVersionContainer.renameTo(target))
        binFile = downloader.findNodeExecutableInInstallation(target);

    // update integrity
    _updateIntegrity(target, version);

    // update options to use new binary
    if (binFile != null && !NodeJSOptions.getInstance().isPathValid())
      NodeJSOptions.update(NodeJSOptions.getInstance().toBuilder()
                               .path(binFile.getAbsolutePath())
                               .build());
  }

  /**
   * Downloads the latest typescript-language-server
   *
   * @param pHandle Progress
   */
  protected void downloadOrUpdateBundledTypeScript(@NotNull ProgressHandle pHandle) throws IOException, InterruptedException, TimeoutException
  {
    File target = BundledNodeJS.getInstance().getBundledNodeJSContainer();
    if (!target.exists())
      return;

    // Create node_modules folder to install the typescript module in the correct directory
    //noinspection ResultOfMethodCallIgnored
    new File(target, "node_modules").mkdir();

    // prepare
    INodeJSExecutor executor = BundledNodeJS.getInstance().getBundledExecutor();
    INodeJSEnvironment environment = BundledNodeJS.getInstance().getBundledEnvironment();
    String module = TypeScriptLanguageServerProvider.NEEDED_MODULE;

    // Install
    pHandle.progress(NbBundle.getMessage(NodeJSInstaller.class, "LBL_Progress_Download_TypeScript"));
    executor.executeSync(environment, INodeJSExecBase.packageManager(), -1, "install", "--prefix", target.getAbsolutePath(), module);

    // Update
    pHandle.progress(NbBundle.getMessage(NodeJSInstaller.class, "LBL_Progress_Update_TypeScript"));
    executor.executeSync(environment, INodeJSExecBase.packageManager(), -1, "update", "--prefix", target.getAbsolutePath(), module);
  }

  /**
   * Determines, if the integrity of pTarget can be checked and the check is OK
   *
   * @param pTarget  target to verify
   * @param pVersion version to check
   * @return true, if OK
   */
  private boolean _isIntegrityOK(@NotNull File pTarget, @NotNull String pVersion)
  {
    File file = new File(pTarget, _INSTALLER_INTEGRITYCHECK_FILE);
    if (!file.exists() || !file.isFile() || !file.canRead())
      return false;

    try
    {
      String version = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).get(0);
      return pVersion.equals(version);
    }
    catch (Exception e)
    {
      return false;
    }
  }

  /**
   * Updates the integrity of the given target to pVersion
   *
   * @param pTarget  target to update
   * @param pVersion version to set
   */
  private void _updateIntegrity(@NotNull File pTarget, @NotNull String pVersion)
  {
    File file = new File(pTarget, _INSTALLER_INTEGRITYCHECK_FILE);

    if (file.exists())
      //noinspection ResultOfMethodCallIgnored
      file.delete();
    else
      //noinspection ResultOfMethodCallIgnored
      file.getParentFile().mkdirs();

    try
    {
      Files.write(file.toPath(), pVersion.getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

}
