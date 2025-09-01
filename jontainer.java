import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

class jontainer {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("No command provided");
      System.exit(1);
    }

    switch (args[0]) {
      case "run" -> {
        try {
          run(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      default -> {
        System.out.println("Unknown command: " + args[0]);
        System.exit(1);
      }
    }
  }

  private static void run(String[] args) throws Exception {
    System.out.println("Running " + Arrays.toString(args));

    // Prepend unshare -u to create new UTS namespace
    List<String> command = Stream.concat(Stream.of("unshare", "-u"), Arrays.stream(args)).toList();

    new ProcessBuilder(command).inheritIO().start().waitFor();
  }
}
