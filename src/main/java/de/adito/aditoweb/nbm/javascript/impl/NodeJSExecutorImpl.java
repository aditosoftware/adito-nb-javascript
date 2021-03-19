package de.adito.aditoweb.nbm.javascript.impl;

import de.adito.aditoweb.nbm.nbide.nbaditointerface.javascript.node.*;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.openide.util.lookup.ServiceProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author w.glanzer, 08.03.2021
 */
@ServiceProvider(service = INodeJSExecutor.class)
public class NodeJSExecutorImpl implements INodeJSExecutor
{

  @NotNull
  @Override
  public String executeSync(@NotNull INodeJSEnvironment pEnv, @NotNull INodeJSExecBase pBase, @NotNull String pCommand, long pTimeout)
      throws IOException, InterruptedException
  {
    // Invalid Environment
    if (!pEnv.isValid())
      throw new IOException("Failed to execute command on nodejs, because version is invalid (" + pEnv + ")");

    // Prepare Process
    Process process = new ProcessBuilder(List.of(pEnv.resolveExecBase(pBase).getAbsolutePath(), pCommand)).start();

    // Execute blocking
    if (pTimeout > -1)
      process.waitFor(pTimeout, TimeUnit.MILLISECONDS);
    else
      process.waitFor();

    // Copy to String
    StringWriter writer = new StringWriter();
    IOUtils.copy(process.getInputStream(), writer, StandardCharsets.UTF_8);

    // trim trailing linebreak
    return writer.toString().trim();
  }

}
