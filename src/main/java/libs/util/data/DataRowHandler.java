package libs.util.data;

@FunctionalInterface
public interface DataRowHandler {
    void handle(String[] strings);
}
