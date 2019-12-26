package code.ponfee.commons.export;

import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.util.Holder;

/**
 * Export multiple file
 *
 * @author Ponfee
 */
public abstract class AbstractSplitExporter extends AbstractDataExporter<Void> {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final String fileSuffix;
    private final ExecutorService executor;

    public AbstractSplitExporter(int batchSize, String savingFilePathPrefix, 
                                 String fileSuffix, ExecutorService executor) {
        Preconditions.checkArgument(batchSize > 0);
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
        this.fileSuffix = fileSuffix;
        this.executor = executor;
    }

    @Override
    public final <E> void build(Table<E> table) {
        CompletionService<Boolean> service = new ExecutorCompletionService<>(executor);
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger split = new AtomicInteger(0);
        Holder<Table<Object[]>> subTable = Holder.of(table.copyOfWithoutTbody(Function.identity()));
        rollingTbody(table, (data, i) -> {
            subTable.get().addRow(data);
            if (count.incrementAndGet() == batchSize) {
                super.nonEmpty();
                // sets a new table and return the last
                Table<Object[]> last = subTable.getAndSet(table.copyOfWithoutTbody(Function.identity()));
                String path = buildFilePath(split.incrementAndGet());
                service.submit(splitExporter(last, path), Boolean.TRUE);
                count.set(0); // reset count and sub table
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            super.nonEmpty();
            String path = buildFilePath(split.incrementAndGet());
            service.submit(splitExporter(subTable.get(), path), Boolean.TRUE);
        }

        MultithreadExecutor.joinDiscard(service, split.get());
    }

    protected abstract AsnycSplitExporter splitExporter(Table<Object[]> subTable, 
                                                        String savingFilePath);

    public @Override final Void export() {
        throw new UnsupportedOperationException();
    }

    public @Override final void close() {}

    private String buildFilePath(int fileNo) {
        return savingFilePathPrefix + String.format("%04d", fileNo) + fileSuffix;
    }

    public static abstract class AsnycSplitExporter implements Runnable {
        private final Table<Object[]> subTable;
        protected final String savingFilePath;

        public AsnycSplitExporter(Table<Object[]> subTable, String savingFilePath) {
            this.subTable = subTable;
            this.savingFilePath = savingFilePath;
        }

        @Override
        public final void run()  {
            subTable.toEnd();
            try (AbstractDataExporter<?> exporter = createExporter()) {
                exporter.build(subTable);
                complete(exporter);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected abstract AbstractDataExporter<?> createExporter() throws IOException;

        protected void complete(AbstractDataExporter<?> exporter) {}
    }

}
