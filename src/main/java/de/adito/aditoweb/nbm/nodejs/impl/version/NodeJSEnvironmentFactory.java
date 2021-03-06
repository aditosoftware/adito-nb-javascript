package de.adito.aditoweb.nbm.nodejs.impl.version;

import com.google.common.base.Strings;
import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import de.adito.aditoweb.nbm.nodejs.impl.NodeJSExecutorImpl;
import lombok.ToString;
import org.jetbrains.annotations.*;
import org.openide.util.BaseUtilities;

import java.io.*;
import java.util.concurrent.TimeoutException;

/**
 * @author w.glanzer, 08.03.2021
 */
public class NodeJSEnvironmentFactory
{

  /**
   * Creates an environment from a nodejs binary
   *
   * @param pBinary Binary of the installation
   * @return the (valid) env
   */
  @Nullable
  public static INodeJSEnvironment create(@NotNull File pBinary)
  {
    _BinaryEnvironment version = new _BinaryEnvironment(pBinary);
    if (version.isValid())
      return version;
    return null;
  }

  private NodeJSEnvironmentFactory()
  {
  }

  /**
   * Version of nodeJS
   */
  @ToString(doNotUseGetters = true)
  private static class _BinaryEnvironment implements INodeJSEnvironment
  {
    private final File nodejsBinary;

    public _BinaryEnvironment(@NotNull File pNodejsBinary)
    {
      nodejsBinary = pNodejsBinary;
    }

    @NotNull
    @Override
    public File getPath()
    {
      return nodejsBinary;
    }

    @NotNull
    @Override
    public File resolveExecBase(@NotNull INodeJSExecBase pBase)
    {
      String extension = "";
      if (BaseUtilities.isWindows())
        extension = pBase.getWindowsExt();
      else if (BaseUtilities.isUnix())
        extension = pBase.getLinuxExt();
      else if (BaseUtilities.isMac())
        extension = pBase.getMacExt();

      File executable = new File(nodejsBinary.getParentFile(), pBase.getBasePath() + (Strings.isNullOrEmpty(extension) ? "" : "." + extension));
      if (executable.exists())
        return executable;

      // not found
      throw new IllegalStateException("Unable to determine absolute path of execution base (" + pBase.getBasePath() + ", " +
                                          nodejsBinary.getParentFile().getAbsolutePath() + ")");
    }

    @NotNull
    @Override
    public String getVersion()
    {
      _ensureValid();

      try
      {
        return _readVersion();
      }
      catch (Exception e)
      {
        throw new IllegalStateException("Failed to retrieve version from nodejs package (" + getPath() + ")", e);
      }
    }

    /**
     * @return true, if this version is valid and can be used
     */
    @Override
    public boolean isValid()
    {
      try
      {
        return !_readVersion().isEmpty();
      }
      catch (Exception e)
      {
        return false;
      }
    }

    /**
     * Ensures, that this version is valid
     */
    private void _ensureValid()
    {
      if (!isValid())
        throw new IllegalArgumentException("NodeJSVersion is not valid (" + getPath() + ")");
    }

    /**
     * Extracts the version from this package
     *
     * @return the version
     */
    @NotNull
    private String _readVersion() throws IOException, InterruptedException, TimeoutException
    {
      String result = NodeJSExecutorImpl.getInternalUnboundExecutor(new File(".")).executeSync(new INodeJSEnvironment()
      {
        @NotNull
        @Override
        public File getPath()
        {
          return _BinaryEnvironment.this.getPath();
        }

        @NotNull
        @Override
        public File resolveExecBase(@NotNull INodeJSExecBase pBase)
        {
          return _BinaryEnvironment.this.getPath();
        }

        @NotNull
        @Override
        public String getVersion()
        {
          return "invalid";
        }

        @Override
        public boolean isValid()
        {
          return true;
        }
      }, INodeJSExecBase.node(), 2000, "--version");

      // only read last line, because this line contains the version
      String[] lines = result.split("\n");
      if (lines.length == 0)
        return "";
      return lines[lines.length - 1];
    }
  }

}
