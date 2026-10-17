{ pkgs, ... }:

# The toolchains, pinned by devenv.lock:
#   devenv shell                  a shell with them
#   devenv test                   the Kotlin tests (mvn test)
#
# Kotlin itself (2.4.21) and its libraries are pinned by pom.xml, which Maven
# reads; this pins the JDK and Maven that run it.
let
  ocamlPackages = pkgs.ocaml-ng.ocamlPackages_5_5;
in
{
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk25; # the current LTS JDK
    maven.enable = true;
    lsp.enable = false;
  };

  languages.ocaml = {
    enable = true; # the compiler, dune, findlib, merlin, utop, odoc, ocamlformat
    packages = ocamlPackages;
    lsp.package = ocamlPackages.ocaml-lsp;
  };

  # the libraries the OCaml build uses (tools/agenda-ocaml)
  packages = [
    ocamlPackages.menhir
    ocamlPackages.zarith
  ];

  # devenv test runs the task devenv:enterTest, and this before it
  tasks."quadruplet:kotlin-test" = {
    exec = "mvn -B test";
    before = [ "devenv:enterTest" ];
  };
}
