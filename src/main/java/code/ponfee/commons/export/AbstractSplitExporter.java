package code.ponfee.commons.export;

import code.ponfee.commons.concurrent.MultithreadExecutors;
import code.ponfee.commons.util.Holder;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Export multiple file
 *
 * @author Ponfee
 */
public abstract class AbstractSplitExporter extends AbstractDataExporter<Void> {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final String fileSuffix;
    private final Executor executor;

    public AbstractSplitExporter(int batchSize, String savingFilePathPrefix, 
                                 String fileSuffix, Executor executor) {
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
                // sets a new table and return the last
                Table<Object[]> last = subTable.set(table.copyOfWithoutTbody(Function.identity()));
                String path = buildFilePath(split.incrementAndGet());
                service.submit(splitExporter(last, path), Boolean.TRUE);
                count.set(0); // reset count and sub table
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            String path = buildFilePath(split.incrementAndGet());
            service.submit(splitExporter(subTable.get(), path), Boolean.TRUE);
        }

        if (split.get() > 0) {
            super.nonEmpty();
            MultithreadExecutors.joinDiscard(service, split.get());
        }
    }

    protected abstract AbstractAsyncSplitExporter splitExporter(Table<Object[]> subTable,
                                                                String savingFilePath);

    @Override
    public final Void export() {
        throw new UnsupportedOperationException();
    }

    private String buildFilePath(int fileNo) {
        return savingFilePathPrefix + String.format("%04d", fileNo) + fileSuffix;
    }

    public static abstract class AbstractAsyncSplitExporter implements Runnable {
        private final Table<Object[]> subTable;
        protected final String savingFilePath;

        public AbstractAsyncSplitExporter(Table<Object[]> subTable, String savingFilePath) {
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
