import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class jontainer {
  public static void main(String[] args) {
    System.out.println("args: " + Arrays.toString(args));

    if (args.length == 0) {
      System.err.println("No command provided");
      System.exit(1);
    }

    var cmd = Arrays.copyOfRange(args, 1, args.length);

    switch (args[0]) {
      case "run" -> {
          Parent.run(cmd);
      }
      case "child" -> {
        Child.run(cmd);
      }
      default -> {
        System.out.println("Unknown command: " + args[0]);
        System.exit(1);
      }
    }
  }
}

class Parent {
  public static void run(String[] cmd) {
    System.out.println(
        "Running parent " + Arrays.toString(cmd) + " as " + ProcessHandle.current().pid());

    String javaExe = ProcessHandle.current().info().command().orElse("java");
    String classPath = System.getProperty("java.class.path");
    String mainClass = System.getProperty("sun.java.command").split(" ")[1];

    Stream<String> cmdWithChild =
        Stream.concat(Stream.of(javaExe, "-cp", classPath, mainClass, "child"), Arrays.stream(cmd));

    // Prepend unshare with namespace args for the child process
    List<String> cmdWithUnshare =
        Stream.concat(
                Stream.of("unshare", "--uts", "--pid", "--mount", "--fork", "--mount-proc"),
                cmdWithChild)
            .toList();

    Util.must(() -> new ProcessBuilder(cmdWithUnshare).inheritIO().start().waitFor());
  }
}

class Child {
  public static void run(String[] cmd) {
    System.out.println(
        "Running child " + Arrays.toString(cmd) + " as " + ProcessHandle.current().pid());

    // set the hostname of current child container
    Util.must(() -> new ProcessBuilder("hostname", "container").inheritIO().start().waitFor());

    Util.must(() -> new ProcessBuilder(cmd).inheritIO().start().waitFor());
  }
}

class Util {
  static void must(ThrowableRunnable x) {
    try {
      x.run();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  @FunctionalInterface
  interface ThrowableRunnable {
    void run() throws Exception;
  }
}
