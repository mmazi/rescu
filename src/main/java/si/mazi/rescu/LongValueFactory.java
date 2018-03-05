package si.mazi.rescu;

public class LongValueFactory implements SynchronizedValueFactory<Long> {
    private long i;

    @Override
    public Long createValue() {
        return i++;
    }
}
