import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/*
 This small utility walks a directory tree using FileVisitor
 and counts files by extension. It demonstrates usage of:
 - java.nio.file.FileVisitor (via SimpleFileVisitor)
 - switch statements for categorization
 - command-line args for start path and verbosity
 Run: java FileVisitorSwitchCounter [startPath] [quiet|verbose]
*/

public class FileVisitor{
    public static void main(String[] args) throws IOException {
        Path start = Paths.get(".");
        int verbosity = 1;
        if (args.length > 0) start = Paths.get(args[0]);
        if (args.length > 1) {
            switch (args[1].toLowerCase()) {
                case "quiet": verbosity = 0; break;
                case "verbose": verbosity = 2; break;
                default: verbosity = 1;
            }
        }
        if (verbosity > 0) System.out.println("Starting traversal at: " + start.toAbsolutePath());
        ExtensionCountingVisitor visitor = new ExtensionCountingVisitor(verbosity);
        Files.walkFileTree(start, visitor);
        if (verbosity > 0) System.out.println("\nSummary:");
        visitor.printSummary();
    }

    private static class ExtensionCountingVisitor extends SimpleFileVisitor<Path> {
        private final Map<String,Integer> counts = new HashMap<>();
        private final int verbosity;
        ExtensionCountingVisitor(int verbosity){ this.verbosity = verbosity; }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (verbosity >= 2) System.out.println("Entering directory: " + dir);
            counts.merge("directory", 1, Integer::sum);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String name = file.getFileName().toString();
            String ext = getExtension(name);
            switch (ext) {
                case "java":
                case "class":
                    counts.merge("java",1,Integer::sum); break;
                case "txt":
                case "md":
                    counts.merge("text",1,Integer::sum); break;
                case "png":
                case "jpg":
                case "jpeg":
                case "gif":
                    counts.merge("image",1,Integer::sum); break;
                case "":
                    counts.merge("no-extension",1,Integer::sum); break;
                default:
                    counts.merge("other",1,Integer::sum);
            }
            if (verbosity >= 2) System.out.printf("Visited: %s (ext='%s')%n", file, ext);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            System.err.println("Failed to visit file: " + file + " -> " + exc.getMessage());
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) System.err.println("Error after visiting directory: " + dir + " -> " + exc.getMessage());
            if (verbosity >= 2) System.out.println("Leaving directory: " + dir);
            return FileVisitResult.CONTINUE;
        }

        private String getExtension(String filename){
            int idx = filename.lastIndexOf('.');
            if (idx <= 0 || idx == filename.length()-1) return "";
            return filename.substring(idx+1).toLowerCase();
        }

        void printSummary(){
            counts.forEach((k,v)-> System.out.printf("%-12s : %d%n", k, v));
        }
    }
}

/*
 Example: java FileVisitorSwitchCounter /home/user verbose
 Counts common file types and prints a simple summary.
 Modify switch cases to add more categories.
 */
