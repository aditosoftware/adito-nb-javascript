package de.adito.aditoweb.nbm.nodejs.impl;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Contains all packages that will be bundled within the bundled nodejs
 *
 * @author w.glanzer, 02.06.2021
 */
public interface IBundledPackages
{

  String TYPESCRIPT = "typescript";
  String TYPESCRIPT_LANGUAGE_SERVER = "typescript-language-server";
  String NPM = "npm";

  /**
   * Returns all packages that are available at runtime within bundled nodejs
   *
   * @return the packages
   */
  @NotNull
  static List<String> getPreinstalledPackages()
  {
    return List.of(NPM, TYPESCRIPT, TYPESCRIPT_LANGUAGE_SERVER);
  }

}
