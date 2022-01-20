package work.lclpnet.build;

public class ProcessResult {

    public final int exitCode;
    public final String stdout, stderr;

    public ProcessResult(int exitCode, String stdout, String stderr) {
        this.exitCode = exitCode;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}
