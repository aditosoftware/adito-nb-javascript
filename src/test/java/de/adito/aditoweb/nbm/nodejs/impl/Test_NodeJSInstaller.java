package de.adito.aditoweb.nbm.nodejs.impl;

import de.adito.aditoweb.nbm.nodejs.impl.options.downloader.INodeJSDownloader;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.netbeans.api.progress.*;

import java.io.*;

/**
 * @author w.glanzer, 11.05.2021
 */
class Test_NodeJSInstaller
{

  private static File target;
  private NodeJSInstaller installer;

  @BeforeAll
  static void beforeAll() throws IOException
  {
    target = new File("./target/bundled_nodejs");

    // clean previously used directory
    FileUtils.deleteDirectory(target);

    // mock
    Mockito.mockStatic(BundledNodeJS.class)
        .when(BundledNodeJS::getInstance)
        .thenReturn(new BundledNodeJS(() -> target));
  }

  @BeforeEach
  void setUp()
  {
    installer = new NodeJSInstaller();
  }

  @Test
  void test_downloadBundledNodeJS() throws Exception
  {
    installer.downloadBundledNodeJS(ProgressHandleFactory.createHandle("", null, null));

    Assertions.assertTrue(target.exists());
    Assertions.assertTrue(target.isDirectory());
    Assertions.assertNotNull(INodeJSDownloader.getInstance().findNodeExecutableInInstallation(target));
  }

  @Test
  void test_downloadOrUpdateBundledTypeScript() throws Exception
  {
    ProgressHandle handle = ProgressHandleFactory.createHandle("", null, null);
    installer.downloadBundledNodeJS(handle);
    installer.downloadOrUpdateBundledTypeScript(handle);

    File module = new File(target, "node_modules/" + IBundledPackages.TYPESCRIPT_LANGUAGE_SERVER);
    Assertions.assertTrue(module.exists());
    Assertions.assertTrue(module.isDirectory());
  }

}
