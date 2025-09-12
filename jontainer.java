import com.sun.jna.Library;
import com.sun.jna.Native;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
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

    // change to new debian fs
    Util.cmust(() -> c.INSTANCE.chroot("/my-fs/debian-fs"));
    Util.cmust(() -> c.INSTANCE.chdir("/"));

    // Mount proc filesystem
    Util.cmust(() -> c.INSTANCE.mount("proc", "proc", "proc", 0, ""));

    // Processbuilder won't work here - cause it forks the process from the original jvm machine
    // -- i'm guessing here
    Util.cmust(() -> c.INSTANCE.system(String.join(" ", cmd)));

    // unmount proc when finishing
    Util.cmust(() -> c.INSTANCE.umount("/proc", 0));
  }
}

interface c extends Library {
  c INSTANCE = Native.load("c", c.class);

  int MS_BIND = 4096;

  // for testing jna writing to sysout
  int write(int fd, String buf, int count);

  int chroot(String path);

  int chdir(String path);

  int system(String command);

  int mount(String source, String target, String filesystemtype, int mountflags, String data);

  int umount(String target, int flags);
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

  static void cmust(Supplier<Integer> x) {
    var res = x.get();
    if (res < 0) {
      int errno = Native.getLastError();
      System.err.println("c call failed with return code: " + res + ", errno: " + errno);
      System.exit(1);
    }
  }
}
